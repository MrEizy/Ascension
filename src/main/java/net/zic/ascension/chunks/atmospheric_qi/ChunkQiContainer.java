package net.zic.ascension.chunks.atmospheric_qi;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.source.SourceChangesSnapshot;
import net.zic.ascension.impl.core.entity.SimpleAscensionEntityData;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the qi of a chunk.
 * Qi is either pure or typed
 *
 */
public class ChunkQiContainer {
    private double energy;
    final ValueContainer energyRegenRate;
    final ValueContainer energyCap;

    private final HashMap<Identifier,ValueContainer> affinities = new HashMap<>();

    public ChunkQiContainer(double energy, double baseEnergyCap,double baseEnergyRegenRate) {
        this.energy = energy;
        this.energyCap = new ValueContainer(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"energy_cap"),baseEnergyCap);
        this.energyRegenRate = new ValueContainer(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"energy_cap"),baseEnergyRegenRate);
    }


    public void addAffinity(Identifier path, double val) {}
    public void addAffinityModifier(Identifier path, ValueContainerModifier modifier) {}
    public void removeAffinityModifier(Identifier path,Identifier modifier) {}

    public void addEnergyCapModifier(ValueContainerModifier modifier) {}
    public void removeEnergyCapModifier(Identifier modifier) {}

    public void addEnergyRegenRateModifier(ValueContainerModifier modifier) {}
    public void removeEnergyRegenRateModifier(Identifier modifier) {}

    public double getEnergy() {
        return energy;
    }

    public void regenEnergy(){
        energy = Math.min(energyCap.getValue(), energyRegenRate.getValue()+energy);
    }

    public static class SyncHandler implements AttachmentSyncHandler<ChunkQiContainer> {

        @Override
        public void write(RegistryFriendlyByteBuf buf, ChunkQiContainer attachment, boolean initialSync) {
            buf.writeDouble(attachment.energy);
            ByteBufHelpers.encodeMap(attachment.affinities,ByteBufHelpers::encodeIdentifier,(val,byteBuf)->ValueContainer.encode(byteBuf,val),buf);
        }

        @Override
        public @Nullable ChunkQiContainer read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable ChunkQiContainer previousValue) {
            //client does not handle any regeneration or cap checks
            if(previousValue == null)previousValue = new ChunkQiContainer(0,
                    0.0,
                   0.0);

            previousValue.energy = buf.readDouble();
            Map<Identifier,ValueContainer> affinities = ByteBufHelpers.decodeMap(ByteBufHelpers::decodeIdentifier,ValueContainer::decode,buf);

            previousValue.affinities.clear();
            previousValue.affinities.putAll(affinities);

            return previousValue;
        }
    }

    public static class Provider implements IAttachmentSerializer<ChunkQiContainer> {


        @Override
        public ChunkQiContainer read(IAttachmentHolder holder, ValueInput input) {
            double energy = input.getDoubleOr("energy",0);
            double baseEnergy = input.getDoubleOr("energy_cap",0);
            double baseRegen = input.getDoubleOr("energy_regen",0);
            return new ChunkQiContainer(energy, baseEnergy, baseRegen);
        }

        @Override
        public boolean write(ChunkQiContainer attachment, ValueOutput output) {
            output.putDouble("energy",attachment.energy);
            output.putDouble("energy_cap",attachment.energyCap.getBaseValue());
            output.putDouble("energy_regen",attachment.energyRegenRate.getBaseValue());
            return true;
        }
    }

}
