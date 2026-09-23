package net.zic.ascension.client.tooltip.providers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterialProvider;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterials;
import net.zic.ascension.common.item.artifacts.pills.PillItem;
import net.zic.ascension.common.item.herbs.HerbItem;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipProviders;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipSubject;
import net.zic.zenithlib.tooltip.manager.ZenithTooltipRepository;

import java.util.Optional;

public final class AscensionAlchemyMaterialTooltipProvider implements ZenithTooltipProviders.ContextualProvider {
    public static final Identifier ID = AscensionCraft.prefix("alchemy_material_tooltips");

    private static final Identifier DEFAULT_TEMPLATE = AscensionCraft.prefix("default_alchemy_material");
    private static final Identifier DEFAULT_THEME = AscensionCraft.prefix("artifact_themes");

    private AscensionAlchemyMaterialTooltipProvider() {
    }

    public static void register() {
        ZenithTooltipProviders.registerContextual(ID, new AscensionAlchemyMaterialTooltipProvider());
    }

    @Override
    public Optional<ZenithTooltipProviders.Result> create(ZenithTooltipContext context) {
        ItemStack stack = context.stack();
        if (stack.getItem() instanceof HerbItem || stack.getItem() instanceof PillItem) {
            return Optional.empty();
        }

        AlchemyMaterialProvider materialProvider = stack.getData(AlchemyMaterials.MATERIAL);
        if (materialProvider == null) {
            return Optional.empty();
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Optional<ZenithTooltipDocument> document = ZenithTooltipRepository.fromTemplate(DEFAULT_TEMPLATE, DEFAULT_THEME);

        return document.map(value -> ZenithTooltipProviders.Result.withSubject(
                value,
                context,
                itemId,
                materialProvider,
                ZenithTooltipSubject.of(stack.getHoverName(), Component.empty())
        ));
    }
}
