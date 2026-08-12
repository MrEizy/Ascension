package net.zic.ascension.util;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteraction;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteractionType;
import net.zic.ascension.configuration.interactions.PathInteractions;

import java.util.ArrayList;
import java.util.Collection;
//TODO consider enforcing no values < 0
// this is going of the idea is you are not just bad at something without an offset. it is not that you suck at using fire. it is that the presence of water affinity smothers your fire
// what i mean is, you dont have negative affinity with fire, but high affinity with water etc
public class PathInteractionUtil {

    public static final Identifier AFFINITY_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"affinity");

    public static double getEffectivePathMultiplier(LivingEntity entity, Identifier path){
        return 1;
    }

    public static double getEffectivePathMultiplier(PathBonusProvider provider, Identifier path){

        double baseMultiplier = 1+provider.getPathBonus(AFFINITY_CATEGORY,path);

        Collection< PathInteraction> interactions = PathInteractions.getInteractionsForTarget(path);

        double relatedValue = 0;

        Collection<PathInteraction> leftovers = new ArrayList<>();
        for(PathInteraction interaction : interactions){
            if(interaction.type() == PathInteractionType.RELATED){
                relatedValue += provider.getPathBonus(AFFINITY_CATEGORY,interaction.source())*interaction.value();
            }else leftovers.add(interaction);
        }
        double totalAffinity = relatedValue +baseMultiplier;
        double generativeMultiplier = 1;
        if(totalAffinity == 0) return 0;
        Collection<PathInteraction> destructive = new ArrayList<>();
        for(PathInteraction leftover : leftovers){
            if(leftover.type() == PathInteractionType.GENERATIVE){
                generativeMultiplier *= Math.max(
                        1+(leftover.value()*provider.getPathBonus(AFFINITY_CATEGORY,leftover.source()))
                                /totalAffinity,1);
            }else destructive.add(leftover);
        }
        totalAffinity *= generativeMultiplier;
        double destructiveMultiplier = 1;
        for(PathInteraction interaction : destructive){
            destructiveMultiplier *= Math.clamp(
                    1-(interaction.value()*provider.getPathBonus(AFFINITY_CATEGORY,interaction.source()))
                            /totalAffinity
                    ,0,1);
        }

        return totalAffinity*destructiveMultiplier;

    }
}
