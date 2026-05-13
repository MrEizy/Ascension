package net.thejadeproject.ascension.refactor_packages.paths;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.paths.custom.ComprehensionPath;
import net.thejadeproject.ascension.refactor_packages.paths.custom.GenericPath;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

public class ModPaths {
    public static final DeferredRegister<IPath> PATHS =DeferredRegister.create(AscensionRegistries.Paths.PATHS_REGISTRY, AscensionCraft.MOD_ID);


    /*
    Paths are the foundation of the Cultivation system. It is what allows cultivation and makes it possible.
    To make a new path just copy existing one and change the
    "essence" -> "Your choice" and also change the .addMajorRealmName("ascension.path.essence.mortal") to be
    .addMajorRealmName("ascension.path.your_choice.mortal") and so on.
     */

    /*
    You might have noticed that there are 2 different paths
    GenericPath and ComprehensionPath.

    GenericPath is the 3 Main Paths so Essence, Body and Soul. These are what we like to call Main Paths It is what all physiques
    Should atleast have 1 of these.

    ComprehensionPath is like the Myriad of dao. This would include stuff Like Flame Dao, Space, etc all sub type paths to
    cultivation. ComprehensionPath is where you really get do design your own stuff!
     */


    // Central Paths
    public static final DeferredHolder<IPath, ? extends GenericPath> ESSENCE = PATHS.register("essence",()->
            new GenericPath(Component.translatable("ascension.path.essence"))
                    .addMajorRealmName("ascension.path.essence.mortal")
                    .addMajorRealmName("ascension.path.essence.qi_condensation")
                    .addMajorRealmName("ascension.path.essence.formation_establishment")
                    .addMajorRealmName("ascension.path.essence.golden_core")
                    .addMajorRealmName("ascension.path.essence.nascent_core")
            );
    public static final DeferredHolder<IPath, ? extends GenericPath> BODY = PATHS.register("body",()->
            new GenericPath(Component.translatable("ascension.path.body"))
                    .addMajorRealmName("ascension.path.body.mortal")
                    .addMajorRealmName("ascension.path.body.skin_tempering")
                    .addMajorRealmName("ascension.path.body.sinew_weaving")
                    .addMajorRealmName("ascension.path.body.bone_forging")
                    .addMajorRealmName("ascension.path.body.heart_kindling")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> SOUL = PATHS.register("soul",()->
            new GenericPath(Component.translatable("ascension.path.soul"))
                    .addMajorRealmName("ascension.path.soul.mortal")
                    .addMajorRealmName("ascension.path.soul.battle_soul")
                    .addMajorRealmName("ascension.path.soul.azure_soul")
                    .addMajorRealmName("ascension.path.soul.silver_soul")
                    .addMajorRealmName("ascension.path.soul.gold_battle_soul")
    );


    // 5 Elements Paths
    public static final DeferredHolder<IPath, ? extends GenericPath> FIRE = PATHS.register("fire",()->
            new ComprehensionPath(Component.translatable("ascension.path.fire"))
                    .addMajorRealmName("ascension.path.fire.kindling")
                    .addMajorRealmName("ascension.path.fire.ignition")
                    .addMajorRealmName("ascension.path.fire.true_flame")
                    .addMajorRealmName("ascension.path.fire.origin_flame")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> WATER = PATHS.register("water",()->
            new ComprehensionPath(Component.translatable("ascension.path.water"))
                    .addMajorRealmName("ascension.path.water.tidepool")
                    .addMajorRealmName("ascension.path.water.current")
                    .addMajorRealmName("ascension.path.water.true_water")
                    .addMajorRealmName("ascension.path.water.origin_water")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> WOOD = PATHS.register("wood",()->
            new ComprehensionPath(Component.translatable("ascension.path.wood"))
                    .addMajorRealmName("ascension.path.wood.sprout")
                    .addMajorRealmName("ascension.path.wood.growth")
                    .addMajorRealmName("ascension.path.wood.true_wood")
                    .addMajorRealmName("ascension.path.wood.origin_wood")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> EARTH = PATHS.register("earth",()->
            new ComprehensionPath(Component.translatable("ascension.path.earth"))
                    .addMajorRealmName("ascension.path.earth.pebble")
                    .addMajorRealmName("ascension.path.earth.stone")
                    .addMajorRealmName("ascension.path.earth.true_earth")
                    .addMajorRealmName("ascension.path.earth.origin_earth")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> METAL = PATHS.register("metal",()->
            new ComprehensionPath(Component.translatable("ascension.path.metal"))
                    .addMajorRealmName("ascension.path.metal.ore")
                    .addMajorRealmName("ascension.path.metal.ingot")
                    .addMajorRealmName("ascension.path.metal.true_metal")
                    .addMajorRealmName("ascension.path.metal.origin_metal")
    );

    //Deviated Elements Paths
    public static final DeferredHolder<IPath, ? extends GenericPath> LIGHTNING = PATHS.register("lightning",()->
            new ComprehensionPath(Component.translatable("ascension.path.lightning"))
                    .addMajorRealmName("ascension.path.lightning.spark")
                    .addMajorRealmName("ascension.path.lightning.lesser_lightning")
                    .addMajorRealmName("ascension.path.lightning.true_lightning")
                    .addMajorRealmName("ascension.path.lightning.origin_lightning")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> WIND = PATHS.register("wind",()->
            new ComprehensionPath(Component.translatable("ascension.path.wind"))
                    .addMajorRealmName("ascension.path.wind.gust")
                    .addMajorRealmName("ascension.path.wind.lesser_breeze")
                    .addMajorRealmName("ascension.path.wind.true_wind")
                    .addMajorRealmName("ascension.path.wind.origin_wind")
    );


    // Weapon Paths
    public static final DeferredHolder<IPath, ? extends GenericPath> SWORD = PATHS.register("sword",()->
            new ComprehensionPath(Component.translatable("ascension.path.sword"))
                    .addMajorRealmName("ascension.path.sword.initiate")
                    .addMajorRealmName("ascension.path.sword.intent")
                    .addMajorRealmName("ascension.path.sword.aura")
                    .addMajorRealmName("ascension.path.sword.unity")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> AXE = PATHS.register("axe",()->
            new ComprehensionPath(Component.translatable("ascension.path.axe"))
                    .addMajorRealmName("ascension.path.axe.initiate")
                    .addMajorRealmName("ascension.path.axe.intent")
                    .addMajorRealmName("ascension.path.axe.aura")
                    .addMajorRealmName("ascension.path.axe.unity")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> BLADE = PATHS.register("blade",()->
            new ComprehensionPath(Component.translatable("ascension.path.blade"))
                    .addMajorRealmName("ascension.path.blade.initiate")
                    .addMajorRealmName("ascension.path.blade.intent")
                    .addMajorRealmName("ascension.path.blade.aura")
                    .addMajorRealmName("ascension.path.blade.unity")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> SPEAR = PATHS.register("spear",()->
            new ComprehensionPath(Component.translatable("ascension.path.spear"))
                    .addMajorRealmName("ascension.path.spear.initiate")
                    .addMajorRealmName("ascension.path.spear.intent")
                    .addMajorRealmName("ascension.path.spear.aura")
                    .addMajorRealmName("ascension.path.spear.unity")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> BOW = PATHS.register("bow",()->
            new ComprehensionPath(Component.translatable("ascension.path.bow"))
                    .addMajorRealmName("ascension.path.bow.initiate")
                    .addMajorRealmName("ascension.path.bow.intent")
                    .addMajorRealmName("ascension.path.bow.aura")
                    .addMajorRealmName("ascension.path.bow.unity")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> TRIDENT = PATHS.register("trident",()->
            new ComprehensionPath(Component.translatable("ascension.path.trident"))
                    .addMajorRealmName("ascension.path.trident.initiate")
                    .addMajorRealmName("ascension.path.trident.intent")
                    .addMajorRealmName("ascension.path.trident.aura")
                    .addMajorRealmName("ascension.path.trident.unity")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> MACE = PATHS.register("mace",()->
            new ComprehensionPath(Component.translatable("ascension.path.mace"))
                    .addMajorRealmName("ascension.path.mace.initiate")
                    .addMajorRealmName("ascension.path.mace.intent")
                    .addMajorRealmName("ascension.path.mace.aura")
                    .addMajorRealmName("ascension.path.mace.unity")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> SHIELD = PATHS.register("shield",()->
            new ComprehensionPath(Component.translatable("ascension.path.shield"))
                    .addMajorRealmName("ascension.path.shield.initiate")
                    .addMajorRealmName("ascension.path.shield.intent")
                    .addMajorRealmName("ascension.path.shield.aura")
                    .addMajorRealmName("ascension.path.shield.unity")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> FIST = PATHS.register("fist",()->
            new ComprehensionPath(Component.translatable("ascension.path.fist"))
                    .addMajorRealmName("ascension.path.fist.initiate")
                    .addMajorRealmName("ascension.path.fist.intent")
                    .addMajorRealmName("ascension.path.fist.aura")
                    .addMajorRealmName("ascension.path.fist.unity")
    );


    // Other
    public static final DeferredHolder<IPath, ? extends GenericPath> BUDDHIST = PATHS.register("buddhist",()->
            new ComprehensionPath(Component.translatable("ascension.path.buddhist"))
                    .addMajorRealmName("ascension.path.buddhist.sramana")
                    .addMajorRealmName("ascension.path.buddhist.arhat")
                    .addMajorRealmName("ascension.path.buddhist.bodhisattva")
                    .addMajorRealmName("ascension.path.buddhist.tathagata")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> DEMONIC = PATHS.register("demonic",()->
            new ComprehensionPath(Component.translatable("ascension.path.demonic"))
                    .addMajorRealmName("ascension.path.demonic.seed")
                    .addMajorRealmName("ascension.path.demonic.heart")
                    .addMajorRealmName("ascension.path.demonic.demon")
                    .addMajorRealmName("ascension.path.demonic.demonic")
    );
    public static final DeferredHolder<IPath, ? extends GenericPath> VIRTUOUS = PATHS.register("virtuous",()->
            new ComprehensionPath(Component.translatable("ascension.path.virtuous"))
                    .addMajorRealmName("ascension.path.virtuous.self")
                    .addMajorRealmName("ascension.path.virtuous.family")
                    .addMajorRealmName("ascension.path.virtuous.state")
                    .addMajorRealmName("ascension.path.virtuous.universal")
    );
    public static void register(IEventBus modEventBus){
        PATHS.register(modEventBus);
    }
}
