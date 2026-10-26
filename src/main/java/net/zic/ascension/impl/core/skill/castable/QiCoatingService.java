package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoating;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingChannel;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingEffects;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingElement;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.common.util.ModTags;
import net.zic.ascension.impl.core.skill.passive.PassiveTriggerService;
import net.zic.ascension.impl.runtime.object.PersistentRuntimeVisuals;
import net.zic.ascension.network.LightningFlashPacket;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Server-side behaviour of Qi Coatings beyond their looks. Coatings themselves are passives (toggle_passive and
 * persistent_visual), which own their upkeep; this runs the Qi Coating elements' combat hooks (QiCoatingElement.Hook) and the
 * Lightning Step spear flash.
 */
public final class QiCoatingService {
    // Element hooks: the melee swing each player made last (AttackEntityEvent), so a hit can be told apart from projectiles and
    // skill damage, when each hook may fire again, and whether a hook is running (its own damage never sets off another hook).
    private static final Map<UUID, Swing> MELEE_SWINGS = new ConcurrentHashMap<>();
    private static final Map<HookKey, Long> HOOK_READY_AT = new ConcurrentHashMap<>();
    private static boolean runningHook;

    // Lightning Step spear flash: holding right click with a spear for FLASH_WINDUP_TICKS while Lightning Step is on spends
    // FLASH_QI_COST qi and sends the player shooting forward (QiCoatingLayer.startLightningFlash moves them). Once per hold,
    // then not again for FLASH_COOLDOWN_TICKS.
    private static final int FLASH_WINDUP_TICKS = 12;
    private static final int FLASH_COOLDOWN_TICKS = 50;
    private static final double FLASH_QI_COST = 3.0D;
    // Durability the spear loses per flash, on top of the usual 1 per target hit (vanilla charges every spear hit the same).
    private static final int FLASH_DURABILITY_COST = 8;
    // Players whose current hold has already flashed (or failed to), and when each may flash again.
    private static final Set<UUID> FLASHED_THIS_HOLD = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> FLASH_READY_AT = new ConcurrentHashMap<>();

    private QiCoatingService() {
    }

    /**
     * Whether External Qi Coating shows on this item: bladed weapons and polearms (swords, blades, axes and spears).
     */
    public static boolean coatsWeapon(ItemStack itemStack) {
        return itemStack.is(ItemTags.SWORDS) || itemStack.is(ItemTags.AXES) || itemStack.is(ItemTags.SPEARS)
                || itemStack.is(ModTags.Items.WEAPON_BLADES) || itemStack.is(ModTags.Items.WEAPON_SPEARS);
    }

    /**
     * Fires the element hooks a hit sets off: body_on_hit or external_on_hit for a player's melee hit (bare-handed or with a
     * coated weapon), and body_on_hurt for a player hurt by an attacker.
     */
    private static void onDamaged(LivingEntity victim, DamageSource source, double damage) {
        if (runningHook || damage <= 0.0D) {
            return;
        }
        Map<Identifier, Double> variables = Map.of(PassiveTriggerService.DAMAGE, damage);
        if (source.getEntity() instanceof ServerPlayer attacker && attacker != victim && isMeleeHit(attacker, victim, source)) {
            ItemStack held = attacker.getMainHandItem();
            if (held.isEmpty()) {
                fireHook(attacker, Hook.BODY_ON_HIT, victim, variables);
            } else if (coatsWeapon(held)) {
                fireHook(attacker, Hook.EXTERNAL_ON_HIT, victim, variables);
            }
        }
        if (victim instanceof ServerPlayer player && source.getEntity() instanceof LivingEntity attacker && attacker != victim) {
            fireHook(player, Hook.BODY_ON_HURT, attacker, variables);
        }
    }

    /**
     * A melee hit: the attacker swung at the victim this tick, or hit them with a spear charge.
     */
    private static boolean isMeleeHit(ServerPlayer attacker, LivingEntity victim, DamageSource source) {
        if (source.getDirectEntity() != attacker) {
            return false;
        }
        if (source.is(DamageTypes.SPEAR)) {
            return true;
        }
        Swing swing = MELEE_SWINGS.get(attacker.getUUID());
        return swing != null && swing.targetId() == victim.getId() && swing.gameTime() == attacker.level().getGameTime();
    }

    /**
     * Runs the hook of the element the player's coating is showing, if it has one and it's off cooldown, under the passive skill
     * showing the element.
     */
    private static void fireHook(ServerPlayer player, Hook hook, LivingEntity target, Map<Identifier, Double> variables) {
        QiCoatingSelectionService.Shown shown = QiCoatingSelectionService.shown(player.getUUID(), hook.channel).orElse(null);
        QiCoatingElement element = shown == null ? null
                : CoreRegistries.safeAccess(CoreRegistries.QI_COATING_ELEMENT_REGISTRY, shown.element(), player.registryAccess());
        QiCoatingElement.Hook definition = element == null ? null : hook.definition.apply(element).orElse(null);
        if (definition == null) {
            return;
        }
        HookKey key = new HookKey(player.getUUID(), hook);
        long now = player.level().getGameTime();
        if (now < HOOK_READY_AT.getOrDefault(key, Long.MIN_VALUE)) {
            return;
        }
        runningHook = true;
        try {
            if (PassiveTriggerService.run(player, target, shown.skill(), definition.conditions(), definition.costs(),
                    definition.actions(), variables)) {
                HOOK_READY_AT.put(key, now + definition.cooldown());
            }
        } finally {
            runningHook = false;
        }
    }

