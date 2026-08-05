package net.zic.ascension.impl.core.effect;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.effect.BuildupChannel;
import net.zic.zenithlib.registry.RegistryHelper;

import java.util.function.Supplier;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AscensionBuildupChannels {
    public static final Registry<BuildupChannel> REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID, "buildup_channel");
    private static final DeferredRegister<BuildupChannel> TYPES = DeferredRegister.create(REGISTRY, AscensionCraft.MOD_ID);
    public static final Supplier<BuildupChannel> FROZEN = TYPES.register("frozen", () -> new BuildupChannel() {
        @Override
        public double apply(net.minecraft.world.entity.LivingEntity entity, double amount, int decayDelay) {
            return FrozenStateService.apply(entity, amount, decayDelay);
        }

        @Override
        public double reduce(net.minecraft.world.entity.LivingEntity entity, double amount) {
            return FrozenStateService.reduce(entity, amount);
        }

        @Override
        public void clear(net.minecraft.world.entity.LivingEntity entity) {
            FrozenStateService.clear(entity);
        }
    });

    private AscensionBuildupChannels() {
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }

    @SubscribeEvent
    public static void registerRegistry(NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    public static BuildupChannel get(Identifier id) {
        return id == null ? null : REGISTRY.getValue(id);
    }
}
