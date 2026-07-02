package net.zic.ascension.api.core.path;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.api.core.path.affinity.AffinityCategoryHolder;
import net.zic.ascension.api.core.path.affinity.AffinityHolder;
import net.zic.ascension.api.core.path.interactions.PathInteraction;
import net.zic.ascension.api.core.path.interactions.PathInteractionType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * Holds all the methods needed to meaningfully handle the conversion of context based path effects(e.g affinity, atmospheric dao and dao damage bonus/resistance)
 * into a universal modifier that can be applied to anything
 */
public class PathEffectValueUtil {
    public static final Identifier NO_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"none");

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


    //TODO note in the future you will be able to get an affinity holder from entityData directly which also holds a reference to source affinity
    //TODO figure out how this will work with categories
    public static double getEffectValue(double initialAffinity,AffinityHolder holder,Identifier path, Identifier category){
        double affinity = initialAffinity;
        HashSet<Identifier> paths = new HashSet<>(holder.getPaths());
        if(category != null) paths.addAll(holder.getPaths(category));

        if(affinity == 0) return 0;


        Collection<PathInteraction> pathInteractions = AscensionCraft.getPathInteractionHolder().getTargetInteractionsFrom(
                path,
                paths
        );
        for(PathInteraction interaction :pathInteractions){
            Identifier sourcePath = interaction.pathA();

            if(!holder.hasAffinity(sourcePath) && !holder.hasAffinity(category,sourcePath)) continue;

            double sourceAffinity = holder.getAffinity(sourcePath)+
                    (holder.hasAffinity(category,sourcePath) ? holder.getAffinity(category,sourcePath) :0);

            if(interaction.type() == PathInteractionType.RELATED){
                affinity += sourceAffinity*interaction.value();
            }
        }
        for(PathInteraction interaction :pathInteractions){
            Identifier sourcePath = interaction.pathA();

            if(!holder.hasAffinity(sourcePath) && !holder.hasAffinity(category,sourcePath)) continue;

            double sourceAffinity = holder.getAffinity(sourcePath)+
                    (holder.hasAffinity(category,sourcePath) ? holder.getAffinity(category,sourcePath) :0);

            double x = (1+sourceAffinity/affinity*interaction.value());
            if(interaction.type() == PathInteractionType.DESTRUCTIVE) {
                affinity *= (1/x);
            }else{
                affinity *= x;
            }
        }



        return affinity;
    }


    public static double getEffectValue(double initialAffinity,LivingEntity affinitySource,Identifier path,Identifier category){
        double affinity = initialAffinity;
        AscensionEntityDataHolder holder = affinitySource.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
        if(holder == null) return initialAffinity;

        AscensionEntityData entityData = holder.getData(affinitySource);
        if(entityData == null) return initialAffinity;

        HashSet<Identifier> paths = new HashSet<>(entityData.getAllAffinities());
        HashSet<Identifier> categorizedPaths = new HashSet<>(entityData.getAllAffinities(category));
        if(category != null) paths.addAll(categorizedPaths);




        Collection<PathInteraction> pathInteractions = AscensionCraft.getPathInteractionHolder().getTargetInteractionsFrom(
                path,
                paths
        );

        //We apply related first, then handel destructive and geneartive since their multipler depends on the final base affinity

        for(PathInteraction interaction :pathInteractions){
            Identifier sourcePath = interaction.pathA();

            if(!entityData.hasAffinity(sourcePath) && !entityData.hasAffinity(category,sourcePath)) continue;

            double sourceAffinity = entityData.getAffinity(sourcePath)+
                    (entityData.hasAffinity(category,sourcePath) ? entityData.getAffinity(category,sourcePath) :0);

            if(interaction.type() == PathInteractionType.RELATED){
                affinity += sourceAffinity*interaction.value();
            }
        }
        for(PathInteraction interaction :pathInteractions){
            Identifier sourcePath = interaction.pathA();

            if(!entityData.hasAffinity(sourcePath) && !entityData.hasAffinity(category,sourcePath)) continue;

            double sourceAffinity = entityData.getAffinity(sourcePath)+
                    (entityData.hasAffinity(category,sourcePath) ? entityData.getAffinity(category,sourcePath) :0);

            double x = (1+sourceAffinity/affinity*interaction.value());
            if(interaction.type() == PathInteractionType.DESTRUCTIVE) {
                affinity *= (1/x);
            }else if (interaction.type() == PathInteractionType.GENERATIVE){
                affinity *= x;
            }
        }



        return affinity;

    }
}
