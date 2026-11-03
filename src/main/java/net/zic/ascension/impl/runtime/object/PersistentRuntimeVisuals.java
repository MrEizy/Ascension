package net.zic.ascension.impl.runtime.object;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.zic.ascension.AscensionCraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class PersistentRuntimeVisuals {
    private static final Map<Key, RuntimeVisualState> ACTIVE = new ConcurrentHashMap<>();

    private PersistentRuntimeVisuals() {
    }

    public static void apply(
            SkillActionContext context, Identifier key, Identifier visual, LivingEntity attached, Vec3 offset,
            boolean rotate, float scale, double spin, int tint, int secondaryTint
    ) {
        remove(context, key);
        SkillDefinitions.Resolved<RuntimeVisualDefinition> resolved = SkillDefinitions.visual(context, visual);
        if (resolved == null || resolved.value() == null || attached == null) {
            return;
        }
        UUID runtimeId = UUID.randomUUID();
        RuntimeVisualState state = new RuntimeVisualState(
                runtimeId, resolved.id(), attached.getUUID(), attached.getBoundingBox().getCenter(), offset, List.of(), List.of(),
                context.level().getGameTime() + 630720000L, 0, RuntimeVisualState.OWNER_RELATIVE | (rotate ? RuntimeVisualState.ROTATE_WITH_OWNER : 0),
                0.0F, runtimeId.getMostSignificantBits(), 0.0D, 0.0D, scale, spin, tint, secondaryTint, null
        );
        ACTIVE.put(new Key(context.caster().getUUID(), context.skill(), key), state);
        RuntimeVisualSync.spawn(context.level(), state);
    }

    public static void remove(SkillActionContext context, Identifier key) {
        RuntimeVisualState state = ACTIVE.remove(new Key(context.caster().getUUID(), context.skill(), key));
        if (state != null) {
            RuntimeVisualSync.remove(context.level(), state);
        }
    }

    /**
     * The ids of the persistent visuals on the entity, such as a Qi Coating's.
     */
    public static List<Identifier> visualsOn(UUID owner) {
        List<Identifier> visuals = new ArrayList<>();
        for (Map.Entry<Key, RuntimeVisualState> entry : ACTIVE.entrySet()) {
            if (entry.getKey().owner().equals(owner)) {
                visuals.add(entry.getValue().visual());
            }
        }
        return visuals;
    }

    /**
     * Visuals are only sent to players in range when they start, so anyone who comes into range of the owner later gets them
     * here.
     */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer) {
            sendOwnedBy(viewer, event.getTarget().getUUID());
        }
    }

    /**
     * Players don't track themselves, so the owner gets its own visuals again after anything that rebuilds its client world.
     */
    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendOwnedBy(player, player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendOwnedBy(player, player.getUUID());
        }
    }

    private static void sendOwnedBy(ServerPlayer viewer, UUID owner) {
        ACTIVE.forEach((key, state) -> {
            if (key.owner().equals(owner)) {
                RuntimeVisualSync.spawnFor(viewer, state);
            }
        });
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE.clear();
    }

    private record Key(UUID owner, Identifier skill, Identifier key) {
    }
}
