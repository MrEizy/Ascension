//package net.zic.ascension.impl.core.skill.passive;
//
//import net.minecraft.core.registries.Registries;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.tags.TagKey;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.EquipmentSlot;
//import net.minecraft.world.entity.ai.attributes.AttributeInstance;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.event.entity.player.PlayerEvent;
//import net.neoforged.neoforge.event.tick.PlayerTickEvent;
//import net.neoforged.neoforge.network.PacketDistributor;
//import net.zic.ascension.AscensionCraft;
//import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
//import net.zic.ascension.api.rpg_engine.source.OriginSource;
//import net.zic.ascension.network.DualWieldStatePayload;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//
//@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
//public final class DualWielding {
//    private DualWielding() {
//    }
//
//    private static final Map<UUID, Boolean> SENT = new HashMap<>();
//    private static final Map<UUID, Long> LAST_OFFHAND_ATTACK = new HashMap<>();
//
//    // ------------------------------------------------------------------------------------------------ the rule
//
//    public static boolean isActive(Player player) {
//        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
//        if (source == null) {
//            return false;
//        }
//        var entries = PassiveSkillService.modifiers(source, player.registryAccess(), PassiveModifiers.DualWield.class);
//        if (entries.isEmpty()) {
//            return false;
//        }
//        return handAccepted(source, entries, player.getMainHandItem()) && handAccepted(source, entries, player.getOffhandItem());
//    }
//
//    private static boolean handAccepted(OriginSource source, java.util.List<? extends PassiveSkillService.Entry<PassiveModifiers.DualWield>> entries, ItemStack stack) {
//        for (var entry : entries) {
//            for (PassiveModifiers.DualWield.Weapon weapon : entry.modifier().weapons()) {
//                if (weapon.path().isPresent() && AscensionOriginSourceHelper.getPathInstance(source, weapon.path().get()) == null) {
//                    continue; // the player doesn't walk the Dao this weapon type needs
//                }
//                if (stack.isEmpty() ? weapon.emptyHand()
//                        : weapon.tag().isPresent() && stack.is(TagKey.create(Registries.ITEM, weapon.tag().get()))) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    // ------------------------------------------------------------------------------------------------ state sync
//
//    @SubscribeEvent
//    public static void onPlayerTick(PlayerTickEvent.Post event) {
//        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 5 != 0) {
//            return;
//        }
//        boolean active = isActive(player);
//        Boolean previous = SENT.put(player.getUUID(), active);
//        if (previous == null || previous != active) {
//            PacketDistributor.sendToPlayer(player, new DualWieldStatePayload(active));
//        }
//    }
//
//    @SubscribeEvent
//    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
//        SENT.remove(event.getEntity().getUUID());
//        LAST_OFFHAND_ATTACK.remove(event.getEntity().getUUID());
//    }
//
//    // ------------------------------------------------------------------------------------------------ the offhand attack
//
//    public static void offhandAttack(ServerPlayer player, int targetId) {
//        if (!isActive(player)) {
//            return;
//        }
//        player.swing(InteractionHand.OFF_HAND, false); // others see the offhand swing; the attacker's own client already animated it
//        if (targetId < 0) {
//            return;
//        }
//        ServerLevel level = player.level();
//        Entity target = level.getEntity(targetId);
//        if (target == null || target == player || !target.isAttackable() || !player.isWithinEntityInteractionRange(target, 1.0)) {
//            return;
//        }
//
//        long now = level.getGameTime();
//        int offhandTicker = (int) Math.min(100_000L, now - LAST_OFFHAND_ATTACK.getOrDefault(player.getUUID(), now - 100_000L));
//        int mainTicker = DualWieldAccess.get(player);
//        ItemStack main = player.getMainHandItem();
//        ItemStack off = player.getOffhandItem();
//
//        shiftModifiers(player, main, off);
//        player.setItemSlot(EquipmentSlot.MAINHAND, off, true);
//        player.setItemSlot(EquipmentSlot.OFFHAND, main, true);
//        DualWieldAccess.set(player, offhandTicker);
//        try {
//            player.attack(target);
//            if (DualWieldAccess.get(player) == 0) {
//                LAST_OFFHAND_ATTACK.put(player.getUUID(), now); // vanilla resets the ticker to 0 only when a real attack happened
//            }
//        } finally {
//            player.setItemSlot(EquipmentSlot.MAINHAND, main, true);
//            player.setItemSlot(EquipmentSlot.OFFHAND, off, true);
//            shiftModifiers(player, off, main);
//            DualWieldAccess.set(player, mainTicker);
//        }
//    }
//
//
//    private static void shiftModifiers(Player player, ItemStack from, ItemStack to) {
//        from.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
//            AttributeInstance instance = player.getAttributes().getInstance(attribute);
//            if (instance != null) {
//                instance.removeModifier(modifier.id());
//            }
//        });
//        to.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
//            AttributeInstance instance = player.getAttributes().getInstance(attribute);
//            if (instance != null) {
//                instance.removeModifier(modifier.id());
//                instance.addTransientModifier(modifier);
//            }
//        });
//    }
//}