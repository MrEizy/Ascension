package net.zic.ascension.api.ascension.core.technique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;

public class TechniqueSerializerHandler implements SerializerHandler<TechniqueHolder> {
    @Override
    public TechniqueHolder read(ValueInput input, RegistryAccess access) {
        TechniqueHolder holder = new TechniqueHolder();
        ValueInput.ValueInputList techniquesInput = input.childrenListOrEmpty("techniques");
        for (ValueInput techniqueInput : techniquesInput) {
            try {
                TechniqueInstance<?> instance = TechniqueInstance.read(techniqueInput, access);
                if (instance == null) continue;
                holder.addTechnique(instance);
            } catch (Exception exception) {
                AscensionCraft.LOGGER.debug("Error loading technique");
                AscensionCraft.LOGGER.debug("stacktrace: ", exception);
            }
        }
        return holder;
    }

    @Override
    public void write(TechniqueHolder writable, ValueOutput output, RegistryAccess access) {
        ValueOutput.ValueOutputList techniqueOutputList = output.childrenList("techniques");
        for (TechniqueInstance<?> instance : writable.getTechniqueInstances()) {
            try {
                instance.write(techniqueOutputList.addChild(), access);
            } catch (Exception exception) {
                AscensionCraft.LOGGER.error("Error writing technique {}", instance.getTechniqueId());
                AscensionCraft.LOGGER.error("stacktrace: ", exception);
            }
        }
    }
}
