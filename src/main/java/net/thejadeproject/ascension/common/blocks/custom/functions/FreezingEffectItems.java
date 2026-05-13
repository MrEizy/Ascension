package net.thejadeproject.ascension.common.blocks.custom.functions;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.items.ModItems;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class FreezingEffectItems {

    // List of items that will cause freezing when in inventory
    private static final List<Item> FREEZING_ITEMS = new ArrayList<>();

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FREEZING_ITEMS.add(ModBlocks.FROST_SILVER_ORE.get().asItem());
            FREEZING_ITEMS.add(ModBlocks.FROST_SILVER_BLOCK.get().asItem());
            FREEZING_ITEMS.add(ModItems.FROST_SILVER_INGOT.get());
            FREEZING_ITEMS.add(ModItems.FROST_SILVER_NUGGET.get());
            FREEZING_ITEMS.add(ModItems.RAW_FROST_SILVER.get());

        });
    }

    public static void addFreezingItem(Item item) {
        if (!FREEZING_ITEMS.contains(item)) {
            FREEZING_ITEMS.add(item);
        }
    }

    public static void removeFreezingItem(Item item) {
        FREEZING_ITEMS.remove(item);
    }

    public static boolean isFreezingItem(Item item) {
        return FREEZING_ITEMS.contains(item);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();

        if (player.isCreative() || player.isSpectator() || player.level().isClientSide()) {
            return;
        }

        boolean hasFreezingItem = false;

        for (ItemStack stack : player.getInventory().items) {
            if (FREEZING_ITEMS.contains(stack.getItem())) {
                hasFreezingItem = true;
                break;
            }
        }

        if (!hasFreezingItem && FREEZING_ITEMS.contains(player.getOffhandItem().getItem())) {
            hasFreezingItem = true;
        }

        if (hasFreezingItem) {
            applyFreezingEffect(player);
        }
    }

    private static void applyFreezingEffect(Player player) {
        player.setTicksFrozen(Math.min(player.getTicksFrozen() + 40, 300));

        if (player.isFullyFrozen()) {
            player.hurt(player.damageSources().freeze(), 1.0F);
        }

        if (player.level().isClientSide()) {

            player.setIsInPowderSnow(true);
        }
    }
}