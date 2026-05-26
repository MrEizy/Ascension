package net.zic.ascension.api.core;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.physique.PhysiqueData;
import net.zic.ascension.api.event.EventReason;
import net.zic.ascension.api.event.bloodline.BloodlineAddedEvent;
import net.zic.ascension.api.event.bloodline.BloodlineRemovedEvent;
import net.zic.ascension.api.event.physique.PhysiqueChangedEvent;

/**
 * Exact same data as an origin source, but further expands upon server only behaviour
 */
public class ServerOriginSource extends OriginSource {


    @Override
    public boolean setPhysique(Identifier physique, PhysiqueData physiqueData, EventReason reason) {
        if(physique == null) return false;
        Identifier oldPhysique = getPhysique();
        PhysiqueData oldPhysiqueData = getPhysiqueData();

        PhysiqueChangedEvent.Pre pre = new PhysiqueChangedEvent.Pre(oldPhysique,oldPhysiqueData,physique,physiqueData,this,reason);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;
        boolean result = super.setPhysique(physique, physiqueData);
        if(!result) return false;

        PhysiqueChangedEvent.Post post = new PhysiqueChangedEvent.Post(oldPhysique,oldPhysiqueData,physique,physiqueData,this,reason);
        NeoForge.EVENT_BUS.post(post);

        return true;
    }


    @Override
    public boolean addBloodline(Identifier bloodline, BloodlineData data, EventReason reason) {
        if(bloodline == null) return false;
        if(hasBloodline(bloodline)) return false;

        BloodlineAddedEvent.Pre pre = new BloodlineAddedEvent.Pre(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;

        boolean result = super.addBloodline(bloodline, data, reason);
        if(!result) return false;

        BloodlineAddedEvent.Post post= new BloodlineAddedEvent.Post(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(post);

        return true;

    }

    @Override
    public boolean removeBloodline(Identifier bloodline, EventReason reason) {
        if(bloodline == null) return false;
        if(!hasBloodline(bloodline)) return false;

        BloodlineData data = getBloodlineData(bloodline);
        BloodlineRemovedEvent.Pre pre = new BloodlineRemovedEvent.Pre(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(pre);
        if(pre.isCanceled()) return false;
        boolean result =  super.removeBloodline(bloodline, reason);

        if(!result) return false;

        BloodlineRemovedEvent.Post post= new BloodlineRemovedEvent.Post(bloodline,data,this,reason);
        NeoForge.EVENT_BUS.post(post);

        return true;
    }
}
