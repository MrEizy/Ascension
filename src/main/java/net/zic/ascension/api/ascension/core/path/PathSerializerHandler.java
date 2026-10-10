package net.zic.ascension.api.ascension.core.path;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.zenithlib.nbt.NbtHelpers;

public class PathSerializerHandler implements SerializerHandler<PathHolder> {
    @Override
    public PathHolder read(ValueInput input, RegistryAccess access) {
        PathHolder holder = new PathHolder();

        ValueInput.ValueInputList pathsInput = input.childrenListOrEmpty("paths");
        for(ValueInput pathInput : pathsInput){
            try {
                Identifier pathId = NbtHelpers.readIdentifier(pathInput,"path");
                Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,pathId,access);
                if(path == null) continue;

                PathInstance data = pathInput.child("data")
                        .map(valueInput -> path.loadInstance(valueInput,access))
                        .orElse(path.newInstance(access));
                holder.addCachedPath(pathId,data);
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
        for(Identifier path : writable.getPaths()){

            try {
                ValueOutput pathOutput = paths.addChild();
                NbtHelpers.writeIdentifier(pathOutput,"path",path);

                if(writable.getPath(path) != null) writable.getPath(path).write(pathOutput.child("data"),access);

            }catch (Exception e){
                AscensionCraft.LOGGER.debug("Error writing path {}",path);
                AscensionCraft.LOGGER.debug("stacktrace",e);
            }
        }
    }
}
