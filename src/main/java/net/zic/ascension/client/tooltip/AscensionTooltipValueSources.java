package net.zic.ascension.client.tooltip;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressActionDescription;
import net.zic.ascension.api.ascension.core.progression.ProgressActionCondition;
import net.zic.ascension.api.ascension.core.progression.ProgressActionConditionReference;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.progression.ProgressActionReference;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueSkillCap;
import net.zic.ascension.api.ascension.core.technique.TechniqueSkillDefinition;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.datapack.path.PathBonusBase;
import net.zic.ascension.api.ascension.datapack.path.PathBonusModifier;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.herbs.HerbDefinition;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.common.item.herbs.HerbItem;
import net.zic.ascension.client.tooltip.providers.AscensionHerbRelatedTooltipProvider;
import net.zic.ascension.impl.core.bloodline.SimpleBloodline;
import net.zic.ascension.impl.core.physique.SimplePhysique;
import net.zic.ascension.impl.core.skill.castable.ActiveSkill;
import net.zic.ascension.impl.core.technique.SimpleTechnique;

import net.zic.ascension.util.PathInteractionUtil;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.animation.RuneDecipherTextEffect;
import net.zic.zenithlib.tooltip.api.animation.ScrambleRevealTextEffect;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.api.element.BadgeElement;
import net.zic.zenithlib.tooltip.api.element.BadgeRowElement;
import net.zic.zenithlib.tooltip.api.element.RowElement;
import net.zic.zenithlib.tooltip.api.element.TextElement;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;
import net.zic.zenithlib.tooltip.api.value.ZenithTooltipSources;
import net.zic.zenithlib.tooltip.api.value.ZenithTooltipValue;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.StringJoiner;
import java.util.TreeMap;

/**
 * Registers Ascension-owned runtime values referenced by registry item tooltips.
 */
public final class AscensionTooltipValueSources {
    public static final Identifier PHYSIQUE_PATHS = AscensionCraft.prefix("physique_paths");
    public static final Identifier PHYSIQUE_STATS = AscensionCraft.prefix("physique_stats");
    public static final Identifier PHYSIQUE_AFFINITIES = AscensionCraft.prefix("physique_affinities");

    public static final Identifier TECHNIQUE_PATH = AscensionCraft.prefix("technique_path");
    public static final Identifier TECHNIQUE_MAX_REALM = AscensionCraft.prefix("technique_max_realm");
    public static final Identifier TECHNIQUE_PROGRESSION_GAINS = AscensionCraft.prefix("technique_progression_gains");

    public static final Identifier BLOODLINE_PURITY = AscensionCraft.prefix("bloodline_purity");
    public static final Identifier BLOODLINE_PURITY_GAINS = AscensionCraft.prefix("bloodline_purity_gains");

    public static final Identifier HERB_AGE = AscensionCraft.prefix("herb_age");
    public static final Identifier HERB_QUALITY = AscensionCraft.prefix("herb_quality");
    public static final Identifier HERB_QUALITY_BADGE = AscensionCraft.prefix("herb_quality_badge");
    public static final Identifier HERB_ORIGIN = AscensionCraft.prefix("herb_origin");
    public static final Identifier HERB_RELATED_TYPE_BADGE = AscensionCraft.prefix("herb_related_type_badge");
    public static final Identifier HERB_RELATED_TARGET_ROW = AscensionCraft.prefix("herb_related_target_row");

    private static final int TECHNIQUE_REVEAL_LOOKAHEAD = 3;

    private static final ScrambleRevealTextEffect BEYOND_COMPREHENSION_EFFECT =
            new ScrambleRevealTextEffect(
                    0.0F,
                    55,
                    ScrambleRevealTextEffect.Mode.SCATTERED,
                    RuneDecipherTextEffect.DEFAULT_RUNE_GLYPHS
            );

    private static boolean registered;

    private AscensionTooltipValueSources() {}

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ZenithTooltipSources.registerValue(PHYSIQUE_PATHS, AscensionTooltipValueSources::physiquePaths);
        ZenithTooltipSources.registerValue(PHYSIQUE_STATS, AscensionTooltipValueSources::physiqueStats);
        ZenithTooltipSources.registerValue(PHYSIQUE_AFFINITIES, AscensionTooltipValueSources::physiqueAffinities);

