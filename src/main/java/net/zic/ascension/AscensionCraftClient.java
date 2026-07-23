package net.zic.ascension;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.zic.ascension.client.keybind.IntrospectionKeybindHandler;
import net.zic.ascension.client.particle.ParticleFieldController;
import net.zic.ascension.client.particle.ParticleFieldParticle;
import net.zic.ascension.client.keybind.ModKeybinds;
import net.zic.ascension.client.keybind.TabletKeybindHandler;
import net.zic.ascension.client.keybind.TabletScrollHandler;
import net.zic.ascension.client.gui.AscensionHudOverlay;
import net.zic.ascension.client.gui.SkillWheelOverlay;
import net.zic.ascension.client.renderer.TabletOutlineRenderer;
import net.zic.ascension.client.tooltip.AscensionClientTooltipProviders;
import net.zic.ascension.common.AscensionCreativeSections;
import net.zic.ascension.common.particle.AscensionParticles;
import net.zic.ascension.api.core.skill.castable.particle_field.ParticleFieldParticleKind;


@Mod(value = AscensionCraft.MOD_ID,dist = Dist.CLIENT)
public class AscensionCraftClient {

    private static final TabletOutlineRenderer TABLET_OUTLINE = new TabletOutlineRenderer();



    public AscensionCraftClient(IEventBus modEventBus, ModContainer modContainer)
    {


        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(AscensionCraftClient::registerKeyBindings);
        modEventBus.addListener(ClientEvents::onClientSetup);
        modEventBus.addListener(AscensionHudOverlay::registerGuiLayers);
        NeoForge.EVENT_BUS.addListener(AscensionHudOverlay::hideVanillaHealth);

    }

    private static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(ModKeybinds.CYCLE_MODE);
        event.register(ModKeybinds.OPEN_INTROSPECTION);
        event.register(ModKeybinds.OPEN_SKILL_WHEEL);
    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID,value = Dist.CLIENT)
    static class ClientEvents{

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {

        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(
                    AscensionParticles.PARTICLE_FIELD_SPARK.get(),
                    sprites -> new ParticleFieldParticle.Provider(ParticleFieldParticleKind.SPARK, sprites)
            );
            event.registerSpriteSet(
                    AscensionParticles.PARTICLE_FIELD_WISP.get(),
                    sprites -> new ParticleFieldParticle.Provider(ParticleFieldParticleKind.WISP, sprites)
            );
            event.registerSpriteSet(
                    AscensionParticles.PARTICLE_FIELD_BLOB.get(),
                    sprites -> new ParticleFieldParticle.Provider(ParticleFieldParticleKind.BLOB, sprites)
            );
            event.registerSpriteSet(
                    AscensionParticles.PARTICLE_FIELD_MOTE.get(),
                    sprites -> new ParticleFieldParticle.Provider(ParticleFieldParticleKind.MOTE, sprites)
            );
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
                AscensionClientTooltipProviders.registerAll();
                AscensionCreativeSections.register();
            });
        }

    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
    static class ScrollEvents {
        @SubscribeEvent
        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            TabletScrollHandler.onMouseScroll(event);
        }
    }

    // ── GAME bus events (in-game ticks, rendering) ────────────────────────────
    @EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
    static class ClientGameEvents {

        @SubscribeEvent
        public static void onRenderLevelStage(net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentFeatures event) {
            TABLET_OUTLINE.onRenderLevelStage(event);
        }

        @SubscribeEvent
        public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
            TabletKeybindHandler.onClientTick(event);
            IntrospectionKeybindHandler.onClientTick(event);
            SkillWheelOverlay.onClientTick();
            ParticleFieldController.tick();
        }
    }



}
