package net.thejadeproject.ascension.common.items.herbs;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;


public class HerbBlockItem extends ItemNameBlockItem {

    /**
     * Called server-side after the herb is eaten.
     * entity is always a Player (checked before calling).
     */
    @FunctionalInterface
    public interface EatEffect {
        void apply(ItemStack stack, Level level, LivingEntity entity);
    }

    @Nullable
    private final EatEffect eatEffect;

    // ── Supplier constructors (preferred — avoids circular init NPE) ──────

    /** Lazy block reference, no special eat effect. */
    public HerbBlockItem(Supplier<? extends Block> blockSupplier, Properties properties) {
        this(blockSupplier, properties, null);
    }

    /** Lazy block reference, with a custom eat effect. */
    public HerbBlockItem(Supplier<? extends Block> blockSupplier, Properties properties,
                         @Nullable EatEffect eatEffect) {
        super(blockSupplier.get(), properties);
        this.eatEffect = eatEffect;
    }

    // ── Direct Block constructors (kept for compatibility) ────────────────

    /** Direct block reference, no special eat effect. */
    public HerbBlockItem(Block block, Properties properties) {
        this(block, properties, null);
    }

    /** Direct block reference, with a custom eat effect. */
    public HerbBlockItem(Block block, Properties properties, @Nullable EatEffect eatEffect) {
        super(block, properties);
        this.eatEffect = eatEffect;
    }

    // ── Item behaviour ────────────────────────────────────────────────────

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (eatEffect != null && !level.isClientSide && entity instanceof Player) {
            eatEffect.apply(stack, level, entity);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        HerbQuality.appendHerbTooltip(stack, tooltip);
    }
}