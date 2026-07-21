package net.zic.ascension.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.path.PathEffectValueUtil;

public class AscensionDamageUtil {
    public static final Identifier DAMAGE_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"damage");
    public static final Identifier RESISTANCE_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"resistance");
    public static double getEffectiveAttackerAffinity(double baseAffinity, LivingEntity attacker,Identifier path){
        return PathEffectValueUtil.getEffectValue(baseAffinity,attacker,path,DAMAGE_CATEGORY);
    }
    //apply the defending entities affinities against the attacking entity affinity
    public static double getFinalAttackerAffinity(double affinity,LivingEntity target,Identifier path){
        return PathEffectValueUtil.getEffectValue(affinity,target,path,PathEffectValueUtil.NO_CATEGORY);
    }


    public static double getEffectiveDefenderAffinity(double baseAffinity,LivingEntity defender,Identifier path){
        return PathEffectValueUtil.getEffectValue(baseAffinity,defender,path,RESISTANCE_CATEGORY);
    }

    public static double getFinalAffinity(double effectiveAttackerAffinityMultiplier, double effectiveDefenderAffinityMultiplier) {
        return Math.clamp(effectiveAttackerAffinityMultiplier-effectiveDefenderAffinityMultiplier,0,effectiveAttackerAffinityMultiplier);
    }

    public static double getDamage(double damage,double effectiveAttackerAffinityMultiplier, double effectiveDefenderAffinityMultiplier){
        return damage * (1+getFinalAffinity(effectiveAttackerAffinityMultiplier,effectiveDefenderAffinityMultiplier));
    }
}