package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.OriginSourceEvent;
import net.zic.ascension.impl.core.entity.SimpleAscensionEntityData;
import org.apache.logging.log4j.core.Core;
import org.jspecify.annotations.NonNull;
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class MobAscensionData extends SimpleAscensionEntityData {

    private MobConfigurationInstance configurationInstance = new MobConfigurationInstance();

    public MobAscensionData(OriginSource source, Mob entity) {
        super(source, entity);
    }
    @SubscribeEvent
    public static void onSourceLoad(OriginSourceEvent.OriginSourceFinishedLoadingEvent event){
        for(LivingEntity attachedEntity : event.getSource().getAttachedEntities()){
            AscensionEntityDataProvider provider = attachedEntity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if(provider == null) continue;
            if(!(provider.getData() instanceof MobAscensionData mobAscensionData)) continue;
            System.out.println(attachedEntity.getClass());
            mobAscensionData.applyConfigurationToSource();
        }
    }
    public Mob getMob(){return (Mob) getEntity();}
    public void setMobConfigurationInstance(MobConfigurationInstance configurationInstance){
        this.configurationInstance = configurationInstance;
        this.configurationInstance.freshApply(getMob());
    }

    public void applyConfigurationToSource(){
        if(configurationInstance != null) configurationInstance.applyToSource(getSource());
    }
    public void applyConfigurationToEntity(){
        if(configurationInstance != null) configurationInstance.applyToMob(getMob());

    }

    @Override
    public void read(ValueInput input) {
        super.read(input);
        configurationInstance.read(input,getSource().getRegistryAccess());
    }

    @Override
    public void write(ValueOutput output) {
        super.write(output);
        configurationInstance.write(output,getSource().getRegistryAccess());
    }


    public static class Provider implements IAttachmentSerializer<MobAscensionData> {
        @Override
        public MobAscensionData read(
                @NonNull IAttachmentHolder holder,
                ValueInput input
        ) {
            if (!(holder instanceof Mob entity)) {
                return null;
            }

            OriginSource originSource = new OriginSource();
            originSource.setCachedData(input.childOrEmpty("source_data"));

            MobAscensionData data = new MobAscensionData(originSource, entity);
            data.read(input);
            return data;
        }

        @Override
        public boolean write(MobAscensionData attachment, ValueOutput output) {
            attachment.getSource().writeOriginSourceData(output.child("source_data"));
            attachment.write(output);
            return true;
        }
    }
}
