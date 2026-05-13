package net.thejadeproject.ascension.refactor_packages.physiques;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.ElementalBodyPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.EvolvingPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.GenericPhysique;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

public class ModPhysiques {
    public static final DeferredRegister<IPhysique> PHYSIQUES = DeferredRegister.create(AscensionRegistries.Physiques.PHSIQUES_REGISTRY, AscensionCraft.MOD_ID);



    /*
        .addPath(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"essence")) allows you to choose what path to give the Physique what it can cultivate.
    Without a path it can not cultivate anything. You can add as many paths as you want or as little as you want.
        You can also use .addPath(ModPaths.---.getId())

    .addPathBonus(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"essence"),0.5) is what chooses how fast it can cultivate that specific path.
    It can be set to 0.1 - Max Integer and the higher the faster.
        You can also use .addPathBonus(ModPaths.---.getId(), X.X)

     */


    public static final DeferredHolder<IPhysique,? extends GenericPhysique> MORTAL = PHYSIQUES.register("mortal",()->
            new EvolvingPhysique(Component.translatable("ascension.physiques.mortal"))
                    .addEvolution(ModPhysiques.BLESSED.getId())
                    .addEvolution(ModPhysiques.BLOOD_FIEND.getId())
                    .addPath(ModPaths.BODY.getId())
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.BODY.getId(),0.5)
                    .addPathBonus(ModPaths.ESSENCE.getId(),0.5)
                    .setDescription(Component.translatable("ascension.physiques.mortal.description"))
                    .setShortDescription(Component.translatable("ascension.physiques.mortal.description.short"))
            );

    public static final DeferredHolder<IPhysique,? extends GenericPhysique> CRIPPLE = PHYSIQUES.register("cripple",()->
            new EvolvingPhysique(Component.translatable("ascension.physiques.cripple"))
                    .addEvolution(MORTAL.getId())
                    .setDescription(Component.translatable("ascension.physiques.cripple.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.cripple.desc.short"))
    );


    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> SEVERED_MERIDIANS = PHYSIQUES.register("severed_meridians", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.severed_meridians"))
                    .addPath(ModPaths.BODY.getId())
                    .addPathBonus(ModPaths.BODY.getId(), 0.5)
                    .setDescription(Component.translatable("ascension.physiques.severed_meridians.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.severed_meridians.desc.short"))
    );



    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> FLAME_TOUCHED = PHYSIQUES.register("flame_touched", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.flame_touched"))
                    .addPath(ModPaths.BODY.getId())
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.BODY.getId(), 0.5)
                    .addPathBonus(ModPaths.ESSENCE.getId(), 1.0)
                    .addPathBonus(ModPaths.FIRE.getId(), 2.0)
                    .setDescription(Component.translatable("ascension.physiques.flame_touched.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.flame_touched.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> FIRE_ATTUNED = PHYSIQUES.register("fire_attuned", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.fire_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(), 1.0)
                    .addPathBonus(ModPaths.FIRE.getId(), 1.25)
                    .setDescription(Component.translatable("ascension.physiques.fire_attuned.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.fire_attuned.desc.short"))
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> WATER_ATTUNED = PHYSIQUES.register("water_attuned", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.water_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(), 1.0)
                    .addPathBonus(ModPaths.WATER.getId(), 1.25)
                    .setDescription(Component.translatable("ascension.physiques.water_attuned.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.water_attuned.desc.short"))
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> EARTH_ATTUNED = PHYSIQUES.register("earth_attuned", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.earth_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(), 1.0)
                    .addPathBonus(ModPaths.EARTH.getId(), 1.25)
                    .setDescription(Component.translatable("ascension.physiques.earth_attuned.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.earth_attuned.desc.short"))
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> WOOD_ATTUNED = PHYSIQUES.register("wood_attuned", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.wood_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(), 1.0)
                    .addPathBonus(ModPaths.WOOD.getId(), 1.25)
                    .setDescription(Component.translatable("ascension.physiques.wood_attuned.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.wood_attuned.desc.short"))
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> METAL_ATTUNED = PHYSIQUES.register("metal_attuned", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.metal_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(), 1.0)
                    .addPathBonus(ModPaths.METAL.getId(), 1.25)
                    .setDescription(Component.translatable("ascension.physiques.metal_attuned.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.metal_attuned.desc.short"))
    );
    // Deviant Elements slightly stronger cause why not?
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> LIGHTNING_ATTUNED = PHYSIQUES.register("lightning_attuned", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.lightning_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(), 1.25)
                    .addPathBonus(ModPaths.LIGHTNING.getId(), 1.75)
                    .setDescription(Component.translatable("ascension.physiques.lightning_attuned.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.lightning_attuned.desc.short"))
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> WIND_ATTUNED = PHYSIQUES.register("wind_attuned", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.wind_attuned"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(), 1.25)
                    .addPathBonus(ModPaths.WIND.getId(), 1.75)
                    .setDescription(Component.translatable("ascension.physiques.wind_attuned.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.wind_attuned.desc.short"))
    );

    // Basic Essence Only Physiques -> Like derivatives of a supreme bone that is common in cultivation novels? IDK what I'm doing any more
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> ESSENCE_BONE_MORTAL = PHYSIQUES.register("mortal_essence_bone", () ->
            new EvolvingPhysique(Component.translatable("ascension.physiques.mortal_essence_bone"))
                    .addEvolution(ModPhysiques.ESSENCE_BONE_SPECIAL.getId())
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(), 1.5)
                    .setDescription(Component.translatable("ascension.physiques.mortal_essence_bone.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.mortal_essence_bone.desc.short"))
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> ESSENCE_BONE_SPECIAL = PHYSIQUES.register("special_essence_bone", () ->
            new EvolvingPhysique(Component.translatable("ascension.physiques.special_essence_bone"))
                    .addEvolution(ModPhysiques.ESSENCE_BONE_HEAVENLY.getId())
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(), 3.0)
                    .setDescription(Component.translatable("ascension.physiques.special_essence_bone.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.special_essence_bone.desc.short"))
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> ESSENCE_BONE_HEAVENLY = PHYSIQUES.register("heavenly_essence_bone", () ->
            new EvolvingPhysique(Component.translatable("ascension.physiques.heavenly_essence_bone"))
                    .addEvolution(ModPhysiques.ESSENCE_BONE_DIVINE.getId())
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(), 4.0)
                    .setDescription(Component.translatable("ascension.physiques.heavenly_essence_bone.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.heavenly_essence_bone.desc.short"))
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> ESSENCE_BONE_DIVINE = PHYSIQUES.register("divine_essence_bone", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.divine_essence_bone"))
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(), 5.0)
                    .setDescription(Component.translatable("ascension.physiques.divine_essence_bone.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.divine_essence_bone.desc.short"))
    );


    // --- Elemental Body Physiques (tier 1; fuse in-place to upgrade) ---
    public static final DeferredHolder<IPhysique, ? extends ElementalBodyPhysique> FIRE_BODY = PHYSIQUES.register("fire_body", () ->
            new ElementalBodyPhysique(ModPaths.FIRE.getId(), Component.translatable("ascension.physiques.fire_body")));

    public static final DeferredHolder<IPhysique, ? extends ElementalBodyPhysique> WATER_BODY = PHYSIQUES.register("water_body", () ->
            new ElementalBodyPhysique(ModPaths.WATER.getId(), Component.translatable("ascension.physiques.water_body")));

    public static final DeferredHolder<IPhysique, ? extends ElementalBodyPhysique> WOOD_BODY = PHYSIQUES.register("wood_body", () ->
            new ElementalBodyPhysique(ModPaths.WOOD.getId(), Component.translatable("ascension.physiques.wood_body")));

    public static final DeferredHolder<IPhysique, ? extends ElementalBodyPhysique> EARTH_BODY = PHYSIQUES.register("earth_body", () ->
            new ElementalBodyPhysique(ModPaths.EARTH.getId(), Component.translatable("ascension.physiques.earth_body")));

    public static final DeferredHolder<IPhysique, ? extends ElementalBodyPhysique> METAL_BODY = PHYSIQUES.register("metal_body", () ->
            new ElementalBodyPhysique(ModPaths.METAL.getId(), Component.translatable("ascension.physiques.metal_body")));

    // Body Physiques

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> TYRANT_BODY = PHYSIQUES.register("tyrant_body", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.tyrant_body"))
                    .addPath(ModPaths.BODY.getId())
                    .addPathBonus(ModPaths.BODY.getId(), 1.2)
                    .addPath(ModPaths.FIST.getId())
                    .addPathBonus(ModPaths.FIST.getId(), 2.2)
                    .setDescription(Component.translatable("ascension.physiques.tyrant_body.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.tyrant_body.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> STONE_MONKEY = PHYSIQUES.register("stone_monkey", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.stone_monkey"))
                    .addPath(ModPaths.BODY.getId())
                    .addPath(ModPaths.EARTH.getId())
                    .addPathBonus(ModPaths.BODY.getId(), 1.75)
                    .addPathBonus(ModPaths.EARTH.getId(), 3.0)
                    .setShortDescription(Component.translatable("ascension.physiques.stone_monkey.desc.short"))
                    .setDescription(Component.translatable("ascension.physiques.stone_monkey.desc"))
    );


    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> WORLD_DOMINATOR =
            PHYSIQUES.register("world_dominator", () ->
                    new GenericPhysique(Component.translatable("ascension.physiques.world_dominator")) {
                        @Override
                        public void onPhysiqueAdded(IEntityData heldEntity, ResourceLocation oldPhysique, IPhysiqueData oldPhysiqueData) {
                            super.onPhysiqueAdded(heldEntity, oldPhysique, oldPhysiqueData);
                            broadcastRareAcquired(heldEntity, "ascension.message.physique.world_dominator.acquired");}
            }
            .addPath(ModPaths.BODY.getId()).addPathBonus(ModPaths.BODY.getId(), 5.0)
                            .setDescription(Component.translatable("ascension.physiques.world_dominator.desc"))
            );


    // Soul Physiques

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> ACADEMIC_SPIRIT = PHYSIQUES.register("academic_spirit", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.academic_spirit"))
                    .addPath(ModPaths.SOUL.getId())
                    .addPathBonus(ModPaths.SOUL.getId(), 2.0)
                    .setDescription(Component.translatable("ascension.physiques.academic_spirit.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.academic_spirit.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> CLEAR_SPIRIT = PHYSIQUES.register("clear_spirit", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.clear_spirit"))
                    .addPath(ModPaths.SOUL.getId())
                    .addPathBonus(ModPaths.SOUL.getId(), 1.25)
                    .setDescription(Component.translatable("ascension.physiques.clear_spirit.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.clear_spirit.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> DREAMING_SOUL = PHYSIQUES.register("dreaming_soul", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.dreaming_soul"))
                    .addPath(ModPaths.SOUL.getId())
                    .addPathBonus(ModPaths.SOUL.getId(), 1.65)
                    .setDescription(Component.translatable("ascension.physiques.dreaming_soul.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.dreaming_soul.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> SOUL_GAZE = PHYSIQUES.register("soul_gaze", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.soul_gaze"))
                    .addPath(ModPaths.SOUL.getId())
                    .addPathBonus(ModPaths.SOUL.getId(), 1.35)
                    .setDescription(Component.translatable("ascension.physiques.soul_gaze.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.soul_gaze.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> ASHEN_SOUL_FLAME = PHYSIQUES.register("ashen_soul_flame", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.ashen_soul_flame"))
                    .addPath(ModPaths.SOUL.getId())
                    .addPath(ModPaths.FIRE.getId())
                    .addPathBonus(ModPaths.SOUL.getId(), 2.5)
                    .addPathBonus(ModPaths.FIRE.getId(), 1.75)
                    .setDescription(Component.translatable("ascension.physiques.ashen_soul_flame.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.ashen_soul_flame.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> THUNDERING_SOUL_CORE = PHYSIQUES.register("thundering_soul_core", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.thundering_soul_core"))
                    .addPath(ModPaths.SOUL.getId())
                    .addPath(ModPaths.LIGHTNING.getId())
                    .addPathBonus(ModPaths.SOUL.getId(), 3.0)
                    .addPathBonus(ModPaths.LIGHTNING.getId(), 2.25)
                    .setDescription(Component.translatable("ascension.physiques.thundering_soul_core.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.thundering_soul_core.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> SOUL_SWORD_HEART = PHYSIQUES.register("soul_sword_heart", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.soul_sword_heart"))
                    .addPath(ModPaths.SOUL.getId())
                    .addPath(ModPaths.SWORD.getId())
                    .addPathBonus(ModPaths.SOUL.getId(), 3.0)
                    .addPathBonus(ModPaths.SWORD.getId(), 2.0)
                    .setDescription(Component.translatable("ascension.physiques.soul_sword_heart.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.soul_sword_heart.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> SPEAR_SOUL_MARK = PHYSIQUES.register("spear_soul_mark", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.spear_soul_mark"))
                    .addPath(ModPaths.SOUL.getId())
                    .addPath(ModPaths.SPEAR.getId())
                    .addPathBonus(ModPaths.SOUL.getId(), 2.75)
                    .addPathBonus(ModPaths.SPEAR.getId(), 1.75)
                    .setDescription(Component.translatable("ascension.physiques.spear_soul_mark.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.spear_soul_mark.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> SOUL_CROWNED_KING =
            PHYSIQUES.register("soul_crowned_mind", () ->
                    new GenericPhysique(Component.translatable("ascension.physiques.soul_crowned_king")) {
                        @Override
                        public void onPhysiqueAdded(IEntityData heldEntity, ResourceLocation oldPhysique, IPhysiqueData oldPhysiqueData) {
                            super.onPhysiqueAdded(heldEntity, oldPhysique, oldPhysiqueData);
                            broadcastRareAcquired(heldEntity, "ascension.message.physique.soul_crowned_king.acquired");
                        }
                    }.addPath(ModPaths.SOUL.getId()).addPathBonus(ModPaths.SOUL.getId(), 5.0)
                            .setDescription(Component.translatable("ascension.physiques.soul_crowned_king.desc"))
            );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> YIN_YANG_EYES = PHYSIQUES.register("yin_yang_eyes", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.yin_yang_eyes"))
                    .addPath(ModPaths.SOUL.getId())
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.SOUL.getId(), 3.0)
                    .addPathBonus(ModPaths.ESSENCE.getId(), 1.25)
                    .setDescription(Component.translatable("ascension.physiques.yin_yang_eyes.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.yin_yang_eyes.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> YIN_EYES = PHYSIQUES.register("yin_eyes", () ->
            new EvolvingPhysique(Component.translatable("ascension.physiques.yin_eyes"))
                    .addEvolution(ModPhysiques.YIN_YANG_EYES.getId())
                    .addPath(ModPaths.SOUL.getId())
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.SOUL.getId(), 1.0)
                    .addPathBonus(ModPaths.ESSENCE.getId(), 0.05)
                    .setDescription(Component.translatable("ascension.physiques.yin_eyes.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.yin_eyes.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> YANG_EYES = PHYSIQUES.register("yang_eyes", () ->
            new EvolvingPhysique(Component.translatable("ascension.physiques.yang_eyes"))
                    .addEvolution(ModPhysiques.YIN_YANG_EYES.getId())
                    .addPath(ModPaths.SOUL.getId())
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.SOUL.getId(), 1.0)
                    .addPathBonus(ModPaths.ESSENCE.getId(), 0.05)
                    .setDescription(Component.translatable("ascension.physiques.yang_eyes.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.yang_eyes.desc.short"))
    );


    // Weapon Physiques
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> SWORD_BONE = PHYSIQUES.register("sword_bone", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.sword_bone"))
                    .addPath(ModPaths.BODY.getId())
                    .addPathBonus(ModPaths.BODY.getId(), 0.5)
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.ESSENCE.getId(), 0.5)
                    .addPath(ModPaths.SWORD.getId())
                    .addPathBonus(ModPaths.SWORD.getId(), 2.0)
                    .setDescription(Component.translatable("ascension.physiques.sword_bone.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.sword_bone.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> FLOW_SEVERING_EYES = PHYSIQUES.register("flow_severing_eyes", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.flow_severing_eyes"))
                    .addPath(ModPaths.SWORD.getId())
                    .addPath(ModPaths.BLADE.getId())
                    .addPath(ModPaths.AXE.getId())
                    .addPath(ModPaths.SPEAR.getId())
                    .addPathBonus(ModPaths.SWORD.getId(), 2.0)
                    .addPathBonus(ModPaths.BLADE.getId(), 2.0)
                    .addPathBonus(ModPaths.AXE.getId(), 2.0)
                    .addPathBonus(ModPaths.SPEAR.getId(), 2.0)
                    .setDescription(Component.translatable("ascension.physiques.flow_severing_eyes.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.flow_severing_eyes.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> THIN_SWORD_PULSE = PHYSIQUES.register("thin_sword_pulse", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.thin_sword_pulse"))
                    .addPath(ModPaths.SWORD.getId())
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.SWORD.getId(), 1.5)
                    .addPathBonus(ModPaths.ESSENCE.getId(), 1.25)
                    .setDescription(Component.translatable("ascension.physiques.thin_sword_pulse.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.thin_sword_pulse.desc.short"))
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> WILD_CLEAVER_VETERAN = PHYSIQUES.register("wild_cleaver_veteran", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.wild_cleaver_veteran"))
                    .addPath(ModPaths.BLADE.getId())
                    .addPath(ModPaths.BODY.getId())
                    .addPathBonus(ModPaths.BLADE.getId(), 1.5)
                    .addPathBonus(ModPaths.BODY.getId(), 1.25)
                    .setDescription(Component.translatable("ascension.physiques.wild_cleaver_veteran.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.wild_cleaver_veteran.desc.short"))
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> ARROW_BLESSED = PHYSIQUES.register("arrow_blessed", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.arrow_blessed"))
                    .addPath(ModPaths.BOW.getId())
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.BOW.getId(), 1.5)
                    .addPathBonus(ModPaths.ESSENCE.getId(), 1.25)
                    .setDescription(Component.translatable("ascension.physiques.arrow_blessed.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.arrow_blessed.desc.short"))
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> IRON_BULWARK_SPINE = PHYSIQUES.register("iron_bulwark_spine", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.iron_bulwark_spine"))
                    .addPath(ModPaths.SHIELD.getId())
                    .addPath(ModPaths.BODY.getId())
                    .addPathBonus(ModPaths.SHIELD.getId(), 1.5)
                    .addPathBonus(ModPaths.BODY.getId(), 1.25)
                    .setDescription(Component.translatable("ascension.physiques.iron_bulwark_spine.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.iron_bulwark_spine.desc.short"))
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> BRUISED_KNUCKLE_BODY = PHYSIQUES.register("bruised_knuckle_body", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.bruised_knuckle_body"))
                    .addPath(ModPaths.FIST.getId())
                    .addPath(ModPaths.BODY.getId())
                    .addPathBonus(ModPaths.FIST.getId(), 1.5)
                    .addPathBonus(ModPaths.BODY.getId(), 1.25)
                    .setDescription(Component.translatable("ascension.physiques.bruised_knuckle_body.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.bruised_knuckle_body.desc.short"))
    );
    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> POINTED_EYES = PHYSIQUES.register("pointed_eyes", () ->
            new GenericPhysique(Component.translatable("ascension.physiques.pointed_eyes"))
                    .addPath(ModPaths.SPEAR.getId())
                    .addPath(ModPaths.ESSENCE.getId())
                    .addPathBonus(ModPaths.SPEAR.getId(), 1.5)
                    .addPathBonus(ModPaths.ESSENCE.getId(), 1.25)
                    .setDescription(Component.translatable("ascension.physiques.pointed_eyes.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.pointed_eyes.desc.short"))
    );

    // Other Physiques

    public static final DeferredHolder<IPhysique,? extends GenericPhysique> BLESSED = PHYSIQUES.register("blessed",()->
            new EvolvingPhysique(Component.translatable("ascension.physiques.blessed"))
                    .addEvolution(ModPhysiques.VIRTUOSO_BUDDHA.getId())
                    .addEvolution(ModPhysiques.WRATHFUL_VAJRA.getId())
                    .addPath(ModPaths.VIRTUOUS.getId())
                    .addPath(ModPaths.BODY.getId())
                    .addPathBonus(ModPaths.VIRTUOUS.getId(), 1.5)
                    .addPathBonus(ModPaths.BODY.getId(), 1.0)
                    .setDescription(Component.translatable("ascension.physiques.blessed.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.blessed.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> VIRTUOSO_BUDDHA = PHYSIQUES.register("virtuoso_buddha",()->
            new GenericPhysique(Component.translatable("ascension.physiques.virtuoso_buddha"))
                    .addPath(ModPaths.BODY.getId())
                    .addPath(ModPaths.BUDDHIST.getId())
                    .addPath(ModPaths.VIRTUOUS.getId())
                    .addPathBonus(ModPaths.BODY.getId(), 3.0)
                    .addPathBonus(ModPaths.BUDDHIST.getId(), 2.0)
                    .addPathBonus(ModPaths.VIRTUOUS.getId(), 1.0)
                    .setDescription(Component.translatable("ascension.physiques.virtuoso_buddha.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.virtuoso_buddha.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> WRATHFUL_VAJRA = PHYSIQUES.register("wrathful_vajra",()->
            new GenericPhysique(Component.translatable("ascension.physiques.wrathful_vajra"))
                    .addPath(ModPaths.BODY.getId())
                    .addPath(ModPaths.BUDDHIST.getId())
                    .addPathBonus(ModPaths.BODY.getId(), 3.5)
                    .addPathBonus(ModPaths.BUDDHIST.getId(), 2.0)
                    .setDescription(Component.translatable("ascension.physiques.wrathful_vajra.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.wrathful_vajra.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> BLOOD_FIEND = PHYSIQUES.register("blood_fiend",()->
            new EvolvingPhysique(Component.translatable("ascension.physiques.blood_fiend"))
                    .addEvolution(ModPhysiques.BLOOD_WRAITH.getId())
                    .addPath(ModPaths.SOUL.getId())
                    .addPath(ModPaths.DEMONIC.getId())
                    .addPathBonus(ModPaths.SOUL.getId(),1.0)
                    .addPathBonus(ModPaths.DEMONIC.getId(),2.0)
                    .setDescription(Component.translatable("ascension.physiques.blood_fiend.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.blood_fiend.desc.short"))
    );

    public static final DeferredHolder<IPhysique, ? extends GenericPhysique> BLOOD_WRAITH = PHYSIQUES.register("blood_wraith",()->
            new GenericPhysique(Component.translatable("ascension.physiques.blood_wraith"))
                    .addPath(ModPaths.SOUL.getId())
                    .addPath(ModPaths.DEMONIC.getId())
                    .addPathBonus(ModPaths.SOUL.getId(),2.0)
                    .addPathBonus(ModPaths.DEMONIC.getId(),4.0)
                    .setDescription(Component.translatable("ascension.physiques.blood_wraith.desc"))
                    .setShortDescription(Component.translatable("ascension.physiques.blood_wraith.desc.short"))
    );





    public static void register(IEventBus modEventBus){
        PHYSIQUES.register(modEventBus);
    }

}
