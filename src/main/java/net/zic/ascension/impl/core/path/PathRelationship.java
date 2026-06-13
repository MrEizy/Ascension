package net.zic.ascension.impl.core.path;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.path.interactions.PathInteraction;
import net.zic.ascension.api.core.path.interactions.PathInteractionType;

public record PathRelationship(Identifier path, String target, PathInteractionType type,double value){
    public static final Codec<PathRelationship> CODEC = RecordCodecBuilder.create(
            instance->
                    instance.group(
                            Identifier.CODEC.fieldOf("path").forGetter(PathRelationship::path),
                            Codec.STRING.validate(
                                    target->switch (target){
                                        case "self", "other" -> DataResult.success(target);
                                        default -> DataResult.error(
                                                ()->"target can only be self or other"
                                        );
                                    }
                            ).optionalFieldOf("target","self").forGetter(PathRelationship::target),
                            Codec.STRING.comapFlatMap(
                                    type -> {
                                        try {
                                            return DataResult.success(
                                                    PathInteractionType.valueOf(type.toUpperCase())
                                            );
                                        } catch (IllegalArgumentException e) {
                                            return DataResult.<PathInteractionType>error(() ->
                                                    "Unknown target type: " + type);
                                        }
                                    },
                                    PathInteractionType::name
                            ).fieldOf("type").forGetter(PathRelationship::type),
                            Codec.DOUBLE.fieldOf("value").forGetter(PathRelationship::value)
                    )
                            .apply(instance,PathRelationship::new)
    );

    public PathInteraction asInteraction(Identifier selfPath){
        return new PathInteraction(target.equals("self") ?path:selfPath,target.equals("self")?selfPath:path,type,value);
    }
}
