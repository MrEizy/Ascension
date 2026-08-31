package net.zic.ascension.api.ascension.core.skill.passive;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillCostDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.SkillCondition;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;

import java.util.List;
import java.util.Optional;

public record PassiveTrigger(
        Event event,
        Optional<Identifier> weaponTag,
        boolean allowEmptyHand,
        int cooldown,
        int priority,
        List<ActiveSkillCostDefinition> costs,
        List<SkillCondition> conditions,
        List<SkillAction> actions
) {
    public static final Codec<PassiveTrigger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Event.CODEC.fieldOf("event").forGetter(PassiveTrigger::event),
            Identifier.CODEC.optionalFieldOf("weapon_tag").forGetter(PassiveTrigger::weaponTag),
            Codec.BOOL.optionalFieldOf("allow_empty_hand", false).forGetter(PassiveTrigger::allowEmptyHand),
            Codec.intRange(0, 72000).optionalFieldOf("cooldown", 0).forGetter(PassiveTrigger::cooldown),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(PassiveTrigger::priority),
            ActiveSkillCostDefinition.LIST_CODEC.optionalFieldOf("cost", List.of()).forGetter(PassiveTrigger::costs),
            SkillCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(PassiveTrigger::conditions),
            SkillAction.CODEC.listOf().optionalFieldOf("actions", List.of()).forGetter(PassiveTrigger::actions)
    ).apply(instance, PassiveTrigger::new));

    public PassiveTrigger {
        event = event == null ? Event.ATTACK : event;
        weaponTag = weaponTag == null ? Optional.empty() : weaponTag;
        cooldown = Math.max(0, cooldown);
        costs = costs == null ? List.of() : List.copyOf(costs);
        conditions = conditions == null ? List.of() : List.copyOf(conditions);
        actions = actions == null ? List.of() : List.copyOf(actions);
    }

    public enum Event implements StringRepresentable {
        ATTACK("attack"),
        DAMAGE_DEALT("damage_dealt"),
        DAMAGE_TAKEN("damage_taken"),
        KILL("kill"),
        SKILL_CAST("skill_cast"),
        RESOURCE_CHANGED("resource_changed");

        public static final Codec<Event> CODEC = StringRepresentable.fromEnum(Event::values);
        private final String name;

        Event(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
