package net.zic.ascension.api.core.bloodline.purity;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.progression.ProgressDirection;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.progression.ProgressActionCondition;
import net.zic.ascension.api.core.source.OriginSource;


public interface PurityChangeActionCondition extends ProgressActionCondition {
    @Override
    default boolean test(OriginSource source, Identifier contextIdentifier, ProgressDirection direction){
        //first test to make sure context is a bloodline
        Bloodline bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,contextIdentifier,source.getRegistryAccess());
        if(bloodline == null) return false;

        BloodlineData data = source.getBloodlineData(contextIdentifier);
        int purity = data.getPurity();
        if(direction == ProgressDirection.DOWN) purity++;

        return test(source,bloodline,data,purity,direction);
    }

    boolean test(OriginSource source,Bloodline bloodline,BloodlineData data,int purity,ProgressDirection direction);

}
