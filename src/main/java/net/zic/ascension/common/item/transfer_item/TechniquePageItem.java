package net.zic.ascension.common.item.transfer_item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.impl.core.technique.Paged;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * One page of a lost_technique. Hold a technique binder in the off hand and use the page in the main hand to put it in the
 * binder. When the binder has every page it turns into the technique's manual.
 * The technique is the registry_id_holder of the stack, the page is its page_index (starting at 0).
 */
public class TechniquePageItem extends Item {
    public TechniquePageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack page = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack binder = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!(binder.getItem() instanceof TechniqueBinderItem)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Identifier techniqueId = page.get(AscensionComponents.REGISTRY_ID_HOLDER.get());
        Integer pageIndex = page.get(AscensionComponents.PAGE_INDEX.get());
        if (techniqueId == null || pageIndex == null) {
            return InteractionResult.FAIL;
        }
        Technique<?> technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, techniqueId, level.registryAccess());
        if (!(technique instanceof Paged paged) || pageIndex < 0 || pageIndex >= paged.pages().amount()) {
            player.sendOverlayMessage(Component.translatable("ascension.page.invalid").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        Identifier boundTechnique = binder.get(AscensionComponents.REGISTRY_ID_HOLDER.get());
        List<Integer> collected = binder.getOrDefault(AscensionComponents.COLLECTED_PAGES.get(), List.of());
        if (boundTechnique == null) {
            // an empty binder takes on the technique of the first page put in it
            binder.set(AscensionComponents.REGISTRY_ID_HOLDER.get(), techniqueId);
            collected = List.of();
        } else if (!boundTechnique.equals(techniqueId)) {
            player.sendOverlayMessage(Component.translatable("ascension.page.mismatch").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        } else if (collected.contains(pageIndex)) {
            player.sendOverlayMessage(Component.translatable("ascension.page.duplicate").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        List<Integer> updated = new ArrayList<>(collected);
        updated.add(pageIndex);
        binder.set(AscensionComponents.COLLECTED_PAGES.get(), List.copyOf(updated));

        int required = paged.pages().amount();
        if (updated.size() >= required) {
            player.setItemInHand(InteractionHand.OFF_HAND, TechniquePageItems.manual(techniqueId, technique));
            player.sendOverlayMessage(Component.translatable("ascension.page.complete", technique.getName(null)).withStyle(ChatFormatting.GOLD));
        } else {
            player.sendOverlayMessage(Component.translatable("ascension.page.added",
                    paged.pages().pageName(pageIndex), updated.size(), required).withStyle(ChatFormatting.GREEN));
        }
        if (!player.getAbilities().instabuild) {
            page.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        Identifier techniqueId = stack.get(AscensionComponents.REGISTRY_ID_HOLDER.get());
        Integer pageIndex = stack.get(AscensionComponents.PAGE_INDEX.get());
        if (techniqueId != null && pageIndex != null
                && TechniquePageItems.find(context.registries(), techniqueId) instanceof Paged paged) {
            lines.accept(paged.pages().pageName(pageIndex).copy().withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, display, lines, flag);
    }
}