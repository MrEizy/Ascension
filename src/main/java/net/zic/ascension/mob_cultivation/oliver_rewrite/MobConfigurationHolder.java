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
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.OriginSourceEvent;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import org.jspecify.annotations.NonNull;

/**
 * A holder for a specific instance of a mobs config
 */
public class MobConfigurationHolder implements ConfigurableMobData {
    private final Mob mob;
    private MobConfigurationInstance configurationInstance = new MobConfigurationInstance();

    public MobConfigurationHolder(Mob mob) {
        this.mob = mob;
    }



    public Mob getMob(){return mob;}
    public OriginSource getSource(){
        AscensionEntityDataProvider provider = getMob().getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);

        return (provider == null || provider.getData() == null) ? null : provider.getData().getSource() ;
    }
    @Override
    public void setMobConfigurationInstance(MobConfigurationInstance configurationInstance){
        if(this.configurationInstance != null) removeConfigurationInstance();
        this.configurationInstance = configurationInstance;
    }

    @Override
    public MobConfigurationInstance getConfigurationInstance() {
        return configurationInstance;
    }

    public void removeConfigurationInstance(){
        if(getMob() == null) return;
        configurationInstance.removeFromMob(getMob());

        AscensionEntityDataProvider provider = getMob().getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if(provider == null || provider.getData() == null || provider.getData().getSource() == null) return;
        configurationInstance.removeFromSource(provider.getData().getSource());
        configurationInstance = null;
    }

    public void applyConfigurationToSource(){
        if(configurationInstance != null) configurationInstance.applyToSource(getSource());
    }
    public void applyConfigurationToEntity(){
        if(configurationInstance != null) configurationInstance.applyToMob(getMob());

    }


    public void read(ValueInput input) {
        configurationInstance.read(input,getSource().getRegistryAccess());
    }


    public void write(ValueOutput output) {
        configurationInstance.write(output,getSource().getRegistryAccess());
    }

    public static class Provider implements IAttachmentSerializer<MobConfigurationHolder> {
        @Override
        public MobConfigurationHolder read(
                @NonNull IAttachmentHolder holder,
                ValueInput input
        ) {
            if (!(holder instanceof Mob entity)) {
                return null;
            }
            MobConfigurationHolder instance = new MobConfigurationHolder(entity);
            instance.read(input);

            return instance;
        }

        @Override
        public boolean write(MobConfigurationHolder attachment, ValueOutput output) {
            attachment.write(output);
            return true;
        }
    }
}
