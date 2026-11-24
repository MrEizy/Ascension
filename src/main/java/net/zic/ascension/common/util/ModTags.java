package net.zic.ascension.common.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.technique.Technique;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> DESTRUCTIBLE_BLOCKS = createTag("blocks_destruction");
        public static final TagKey<Block> CHAKRAM_DESTRUCTION = createTag("chakram_destruction");
        public static final TagKey<Block> LINKABLE_CONTAINERS = createTag("linkable_containers");
        public static final TagKey<Block> PROJECTION_UNBREAKABLE = createTag("projection_unbreakable");
        public static final TagKey<Block> HERB_SOILS = createTag("herb_soils");
        // What the Earth element's External Qi Coating mines faster (its external_on_mine speed).
        public static final TagKey<Block> EARTH_QI_COATING_MINEABLE = createTag("earth_qi_coating_mineable");

        public static final  TagKey<Block> NEEDS_BLACK_IRON_TOOL = createTag("needs_black_iron_tool");
        public static final  TagKey<Block> INCORRECT_FOR_BLACK_IRON_TOOL = createTag("incorrect_for_black_iron_tool");

        public static final  TagKey<Block> NEEDS_FROST_SILVER_TOOL = createTag("needs_frost_silver_tool");
        public static final  TagKey<Block> INCORRECT_FOR_FROST_SILVER_TOOL = createTag("incorrect_for_frost_silver_tool");

        public static final  TagKey<Block> NEEDS_SPIRITUAL_STONE_TOOL = createTag("needs_spiritual_stone_tool");
        public static final  TagKey<Block> INCORRECT_FOR_SPIRITUAL_STONE_TOOL = createTag("incorrect_for_spiritual_stone_tool");



        //Weapon Stuff
        public static final TagKey<Block> NO_RICOCHET = createTag("no_ricochet");
        public static final TagKey<Block> BOUNCY = createTag("bouncy");


        private static TagKey<Block> createCommonTag(String path) {
            return BlockTags.create(Identifier.fromNamespaceAndPath("c", path));
        }

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
        }
    }

    public static class DamageTypes {
        public static final ResourceKey<DamageType> CHAKRAM =
                ResourceKey.create(Registries.DAMAGE_TYPE,
                        Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "chakram"));

        private DamageTypes() {}
    }

    public static class Items {

        public static final TagKey<Item> ARTIFACTS = createItemTag("artifacts");
        public static final TagKey<Item> HERBS = createItemTag("herbs");
        public static final TagKey<Item> PILLS = createItemTag("pills");
        public static final TagKey<Item> ARMOR = createItemTag("armor");
        public static final TagKey<Item> TOOLS = createItemTag("tools");
        public static final TagKey<Item> TALISMANS = createItemTag("talismans");
        public static final TagKey<Item> MATERIALS = createItemTag("materials");
        public static final TagKey<Item> SPIRIT_STONE = createItemTag("spirit_stone");


        public static final TagKey<Item> BLACK_IRON_REPAIRABLE = createItemTag("black_iron_repairable");
        public static final TagKey<Item> FROST_SILVER_REPAIRABLE = createItemTag("frost_silver_repairable");
        public static final TagKey<Item> SPIRITUAL_STONE_REPAIRABLE = createItemTag("spiritual_stone_repairable");
        public static final TagKey<Item> JADE_REPAIRABLE = createItemTag("jade_repairable");

        public static final TagKey<Item> WEAPON_BLADES = createItemTag("weapon/blades");
        public static final TagKey<Item> WEAPON_BOWS = createItemTag("weapon/bows");
        public static final TagKey<Item> WEAPON_FISTS = createItemTag("weapon/fists");
        public static final TagKey<Item> WEAPON_MACES = createItemTag("weapon/maces");
        public static final TagKey<Item> WEAPON_SPEARS = createItemTag("weapon/spears");
        public static final TagKey<Item> WEAPON_SCYTHES = createItemTag("weapon/scythes");

        public static final TagKey<Item> CHAKRAMS = createItemTag("weapon/chakrams");
        public static final TagKey<Item> CONTROLLABLE_SWORDS = createItemTag("weapon/controllable_swords");

        public static final TagKey<Item> C_TOOLS_BLADE = createCommonItemTag("tools/blade");
        public static final TagKey<Item> C_WEAPONS_BLADE = createCommonItemTag("weapons/blade");
        public static final TagKey<Item> C_TOOLS_GAUNTLET = createCommonItemTag("tools/gauntlet");
        public static final TagKey<Item> C_WEAPONS_GAUNTLET = createCommonItemTag("weapons/gauntlet");
        public static final TagKey<Item> C_TOOLS_SPEAR = createCommonItemTag("tools/spear");
        public static final TagKey<Item> C_WEAPONS_SPEAR = createCommonItemTag("weapons/spear");
        public static final TagKey<Item> C_TOOLS_SWORD = createCommonItemTag("tools/sword");
        public static final TagKey<Item> C_WEAPONS_SWORD = createCommonItemTag("weapons/sword");
        public static final TagKey<Item> C_TOOLS_SWORDS = createCommonItemTag("tools/swords");
        public static final TagKey<Item> C_WEAPONS_SWORDS = createCommonItemTag("weapons/swords");
        public static final TagKey<Item> C_TOOLS_SCYTHE = createCommonItemTag("tools/scythe");
        public static final TagKey<Item> C_WEAPONS_SCYTHE = createCommonItemTag("weapons/scythe");

        public static final TagKey<Item> ORDINARY_ITEMS = createItemTag("ordinary_items");
        public static final TagKey<Item> PROFOUND_ITEMS = createItemTag("profound_items");
        public static final TagKey<Item> HEAVEN_ITEMS = createItemTag("heaven_items");
        public static final TagKey<Item> SAINT_ITEMS = createItemTag("saint_items");
        public static final TagKey<Item> GOD_ITEMS = createItemTag("god_items");
        public static final TagKey<Item> HEAVENS_PATH_ITEMS = createItemTag("heavens_path_items");

        private static TagKey<Item> createCommonItemTag(String path) {
            return ItemTags.create(Identifier.fromNamespaceAndPath("c", path));
        }

        private static TagKey<Item> createItemTag(String name) {
            return ItemTags.create(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
        }
    }

    public static class Enchantments {
        public static final TagKey<Enchantment> CHAKRAM = createTag("chakram");

        public static final TagKey<Enchantment> CHAKRAM_EXCLUSIVE = createTag("exclusive_set/chakram");

        private static TagKey<Enchantment> createTag(String name) {
            return TagKey.create(Registries.ENCHANTMENT,
                    Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
        }

        private Enchantments() {}
    }

    public static class Biomes {
        public static final TagKey<Biome> MOUNTAIN_BIOMES = createTag("mountain_biomes");
        public static final TagKey<Biome> FOOTHILL_BIOMES = createTag("foothill_biomes");
        public static final TagKey<Biome> MOUNTAIN_SLOPE_BIOMES = createTag("mountain_slope_biomes");
        public static final TagKey<Biome> MOUNTAIN_PEAK_BIOMES = createTag("mountain_peak_biomes");
        public static final TagKey<Biome> FOREST_BIOMES = createTag("forest_biomes");
        public static final TagKey<Biome> OPEN_COUNTRY_BIOMES = createTag("open_country_biomes");

        private static TagKey<Biome> createTag(String name) {
            return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
        }
    }

    public static class Structures {
        public static final TagKey<Structure> PAGODAS = createTag("pagodas");

        private static TagKey<Structure> createTag(String name) {
            return TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
        }
    }

    public static class EntityTypes {
        public static final TagKey<EntityType<?>> FROZEN_IMMUNE = createTag("frozen_immune");
        public static final TagKey<EntityType<?>> FROZEN_RESISTANT = createTag("frozen_resistant");
        public static final TagKey<EntityType<?>> FROZEN_BOSS_PROFILE = createTag("frozen_boss_profile");
        public static final TagKey<EntityType<?>> RANGED_PROJECTILE = createTag("ranged_projectiles");
        public static final TagKey<EntityType<?>> STAGGER_IMMUNE = createTag("stagger_immune");
        public static final TagKey<EntityType<?>> STAGGER_RESISTANT = createTag("stagger_resistant");
        public static final TagKey<EntityType<?>> STAGGER_BOSS_PROFILE = createTag("stagger_boss_profile");
        public static final TagKey<EntityType<?>> ORTHODOX = createTag("orthodox");
        public static final TagKey<EntityType<?>> UNORTHODOX = createTag("unorthodox");
        public static final TagKey<EntityType<?>> DEMONIC = createTag("demonic");
        public static final TagKey<EntityType<?>> MOB_CULTIVATION_PASSIVE = createTag("mob_cultivation/passive");
        public static final TagKey<EntityType<?>> MOB_CULTIVATION_HOSTILE = createTag("mob_cultivation/hostile");
        public static final TagKey<EntityType<?>> MOB_CULTIVATION_BOSSES = createTag("mob_cultivation/bosses");
        public static final TagKey<EntityType<?>> MOB_CULTIVATION_EXCLUDED = createTag("mob_cultivation/excluded");


        private static TagKey<EntityType<?>> createTag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
        }
    }




    public static class Skills {
        // Skills that only back another skill, such as the passive a Qi Coating hotbar skill toggles; left out of skill lists.
        public static final TagKey<Skill<?>> HIDDEN = createTag("hidden");

        private static TagKey<Skill<?>> createTag(String name) {
            return TagKey.create(CoreRegistries.SKILL_REGISTRY.key(), Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
        }

        /**
         * Whether the skill should appear in skill lists and tooltips.
         */
        public static boolean isListed(Identifier skill, RegistryAccess access) {
            return CoreRegistries.SKILL_REGISTRY.get(access).get(skill).map(holder -> !holder.is(HIDDEN)).orElse(true);
        }
    }

    // Dev/test entries tagged hidden are left out of the transfer creative tabs (and so out of JEI).
    public static class Physiques {
        public static final TagKey<Physique<?>> HIDDEN =
                TagKey.create(CoreRegistries.PHYSIQUE_REGISTRY.key(), Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "hidden"));
    }

    public static class Bloodlines {
        public static final TagKey<Bloodline<?>> HIDDEN =
                TagKey.create(CoreRegistries.BLOODLINE_REGISTRY.key(), Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "hidden"));
    }

    public static class Techniques {
        public static final TagKey<Technique<?>> HIDDEN =
                TagKey.create(CoreRegistries.TECHNIQUE_REGISTRY.key(), Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "hidden"));
    }
}
