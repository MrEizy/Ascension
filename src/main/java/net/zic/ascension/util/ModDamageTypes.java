package net.zic.ascension.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.damagesource.DamageType;
import net.zic.ascension.AscensionCraft;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> PILL_RESIDUE =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pill_residue"));
}
