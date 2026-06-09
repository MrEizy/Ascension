package net.zic.ascension.impl.datapack.skill.castable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.SimplePassiveSkill;
import net.zic.ascension.impl.core.skill.castable.DebugCastable;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;
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
}
