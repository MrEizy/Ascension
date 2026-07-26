package net.zic.ascension.api.ascension.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.physique.PhysiqueData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;
import oshi.util.tuples.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

//TODO add logic to handle a max number of bloodlines
public class BloodlineHolder implements DataSourceInstance {

    private final HashMap<Identifier,BloodlineData> bloodlines = new HashMap<>();

    private final HashSet<Identifier> dirtyBloodlines = new HashSet<>();
    private final HashSet<Identifier> toRemoveBloodlines = new HashSet<>();
    public boolean addBloodline(Identifier bloodline,BloodlineData data){
        if(hasBloodline(bloodline)) return false;
        bloodlines.put(bloodline,data);
        dirtyBloodlines.add(bloodline);
        return true;
    }
    public boolean removeBloodline(Identifier bloodline){
        if(hasBloodline(bloodline)){
            toRemoveBloodlines.add(bloodline);
            dirtyBloodlines.remove(bloodline);
            bloodlines.remove(bloodline);
            return true;
        }
        return false;
    }
    public boolean hasBloodline(Identifier bloodline){
        return bloodlines.containsKey(bloodline);
    }

    public BloodlineData getBloodline(Identifier bloodline){
        return bloodlines.get(bloodline);
    }
    public Bloodline getBloodline(Identifier bloodline,RegistryAccess access){
        return CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,bloodline,access);
    }
    public Collection<Identifier> getBloodlines(){
        return bloodlines.keySet();
    }

    public void markBloodlineDirty(Identifier bloodline){
        if(hasBloodline(bloodline)) dirtyBloodlines.add(bloodline);
    }
    @Override
    public DataSource getDataSource() {
        return CoreHolderProviders.BLOODLINE_HOLDER_PROVIDER.get();
    }

    public void write(ValueOutput output, RegistryAccess access){

        ValueOutput.ValueOutputList bloodlineOutputList = output.childrenList("bloodlines");
        for(Identifier bloodline : getBloodlines()){
            AscensionCraft.LOGGER.debug("Writing Bloodline {}",bloodline);
            try{
                ValueOutput bloodlineOutput = bloodlineOutputList.addChild();
                NbtHelpers.writeIdentifier(bloodlineOutput,"id",bloodline);
                ValueOutput dataOutput = bloodlineOutput.child("data");
                getBloodline(bloodline).write(dataOutput);
            } catch (Exception e){
                AscensionCraft.LOGGER.error("Error writing bloodline {}",bloodline);
                AscensionCraft.LOGGER.error("stacktrace: ",e);
            }

        }

    }
    public void read(ValueInput input, RegistryAccess access){
        try {
            ValueInput.ValueInputList bloodlinesInput = input.childrenListOrEmpty("bloodlines");

            for(ValueInput bloodlineInput : bloodlinesInput.stream().toList()){
                try {
                    Identifier id = NbtHelpers.readIdentifier(bloodlineInput,"id");
                    AscensionCraft.LOGGER.debug("Reading Bloodline {}",id);

                    Optional<ValueInput> data = bloodlineInput.child("data");
                    Bloodline bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,id,access);

                    if(data.isEmpty()) throw new Exception("no bloodline data present for bloodline "+id);
                    else addBloodline(id,bloodline.loadData(data.get(),access));
                } catch (Exception e){
                    AscensionCraft.LOGGER.error("error loading bloodline");
                    AscensionCraft.LOGGER.error("stacktrace : ",e);
                }

            }
        } catch (Exception e){
            AscensionCraft.LOGGER.error("Error loading all bloodlines");
            AscensionCraft.LOGGER.error("stacktrace : ",e);
        }

    }
    public void encode(ByteBuf buf, RegistryAccess access){
        buf.writeInt(dirtyBloodlines.size());
        for(Identifier dirtyBloodline : dirtyBloodlines){
            ByteBufHelpers.encodeIdentifier(dirtyBloodline,buf);
            getBloodline(dirtyBloodline).encode(buf);
        }
        ByteBufHelpers.encodeCollection(toRemoveBloodlines,buf,ByteBufHelpers::encodeIdentifier);
        dirtyBloodlines.clear();
        toRemoveBloodlines.clear();
    }
    public void decode(ByteBuf buf,RegistryAccess access){
        int size = buf.readInt();
        for(int i = 0;i<size;i++){
            Identifier bloodlineId = ByteBufHelpers.decodeIdentifier(buf);
            Bloodline bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,bloodlineId,access);
            BloodlineData data = bloodline.loadData(buf);
            bloodlines.put(bloodlineId,data);
        }
        ByteBufHelpers.decodeArray(buf,ByteBufHelpers::decodeIdentifier).forEach(this::removeBloodline);

        dirtyBloodlines.clear();
        toRemoveBloodlines.clear();
    }
}
