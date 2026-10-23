package net.zic.ascension.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteraction;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteractionHelper;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteractionType;

import java.util.function.ToDoubleFunction;

/**
 * Resolves effective affinity from path interactions.
 * <p>
 * Interactions only apply to the affinity category, every other category is a plain {@code 1 + bonus} multiplier.
 * <ol>
 *     <li>RELATED feeds a portion of its source into the base: {@code E0 = A(target) + sum(v * A(source))}</li>
 *     <li>GENERATIVE / DESTRUCTIVE each apply pressure based on the ratio between the source and the base:
 *     {@code p = v * A(source) / (E0 + C)}, {@code term = clamp(W * ln(1 + p), 0, T_MAX)}</li>
 *     <li>{@code E = E0 * exp(sum(generative) - sum(destructive))}</li>
 * </ol>
 * Terms are capped, so a single interaction can never zero (or explode) a path, and equal generative and
 * destructive pressure cancel out, which lets a suppressed path come back.
 * <p>
 * Affinities are never negative: you are not bad at fire, the presence of water smothers your fire.
 */
public class PathInteractionUtil {

    public static final Identifier AFFINITY_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"affinity");

    /** softening constant, keeps the ratio finite when the base affinity is 0 */
    public static final double C = 1.0D;
    /** weight applied to every interaction term */
    public static final double W = 1.0D;
    /** max magnitude of a single term (and of the summed collision), exp(3) ~ x20 / x0.05 */
    public static final double T_MAX = 3.0D;

    public static double getEffectiveAffinity(LivingEntity entity, Identifier path){
        return getEffectiveAffinity(entity.registryAccess(), entity.getData(CoreAttachments.PATH_BONUS_HOLDER), path);
    }

    public static double getEffectiveAffinity(RegistryAccess access, PathBonusProvider provider, Identifier path){
        return getEffectiveAffinity(access, id -> provider.getPathBonus(AFFINITY_CATEGORY, id), path);
    }

    public static double getEffectiveAffinity(RegistryAccess access, ToDoubleFunction<Identifier> affinity, Identifier path){
        double base = raw(affinity, path);
        for(PathInteraction interaction : PathInteractionHelper.getRelated(access, path)){
            base += interaction.value() * raw(affinity, interaction.source());
        }

        double pressure = 0;
        for(PathInteraction interaction : PathInteractionHelper.getGenerative(access, path)){
            pressure += term(interaction.value() * raw(affinity, interaction.source()), base);
        }
        for(PathInteraction interaction : PathInteractionHelper.getDestructive(access, path)){
            pressure -= term(interaction.value() * raw(affinity, interaction.source()), base);
        }
        return base * Math.exp(pressure);
    }

    /**
     * Plain multiplier for non affinity categories, these never interact with other paths.
     */
    public static double getPathMultiplier(PathBonusProvider provider, Identifier category, Identifier path){
        return 1 + provider.getPathBonus(category, path);
    }

    /**
     * Damage multiplier when an attack of {@code path} hits the defender.
     * Both sides are resolved independently and only compared here.
     * <ul>
     *     <li>a defender path that destroys {@code path} resists the attack</li>
     *     <li>a defender path destroyed by {@code path} is overwhelmed by the attack</li>
     * </ul>
     */
    public static double getCollisionMultiplier(LivingEntity attacker, LivingEntity defender, Identifier path){
        RegistryAccess access = attacker.registryAccess();
        PathBonusProvider defenderProvider = defender.getData(CoreAttachments.PATH_BONUS_HOLDER);
        double attack = getEffectiveAffinity(attacker, path);

        double pressure = 0;
        for(PathInteraction interaction : PathInteractionHelper.getDestructive(access, path)){
            double defence = getEffectiveAffinity(access, defenderProvider, interaction.source());
            pressure -= term(interaction.value() * defence, attack);
        }
        for(PathInteraction interaction : PathInteractionHelper.getInteractionsFrom(access, path, PathInteractionType.DESTRUCTIVE)){
            double defence = getEffectiveAffinity(access, defenderProvider, interaction.target());
            if(defence <= 0) continue;
            pressure += term(interaction.value() * attack, defence);
        }
        return Math.exp(Math.clamp(pressure, -T_MAX, T_MAX));
    }

    private static double term(double source, double base){
        if(source <= 0) return 0;
        return Math.clamp(W * Math.log1p(source / (base + C)), 0, T_MAX);
    }

    private static double raw(ToDoubleFunction<Identifier> affinity, Identifier path){
        return Math.max(0, affinity.applyAsDouble(path));
    }
}