    /**
     * Lightning Step spear flash, checked every server tick for each player.
     */
    private static void tickLightningFlash(ServerPlayer player) {
        UUID id = player.getUUID();
        ItemStack held = player.getUseItem();
        if (!player.isUsingItem() || !isSpear(held)) {
            FLASHED_THIS_HOLD.remove(id);
            return;
        }
        if (player.getTicksUsingItem() < FLASH_WINDUP_TICKS || FLASHED_THIS_HOLD.contains(id) || !hasLightningStep(player)) {
            return;
        }
        long now = player.level().getGameTime();
        if (now < FLASH_READY_AT.getOrDefault(id, Long.MIN_VALUE)) {
            return;
        }
        FLASHED_THIS_HOLD.add(id);
        ResourceTransactionRequest cost = ResourceTransactionRequest.of(player, AscensionCraft.prefix("qi"), ResourceOperation.CONSUME,
                FLASH_QI_COST, new ResourceSourceIdentity.Simple(AscensionCraft.prefix("skill_casting"), Set.of(ResourceSourceIdentity.Tags.SKILL)));
        if (!ResourceTransactionService.transact(cost).succeeded()) {
            return;
        }
        FLASH_READY_AT.put(id, now + FLASH_COOLDOWN_TICKS);
        held.hurtAndBreak(FLASH_DURABILITY_COST, player, player.getUsedItemHand().asEquipmentSlot());
        Vec3 look = player.getLookAngle();
        Vec3 direction = new Vec3(look.x, 0.0D, look.z);
        if (direction.lengthSqr() < 1.0E-4D) {
            return;
        }
        direction = direction.normalize();
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new LightningFlashPacket(player.getId(), (float) direction.x, (float) direction.z));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS, 0.6F, 1.6F);
    }

    /**
     * Whether the player has a coating on with the Lightning Step effect (QiCoatingEffects.LIGHTNING_STEP): one of their
     * persistent visuals names a coating that adds it.
     */
    private static boolean hasLightningStep(ServerPlayer player) {
        RegistryAccess access = player.level().registryAccess();
        for (Identifier visual : PersistentRuntimeVisuals.visualsOn(player.getUUID())) {
            QiCoating coating = QiCoating.fromVisual(visual, access);
            if (coating != null && coating.hasEffect(QiCoatingEffects.LIGHTNING_STEP)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Spears: anything with the vanilla spear charge, or tagged as a spear.
     */
    private static boolean isSpear(ItemStack stack) {
        return stack.has(DataComponents.KINETIC_WEAPON) || stack.is(ItemTags.SPEARS) || stack.is(ModTags.Items.WEAPON_SPEARS);
    }

    /**
     * The element hooks, and the coating channel each one belongs to.
     */
    private enum Hook {
        BODY_ON_HIT(QiCoatingChannel.BODY, QiCoatingElement::bodyOnHit),
        BODY_ON_HURT(QiCoatingChannel.BODY, QiCoatingElement::bodyOnHurt),
        EXTERNAL_ON_HIT(QiCoatingChannel.EXTERNAL, QiCoatingElement::externalOnHit);

        private final QiCoatingChannel channel;
        private final Function<QiCoatingElement, Optional<QiCoatingElement.Hook>> definition;

        Hook(QiCoatingChannel channel, Function<QiCoatingElement, Optional<QiCoatingElement.Hook>> definition) {
            this.channel = channel;
            this.definition = definition;
        }
    }

    private record HookKey(UUID player, Hook hook) {
    }

    private record Swing(int targetId, long gameTime) {
    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static final class ServerEvents {
        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                MELEE_SWINGS.put(player.getUUID(), new Swing(event.getTarget().getId(), player.level().getGameTime()));
            }
        }

        @SubscribeEvent
        public static void onDamaged(RPGEngineEntityDamagedEvent.Post event) {
            if (!event.getEntity().level().isClientSide()) {
                QiCoatingService.onDamaged(event.getEntity(), event.getSource(), event.getDamage());
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                tickLightningFlash(serverPlayer);
            }
        }

        /**
         * TEMPORARY, for tuning the Lightning Step spear flash: logs every spear charge hit a player lands, before and after the
         * damage pipeline. Remove once the flash damage is settled.
         */
        @SubscribeEvent
        public static void onSpearHit(LivingDamageEvent.Post event) {
            if (event.getSource().is(DamageTypes.SPEAR) && event.getSource().getEntity() instanceof ServerPlayer attacker) {
                AscensionCraft.LOGGER.info("[spear flash debug] {} hit {} with {}: {} damage before the damage pipeline, {} taken from health "
                                + "(speed {} b/s, flashing {})",
                        attacker.getName().getString(), event.getEntity().getName().getString(),
                        attacker.getMainHandItem().getHoverName().getString(), event.getOriginalDamage(), event.getHealthDamage(),
                        String.format("%.1f", attacker.getKnownSpeed().length() * 20.0D),
                        attacker.level().getGameTime() < FLASH_READY_AT.getOrDefault(attacker.getUUID(), Long.MIN_VALUE)
                                - FLASH_COOLDOWN_TICKS + 6);
            }
        }

        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide()) {
                FLASHED_THIS_HOLD.remove(player.getUUID());
                FLASH_READY_AT.remove(player.getUUID());
                MELEE_SWINGS.remove(player.getUUID());
                HOOK_READY_AT.keySet().removeIf(key -> key.player().equals(player.getUUID()));
            }
        }
    }
}
