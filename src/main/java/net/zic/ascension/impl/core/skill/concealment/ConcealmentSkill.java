package net.zic.ascension.impl.core.skill.concealment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.passive.PassiveSkill;
import net.zic.ascension.impl.core.skill.passive.ToggleablePassiveSkill;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;

import java.util.Optional;

public final class ConcealmentSkill extends ToggleablePassiveSkill {
    private final Identity identity;
    private final int priority;

    public ConcealmentSkill(PassiveSkill passive, Identity identity, int priority) {
        super(
                passive.getName(),
                passive.getDescription(),
                passive.getConfiguredLevels(),
                passive.getConfiguredBaseLevelCap(),
                passive.getLevelExperienceRequirements(),
                passive.modifiers(),
                passive.triggers(),
                passive.definitions(),
                passive.isEnabledByDefault(),
                passive.getUpkeep(),
                passive.stateGroup(),
                passive.grantedSkills(),
                passive.enableConditions(),
                passive.enableActions(),
                passive.disableActions()
        );
        this.identity = identity;
        this.priority = priority;
    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.CONCEALMENT_SKILL_TYPE.get();
    }

    public Identity identity() {
        return identity;
    }

    public int priority() {
        return priority;
    }

    public record Identity(Optional<Component> deathMessage) {
        public static final Identity EMPTY = new Identity(Optional.empty());
        public static final Codec<Identity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ComponentSerialization.CODEC.optionalFieldOf("death_message").forGetter(Identity::deathMessage)
        ).apply(instance, Identity::new));
    }
}
