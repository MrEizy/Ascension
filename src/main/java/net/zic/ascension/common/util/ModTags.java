package net.zic.ascension.common.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.zic.ascension.AscensionCraft;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> DESTRUCTIBLE_BLOCKS = createTag("blocks_destruction");
        public static final TagKey<Block> LINKABLE_CONTAINERS = createTag("linkable_containers");




        private static TagKey<Block> createCommonTag(String path) {
            return BlockTags.create(Identifier.fromNamespaceAndPath("c", path));
        }

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> ARTIFACTS = createItemTag("artifacts");
        public static final TagKey<Item> PILLS = createItemTag("pills");
        public static final TagKey<Item> ARMOR = createItemTag("armor");
        public static final TagKey<Item> TOOLS = createItemTag("tools");
        public static final TagKey<Item> TALISMANS = createItemTag("talismans");


        public static final TagKey<Item> ORDINARY_ITEMS = createItemTag("ordinary_items");
        public static final TagKey<Item> PROFOUND_ITEMS = createItemTag("profound_items");
        public static final TagKey<Item> HEAVEN_ITEMS = createItemTag("heaven_items");
        public static final TagKey<Item> SAINT_ITEMS = createItemTag("saint_items");
        public static final TagKey<Item> GOD_ITEMS = createItemTag("god_items");
        public static final TagKey<Item> HEAVENS_PATH_ITEMS = createItemTag("heavens_path_items");

        private static TagKey<Item> createItemTag(String name) {
            return ItemTags.create(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
        }
    }

    public static class EntityTypes {
        public static final TagKey<EntityType<?>> FROZEN_IMMUNE = createTag("frozen_immune");
        public static final TagKey<EntityType<?>> FROZEN_RESISTANT = createTag("frozen_resistant");
        public static final TagKey<EntityType<?>> FROZEN_BOSS_PROFILE = createTag("frozen_boss_profile");


        private static TagKey<EntityType<?>> createTag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
        }
    }
}
