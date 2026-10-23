package net.zic.ascension.api.ascension.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

public class PhysiqueDataSource implements DataSource<PhysiqueHolder> {
    public static final SyncHandler<PhysiqueHolder> SYNC_HANDLER = new SyncHandler<>() {
        @Override
        public void decode(PhysiqueHolder existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.decode(buf, access);
        }

        @Override
        public void encode(PhysiqueHolder encodable, ByteBuf buf, RegistryAccess access) {
            encodable.encode(buf, access);
        }
    };
    public static final SerializerHandler<PhysiqueHolder> SERIALIZER_HANDLER = new SerializerHandler<PhysiqueHolder>() {
        @Override
        public PhysiqueHolder read(ValueInput input, RegistryAccess access) {
            PhysiqueHolder holder = new PhysiqueHolder();
            try{
                PhysiqueInstance<?> instance = PhysiqueInstance.read(input,access);
                if(instance == null) return holder;
                holder.setPhysique(instance);

                AscensionCraft.LOGGER.info("Loaded physique {}",instance.getPhysiqueId());
            }catch (Exception e){
                AscensionCraft.LOGGER.info("Error loading physique");
            }
            return holder;
        }

        @Override
        public void write(PhysiqueHolder writable, ValueOutput output, RegistryAccess access) {
            PhysiqueInstance<?> instance = writable.getPhysique();
            if(instance == null) return;
            try{
                AscensionCraft.LOGGER.debug("Writing physique {}",instance.getPhysiqueId());
                instance.write(output,access);
            }catch (Exception e){
                AscensionCraft.LOGGER.warn("Error writing physique {}",instance.getPhysiqueId());
                AscensionCraft.LOGGER.warn("stacktrace: ",e);
            }
        }
    };
    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.HIGHEST;
    }

    @Override
    public void onAdded(OriginSource source, PhysiqueHolder holder) {

        PhysiqueInstance<?> instance = holder.getPhysique();
        if(instance == null) return;
        holder.setPhysique(null);
        AscensionOriginSourceHelper.setPhysique(source,instance,false);
    }

    @Override
    public void onRemoved(OriginSource source, PhysiqueHolder holder) {
        PhysiqueInstance<?> oldPhysique = holder.getPhysique();
        if(oldPhysique == null) return;

        AscensionOriginSourceHelper.setPhysique(source,(Identifier) null);

        holder.setPhysique(oldPhysique);

    }

    @Override
    public void preFinishedLoading(OriginSource source, PhysiqueHolder holder) {

    }

    @Override
    public void finishedLoading(OriginSource source, PhysiqueHolder holder) {

    }

    @Override
    public void applyToEntity(LivingEntity entity, PhysiqueHolder holder) {
        PhysiqueInstance<?> instance = holder.getPhysique();
        if (instance != null) instance.applyToEntity(entity);
    }

    @Override
    public void removeFromEntity(LivingEntity entity, PhysiqueHolder holder) {

        PhysiqueInstance<?> instance = holder.getPhysique();
        if (instance != null) instance.removeFromEntity(entity);
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
        return SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<PhysiqueHolder> syncHandler(boolean fullPatch) {
        return SYNC_HANDLER;
    }
}
