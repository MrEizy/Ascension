package net.thejadeproject.ascension.particle.aura;

import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;

public record AuraParticleColor(float red, float green, float blue) {

    public static final AuraParticleColor ESSENCE = new AuraParticleColor(0.75f, 0.90f, 1.00f);
    public static final AuraParticleColor BODY = new AuraParticleColor(1.00f, 0.28f, 0.08f);
    public static final AuraParticleColor SOUL = new AuraParticleColor(0.62f, 0.18f, 1.00f);

    public static final AuraParticleColor FIRE = new AuraParticleColor(1.00f, 0.22f, 0.04f);
    public static final AuraParticleColor WATER = new AuraParticleColor(0.18f, 0.55f, 1.00f);
    public static final AuraParticleColor WOOD = new AuraParticleColor(0.20f, 0.82f, 0.28f);
    public static final AuraParticleColor EARTH = new AuraParticleColor(0.58f, 0.38f, 0.14f);
    public static final AuraParticleColor METAL = new AuraParticleColor(0.78f, 0.82f, 0.88f);

    public static final AuraParticleColor LIGHTNING = new AuraParticleColor(0.72f, 0.82f, 1.00f);
    public static final AuraParticleColor WIND = new AuraParticleColor(0.65f, 1.00f, 0.82f);

    public static final AuraParticleColor SWORD = new AuraParticleColor(0.68f, 0.86f, 1.00f);
    public static final AuraParticleColor AXE = new AuraParticleColor(0.95f, 0.42f, 0.18f);
    public static final AuraParticleColor BLADE = new AuraParticleColor(0.80f, 0.10f, 0.18f);
    public static final AuraParticleColor SPEAR = new AuraParticleColor(0.95f, 0.90f, 0.62f);
    public static final AuraParticleColor BOW = new AuraParticleColor(0.35f, 0.85f, 0.45f);
    public static final AuraParticleColor TRIDENT = new AuraParticleColor(0.15f, 0.82f, 0.95f);
    public static final AuraParticleColor MACE = new AuraParticleColor(0.72f, 0.58f, 0.42f);
    public static final AuraParticleColor SHIELD = new AuraParticleColor(0.45f, 0.65f, 1.00f);
    public static final AuraParticleColor FIST = new AuraParticleColor(1.00f, 0.48f, 0.18f);

    public static final AuraParticleColor BUDDHIST = new AuraParticleColor(1.00f, 0.78f, 0.20f);
    public static final AuraParticleColor DEMONIC = new AuraParticleColor(0.55f, 0.02f, 0.08f);
    public static final AuraParticleColor VIRTUOUS = new AuraParticleColor(1.00f, 0.93f, 0.42f);

    public static final AuraParticleColor DEFAULT = ESSENCE;

    public static AuraParticleColor forPath(ResourceLocation pathId) {
        if (pathId == null) return DEFAULT;

        if (pathId.equals(ModPaths.ESSENCE.getId())) return ESSENCE;
        if (pathId.equals(ModPaths.BODY.getId())) return BODY;
        if (pathId.equals(ModPaths.SOUL.getId())) return SOUL;

        if (pathId.equals(ModPaths.FIRE.getId())) return FIRE;
        if (pathId.equals(ModPaths.WATER.getId())) return WATER;
        if (pathId.equals(ModPaths.WOOD.getId())) return WOOD;
        if (pathId.equals(ModPaths.EARTH.getId())) return EARTH;
        if (pathId.equals(ModPaths.METAL.getId())) return METAL;

        if (pathId.equals(ModPaths.LIGHTNING.getId())) return LIGHTNING;
        if (pathId.equals(ModPaths.WIND.getId())) return WIND;

        if (pathId.equals(ModPaths.SWORD.getId())) return SWORD;
        if (pathId.equals(ModPaths.AXE.getId())) return AXE;
        if (pathId.equals(ModPaths.BLADE.getId())) return BLADE;
        if (pathId.equals(ModPaths.SPEAR.getId())) return SPEAR;
        if (pathId.equals(ModPaths.BOW.getId())) return BOW;
        if (pathId.equals(ModPaths.TRIDENT.getId())) return TRIDENT;
        if (pathId.equals(ModPaths.MACE.getId())) return MACE;
        if (pathId.equals(ModPaths.SHIELD.getId())) return SHIELD;
        if (pathId.equals(ModPaths.FIST.getId())) return FIST;

        if (pathId.equals(ModPaths.BUDDHIST.getId())) return BUDDHIST;
        if (pathId.equals(ModPaths.DEMONIC.getId())) return DEMONIC;
        if (pathId.equals(ModPaths.VIRTUOUS.getId())) return VIRTUOUS;

        return DEFAULT;
    }
}