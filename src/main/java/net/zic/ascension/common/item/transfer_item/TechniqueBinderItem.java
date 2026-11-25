package net.zic.ascension.common.item.transfer_item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.impl.core.technique.Paged;

import java.util.List;
import java.util.function.Consumer;

/**
 * Collects technique pages. It does nothing itself: hold it in the off hand and use a technique page. Its technique is the
 * registry_id_holder of the stack and the pages it has are its collected_pages.
 */
public class TechniqueBinderItem extends Item {
    public TechniqueBinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        Identifier techniqueId = stack.get(AscensionComponents.REGISTRY_ID_HOLDER.get());
        if (techniqueId == null) {
            lines.accept(Component.translatable("ascension.binder.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        List<Integer> collected = stack.getOrDefault(AscensionComponents.COLLECTED_PAGES.get(), List.of());
        Technique<?> technique = TechniquePageItems.find(context.registries(), techniqueId);
        if (technique != null) {
            lines.accept(Component.translatable("ascension.binder.technique", technique.getName(null)).withStyle(ChatFormatting.GOLD));
        }
        if (technique instanceof Paged paged) {
            int required = paged.pages().amount();
            lines.accept(Component.translatable("ascension.binder.progress", collected.size(), required).withStyle(ChatFormatting.GRAY));
            for (int i = 0; i < required; i++) {
                boolean has = collected.contains(i);
                lines.accept(Component.literal("  " + (has ? "\u2713" : "\u2717") + " ")
                        .append(paged.pages().pageName(i))
                        .withStyle(has ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            }
        }
    }
}