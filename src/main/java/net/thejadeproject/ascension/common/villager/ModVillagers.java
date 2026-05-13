package net.thejadeproject.ascension.common.villager;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, AscensionCraft.MOD_ID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, AscensionCraft.MOD_ID);


    /*public static final Holder<PoiType> HERBALIST_POI = POI_TYPES.register("herbalist_poi",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.PILL_CAULDRON_HUMAN_LOW.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final Holder<VillagerProfession> HERBALIST = VILLAGER_PROFESSIONS.register("herbalist",
            () -> new VillagerProfession("herbalist", holder -> holder.value() == HERBALIST_POI.value(),
                    poiTypeHolder -> poiTypeHolder.value() == HERBALIST_POI.value(), ImmutableSet.of(), ImmutableSet.of(),
                    SoundType.MOSS.getPlaceSound()));*/



    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        VILLAGER_PROFESSIONS.register(eventBus);
    }

}
