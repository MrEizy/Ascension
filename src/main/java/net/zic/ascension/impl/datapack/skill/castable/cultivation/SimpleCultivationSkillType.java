package net.zic.ascension.impl.datapack.skill.castable.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.ascension.core.skill.particle_field.ParticleFieldDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.CastSoundDefinition;
import net.zic.ascension.impl.core.skill.castable.cultivation.SimpleCultivationSkill;
import net.zic.ascension.impl.core.skill.castable.cultivation.SimpleCultivationSkillData;

import java.util.List;
import java.util.Optional;

public class SimpleCultivationSkillType extends SkillType {
    @Override
    public MapCodec<? extends Skill> codec() {
        return RecordCodecBuilder.<SimpleCultivationSkill>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimpleCultivationSkill::name),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(SimpleCultivationSkill::description),
                        Identifier.CODEC.fieldOf("path").forGetter(SimpleCultivationSkill::primaryPath),
                        Identifier.CODEC.optionalFieldOf("secondary_paths").forGetter((obj)-> Optional.of(obj.secondaryPath())),
                        Codec.DOUBLE.fieldOf("rate").forGetter(SimpleCultivationSkill::baseRate),
                        ParticleFieldDefinition.CODEC.optionalFieldOf("particle_field").forGetter(SimpleCultivationSkill::particleField)
                ).apply(instance, (name,description,path,secondaryPath,rate,particleField)->
                        new SimpleCultivationSkill(name,description,path,secondaryPath.orElse(path),rate,particleField))
        );
    }

    @Override
    public MapCodec<? extends SkillData> dataCodec() {
        return MapCodec.unit(SimpleCultivationSkillData::new);
    }
}
