package net.zic.ascension;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;


import net.zic.ascension.common.AscensionAttachments;
import net.zic.ascension.common.item.AscensionItems;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.core.entity.AscensionStats;
import net.zic.ascension.datapack.physique.AscensionPhysiqueTypes;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.HashMap;
import java.util.Map;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AscensionCraft.MOD_ID)
public class AscensionCraft {
    public static float hue;
    public static final String MOD_ID = "ascension";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<String, String> SECT_DATA = new HashMap<>();

    public static Identifier prefix(String name){
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.

    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MOD_ID);
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, MOD_ID);

    public void register(IEventBus modEventBus){
        COMPONENTS.register(modEventBus);
        RECIPES.register(modEventBus);


        NeoForge.EVENT_BUS.addListener(this::registerCommands);


    }

    public AscensionCraft(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onLoadComplete);


        AscensionPhysiqueTypes.register(modEventBus);
        AscensionAttachments.register(modEventBus);
        AscensionComponents.register(modEventBus);
        AscensionItems.register(modEventBus);
        AscensionStats.register(modEventBus);
        register(modEventBus);
        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);


        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "ascension/Ascension-Common.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.CULTIVATION_SPEC, "ascension/Ascension-Cultivation.toml");

        modEventBus.addListener(this::registerKeyBindings);

    }


    private void registerKeyBindings(RegisterKeyMappingsEvent event) {


    }


    public void onLoadComplete(FMLLoadCompleteEvent event) {

        // Subject to change
        /*
        EntityAttributeManager.changeAttributeRange(1.0, Double.MAX_VALUE, (RangedAttribute) Attributes.MAX_HEALTH.value());
        EntityAttributeManager.changeAttributeRange(0.0, Double.MAX_VALUE, (RangedAttribute) Attributes.ATTACK_DAMAGE.value());
        EntityAttributeManager.changeAttributeRange(0.0, Double.MAX_VALUE, (RangedAttribute) Attributes.ARMOR.value());
        EntityAttributeManager.changeAttributeRange(0.0, Double.MAX_VALUE, (RangedAttribute) Attributes.ARMOR_TOUGHNESS.value());
        EntityAttributeManager.changeAttributeRange(0.0, Double.MAX_VALUE, (RangedAttribute) Attributes.SAFE_FALL_DISTANCE.value());

        EntityAttributeManager.changeAttributeRange(0.0, 100.0, (RangedAttribute) Attributes.MOVEMENT_SPEED.value());
        EntityAttributeManager.changeAttributeRange(0.0, 100.0, (RangedAttribute) Attributes.JUMP_STRENGTH.value());


         */
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }


    private void registerCommands(RegisterCommandsEvent event) {

    }


    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static class ModEvents {

        @SubscribeEvent
        public static void onEntityAttributeModificationEvent(final EntityAttributeModificationEvent event) {

        }





        @SubscribeEvent
        public static void registerPayloads(RegisterPayloadHandlersEvent event) {



        }
    }
}