package net.zic.ascension.impl.datapack.path;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.datapack.path.PathType;
import net.zic.ascension.impl.core.path.PathRelationship;
import net.zic.ascension.impl.core.path.MajorRealmDefinition;
import net.zic.ascension.impl.core.path.simple.SimplePath;

import java.util.List;

public class SimplePathType extends PathType {
    @Override
    public MapCodec<? extends Path> codec() {
        return RecordCodecBuilder.<SimplePath>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimplePath::name),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(SimplePath::description),
                        MajorRealmDefinition.CODEC.listOf().fieldOf("realms").forGetter(SimplePath::realms),
                        PathRelationship.CODEC.listOf().optionalFieldOf("interactions", List.of()).forGetter(SimplePath::pathRelationships)
                ).apply(instance, SimplePath::new)
        );
    }
}
