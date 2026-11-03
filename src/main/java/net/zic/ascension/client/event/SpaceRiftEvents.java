package net.zic.ascension.client.event;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.renderer.SpaceRiftRenderer;
import net.zic.ascension.client.visual.SpaceRiftClientState;
import net.zic.ascension.network.SpaceRiftInteractPayload;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class SpaceRiftEvents {
    private static final float DEFAULT_RADIUS = 3.0F;
    private static final float DEFAULT_DURATION = 10.0F;
    private static final double FRONT_DISTANCE = 6.0D;
    private static final double FRONT_MIN_LIFT = 1.0D;
    private static final double FRONT_GROUND_CLEARANCE = 0.5D;
    private static final double ABOVE_DISTANCE = 15.0D;
    private static final double CLICK_REACH = 6.0D;
    private static final List<PendingShatter> PENDING_SHATTERS = new ArrayList<>();

    private record PendingShatter(Vec3 center, long atNanos) {
    }

    private SpaceRiftEvents() {
    }

    @SubscribeEvent
    public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        SpaceRiftRenderer.renderRifts(
                new Matrix4f(event.getModelViewMatrix()),
                new Matrix4f(event.getLevelRenderState().cameraRenderState.projectionMatrix)
        );
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (PENDING_SHATTERS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        long now = System.nanoTime();
        for (Iterator<PendingShatter> iterator = PENDING_SHATTERS.iterator(); iterator.hasNext(); ) {
            PendingShatter shatter = iterator.next();
            if (now < shatter.atNanos()) {
                continue;
            }
            iterator.remove();
            if (minecraft.level != null) {
                minecraft.level.playLocalSound(shatter.center().x, shatter.center().y, shatter.center().z,
                        SoundEvents.GLASS_BREAK, SoundSource.AMBIENT, 2.0F, 0.55F, false);
            }
        }
    }

    /** Clicking into the hole of your own open rift reaches through it (e.g. the Void Pocket's ender chest). */
    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem() && !event.isAttack()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        Vec3 eye = player.getEyePosition();
        SpaceRiftClientState.Pick pick = SpaceRiftClientState.get()
                .pickInteractable(eye, player.getLookAngle(), CLICK_REACH);
        if (pick == null) {
            return;
        }
        // Anything solid between the player and the rift gets the click instead.
        HitResult hit = minecraft.hitResult;
        if (hit != null && hit.getType() != HitResult.Type.MISS && hit.getLocation().distanceTo(eye) < pick.distance()) {
            return;
        }
        ClientPacketDistributor.sendToServer(new SpaceRiftInteractPayload(pick.id()));
        event.setSwingHand(true);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        PENDING_SHATTERS.clear();
        SpaceRiftClientState.get().clear();
    }

    // Client-only preview command.
    // Usage: /rift [front|above] [radius] [seconds]. Without a placement the rift hangs above and ahead of you, tilted toward you.
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = withSizeArguments(Commands.literal("rift"), Placement.ANGLED);
        root.then(withSizeArguments(Commands.literal("front"), Placement.FRONT));
        root.then(withSizeArguments(Commands.literal("above"), Placement.ABOVE));
        event.getDispatcher().register(root);
    }

    private enum Placement {
        /** Above and ahead of the player, tilted to face them. */
        ANGLED,
        /** Upright, directly in front of the player at eye height. */
        FRONT,
        /** Flat, directly overhead, facing down. */
        ABOVE
    }

    private static LiteralArgumentBuilder<CommandSourceStack> withSizeArguments(
            LiteralArgumentBuilder<CommandSourceStack> node, Placement placement) {
        return node
                .executes(_ -> openRift(placement, DEFAULT_RADIUS, DEFAULT_DURATION))
                .then(Commands.argument("radius", FloatArgumentType.floatArg(0.5F, 32.0F))
                        .executes(context -> openRift(placement, radius(context), DEFAULT_DURATION))
                        .then(Commands.argument("seconds", FloatArgumentType.floatArg(3.0F, 120.0F))
                                .executes(context -> openRift(placement, radius(context),
                                        FloatArgumentType.getFloat(context, "seconds")))));
    }

    private static float radius(CommandContext<CommandSourceStack> context) {
        return FloatArgumentType.getFloat(context, "radius");
    }

    private static int openRift(Placement placement, float radius, float seconds) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return 0;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        forward = forward.lengthSqr() > 1.0E-4D ? forward.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

        Vec3 center;
        Vec3 facing;
        Vec3 upHint;
        switch (placement) {
            case FRONT -> {
                // Lift at least a block, and enough that the bottom of the hole stays above the player's feet.
                double eyeHeight = eye.y - player.getY();
                double lift = Math.max(FRONT_MIN_LIFT, radius + FRONT_GROUND_CLEARANCE - eyeHeight);
                center = eye.add(forward.scale(FRONT_DISTANCE)).add(0.0D, lift, 0.0D);
                facing = forward.reverse();
                upHint = worldUp;
            }
            case ABOVE -> {
                center = eye.add(0.0D, ABOVE_DISTANCE, 0.0D);
                facing = worldUp.reverse();
                upHint = forward;
            }
            default -> {
                center = eye.add(forward.scale(radius * 1.6D + 2.0D)).add(0.0D, radius * 0.9D + 1.5D, 0.0D);
                facing = eye.subtract(center);
                upHint = worldUp;
            }
        }
        SpaceRiftClientState.get().open(minecraft.level.dimension().identifier(), center,
                facing, upHint, radius, seconds);
        playOpenSounds(center);
        return 1;
    }

    /** A low drain while space collapses, then the glass shatters at the snap. */
    public static void playOpenSounds(Vec3 center) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        minecraft.level.playLocalSound(center.x, center.y, center.z, SoundEvents.BEACON_DEACTIVATE,
                SoundSource.AMBIENT, 2.0F, 0.5F, false);
        PENDING_SHATTERS.add(new PendingShatter(center,
                System.nanoTime() + (long) (SpaceRiftClientState.COLLAPSE_SECONDS * 1.0E9D)));
    }
}
