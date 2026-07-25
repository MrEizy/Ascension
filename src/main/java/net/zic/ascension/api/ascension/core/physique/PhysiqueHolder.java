package net.zic.ascension.api.ascension.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.RPGEngineRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.network.ByteBufHelpers;

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
        output.putString("physique",physique.toString());
        getData().write(output.child("data"));
    }
    public void read(ValueInput input, RegistryAccess access){
        Identifier physique = Identifier.parse(input.getStringOr("physique","none"));
        PhysiqueData data = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY,physique,access).loadData(input,access);
        this.physique = physique;
        this.data = data;
    }
    public void encode(ByteBuf buf,RegistryAccess access){
        ByteBufHelpers.encodeIdentifier(physique,buf);
        getData().encode(buf);
    }
    public void decode(ByteBuf buf,RegistryAccess access){
        Identifier id = ByteBufHelpers.decodeIdentifier(buf);
        PhysiqueData data = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY,physique,access).loadData(buf);
        this.physique = id;
        this.data = data;
    }
}
