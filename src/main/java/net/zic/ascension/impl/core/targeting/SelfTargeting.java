package net.zic.ascension.impl.core.targeting;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.targeting.SkillTarget;
import net.zic.ascension.api.ascension.core.targeting.TargetingContext;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.zic.ascension.api.ascension.core.targeting.TargetingResult;
import net.zic.ascension.impl.datapack.targeting.AscensionTargetingTypes;

public final class SelfTargeting implements TargetingDefinition {
    public static final MapCodec<SelfTargeting> CODEC = MapCodec.unit(SelfTargeting::new);

    @Override
    public CodecType<TargetingDefinition> getType() {
        return AscensionTargetingTypes.SELF.get();
    }

    @Override
    public TargetingResult resolve(TargetingContext context) {
        return TargetingResult.success(SkillTarget.entity(context.caster()));
    }
}
