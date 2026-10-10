package net.zic.ascension.api.ascension.core.bloodline;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.zenithlib.nbt.NbtHelpers;

public class BloodlineSerializerHandler implements SerializerHandler<BloodlineHolder> {
    @Override
    public BloodlineHolder read(ValueInput input, RegistryAccess access) {
        BloodlineHolder holder = new BloodlineHolder();
        try {
            ValueInput.ValueInputList bloodlinesInput = input.childrenListOrEmpty("bloodlines");

            for(ValueInput bloodlineInput : bloodlinesInput.stream().toList()){
                try {
                    Identifier id = NbtHelpers.readIdentifier(bloodlineInput,"id");
                    Bloodline bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,id,access);
                    if(bloodline == null) throw new Exception("bloodline "+id+" does not exist");

                    BloodlineData bloodlineData = bloodlineInput.child("data")
                            .map(valueInput -> bloodline.loadData(valueInput,access))
                            .orElse(bloodline.newData(access));

                    holder.addBloodline(id,bloodlineData);
                } catch (Exception e){
                    AscensionCraft.LOGGER.error("error loading bloodline");
                    AscensionCraft.LOGGER.error("stacktrace : ",e);
                }

            }
        } catch (Exception e){
            AscensionCraft.LOGGER.error("Error loading all bloodlines");
            AscensionCraft.LOGGER.error("stacktrace : ",e);
        }
        return holder;
    }

    @Override
    public void write(BloodlineHolder writable, ValueOutput output, RegistryAccess access) {
        ValueOutput.ValueOutputList bloodlineOutputList = output.childrenList("bloodlines");
        for(Identifier bloodline : writable.getBloodlines()){
            AscensionCraft.LOGGER.debug("Writing Bloodline {}",bloodline);
            try{
                ValueOutput bloodlineOutput = bloodlineOutputList.addChild();
                NbtHelpers.writeIdentifier(bloodlineOutput,"id",bloodline);
                ValueOutput dataOutput = bloodlineOutput.child("data");
                writable.getBloodline(bloodline).write(dataOutput,access);
            } catch (Exception e){
                AscensionCraft.LOGGER.debug("Error writing bloodline {}",bloodline);
                AscensionCraft.LOGGER.debug("stacktrace: ",e);
            }

        }
    }
}
