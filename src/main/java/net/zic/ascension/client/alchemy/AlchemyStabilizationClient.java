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
    private static BlockPos predictedPos;
    private static long predictedSeed;
    private static int predictedMask;
    private static final long[] predictedUntil = new long[AlchemyStabilization.MAX_TARGETS];
    private static final int[] predictedPoints = new int[AlchemyStabilization.MAX_TARGETS];
    private static String feedback;
    private static long feedbackUntil;

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
        AlchemyStabilization.Settings settings = furnace.stabilizationSettings();
        int total = settings.targetCount();
        syncPrediction(minecraft, furnace);
        int completed = Integer.bitCount(furnace.stabilizationHitMask() | predictedMask);
        int earned = furnace.stabilizationScore();
        for (int i = 0; i < total; i++) {
            if ((predictedMask & (1 << i)) != 0) earned += predictedPoints[i];
        }
        int score = total == 0 ? 0 : Math.round((float) earned / total);
        long elapsed = minecraft.level.getGameTime() - furnace.stabilizationStartTick();
        String label = elapsed < settings.prepareTicks()
                ? "Stabilize  |  Get ready"
                : "Stabilize " + score + "%  |  " + completed + "/" + total;
        int textWidth = minecraft.font.width(label);
        int boxWidth = Math.max(132, textWidth + 14);
        int left = (width - boxWidth) / 2;
        int top = height - 73;
        graphics.fill(left, top, left + boxWidth, top + 20, 0xB22D2115);
        graphics.fill(left + 1, top + 1, left + boxWidth - 1, top + 2, 0xB7B98A45);
        graphics.text(minecraft.font, Component.literal(label), (width - textWidth) / 2, top + 4, 0xFFFBE1A5, false);
        int barLeft = left + 7;
        int barRight = left + boxWidth - 7;
        graphics.fill(barLeft, top + 15, barRight, top + 17, 0x88624C32);
        graphics.fill(barLeft, top + 15, barLeft + (barRight - barLeft) * score / 100,
                top + 17, 0xFFF5B54C);
        if (feedback != null && minecraft.level.getGameTime() < feedbackUntil) {
            graphics.text(minecraft.font, Component.literal(feedback),
                    (width - minecraft.font.width(feedback)) / 2, top - 12, 0xFFFFC16D, true);
        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            activePos = null;
            resetPrediction();
            return;
        }
        if (activePos == null || minecraft.level.getGameTime() % 5L == 0L
                || !(minecraft.level.getBlockEntity(activePos) instanceof AlchemyFurnaceBlockEntity furnace)
                || furnace.phase() != AlchemyFurnaceBlockEntity.Phase.STABILIZATION) {
            activePos = nearestFurnace(minecraft);
        }
        AlchemyFurnaceBlockEntity furnace = activeFurnace(minecraft);
        syncPrediction(minecraft, furnace);
        if (furnace != null && owner(furnace, minecraft.player)
                && minecraft.screen instanceof AlchemyFurnaceScreen) {
            minecraft.screen.onClose();
        }
    }

    private static void resetPrediction() {
        predictedPos = null;
        predictedMask = 0;
        feedback = null;
        feedbackUntil = 0L;
    }

    private static void syncPrediction(Minecraft minecraft, AlchemyFurnaceBlockEntity furnace) {
        if (furnace == null) {
            resetPrediction();
            return;
        }
        if (predictedPos == null) return;
        if (!predictedPos.equals(activePos) || predictedSeed != furnace.stabilizationSeed()) {
            resetPrediction();
            return;
        }
        int confirmed = furnace.stabilizationHitMask();
        for (int i = 0; i < AlchemyStabilization.MAX_TARGETS; i++) {
            int bit = 1 << i;
            if ((predictedMask & bit) != 0 && ((confirmed & bit) != 0
                    || minecraft.level.getGameTime() >= predictedUntil[i])) {
                predictedMask &= ~bit;
            }
        }
    }

    public static void onHitResult(BlockPos pos, long seed, int index, boolean accepted) {
        if (predictedPos == null || !predictedPos.equals(pos) || predictedSeed != seed
                || index < 0 || index >= AlchemyStabilization.MAX_TARGETS) return;
        int bit = 1 << index;
        if (accepted) {
            predictedMask |= bit;
            predictedUntil[index] = Long.MAX_VALUE;
        } else {
            predictedMask &= ~bit;
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                feedback = "Hit not registered";
                feedbackUntil = minecraft.level.getGameTime() + 14L;
            }
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
        syncPrediction(minecraft, furnace);
        double best = Double.MAX_VALUE;
        int selected = -1;
        int tick = (int) (minecraft.level.getGameTime() - furnace.stabilizationStartTick());
        AlchemyStabilization.Settings settings = furnace.stabilizationSettings();
        for (int i = 0; i < settings.targetCount(); i++) {
            if (((furnace.stabilizationHitMask() | predictedMask) & (1 << i)) != 0) continue;
            int remaining = settings.hitTick(i) - tick;
            if (remaining > settings.approachTicks() || remaining < -settings.goodWindow()) continue;
            Vec3 point = AlchemyStabilization.position(activePos, furnace.getBlockState(),
                    AlchemyStabilization.targetPosition(settings, furnace.stabilizationSeed(), i));
            Vec3 ray = point.subtract(eye);
            double distance = ray.length();
            if (distance > AlchemyStabilization.INTERACTION_REACH || distance <= 0.15D) continue;
            double miss = ray.cross(look).length();
            if (ray.dot(look) <= 0.0D || miss > AlchemyStabilization.TARGET_HIT_RADIUS || miss >= best) continue;
            best = miss;
            selected = i;
        }
        if (selected < 0 || lastHitTick == minecraft.level.getGameTime() && lastHitIndex == selected) return;
        lastHitIndex = selected;
        lastHitTick = minecraft.level.getGameTime();
        int points = AlchemyStabilization.points(settings, selected, tick);
        if (points <= 0) return;
        predictedPos = activePos;
        predictedSeed = furnace.stabilizationSeed();
        predictedMask |= 1 << selected;
        predictedPoints[selected] = points;
        predictedUntil[selected] = minecraft.level.getGameTime() + 18L;
        feedback = points == 100 ? "Perfect" : points == 75 ? "Good" : "Early";
        feedbackUntil = minecraft.level.getGameTime() + 12L;
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
        syncPrediction(minecraft, furnace);
        int hitMask = furnace.stabilizationHitMask() | predictedMask;
        long seed = furnace.stabilizationSeed();
        AfterShaderPackDraws.drawOrDefer(event.getPoseStack(), RenderTypes.lightning(), (pose, vertices) -> {
            for (int i = 0; i < settings.targetCount(); i++) {
                if ((hitMask & (1 << i)) != 0) continue;
                float remaining = settings.hitTick(i) - tick;
                if (remaining > settings.approachTicks() || remaining < -settings.goodWindow()) continue;
                Vec3 world = AlchemyStabilization.position(pos, furnace.getBlockState(),
                        AlchemyStabilization.targetPosition(settings, seed, i));
                drawTarget(pose, vertices, world.subtract(camera), camera.subtract(world), remaining, tick, settings);
            }
        });
    }

    private static void drawTarget(PoseStack.Pose pose, VertexConsumer vertices, Vec3 point, Vec3 toCamera,
                                   float remaining, float tick, AlchemyStabilization.Settings settings) {
        if (toCamera.lengthSqr() < 0.001D) return;
        Vec3 normal = toCamera.normalize();
        Vec3 upHint = Math.abs(normal.y) > 0.98D
                ? new Vec3(0.0D, 0.0D, 1.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = upHint.cross(normal).normalize();
        if (right.lengthSqr() < 0.001D) return;
        Vec3 up = normal.cross(right).normalize();
        float progress = Math.max(0.0F, Math.min(1.0F, remaining / settings.approachTicks()));
        float pulse = 0.5F + 0.5F * (float) Math.sin(tick * 0.2F);
        double radius = 0.074D + 0.175D * progress;
        int ringColor = Math.abs(remaining) <= settings.perfectWindow() ? 0xFFFFE7A2
                : Math.abs(remaining) <= settings.goodWindow() ? 0xFFFFC566 : 0xDFFF933A;
        addDisc(pose, vertices, point, right, up, 0.066D + 0.006D * pulse, 0x6EFFA536);
        addDisc(pose, vertices, point, right, up, 0.047D, 0xFFF8CB70);
        addDisc(pose, vertices, point, right, up, 0.021D, 0xFFFFF2C4);
        addRing(pose, vertices, point, right, up, 0.065D, 0.073D, 0xCFFFD185);
        addRing(pose, vertices, point, right, up, radius - 0.010D, radius, ringColor);
        addArc(pose, vertices, point, right, up, 0.092D, 0.101D,
                tick * 0.105D, Math.PI * 0.64D, 0xBFFFD47E);
        addArc(pose, vertices, point, right, up, 0.092D, 0.101D,
                tick * 0.105D + Math.PI, Math.PI * 0.64D, 0xBFFFD47E);
        addArc(pose, vertices, point, right, up, radius + 0.018D, radius + 0.025D,
                -tick * 0.065D, Math.PI * 0.43D, 0x9FFF9F32);
    }

    private static void addArc(PoseStack.Pose pose, VertexConsumer vertices, Vec3 center,
                               Vec3 right, Vec3 up, double inner, double outer,
                               double start, double sweep, int color) {
        for (int i = 0; i < 12; i++) {
            double a = start + sweep * i / 12.0D;
            double b = start + sweep * (i + 1) / 12.0D;
            addVertex(pose, vertices, center, right, up, Math.cos(a) * inner, Math.sin(a) * inner, color);
            addVertex(pose, vertices, center, right, up, Math.cos(a) * outer, Math.sin(a) * outer, color);
            addVertex(pose, vertices, center, right, up, Math.cos(b) * outer, Math.sin(b) * outer, color);
            addVertex(pose, vertices, center, right, up, Math.cos(b) * inner, Math.sin(b) * inner, color);
        }
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
        resetPrediction();
    }
}
