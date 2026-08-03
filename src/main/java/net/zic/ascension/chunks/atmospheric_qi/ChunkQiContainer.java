package net.zic.ascension.chunks.atmospheric_qi;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.ascension.AscensionCraft;

import net.zic.ascension.api.ascension.core.path.PathEffectValueUtil;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * Holds the qi of a chunk.
 * Qi is either pure or typed
 *
 */
public class ChunkQiContainer implements PathBonusProvider {
    private double energy;
    final ValueContainer energyRegenRate;
    final ValueContainer energyCap;

    private boolean loaded;

    private final PathBonusHolder affinities = new PathBonusHolder();
    public ChunkQiContainer(double energy, double baseEnergyCap,double baseEnergyRegenRate) {
        this(energy,baseEnergyCap,baseEnergyRegenRate,false);
    }
    public ChunkQiContainer(double energy, double baseEnergyCap,double baseEnergyRegenRate,boolean loaded){
        this.energy = energy;
        this.energyCap = new ValueContainer(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"energy_cap"),baseEnergyCap);
        this.energyRegenRate = new ValueContainer(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"energy_cap"),baseEnergyRegenRate);
        this.loaded = loaded;
    }

    public static ChunkQiContainer getContainer(ChunkAccess access){
        ChunkQiContainer container = access.getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER);
        return null;
    }

    public void addAffinity(Identifier path, double val) {
        affinities.addBonus(PathEffectValueUtil.AFFINITY_CATEGORY,path,val);
    }
    public void addAffinityModifier(Identifier path, ValueContainerModifier modifier) {
        affinities.addBonusModifier(PathEffectValueUtil.AFFINITY_CATEGORY,path,modifier);
    }
    public void removeAffinityModifier(Identifier path,Identifier modifier) {
        affinities.removeBonusModifier(PathEffectValueUtil.AFFINITY_CATEGORY,path,modifier);
    }

    public void addEnergyCapModifier(ValueContainerModifier modifier) {}
    public void removeEnergyCapModifier(Identifier modifier) {}

    public void addEnergyRegenRateModifier(ValueContainerModifier modifier) {}
    public void removeEnergyRegenRateModifier(Identifier modifier) {}

    public double getEnergy() {
        return energy;
    }
    public double getEnergyCap(){return energyCap.getValue();}
    public double getEnergyRegenRate(){return energyRegenRate.getValue();}

    public void regenEnergy(){
        energy = Math.min(energyCap.getValue(), energyRegenRate.getValue()+energy);
    }

    @Override
    public ValueContainer getPathBonusContainer(Identifier category, Identifier path) {
        return affinities.getPathBonusContainer(category,path);
    }

    @Override
    public double getPathBonus(Identifier category, Identifier path) {
        return affinities.getBonus(category,path);
    }

    @Override
    public Collection<PathBonus> getAllPathBonuses() {
        return affinities.getAllPathBonuses();
    }

    @Override
    public Collection<Identifier> getAllPathBonusesInCategory(Identifier category) {
        return affinities.getAllPathBonusesInCategory(category);
    }


    public static class SyncHandler implements AttachmentSyncHandler<ChunkQiContainer> {

        @Override
        public void write(RegistryFriendlyByteBuf buf, ChunkQiContainer attachment, boolean initialSync) {
            buf.writeDouble(attachment.energy);
      }

        @Override
        public @Nullable ChunkQiContainer read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable ChunkQiContainer previousValue) {
            //client does not handle any regeneration or cap checks
            if(previousValue == null)previousValue = new ChunkQiContainer(0,
                    0.0,
                   0.0);

            previousValue.energy = buf.readDouble();

            return previousValue;
        }
    }

    public static class Provider implements IAttachmentSerializer<ChunkQiContainer> {


        @Override
        public ChunkQiContainer read(IAttachmentHolder holder, ValueInput input) {
            double energy = input.getDoubleOr("energy",0);

            return new ChunkQiContainer(energy, 0, 0);
        }

        @Override
        public boolean write(ChunkQiContainer attachment, ValueOutput output) {
            output.putDouble("energy",attachment.energy);
            return true;
        }
    }

}
