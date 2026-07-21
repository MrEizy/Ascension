package net.zic.ascension.client.tooltip.providers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipProviders;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipSubject;
import net.zic.zenithlib.tooltip.manager.ZenithTooltipRepository;

import java.util.Optional;

/**
 * ItemStack component → registry id → datapack registry entry → item_tooltip/default template → contextual ZenithLib document.
 */
public final class AscensionTransferItemTooltipProvider implements ZenithTooltipProviders.ContextualProvider {
    public static final Identifier ID = AscensionCraft.prefix("registry_tooltips");

    private static final Identifier DEFAULT_BLOODLINE_TEMPLATE = AscensionCraft.prefix("default_bloodline_essence");
    private static final Identifier DEFAULT_PHYSIQUE_TEMPLATE = AscensionCraft.prefix("default_physique_essence");
    private static final Identifier DEFAULT_TECHNIQUE_TEMPLATE = AscensionCraft.prefix("default_technique_manual");

    private static final Identifier BLOODLINE_THEME = AscensionCraft.prefix("bloodline_essence");
    private static final Identifier PHYSIQUE_THEME = AscensionCraft.prefix("physique_essence");
    private static final Identifier TECHNIQUE_THEME = AscensionCraft.prefix("technique_manual");

    private AscensionTransferItemTooltipProvider() {}

    public static void register() {
        ZenithTooltipProviders.registerContextual(ID, new AscensionTransferItemTooltipProvider());
    }

    @Override
    public Optional<ZenithTooltipProviders.Result> create(ZenithTooltipContext context) {
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
                            physique.itemTooltip(),
                            DEFAULT_PHYSIQUE_TEMPLATE,
                            PHYSIQUE_THEME
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
                            technique.itemTooltip(),
                            DEFAULT_TECHNIQUE_TEMPLATE,
                            TECHNIQUE_THEME
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
                            bloodline.itemTooltip(),
                            DEFAULT_BLOODLINE_TEMPLATE,
                            BLOODLINE_THEME
                    );
        }

        return Optional.empty();
    }

    private static <T> Optional<ZenithTooltipProviders.Result> toResult(
            ZenithTooltipContext context,
            Identifier registryId,
            T subjectValue,
            Component name,
            Component description,
            Optional<AscensionItemTooltipDefinition> tooltip,
            Identifier defaultTemplate,
            Identifier defaultTheme
    ) {
        return resolveDocument(tooltip, defaultTemplate, defaultTheme)
                .map(document -> ZenithTooltipProviders.Result.withSubject(
                        document,
                        context,
                        registryId,
                        subjectValue,
                        ZenithTooltipSubject.of(name, description)
                ));
    }

    private static Optional<ZenithTooltipDocument> resolveDocument(
            Optional<AscensionItemTooltipDefinition> tooltip,
            Identifier defaultTemplate,
            Identifier defaultTheme
    ) {
        if (tooltip.isEmpty()) {
            return ZenithTooltipRepository.fromTemplate(defaultTemplate, defaultTheme);
        }

        AscensionItemTooltipDefinition definition = tooltip.orElseThrow();
        if (definition.hasInlineTemplate()) {
            return Optional.of(definition.themed(resolveTheme(definition, defaultTheme)));
        }

        return ZenithTooltipRepository.fromTemplate(
                definition.template().orElse(defaultTemplate),
                definition.theme().orElse(defaultTheme)
        );
    }

    private static ZenithTooltipTheme resolveTheme(
            AscensionItemTooltipDefinition definition,
            Identifier defaultTheme
    ) {
        return ZenithTooltipRepository.themesView()
                .getOrDefault(
                        definition.theme().orElse(defaultTheme),
                        ZenithTooltipTheme.defaultTheme()
                );
    }
}
