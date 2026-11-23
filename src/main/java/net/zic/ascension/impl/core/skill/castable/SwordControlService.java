package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.event.skill.SkillCastEvent;
import net.zic.ascension.common.entities.AscEntities;
import net.zic.ascension.common.entities.weapons.ControlledSwordEntity;
import net.zic.ascension.common.util.ModTags;
import net.zic.ascension.skill_casting.SkillCaster;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class SwordControlService {
    public static final Identifier SKILL = AscensionCraft.prefix("castable/sword_control");
    public static final Identifier TWIN_SKILL = AscensionCraft.prefix("castable/twin_sword_control");
    private static final Map<UUID, SwordGroup> ACTIVE = new ConcurrentHashMap<>();

    private SwordControlService() {
    }

    public static boolean isControlSkill(Identifier skill) {
        return SKILL.equals(skill) || TWIN_SKILL.equals(skill);
    }

    public static void pulse(LivingEntity caster, double maximumDistance, int swordCount,
                             double controlPrecision, double formationSpacing) {
        if (!(caster instanceof ServerPlayer player)) {
            return;
        }
        SkillCaster skillCaster = SkillCaster.getExisting(player);
        Identifier skill = skillCaster == null ? null : skillCaster.getCastingSkill();
        if (!isControlSkill(skill)) {
            return;
        }
        int count = Math.clamp(swordCount, 1, 2);
        SwordGroup group = ACTIVE.computeIfAbsent(player.getUUID(), id -> new SwordGroup(skill));
        group.prune(player);
        if (!group.skill.equals(skill) || group.anyRecalling()) {
            return;
        }
        if (count == 2 && !bothHandsHaveSwords(player)) {
            group.recall();
            return;
        }
        InteractionHand first = count == 2 ? InteractionHand.MAIN_HAND
                : group.swords.isEmpty() ? swordHand(player) : group.swords.keySet().iterator().next();
        if (first == null) {
            group.recall();
            return;
        }
        InteractionHand[] hands = count == 2
                ? new InteractionHand[]{InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND}
                : new InteractionHand[]{first};
        for (InteractionHand hand : hands) {
            ControlledSwordEntity sword = group.swords.get(hand);
            if (sword == null) {
                double offset = count == 2 ? (hand == InteractionHand.MAIN_HAND ? -0.5D : 0.5D) * formationSpacing : 0.0D;
                sword = new ControlledSwordEntity(AscEntities.CONTROLLED_SWORD.get(), player.level(), player, hand, offset);
                if (!player.level().addFreshEntity(sword)) {
                    group.recall();
                    return;
                }
                group.swords.put(hand, sword);
            }
        }
        for (Map.Entry<InteractionHand, ControlledSwordEntity> entry : group.swords.entrySet()) {
            if (count == 1 && entry.getKey() != first) {
                entry.getValue().recall();
                continue;
            }
            double offset = count == 2
                    ? (entry.getKey() == InteractionHand.MAIN_HAND ? -0.5D : 0.5D) * formationSpacing : 0.0D;
            entry.getValue().command(player, maximumDistance, offset, count == 2 ? controlPrecision : 1.0D);
        }
    }

    public static void adjustDistance(ServerPlayer player, int steps) {
        if (steps == 0) {
            return;
        }
        SkillCaster caster = SkillCaster.getExisting(player);
        if (caster == null || !isControlSkill(caster.getCastingSkill())) {
            return;
        }
        SwordGroup group = ACTIVE.get(player.getUUID());
        if (group == null || !group.skill.equals(caster.getCastingSkill())) {
            return;
        }
        group.swords.values().forEach(sword -> {
            if (!sword.isRemoved() && !sword.isRecalling() && sword.level() == player.level()) {
                sword.adjustDistance(steps);
            }
        });
    }

    private static InteractionHand swordHand(Player player) {
        if (player.getMainHandItem().is(ModTags.Items.CONTROLLABLE_SWORDS)) {
            return InteractionHand.MAIN_HAND;
        }
        return player.getOffhandItem().is(ModTags.Items.CONTROLLABLE_SWORDS) ? InteractionHand.OFF_HAND : null;
    }

    private static boolean bothHandsHaveSwords(Player player) {
        return player.getMainHandItem().is(ModTags.Items.CONTROLLABLE_SWORDS)
                && player.getOffhandItem().is(ModTags.Items.CONTROLLABLE_SWORDS);
    }

    private static void recall(Player player) {
        SwordGroup group = ACTIVE.get(player.getUUID());
        if (group != null) {
            group.recall();
        }
    }

    @SubscribeEvent
    public static void onCastStart(SkillCastEvent.Start.Pre event) {
        if (!isControlSkill(event.getSkill()) || !(event.getCaster() instanceof ServerPlayer player)) {
            return;
        }
        SwordGroup group = ACTIVE.get(player.getUUID());
        if (group != null) {
            group.prune(player);
            if (!group.swords.isEmpty()) {
                event.setCanceled(true);
                player.sendOverlayMessage(Component.translatable("ascension.skill.sword_control.returning"));
                return;
            }
            ACTIVE.remove(player.getUUID(), group);
        }
        boolean hasWeapons = TWIN_SKILL.equals(event.getSkill()) ? bothHandsHaveSwords(player) : swordHand(player) != null;
        if (!hasWeapons) {
            event.setCanceled(true);
            player.sendOverlayMessage(Component.translatable(TWIN_SKILL.equals(event.getSkill())
                    ? "ascension.skill.twin_sword_control.requires_swords"
                    : "ascension.skill.sword_control.requires_sword"));
        }
    }

    @SubscribeEvent
    public static void onCastEnd(SkillCastEvent.End event) {
        if (isControlSkill(event.getSkill()) && event.getCaster() instanceof Player player) {
            recall(player);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            SwordGroup group = ACTIVE.get(player.getUUID());
            if (group != null && group.swords.values().stream().anyMatch(sword -> !sword.isRemoved() && !sword.isRecalling())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        SwordGroup group = ACTIVE.get(player.getUUID());
        if (group == null) {
            return;
        }
        group.prune(player);
        if (group.swords.isEmpty()) {
            ACTIVE.remove(player.getUUID(), group);
            return;
        }
        if (group.anyRecalling()) {
            SkillCaster caster = SkillCaster.getExisting(player);
            if (caster != null && isControlSkill(caster.getCastingSkill())) {
                caster.invalidateCast();
            }
            group.recall();
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            recall(event.getEntity());
            ACTIVE.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            recall(event.getEntity());
            ACTIVE.remove(event.getEntity().getUUID());
        }
    }

    private static final class SwordGroup {
        private final Identifier skill;
        private final EnumMap<InteractionHand, ControlledSwordEntity> swords = new EnumMap<>(InteractionHand.class);

        private SwordGroup(Identifier skill) {
            this.skill = skill;
        }

        private void prune(Player player) {
            swords.values().removeIf(sword -> sword.isRemoved() || sword.level() != player.level());
        }

        private boolean anyRecalling() {
            return swords.values().stream().anyMatch(ControlledSwordEntity::isRecalling);
        }

        private void recall() {
            swords.values().forEach(sword -> {
                if (!sword.isRemoved()) {
                    sword.recall();
                }
            });
        }
    }
}
