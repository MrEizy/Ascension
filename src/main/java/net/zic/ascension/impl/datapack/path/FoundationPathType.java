package net.zic.ascension.impl.datapack.path;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.datapack.path.PathType;
import net.zic.ascension.impl.core.path.PathRelationship;
import net.zic.ascension.impl.core.path.foundation.FoundationMajorRealmDefinition;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;

import java.util.List;

public class FoundationPathType extends PathType {
    @Override
    public MapCodec<? extends Path> codec() {
        return RecordCodecBuilder.<FoundationPath>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(FoundationPath::name),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(FoundationPath::description),
                        FoundationMajorRealmDefinition.CODEC.listOf().fieldOf("realms").forGetter(FoundationPath::realms),
                        PathRelationship.CODEC.listOf().optionalFieldOf("interactions", List.of()).forGetter(FoundationPath::pathRelationships)
                ).apply(instance, FoundationPath::new)
        );
    }
}