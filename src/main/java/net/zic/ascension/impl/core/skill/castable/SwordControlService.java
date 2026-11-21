package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class SwordControlService {
    public static final Identifier SKILL = AscensionCraft.prefix("castable/sword_control");
    private static final Map<UUID, ControlledSwordEntity> ACTIVE = new ConcurrentHashMap<>();

    private SwordControlService() {
    }

    public static void pulse(net.minecraft.world.entity.LivingEntity caster) {
        if (!(caster instanceof ServerPlayer player)) {
            return;
        }
        ControlledSwordEntity current = ACTIVE.get(player.getUUID());
        if (current != null && (current.isRemoved() || current.level() != player.level())) {
            ACTIVE.remove(player.getUUID(), current);
            current = null;
        }
        if (current == null) {
            InteractionHand hand = swordHand(player);
            if (hand == null) {
                return;
            }
            current = new ControlledSwordEntity(AscEntities.CONTROLLED_SWORD.get(), player.level(), player, hand);
            if (!player.level().addFreshEntity(current)) {
                return;
            }
            ACTIVE.put(player.getUUID(), current);
        }
        if (!current.isRecalling()) {
            current.command(player);
        }
    }

    public static void adjustDistance(ServerPlayer player, int steps) {
        if (steps == 0) {
            return;
        }
        SkillCaster caster = SkillCaster.getExisting(player);
        if (caster == null || !SKILL.equals(caster.getCastingSkill())) {
            return;
        }
        ControlledSwordEntity sword = ACTIVE.get(player.getUUID());
        if (sword != null && !sword.isRemoved() && !sword.isRecalling()
                && sword.level() == player.level()) {
            sword.adjustDistance(steps);
        }
    }

    private static InteractionHand swordHand(Player player) {
        if (player.getMainHandItem().is(ModTags.Items.CONTROLLABLE_SWORDS)) {
            return InteractionHand.MAIN_HAND;
        }
        return player.getOffhandItem().is(ModTags.Items.CONTROLLABLE_SWORDS) ? InteractionHand.OFF_HAND : null;
    }

    private static void recall(Player player) {
        ControlledSwordEntity sword = ACTIVE.get(player.getUUID());
        if (sword != null && !sword.isRemoved()) {
            sword.recall();
        }
    }

    @SubscribeEvent
    public static void onCastStart(SkillCastEvent.Start.Pre event) {
        if (!SKILL.equals(event.getSkill()) || !(event.getCaster() instanceof ServerPlayer player)) {
            return;
        }
        ControlledSwordEntity sword = ACTIVE.get(player.getUUID());
        if (sword != null && !sword.isRemoved() && sword.isRecalling()) {
            event.setCanceled(true);
            player.sendOverlayMessage(Component.translatable("ascension.skill.sword_control.returning"));
            return;
        }
        if (swordHand(player) == null) {
            event.setCanceled(true);
            player.sendOverlayMessage(Component.translatable("ascension.skill.sword_control.requires_sword"));
        }
    }

    @SubscribeEvent
    public static void onCastEnd(SkillCastEvent.End event) {
        if (SKILL.equals(event.getSkill()) && event.getCaster() instanceof Player player) {
            recall(player);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            ControlledSwordEntity sword = ACTIVE.get(player.getUUID());
            if (sword != null && !sword.isRemoved() && !sword.isRecalling()) {
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
        ControlledSwordEntity sword = ACTIVE.get(player.getUUID());
        if (sword != null && (sword.isRemoved() || sword.level() != player.level())) {
            ACTIVE.remove(player.getUUID(), sword);
            return;
        }
        if (sword != null && sword.isRecalling()) {
            SkillCaster caster = SkillCaster.getExisting(player);
            if (caster != null && SKILL.equals(caster.getCastingSkill())) {
                caster.invalidateCast();
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            recall(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            recall(event.getEntity());
        }
    }
}
