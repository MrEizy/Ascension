package net.zic.ascension.api.ascension.core.path;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityPathBonusHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.ascension.api.ascension.core.path.interactions.PathInteraction;
import net.zic.ascension.api.ascension.core.path.interactions.PathInteractionType;

import java.util.Collection;
import java.util.HashSet;

/**
 * Holds all the methods needed to meaningfully handle the conversion of context based path effects(e.g affinity, atmospheric dao and dao damage bonus/resistance)
 * into a universal modifier that can be applied to anything
 */
public class PathEffectValueUtil {
    public static final Identifier NO_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"none");
    public static final Identifier AFFINITY_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"affinity");
    /*
        TODO consider making categories a registry, and then each category would have a name/description
        +mask for if it applies to generative, destructive or related path
        e.g if i use resistance category for fire, should water also account for resistance, most likely not
     */

    //TODO consider how damage resistance works
    /*
        say i attack with a fireball and the target has a high water affinity and a high fire affinity, which should be used to "resist" the fireball
        I cannot just use their fire affinity since water lowers it

        potentially i get the total affinity of the target, and use that as the "source" affinity that gets modified by the target?

        so if they attack me with fire my wood boosts its damage and water reduces it

        then as a separate modifier my OWN fire affinity further applies a damage reduction effect (formula TBD)


        TODO these calculations might get expensive, so consider setting up a cache of sorts (at least for internal values)


        TODO when using a path in these calculations I do not consider that paths own relations, only using it's base value(to prevent recursive problems)

        TODO therefore path interatcions only affect the path DIRECTLY being used/referenced
     */

    public static double getEffectiveAffinity(PathBonusProvider provider,double initialAffinity,Identifier path,boolean ignoreRelated){
        double affinity = initialAffinity;
        Collection<Identifier> availablePaths = provider.getAllPathBonusesInCategory(AFFINITY_CATEGORY);

        Collection<PathInteraction> pathInteractions = AscensionCraft.getPathInteractionHolder().getTargetInteractionsFrom(
                path,
                availablePaths
        );

        if(!ignoreRelated) {
            for (PathInteraction interaction : pathInteractions) {
                Identifier sourcePath = interaction.pathA();

                double sourceAffinity = provider.getPathBonus(AFFINITY_CATEGORY, sourcePath);


                if (interaction.type() == PathInteractionType.RELATED) {
                    affinity += sourceAffinity * interaction.value();
                }
            }
        }
        if(affinity == 0) return 0;

        double finalAffinity = affinity;
        for(PathInteraction interaction :pathInteractions){

            if (interaction.value() == 0) continue;

            Identifier sourcePath = interaction.pathA();
            double sourceAffinity = provider.getPathBonus(AFFINITY_CATEGORY,sourcePath);


            double x = (1+sourceAffinity/affinity*interaction.value());
            if(interaction.type() == PathInteractionType.DESTRUCTIVE) {
                finalAffinity *= (1/x);
            }else if(interaction.type() == PathInteractionType.GENERATIVE){
                finalAffinity *= x;
            }
        }



        return affinity;
    }
    public static double getEffectiveAffinity(LivingEntity affinitySource,double initialAffinity,Identifier path,boolean ignoreRelated){
        AscensionEntityPathBonusHolder holder = affinitySource.getData(CoreAttachments.PATH_BONUS_HOLDER);
        return getEffectiveAffinity(holder,initialAffinity,path,false);
    }
    public static double getEffectiveAffinity(LivingEntity affinitySource,double initialAffinity,Identifier path){
        AscensionEntityPathBonusHolder holder = affinitySource.getData(CoreAttachments.PATH_BONUS_HOLDER);
        return getEffectiveAffinity(holder,initialAffinity,path,false);
    }
    public static double getEffectiveAffinity(LivingEntity affinitySource,Identifier path){
        AscensionEntityPathBonusHolder holder = affinitySource.getData(CoreAttachments.PATH_BONUS_HOLDER);
        return getEffectiveAffinity(holder,holder.getPathBonus(AFFINITY_CATEGORY,path),path,false);
    }
}
