package net.zic.ascension.client.tooltip.providers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.artifacts.pills.PillItem;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.zenithlib.classification.ZenithClassification;
import net.zic.zenithlib.classification.ZenithClassifications;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipProviders;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipSubject;
import net.zic.zenithlib.tooltip.manager.ZenithTooltipRepository;

import java.util.Optional;

public final class AscensionPillTooltipProvider implements ZenithTooltipProviders.ContextualProvider {
    public static final Identifier ID = AscensionCraft.prefix("pill_tooltips");
    public static final Identifier CLASSIFICATION_ID = AscensionCraft.prefix("pill_classifications");
    public static final Identifier CATEGORY = AscensionCraft.prefix("pill");

    private static final Identifier DEFAULT_TEMPLATE = AscensionCraft.prefix("default_pill");
    private static final Identifier DEFAULT_THEME = AscensionCraft.prefix("artifact_themes");

    private AscensionPillTooltipProvider() {
    }

    public static void register() {
        ZenithTooltipProviders.registerContextual(ID, new AscensionPillTooltipProvider());
        ZenithClassifications.registerProvider(CLASSIFICATION_ID, AscensionPillTooltipProvider::classification);
    }

    @Override
    public Optional<ZenithTooltipProviders.Result> create(ZenithTooltipContext context) {
        ItemStack stack = context.stack();
        if (!(stack.getItem() instanceof PillItem pill)) {
            return Optional.empty();
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Optional<ZenithTooltipDocument> document = ZenithTooltipRepository.fromTemplate(DEFAULT_TEMPLATE, DEFAULT_THEME);

        return document.map(value -> ZenithTooltipProviders.Result.withSubject(
                value,
                context,
                itemId,
                pill.definition(),
                ZenithTooltipSubject.of(
                        stack.getHoverName(),
                        Component.translatable(itemId.getNamespace() + ".pill." + itemId.getPath() + ".description")
                )
        ));
    }

    private static Optional<ZenithClassification> classification(ItemStack stack, Identifier itemId) {
        if (!(stack.getItem() instanceof PillItem pill)) {
            return Optional.empty();
        }

        AscensionComponents.PillData data = pill.data(stack);
        Optional<ZenithClassification.Category> category = Optional.ofNullable(ZenithClassifications.categoriesView().get(CATEGORY));
        Optional<ZenithClassification.Rank> rank = Optional.ofNullable(ZenithClassifications.ranksView().get(data.rank()));

        return Optional.of(new ZenithClassification(Optional.of(CATEGORY), category, Optional.of(data.rank()), rank));
    }
}
