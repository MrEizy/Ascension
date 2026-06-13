package net.zic.ascension.impl.datapack.path;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.datapack.path.PathType;
import net.zic.ascension.impl.core.path.PathRelationship;
import net.zic.ascension.impl.core.path.SimplePath;

import java.util.List;

public class SimplePathType extends PathType {
    @Override
    public MapCodec<? extends Path> codec() {
        return RecordCodecBuilder.<SimplePath>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimplePath::name),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(SimplePath::description),
                        SimplePath.MajorRealm.CODEC.listOf().fieldOf("realms").forGetter(SimplePath::realms),
                        PathRelationship.CODEC.listOf().optionalFieldOf("interactions", List.of()).forGetter(SimplePath::pathRelationships)
                ).apply(instance, SimplePath::new)
        );
    }
}
