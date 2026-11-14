package net.zic.ascension.common.entities;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.zic.ascension.common.entities.rat.TreasureRatEntity;
import net.zic.ascension.common.entities.npc.CultivatorNpcEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.impl.core.innerworld.InnerWorldGhost;

public class AscEntities {
    private AscEntities() {

    }

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, AscensionCraft.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<InnerWorldGhost>> GHOST =
            ENTITY_TYPES.register("inner_world_ghost", id -> EntityType.Builder
                    .of(InnerWorldGhost::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<TreasureRatEntity>> TREASURE_RAT =
            ENTITY_TYPES.register("treasure_rat", id -> EntityType.Builder
                    .of(TreasureRatEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.35F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<CultivatorNpcEntity>> MALE_CULTIVATOR = npc("male_cultivator");
    public static final DeferredHolder<EntityType<?>, EntityType<CultivatorNpcEntity>> FEMALE_CULTIVATOR = npc("female_cultivator");
    public static final DeferredHolder<EntityType<?>, EntityType<CultivatorNpcEntity>> SECT_ELDER = npc("sect_elder");
    public static final DeferredHolder<EntityType<?>, EntityType<CultivatorNpcEntity>> SECT_MASTER = npc("sect_master");
    public static final DeferredHolder<EntityType<?>, EntityType<CultivatorNpcEntity>> YOUNG_MASTER = npc("young_master");

    private static DeferredHolder<EntityType<?>, EntityType<CultivatorNpcEntity>> npc(String id) {
        return ENTITY_TYPES.register(id, name -> EntityType.Builder
                .of(CultivatorNpcEntity::new, MobCategory.MISC)
                .sized(0.6F, 1.85F)
                .clientTrackingRange(10)
                .build(ResourceKey.create(Registries.ENTITY_TYPE, name)));
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static final class SpawnPlacements {
        @SubscribeEvent
        public static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
            event.register(TREASURE_RAT.get(), SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Animal::checkAnimalSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static final class Attributes {
        @SubscribeEvent
        public static void onAttributes(EntityAttributeCreationEvent event) {
            event.put(GHOST.get(), LivingEntity.createLivingAttributes().build());
            event.put(TREASURE_RAT.get(), TreasureRatEntity.createAttributes().build());
            var npcAttributes = CultivatorNpcEntity.createAttributes().build();
            event.put(MALE_CULTIVATOR.get(), npcAttributes);
            event.put(FEMALE_CULTIVATOR.get(), npcAttributes);
            event.put(SECT_ELDER.get(), npcAttributes);
            event.put(SECT_MASTER.get(), npcAttributes);
            event.put(YOUNG_MASTER.get(), npcAttributes);
        }
    }
}
