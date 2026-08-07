package net.zic.ascension.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathEffectValueUtil;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;


import java.util.List;

public class CultivationUtil {
    public static final Identifier CULTIVATION_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"cultivation");
    public static final Identifier FOUNDATION_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"foundation");

    /**

     * TODO update canBreakthrough to tryBreakthrough, that way you can safely assume it is safe to trigger things like tribulations from it
     * TODO update to use effect Value
     * @param caster
     * @param source
     * @param pathInstance
     * @param secondaryPath the path used to cultivate
     * @param baseRate
     */
    public static void cultivate(LivingEntity caster, OriginSource source,Identifier path, PathInstance pathInstance, Identifier secondaryPath, double baseRate){



        double cultivationAmount = baseRate*(1+PathEffectValueUtil.getEffectiveAffinity(caster,path));

        if(!path.equals(secondaryPath) && secondaryPath != null)cultivationAmount += baseRate*(1+PathEffectValueUtil.getEffectiveAffinity(caster,secondaryPath));


        pathInstance.progressPath(secondaryPath,cultivationAmount,source,caster);

        AscensionOriginSourceHelper.markPathDirty(source,path);
    }

}
