package net.zic.ascension.common.entities;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
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

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static final class Attributes {
        @SubscribeEvent
        public static void onAttributes(EntityAttributeCreationEvent event) {
            event.put(GHOST.get(), LivingEntity.createLivingAttributes().build());
        }
    }
}
