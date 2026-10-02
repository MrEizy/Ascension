package net.zic.ascension.impl.datapack.path;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.datapack.path.PathType;
import net.zic.ascension.impl.core.path.realms.MajorRealmDefinition;

import net.zic.ascension.impl.core.path.simple.SimplePath;

import java.util.List;
import java.util.UUID;

public class SimplePathType extends PathType {
    @Override
    public MapCodec<? extends Path> codec() {
        return RecordCodecBuilder.<SimplePath>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimplePath::name),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(SimplePath::description),
                        MajorRealmDefinition.CODEC.listOf().fieldOf("realms").forGetter(SimplePath::getMajorRealmDefinitions),
                        ProgressActionHolder.PROGRESS_HOLDER_CODEC.optionalFieldOf("actions",new ProgressActionHolder(UUID.randomUUID(),List.of())).forGetter(SimplePath::getProgressActionHolder)
                  ).apply(instance, SimplePath::new)
        );
    }
}
