package net.zic.ascension.api.ascension.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;

public class PhysiqueDataSource implements DataSource<PhysiqueHolder> {
    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.HIGHEST;
    }

    @Override
    public void onAdded(OriginSource source, PhysiqueHolder holder) {

        if(holder.getPhysique() == null) return;
        Identifier physique = holder.getPhysique();
        PhysiqueData data = holder.getData();
        holder.setPhysique(null,null);
        AscensionOriginSourceHelper.setPhysique(source,physique,data,false);
    }

    @Override
    public void onRemoved(OriginSource source, PhysiqueHolder holder) {
        if(holder.getPhysique() == null) return;
        Identifier oldPhysique = holder.getPhysique();
        PhysiqueData oldPhysiqueData = holder.getData();

        AscensionOriginSourceHelper.setPhysique(source,null);

        holder.setPhysique(oldPhysique,oldPhysiqueData);

    }

    @Override
    public void preFinishedLoading(OriginSource source, PhysiqueHolder holder) {

    }

    @Override
    public void finishedLoading(OriginSource source, PhysiqueHolder holder) {

    }

    @Override
    public void applyToEntity(LivingEntity entity, PhysiqueHolder holder) {
        if (holder.getPhysique() == null || holder.getData() == null) {
            return;
        }

        Physique physique = holder.getPhysique(entity.level().registryAccess());
        if (physique == null) {
            return;
        }

        physique.applyToEntity(entity, holder.getData());
    }

    @Override
    public void removeFromEntity(LivingEntity entity, PhysiqueHolder holder) {

        if (holder.getPhysique() == null || holder.getData() == null) {
            return;
        }

        Physique physique = holder.getPhysique(entity.level().registryAccess());
        if (physique == null) {
            return;
        }

        physique.removeFromEntity(entity, holder.getData());
    }

    @Override
    public PhysiqueHolder newInstance(RegistryAccess access) {
        return new PhysiqueHolder();
    }

    @Override
    public Class<PhysiqueHolder> getInstanceClass() {
        return PhysiqueHolder.class;
    }

    @Override
    public SerializerHandler<PhysiqueHolder> serializerHandler() {
        return new SerializerHandler<PhysiqueHolder>() {
            @Override
            public PhysiqueHolder read(ValueInput input, RegistryAccess access) {
                PhysiqueHolder holder = new PhysiqueHolder();
                try{
                    Identifier id = NbtHelpers.readIdentifier(input,"physique");
                    Physique physique = CoreRegistries.PHYSIQUE_REGISTRY.get(access).getValue(id);
                    if(physique == null) throw new Exception("physique "+id+" does not exist");

                    PhysiqueData physiqueData = input.child("data")
                            .map(valueInput -> physique.loadData(valueInput,access))
                            .orElse(physique.newData(access));

                    holder.setPhysique(id,physiqueData);

                    AscensionCraft.LOGGER.info("Loaded physique {}",id);
                }catch (Exception e){
                    AscensionCraft.LOGGER.info("Error loading physique");
                }
                return holder;
            }

            @Override
            public void write(PhysiqueHolder writable, ValueOutput output, RegistryAccess access) {
                if(writable.getPhysique() == null || writable.getData() == null) return;
                try{

                    AscensionCraft.LOGGER.debug("Writing physique {}",writable.getPhysique());
                    NbtHelpers.writeIdentifier(output,"physique",writable.getPhysique());

                    writable.getData().write(output.child("data"),access);

                }catch (Exception e){
                    AscensionCraft.LOGGER.warn("Error writing physique {}",writable.getPhysique());
                    AscensionCraft.LOGGER.warn("stacktrace: ",e);
                }
            }
        };
    }

    @Override
    public SyncHandler<PhysiqueHolder> syncHandler(boolean fullPatch) {
        return new SyncHandler<>() {
            @Override
            public void decode(PhysiqueHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
                if(!buf.readBoolean()){
                    existingEncodable.physique = null;
                    existingEncodable.data = null;
                    return;
                }
                existingEncodable.physique = ByteBufHelpers.decodeIdentifier(buf);
                existingEncodable.data = existingEncodable.getPhysique(access).loadData(buf,access);
            }

            @Override
            public void encode(PhysiqueHolder encodable, ByteBuf buf, RegistryAccess access) {
                buf.writeBoolean(encodable.physique != null);
                if(encodable.physique == null) return;

                ByteBufHelpers.encodeIdentifier(encodable.physique,buf);
                encodable.data.encode(buf,access);
            }
        };
    }
}
