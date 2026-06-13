package net.zic.ascension.api.core.source;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
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

import java.util.*;

/**
 * A snapshot of the change a source has undergone
 */
public class SourceChangesSnapshot {
    Identifier physique;
    PhysiqueData physiqueData;

    List<Pair<Identifier, BloodlineData>> toAddBloodlines = new ArrayList<>();
    Set<Identifier> toRemoveBloodline = new HashSet<>();

    List<Pair<Identifier, PathData>> toAddPaths = new ArrayList<>();
    Set<Identifier> toRemovePaths = new HashSet<>();

    List<Pair<Identifier, SkillData>> toAddSkills = new ArrayList<>();
    Set<Identifier> toRemoveSkills = new HashSet<>();

    List<Pair<Identifier, DataSourceInstance>> toAddDataSources = new ArrayList<>();
    Set<Identifier> toRemoveDataSources = new HashSet<>();

     Set<StatInstance> dirtyStats = new HashSet<>();
     Set<ValueContainer> dirtyAffinity = new HashSet<>();
     HashMap<Identifier,Set<ValueContainer>> dirtyCategorizedAffinity = new HashMap<>();
     private SourceChangesSnapshot(){

    }



    public SourceChangesSnapshot(
            Identifier physique,
            PhysiqueData physiqueData,
            Map<Identifier, BloodlineData> toAddBloodlines,
            Set<Identifier> toRemoveBloodlines,
            Map<Identifier, PathData> toAddPaths,
            Set<Identifier> toRemovePaths,
            Map<Identifier, SkillData> toAddSkills,
            Set<Identifier> toRemoveSkills,
            Map<Identifier, DataSourceInstance> toAddDataSources,
            Set<Identifier> toRemoveDataSources,
            Set<StatInstance> dirtyStats,
            Set<ValueContainer> dirtyAffinity,
            HashMap<Identifier,Set<ValueContainer>> dirtyCategorizedAffinity
    ) {
        this.physique = physique;
        this.physiqueData = physiqueData;

        toAddBloodlines.forEach((key, value) ->
                this.toAddBloodlines.add(new Pair<>(key, value))
        );
        this.toRemoveBloodline = new HashSet<>(toRemoveBloodlines);

        toAddPaths.forEach((key, value) ->
                this.toAddPaths.add(new Pair<>(key, value))
        );
        this.toRemovePaths = new HashSet<>(toRemovePaths);

        toAddSkills.forEach((key, value) ->
                this.toAddSkills.add(new Pair<>(key, value))
        );
        this.toRemoveSkills = new HashSet<>(toRemoveSkills);

        toAddDataSources.forEach((key, value) ->
                this.toAddDataSources.add(new Pair<>(key, value))
        );
        this.toRemoveDataSources = new HashSet<>(toRemoveDataSources);

        this.dirtyStats = new HashSet<>(dirtyStats);
        this.dirtyAffinity = new HashSet<>(dirtyAffinity);

        this.dirtyCategorizedAffinity = new HashMap<>(dirtyCategorizedAffinity);
    }

    public void encode(ByteBuf buf) {
        buf.writeBoolean(physique != null);
        if (physique != null) {
            ByteBufHelpers.encodeIdentifier(physique, buf);
            buf.writeBoolean(physiqueData != null);
            if (physiqueData != null) {
                physiqueData.encode(buf);
            }
        }

        ByteBufHelpers.encodeCollection(toAddBloodlines, buf, (pair, byteBuf) -> {
            ByteBufHelpers.encodeIdentifier(pair.getA(), byteBuf);
            pair.getB().encode(byteBuf);
        });
        ByteBufHelpers.encodeCollection(toRemoveBloodline, buf, ByteBufHelpers::encodeIdentifier);

        ByteBufHelpers.encodeCollection(toAddPaths, buf, (pair, byteBuf) -> {
            ByteBufHelpers.encodeIdentifier(pair.getA(), byteBuf);
            pair.getB().encode(byteBuf);
        });
        ByteBufHelpers.encodeCollection(toRemovePaths, buf, ByteBufHelpers::encodeIdentifier);

        ByteBufHelpers.encodeCollection(toAddSkills, buf, (pair, byteBuf) -> {
            ByteBufHelpers.encodeIdentifier(pair.getA(), byteBuf);
            byteBuf.writeBoolean(pair.getB() != null);
            if (pair.getB() != null) {
                pair.getB().encode(byteBuf);
            }
        });
        ByteBufHelpers.encodeCollection(toRemoveSkills, buf, ByteBufHelpers::encodeIdentifier);

        ByteBufHelpers.encodeCollection(toAddDataSources, buf, (pair, byteBuf) -> {
            ByteBufHelpers.encodeIdentifier(pair.getA(), byteBuf);
            pair.getB().encode(byteBuf);
        });
        ByteBufHelpers.encodeCollection(toRemoveDataSources, buf, ByteBufHelpers::encodeIdentifier);

        ByteBufHelpers.encodeCollection(dirtyStats, buf, StatInstance::encode);
        ByteBufHelpers.encodeCollection(dirtyAffinity, buf, (container, byteBuf) ->
                ValueContainer.encode(byteBuf, container)
        );
        ByteBufHelpers.encodeMap(dirtyCategorizedAffinity,ByteBufHelpers::encodeIdentifier, (data,byteBuf)->{
            ByteBufHelpers.encodeCollection(data,byteBuf,(container,byteBuf2)->{
                ValueContainer.encode(byteBuf2,container);
            });
        },buf);
    }

