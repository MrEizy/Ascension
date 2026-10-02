package net.zic.ascension.datagen.villager;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;

import java.util.List;
import java.util.Optional;

public class AscVillagerTrades {

    public static final ResourceKey<VillagerTrade> LIBRARIAN_3_EAGLE_CLAW_PALM_TECHNIQUE =
            createKey("librarian/3/eagle_claw_palm_technique");
    public static final ResourceKey<VillagerTrade> LIBRARIAN_3_GRASPING_SAND_TECHNIQUE =
            createKey("librarian/3/grasping_sand_technique");
    public static final ResourceKey<VillagerTrade> LIBRARIAN_3_IMPERIAL_SEVEN_STANCES =
            createKey("librarian/3/imperial_seven_stances");
    public static final ResourceKey<VillagerTrade> LIBRARIAN_3_IRON_FIST_TEMPERING_MANUAL =
            createKey("librarian/3/iron_fist_tempering_manual");
    public static final ResourceKey<VillagerTrade> LIBRARIAN_3_LIGHTNESS_TECHNIQUE =
            createKey("librarian/3/lightness_technique");
    public static final ResourceKey<VillagerTrade> LIBRARIAN_3_MARTIAL_TRANSCENDENCE_VOL_1 =
            createKey("librarian/3/martial_transcendence_vol_1");
    public static final ResourceKey<VillagerTrade> LIBRARIAN_3_NINE_PATHS_OF_TRUTH =
            createKey("librarian/3/nine_paths_of_truth");
    public static final ResourceKey<VillagerTrade> LIBRARIAN_3_SHADOWLESS_ART =
            createKey("librarian/3/shadowless_art");
    public static final ResourceKey<VillagerTrade> LIBRARIAN_3_SUSTAINED_SPIRIT_ART =
            createKey("librarian/3/sustained_spirit_art");
    public static final ResourceKey<VillagerTrade> LIBRARIAN_3_SWORD_DRAW_MANUAL =
            createKey("librarian/3/sword_draw_manual");

