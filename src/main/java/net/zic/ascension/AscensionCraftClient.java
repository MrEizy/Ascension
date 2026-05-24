package net.zic.ascension;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;


@Mod(value = AscensionCraft.MOD_ID,dist = Dist.CLIENT)
public class AscensionCraftClient {
    public AscensionCraftClient(IEventBus modEventBus, ModContainer modContainer)
    {


        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID,value = Dist.CLIENT)
    static class ClientEvents{

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {

        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {

        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {



        }


        @SubscribeEvent
        public static void onRegisterTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {

        }


        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {

            });
        }

    }



}
