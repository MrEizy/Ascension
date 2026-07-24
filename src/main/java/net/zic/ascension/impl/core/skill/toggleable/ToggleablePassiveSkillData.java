package net.zic.ascension.impl.core.skill.toggleable;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;

public class ToggleablePassiveSkillData implements SkillData {
    private boolean enabled;

    public ToggleablePassiveSkillData(boolean enabled) {
        this.enabled = enabled;
    }

    public ToggleablePassiveSkillData(ValueInput input, boolean defaultEnabled) {
        this(input.getBooleanOr("enabled", defaultEnabled));
    }

    public ToggleablePassiveSkillData(ByteBuf buf) {
        this(buf.readBoolean());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void write(ValueOutput output) {
        output.putBoolean("enabled", enabled);
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeBoolean(enabled);
    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.TOGGLEABLE_PASSIVE_SKILL_TYPE.get();
    }
}
