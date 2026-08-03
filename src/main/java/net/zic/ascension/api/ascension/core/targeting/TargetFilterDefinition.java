package net.zic.ascension.api.ascension.core.targeting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;

public record TargetFilterDefinition(
        Set<Relation> relations,
        boolean includePlayers,
        boolean requireLineOfSight
) {
    private static final Codec<Set<Relation>> RELATIONS_CODEC = Relation.CODEC.listOf().xmap(
            Set::copyOf,
            List::copyOf
    );

    public static final MapCodec<TargetFilterDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RELATIONS_CODEC.optionalFieldOf("relations", Set.of(Relation.NEUTRAL, Relation.HOSTILE))
                    .forGetter(TargetFilterDefinition::relations),
            Codec.BOOL.optionalFieldOf("include_players", true).forGetter(TargetFilterDefinition::includePlayers),
            Codec.BOOL.optionalFieldOf("line_of_sight", true).forGetter(TargetFilterDefinition::requireLineOfSight)
    ).apply(instance, TargetFilterDefinition::new));

    public TargetFilterDefinition {
        relations = relations == null ? Set.of() : Set.copyOf(relations);
    }

    public static TargetFilterDefinition hostile() {
        return new TargetFilterDefinition(Set.of(Relation.HOSTILE), true, true);
    }

    public boolean matches(LivingEntity caster, LivingEntity target) {
        if (target == null || target.isRemoved() || !target.isAlive()) {
            return false;
        }
        if (target instanceof Player && !includePlayers) {
            return false;
        }
        return relations.contains(relation(caster, target));
    }

    public Relation relation(LivingEntity caster, LivingEntity target) {
        if (target == caster) {
            return Relation.SELF;
        }
        if (caster.isAlliedTo(target)) {
            return Relation.ALLY;
        }
        boolean hostile = target instanceof Enemy
                || target instanceof Mob mob && mob.getTarget() == caster
                || caster.getLastHurtMob() == target
                || caster.getLastHurtByMob() == target;
        return hostile ? Relation.HOSTILE : Relation.NEUTRAL;
    }

    public enum Relation implements StringRepresentable {
        SELF("self"),
        ALLY("ally"),
        NEUTRAL("neutral"),
        HOSTILE("hostile");

        public static final Codec<Relation> CODEC = StringRepresentable.fromEnum(Relation::values);

        private final String name;

        Relation(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
