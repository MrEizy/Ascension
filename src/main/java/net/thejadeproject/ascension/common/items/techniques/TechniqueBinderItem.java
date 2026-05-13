package net.thejadeproject.ascension.common.items.techniques;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.helpers.TechniqueManualRegistry;

import java.util.ArrayList;
import java.util.List;

public class TechniqueBinderItem extends Item {
    public TechniqueBinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String techniqueId = stack.get(ModDataComponents.TECHNIQUE_ID.get());
        List<Integer> collectedIndices = stack.getOrDefault(ModDataComponents.COLLECTED_PAGE_INDICES.get(), new ArrayList<>());

        if (techniqueId != null) {
            ResourceLocation id = ResourceLocation.parse(techniqueId);
            ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(id);
            var manualData = TechniqueManualRegistry.get(id);

            if (technique != null) {
                tooltipComponents.add(Component.translatable("ascension.binder.technique", technique.getDisplayTitle()).withStyle(ChatFormatting.GOLD));
            }

            if (manualData.isPresent()) {
                int required = manualData.get().requiredPages();
                tooltipComponents.add(Component.translatable("ascension.binder.progress", collectedIndices.size(), required).withStyle(ChatFormatting.GRAY));

                List<String> chapters = manualData.get().chapterTranslationKeys();
                for (int i = 0; i < chapters.size(); i++) {
                    boolean hasPage = collectedIndices.contains(i);
                    ChatFormatting color = hasPage ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY;
                    String symbol = hasPage ? "✓" : "✗";
                    tooltipComponents.add(Component.literal("  " + symbol + " ").append(Component.translatable(chapters.get(i))).withStyle(color));
                }
            }
        } else {
            tooltipComponents.add(Component.translatable("ascension.binder.empty").withStyle(ChatFormatting.GRAY));
        }
    }
}