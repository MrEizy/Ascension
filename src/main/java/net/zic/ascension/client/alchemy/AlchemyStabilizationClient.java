package net.zic.ascension.client.alchemy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyStabilization;
import net.zic.ascension.client.renderer.AfterShaderPackDraws;
import net.zic.ascension.common.blocks.alchemy.AlchemyFurnaceBlock;
import net.zic.ascension.common.blocks.entity.AlchemyFurnaceBlockEntity;
import net.zic.ascension.common.gui.menus.alchemy.AlchemyFurnaceScreen;
import net.zic.ascension.network.alchemy.ServerboundStabilizationHitPacket;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class AlchemyStabilizationClient {
    private static final Identifier HUD_ID = AscensionCraft.prefix("alchemy_stabilization");
    private static BlockPos activePos;
    private static long lastHitTick = Long.MIN_VALUE;
    private static int lastHitIndex = -1;

    private AlchemyStabilizationClient() {
    }

    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, HUD_ID, AlchemyStabilizationClient::renderHud);
    }

    private static void renderHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.screen != null
                || minecraft.options.hideGui || minecraft.player.isSpectator()) return;
        AlchemyFurnaceBlockEntity furnace = activeFurnace(minecraft);
        if (furnace == null || !owner(furnace, minecraft.player)) return;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int total = furnace.stabilizationSettings().targetCount();
        int completed = Integer.bitCount(furnace.stabilizationHitMask());
        int score = total == 0 ? 0 : Math.round(100.0F * furnace.stabilizationScore() / (100.0F * total));
        String label = "Stabilization " + score + "%  |  " + completed + "/" + total;
        int left = (width - minecraft.font.width(label)) / 2;
        int y = height / 2 + 28;
        graphics.fill(left - 6, y - 3, left + minecraft.font.width(label) + 6, y + 12, 0xB51B2730);
        graphics.text(minecraft.font, Component.literal(label), left, y, 0xFFDFF5FF, false);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            activePos = null;
            return;
        }
        if (activePos == null || minecraft.level.getGameTime() % 5L == 0L
                || !(minecraft.level.getBlockEntity(activePos) instanceof AlchemyFurnaceBlockEntity furnace)
                || furnace.phase() != AlchemyFurnaceBlockEntity.Phase.STABILIZATION) {
            activePos = nearestFurnace(minecraft);
        }
        AlchemyFurnaceBlockEntity furnace = activeFurnace(minecraft);
        if (furnace != null && owner(furnace, minecraft.player)
                && minecraft.screen instanceof AlchemyFurnaceScreen) {
            minecraft.screen.onClose();
        }
    }

    private static BlockPos nearestFurnace(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        BlockPos center = player.blockPosition();
        BlockPos result = null;
        double best = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -5; x <= 5; x++) {
            for (int y = -2; y <= 3; y++) {
                for (int z = -5; z <= 5; z++) {
                    cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (!(minecraft.level.getBlockState(cursor).getBlock() instanceof AlchemyFurnaceBlock)
                            || !(minecraft.level.getBlockEntity(cursor) instanceof AlchemyFurnaceBlockEntity furnace)
                            || furnace.phase() != AlchemyFurnaceBlockEntity.Phase.STABILIZATION) continue;
                    double distance = player.distanceToSqr(cursor.getX() + 0.5D,
                            cursor.getY() + 0.6D, cursor.getZ() + 0.5D);
                    if (distance > 64.0D) continue;
                    if (owner(furnace, player)) distance -= 64.0D;
                    if (distance < best) {
                        best = distance;
                        result = cursor.immutable();
                    }
                }
            }
        }
        return result;
    }

    private static AlchemyFurnaceBlockEntity activeFurnace(Minecraft minecraft) {
        return activePos != null && minecraft.level != null
                && minecraft.level.getBlockEntity(activePos) instanceof AlchemyFurnaceBlockEntity furnace
                && furnace.phase() == AlchemyFurnaceBlockEntity.Phase.STABILIZATION ? furnace : null;
    }

    private static boolean owner(AlchemyFurnaceBlockEntity furnace, LocalPlayer player) {
        return furnace.alchemistId() != null && furnace.alchemistId().equals(player.getUUID());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || event.isCanceled()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.screen != null) return;
        AlchemyFurnaceBlockEntity furnace = activeFurnace(minecraft);
        if (furnace == null || !owner(furnace, minecraft.player)) return;
        Vec3 eye = minecraft.player.getEyePosition();
        Vec3 look = minecraft.player.getLookAngle();
        Vec3 towardFurnace = Vec3.atCenterOf(activePos).subtract(eye);
        double range = towardFurnace.length();
        if (range > AlchemyStabilization.INTERACTION_REACH + 0.5D || range < 0.15D
                || towardFurnace.normalize().dot(look) < Math.cos(Math.atan2(0.95D, range))) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        double best = Double.MAX_VALUE;
        int selected = -1;
        int tick = (int) (minecraft.level.getGameTime() - furnace.stabilizationStartTick());
        AlchemyStabilization.Settings settings = furnace.stabilizationSettings();
        for (int i = 0; i < settings.targetCount(); i++) {
            if ((furnace.stabilizationHitMask() & (1 << i)) != 0) continue;
            int delta = Math.abs(tick - settings.hitTick(i));
            if (delta > settings.goodWindow() + 1) continue;
            Vec3 point = AlchemyStabilization.position(activePos, furnace.getBlockState(),
                    AlchemyStabilization.targetPosition(settings, furnace.stabilizationSeed(), i));
            Vec3 ray = point.subtract(eye);
            double distance = ray.length();
            if (distance > AlchemyStabilization.INTERACTION_REACH || distance <= 0.15D) continue;
            double miss = ray.cross(look).length();
            if (ray.dot(look) <= 0.0D || miss > 0.15D || miss >= best) continue;
            best = miss;
            selected = i;
        }
        if (selected < 0 || lastHitTick == minecraft.level.getGameTime() && lastHitIndex == selected) return;
        lastHitIndex = selected;
        lastHitTick = minecraft.level.getGameTime();
        ClientPacketDistributor.sendToServer(new ServerboundStabilizationHitPacket(activePos, selected, tick));
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;
        AlchemyFurnaceBlockEntity furnace = activeFurnace(minecraft);
        if (furnace == null) return;
        AlchemyStabilization.Settings settings = furnace.stabilizationSettings();
        float tick = minecraft.level.getGameTime()
                + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true) - furnace.stabilizationStartTick();
        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        BlockPos pos = activePos;
        int hitMask = furnace.stabilizationHitMask();
        long seed = furnace.stabilizationSeed();
        AfterShaderPackDraws.drawOrDefer(event.getPoseStack(), RenderTypes.lightning(), (pose, vertices) -> {
            for (int i = 0; i < settings.targetCount(); i++) {
                if ((hitMask & (1 << i)) != 0) continue;
                float remaining = settings.hitTick(i) - tick;
                if (remaining > settings.approachTicks() || remaining < -settings.goodWindow()) continue;
                Vec3 world = AlchemyStabilization.position(pos, furnace.getBlockState(),
                        AlchemyStabilization.targetPosition(settings, seed, i));
                drawTarget(pose, vertices, world.subtract(camera), camera.subtract(world), remaining, settings);
            }
        });
    }

    private static void drawTarget(PoseStack.Pose pose, VertexConsumer vertices, Vec3 point, Vec3 toCamera,
                                   float remaining, AlchemyStabilization.Settings settings) {
        if (toCamera.lengthSqr() < 0.001D) return;
        Vec3 normal = toCamera.normalize();
        Vec3 upHint = Math.abs(normal.y) > 0.98D
                ? new Vec3(0.0D, 0.0D, 1.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = upHint.cross(normal).normalize();
        if (right.lengthSqr() < 0.001D) return;
        Vec3 up = normal.cross(right).normalize();
        float progress = Math.max(0.0F, Math.min(1.0F, remaining / settings.approachTicks()));
        double radius = 0.052D + 0.135D * progress;
        int ringColor = Math.abs(remaining) <= settings.perfectWindow() ? 0xEFFFF0AB : 0xDFA3EDFF;
        addDisc(pose, vertices, point, right, up, 0.033D, 0xE7C8F6FF);
        addRing(pose, vertices, point, right, up, 0.045D, 0.052D, 0xA456B7F1);
        addRing(pose, vertices, point, right, up, radius - 0.007D, radius, ringColor);
    }

    private static void addRing(PoseStack.Pose pose, VertexConsumer vertices, Vec3 center,
                                Vec3 right, Vec3 up, double inner, double outer, int color) {
        for (int i = 0; i < 24; i++) {
            double a = i * Math.PI / 12.0D;
            double b = (i + 1) * Math.PI / 12.0D;
            addVertex(pose, vertices, center, right, up, Math.cos(a) * inner, Math.sin(a) * inner, color);
            addVertex(pose, vertices, center, right, up, Math.cos(a) * outer, Math.sin(a) * outer, color);
            addVertex(pose, vertices, center, right, up, Math.cos(b) * outer, Math.sin(b) * outer, color);
            addVertex(pose, vertices, center, right, up, Math.cos(b) * inner, Math.sin(b) * inner, color);
        }
    }

    private static void addDisc(PoseStack.Pose pose, VertexConsumer vertices, Vec3 center,
                                Vec3 right, Vec3 up, double radius, int color) {
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6.0D;
            double b = (i + 1) * Math.PI / 6.0D;
            addVertex(pose, vertices, center, right, up, 0.0D, 0.0D, color);
            addVertex(pose, vertices, center, right, up, Math.cos(a) * radius, Math.sin(a) * radius, color);
            addVertex(pose, vertices, center, right, up, Math.cos(b) * radius, Math.sin(b) * radius, color);
            addVertex(pose, vertices, center, right, up, Math.cos(b) * radius, Math.sin(b) * radius, color);
        }
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer vertices, Vec3 center,
                                  Vec3 right, Vec3 up, double x, double y, int color) {
        Vec3 point = center.add(right.scale(x)).add(up.scale(y));
        vertices.addVertex(pose, (float) point.x(), (float) point.y(), (float) point.z()).setColor(color);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        activePos = null;
        lastHitIndex = -1;
        lastHitTick = Long.MIN_VALUE;
    }
}
