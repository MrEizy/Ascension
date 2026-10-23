package net.zic.ascension.api.ascension.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import org.jspecify.annotations.Nullable;

//an origin holds at most one physique
public class PhysiqueHolder implements DataSourceInstance<PhysiqueDataSource> {

    private @Nullable PhysiqueInstance<?> physique;

    public @Nullable Identifier getPhysiqueId(){
        return physique == null ? null : physique.getPhysiqueId();
    }
    public @Nullable PhysiqueInstance<?> getPhysique(){
        return physique;
    }

    /**
     * replaces the held physique, runs no physique logic
     * @return false if the new physique is the same as the held one
     */
    public boolean setPhysique(@Nullable PhysiqueInstance<?> instance){
        Identifier newId = instance == null ? null : instance.getPhysiqueId();
        Identifier oldId = getPhysiqueId();
        if((newId != null && newId.equals(oldId)) || (newId == null && oldId == null)) return false;
        physique = instance;
        return true;
    }

    //──Sync────────────────────────────────────────────────────────

    void encode(ByteBuf buf, RegistryAccess access) {
        buf.writeBoolean(physique != null);
        if(physique != null) physique.encode(buf, access);
    }

    void decode(ByteBuf buf, RegistryAccess access) {
        physique = buf.readBoolean() ? PhysiqueInstance.decode(buf, access) : null;
    }

    @Override
    public PhysiqueDataSource getDataSource() {
        return CoreDataSources.PHYSIQUE_DATA_SOURCE.get();
    }
}
