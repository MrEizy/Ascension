package net.zic.ascension.api.core.targeting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public record TargetFilterDefinition(
        boolean includeSelf,
        boolean includeAllies,
        boolean includeNeutral,
        boolean includeHostile,
        boolean includePlayers,
        boolean requireLineOfSight
) {
    public static final MapCodec<TargetFilterDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("include_self", false).forGetter(TargetFilterDefinition::includeSelf),
            Codec.BOOL.optionalFieldOf("include_allies", false).forGetter(TargetFilterDefinition::includeAllies),
            Codec.BOOL.optionalFieldOf("include_neutral", true).forGetter(TargetFilterDefinition::includeNeutral),
            Codec.BOOL.optionalFieldOf("include_hostile", true).forGetter(TargetFilterDefinition::includeHostile),
            Codec.BOOL.optionalFieldOf("include_players", true).forGetter(TargetFilterDefinition::includePlayers),
            Codec.BOOL.optionalFieldOf("require_line_of_sight", true).forGetter(TargetFilterDefinition::requireLineOfSight)
    ).apply(instance, TargetFilterDefinition::new));

    public static TargetFilterDefinition hostile() {
        return new TargetFilterDefinition(false, false, false, true, true, true);
    }

    public boolean matches(LivingEntity caster, LivingEntity target) {
        if (target == null || target.isRemoved() || !target.isAlive()) {
            return false;
        }
        if (target == caster) {
            return includeSelf;
        }
        if (target instanceof Player && !includePlayers) {
            return false;
        }
        if (caster.isAlliedTo(target)) {
            return includeAllies;
        }
        boolean hostile = target instanceof Enemy
                || target instanceof Mob mob && mob.getTarget() == caster
                || caster.getLastHurtMob() == target
                || caster.getLastHurtByMob() == target;
        return hostile ? includeHostile : includeNeutral;
    }
}
