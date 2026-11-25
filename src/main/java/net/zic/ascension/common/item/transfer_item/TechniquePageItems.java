package net.zic.ascension.common.item.transfer_item;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.impl.core.technique.Paged;
import org.jspecify.annotations.Nullable;

/**
 * Builds the stacks of the technique page system, and finds a technique from a tooltip.
 */
public final class TechniquePageItems {
    private TechniquePageItems() {
    }

    /** the finished technique manual a complete binder turns into */
    public static ItemStack manual(Identifier techniqueId, Technique<?> technique) {
        ItemStack stack = new ItemStack(ModItems.TECHNIQUE_MANUAL.get());
        stack.set(AscensionComponents.REGISTRY_ID_HOLDER.get(), techniqueId);
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.ascension.technique_manual.named", technique.getName(null)));
        return stack;
    }

    /** one page (index starts at 0) of a lost technique */
    public static ItemStack page(Identifier techniqueId, Technique<?> technique, int index) {
        ItemStack stack = new ItemStack(ModItems.TECHNIQUE_PAGE.get());
        stack.set(AscensionComponents.REGISTRY_ID_HOLDER.get(), techniqueId);
        stack.set(AscensionComponents.PAGE_INDEX.get(), index);
        Component pageName = technique instanceof Paged paged ? paged.pages().pageName(index) : Component.literal(String.valueOf(index + 1));
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.ascension.technique_page.named", technique.getName(null), pageName));
        return stack;
    }

    /** looks a technique up from the registries a tooltip is given, null if it cannot be found */
    public static @Nullable Technique<?> find(HolderLookup.@Nullable Provider registries, Identifier techniqueId) {
        if (registries == null) {
            return null;
        }
        return registries.lookup(CoreRegistries.TECHNIQUE_REGISTRY.key())
                .flatMap(lookup -> lookup.get(ResourceKey.create(CoreRegistries.TECHNIQUE_REGISTRY.key(), techniqueId)))
                .map(Holder.Reference::value)
                .orElse(null);
    }
}