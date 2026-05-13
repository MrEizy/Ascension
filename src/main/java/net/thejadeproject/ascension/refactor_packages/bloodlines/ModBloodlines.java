package net.thejadeproject.ascension.refactor_packages.bloodlines;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.bloodlines.generic.AwakenedBeastBloodline;
import net.thejadeproject.ascension.refactor_packages.bloodlines.generic.GenericBloodline;
import net.thejadeproject.ascension.refactor_packages.bloodlines.generic.KillQualityWeights;
import net.thejadeproject.ascension.refactor_packages.bloodlines.generic.PurityBloodline;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.util.ModAttributes;

public class ModBloodlines {

    public static final DeferredRegister<IBloodline> BLOODLINES =
            DeferredRegister.create(AscensionRegistries.Bloodlines.BLOODLINE_REGISTRY, AscensionCraft.MOD_ID);

    // ── Modifier keys ────────────────────────────────────────────────────────
    private static final ResourceLocation PHOENIX_KEY = rl("phoenix_bloodline");
    private static final ResourceLocation BEAST_KEY = rl("beast_bloodline");
    private static final ResourceLocation AWAKENED_BEAST_KEY = rl("awakened_beast_bloodline");

    // ── Evolution target ResourceLocations ───────────────────────────────────
    // Defined as plain fields so they can be referenced before the DeferredHolder is constructed.
    private static final ResourceLocation BEAST_EVOLVES_INTO = rl("awakened_beast_bloodline");

    // null = final tier, no further evolution
    private static final ResourceLocation AWAKENED_BEAST_EVOLVES_INTO = null;

    // ── Kill quality tables ──────────────────────────────────────────────────

    /**
     * Beast Bloodline kill weights.
     * - Passives give nothing (predators don't grow from killing harmless things)
     * - Bosses give a big chunk
     * - Higher-realm mobs give more, scaled by combat power
     * - Default hostile kill gives 0.01 (10,000 kills to evolve on base mobs)
     * - Mobs at or above Golden Core give a flat bonus
     */
    private static final KillQualityWeights BEAST_KILL_WEIGHTS = KillQualityWeights.builder()
            .defaultGain(0.01)
            .ifPassive(0.0)
            .ifBoss(1.0)
            .ifRealmAtLeast("golden_core", 1, 0.08)
            .ifRealmAtLeast("formation_establishment", 1, 0.04)
            .ifRealmAtLeast("qi_gathering", 1, 0.02)
            .ifHealthAtLeast(100, 0.03)
            .build();

    /**
     * Awakened Beast kill weights — halved gain rate, boss weighting preserved.
     * Takes ~twice as many kills to reach max purity again.
     */
    private static final KillQualityWeights AWAKENED_BEAST_KILL_WEIGHTS = KillQualityWeights.builder()
            .defaultGain(0.005)
            .ifPassive(0.0)
            .ifBoss(0.5)
            .ifRealmAtLeast("golden_core", 1, 0.04)
            .ifRealmAtLeast("formation_establishment", 1, 0.02)
            .ifRealmAtLeast("qi_gathering", 1, 0.01)
            .ifHealthAtLeast(100, 0.015)
            .build();


    // ── Registrations ────────────────────────────────────────────────────────

    /**
     * Mortal Bloodline nothing special just regular
     */
    public static final DeferredHolder<IBloodline, GenericBloodline> HUMAN_BLOODLINE =
            BLOODLINES.register("human_bloodline", () ->
                    new GenericBloodline(Component.translatable("ascension.bloodline.human_bloodline"))
                            .setShortDescription(Component.translatable("ascension.bloodline.human_bloodline.short"))
                            .setDescription(Component.translatable("ascension.bloodline.human_bloodline.desc"))
            );


