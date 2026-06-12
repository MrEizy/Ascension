package net.zic.ascension.client.tooltip.providers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.zenithlib.tooltip.api.ZenithContextualTooltipDocumentProvider;
import net.zic.zenithlib.tooltip.api.ZenithTooltipProviderResult;
import net.zic.zenithlib.tooltip.api.ZenithTooltipProviders;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipSubject;
import net.zic.zenithlib.tooltip.manager.ZenithTooltipRepository;

import java.util.Optional;

/**
 * ItemStack component → registry id → datapack registry entry → item_tooltip → contextual ZenithLib document.
 */
public final class AscensionTransferItemTooltipProvider implements ZenithContextualTooltipDocumentProvider {
    public static final Identifier ID = AscensionCraft.prefix("registry_tooltips");

    private AscensionTransferItemTooltipProvider() {}

    public static void register() {
        ZenithTooltipProviders.registerContextual(ID, new AscensionTransferItemTooltipProvider());
    }

    @Override
    public Optional<ZenithTooltipProviderResult> create(ZenithTooltipContext context) {
        ItemStack stack = context.stack();

        if (context.registryAccess().isEmpty() || !stack.has(AscensionComponents.REGISTRY_ID_HOLDER)) {
            return Optional.empty();
        }

        Identifier registryId = stack.get(AscensionComponents.REGISTRY_ID_HOLDER);
        if (registryId == null) {
            return Optional.empty();
        }

        RegistryAccess access = context.registryAccess().orElseThrow();

        if (stack.getItem() == ModItems.PHYSIQUE_ESSENCE.get()) {
            Physique physique = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, registryId, access);
            return physique == null
                    ? Optional.empty()
                    : toResult(
                            context,
                            registryId,
                            physique,
                            physique.name(),
                            physique.description(),
                            physique.itemTooltip()
                    );
        }

        if (stack.getItem() == ModItems.TECHNIQUE_MANUAL.get()) {
            Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, registryId, access);
            return technique == null
                    ? Optional.empty()
                    : toResult(
                            context,
                            registryId,
                            technique,
                            technique.getName(null),
                            technique.getDescription(null),
                            technique.itemTooltip()
                    );
        }

        if (stack.getItem() == ModItems.BLOODLINE_ESSENCE.get()) {
            Bloodline bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY, registryId, access);
            return bloodline == null
                    ? Optional.empty()
                    : toResult(
                            context,
                            registryId,
                            bloodline,
                            bloodline.getName(),
                            bloodline.getDescription(),
                            bloodline.itemTooltip()
                    );
        }

        return Optional.empty();
    }

    private static <T> Optional<ZenithTooltipProviderResult> toResult(
            ZenithTooltipContext context,
            Identifier registryId,
            T subjectValue,
            Component name,
            Component description,
            Optional<AscensionItemTooltipDefinition> tooltip
    ) {
        return tooltip.map(definition -> ZenithTooltipProviderResult.withSubject(
                definition.themed(resolveTheme(definition)),
                context,
                registryId,
                subjectValue,
                ZenithTooltipSubject.of(name, description)
        ));
    }

    private static ZenithTooltipTheme resolveTheme(AscensionItemTooltipDefinition definition) {
        return definition.theme()
                .map(id -> ZenithTooltipRepository.themesView().getOrDefault(id, ZenithTooltipTheme.defaultTheme()))
                .orElseGet(ZenithTooltipTheme::defaultTheme);
    }
}
