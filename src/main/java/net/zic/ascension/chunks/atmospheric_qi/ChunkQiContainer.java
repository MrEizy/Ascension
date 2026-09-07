package net.zic.ascension.chunks.atmospheric_qi;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.ascension.AscensionCraft;

import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.MultiSourcePathBonusHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.util.PathInteractionUtil;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Holds the qi of a chunk.
 * Qi is either pure or typed
 * TODO split qi and affinity into 2 separate attachments
 */
public class ChunkQiContainer implements PathBonusProvider {
    private double energy;
    final ValueContainer<Double> energyRegenRate;
    final ValueContainer<Double> energyCap;

    private boolean loaded;

    private final PathBonusHolder affinities = new PathBonusHolder();
    public ChunkQiContainer(double energy, double baseEnergyCap,double baseEnergyRegenRate) {
        this(energy,baseEnergyCap,baseEnergyRegenRate,false);
    }
    public ChunkQiContainer(double energy, double baseEnergyCap,double baseEnergyRegenRate,boolean loaded){
        this.energy = energy;
        this.energyCap = ValueContainerHelpers.doubleValueContainer(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"energy_cap"),baseEnergyCap);
        this.energyRegenRate = ValueContainerHelpers.doubleValueContainer(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"regen_rate"),baseEnergyRegenRate);
        this.loaded = loaded;
    }

    public static ChunkQiContainer getContainer(ChunkAccess access){
        return access.getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER);
    }
    public void addFlatAffinityModifier(Identifier path, Modifier<Double> modifier) {
        affinities.addFlatModifier(PathInteractionUtil.AFFINITY_CATEGORY,path,modifier);
    }
    public void addMultiplierAffinityModifier(Identifier path, Modifier<Double> modifier) {
        affinities.addMultiplierModifier(PathInteractionUtil.AFFINITY_CATEGORY,path,modifier);
    }

    public void removeAffinityModifier(Identifier path,Identifier modifier) {
        affinities.removeModifier(PathInteractionUtil.AFFINITY_CATEGORY,path,modifier);
    }
    //TODO ACTUALLY IMPLEMENT THESE AFTER THE MERGE IS DONE
    public void addFlatEnergyCapModifier(Modifier<Double> modifier) {}
     public void removeEnergyCapModifier(Identifier modifier) {}

    public void addEnergyRegenRateModifier(Modifier<Double> modifier) {}
    public void removeEnergyRegenRateModifier(Identifier modifier) {}

    public double getEnergy() {
        return energy;
    }

    public boolean tryConsumeEnergy(double amount) {
        if (!Double.isFinite(amount) || amount < 0.0D) {
            throw new IllegalArgumentException("Atmospheric Qi consumption must be a finite non-negative number");
        }
        if (amount == 0.0D) {
            return true;
        }
        if (energy + 1.0E-9D < amount) {
            return false;
        }

        energy = Math.max(0.0D, energy - amount);
        return true;
    }
    public double getEnergyCap(){return energyCap.getValue();}
    public double getEnergyRegenRate(){return energyRegenRate.getValue();}

    public double getEnergyFraction() {
        double cap = getEnergyCap();
        if (cap <= 0.0D) return 0.0D;
        return Mth.clamp(energy / cap, 0.0D, 1.0D);
    }

    public double getAffinity(Identifier path) {
        double direct = affinities.getPathBonus(PathInteractionUtil.AFFINITY_CATEGORY, path);
        if (direct != 0.0D) {
            return direct;
        }

        String pathName = path.getPath();
        int slash = pathName.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < pathName.length()) {
            Identifier shorthand = Identifier.fromNamespaceAndPath(
                    path.getNamespace(),
                    pathName.substring(slash + 1)
            );
            return affinities.getPathBonus(PathInteractionUtil.AFFINITY_CATEGORY, shorthand);
        }

        return direct;
    }

    public ValueContainer<Double> getAffinityContainer(Identifier path) {
        return affinities.getContainer(PathInteractionUtil.AFFINITY_CATEGORY, path);
    }

    public Collection<Identifier> getAllAffinities() {
        return affinities.getPathBonuses().stream().filter(bonus->bonus.category().equals(PathInteractionUtil.AFFINITY_CATEGORY)).map(PathBonus::path).collect(Collectors.toSet());
    }

    public boolean hasAtmosphericConfiguration() {
        return getEnergyCap() > 0.0D || !getAllAffinities().isEmpty();
    }

    public void regenEnergy(){
        energy = Math.min(energyCap.getValue(), energyRegenRate.getValue()+energy);
    }

    @Override
    public ValueContainer<Double> getPathBonusContainer(Identifier category, Identifier path) {
        return affinities.getContainer(category,path);
    }

    @Override
    public double getPathBonus(Identifier category, Identifier path) {
        return affinities.getPathBonus(category,path);
    }

    @Override
    public Collection<PathBonus> getAllPathBonuses() {
        return affinities.getPathBonuses();
    }

    @Override
    public Collection<Identifier> getAllPathBonusesInCategory(Identifier category) {
        return getAllPathBonuses().stream().filter(bonus->bonus.category().equals(category)).map(PathBonus::path).collect(Collectors.toSet());

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
