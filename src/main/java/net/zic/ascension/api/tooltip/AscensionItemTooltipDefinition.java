package net.zic.ascension.api.tooltip;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipPage;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTemplate;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.ZenithTooltipThemeOverride;

import java.util.List;
import java.util.Optional;

/** Tooltip metadata embedded directly inside an Ascension datapack registry entry. */
public record AscensionItemTooltipDefinition(
        Optional<Identifier> theme,
        Optional<ZenithTooltipThemeOverride> themeOverrides,
        Optional<Identifier> template,
        Optional<Identifier> rank,
        List<ZenithTooltipPage> pages,
        List<Identifier> animationPresets
) {
    public static final Codec<AscensionItemTooltipDefinition> CODEC = RecordCodecBuilder.create(instance ->
                    instance.group(
                            Identifier.CODEC.optionalFieldOf("theme").forGetter(AscensionItemTooltipDefinition::theme),
                            ZenithTooltipThemeOverride.CODEC.optionalFieldOf("theme_overrides").forGetter(AscensionItemTooltipDefinition::themeOverrides),
                            Identifier.CODEC.optionalFieldOf("template").forGetter(AscensionItemTooltipDefinition::template),
                            Identifier.CODEC.optionalFieldOf("rank").forGetter(AscensionItemTooltipDefinition::rank),
                            ZenithTooltipPage.CODEC.listOf().optionalFieldOf("pages", List.of()).forGetter(AscensionItemTooltipDefinition::pages),
                            Identifier.CODEC.listOf().optionalFieldOf("animation_presets", List.of()).forGetter(AscensionItemTooltipDefinition::animationPresets)).apply(instance, AscensionItemTooltipDefinition::new)
            );

    public AscensionItemTooltipDefinition {
        theme = theme == null ? Optional.empty() : theme;
        themeOverrides = themeOverrides == null ? Optional.empty() : themeOverrides;
        template = template == null ? Optional.empty() : template;
        rank = rank == null ? Optional.empty() : rank;
        pages = pages == null ? List.of() : List.copyOf(pages);
        animationPresets = animationPresets == null ? List.of() : List.copyOf(animationPresets);
    }

    public AscensionItemTooltipDefinition(
            Optional<Identifier> theme,
            Optional<Identifier> template,
            Optional<Identifier> rank,
            List<ZenithTooltipPage> pages,
            List<Identifier> animationPresets
    ) {
        this(theme, Optional.empty(), template, rank, pages, animationPresets);
    }

    public boolean hasInlineTemplate() {
        return !pages.isEmpty();
    }

    public ZenithTooltipTemplate inlineTemplate() {
        return new ZenithTooltipTemplate(pages, animationPresets);
    }

    public ZenithTooltipDocument themed(ZenithTooltipTheme theme) {
        return inlineTemplate().themed(theme);
    }
}