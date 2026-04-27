package net.thejadeproject.ascension.refactor_packages.physiques;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.GenericPhysique;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

public class ModPhysiques {
    public static final DeferredRegister<IPhysique> PHYSIQUES =DeferredRegister.create(AscensionRegistries.Physiques.PHSIQUES_REGISTRY, AscensionCraft.MOD_ID);



    /*
    .addPath(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"essence")) allows you to choose what path to give the Physique what it can cultivate.
    Without a path it can not cultivate anything. You can add as many paths as you want or as little as you want.

    .addPathBonus(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"essence"),0.5) is what chooses how fast it can cultivate that specific path.
    It can be set to 0.1 - Max Integer and the higher the faster. -[number] reduces efficiency

     */


    public static final DeferredHolder<IPhysique,? extends GenericPhysique> MORTAL = PHYSIQUES.register("mortal",()->
            new GenericPhysique(Component.translatable("ascension.physiques.mortal"))
                    .addPath(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"body"))
                    .addPath(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"essence"))
                    .addPathBonus(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"body"),0.5)
                    .addPathBonus(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"essence"),0.5)
            );

    public static final DeferredHolder<IPhysique,? extends GenericPhysique> CRIPPLE = PHYSIQUES.register("cripple",()->
            new GenericPhysique(Component.translatable("ascension.physiques.cripple"))
            );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> SEVERED_MERIDIANS = PHYSIQUES.register("severed_meridians",()->
            new GenericPhysique(Component.translatable("ascension.physiques.severed_meridians"))
                    .addPath(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"body"))
                    .addPathBonus(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"body"),0.5)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> SWORD_BONE = PHYSIQUES.register("sword_bone",()->
            new GenericPhysique(Component.translatable("ascension.physiques.sword_bone"))
                    .addPath(ModPaths.BODY.getId())
                    .addPathBonus(ModPaths.BODY.getId(),0.5)
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(),0.5)
                    .addPath(ModPaths.SWORD.getId())
                    .addPathBonus(ModPaths.SWORD.getId(),2.0)
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> FLAME_TOUCHED = PHYSIQUES.register("flame_touched",()->
            new GenericPhysique(Component.translatable("ascension.physiques.flame_touched"))
                    .addPath(ModPaths.BODY.getId())
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.BODY.getId(),0.5)
                    .addPathBonus(ModPaths.ESSENCE.getId(),1.0)
                    .addPathBonus(ModPaths.FIRE.getId(),2.0)
    );

    /*
    Basic Attuned Physiques (IDK dropped by unsuspecting villagers or pillagers in like really low percentages?
    Like the 'regular beings in the world have some a small chance of being born with a weak spirit-root' type shi that cultivation novels pull)
    This would also lead to things like villager slaughter farms, which promotes the demonic path :0
    And then do the same but with like body path, where instead of attuned, its like different organs (plays into flips technique idea)

    But IDK if these are like too repetitive since there are already techniques...
   */
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> FIRE_ATTUNED = PHYSIQUES.register("fire_attuned",()->
            new GenericPhysique(Component.translatable("ascension.physiques.fire_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(),1.0)
                    .addPathBonus(ModPaths.FIRE.getId(),1.25)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> WATER_ATTUNED = PHYSIQUES.register("water_attuned",()->
            new GenericPhysique(Component.translatable("ascension.physiques.water_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(),1.0)
                    .addPathBonus(ModPaths.WATER.getId(),1.25)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> EARTH_ATTUNED = PHYSIQUES.register("earth_attuned",()->
            new GenericPhysique(Component.translatable("ascension.physiques.earth_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(),1.0)
                    .addPathBonus(ModPaths.EARTH.getId(),1.25)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> WOOD_ATTUNED = PHYSIQUES.register("wood_attuned",()->
            new GenericPhysique(Component.translatable("ascension.physiques.wood_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(),1.0)
                    .addPathBonus(ModPaths.WOOD.getId(),1.25)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> METAL_ATTUNED = PHYSIQUES.register("metal_attuned",()->
            new GenericPhysique(Component.translatable("ascension.physiques.metal_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(),1.0)
                    .addPathBonus(ModPaths.METAL.getId(),1.25)
    );
    // Deviant Elements slightly stronger cause why not?
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> LIGHTNING_ATTUNED = PHYSIQUES.register("lightning_attuned",()->
            new GenericPhysique(Component.translatable("ascension.physiques.lightning_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(),1.25)
                    .addPathBonus(ModPaths.LIGHTNING.getId(),1.75)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> WIND_ATTUNED = PHYSIQUES.register("wind_attuned",()->
            new GenericPhysique(Component.translatable("ascension.physiques.wind_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(),1.25)
                    .addPathBonus(ModPaths.WIND.getId(),1.75)
    );

    // Basic Essence Only Physiques -> Like derivatives of a supreme bone that is common in cultivation novels? IDK what I'm doing any more
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> ESSENCE_BONE_MORTAL = PHYSIQUES.register("mortal_essence_bone",()->
            new GenericPhysique(Component.translatable("ascension.physiques.mortal_essence_bone"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(),1.5)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> ESSENCE_BONE_SPECIAL = PHYSIQUES.register("special_essence_bone",()->
            new GenericPhysique(Component.translatable("ascension.physiques.special_essence_bone"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(),2.0)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> ESSENCE_BONE_HEAVENLY = PHYSIQUES.register("heavenly_essence_bone",()->
            new GenericPhysique(Component.translatable("ascension.physiques.heavenly_essence_bone"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(),3.0)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> ESSENCE_BONE_DIVINE = PHYSIQUES.register("divine_essence_bone",()->
            new GenericPhysique(Component.translatable("ascension.physiques.divine_essence_bone"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(),5.0)
    );

    // Weapon Physiques
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> FLOW_SEVERING_EYES = PHYSIQUES.register("flow_severing_eyes",()->
            new GenericPhysique(Component.translatable("ascension.physiques.flow_severing_eyes"))
                    .addPath(ModPaths.SWORD.getId())
                    .addPath(ModPaths.BLADE.getId())
                    .addPath(ModPaths.AXE.getId())
                    .addPath(ModPaths.SPEAR.getId())
                    .addPathBonus(ModPaths.SWORD.getId(),2.0)
                    .addPathBonus(ModPaths.BLADE.getId(),2.0)
                    .addPathBonus(ModPaths.AXE.getId(),2.0)
                    .addPathBonus(ModPaths.SPEAR.getId(),2.0)
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> THIN_SWORD_PULSE = PHYSIQUES.register("thin_sword_pulse",()->
            new GenericPhysique(Component.translatable("ascension.physiques.thin_sword_pulse"))
            .addPath(ModPaths.SWORD.getId())
            .addPath(ModPaths.ESSENCE.getId())
            .addPathBonus(ModPaths.SWORD.getId(),1.5)
            .addPathBonus(ModPaths.ESSENCE.getId(),1.25)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> WILD_CLEAVER_VETERAN = PHYSIQUES.register("wild_cleaver_veteran",()->
            new GenericPhysique(Component.translatable("ascension.physiques.wild_cleaver_veteran"))
            .addPath(ModPaths.BLADE.getId())
            .addPath(ModPaths.BODY.getId())
            .addPathBonus(ModPaths.BLADE.getId(),1.5)
            .addPathBonus(ModPaths.BODY.getId(),1.25)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> ARROW_BLESSED = PHYSIQUES.register("arrow_blessed",()->
            new GenericPhysique(Component.translatable("ascension.physiques.arrow_blessed"))
            .addPath(ModPaths.BOW.getId())
            .addPath(ModPaths.ESSENCE.getId())
            .addPathBonus(ModPaths.BOW.getId(),1.5)
            .addPathBonus(ModPaths.ESSENCE.getId(),1.25)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> IRON_BULWARK_SPINE = PHYSIQUES.register("iron_bulwark_spine",()->
            new GenericPhysique(Component.translatable("ascension.physiques.iron_bulwark_spine"))
            .addPath(ModPaths.SHIELD.getId())
            .addPath(ModPaths.BODY.getId())
            .addPathBonus(ModPaths.SHIELD.getId(),1.5)
            .addPathBonus(ModPaths.BODY.getId(),1.25)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> BRUISED_KNUCKLE_BODY = PHYSIQUES.register("bruised_knuckle_body",()->
            new GenericPhysique(Component.translatable("ascension.physiques.bruised_knuckle_body"))
            .addPath(ModPaths.FIST.getId())
            .addPath(ModPaths.BODY.getId())
            .addPathBonus(ModPaths.FIST.getId(),1.5)
            .addPathBonus(ModPaths.BODY.getId(),1.25)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> POINTED_EYES = PHYSIQUES.register("pointed_eyes",()->
            new GenericPhysique(Component.translatable("ascension.physiques.pointed_eyes"))
            .addPath(ModPaths.SPEAR.getId())
            .addPath(ModPaths.ESSENCE.getId())
            .addPathBonus(ModPaths.SPEAR.getId(),1.5)
            .addPathBonus(ModPaths.ESSENCE.getId(),1.25)
    );

    // Other Physiques
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> VIRTUOSO_BUDDHA = PHYSIQUES.register("virtuoso_buddha",()->
            new GenericPhysique(Component.translatable("ascension.physiques.virtuoso_buddha"))
                    .addPath(ModPaths.BODY.getId())
                    .addPath(ModPaths.BUDDHIST.getId())
                    .addPath(ModPaths.VIRTUOUS.getId())
                    .addPathBonus(ModPaths.BODY.getId(),3.0)
                    .addPathBonus(ModPaths.BUDDHIST.getId(),2.0)
                    .addPathBonus(ModPaths.VIRTUOUS.getId(),1.0)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> BLOOD_FIEND = PHYSIQUES.register("blood_fiend",()->
            new GenericPhysique(Component.translatable("ascension.physiques.blood_fiend"))
                    .addPath(ModPaths.SOUL.getId())
                    .addPath(ModPaths.DEMONIC.getId())
                    .addPathBonus(ModPaths.SOUL.getId(),2.0)
                    .addPathBonus(ModPaths.DEMONIC.getId(),3.0)
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> STONE_MONKEY = PHYSIQUES.register("stone_monkey",()->
                    new GenericPhysique(Component.translatable("ascension.physiques.stone_monkey"))
                            .addPath(ModPaths.BODY.getId())
                            .addPath(ModPaths.EARTH.getId())
                            .addPathBonus(ModPaths.BODY.getId(),1.75)
                            .addPathBonus(ModPaths.EARTH.getId(),2.0)
            // chaos dao idk?
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> TYRANT_BODY = PHYSIQUES.register("tyrant_body",()->
            new GenericPhysique(Component.translatable("ascension.physiques.tyrant_body"))
                    .addPath(ModPaths.BODY.getId())
                    .addPathBonus(ModPaths.BODY.getId(),1.2)
                    .addPath(ModPaths.FIST.getId())
                    .addPathBonus(ModPaths.FIST.getId(), 2.2)
    );


    // TODO: change this
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> SCHOLARS_SOUL = PHYSIQUES.register("scholars_soul",()->
            new GenericPhysique(Component.translatable("ascension.physiques.scholars_soul"))
                    .addPath(ModPaths.SOUL.getId())
                    .addPathBonus(ModPaths.SOUL.getId(),1.5)
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> WORLD_DOMINATOR =
            PHYSIQUES.register("world_dominator", () ->
                    new GenericPhysique(Component.translatable("ascension.physiques.world_dominator")) {

                        @Override
                        public void onPhysiqueAdded(
                                IEntityData heldEntity,
                                ResourceLocation oldPhysique,
                                IPhysiqueData oldPhysiqueData
                        ) {
                            super.onPhysiqueAdded(heldEntity, oldPhysique, oldPhysiqueData);

                            if (heldEntity.getAttachedEntity() instanceof ServerPlayer player) {
                                Component message = Component.translatable(
                                        "ascension.message.physique.world_dominator.acquired",
                                        player.getDisplayName().copy().withStyle(ChatFormatting.WHITE),
                                        Component.translatable("ascension.physiques.world_dominator.bracketed")
                                                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)
                                ).withStyle(ChatFormatting.GOLD);

                                player.server.getPlayerList().broadcastSystemMessage(message, false);
                            }
                        }
                    }
                            .addPath(ModPaths.BODY.getId()).addPathBonus(ModPaths.BODY.getId(), 5.0)
            );



    public static void register(IEventBus modEventBus){
        PHYSIQUES.register(modEventBus);
    }

}