        ZenithTooltipSources.registerValue(TECHNIQUE_PATH, AscensionTooltipValueSources::techniquePath);
        ZenithTooltipSources.registerValue(TECHNIQUE_MAX_REALM, AscensionTooltipValueSources::techniqueMaxRealm);

        ZenithTooltipSources.registerValue(BLOODLINE_PURITY, AscensionTooltipValueSources::bloodlinePurity);

        ZenithTooltipSources.registerValue(HERB_AGE, AscensionTooltipValueSources::herbAge);
        ZenithTooltipSources.registerValue(HERB_QUALITY, AscensionTooltipValueSources::herbQuality);
        ZenithTooltipSources.registerValue(HERB_ORIGIN, AscensionTooltipValueSources::herbOrigin);

        ZenithTooltipSources.registerElement(PHYSIQUE_PATHS, context -> badges(PHYSIQUE_PATHS, context, ZenithTooltipColor.ACCENT));
        ZenithTooltipSources.registerElement(PHYSIQUE_STATS, context -> rows(PHYSIQUE_STATS, context));
        ZenithTooltipSources.registerElement(PHYSIQUE_AFFINITIES, context -> rows(PHYSIQUE_AFFINITIES, context));
        ZenithTooltipSources.registerElement(TECHNIQUE_PROGRESSION_GAINS, AscensionTooltipValueSources::techniqueProgressionElements);
        ZenithTooltipSources.registerElement(BLOODLINE_PURITY_GAINS, AscensionTooltipValueSources::bloodlinePurityElements);
        ZenithTooltipSources.registerElement(HERB_QUALITY_BADGE, AscensionTooltipValueSources::herbQualityBadge);
        ZenithTooltipSources.registerElement(HERB_RELATED_TYPE_BADGE, AscensionTooltipValueSources::herbRelatedTypeBadge);
        ZenithTooltipSources.registerElement(HERB_RELATED_TARGET_ROW, AscensionTooltipValueSources::herbRelatedTargetRow);
    }

    private static List<ZenithTooltipElement> badges(
            Identifier source,
            ZenithTooltipContext context,
            ZenithTooltipColor color
    ) {
        List<BadgeElement> badges = ZenithTooltipSources.resolveValue(source, context, ZenithTooltipValue.TextList.class)
                .stream()
                .flatMap(value -> value.entries().stream())
                .map(component -> new BadgeElement(
                        ZenithTooltipText.resolved(component),
                        color,
                        ZenithTooltipColor.BACKGROUND,
                        color
                ).withBackgroundGradient(
                        BadgeElement.GradientDirection.HORIZONTAL,
                        ZenithTooltipColor.BACKGROUND,
                        ZenithTooltipColor.BORDER_BOTTOM
                ))
                .toList();

        return badges.isEmpty()
                ? List.of()
                : List.of(new BadgeRowElement(badges));
    }

    private static List<ZenithTooltipElement> rows(
            Identifier source,
            ZenithTooltipContext context
    ) {
        return ZenithTooltipSources.resolveValue(source, context, ZenithTooltipValue.Rows.class)
                .stream()
                .flatMap(value -> value.entries().stream())
                .map(row -> new RowElement(
                        ZenithTooltipText.resolved(row.left()),
                        ZenithTooltipText.resolved(row.right()),
                        ZenithTooltipColor.TEXT,
                        toneColor(row.tone())
                ))
                .map(element -> (ZenithTooltipElement) element)
                .toList();
    }

    private static ZenithTooltipColor toneColor(ZenithTooltipValue.Tone tone) {
        return switch (tone) {
            case POSITIVE -> ZenithTooltipColor.POSITIVE;
            case NEGATIVE -> ZenithTooltipColor.NEGATIVE;
            case SPECIAL -> ZenithTooltipColor.ACCENT;
            case NEUTRAL -> ZenithTooltipColor.MUTED;
        };
    }

    private static ZenithTooltipColor toneColor(ProgressActionDescription.Tone tone) {
        return switch (tone) {
            case POSITIVE -> ZenithTooltipColor.POSITIVE;
            case NEGATIVE -> ZenithTooltipColor.NEGATIVE;
            case SPECIAL -> ZenithTooltipColor.ACCENT;
            case NEUTRAL -> ZenithTooltipColor.MUTED;
        };
    }

    private static Optional<ZenithTooltipValue> physiquePaths(
            ZenithTooltipContext context
    ) {
        Optional<SimplePhysique> physique =
                context.subject(SimplePhysique.class);

        if (physique.isEmpty() || context.registryAccess().isEmpty()) {
            return Optional.empty();
        }

        RegistryAccess access = context.registryAccess().orElseThrow();

        List<Component> paths = physique.orElseThrow()
                .unlockedPaths()
                .stream()
                .map(id -> pathName(id, access))
                .toList();

        return Optional.of(ZenithTooltipValue.textList(paths));
    }

    private static Optional<ZenithTooltipValue> physiqueStats(
            ZenithTooltipContext context
    ) {
        return context.subject(SimplePhysique.class).map(physique ->
                ZenithTooltipValue.rows(
                        combinedStatRows(
                                physique.baseStats(),
                                physique.statModifiers()
                        )
                )
        );
    }

    private static Optional<ZenithTooltipValue> physiqueAffinities(
            ZenithTooltipContext context
    ) {
        if (context.registryAccess().isEmpty()) {
            return Optional.empty();
        }

        RegistryAccess access = context.registryAccess().orElseThrow();
        return context.subject(SimplePhysique.class).map(physique -> {
            List<PathBonusBase> baseAffinities = physique.basePathBonuses()
                    .stream()
                    .filter(value -> value.category().equals(PathInteractionUtil.AFFINITY_CATEGORY))
                    .toList();

            List<PathBonusModifier> affinityModifiers = physique.pathBonusModifiers()
                    .stream()
                    .filter(value -> value.category().equals(PathInteractionUtil.AFFINITY_CATEGORY))
                    .toList();

            return ZenithTooltipValue.rows(
                    combinedAffinityRows(
                            baseAffinities,
                            affinityModifiers,
                            access
                    )
            );
        });
    }

    private static Optional<ZenithTooltipValue> techniquePath(
            ZenithTooltipContext context
    ) {
        Optional<Technique> technique = context.subject(Technique.class);

        if (technique.isEmpty() || context.registryAccess().isEmpty()) {
            return Optional.empty();
        }

        Identifier pathId = technique.orElseThrow().getPath();
        if (pathId == null) {
            return Optional.empty();
        }

        return Optional.of(
                ZenithTooltipValue.text(
                        pathName(
                                pathId,
                                context.registryAccess().orElseThrow()
                        )
                )
        );
    }

    private static Optional<ZenithTooltipValue> techniqueMaxRealm(
            ZenithTooltipContext context
    ) {
        Optional<Technique> technique = context.subject(Technique.class);

        if (technique.isEmpty() || context.registryAccess().isEmpty()) {
            return Optional.empty();
        }

        int maxRealm = technique.orElseThrow().getMaxMajorRealm(
                null,
                context.registryAccess().orElseThrow()
        );

        return Optional.of(
                ZenithTooltipValue.text(
                        Component.literal(Integer.toString(maxRealm))
                )
        );
    }

    private static Optional<ZenithTooltipValue> bloodlinePurity(
            ZenithTooltipContext context
    ) {
        if (context.subject(Bloodline.class).isEmpty()) {
            return Optional.empty();
        }

        int purity = context.stack().getOrDefault(
                AscensionComponents.PURITY,
                1
        );

        purity = Math.max(1, Math.min(100, purity));

        return Optional.of(
                ZenithTooltipValue.progress(
                        purity,
                        100,
                        Component.translatable(
                                "ascension.tooltip.value.percent",
                                purity
                        )
                )
        );
    }

    private static List<ZenithTooltipValue.Row> combinedStatRows(
            Collection<ValueContainer.BaseModifier> baseStats,
            Map<Identifier, List<ValueContainerModifier>> modifiers
    ) {
        Map<Identifier, List<String>> values = new java.util.TreeMap<>(
                Comparator.comparing(Identifier::toString)
        );
        Map<Identifier, Double> tones = new java.util.HashMap<>();

        for (ValueContainer.BaseModifier base : baseStats) {
            values.computeIfAbsent(base.container(), ignored -> new ArrayList<>())
                    .add(signedNumber(base.val()));
            tones.merge(base.container(), base.val(), AscensionTooltipValueSources::mergeTone);
        }

        modifiers.forEach((stat, statModifiers) -> statModifiers.stream()
                .sorted(Comparator.comparing(modifier -> modifier.getIdentifier().toString()))
                .forEach(modifier -> {
                    values.computeIfAbsent(stat, ignored -> new ArrayList<>())
                            .add(formatModifier(modifier));
                    tones.merge(stat, modifier.getVal(), AscensionTooltipValueSources::mergeTone);
                }));

        return values.entrySet().stream()
                .map(entry -> ZenithTooltipValue.row(
                        statName(entry.getKey()),
                        Component.literal(String.join(" · ", entry.getValue())),
                        tone(tones.getOrDefault(entry.getKey(), 0.0))
                ))
                .toList();
    }

    private static List<ZenithTooltipValue.Row> combinedAffinityRows(
            Collection<PathBonusBase> baseAffinities,
            Collection<PathBonusModifier> modifiers,
            RegistryAccess access
    ) {
        Map<Identifier, List<String>> values = new java.util.TreeMap<>(
                Comparator.comparing(Identifier::toString)
        );
        Map<Identifier, Double> tones = new java.util.HashMap<>();

        for (PathBonusBase base : baseAffinities) {
            values.computeIfAbsent(base.path(), ignored -> new ArrayList<>())
                    .add(signedNumber(base.value()));
            tones.merge(base.path(), base.value(), AscensionTooltipValueSources::mergeTone);
        }

        modifiers.stream()
                .sorted(Comparator.comparing(
                        modifier -> modifier.path() + "|" + modifier.modifier().getIdentifier()
                ))
                .forEach(modifier -> {
                    values.computeIfAbsent(modifier.path(), ignored -> new ArrayList<>())
                            .add(formatModifier(modifier.modifier()));
                    tones.merge(
                            modifier.path(),
                            modifier.modifier().getVal(),
                            AscensionTooltipValueSources::mergeTone
                    );
                });

        return values.entrySet().stream()
                .map(entry -> ZenithTooltipValue.row(
                        pathName(entry.getKey(), access),
                        Component.literal(String.join(" · ", entry.getValue())),
                        tone(tones.getOrDefault(entry.getKey(), 0.0))
                ))
                .toList();
    }

    private static double mergeTone(double current, double next) {
        if (current == 0.0) {
            return next;
        }
        if (next == 0.0) {
            return current;
        }
        return Math.signum(current) == Math.signum(next) ? current : 0.0;
    }

    private static List<ZenithTooltipElement> techniqueProgressionElements(
            ZenithTooltipContext context
    ) {
        Optional<SimpleTechnique> technique = context.subject(SimpleTechnique.class);
        if (technique.isEmpty() || context.registryAccess().isEmpty()) {
            return List.of();
        }

        SimpleTechnique resolvedTechnique = technique.orElseThrow();
        RegistryAccess access = context.registryAccess().orElseThrow();
        int currentMajorRealm = currentTechniqueMajorRealm(resolvedTechnique);
        List<ZenithTooltipElement> elements = new ArrayList<>();
        elements.addAll(techniqueSkillProgressionElements(resolvedTechnique, access, currentMajorRealm));
        elements.addAll(progressionElements(
                resolvedTechnique.getHolder(),
                access,
                OptionalInt.of(currentMajorRealm)
        ));
        return List.copyOf(elements);
    }

    private static List<ZenithTooltipElement> techniqueSkillProgressionElements(
            SimpleTechnique technique,
            RegistryAccess access,
            int currentMajorRealm
    ) {
        Map<Integer, List<ZenithTooltipElement>> rows = new TreeMap<>();

        technique.getSkills().entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .forEach(entry -> {
                    Identifier skillId = entry.getKey();
                    TechniqueSkillDefinition definition = entry.getValue();
                    addTechniqueSkillRow(
                            rows,
                            definition.unlock(),
                            currentMajorRealm,
                            skillName(skillId, access),
                            Component.translatable("ascension.tooltip.progression.unlock")
                    );

                    definition.caps().entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .filter(capEntry -> capEntry.getValue().progression() > 1)
                            .forEach(capEntry -> addTechniqueSkillRow(
                                    rows,
                                    capEntry.getKey(),
                                    currentMajorRealm,
                                    skillName(skillId, access),
                                    techniqueSkillCapValue(skillId, capEntry.getValue(), access)
                            ));
                });

        List<ZenithTooltipElement> elements = new ArrayList<>();
        rows.forEach((realm, realmRows) -> {
            Component label = realm == 0
                    ? Component.translatable("ascension.tooltip.progression.condition.on_learn")
                    : Component.translatable("ascension.tooltip.progression.condition.major_realm", realm);
            elements.add(new RowElement(
                    ZenithTooltipText.resolved(label),
                    ZenithTooltipText.resolved(Component.empty()),
                    ZenithTooltipColor.ACCENT,
                    ZenithTooltipColor.MUTED
            ));
            elements.addAll(realmRows);
        });
        return List.copyOf(elements);
    }

    private static void addTechniqueSkillRow(
            Map<Integer, List<ZenithTooltipElement>> rows,
            int realm,
            int currentMajorRealm,
            Component label,
            Component value
    ) {
        ZenithTooltipElement element = realm > currentMajorRealm + TECHNIQUE_REVEAL_LOOKAHEAD
                ? beyondComprehensionElement()
                : new RowElement(
                        ZenithTooltipText.resolved(label),
                        ZenithTooltipText.resolved(value),
                        ZenithTooltipColor.TEXT,
                        ZenithTooltipColor.ACCENT
                );
        rows.computeIfAbsent(realm, ignored -> new ArrayList<>()).add(element);
    }

    private static Component techniqueSkillCapValue(
            Identifier skillId,
            TechniqueSkillCap cap,
            RegistryAccess access
    ) {
        Skill skill = CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skillId,
                access
        );
        if (skill instanceof ActiveSkill) {
            return Component.literal("Mastery cap: " + cap.masteryRank().displayName());
        }
        return Component.translatable("ascension.tooltip.progression.level", cap.progression());
    }

    private static Component skillName(Identifier skillId, RegistryAccess access) {
        Skill skill = CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skillId,
                access
        );
        return skill == null ? Component.literal(skillId.getPath()) : skill.getName();
    }

    private static List<ZenithTooltipElement> bloodlinePurityElements(
            ZenithTooltipContext context
    ) {
        Optional<SimpleBloodline> bloodline = context.subject(SimpleBloodline.class);
        if (bloodline.isEmpty() || context.registryAccess().isEmpty()) {
            return List.of();
        }

        return progressionElements(
                bloodline.orElseThrow().getHolder(),
                context.registryAccess().orElseThrow(),
                OptionalInt.empty()
        );
    }

    private static int currentTechniqueMajorRealm(SimpleTechnique technique) {
        return ClientAscensionData.getSource()
                .map(source -> {
                    Identifier path = technique.getPath();
                    if (path == null || !AscensionOriginSourceHelper.hasPath(source, path)) {
                        return -1;
                    }

                    PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(source, path);
                    return pathInstance == null ? -1 : pathInstance.getCurrentMajorRealm();
                })
                .orElse(-1);
    }

    private static List<ZenithTooltipElement> progressionElements(
            ProgressActionHolder holder,
            RegistryAccess access,
            OptionalInt currentMajorRealm
    ) {
        List<ZenithTooltipElement> elements = new ArrayList<>();

        for (Pair<ProgressActionConditionReference, List<ProgressActionReference>> listener
                : holder.listeners()) {
            ProgressActionCondition condition = listener.getFirst().resolve(access);
            if (condition == null) {
                continue;
            }

            List<ProgressActionDescription> descriptions = new ArrayList<>();
            for (ProgressActionReference actionReference : listener.getSecond()) {
                ProgressAction action = actionReference.resolve(access);
                if (action != null) {
                    descriptions.addAll(action.getDescriptions(access));
                }
            }

            if (descriptions.isEmpty()) {
                continue;
            }

            elements.add(new RowElement(
                    ZenithTooltipText.resolved(condition.getDescription(access)),
                    ZenithTooltipText.resolved(Component.empty()),
                    ZenithTooltipColor.ACCENT,
                    ZenithTooltipColor.MUTED
            ));

            boolean obscureDescriptions = shouldObscureDescriptions(condition, currentMajorRealm);
            for (ProgressActionDescription description : descriptions) {
                if (obscureDescriptions) {
                    elements.add(beyondComprehensionElement());
                    continue;
                }

                elements.add(new RowElement(
                        ZenithTooltipText.resolved(description.label()),
                        ZenithTooltipText.resolved(description.value()),
                        ZenithTooltipColor.TEXT,
                        toneColor(description.tone())
                ));
            }
        }

        return List.copyOf(elements);
    }

    private static boolean shouldObscureDescriptions(
            ProgressActionCondition condition,
            OptionalInt currentMajorRealm
    ) {
        if (currentMajorRealm.isEmpty()) {
            return false;
        }

        OptionalInt earliestMajorRealm = condition.getEarliestMajorRealm();
        return earliestMajorRealm.isPresent()
                && earliestMajorRealm.getAsInt()
                > currentMajorRealm.getAsInt() + TECHNIQUE_REVEAL_LOOKAHEAD;
    }

    private static ZenithTooltipElement beyondComprehensionElement() {
        return TextElement.animated(
                ZenithTooltipText.resolved(Component.translatable(
                        "ascension.tooltip.progression.beyond_comprehension"
                )),
                ZenithTooltipColor.MUTED,
                BEYOND_COMPREHENSION_EFFECT
        );
    }

    private static Optional<ZenithTooltipValue> herbAge(ZenithTooltipContext context) {
        return herbContext(context).map(herb -> ZenithTooltipValue.text(
                Component.translatable(
                        "ascension.herb.tooltip.age_value",
                        herb.definition().ageThreshold(herb.data().ageTier()).years()
                )
        ));
    }

    private static Optional<ZenithTooltipValue> herbQuality(ZenithTooltipContext context) {
        return herbContext(context).map(herb -> {
            HerbDefinition.Quality quality = HerbDefinition.Quality.byTier(herb.data().qualityTier());
            String key = "ascension.herb.quality." + quality.name().toLowerCase(java.util.Locale.ROOT);
            return ZenithTooltipValue.text(Component.translatable(key));
        });
    }

    private static List<ZenithTooltipElement> herbQualityBadge(ZenithTooltipContext context) {
        return herbContext(context)
                .map(herb -> {
                    HerbDefinition.Quality quality = HerbDefinition.Quality.byTier(herb.data().qualityTier());
                    String key = "ascension.herb.quality." + quality.name().toLowerCase(java.util.Locale.ROOT);
                    ZenithTooltipColor color = herbQualityColor(quality);

                    BadgeElement badge = new BadgeElement(
                            ZenithTooltipText.resolved(Component.translatable(key)),
                            color,
                            ZenithTooltipColor.BACKGROUND,
                            color
                    ).withBackgroundGradient(
                            BadgeElement.GradientDirection.HORIZONTAL,
                            ZenithTooltipColor.BACKGROUND,
                            ZenithTooltipColor.BORDER_BOTTOM
                    );

                    return List.<ZenithTooltipElement>of(new BadgeRowElement(List.of(badge)));
                })
                .orElseGet(List::of);
    }

    private static ZenithTooltipColor herbQualityColor(HerbDefinition.Quality quality) {
        return switch (quality) {
            case POOR -> ZenithTooltipColor.MUTED;
            case COMMON -> ZenithTooltipColor.TEXT;
            case GOOD -> ZenithTooltipColor.POSITIVE;
            case SUPERIOR -> ZenithTooltipColor.ACCENT;
            case PERFECT -> ZenithTooltipColor.WARNING;
        };
    }

    private static List<ZenithTooltipElement> herbRelatedTypeBadge(ZenithTooltipContext context) {
        return AscensionHerbRelatedTooltipProvider.resolve(context.stack())
                .map(info -> {
                    BadgeElement badge = new BadgeElement(
                            ZenithTooltipText.resolved(Component.translatable(info.kind().typeKey())),
                            ZenithTooltipColor.ACCENT,
                            ZenithTooltipColor.BACKGROUND,
                            ZenithTooltipColor.ACCENT
                    ).withBackgroundGradient(
                            BadgeElement.GradientDirection.HORIZONTAL,
                            ZenithTooltipColor.BACKGROUND,
                            ZenithTooltipColor.BORDER_BOTTOM
                    );

                    return List.<ZenithTooltipElement>of(new BadgeRowElement(List.of(badge)));
                })
                .orElseGet(List::of);
    }

    private static List<ZenithTooltipElement> herbRelatedTargetRow(ZenithTooltipContext context) {
        return AscensionHerbRelatedTooltipProvider.resolve(context.stack())
                .map(info -> List.<ZenithTooltipElement>of(new RowElement(
                        ZenithTooltipText.resolved(Component.translatable(info.kind().targetLabelKey())),
                        ZenithTooltipText.resolved(new net.minecraft.world.item.ItemStack(info.targetItem()).getHoverName()),
                        ZenithTooltipColor.TEXT,
                        ZenithTooltipColor.ACCENT
                )))
                .orElseGet(List::of);
    }

    private static Optional<ZenithTooltipValue> herbOrigin(ZenithTooltipContext context) {
        return herbContext(context).map(herb -> ZenithTooltipValue.text(
                Component.translatable(herb.data().wild() ? "ascension.herb.origin.wild" : "ascension.herb.origin.cultivated")
        ));
    }

    private static Optional<HerbContext> herbContext(ZenithTooltipContext context) {
        if (!(context.stack().getItem() instanceof HerbItem herbItem)) {
            return Optional.empty();
        }

        return Optional.of(new HerbContext(herbItem.definition(), herbItem.data(context.stack())));
    }

    private static Component statName(Identifier id) {
        Stat stat = ZenithRegistries.STAT_REGISTRY.getValue(id);

        return stat == null
                ? Component.literal(humanName(id.getPath()))
                : stat.getName();
    }

    private static Component pathName(
            Identifier id,
            RegistryAccess access
    ) {
        Path path = CoreRegistries.safeAccess(
                CoreRegistries.PATH_REGISTRY,
                id,
                access
        );

        return path == null
                ? Component.literal(humanName(id.getPath()))
                : path.name();
    }

    private static String formatModifier(
            ValueContainerModifier modifier
    ) {
        return switch (modifier.getOperation()) {
            case MULTIPLY_BASE, MULTIPLY_FINAL ->
                    signedNumber(modifier.getVal() * 100.0) + "%";

            case ADD_BASE, ADD_FINAL ->
                    signedNumber(modifier.getVal());
        };
    }

    private static String signedNumber(double value) {
        String number = BigDecimal.valueOf(Math.abs(value))
                .stripTrailingZeros()
                .toPlainString();

        if (value > 0) {
            return "+" + number;
        }

        if (value < 0) {
            return "-" + number;
        }

        return number;
    }

    private static ZenithTooltipValue.Tone tone(double value) {
        if (value > 0) {
            return ZenithTooltipValue.Tone.POSITIVE;
        }

        if (value < 0) {
            return ZenithTooltipValue.Tone.NEGATIVE;
        }

        return ZenithTooltipValue.Tone.NEUTRAL;
    }

    private static String humanName(String value) {
        String path = value;
        int slash = path.lastIndexOf('/');

        if (slash >= 0 && slash + 1 < path.length()) {
            path = path.substring(slash + 1);
        }

        String[] words = path.replace('-', '_').split("_");
        StringJoiner result = new StringJoiner(" ");

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            result.add(
                    Character.toUpperCase(word.charAt(0))
                            + word.substring(1)
            );
        }

        return result.toString();
    }


    private record HerbContext(
            HerbDefinition definition,
            AscensionComponents.HerbData data
    ) {}

}
