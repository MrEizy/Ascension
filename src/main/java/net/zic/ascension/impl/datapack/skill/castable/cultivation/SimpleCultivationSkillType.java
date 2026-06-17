package net.zic.ascension.impl.datapack.skill.castable.cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.castable.DebugCastable;
import net.zic.ascension.impl.core.skill.castable.cultivation.SimpleCultivationSkill;

import java.util.List;
import java.util.UUID;

public class SimpleCultivationSkillType extends SkillType {
    @Override
    public MapCodec<? extends Skill> codec() {
        return RecordCodecBuilder.<SimpleCultivationSkill>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimpleCultivationSkill::name),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(SimpleCultivationSkill::description),
                        Identifier.CODEC.fieldOf("path").forGetter(SimpleCultivationSkill::primaryPath),
                        Identifier.CODEC.listOf().optionalFieldOf("secondary_paths", List.of()).forGetter(SimpleCultivationSkill::secondaryPaths),
                        Codec.DOUBLE.fieldOf("rate").forGetter(SimpleCultivationSkill::baseRate)
                ).apply(instance, SimpleCultivationSkill::new)
        );
    }

    @Override
    public MapCodec<? extends SkillData> dataCodec() {
        return null;
    }
}
