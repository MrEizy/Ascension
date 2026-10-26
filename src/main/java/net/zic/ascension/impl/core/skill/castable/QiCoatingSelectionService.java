package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoating;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingChannel;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingElement;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingSelection;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.skill.passive.PassiveSkill;
import net.zic.ascension.impl.core.skill.passive.PassiveSkillService;
import net.zic.ascension.impl.runtime.object.PersistentRuntimeVisuals;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server side of the Qi Coating skills: which element each channel shows (picked in the coating wheel, stored in
 * CoreAttachments.QI_COATING_SELECTION), and switching between elements while a coating is on. A switch never shows two
 * coatings at once: the old one is taken off, and the new one only goes on once the old one has had time to shrink away.
 */
public final class QiCoatingSelectionService {
    // How often players with a coating on are checked for still having its element's path.
    private static final int PATH_CHECK_INTERVAL = 20;

    // The element each player's channel is showing right now, and through which passive skill.
    private static final Map<Key, Shown> SHOWN = new ConcurrentHashMap<>();
    // Switches waiting for the old coating to shrink away. One per channel: a newer pick replaces it.
    private static final Map<Key, Shown> PENDING = new ConcurrentHashMap<>();
    private static final Map<Key, Long> PENDING_DUE = new ConcurrentHashMap<>();

    private QiCoatingSelectionService() {
    }

    /**
     * The elements the source can pick on a channel: those whose path it has (or that need none) and that have a look for the channel, in wheel order.
     */
    public static List<Identifier> available(OriginSource source, QiCoatingChannel channel, RegistryAccess access) {
        if (source == null || access == null) {
            return List.of();
        }
        return CoreRegistries.QI_COATING_ELEMENT_REGISTRY.get(access).entrySet().stream()
                .filter(entry -> entry.getValue().visual(channel).isPresent())
                .filter(entry -> entry.getValue().path().map(path -> AscensionOriginSourceHelper.hasPath(source, path)).orElse(true))
                .sorted(Comparator.comparingInt((Map.Entry<ResourceKey<QiCoatingElement>, QiCoatingElement> entry) -> entry.getValue().order())
                        .thenComparing(entry -> entry.getKey().identifier().toString()))
                .map(entry -> entry.getKey().identifier())
                .toList();
    }

