package net.thejadeproject.ascension;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.blocks.custom.functions.FreezingEffectItems;
import net.thejadeproject.ascension.common.blocks.entity.ModBlockEntities;
import net.thejadeproject.ascension.common.command.AscensionCommand;
import net.thejadeproject.ascension.common.command.commands.ReadChapterCommand;

import net.thejadeproject.ascension.common.items.artifacts.talismans.SoulAnchorTalisman;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.datagen.loot.functions.SetRandomIntComponentFunction;
import net.thejadeproject.ascension.datagen.loot.functions.SetTechniquePageFunction;
import net.thejadeproject.ascension.entity.custom.NeedleProjectile;
import net.thejadeproject.ascension.events.TeleportationEventHandler;

import net.thejadeproject.ascension.common.items.artifacts.talismans.DeathRecallTalisman;


import net.thejadeproject.ascension.common.effects.ModEffects;
import net.thejadeproject.ascension.entity.ModEntities;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.datagen.loot.ModLootModifiers;
import net.thejadeproject.ascension.datagen.loot.conditions.ModLootConditions;
import net.thejadeproject.ascension.mob_cultivation.util.EntityAttributeManager;
import net.thejadeproject.ascension.network.ModPayloads;
import net.thejadeproject.ascension.particle.ModParticles;

import net.thejadeproject.ascension.recipe.ModRecipes;
import net.thejadeproject.ascension.menus.ModMenuTypes;


import net.thejadeproject.ascension.refactor_packages.alchemy.ModPillEffects;
import net.thejadeproject.ascension.refactor_packages.bloodlines.ModBloodlines;
import net.thejadeproject.ascension.refactor_packages.entity_data.GenericEntityData;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.ModDataSources;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.handlers.player.InputHandler;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.physiques.ModPhysiques;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.stats.custom.ModStats;
import net.thejadeproject.ascension.refactor_packages.techniques.ModTechniques;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.handlers.BloodfeastKillHandler;
import net.thejadeproject.ascension.util.KeyBindHandler;

import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.util.ModAttributes;
import net.thejadeproject.ascension.util.ToolTips.ToolTipManager;
import net.thejadeproject.ascension.common.villager.ModVillagers;
import net.thejadeproject.ascension.worldgen.ModFeatureRegistration;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AscensionCraft.MOD_ID)
public class AscensionCraft {
    public static float hue;
    public static final String MOD_ID = "ascension";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<String, String> SECT_DATA = new HashMap<>();

    public static ResourceLocation prefix(String name){
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.

    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MOD_ID);

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> SPATIALRING_UUID = COMPONENTS.register("spatialring_uuid", () -> DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build());

    public void register(IEventBus modEventBus){
        COMPONENTS.register(modEventBus);
        RECIPES.register(modEventBus);

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);

        ModRecipes.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        ModLootModifiers.register(modEventBus);
        ModLootConditions.register(modEventBus);
        ModAttachments.register(modEventBus);

        SetRandomIntComponentFunction.LOOT_FUNCTION_TYPES.register(modEventBus);
        SetTechniquePageFunction.LOOT_FUNCTION_TYPES.register(modEventBus);

        ModParticles.register(modEventBus);
        ModEntities.register(modEventBus);

        ModPhysiques.register(modEventBus);
        ModForms.register(modEventBus);
        ModBloodlines.register(modEventBus);
        ModPaths.register(modEventBus);

        ModEffects.register(modEventBus);

        ModAttributes.register(modEventBus);

        ModSkills.register(modEventBus);

        ModPillEffects.register(modEventBus);
        ModStats.register(modEventBus);
        // In your main mod class, in the constructor:
        NeoForge.EVENT_BUS.register(TeleportationEventHandler.class);

        ModVillagers.VILLAGER_PROFESSIONS.register(modEventBus);
        ModVillagers.POI_TYPES.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        ModTechniques.register(modEventBus);
        ModDataSources.register(modEventBus);
        ModDataComponents.register(modEventBus);
        CreativeTabHandler.register(modEventBus);

        ModFeatureRegistration.register(modEventBus);

