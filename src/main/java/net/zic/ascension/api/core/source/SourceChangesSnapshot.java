package net.zic.ascension.api.core.source;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.data_source.DataSourceInstance;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.physique.PhysiqueData;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.StatInstance;
import net.zic.zenithlib.value_containers.ValueContainer;
import oshi.util.tuples.Pair;

import javax.xml.crypto.Data;
import java.util.*;

/**
 * a snapshot of the changes a source has undergone
 */
public class SourceChangesSnapshot {
     Identifier physique;
     PhysiqueData physiqueData;

     List<Pair<Identifier,BloodlineData>> toAddBloodlines = new ArrayList<>();
     Set<Identifier> toRemoveBloodline = new HashSet<>();

     List<Pair<Identifier,PathData>> toAddPaths = new ArrayList<>();
     Set<Identifier> toRemovePaths= new HashSet<>();

     List<Pair<Identifier,SkillData>> toAddSkills = new ArrayList<>();
     Set<Identifier> toRemoveSkills = new HashSet<>();

     List<Pair<Identifier,DataSourceInstance>> toAddDataSources = new ArrayList<>();
     Set<Identifier> toRemoveDataSources = new HashSet<>();

     Set<StatInstance> dirtyStats = new HashSet<>();
     Set<ValueContainer> dirtyAffinity = new HashSet<>();

    private SourceChangesSnapshot(){

    }
    public SourceChangesSnapshot(
            Identifier physique,
            PhysiqueData physiqueData,
            Map<Identifier,BloodlineData> toAddBloodlines,
            Set<Identifier> toRemoveBloodlines,
            Map<Identifier,PathData> toAddPaths,
            Set<Identifier> toRemovePaths,
            Map<Identifier,SkillData> toAddSkills,
            Set<Identifier> toRemoveSkills,
            Map<Identifier, DataSourceInstance> toAddDataSources,
            Set<Identifier> toRemoveDataSources,
            Set<StatInstance> dirtyStats,
            Set<ValueContainer> dirtyAffinity
    ){
        this.physique =physique;
        this.physiqueData = physiqueData;

        ArrayList<Pair<Identifier,BloodlineData>> bloodlines = new ArrayList<>();
        toAddBloodlines.forEach((key,val)->bloodlines.add(new Pair<>(key,val)));
        this.toAddBloodlines = bloodlines;

        ArrayList<Pair<Identifier,PathData>> paths = new ArrayList<>();
        toAddPaths.forEach((key,val)->paths.add(new Pair<>(key,val)));
        this.toAddPaths = paths;

        ArrayList<Pair<Identifier,SkillData>> skillData = new ArrayList<>();
        toAddSkills.forEach((key,val)->skillData.add(new Pair<>(key,val)));
        this.toAddSkills = skillData;

        ArrayList<Pair<Identifier,DataSourceInstance>> dataSources = new ArrayList<>();
        toAddDataSources.forEach((key,val)->dataSources.add(new Pair<>(key,val)));
        this.toAddDataSources = dataSources;

        this.dirtyStats = dirtyStats;
        this.dirtyAffinity = dirtyAffinity;
    }

