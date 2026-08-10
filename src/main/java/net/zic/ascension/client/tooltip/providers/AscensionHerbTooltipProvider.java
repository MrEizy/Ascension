package net.zic.ascension.client.tooltip.providers;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.herbs.HerbDefinition;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.common.item.herbs.HerbItem;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipProviders;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipSubject;
import net.zic.zenithlib.tooltip.manager.ZenithTooltipRepository;

import java.util.Optional;

/**
 * Supplies the stack-aware tooltip used by harvested herbs
 */
public final class AscensionHerbTooltipProvider implements ZenithTooltipProviders.ContextualProvider {
    public static final Identifier ID = AscensionCraft.prefix("herb_tooltips");

    private static final Identifier DEFAULT_TEMPLATE = AscensionCraft.prefix("default_herb");
    private static final Identifier DEFAULT_THEME = AscensionCraft.prefix("herb");

    private AscensionHerbTooltipProvider() {}

    public static void register() {
        ZenithTooltipProviders.registerContextual(ID, new AscensionHerbTooltipProvider());
    }

    @Override
    public Optional<ZenithTooltipProviders.Result> create(ZenithTooltipContext context) {
        ItemStack stack = context.stack();
        if (!(stack.getItem() instanceof HerbItem herbItem)) {
            return Optional.empty();
        }

        HerbDefinition definition = herbItem.definition();
        AscensionComponents.HerbData data = herbItem.data(stack);
        int years = definition.ageThreshold(data.ageTier()).years();

        Component baseName = stack.getHoverName();
        Component displayName = years > 1
                ? Component.translatable("ascension.herb.tooltip.aged_name", years, baseName)
                : baseName;

        Optional<ZenithTooltipDocument> document = ZenithTooltipRepository.fromTemplate(
                DEFAULT_TEMPLATE,
                DEFAULT_THEME
        );

        return document.map(value -> ZenithTooltipProviders.Result.withSubject(
                value,
                context,
                definition.id(),
                definition,
                ZenithTooltipSubject.of(displayName, herbDescription(definition))
        ));
    }

    private static Component herbDescription(HerbDefinition definition) {
        Identifier id = definition.id();
        return Component.translatable(id.getNamespace() + ".herb." + id.getPath() + ".description");
    }
}