        NeoForge.EVENT_BUS.register(new BloodfeastKillHandler());
        //NeoForge.EVENT_BUS.register(new BloodfeastAoeRenderer());

    }

    public AscensionCraft(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onLoadComplete);

        // Register ourselves for server and other game events we are interested in.
        NeoForge.EVENT_BUS.register(this);


        register(modEventBus);
        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);


        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "ascension/Ascension-Common.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.CULTIVATION_SPEC, "ascension/Ascension-Cultivation.toml");

        modEventBus.addListener(this::registerKeyBindings);
        NeoForge.EVENT_BUS.addListener(this::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogOut);
    }


    private void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindHandler.TOGGLE_ARTIFACT_MODE_KEY);

        event.register(InputHandler.INTROSPECTION);
        event.register(InputHandler.SKILL_WHEEL_OVERLAY);
        event.register(InputHandler.CAST_SKILL_KEY);

    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag playerData = player.getPersistentData();
        ListTag talismansList = new ListTag();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof DeathRecallTalisman talisman) {
                talisman.onPlayerDeath(player, stack);

                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemTag.put("Item", stack.save(player.registryAccess()));
                talismansList.add(itemTag);

                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        if (!talismansList.isEmpty()) {
            playerData.put("DeathRecallTalismans", talismansList);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        ServerPlayer newPlayer = (ServerPlayer) event.getEntity();
        ServerPlayer oldPlayer = (ServerPlayer) event.getOriginal();

        CompoundTag oldData = oldPlayer.getPersistentData();

        if (oldData.contains("DeathRecallTalismans")) {
            ListTag talismansList = oldData.getList("DeathRecallTalismans", Tag.TAG_COMPOUND);
            RegistryAccess registryAccess = newPlayer.registryAccess();

            for (int i = 0; i < talismansList.size(); i++) {
                CompoundTag itemTag = talismansList.getCompound(i);
                int preferredSlot = itemTag.getInt("Slot");
                ItemStack stack = ItemStack.parse(registryAccess, itemTag.getCompound("Item")).orElse(ItemStack.EMPTY);

                if (!stack.isEmpty()) {
                    // Try to put back in original slot first, otherwise find empty slot
                    if (newPlayer.getInventory().getItem(preferredSlot).isEmpty()) {
                        newPlayer.getInventory().setItem(preferredSlot, stack);
                    } else {
                        newPlayer.getInventory().add(stack);
                    }

                    // Notify player
                    newPlayer.displayClientMessage(
                            Component.translatable("ascension.deathrecall.bound_on_respawn"),
                            true
                    );
                }
            }

            // Clean up old data
            oldData.remove("DeathRecallTalismans");
        }
    }


    @SubscribeEvent
    public void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        event.getDrops().removeIf(itemEntity ->
                itemEntity.getItem().getItem() instanceof DeathRecallTalisman
        );

        SoulAnchorTalisman.tryActivateFromDrops(player, event.getDrops());
    }


    private void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide()) {
            event.getEntity().getData(ModAttachments.ENTITY_DATA).tick();
        }
    }

    private void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event){
        
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = (Player) event.getEntity();
        //player.setHealth(player.getMaxHealth()/2);
        player.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(player.getData(ModAttachments.MOVEMENT_SPEED));

        if(!event.getEntity().level().isClientSide()){
            //TODO ensure sync
            //System.out.println("TRYING TO SYNC PLAYER DATA");
            if(player.getData(ModAttachments.ENTITY_DATA) instanceof GenericEntityData genericEntityData){
                genericEntityData.sync(player);
                genericEntityData.getAscensionAttributeHolder().log();
            }
            player.getData(ModAttachments.ENTITY_DATA).getSkillCastHandler().sync(player);
            PacketDistributor.sendToPlayer((ServerPlayer) player,new SyncAttributeHolder(player.getData(ModAttachments.ENTITY_DATA).getAscensionAttributeHolder()));

            for(IEntityFormData formData:player.getData(ModAttachments.ENTITY_DATA).getFormData()){
                formData.getStatSheet().sync((ServerPlayer) player,formData.getEntityFormId());
            }
        }


    }

    public void onLoadComplete(FMLLoadCompleteEvent event) {

        // Subject to change
        EntityAttributeManager.changeAttributeRange(1.0, Double.MAX_VALUE, (RangedAttribute) Attributes.MAX_HEALTH.value());
        EntityAttributeManager.changeAttributeRange(0.0, Double.MAX_VALUE, (RangedAttribute) Attributes.ATTACK_DAMAGE.value());
        EntityAttributeManager.changeAttributeRange(0.0, Double.MAX_VALUE, (RangedAttribute) Attributes.ARMOR.value());
        EntityAttributeManager.changeAttributeRange(0.0, Double.MAX_VALUE, (RangedAttribute) Attributes.ARMOR_TOUGHNESS.value());
        EntityAttributeManager.changeAttributeRange(0.0, Double.MAX_VALUE, (RangedAttribute) Attributes.SAFE_FALL_DISTANCE.value());

        EntityAttributeManager.changeAttributeRange(0.0, 100.0, (RangedAttribute) Attributes.MOVEMENT_SPEED.value());
        EntityAttributeManager.changeAttributeRange(0.0, 100.0, (RangedAttribute) Attributes.JUMP_STRENGTH.value());

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ToolTipManager.registerAllTooltips();
        FreezingEffectItems.onCommonSetup(event);

        event.enqueueWork(() -> {
            ModSkills.registerTickingSkills();
            DispenserBlock.registerBehavior(ModItems.SILVER_NEEDLE.get(), new DefaultDispenseItemBehavior() {
                @Override
                protected ItemStack execute(BlockSource source, ItemStack stack) {
                    Level level = source.level();
                    Direction facing = source.state().getValue(DispenserBlock.FACING);
                    Position position = DispenserBlock.getDispensePosition(source);

                    NeedleProjectile needle = new NeedleProjectile(
                            level, position.x(), position.y(), position.z(), stack.copyWithCount(1)
                    );
                    needle.shoot(facing.getStepX(), facing.getStepY() + 0.1, facing.getStepZ(), 2.5f, 1.0f);
                    level.addFreshEntity(needle);
                    stack.shrink(1);
                    return stack;
                }
            });
        });

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.RAT_SPAWN_EGG);
        }
    }


    private void registerCommands(RegisterCommandsEvent event) {
        AscensionCommand.register(event.getDispatcher());
        ReadChapterCommand.register(event.getDispatcher());
    }


    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static class ModEvents {

        @SubscribeEvent
        public static void onEntityAttributeModificationEvent(final EntityAttributeModificationEvent event) {
            event.add(EntityType.PLAYER, ModAttributes.MAX_CASTING_INSTANCES);
            event.add(EntityType.PLAYER, ModAttributes.PLAYER_MAX_QI);
            event.add(EntityType.PLAYER, ModAttributes.PLAYER_QI_REGEN_RATE);
            event.add(EntityType.PLAYER, ModAttributes.SKILL_DAMAGE_MULTIPLIER);
            event.add(EntityType.PLAYER, ModAttributes.MAX_QI);
            event.add(EntityType.PLAYER, ModAttributes.QI_REGEN_RATE);

        }






        @SubscribeEvent
        public static void addWanderingTraderTrades(WandererTradesEvent event) {
            List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();
            List<VillagerTrades.ItemListing> genericTrades = event.getGenericTrades();

            rareTrades.add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(ModItems.SPIRITUAL_STONE.get(), 64),
                    makePageStack(AscensionCraft.MOD_ID + ":white_lightning_ten_stage_technique", 0),
                    1, 10, 0f
            ));
            rareTrades.add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(ModItems.SPIRITUAL_STONE.get(), 64),
                    makePageStack(AscensionCraft.MOD_ID + ":white_lightning_ten_stage_technique", 1),
                    1, 10, 0f
            ));
            rareTrades.add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(ModItems.SPIRITUAL_STONE.get(), 64),
                    makePageStack(AscensionCraft.MOD_ID + ":white_lightning_ten_stage_technique", 3),
                    1, 10, 0f
            ));
            rareTrades.add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(ModItems.SPIRITUAL_STONE.get(), 64),
                    makePageStack(AscensionCraft.MOD_ID + ":white_lightning_ten_stage_technique", 4),
                    1, 10, 0f
            ));
            rareTrades.add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(ModItems.SPIRITUAL_STONE.get(), 64),
                    makePageStack(AscensionCraft.MOD_ID + ":white_lightning_ten_stage_technique", 6),
                    1, 10, 0f
            ));
            rareTrades.add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(ModItems.SPIRITUAL_STONE.get(), 64),
                    makePageStack(AscensionCraft.MOD_ID + ":white_lightning_ten_stage_technique", 7),
                    1, 10, 0f
            ));
            rareTrades.add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(ModItems.SPIRITUAL_STONE.get(), 64),
                    makePageStack(AscensionCraft.MOD_ID + ":white_lightning_ten_stage_technique", 8),
                    1, 10, 0f
            ));
            rareTrades.add((entity, randomSource) -> new MerchantOffer(
                    new ItemCost(ModItems.SPIRITUAL_STONE.get(), 64),
                    makePageStack(AscensionCraft.MOD_ID + ":white_lightning_ten_stage_technique", 9),
                    1, 10, 0f
            ));
        }

        private static ItemStack makePageStack(String techniqueId, int pageIndex) {
            ItemStack stack = new ItemStack(ModItems.TECHNIQUE_PAGE.get());
            stack.set(ModDataComponents.TECHNIQUE_ID.get(), techniqueId);
            stack.set(ModDataComponents.PAGE_INDEX.get(), pageIndex);
            return stack;
        }




        @SubscribeEvent
        public static void registerPayloads(RegisterPayloadHandlersEvent event) {


            ModPayloads.registerPayloads(event);
        }
    }
}