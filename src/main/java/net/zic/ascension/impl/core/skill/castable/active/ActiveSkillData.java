package net.zic.ascension.impl.core.skill.castable.active;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.skill.levelled.LevelledSkillData;
import net.zic.ascension.api.core.skill.levelled.SkillProgressionData;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;

public final class ActiveSkillData implements LevelledSkillData {
    private final SkillProgressionData progression;

    public ActiveSkillData(SkillProgressionData progression) {
        this.progression = progression == null ? new SkillProgressionData() : progression;
    }

    public ActiveSkillData(ValueInput input) {
        this(new SkillProgressionData(input.childOrEmpty("progression")));
    }

    public ActiveSkillData(ByteBuf buf) {
        this(new SkillProgressionData(buf));
    }

    @Override
    public SkillProgressionData getSkillProgression() {
        return progression;
    }

    @Override
    public void write(ValueOutput output) {
        progression.write(output.child("progression"));
    }

    @Override
    public void encode(ByteBuf buf) {
        progression.encode(buf);
    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.ACTIVE_SKILL_TYPE.get();
    }
}