    public void encode(ByteBuf buf){
        buf.writeBoolean(physique != null); //are we encoding a physique change
        if(physique != null){
            ByteBufHelpers.encodeIdentifier(physique,buf);
            buf.writeBoolean(physiqueData != null); //are we encoding data or not
            if(physiqueData != null) physiqueData.encode(buf);
        }

        //encode bloodlines
        ByteBufHelpers.encodeCollection(toAddBloodlines,buf,(pair,byteBuf)->{
            ByteBufHelpers.encodeIdentifier(pair.getA(),byteBuf);
            pair.getB().encode(byteBuf);
        });
        ByteBufHelpers.encodeCollection(toRemoveBloodline,buf,ByteBufHelpers::encodeIdentifier);

        //encode paths
        ByteBufHelpers.encodeCollection(toAddPaths,buf, (pair,byteBuf)->{
            ByteBufHelpers.encodeIdentifier(pair.getA(),byteBuf);
            pair.getB().encode(byteBuf);
        });
        ByteBufHelpers.encodeCollection(toRemovePaths,buf, ByteBufHelpers::encodeIdentifier);

        //encode skills
        ByteBufHelpers.encodeCollection(toAddSkills,buf,(pair,byteBuf)->{
            ByteBufHelpers.encodeIdentifier(pair.getA(),byteBuf);
            buf.writeBoolean(pair.getB()!=null);
            if(pair.getB() != null) pair.getB().encode(byteBuf);
        });
        ByteBufHelpers.encodeCollection(toRemoveSkills,buf,ByteBufHelpers::encodeIdentifier);

        ByteBufHelpers.encodeCollection(toAddDataSources,buf,(pair,byteBuf)->{
            ByteBufHelpers.encodeIdentifier(pair.getA(),byteBuf);
            pair.getB().encode(byteBuf);
        });
        ByteBufHelpers.encodeCollection(toRemoveDataSources,buf,ByteBufHelpers::encodeIdentifier);

        ByteBufHelpers.encodeCollection(dirtyStats,buf,StatInstance::encode);
        ByteBufHelpers.encodeCollection(dirtyAffinity,buf,(data,byteBuf)-> {
            ValueContainer.encode(byteBuf,data);
        });

    }

    public static SourceChangesSnapshot decode(ByteBuf buf, RegistryAccess access){
        SourceChangesSnapshot snapshot = new SourceChangesSnapshot();
        if(buf.readBoolean()){
            snapshot.physique = ByteBufHelpers.decodeIdentifier(buf);
            if(buf.readBoolean()) snapshot.physiqueData = CoreRegistries.PHYSIQUE_REGISTRY.get(access).getValue(snapshot.physique).loadData(buf);
        }

        snapshot.toAddBloodlines = ByteBufHelpers.decodeArray(buf,(byteBuf)->{
            Identifier identifier = ByteBufHelpers.decodeIdentifier(byteBuf);
            BloodlineData data = CoreRegistries.BLOODLINE_REGISTRY.get(access).getValue(identifier).loadData(byteBuf);
            return new Pair<>(identifier,data);
        });
        snapshot.toRemoveBloodline = new HashSet<>(ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier));

        snapshot.toAddPaths = ByteBufHelpers.decodeArray(buf,(byteBuf)->{
            Identifier identifier = ByteBufHelpers.decodeIdentifier(byteBuf);
            PathData data = CoreRegistries.PATH_REGISTRY.get(access).getValue(identifier).loadData(byteBuf);
            return new Pair<>(identifier,data);
        });
        snapshot.toRemovePaths = new HashSet<>(ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier));

        snapshot.toAddSkills = ByteBufHelpers.decodeArray(buf,(byteBuf)->{
            Identifier identifier = ByteBufHelpers.decodeIdentifier(byteBuf);
            SkillData data = null;
            if(buf.readBoolean()) data = CoreRegistries.SKILL_REGISTRY.get(access).getValue(identifier).loadData(byteBuf);
            return new Pair<>(identifier,data);
        });
        snapshot.toRemoveSkills = new HashSet<>(ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier));

        snapshot.toAddDataSources = ByteBufHelpers.decodeArray(buf,(byteBuf)->{
            Identifier identifier = ByteBufHelpers.decodeIdentifier(byteBuf);
            DataSourceInstance data = CoreRegistries.DATA_SOURCE_REGISTRY.get(access).getValue(identifier).loadInstance(byteBuf);
            return new Pair<>(identifier,data);
        });
        snapshot.toRemoveDataSources = new HashSet<>(ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier));


        snapshot.dirtyStats = new HashSet<>(ByteBufHelpers.decodeArray(buf, StatInstance::decode));

        snapshot.dirtyAffinity = new HashSet<>(ByteBufHelpers.decodeArray(buf, ValueContainer::decode));


        return snapshot;
    }


}
