package net.zic.ascension.api.core.path;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.source.OriginSource;

import java.util.logging.Level;

/**
 * Holds all the methods needed to meaningfully handle the conversion of context based path effects(e.g affinity, atmospheric dao and dao damage bonus/resistance)
 * into a universal modifier that can be applied to anything
 */
public class PathEffectValueUtil {


    //TODO get it to pass extra info like level and stuff? this way we can properly get all influenced values
    public static double getPathEffectFromAffinity(Level level, LivingEntity entity, BlockPos pos, AffinityHolder holder){
        //why am i doing it like this?
        //doing everything through affinity "kinda" works but coneptualy causes problems
        //mainly separation of context. i might want to give an affinity bonus ONLY to damage,but the current system does not allow for that

        //the other way to handle this i guess is to have 2 value containers for each path? the base affinity one then a damage one? the damage one
        //but then the probelm becomes why stop at that why not add a cultvation one as well
        return 0;
    }
}
