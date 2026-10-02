package net.zic.ascension.common.item.artifacts.pills;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;
import net.zic.ascension.common.item.components.AscensionComponents;

import java.util.Objects;

public class PillItem extends Item {

    private final Definition definition;

    public PillItem(Properties properties, Definition definition) {
        super(properties
                .component(DataComponents.CONSUMABLE, Consumables.defaultFood().consumeSeconds(1.0F).build())
                .component(AscensionComponents.PILL_DATA.get(), AscensionComponents.PillData.DEFAULT));
        this.definition = Objects.requireNonNull(definition);
    }

    public Definition definition() {
        return definition;
    }

    public AscensionComponents.PillData data(ItemStack stack) {
        return stack.getOrDefault(AscensionComponents.PILL_DATA.get(), AscensionComponents.PillData.DEFAULT);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!definition.canConsume().test(level, player, stack, data(stack))) {
            return InteractionResult.FAIL;
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return stack;
        }

        AscensionComponents.PillData data = data(stack);
        if (!definition.canConsume().test(level, player, stack, data)) {
            return stack;
        }

        if (!level.isClientSide()) {
            definition.effect().apply(level, player, stack, data);
        }

        return super.finishUsingItem(stack, level, entity);
    }

    public record Definition(CanConsume canConsume, Effect effect, EffectDescription effectDescription) {
        public Definition {
            canConsume = Objects.requireNonNull(canConsume);
            effect = Objects.requireNonNull(effect);
            effectDescription = Objects.requireNonNull(effectDescription);
        }

        public Component effectDescription(AscensionComponents.PillData data) {
            return effectDescription.describe(data == null ? AscensionComponents.PillData.DEFAULT : data);
        }

        public static Builder builder(Effect effect) {
            return new Builder(effect);
        }

        public static final class Builder {
            private final Effect effect;
            private CanConsume canConsume = (level, player, stack, data) -> true;
            private EffectDescription effectDescription = data -> Component.empty();

            private Builder(Effect effect) {
                this.effect = Objects.requireNonNull(effect);
            }

            public Builder canConsume(CanConsume canConsume) {
                this.canConsume = Objects.requireNonNull(canConsume);
                return this;
            }

            public Builder effectDescription(EffectDescription effectDescription) {
                this.effectDescription = Objects.requireNonNull(effectDescription);
                return this;
            }

            public Definition build() {
                return new Definition(canConsume, effect, effectDescription);
            }
        }
    }

    @FunctionalInterface
    public interface CanConsume {
        boolean test(Level level, Player player, ItemStack stack, AscensionComponents.PillData data);
    }

    @FunctionalInterface
    public interface Effect {
        void apply(Level level, Player player, ItemStack stack, AscensionComponents.PillData data);
    }

    @FunctionalInterface
    public interface EffectDescription {
        Component describe(AscensionComponents.PillData data);
    }
}
