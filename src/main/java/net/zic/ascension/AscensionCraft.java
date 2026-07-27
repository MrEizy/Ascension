package net.zic.ascension;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.path.interactions.PathInteractionHolder;
import net.zic.ascension.common.ModCreativeModeTabs;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.command.AscensionCommand;
import net.zic.ascension.common.command.commands.StatDisplayCommand;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.common.util.AscensionAttributes;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationTypes;
import net.zic.ascension.network.*;
import net.zic.ascension.impl.core.entity.AscensionStats;

import net.zic.ascension.impl.datapack.bloodline.AscensionBloodlineTypes;
import net.zic.ascension.impl.datapack.path.AscensionPathTypes;
import net.zic.ascension.impl.datapack.physique.AscensionPhysiqueTypes;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionConditionTypes;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionTypes;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import net.zic.ascension.impl.datapack.technique.AscensionTechniqueTypes;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.HashMap;
import java.util.Map;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
@Mod(AscensionCraft.MOD_ID)
public class AscensionCraft {
    public static float hue;
    public static final String MOD_ID = "ascension";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<String, String> SECT_DATA = new HashMap<>();

 private static final PathInteractionHolder pathInteractionHolder = new PathInteractionHolder();
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

        CoreHolderProviders.register(modEventBus);
        CoreAttachments.register(modEventBus);

        AscensionPhysiqueTypes.register(modEventBus);
        AscensionAttachments.register(modEventBus);
        AscensionComponents.register(modEventBus);
        ModItems.register(modEventBus);

        ModCreativeModeTabs.register(modEventBus);


        AscensionStats.register(modEventBus);

        AscensionBloodlineTypes.register(modEventBus);
        AscensionProgressActionTypes.register(modEventBus);
        AscensionProgressActionConditionTypes.register(modEventBus);
        AscensionSkillTypes.register(modEventBus);
        AscensionTechniqueTypes.register(modEventBus);
        AscensionPathTypes.register(modEventBus);
        AscensionTribulationTypes.register(modEventBus);


        AscensionAttributes.register(modEventBus);

    }

    public AscensionCraft(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onLoadComplete);



        register(modEventBus);
        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "ascension/Ascension-Common.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.CULTIVATION_SPEC, "ascension/Ascension-Cultivation.toml");

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


    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event){
        StatDisplayCommand.register(event.getDispatcher());
        AscensionCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerLaunch(ServerStartedEvent event){





    }






    public static PathInteractionHolder getPathInteractionHolder(){return pathInteractionHolder;}
    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static class ModEvents {

        @SubscribeEvent
        public static void onEntityAttributeModificationEvent(final EntityAttributeModificationEvent event) {

            event.add(
                    EntityType.PLAYER,
                    AscensionAttributes.MAX_QI
            );
        }




        @SubscribeEvent
        public static void registerPayloads(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar(AscensionCraft.MOD_ID);

            registrar.playToServer(
                    CycleDropModePacket.TYPE,
                    CycleDropModePacket.STREAM_CODEC,
                    CycleDropModePacket::handle
            );
            registrar.playToServer(
                    CycleShapePacket.TYPE,
                    CycleShapePacket.STREAM_CODEC,
                    CycleShapePacket::handle
            );
            registrar.playToServer(
                    UpdateSkillSlotPacket.TYPE,
                    UpdateSkillSlotPacket.STREAM_CODEC,
                    UpdateSkillSlotPacket::handle
            );
            registrar.playToServer(
                    SelectSkillSlotPacket.TYPE,
                    SelectSkillSlotPacket.STREAM_CODEC,
                    SelectSkillSlotPacket::handle
            );

            registrar.playToServer(
                    ToggleCultivationSuppressedPacket.TYPE,
                    ToggleCultivationSuppressedPacket.STREAM_CODEC,
                    ToggleCultivationSuppressedPacket::handle
            );

            registrar.playToServer(
                    UpdateAttributeSuppressionPacket.TYPE,
                    UpdateAttributeSuppressionPacket.STREAM_CODEC,
                    UpdateAttributeSuppressionPacket::handle
            );

            registrar.playToClient(
                    OpenStarterSelectionPacket.TYPE,
                    OpenStarterSelectionPacket.STREAM_CODEC,
                    OpenStarterSelectionPacket::handle
            );

            registrar.playToServer(
                    ChooseStarterOptionPacket.TYPE,
                    ChooseStarterOptionPacket.STREAM_CODEC,
                    ChooseStarterOptionPacket::handle
            );

        }
    }


}