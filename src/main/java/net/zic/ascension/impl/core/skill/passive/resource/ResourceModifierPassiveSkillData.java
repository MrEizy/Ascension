package net.zic.ascension.impl.core.skill.passive.resource;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.skill.levelled.LevelledSkillData;
import net.zic.ascension.api.core.skill.levelled.SkillProgressionData;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;

public final class ResourceModifierPassiveSkillData implements LevelledSkillData {
    private final SkillProgressionData progression;

    public ResourceModifierPassiveSkillData(SkillProgressionData progression) {
        this.progression = progression == null ? new SkillProgressionData() : progression;
    }

    public ResourceModifierPassiveSkillData(ValueInput input) {
        this(new SkillProgressionData(input.childOrEmpty("progression")));
    }

    public ResourceModifierPassiveSkillData(ByteBuf buf) {
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
        return AscensionSkillTypes.RESOURCE_MODIFIER_PASSIVE_SKILL_TYPE.get();
    }
}
