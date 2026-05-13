package net.zic.ascension;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
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
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.clients.renderer.TechniqueStandRenderer;
import net.zic.ascension.common.blocks.entity.ModBlockEntities;
import net.zic.ascension.clients.FlameGourdClientTooltip;
import net.zic.ascension.clients.hud.FlameBarOverlay;
import net.zic.ascension.clients.renderer.CauldronPedestalRenderer;
import net.zic.ascension.clients.renderer.FlameStandRenderer;
import net.zic.ascension.clients.renderer.PillCauldronLowHumanBlockEntityRenderer;
import net.zic.ascension.entity.ModEntities;
import net.zic.ascension.entity.client.CushionRenderer;
import net.zic.ascension.entity.client.form.PlayerBodyEntityRenderer;
import net.zic.ascension.entity.client.rat.RatRenderer;
import net.zic.ascension.common.items.data_components.ModDataComponents;

import net.zic.ascension.common.items.ModItems;
import net.zic.ascension.common.items.artifacts.FlameGourd;
import net.zic.ascension.menus.ModMenuTypes;
import net.zic.ascension.menus.custom.herb_pouch.HerbPouchScreen;
import net.zic.ascension.menus.custom.pill_cauldron.PillCauldronLowHumanScreen;
import net.zic.ascension.menus.custom.spirit_ring.SpatialRingInventoryScreen;
import net.zic.ascension.menus.custom.spirit_ring.SpatialRingModifierScreen;
import net.zic.ascension.particle.ModParticles;
import net.zic.ascension.particle.aura.*;
import net.zic.ascension.particle.particles.CultivationParticles;
import net.zic.ascension.refactor_packages.gui.ModOverlays;
import net.zic.ascension.shaders.client.ModShaders;
import net.zic.ascension.shaders.client.RiftRenderer;

import net.zic.ascension.util.KeyBindHandler;
import net.favouriteless.modopedia.api.registries.client.PageComponentRegistry;
import net.zic.ascension.common.modopedia.ContinueButtonComponent;

@Mod(value = AscensionCraft.MOD_ID,dist = Dist.CLIENT)
public class AscensionCraftClient {
    public AscensionCraftClient(IEventBus modEventBus, ModContainer modContainer)
    {
        KeyBindHandler.register();
        PageComponentRegistry.get().register(ContinueButtonComponent.ID, ContinueButtonComponent::new);


        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        NeoForge.EVENT_BUS.register(FlameBarOverlay.class);

        ModOverlays.register();
    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID,value = Dist.CLIENT)
    static class ClientEvents{

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.PILL_CAULDRON_LOW_HUMAN_MENU.get(), PillCauldronLowHumanScreen::new);
            event.register(ModMenuTypes.SPATIAL_RING_INVENTORY_MENU.get(), SpatialRingInventoryScreen::new);
            event.register(ModMenuTypes.SPATIAL_RING_MODIFIER_MENU.get(), SpatialRingModifierScreen::new);
            event.register(ModMenuTypes.HERB_POUCH_MENU.get(), HerbPouchScreen::new);

        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.CULTIVATION_PARTICLES.get(), CultivationParticles.Provider::new);
            event.registerSpriteSet(ModParticles.AURA_SMOKE.get(), AuraSmokeParticle.Provider::new);
            event.registerSpriteSet(ModParticles.AURA_SPARK.get(), AuraSparkParticle.Provider::new);
            event.registerSpriteSet(ModParticles.AURA_FLAME.get(), AuraFlameParticle.Provider::new);
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {

            event.registerEntityRenderer(ModEntities.PILL_PROJECTILE.get(), ThrownItemRenderer::new);

            // Floating item above each ingredient pedestal
            event.registerBlockEntityRenderer(
                    ModBlockEntities.CAULDRON_PEDESTAL.get(),
                    CauldronPedestalRenderer::new
            );

            event.registerBlockEntityRenderer(
                    ModBlockEntities.TECHNIQUE_STAND_BE.get(),
                    TechniqueStandRenderer::new
            );

            // Ghost-block hints for missing multiblock pieces on the cauldron itself
            event.registerBlockEntityRenderer(
                    ModBlockEntities.PILL_CAULDRON_LOW_HUMAN.get(),
                    PillCauldronLowHumanBlockEntityRenderer::new
            );

            // Flame item floating above the lit Flame Stand
            event.registerBlockEntityRenderer(
                    ModBlockEntities.FLAME_STAND.get(),
                    FlameStandRenderer::new
            );

        }


        @SubscribeEvent
        public static void onRegisterTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(FlameGourd.FlameGourdTooltip.class, FlameGourdClientTooltip::new);
        }

        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent event) {
            try {
                ModShaders.register(event);
            } catch (Exception e) {
                throw new RuntimeException("Failed to register Ascension shaders", e);
            }
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                // REGISTER RENDERERS - MUST BE IN enqueueWork
                EntityRenderers.register(ModEntities.RIFT.get(), RiftRenderer::new);
                EntityRenderers.register(ModEntities.RAT.get(), RatRenderer::new);
                EntityRenderers.register(ModEntities.POISON_PILL.get(), ThrownItemRenderer::new);
                EntityRenderers.register(ModEntities.CUSHION_ENTITY.get(), CushionRenderer::new);
                EntityRenderers.register(ModEntities.NEEDLE_PROJECTILE.get(), ThrownItemRenderer::new);
                EntityRenderers.register(ModEntities.FAKE_PLAYER.get(), PlayerBodyEntityRenderer::new);
                // Register item properties
                ItemProperties.register(ModItems.SPIRITUAL_STONE.get(),
                        ResourceLocation.fromNamespaceAndPath("ascension", "stack_size"),
                        (itemStack, clientLevel, livingEntity, seed) -> {
                            int count = itemStack.getCount();
                            if (count >= 32) return 3.0F;
                            if (count >= 16) return 2.0F;
                            if (count >= 2) return 1.0F;
                            return 0.0F;
                        });

                ItemProperties.register(ModItems.PHYSIQUE_ESSENCE.get(),
                        ResourceLocation.fromNamespaceAndPath("ascension", "physique_variant"),
                        (itemStack, clientLevel, livingEntity, seed) -> {
                            String physiqueId = itemStack.get(ModDataComponents.PHYSIQUE_ID.get());
                            if (physiqueId != null && physiqueId.equals("ascension:jade_bone_physique")) {
                                return 1.0F;
                            }
                            return 0.0F;
                        });

                ItemProperties.register(ModItems.TECHNIQUE_MANUAL.get(),
                        ResourceLocation.fromNamespaceAndPath("ascension", "technique_variant"),
                        (itemStack, clientLevel, livingEntity, seed) -> {
                            String techniqueId = itemStack.get(ModDataComponents.TECHNIQUE_ID.get());
                            if (techniqueId == null) return 0.0F;
                            return switch (techniqueId) {
                                case "ascension:heart_fire_technique"  -> 1.0F;
                                case "ascension:kidney_water_technique" -> 2.0F;
                                case "ascension:liver_wood_technique"  -> 3.0F;
                                case "ascension:spleen_earth_technique" -> 4.0F;
                                case "ascension:lung_metal_technique"  -> 5.0F;
                                default -> 0.0F;
                            };
                        });
            });
        }

    }



}