    public static SourceChangesSnapshot decode(ByteBuf buf, RegistryAccess access) {
        SourceChangesSnapshot snapshot = new SourceChangesSnapshot();
        if (buf.readBoolean()) {
            snapshot.physique = ByteBufHelpers.decodeIdentifier(buf);
            if (buf.readBoolean()) {
                snapshot.physiqueData = CoreRegistries.PHYSIQUE_REGISTRY
                        .get(access)
                        .getValue(snapshot.physique)
                        .loadData(buf);
            }
        }

        snapshot.toAddBloodlines = ByteBufHelpers.decodeArray(buf, byteBuf -> {
            Identifier identifier = ByteBufHelpers.decodeIdentifier(byteBuf);
            BloodlineData data = CoreRegistries.BLOODLINE_REGISTRY
                    .get(access)
                    .getValue(identifier)
                    .loadData(byteBuf);
            return new Pair<>(identifier, data);
        });
        snapshot.toRemoveBloodline = new HashSet<>(
                ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier)
        );

        snapshot.toAddPaths = ByteBufHelpers.decodeArray(buf, byteBuf -> {
            Identifier identifier = ByteBufHelpers.decodeIdentifier(byteBuf);
            PathData data = CoreRegistries.PATH_REGISTRY
                    .get(access)
                    .getValue(identifier)
                    .loadData(byteBuf, access);
            return new Pair<>(identifier, data);
        });
        snapshot.toRemovePaths = new HashSet<>(
                ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier)
        );

        snapshot.toAddSkills = ByteBufHelpers.decodeArray(buf, byteBuf -> {
            Identifier identifier = ByteBufHelpers.decodeIdentifier(byteBuf);
            SkillData data = null;
            if (byteBuf.readBoolean()) {
                data = CoreRegistries.SKILL_REGISTRY
                        .get(access)
                        .getValue(identifier)
                        .loadData(byteBuf);
            }
            return new Pair<>(identifier, data);
        });
        snapshot.toRemoveSkills = new HashSet<>(
                ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier)
        );

        snapshot.toAddDataSources = ByteBufHelpers.decodeArray(buf, byteBuf -> {
            Identifier identifier = ByteBufHelpers.decodeIdentifier(byteBuf);
            DataSourceInstance data = CoreRegistries.DATA_SOURCE_REGISTRY
                    .get(access)
                    .getValue(identifier)
                    .loadInstance(byteBuf);
            return new Pair<>(identifier, data);
        });
        snapshot.toRemoveDataSources = new HashSet<>(
                ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier)
        );

        snapshot.dirtyStats = new HashSet<>(
                ByteBufHelpers.decodeArray(buf, StatInstance::decode)
        );
        snapshot.dirtyAffinity = new HashSet<>(
                ByteBufHelpers.decodeArray(buf, ValueContainer::decode)
        );

        ByteBufHelpers.decodeMap(snapshot.dirtyCategorizedAffinity,ByteBufHelpers::decodeIdentifier,(byteBuf)->
            new HashSet<>(ByteBufHelpers.decodeArray(byteBuf,ValueContainer::decode))
        ,buf);

        return snapshot;
    }
}
