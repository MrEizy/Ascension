package net.zic.ascension.api.ascension.core.technique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.zenithlib.nbt.NbtHelpers;

import java.util.Optional;

public class TechniqueSerializerHandler implements SerializerHandler<TechniqueHolder> {
    @Override
    public TechniqueHolder read(ValueInput input, RegistryAccess access) {
        TechniqueHolder holder = new TechniqueHolder();
        try {
            ValueInput.ValueInputList techniquesInput = input.childrenListOrEmpty("techniques");
            for (ValueInput techniqueInput : techniquesInput.stream().toList()) {
                try {
                    Identifier techniqueId = NbtHelpers.readIdentifier(techniqueInput, "id");

                    Optional<ValueInput> dataInput = techniqueInput.child("data");
                    Technique technique = holder.getTechnique(techniqueId, access);
                    if (technique == null) {
                        throw new IllegalStateException("unknown technique " + techniqueId);
                    }
                    if (dataInput.isEmpty()) {
                        throw new IllegalStateException("no technique data present for technique " + techniqueId);
                    }
                    holder.addTechnique(techniqueId, technique.loadData(dataInput.get()));
                } catch (Exception exception) {
                    AscensionCraft.LOGGER.debug("Error loading technique");
                    AscensionCraft.LOGGER.debug("stacktrace: ", exception);
                }
            }
        } catch (Exception exception) {
            AscensionCraft.LOGGER.debug("Error loading all techniques");
            AscensionCraft.LOGGER.debug("stacktrace: ", exception);
        }
        return holder;
    }

    @Override
    public void write(TechniqueHolder writable, ValueOutput output, RegistryAccess access) {
        ValueOutput.ValueOutputList techniqueOutputList = output.childrenList("techniques");
        for (Identifier techniqueId : writable.getTechniques()) {
            try {
                ValueOutput techniqueOutput = techniqueOutputList.addChild();
                NbtHelpers.writeIdentifier(techniqueOutput, "id", techniqueId);
                writable.getTechniqueData(techniqueId).write(techniqueOutput.child("data"));
            } catch (Exception exception) {
                AscensionCraft.LOGGER.error("Error writing technique {}", techniqueId);
                AscensionCraft.LOGGER.error("stacktrace: ", exception);
            }
        }
    }
}
