package net.zic.ascension.api.ascension.core.path;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;

public class PathSerializerHandler implements SerializerHandler<PathHolder> {
    @Override
    public PathHolder read(ValueInput input, RegistryAccess access) {
        PathHolder holder = new PathHolder();

        //paths are cached until their owners add them again
        ValueInput.ValueInputList pathsInput = input.childrenListOrEmpty("paths");
        for(ValueInput pathInput : pathsInput){
            try {
                PathInstance<?> instance = PathInstance.read(pathInput,access);
                if(instance == null) continue;
                holder.addCachedPath(instance);
            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error loading path");
                AscensionCraft.LOGGER.debug("stacktrace: ",e);
            }
        }
        return holder;
    }

    @Override
    public void write(PathHolder writable, ValueOutput output, RegistryAccess access) {
        ValueOutput.ValueOutputList paths = output.childrenList("paths");
        for(PathInstance<?> instance : writable.getPathInstances()){
            try {
                instance.write(paths.addChild(),access);
            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error writing path {}",instance.getPathId());
                AscensionCraft.LOGGER.debug("stacktrace",e);
            }
        }
    }
}
