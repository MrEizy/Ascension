package net.zic.ascension.api.ascension.datapack.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.requirement.Requirement;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

public abstract class RequirementType {
    public abstract MapCodec<? extends Requirement> codec();

    public static final Codec<Requirement> REQUIREMENT_CODEC = TypeRegistries.REQUIREMENT_TYPE_REGISTRY.byNameCodec()
            .dispatch(Requirement::getType, RequirementType::codec);
}