    private static final Identifier ID_EAGLE_CLAW_PALM =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "1_ordinary/eagle_claw_palm_technique");
    private static final Identifier ID_GRASPING_SAND =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "1_ordinary/grasping_sand_technique");
    private static final Identifier ID_IMPERIAL_SEVEN_STANCES =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "1_ordinary/imperial_seven_stances");
    private static final Identifier ID_IRON_FIST_TEMPERING =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "1_ordinary/iron_fist_tempering_manual");
    private static final Identifier ID_LIGHTNESS =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "1_ordinary/lightness_technique");
    private static final Identifier ID_MARTIAL_TRANSCENDENCE_VOL_1 =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "1_ordinary/martial_transcendence_vol_1");
    private static final Identifier ID_NINE_PATHS_OF_TRUTH =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "1_ordinary/nine_paths_of_truth");
    private static final Identifier ID_SHADOWLESS_ART =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "1_ordinary/shadowless_art");
    private static final Identifier ID_SUSTAINED_SPIRIT_ART =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "1_ordinary/sustained_spirit_art");
    private static final Identifier ID_SWORD_DRAW =
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "1_ordinary/sword_draw_manual");

    public static void bootStrap(BootstrapContext<VillagerTrade> context) {

        context.register(LIBRARIAN_3_EAGLE_CLAW_PALM_TECHNIQUE, new VillagerTrade(
                new TradeCost(ModItems.SPIRITUAL_STONE, 13),
                new ItemStackTemplate(ModItems.TECHNIQUE_MANUAL, 1),
                12, 6, 0.01f,
                Optional.empty(),
                List.of(SetComponentsFunction.setComponent(
                        AscensionComponents.REGISTRY_ID_HOLDER.get(), ID_EAGLE_CLAW_PALM).build())));

        context.register(LIBRARIAN_3_GRASPING_SAND_TECHNIQUE, new VillagerTrade(
                new TradeCost(ModItems.SPIRITUAL_STONE, 13),
                new ItemStackTemplate(ModItems.TECHNIQUE_MANUAL, 1),
                12, 6, 0.01f,
                Optional.empty(),
                List.of(SetComponentsFunction.setComponent(
                        AscensionComponents.REGISTRY_ID_HOLDER.get(), ID_GRASPING_SAND).build())));

        context.register(LIBRARIAN_3_IMPERIAL_SEVEN_STANCES, new VillagerTrade(
                new TradeCost(ModItems.SPIRITUAL_STONE, 16),
                new ItemStackTemplate(ModItems.TECHNIQUE_MANUAL, 1),
                12, 6, 0.01f,
                Optional.empty(),
                List.of(SetComponentsFunction.setComponent(
                        AscensionComponents.REGISTRY_ID_HOLDER.get(), ID_IMPERIAL_SEVEN_STANCES).build())));

        context.register(LIBRARIAN_3_IRON_FIST_TEMPERING_MANUAL, new VillagerTrade(
                new TradeCost(ModItems.SPIRITUAL_STONE, 13),
                new ItemStackTemplate(ModItems.TECHNIQUE_MANUAL, 1),
                12, 6, 0.01f,
                Optional.empty(),
                List.of(SetComponentsFunction.setComponent(
                        AscensionComponents.REGISTRY_ID_HOLDER.get(), ID_IRON_FIST_TEMPERING).build())));

        context.register(LIBRARIAN_3_LIGHTNESS_TECHNIQUE, new VillagerTrade(
                new TradeCost(ModItems.SPIRITUAL_STONE, 14),
                new ItemStackTemplate(ModItems.TECHNIQUE_MANUAL, 1),
                12, 6, 0.01f,
                Optional.empty(),
                List.of(SetComponentsFunction.setComponent(
                        AscensionComponents.REGISTRY_ID_HOLDER.get(), ID_LIGHTNESS).build())));

        context.register(LIBRARIAN_3_MARTIAL_TRANSCENDENCE_VOL_1, new VillagerTrade(
                new TradeCost(ModItems.SPIRITUAL_STONE, 18),
                new ItemStackTemplate(ModItems.TECHNIQUE_MANUAL, 1),
                12, 6, 0.01f,
                Optional.empty(),
                List.of(SetComponentsFunction.setComponent(
                        AscensionComponents.REGISTRY_ID_HOLDER.get(), ID_MARTIAL_TRANSCENDENCE_VOL_1).build())));

        context.register(LIBRARIAN_3_NINE_PATHS_OF_TRUTH, new VillagerTrade(
                new TradeCost(ModItems.SPIRITUAL_STONE, 20),
                new ItemStackTemplate(ModItems.TECHNIQUE_MANUAL, 1),
                12, 6, 0.01f,
                Optional.empty(),
                List.of(SetComponentsFunction.setComponent(
                        AscensionComponents.REGISTRY_ID_HOLDER.get(), ID_NINE_PATHS_OF_TRUTH).build())));

        context.register(LIBRARIAN_3_SHADOWLESS_ART, new VillagerTrade(
                new TradeCost(ModItems.SPIRITUAL_STONE, 15),
                new ItemStackTemplate(ModItems.TECHNIQUE_MANUAL, 1),
                12, 6, 0.01f,
                Optional.empty(),
                List.of(SetComponentsFunction.setComponent(
                        AscensionComponents.REGISTRY_ID_HOLDER.get(), ID_SHADOWLESS_ART).build())));

        context.register(LIBRARIAN_3_SUSTAINED_SPIRIT_ART, new VillagerTrade(
                new TradeCost(ModItems.SPIRITUAL_STONE, 14),
                new ItemStackTemplate(ModItems.TECHNIQUE_MANUAL, 1),
                12, 6, 0.01f,
                Optional.empty(),
                List.of(SetComponentsFunction.setComponent(
                        AscensionComponents.REGISTRY_ID_HOLDER.get(), ID_SUSTAINED_SPIRIT_ART).build())));

        context.register(LIBRARIAN_3_SWORD_DRAW_MANUAL, new VillagerTrade(
                new TradeCost(ModItems.SPIRITUAL_STONE, 13),
                new ItemStackTemplate(ModItems.TECHNIQUE_MANUAL, 1),
                12, 6, 0.01f,
                Optional.empty(),
                List.of(SetComponentsFunction.setComponent(
                        AscensionComponents.REGISTRY_ID_HOLDER.get(), ID_SWORD_DRAW).build())));
    }

    private static ResourceKey<VillagerTrade> createKey(String name) {
        return ResourceKey.create(
                Registries.VILLAGER_TRADE,
                Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
    }
}