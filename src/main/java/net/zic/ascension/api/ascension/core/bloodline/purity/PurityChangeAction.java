package net.zic.ascension.api.ascension.core.bloodline.purity;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.RegistryObjectData;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.rpg_engine.source.OriginSource;


import java.util.UUID;

/**
 * provides context needed for purity changes
 */
public interface PurityChangeAction extends ProgressAction {

    @Override
    default void run(UUID holderId, OriginSource source, Identifier contextIdentifier, RegistryObjectData contextData, ProgressDirection direction){
        //first test to make sure context is a bloodline
        Bloodline bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,contextIdentifier,source.getRegistryAccess());
        if(bloodline == null) return;
        if(!(contextData instanceof BloodlineData data)) return;

        int purity = data.getPurity();
        if(direction == ProgressDirection.DOWN) purity++;

        run(holderId,source,bloodline,data,purity,direction);
    }

    void run(UUID holderId, OriginSource source, Bloodline bloodline, BloodlineData data, int purity, ProgressDirection direction);

}
