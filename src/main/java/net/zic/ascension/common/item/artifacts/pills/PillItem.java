package net.zic.ascension.common.item.artifacts.pills;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class PillItem extends Item {
    private final Definition definition;

    public PillItem(Properties properties, Definition definition) {
        super(properties);
        this.definition = Objects.requireNonNull(definition);
    }

    public Definition definition() {
        return definition;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!definition.effect().apply(level, player, stack)) {
            return InteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    public record Definition(Effect effect) {
        public Definition {
            effect = Objects.requireNonNull(effect);
        }
    }

    @FunctionalInterface
    public interface Effect {
        boolean apply(Level level, Player player, ItemStack stack);
    }
}
