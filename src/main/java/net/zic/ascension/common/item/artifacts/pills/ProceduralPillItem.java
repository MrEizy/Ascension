package net.zic.ascension.common.item.artifacts.pills;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalEffectApplicator;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalProfile;
import net.zic.ascension.common.item.components.AscensionComponents;

import java.util.function.Consumer;
import java.util.Locale;

/**
 * A single consumable whose persistent profile defines every individual pill's actual medicine.
 * Unconfigured stacks cannot be consumed; only the server can apply the resolved medicinal effects.
 */
public final class ProceduralPillItem extends Item {
    public ProceduralPillItem(Properties properties) {
        super(properties.component(DataComponents.CONSUMABLE,
                Consumables.defaultFood().consumeSeconds(1.0F).build()));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        MedicinalProfile profile = player.getItemInHand(hand).get(AscensionComponents.MEDICINAL_PILL.get());
        if (profile == null || profile.composition().isEmpty() || profile.effects().isEmpty()) return InteractionResult.FAIL;
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        MedicinalProfile profile = stack.get(AscensionComponents.MEDICINAL_PILL.get());
        if (profile == null || profile.composition().isEmpty() || profile.effects().isEmpty()) return stack;
        if (!level.isClientSide()) MedicinalEffectApplicator.consume(profile, entity);
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        MedicinalProfile profile = stack.get(AscensionComponents.MEDICINAL_PILL.get());
        if (profile == null) {
            tooltip.accept(Component.translatable("ascension.alchemy.pill.inert").withStyle(ChatFormatting.GRAY));
            return;
        }
        var data = stack.getOrDefault(AscensionComponents.PILL_DATA.get(), AscensionComponents.PillData.DEFAULT);
        tooltip.accept(Component.translatable("ascension.alchemy.pill.rank", data.rank().getPath(),
                Component.translatable(data.grade().translationKey())).withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("ascension.alchemy.pill.purity",
                Math.round(profile.composition().substance().purity() * 100.0D)).withStyle(ChatFormatting.AQUA));
        tooltip.accept(Component.translatable("ascension.alchemy.pill.toxicity",
                String.format(Locale.ROOT, "%.2f", profile.composition().toxicity())).withStyle(ChatFormatting.GRAY));
        for (MedicinalProfile.Effect effect : profile.effects()) {
            tooltip.accept(Component.literal((effect.adverse() ? "− " : "+ ")
                    + effect.id().getPath().replace("alchemy/effect/", "").replace('_', ' ')
                    + "  " + String.format(Locale.ROOT, "%.2f", effect.potency())
                    + (effect.durationTicks() > 0 ? "  (" + effect.durationTicks() / 20 + "s)" : ""))
                    .withStyle(effect.adverse() ? ChatFormatting.RED : ChatFormatting.GREEN));
        }
    }
}
