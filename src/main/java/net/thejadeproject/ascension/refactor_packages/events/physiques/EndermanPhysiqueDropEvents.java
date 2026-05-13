package net.thejadeproject.ascension.refactor_packages.events.physiques;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.refactor_packages.physiques.ModPhysiques;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class EndermanPhysiqueDropEvents {

    private static final float DROP_CHANCE = 0.07f;
    private static final int MIN_PURITY = 10;
    private static final int MAX_PURITY = 30;

    private EndermanPhysiqueDropEvents() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EnderMan enderman)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!enderman.isCreepy() && !enderman.hasBeenStaredAt()) return;
        if (player.level().random.nextFloat() > DROP_CHANCE) return;

        long timeOfDay = player.level().getDayTime() % 24000L;
        // Only drops at night (13000-23000). First half = Yin, second half = Yang.
        if (timeOfDay < 13000L || timeOfDay >= 23000L) return;
        boolean isFirstHalfOfNight = timeOfDay < 18000L;
        ResourceLocation physiqueId = isFirstHalfOfNight
                ? ModPhysiques.YIN_EYES.getId()
                : ModPhysiques.YANG_EYES.getId();

        int purity = MIN_PURITY + player.level().random.nextInt(MAX_PURITY - MIN_PURITY + 1);

        ItemStack stack = new ItemStack(ModItems.PHYSIQUE_ESSENCE.get());
        stack.set(ModDataComponents.PHYSIQUE_ID.get(), physiqueId.toString());
        stack.set(ModDataComponents.PURITY.get(), purity);

        enderman.spawnAtLocation(stack);
    }
}
