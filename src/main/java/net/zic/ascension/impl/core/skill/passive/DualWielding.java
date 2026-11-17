package net.zic.ascension.impl.core.skill.passive;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.network.DualWieldStatePayload;
import net.zic.ascension.network.OffhandCooldownPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class DualWielding {
    private DualWielding() {
    }

    private static final int I_FRAME_WINDOW = 10;

    private static final Map<UUID, Boolean> SENT = new HashMap<>();
    private static final Map<UUID, Long> LAST_OFFHAND_ATTACK = new HashMap<>();
    private static final Map<UUID, LastHit> LAST_HIT = new HashMap<>();

    private static boolean inOffhandAttack;

    private record LastHit(int targetId, boolean offhand, long time) {
    }


    public static boolean isActive(Player player) {
        if (!DualWieldAccess.available()) {
            return false;
        }
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (source == null) {
            return false;
        }
        var entries = PassiveSkillService.modifiers(source, player.registryAccess(), PassiveModifiers.DualWield.class);
        if (entries.isEmpty()) {
            return false;
        }
        return handAccepted(source, entries, player.getMainHandItem()) && handAccepted(source, entries, player.getOffhandItem());
    }

    private static boolean handAccepted(OriginSource source, java.util.List<? extends PassiveSkillService.Entry<PassiveModifiers.DualWield>> entries, ItemStack stack) {
        for (var entry : entries) {
            for (PassiveModifiers.DualWield.Weapon weapon : entry.modifier().weapons()) {
                if (weapon.path().isPresent() && AscensionOriginSourceHelper.getPathInstance(source, weapon.path().get()) == null) {
                    continue;
                }
                if (stack.isEmpty() ? weapon.emptyHand()
                        : weapon.tag().isPresent() && stack.is(TagKey.create(Registries.ITEM, weapon.tag().get()))) {
                    return true;
                }
            }
        }
        return false;
    }


    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 5 != 0) {
            return;
        }
        boolean active = isActive(player);
        Boolean previous = SENT.put(player.getUUID(), active);
        if (previous == null || previous != active) {
            PacketDistributor.sendToPlayer(player, new DualWieldStatePayload(active));
            PacketDistributor.sendToPlayer(player, new OffhandCooldownPayload(active ? remainingCooldown(player) : 0));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        SENT.remove(uuid);
        LAST_OFFHAND_ATTACK.remove(uuid);
        LAST_HIT.remove(uuid);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        UUID uuid = event.getEntity().getUUID();
        LAST_OFFHAND_ATTACK.remove(uuid);
        LAST_HIT.remove(uuid);
    }


    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (inOffhandAttack || !(event.getEntity() instanceof ServerPlayer player) || !isActive(player)) {
            return;
        }
        Entity target = event.getTarget();
        if (shouldBypass(player, target, false)) {
            DualWieldAccess.clearIFrames(target);
        }
        LAST_HIT.put(player.getUUID(), new LastHit(target.getId(), false, player.level().getGameTime()));
    }

    private static boolean shouldBypass(ServerPlayer player, Entity target, boolean offhand) {
        LastHit last = LAST_HIT.get(player.getUUID());
        if (last == null || last.offhand() == offhand || last.targetId() != target.getId()) {
            return false;
        }
        long elapsed = player.level().getGameTime() - last.time();
        return elapsed >= 0 && elapsed <= I_FRAME_WINDOW;
    }


    public static void offhandAttack(ServerPlayer player, int targetId) {
        if (!isActive(player)) {
            return;
        }
        player.swing(InteractionHand.OFF_HAND, false);
        if (targetId < 0) {
            return;
        }
        ServerLevel level = player.level();
        Entity target = level.getEntity(targetId);
        if (target == null || target == player || !target.isAttackable() || !player.isWithinEntityInteractionRange(target, 1.0)) {
            return;
        }

        long now = level.getGameTime();
        UUID uuid = player.getUUID();
        int offhandTicker = (int) Math.min(100_000L, now - LAST_OFFHAND_ATTACK.getOrDefault(uuid, now - 100_000L));
        int mainTicker = DualWieldAccess.get(player);
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        boolean bypass = shouldBypass(player, target, true);

        shiftModifiers(player, main, off);
        player.setItemSlot(EquipmentSlot.MAINHAND, off, true);
        player.setItemSlot(EquipmentSlot.OFFHAND, main, true);
        DualWieldAccess.set(player, offhandTicker);
        inOffhandAttack = true;
        try {
            if (bypass) {
                try (DualWieldAccess.IFrames ignored = DualWieldAccess.bypassIFrames(target)) {
                    player.attack(target);
                }
            } else {
                player.attack(target);
            }

            LAST_HIT.put(uuid, new LastHit(target.getId(), true, now));
            LAST_OFFHAND_ATTACK.put(uuid, now);
            PacketDistributor.sendToPlayer(player, new OffhandCooldownPayload(offhandDelay(player)));
        } finally {
            inOffhandAttack = false;
            player.setItemSlot(EquipmentSlot.MAINHAND, main, true);
            player.setItemSlot(EquipmentSlot.OFFHAND, off, true);
            shiftModifiers(player, off, main);
            DualWieldAccess.set(player, mainTicker);
        }
    }

    private static int offhandDelay(Player player) {
        double speed = Math.max(0.1D, player.getAttributeValue(Attributes.ATTACK_SPEED));
        return (int) Math.ceil(20.0D / speed);
    }

    private static int remainingCooldown(ServerPlayer player) {
        Long last = LAST_OFFHAND_ATTACK.get(player.getUUID());
        if (last == null) {
            return 0;
        }
        long elapsed = player.level().getGameTime() - last;
        int delay = (int) Math.ceil(20.0D / Math.max(0.1D, player.getAttributeValue(Attributes.ATTACK_SPEED)));
        return (int) Math.max(0L, delay - elapsed);
    }

    private static void shiftModifiers(Player player, ItemStack from, ItemStack to) {
        from.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            AttributeInstance instance = player.getAttributes().getInstance(attribute);
            if (instance != null) {
                instance.removeModifier(modifier.id());
            }
        });
        to.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            AttributeInstance instance = player.getAttributes().getInstance(attribute);
            if (instance != null) {
                instance.removeModifier(modifier.id());
                instance.addTransientModifier(modifier);
            }
        });
    }
}