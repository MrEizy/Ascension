package net.zic.ascension.api.ascension.core.qi_coating;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveSkillCostDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.SkillCondition;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;

import java.util.List;
import java.util.Optional;

/**
 * An element the Qi Coating skills can be switched to, loaded from
 * {@code data/<namespace>/ascension/skill_system/qi_coating_elements/}. The player needs {@code path} to pick it (an element
 * without one, like Pure Qi, is open to every cultivator), and it shows
 * {@code body_visual} through Qi Coating and {@code external_visual} through External Qi Coating (runtime visual ids, as for
 * {@code ascension:persistent_visual}). An element without a visual for a channel isn't offered there.
 * <p>
 * The hooks are what the element does in combat while it is shown (QiCoatingService runs them): {@code body_on_hit} when the
 * player lands a bare-handed melee hit, {@code body_on_hurt} when an attacker hurts them, and {@code external_on_hit} when they
 * land a melee hit with a weapon External Qi Coating shows on.
 *
 * @param name           the name shown in the coating wheel
 * @param path           the path (Dao) the player needs, or empty if anyone can pick it
 * @param bodyVisual     the runtime visual Qi Coating shows
 * @param externalVisual the runtime visual External Qi Coating shows
 * @param order          where it sits in the wheel, lowest first
 * @param bodyOnHit      run on the entity hit, for a bare-handed melee hit while Qi Coating shows this element
 * @param bodyOnHurt     run on the attacker, when the player is hurt while Qi Coating shows this element
 * @param externalOnHit  run on the entity hit, for a melee hit with a coated weapon while External Qi Coating shows this element
 */
public record QiCoatingElement(
        Component name,
        Optional<Identifier> path,
        Optional<Identifier> bodyVisual,
        Optional<Identifier> externalVisual,
        int order,
        Optional<Hook> bodyOnHit,
        Optional<Hook> bodyOnHurt,
        Optional<Hook> externalOnHit
) {
    public static final Codec<QiCoatingElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(QiCoatingElement::name),
            Identifier.CODEC.optionalFieldOf("path").forGetter(QiCoatingElement::path),
            Identifier.CODEC.optionalFieldOf("body_visual").forGetter(QiCoatingElement::bodyVisual),
            Identifier.CODEC.optionalFieldOf("external_visual").forGetter(QiCoatingElement::externalVisual),
            Codec.INT.optionalFieldOf("order", 0).forGetter(QiCoatingElement::order),
            Hook.CODEC.optionalFieldOf("body_on_hit").forGetter(QiCoatingElement::bodyOnHit),
            Hook.CODEC.optionalFieldOf("body_on_hurt").forGetter(QiCoatingElement::bodyOnHurt),
            Hook.CODEC.optionalFieldOf("external_on_hit").forGetter(QiCoatingElement::externalOnHit)
    ).apply(instance, QiCoatingElement::new));

    public Optional<Identifier> visual(QiCoatingChannel channel) {
        return channel == QiCoatingChannel.BODY ? bodyVisual : externalVisual;
    }

    /**
     * What an element does when its hook fires: like a passive skill's trigger without the event, which the hook itself decides.
     *
     * @param cooldown   ticks before it can fire again; 0 fires on every hit
     * @param costs      paid each time it fires; it doesn't fire if the player can't pay
     * @param conditions all must pass for it to fire
     * @param actions    skill actions run with the entity hit (or the attacker, for body_on_hurt) as the target
     */
    public record Hook(int cooldown, List<ActiveSkillCostDefinition> costs, List<SkillCondition> conditions, List<SkillAction> actions) {
        public static final Codec<Hook> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 72000).optionalFieldOf("cooldown", 0).forGetter(Hook::cooldown),
                ActiveSkillCostDefinition.LIST_CODEC.optionalFieldOf("cost", List.of()).forGetter(Hook::costs),
                SkillCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(Hook::conditions),
                SkillAction.CODEC.listOf().fieldOf("actions").forGetter(Hook::actions)
        ).apply(instance, Hook::new));

        public Hook {
            costs = List.copyOf(costs);
            conditions = List.copyOf(conditions);
            actions = List.copyOf(actions);
        }
    }
}
