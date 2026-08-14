package net.zic.ascension.client.tooltip;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressActionCondition;
import net.zic.ascension.api.ascension.core.progression.ProgressActionConditionReference;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.progression.ProgressActionReference;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.datapack.path.PathBonusBase;
import net.zic.ascension.api.ascension.datapack.path.PathBonusModifier;
import net.zic.ascension.common.herbs.HerbDefinition;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.common.item.herbs.HerbItem;
import net.zic.ascension.client.tooltip.providers.AscensionHerbRelatedTooltipProvider;
import net.zic.ascension.impl.core.technique.realm_change.condition.RealmChangeConditions;
import net.zic.ascension.impl.core.bloodline.SimpleBloodline;
import net.zic.ascension.impl.core.bloodline.purity.condition.OnPurityInRangeCondition;
import net.zic.ascension.impl.core.physique.SimplePhysique;
import net.zic.ascension.impl.core.progression.GiveBaseStatsAction;
import net.zic.ascension.impl.core.progression.GivePathBonusesAction;
import net.zic.ascension.impl.core.technique.SimpleTechnique;

import net.zic.ascension.util.PathInteractionUtil;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.api.element.BadgeElement;
import net.zic.zenithlib.tooltip.api.element.BadgeRowElement;
import net.zic.zenithlib.tooltip.api.element.RowElement;
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
import java.util.StringJoiner;
import java.util.stream.Collectors;

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
        ZenithTooltipSources.registerValue(TECHNIQUE_PROGRESSION_GAINS, AscensionTooltipValueSources::techniqueProgressionGains);

        ZenithTooltipSources.registerValue(BLOODLINE_PURITY, AscensionTooltipValueSources::bloodlinePurity);
        ZenithTooltipSources.registerValue(BLOODLINE_PURITY_GAINS, AscensionTooltipValueSources::bloodlinePurityGains);

        ZenithTooltipSources.registerValue(HERB_AGE, AscensionTooltipValueSources::herbAge);
        ZenithTooltipSources.registerValue(HERB_QUALITY, AscensionTooltipValueSources::herbQuality);
        ZenithTooltipSources.registerValue(HERB_ORIGIN, AscensionTooltipValueSources::herbOrigin);

        ZenithTooltipSources.registerElement(PHYSIQUE_PATHS, context -> badges(PHYSIQUE_PATHS, context, ZenithTooltipColor.ACCENT));
        ZenithTooltipSources.registerElement(PHYSIQUE_STATS, context -> rows(PHYSIQUE_STATS, context));
        ZenithTooltipSources.registerElement(PHYSIQUE_AFFINITIES, context -> rows(PHYSIQUE_AFFINITIES, context));
        ZenithTooltipSources.registerElement(TECHNIQUE_PROGRESSION_GAINS, context -> rows(TECHNIQUE_PROGRESSION_GAINS, context));
        ZenithTooltipSources.registerElement(BLOODLINE_PURITY_GAINS, context -> rows(BLOODLINE_PURITY_GAINS, context));
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
        return context.subject(SimplePhysique.class).map(physique -> {
            RegistryAccess access = context.registryAccess().orElse(null);

            return ZenithTooltipValue.rows(
                    combineRows(
                            baseRows(
                                    physique.baseStats(),
                                    false,
                                    access
                            ),
                            modifierRows(
                                    physique.statModifiers(),
                                    false,
                                    access
                            )
                    )
            );
        });
    }



    private static Optional<ZenithTooltipValue> physiqueAffinities(
            ZenithTooltipContext context
    ) {
        return context.subject(SimplePhysique.class).map(physique -> {
            RegistryAccess access = context.registryAccess().orElse(null);

            return ZenithTooltipValue.rows(
                    combineRows(
                            baseAffinityRows(
                                    physique.basePathBonuses().stream().filter(val->val.category().equals(PathInteractionUtil.AFFINITY_CATEGORY)).collect(Collectors.toCollection(ArrayList::new)),
                                    access
                            ),
                            affinityModifierRows(
                                    physique.pathBonusModifiers().stream().filter(val->val.category().equals(PathInteractionUtil.AFFINITY_CATEGORY)).collect(Collectors.toCollection(ArrayList::new)),
                                    access
                            )
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

    private static Optional<ZenithTooltipValue> techniqueProgressionGains(
            ZenithTooltipContext context
    ) {
        Optional<SimpleTechnique> technique =
                context.subject(SimpleTechnique.class);

        if (technique.isEmpty() || context.registryAccess().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                ZenithTooltipValue.rows(
                        progressionRows(
                                technique.orElseThrow().getHolder(),
                                context.registryAccess().orElseThrow(),
                                AscensionTooltipValueSources::techniqueCadence
                        )
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
                        Component.literal(purity + "%")
                )
        );
    }

    private static Optional<ZenithTooltipValue> bloodlinePurityGains(
            ZenithTooltipContext context
    ) {
        Optional<SimpleBloodline> bloodline =
                context.subject(SimpleBloodline.class);

        if (bloodline.isEmpty() || context.registryAccess().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                ZenithTooltipValue.rows(
                        progressionRows(
                                bloodline.orElseThrow().getHolder(),
                                context.registryAccess().orElseThrow(),
                                AscensionTooltipValueSources::purityCadence
                        )
                )
        );
    }

    private static List<ZenithTooltipValue.Row> combineRows(
            Collection<ZenithTooltipValue.Row> first,
            Collection<ZenithTooltipValue.Row> second
    ) {
        List<ZenithTooltipValue.Row> rows =
                new ArrayList<>(first.size() + second.size());

        rows.addAll(first);
        rows.addAll(second);

        return List.copyOf(rows);
    }


    private static List<ZenithTooltipValue.Row> baseAffinityRows(
            Collection<PathBonusBase> baseAffinities,
            RegistryAccess access
    ){
        return baseAffinities.stream()
                .sorted(
                        Comparator.comparing(
                                baseAffinity -> baseAffinity.path().toString()+baseAffinity.category()
                        )
                )
                .map(modifier -> ZenithTooltipValue.row(
                        pathName(modifier.path(), access),
                        Component.literal(
                                signedNumber(modifier.value())
                        ),
                        tone(modifier.value())
                ))
                .toList();
    }

    private static List<ZenithTooltipValue.Row> baseRows(
            Collection<ValueContainer.BaseModifier> modifiers,
            boolean affinity,
            RegistryAccess access
    ) {
        return modifiers.stream()
                .sorted(
                        Comparator.comparing(
                                modifier -> modifier.container().toString()
                        )
                )
                .map(modifier -> ZenithTooltipValue.row(
                        displayName(
                                modifier.container(),
                                affinity,
                                access
                        ),
                        Component.literal(
                                signedNumber(modifier.val())
                        ),
                        tone(modifier.val())
                ))
                .toList();
    }
    private static List<ZenithTooltipValue.Row> affinityModifierRows(
            List<PathBonusModifier> modifiers,
            RegistryAccess access
    ){
        List<ZenithTooltipValue.Row> rows = new ArrayList<>();
        modifiers.stream().sorted(
                Comparator.comparing(
                        val->val.path().toString()+val.modifier().getIdentifier().toString()
                )
        ).forEach(modifier ->rows.add(
                ZenithTooltipValue.row(
                        pathName(modifier.path(),access),
                        Component.literal(
                                formatModifier(modifier.modifier())
                        ),
                        tone(modifier.modifier().getVal())
                )
            )
        );
        return rows;
    }
    
    private static List<ZenithTooltipValue.Row> modifierRows(
            Map<Identifier, List<ValueContainerModifier>> modifiers,
            boolean affinity,
            RegistryAccess access
    ) {
        List<ZenithTooltipValue.Row> rows = new ArrayList<>();

        modifiers.entrySet()
                .stream()
                .sorted(
                        Comparator.comparing(
                                entry -> entry.getKey().toString()
                        )
                )
                .forEach(entry -> entry.getValue()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        modifier ->
                                                modifier.getIdentifier().toString()
                                )
                        )
                        .forEach(modifier -> rows.add(
                                ZenithTooltipValue.row(
                                        displayName(
                                                entry.getKey(),
                                                affinity,
                                                access
                                        ),
                                        Component.literal(
                                                formatModifier(modifier)
                                        ),
                                        tone(modifier.getVal())
                                )
                        )));

        return List.copyOf(rows);
    }

    private static List<ZenithTooltipValue.Row> progressionRows(
            ProgressActionHolder holder,
            RegistryAccess access,
            CadenceResolver cadenceResolver
    ) {
        List<ZenithTooltipValue.Row> rows = new ArrayList<>();

        for (Pair<
                ProgressActionConditionReference,
                List<ProgressActionReference>
                > listener : holder.listeners()) {

            ProgressActionCondition condition =
                    listener.getFirst().resolve(access);

            if (condition == null) {
                continue;
            }

            String cadence = cadenceResolver.resolve(condition);

            for (ProgressActionReference actionReference
                    : listener.getSecond()) {

                ProgressAction action = actionReference.resolve(access);

                if (action instanceof GiveBaseStatsAction statsAction) {
                    for (ValueContainer.BaseModifier modifier
                            : statsAction.baseStats()) {

                        rows.add(
                                ZenithTooltipValue.row(
                                        statName(modifier.container()),
                                        Component.literal(
                                                signedNumber(modifier.val())
                                                        + " · "
                                                        + cadence
                                        ),
                                        tone(modifier.val())
                                )
                        );
                    }
                    continue;
                }

                if (action instanceof GivePathBonusesAction pathBonusAction) {
                    for (PathBonusBase bonus : pathBonusAction.bonuses()) {
                        Component label = Component.empty()
                                .append(pathName(bonus.path(), access))
                                .append(" Affinity");

                        rows.add(
                                ZenithTooltipValue.row(
                                        label,
                                        Component.literal(
                                                signedNumber(bonus.value())
                                                        + " · "
                                                        + cadence
                                        ),
                                        tone(bonus.value())
                                )
                        );
                    }
                }
            }
        }

        return List.copyOf(rows);
    }

    private static String techniqueCadence(
            ProgressActionCondition condition
    ) {
        if (condition instanceof RealmChangeConditions.EveryMajorRealm) {
            return "Each Major Realm";
        }

        if (condition instanceof RealmChangeConditions.EveryMinorRealm) {
            return "Each Minor Realm";
        }

        if (condition instanceof RealmChangeConditions.EveryRealm) {
            return "On Learn + Each Realm";
        }

        if (condition instanceof RealmChangeConditions.MajorRealmsIn selected) {
            if (selected.majorRealms().size() == 1
                    && selected.majorRealms().contains(0)) {
                return "On Learn";
            }

            return "Major Realms " + joinInts(selected.majorRealms());
        }

        if (condition instanceof RealmChangeConditions.MinorRealmsIn selected) {
            return "Minor Realms " + joinInts(selected.minorRealms());
        }

        if (condition instanceof RealmChangeConditions.RealmsIn) {
            return "Selected Realms";
        }

        return "Conditional";
    }

    private static String purityCadence(
            ProgressActionCondition condition
    ) {
        if (!(condition instanceof OnPurityInRangeCondition purity)) {
            return "Conditional";
        }

        if (purity.start() == 1 && purity.end() == 1) {
            return "On Acquisition";
        }

        if (purity.start() == purity.end()) {
            return "At " + purity.start() + "% Purity";
        }

        if (purity.start() == 1 && purity.end() == 100) {
            return "On Acquisition + Each 1% Purity";
        }

        return "Each 1% Purity ("
                + purity.start()
                + "–"
                + purity.end()
                + "%)";
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

    private static Component displayName(
            Identifier id,
            boolean affinity,
            RegistryAccess access
    ) {
        return affinity && access != null
                ? pathName(id, access)
                : statName(id);
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

    private static String joinInts(Collection<Integer> values) {
        return values.stream()
                .sorted()
                .map(String::valueOf)
                .reduce((left, right) -> left + ", " + right)
                .orElse("None");
    }

    private record HerbContext(
            HerbDefinition definition,
            AscensionComponents.HerbData data
    ) {}

    @FunctionalInterface
    private interface CadenceResolver {
        String resolve(ProgressActionCondition condition);
    }
}
