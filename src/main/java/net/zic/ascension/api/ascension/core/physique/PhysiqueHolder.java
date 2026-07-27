package net.zic.ascension.api.ascension.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.RPGEngineRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.Optional;

public class PhysiqueHolder implements DataSourceInstance {

    private Identifier physique;
    private PhysiqueData data;

    public Identifier getPhysique(){
        return physique;
    }
    public PhysiqueData getData(){
        return data;
    }
    public Physique getPhysique(RegistryAccess access){
        return CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY,getPhysique(),access);
    }

    public boolean setPhysique(Identifier id,PhysiqueData data){
        if(id == null || id.equals(physique)) return false;
        physique = id;
        this.data = data;
        return true;
    }

    @Override
    public DataSource getDataSource() {
        return CoreHolderProviders.PHYSIQUE_HOLDER_PROVIDER.get();
    }

    public void write(ValueOutput output,RegistryAccess access){
        try{
            AscensionCraft.LOGGER.debug("Writing physique {}",getPhysique());
            NbtHelpers.writeIdentifier(output,"physique",getPhysique());
            ValueOutput data = output.child("data");
            if(getData() != null) getData().write(data);
            else throw new Exception("no physique data for physique "+physique);
        }catch (Exception e){
            AscensionCraft.LOGGER.error("Error writing physique {}",getPhysique());
            AscensionCraft.LOGGER.error("stacktrace: ",e);
        }
    }
    public void read(ValueInput input, RegistryAccess access){
        try{

            Identifier id = NbtHelpers.readIdentifier(input,"physique");


            Optional<ValueInput> data = input.child("data");
            PhysiqueData physiqueData = data.map(valueInput ->
                    CoreRegistries.PHYSIQUE_REGISTRY.get(access).getValue(id).loadData(valueInput,access))
                    .orElse(CoreRegistries.PHYSIQUE_REGISTRY.get(access).getValue(id).newData(access));

            setPhysique(id,physiqueData);

            AscensionCraft.LOGGER.info("Loaded physique {}",id);
        }catch (Exception e){
            AscensionCraft.LOGGER.error("Error loading physique");
            AscensionCraft.LOGGER.error("stacktrace : ",e);
            //TODO set technique to default
        }
    }
    public void encode(ByteBuf buf,RegistryAccess access){
        buf.writeBoolean( physique != null);
        if(physique == null) return;
        ByteBufHelpers.encodeIdentifier(physique,buf);
        getData().encode(buf);
    }
    public void decode(ByteBuf buf,RegistryAccess access){
        if(!buf.readBoolean()){
            physique = null;
            data = null;
            return;
        }
        Identifier id = ByteBufHelpers.decodeIdentifier(buf);
        PhysiqueData data = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY,id,access).loadData(buf);
        this.physique = id;
        this.data = data;
    }
}
