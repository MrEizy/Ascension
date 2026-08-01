package net.zic.ascension.impl.datapack.skill.castable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.castable.DebugCastable;

import java.util.UUID;

public class DebugCastableType extends SkillType {
    @Override
    public MapCodec<? extends Skill> codec() {
        return RecordCodecBuilder.<DebugCastable>mapCodec(instance ->
                instance.group(
                        Codec.STRING.fieldOf("message").forGetter(DebugCastable::message),
                        Codec.INT.optionalFieldOf("cooldown",0).forGetter(DebugCastable::cooldown)
                ).apply(instance, (message,cooldown)->new DebugCastable(message,cooldown, UUID.randomUUID()))
        );
    }

    @Override
    public MapCodec<? extends SkillData> dataCodec() {
        return null;
    }

}
