package net.zic.ascension.api.ascension.core.bloodline.purity;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.RegistryObjectData;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.progression.ProgressActionCondition;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;


public interface PurityChangeActionCondition extends ProgressActionCondition {
    @Override
    default boolean test(AscensionOriginSource source, Identifier contextIdentifier, RegistryObjectData contextData, ProgressDirection direction){
        //first test to make sure context is a bloodline
        Bloodline bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,contextIdentifier,source.getRegistryAccess());
        if(bloodline == null) return false;
        if(!(contextData instanceof BloodlineData data)) return false;
        int purity = data.getPurity();
        if(direction == ProgressDirection.DOWN) purity++;

        return test(source,bloodline,data,purity,direction);
    }

    boolean test(AscensionOriginSource source, Bloodline bloodline, BloodlineData data, int purity, ProgressDirection direction);

}