    /*
    Phoenix Bloodline This is just an example for now. What do you guys think?
     */
    public static final DeferredHolder<IBloodline, GenericBloodline> PHOENIX_BLOODLINE =
            BLOODLINES.register("phoenix_bloodline", () ->
                            new GenericBloodline(Component.translatable("ascension.bloodline.phoenix_bloodline"))
                                    .setShortDescription(Component.translatable("ascension.bloodline.phoenix_bloodline.short"))
                                    .setDescription(Component.translatable("ascension.bloodline.phoenix_bloodline.desc"))
                                    .addFlatAttribute(ModAttributes.MAX_QI, 100, PHOENIX_KEY)
                                    .addFlatAttribute(ModAttributes.QI_REGEN_RATE, 0.5, PHOENIX_KEY)
                                    .addFlatAttribute(Attributes.MOVEMENT_SPEED, 0.04, PHOENIX_KEY)
                                    .addFlatAttribute(Attributes.MAX_HEALTH, 20, PHOENIX_KEY)

                    // .addSuppressionTier(3)
                    // .addSuppressionBloodline(rl("beast_bloodline"))
                    // .addInherentSkill(ModSkills.PHOENIX_REBIRTH_PASSIVE.getId()) //Example
            );


    /**
     * Beast Bloodline — Tier 1. Raw physicality. Evolves into Awakened Beast at 100 purity.
     * Purity grows 0.01 per kill → ~10000 kills to awaken.
     */
    public static final DeferredHolder<IBloodline, PurityBloodline> BEAST_BLOODLINE =
            BLOODLINES.register("beast_bloodline", () ->
                    (PurityBloodline) new PurityBloodline(
                            Component.translatable("ascension.bloodline.beast_bloodline"),
                            BEAST_EVOLVES_INTO
                    )
                            .setKillWeights(BEAST_KILL_WEIGHTS)
                            .setShortDescription(Component.translatable("ascension.bloodline.beast_bloodline.short"))
                            .setDescription(Component.translatable("ascension.bloodline.beast_bloodline.desc"))
                            .addFlatAttribute(Attributes.MAX_HEALTH,     20,    BEAST_KEY)
                            .addFlatAttribute(Attributes.ATTACK_DAMAGE,   4,    BEAST_KEY)
                            .addFlatAttribute(Attributes.MOVEMENT_SPEED,  0.02, BEAST_KEY)
                            .addFlatAttribute(Attributes.ARMOR,           2,    BEAST_KEY)
                            .addSuppressionTier(2)
                            .addSuppressionBloodline(rl("phoenix_bloodline"))
            );

    /**
     * Awakened Beast Bloodline — Tier 2. Evolved form of the Beast bloodline.
     * Stronger modifiers.
     * Currently the final tier (evolvesInto = null).
     */
    public static final DeferredHolder<IBloodline, AwakenedBeastBloodline> AWAKENED_BEAST_BLOODLINE =
            BLOODLINES.register("awakened_beast_bloodline", () ->
                    (AwakenedBeastBloodline) new AwakenedBeastBloodline(
                            Component.translatable("ascension.bloodline.awakened_beast_bloodline"),
                            AWAKENED_BEAST_EVOLVES_INTO
                    )
                            .setKillWeights(AWAKENED_BEAST_KILL_WEIGHTS)
                            .setShortDescription(Component.translatable("ascension.bloodline.awakened_beast_bloodline.short"))
                            .setDescription(Component.translatable("ascension.bloodline.awakened_beast_bloodline.desc"))
                            .addFlatAttribute(Attributes.MAX_HEALTH,      50,    AWAKENED_BEAST_KEY)
                            .addFlatAttribute(Attributes.ATTACK_DAMAGE,   10,    AWAKENED_BEAST_KEY)
                            .addFlatAttribute(Attributes.MOVEMENT_SPEED,   0.04, AWAKENED_BEAST_KEY)
                            .addFlatAttribute(Attributes.ARMOR,            6,    AWAKENED_BEAST_KEY)
                            .addFlatAttribute(Attributes.ARMOR_TOUGHNESS,  2,    AWAKENED_BEAST_KEY)
                            .addSuppressionTier(3)
            );


    // ── Registration ─────────────────────────────────────────────────────────

    public static void register(IEventBus modEventBus) {
        BLOODLINES.register(modEventBus);
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, path);
    }
}
