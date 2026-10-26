package net.zic.ascension.api.ascension.core.golden_core.data_source;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.golden_core.GoldenCoreInstance;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;

public class GoldenCoreSerializerHandler implements SerializerHandler<GoldenCoreHolder> {
    @Override
    public GoldenCoreHolder read(ValueInput input, RegistryAccess access) {
        GoldenCoreHolder holder = new GoldenCoreHolder();

        //cores are cached until their owners add them again
        ValueInput.ValueInputList coresInput = input.childrenListOrEmpty("golden_cores");
        for(ValueInput coreInput : coresInput){
            try {
                GoldenCoreInstance<?> instance = GoldenCoreInstance.read(coreInput,access);
                if(instance == null) continue;
                AscensionCraft.LOGGER.debug("Reading Golden Core {} ({})",instance.getCoreId(),instance.getId());
                holder.addCachedGoldenCore(instance);
            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error loading golden core");
                AscensionCraft.LOGGER.debug("stacktrace: ",e);
            }
        }
        return holder;
    }

    @Override
    public void write(GoldenCoreHolder writable, ValueOutput output, RegistryAccess access) {

        ValueOutput.ValueOutputList coreOutputList = output.childrenList("golden_cores");
        for(GoldenCoreInstance<?> instance : writable.getGoldenCoreInstances()){
            try {
                instance.write(coreOutputList.addChild(),access);
            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error writing Golden Core {} ({})",instance.getCoreId(),instance.getId());
                AscensionCraft.LOGGER.debug("stacktrace",e);
            }
        }
    }
}
