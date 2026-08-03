package net.zic.ascension.impl.value.source;

import net.zic.ascension.api.ascension.datapack.CodecType;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.skill.levelled.SkillLevelResolver;
import net.zic.ascension.api.ascension.value.ScaledValueContext;
import net.zic.ascension.api.ascension.value.ScaledValueSource;

import java.util.Optional;

public record SkillLevelScaledValueSource(Optional<Identifier> skill) implements ScaledValueSource {
    public static final MapCodec<SkillLevelScaledValueSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("skill").forGetter(SkillLevelScaledValueSource::skill)
    ).apply(instance, SkillLevelScaledValueSource::new));

    @Override
    public CodecType<ScaledValueSource> getType() {
        return AscensionScaledValueSourceTypes.SKILL_LEVEL.get();
    }

    @Override
    public double resolve(ScaledValueContext context) {
        Identifier skillId = skill.orElse(context.skill());
        if (context.source() == null || skillId == null) {
            return 0.0D;
        }
        return SkillLevelResolver.resolve(context.source(), skillId).effectiveLevel();
    }
}
