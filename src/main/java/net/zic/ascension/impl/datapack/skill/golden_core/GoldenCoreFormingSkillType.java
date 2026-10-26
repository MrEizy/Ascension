package net.zic.ascension.impl.datapack.skill.golden_core;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.castable.GoldenCoreFormingSkill;

public final class GoldenCoreFormingSkillType extends SkillType {
    @Override
    public MapCodec<? extends Skill<?>> codec() {
        return RecordCodecBuilder.<GoldenCoreFormingSkill>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(GoldenCoreFormingSkill::getName),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(GoldenCoreFormingSkill::getDescription),
                Identifier.CODEC.fieldOf("golden_core").forGetter(GoldenCoreFormingSkill::goldenCore),
                Identifier.CODEC.fieldOf("path").forGetter(GoldenCoreFormingSkill::path),
                Realm.CODEC.fieldOf("realm").forGetter(GoldenCoreFormingSkill::realm)
        ).apply(instance, GoldenCoreFormingSkill::new));
    }
}
