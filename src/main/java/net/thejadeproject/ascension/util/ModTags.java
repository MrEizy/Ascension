package net.thejadeproject.ascension.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ModTags {
    public static class DamageTypes{

        public static ResourceKey<DamageType> DAO_DAMAGE_KEY =  ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"dao"));



        public static DamageSource dao(DamageSources damageSources, Entity causing, Entity direct){
            return new DamageSource(damageSources.damageTypes.getHolderOrThrow(DAO_DAMAGE_KEY),direct,causing);

        }

    }

    public static class Items {

        public static final TagKey<Item> HERBS = createTag("herbs");
        public static final TagKey<Item> MEDICINAL = createTag("herbs/medicinal");
        public static final TagKey<Item> HUMAN = createTag("herbs/human");
        public static final TagKey<Item> BLADE = createTag("blade");
        public static final TagKey<Item> SPEAR = createTag("spear");
        public static final TagKey<Item> SPEAR_ENCHANTABLE = createTag("spear_enchantable");


        public static final TagKey<Item> FLAMES = createTag("flames");



        public static final TagKey<Item> CURIOS_RING = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("curios", "ring"));


        public static final TagKey<Item> BLACK_IRON_RAW = createCommonTag("raw_materials/black_iron");
        public static final TagKey<Item> INGOTS_BLACK_IRON = createCommonTag("ingots/black_iron");
        public static final TagKey<Item> NUGGETS_BLACK_IRON = createCommonTag("nuggets/black_iron");

        public static final TagKey<Item> FROST_SILVER_RAW = createCommonTag("raw_materials/frost_silver");
        public static final TagKey<Item> INGOTS_FROST_SILVER = createCommonTag("ingots/frost_silver");
        public static final TagKey<Item> NUGGETS_FROST_SILVER = createCommonTag("nuggets/frost_silver");




        public static final TagKey<Item> REPAIR_BLACKLIST = createTag("repair_slip_blacklist");

        public static final TagKey<Item> WOOLABLE = createTag("crafting/woolable");



        public static HashMap<String,TagKey<Item>> daoItemTags = new HashMap<>();

        //attributes/elements

        public static void  createDaoTag(String name){
            daoItemTags.put("ascension:"+name,createTag(name));
        }


        private static TagKey<Item> createCommonTag(String path) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
        }

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
        }
    }

    public static class Blocks {
        public static final TagKey<Block> DESTRUCTIBLE_BLOCKS = createTag("blocks_destruction");
        public static final TagKey<Block> LINKABLE_CONTAINERS = createTag("linkable_containers");


        public static final TagKey<Block> INCORRECT_FOR_JADE_TOOL = createTag("incorrect_for_jade_tool");

        public static final TagKey<Block> INCORRECT_FOR_SPIRITUAL_STONE_TOOL = createTag("incorrect_for_spiritual_stone_tool");
        public static final TagKey<Block> NEEDS_SPIRITUAL_STONE_TOOL = createTag("need_for_spiritual_stone_tool");

        public static final TagKey<Block> STORAGE_BLOCKS_BLACK_IRON = createCommonTag("storage_blocks/black_iron");
        public static final TagKey<Block> STORAGE_BLOCKS_FROST_SILVER = createCommonTag("storage_blocks/frost_silver");


        public static final TagKey<Block> HERB = createCommonTag("herb");



        private static TagKey<Block> createCommonTag(String path) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
        }

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
        }
    }

    // NEW: Entity Type Tags
    public static class EntityTypes {
        // Spirit Sealing Ring blacklist - mobs that cannot be captured
        public static final TagKey<EntityType<?>> SEALING_BLACKLIST = createTag("sealing_blacklist");

        // Bosses that cannot be captured
        public static final TagKey<EntityType<?>> BOSS = createTag("boss");

        // Spirits that can be captured (optional whitelist approach)
        public static final TagKey<EntityType<?>> CAPTURABLE = createTag("capturable");

        // Utility method to check if an entity matches the tag
        public static boolean is(Entity entity, TagKey<EntityType<?>> tag) {
            return entity.getType().is(tag);
        }

        private static TagKey<EntityType<?>> createTag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, name));
        }

        private static TagKey<EntityType<?>> createCommonTag(String path) {
            return TagKey.create(Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath("c", path));
        }
    }
}