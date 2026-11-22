package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import org.jspecify.annotations.NonNull;

public class MobConfigurationHolder implements ConfigurableMobData {
    private final Mob mob;
    private MobConfigurationInstance configurationInstance = new MobConfigurationInstance();
    private boolean entityApplied;
    private boolean sourceApplied;
    private boolean freshlyGenerated;

    public MobConfigurationHolder(Mob mob) {
        this.mob = mob;
    }

    public Mob getMob() {
        return mob;
    }

    public OriginSource getSource() {
        AscensionEntityDataProvider provider = mob.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        return provider == null || provider.getData() == null ? null : provider.getData().getSource();
    }

    @Override
    public void setMobConfigurationInstance(MobConfigurationInstance instance) {
        if (configurationInstance != null && instance != null
                && configurationInstance.getTier() == instance.getTier()
                && configurationInstance.getTraits().equals(instance.getTraits())) return;
        boolean reapplyEntity = entityApplied;
        boolean reapplySource = sourceApplied;
        removeConfigurationInstance();
        configurationInstance = instance;
        freshlyGenerated = !reapplyEntity;
        if (reapplyEntity) applyConfigurationToEntity();
        if (reapplySource) applyConfigurationToSource();
    }

    @Override
    public MobConfigurationInstance getConfigurationInstance() {
        return configurationInstance;
    }

    public void removeConfigurationInstance() {
        if (configurationInstance == null) return;
        if (sourceApplied) configurationInstance.removeFromSource(getSource(), mob.getUUID());
        if (entityApplied) configurationInstance.removeFromMob(mob);
        configurationInstance = null;
        entityApplied = false;
        sourceApplied = false;
        freshlyGenerated = false;
    }

    public void applyConfigurationToSource() {
        if (mob.level().isClientSide() || configurationInstance == null) return;
        OriginSource source = getSource();
        if (source == null) return;
        configurationInstance.applyToSource(source, mob.getUUID());
        sourceApplied = true;
    }

    public void applyConfigurationToEntity() {
        if (configurationInstance == null || entityApplied) return;
        configurationInstance.applyToMob(mob);
        entityApplied = true;
    }

    public void finishSpawnInitialization() {
        if (!freshlyGenerated) return;
        freshlyGenerated = false;
        mob.setHealth(mob.getMaxHealth());
    }

    public void read(ValueInput input) {
        configurationInstance = new MobConfigurationInstance();
        configurationInstance.read(input, mob.registryAccess());
        entityApplied = false;
        sourceApplied = false;
        freshlyGenerated = false;
    }

    public void write(ValueOutput output) {
        if (configurationInstance != null) configurationInstance.write(output, mob.registryAccess());
    }

    public static class Provider implements IAttachmentSerializer<MobConfigurationHolder> {
        @Override
        public MobConfigurationHolder read(@NonNull IAttachmentHolder holder, ValueInput input) {
            if (!(holder instanceof Mob mob)) return null;
            MobConfigurationHolder instance = new MobConfigurationHolder(mob);
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
