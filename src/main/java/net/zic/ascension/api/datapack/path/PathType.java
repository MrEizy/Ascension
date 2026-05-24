package net.zic.ascension.api.datapack.path;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.physique.PhysiqueType;

public abstract class PathType{
    public abstract MapCodec<? extends Path> codec();


    public static Codec<Path> PATH_CODEC = TypeRegistries.PATH_TYPE_REGISTRY.byNameCodec()
            .dispatch(
                    Path::getType,
                    PathType::codec
            );
}
