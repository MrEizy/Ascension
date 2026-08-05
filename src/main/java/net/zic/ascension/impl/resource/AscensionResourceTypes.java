package net.zic.ascension.impl.resource;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceRegistries;
import net.zic.ascension.api.ascension.core.resource.ResourceType;
import net.zic.ascension.impl.resource.stamina.StaminaService;
import net.zic.ascension.mixins.accessor.FoodDataAccessor;

public final class AscensionResourceTypes {
    public static final DeferredRegister<ResourceType> RESOURCE_TYPES = DeferredRegister.create(
            ResourceRegistries.RESOURCE_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );
    public static final DeferredRegister<ResourceOperation> RESOURCE_OPERATIONS = DeferredRegister.create(
            ResourceRegistries.RESOURCE_OPERATION_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<ResourceOperation, ResourceOperation> CONSUME = RESOURCE_OPERATIONS.register(
            "consume",
            () -> ResourceOperation.CONSUME
    );
    public static final DeferredHolder<ResourceOperation, ResourceOperation> DRAIN = RESOURCE_OPERATIONS.register(
            "drain",
            () -> ResourceOperation.DRAIN
    );
    public static final DeferredHolder<ResourceOperation, ResourceOperation> RESTORE = RESOURCE_OPERATIONS.register(
            "restore",
            () -> ResourceOperation.RESTORE
    );
    public static final DeferredHolder<ResourceOperation, ResourceOperation> GENERATE = RESOURCE_OPERATIONS.register(
            "generate",
            () -> ResourceOperation.GENERATE
    );
    public static final DeferredHolder<ResourceOperation, ResourceOperation> ACCUMULATE = RESOURCE_OPERATIONS.register(
            "accumulate",
            () -> ResourceOperation.ACCUMULATE
    );

    public static final DeferredHolder<ResourceType, ResourceType> QI = RESOURCE_TYPES.register("qi", Qi::new);
    public static final DeferredHolder<ResourceType, ResourceType> STAMINA = RESOURCE_TYPES.register("stamina", Stamina::new);
    public static final DeferredHolder<ResourceType, ResourceType> EXHAUSTION = RESOURCE_TYPES.register("exhaustion", Exhaustion::new);
    public static final DeferredHolder<ResourceType, ResourceType> HUNGER = RESOURCE_TYPES.register("hunger", Hunger::new);
    public static final DeferredHolder<ResourceType, ResourceType> SATURATION = RESOURCE_TYPES.register("saturation", Saturation::new);

    private AscensionResourceTypes() {
    }

    public static void register(IEventBus eventBus) {
        RESOURCE_OPERATIONS.register(eventBus);
        RESOURCE_TYPES.register(eventBus);
    }

    private static final class Qi implements ResourceType {
        @Override
        public boolean supports(LivingEntity entity) {
            return entity != null && entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER) != null;
        }

        @Override
        public double getAmount(LivingEntity entity) {
            EntityQiProvider provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
            return provider == null ? 0.0D : provider.getQi();
        }

        @Override
        public double getMaximum(LivingEntity entity) {
            EntityQiProvider provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
            return provider == null ? 0.0D : provider.getMaxQi();
        }

        @Override
        public void setAmount(LivingEntity entity, double amount) {
            EntityQiProvider provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
            if (provider != null) {
                provider.setQi(amount);
            }
        }
    }

    private static final class Stamina implements ResourceType {
        @Override
        public boolean supports(LivingEntity entity) {
            return entity instanceof Player;
        }

        @Override
        public double getAmount(LivingEntity entity) {
            return StaminaService.getStamina(entity);
        }

        @Override
        public double getMaximum(LivingEntity entity) {
            return StaminaService.getMaximumStamina(entity);
        }

        @Override
        public void setAmount(LivingEntity entity, double amount) {
            StaminaService.setStamina(entity, amount);
        }

        @Override
        public void afterApply(
                LivingEntity entity,
                ResourceOperation operation,
                ResourceOperation.Application result
        ) {
            if (result.appliedAmount() > 0.0D
                    && (operation == ResourceOperation.CONSUME || operation == ResourceOperation.DRAIN)) {
                StaminaService.resetRegenerationDelay(entity);
            }
        }
    }

    private static final class Exhaustion implements ResourceType {
        private static final double MAXIMUM = 40.0D;

        @Override
        public boolean supports(LivingEntity entity) {
            return entity instanceof Player;
        }

        @Override
        public double getAmount(LivingEntity entity) {
            return entity instanceof Player player
                    ? accessor(player.getFoodData()).ascension$getExhaustionLevel()
                    : 0.0D;
        }

        @Override
        public double getMaximum(LivingEntity entity) {
            return MAXIMUM;
        }

        @Override
        public void setAmount(LivingEntity entity, double amount) {
            if (entity instanceof Player player) {
                accessor(player.getFoodData()).ascension$setExhaustionLevel((float) Math.clamp(amount, 0.0D, MAXIMUM));
            }
        }

        private FoodDataAccessor accessor(FoodData foodData) {
            return (FoodDataAccessor) foodData;
        }
    }

    private static final class Hunger implements ResourceType {
        @Override
        public boolean supports(LivingEntity entity) {
            return entity instanceof Player;
        }

        @Override
        public double getAmount(LivingEntity entity) {
            return entity instanceof Player player ? player.getFoodData().getFoodLevel() : 0.0D;
        }

        @Override
        public double getMaximum(LivingEntity entity) {
            return 20.0D;
        }

        @Override
        public double normalizeAmount(double amount) {
            return amount <= 0.0D ? 0.0D : Math.ceil(amount);
        }

        @Override
        public void setAmount(LivingEntity entity, double amount) {
            if (entity instanceof Player player) {
                player.getFoodData().setFoodLevel(Math.clamp((int) amount, 0, 20));
            }
        }
    }

    private static final class Saturation implements ResourceType {
        @Override
        public boolean supports(LivingEntity entity) {
            return entity instanceof Player;
        }

        @Override
        public double getAmount(LivingEntity entity) {
            return entity instanceof Player player ? player.getFoodData().getSaturationLevel() : 0.0D;
        }

        @Override
        public double getMaximum(LivingEntity entity) {
            return entity instanceof Player player ? player.getFoodData().getFoodLevel() : 0.0D;
        }

        @Override
        public void setAmount(LivingEntity entity, double amount) {
            if (entity instanceof Player player) {
                player.getFoodData().setSaturation((float) Math.clamp(amount, 0.0D, getMaximum(entity)));
            }
        }
    }
}
