package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.core.damage.AscensionDamageService;
import net.zic.ascension.impl.datapack.skill.AscensionSkillExecutionFeatureTypes;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public record DamageFeature(
        ScaledValue amount,
        Identifier damageType,
        List<Identifier> classifications,
        Optional<Identifier> path,
        Optional<Identifier> technique
) implements SkillExecutionFeature {
    private static final Identifier DEFAULT_DAMAGE_TYPE = Identifier.fromNamespaceAndPath(
            "minecraft",
            "player_attack"
    );

    public static final MapCodec<DamageFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("amount").forGetter(DamageFeature::amount),
            Identifier.CODEC.optionalFieldOf("damage_type", DEFAULT_DAMAGE_TYPE).forGetter(DamageFeature::damageType),
            Identifier.CODEC.listOf().optionalFieldOf("classifications", List.of()).forGetter(DamageFeature::classifications),
            Identifier.CODEC.optionalFieldOf("path").forGetter(DamageFeature::path),
            Identifier.CODEC.optionalFieldOf("technique").forGetter(DamageFeature::technique)
    ).apply(instance, DamageFeature::new));

    public DamageFeature {
        classifications = classifications == null ? List.of() : List.copyOf(classifications);
        path = path == null ? Optional.empty() : path;
        technique = technique == null ? Optional.empty() : technique;
    }

    @Override
    public CodecType<SkillExecutionFeature> getType() {
        return AscensionSkillExecutionFeatureTypes.DAMAGE.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        if (context.target() == null) {
            return;
        }
        AscensionDamageService.apply(context, amount.resolve(context.scaledValueContext()), damageType, new LinkedHashSet<>(classifications), path, technique);
    }
}
