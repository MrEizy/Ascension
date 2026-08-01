package net.zic.ascension.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.path.PathEffectValueUtil;

public class AscensionDamageUtil {
    public static final Identifier DAMAGE_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"damage");
    public static final Identifier RESISTANCE_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"resistance");
    public static double getEffectiveAttackerAffinity(double baseAffinity, LivingEntity attacker,Identifier path){
        return PathEffectValueUtil.getEffectiveAffinity(attacker,baseAffinity,path);
    }
    //apply the defending entities affinities against the attacking entity affinity
    public static double getFinalAttackerAffinity(double affinity,LivingEntity target,Identifier path){
        return PathEffectValueUtil.getEffectiveAffinity(target,affinity,path,true);
    }


    public static double getEffectiveDefenderAffinity(double baseAffinity,LivingEntity defender,Identifier path){
        return PathEffectValueUtil.getEffectiveAffinity(defender,baseAffinity,path);
    }

    public static double getFinalAffinity(double effectiveAttackerAffinityMultiplier, double effectiveDefenderAffinityMultiplier) {
        return Math.clamp(effectiveAttackerAffinityMultiplier-effectiveDefenderAffinityMultiplier,0,effectiveAttackerAffinityMultiplier);
    }


}