    /**
     * The channel a castable skill switches, if it toggles a passive whose on_enable shows a Qi Coating
     * (ascension:qi_coating_visual). This is how the coating wheel knows the selected hotbar skill is a Qi Coating skill.
     */
    public static Optional<QiCoatingChannel> channelOfCastable(Identifier skillId, RegistryAccess access) {
        if (!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, access) instanceof ActiveSkill active)) {
            return Optional.empty();
        }
        for (SkillAction action : active.actions()) {
            if (action instanceof SkillActions.TogglePassive toggle
                    && CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, toggle.skill(), access) instanceof PassiveSkill passive) {
                for (SkillAction enableAction : passive.enableActions()) {
                    if (enableAction instanceof SkillActions.QiCoatingVisual(QiCoatingChannel channel)) {
                        return Optional.of(channel);
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * The element the entity's channel should show: the stored pick if it's still available, otherwise the first available
     * element (which becomes the stored pick). Empty if no element is available.
     */
    public static Optional<Identifier> resolve(LivingEntity entity, QiCoatingChannel channel) {
        List<Identifier> available = available(AscensionOriginSourceHelper.getEntitySource(entity), channel, entity.registryAccess());
        if (available.isEmpty()) {
            return Optional.empty();
        }
        QiCoatingSelection selection = entity.getData(CoreAttachments.QI_COATING_SELECTION);
        Optional<Identifier> stored = selection.get(channel).filter(available::contains);
        if (stored.isPresent()) {
            return stored;
        }
        Identifier first = available.getFirst();
        selection.set(channel, first);
        entity.syncData(CoreAttachments.QI_COATING_SELECTION);
        return Optional.of(first);
    }

    /**
     * Puts the channel's resolved element on the caster, under the passive skill in the context (ascension:qi_coating_visual).
     */
    public static void show(SkillActionContext context, QiCoatingChannel channel) {
        Key key = new Key(context.caster().getUUID(), channel);
        PENDING.remove(key);
        PENDING_DUE.remove(key);
        resolve(context.caster(), channel).ifPresentOrElse(
                element -> showElement(context, channel, element),
                () -> clear(context, channel)
        );
    }

    /**
     * Takes the channel's coating off the caster, and forgets any switch waiting to happen.
     */
    public static void clear(SkillActionContext context, QiCoatingChannel channel) {
        Key key = new Key(context.caster().getUUID(), channel);
        PENDING.remove(key);
        PENDING_DUE.remove(key);
        SHOWN.remove(key);
        PersistentRuntimeVisuals.remove(context, channel.visualKey());
    }

    /**
     * The player picked an element in the coating wheel. It's stored either way; if the channel's coating is on and showing a
     * different element, the old one comes off now and the new one goes on once the old one has shrunk away.
     */
    public static void select(ServerPlayer player, QiCoatingChannel channel, Identifier element) {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (!available(source, channel, player.registryAccess()).contains(element)) {
            return;
        }
        player.getData(CoreAttachments.QI_COATING_SELECTION).set(channel, element);
        player.syncData(CoreAttachments.QI_COATING_SELECTION);

        Key key = new Key(player.getUUID(), channel);
        Shown pending = PENDING.get(key);
        if (pending != null) {
            // Still waiting for the last coating to shrink away: just change what comes on after.
            PENDING.put(key, new Shown(element, pending.skill()));
            return;
        }
        Shown shown = SHOWN.get(key);
        if (shown == null || shown.element().equals(element)) {
            return;
        }
        ServerLevel level = player.level();
        SkillActionContext context = new SkillActionContext(level, player, shown.skill(), null, null, 0.0D, Map.of());
        long shrinkTicks = Math.max(1L, (growMillis(shown.element(), channel, player.registryAccess()) + 49L) / 50L);
        SHOWN.remove(key);
        PersistentRuntimeVisuals.remove(context, channel.visualKey());
        PENDING.put(key, new Shown(element, shown.skill()));
        PENDING_DUE.put(key, level.getGameTime() + shrinkTicks);
    }

    /**
     * What the player's channel is showing right now, if its coating is on (and not between two elements while switching).
     */
    public static Optional<Shown> shown(UUID playerId, QiCoatingChannel channel) {
        return Optional.ofNullable(SHOWN.get(new Key(playerId, channel)));
    }

    private static void showElement(SkillActionContext context, QiCoatingChannel channel, Identifier elementId) {
        QiCoatingElement element = CoreRegistries.safeAccess(CoreRegistries.QI_COATING_ELEMENT_REGISTRY, elementId, context.level().registryAccess());
        Identifier visual = element == null ? null : element.visual(channel).orElse(null);
        if (visual == null) {
            return;
        }
        LivingEntity caster = context.caster();
        PersistentRuntimeVisuals.apply(context, channel.visualKey(), visual, caster, Vec3.ZERO, false,
                1.0F, 0.0D, 0xFFFFFFFF, 0xFFFFFFFF);
        SHOWN.put(new Key(caster.getUUID(), channel), new Shown(elementId, context.skill()));
    }

    private static long growMillis(Identifier elementId, QiCoatingChannel channel, RegistryAccess access) {
        QiCoatingElement element = CoreRegistries.safeAccess(CoreRegistries.QI_COATING_ELEMENT_REGISTRY, elementId, access);
        QiCoating coating = element == null ? null : element.visual(channel).map(visual -> QiCoating.fromVisual(visual, access)).orElse(null);
        return coating == null ? 0L : coating.growMillis();
    }

    private static void tick(ServerPlayer player) {
        ServerLevel level = player.level();
        long now = level.getGameTime();
        for (QiCoatingChannel channel : QiCoatingChannel.values()) {
            Key key = new Key(player.getUUID(), channel);
            Long due = PENDING_DUE.get(key);
            if (due != null && now >= due) {
                Shown pending = PENDING.remove(key);
                PENDING_DUE.remove(key);
                if (pending != null) {
                    showElement(new SkillActionContext(level, player, pending.skill(), null, null, 0.0D, Map.of()), channel, pending.element());
                }
            }
            if (now % PATH_CHECK_INTERVAL == 0) {
                checkPath(player, key, channel);
            }
        }
    }

    /**
     * Turns the channel's coating off if the player no longer has a path for any element it can show.
     */
    private static void checkPath(ServerPlayer player, Key key, QiCoatingChannel channel) {
        Shown current = SHOWN.containsKey(key) ? SHOWN.get(key) : PENDING.get(key);
        if (current == null) {
            return;
        }
        List<Identifier> available = available(AscensionOriginSourceHelper.getEntitySource(player), channel, player.registryAccess());
        if (available.contains(current.element())) {
            return;
        }
        if (available.isEmpty()) {
            PassiveSkillService.setEnabled(player, current.skill(), false);
            return;
        }
        // Lost this element's path but others are left: fall back to the first one.
        Shown pending = PENDING.get(key);
        Identifier fallback = resolve(player, channel).orElse(null);
        if (pending != null) {
            PENDING.put(key, new Shown(fallback, pending.skill()));
        } else if (fallback != null) {
            select(player, channel, fallback);
        }
    }

    private static void forget(UUID playerId) {
        for (QiCoatingChannel channel : QiCoatingChannel.values()) {
            Key key = new Key(playerId, channel);
            SHOWN.remove(key);
            PENDING.remove(key);
            PENDING_DUE.remove(key);
        }
    }

    private record Key(UUID playerId, QiCoatingChannel channel) {
    }

    /**
     * The element a channel is showing and the passive skill showing it.
     */
    public record Shown(Identifier element, Identifier skill) {
    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static final class ServerEvents {
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                tick(serverPlayer);
            }
        }

        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide()) {
                forget(player.getUUID());
            }
        }
    }
}
