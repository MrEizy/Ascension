package net.zic.ascension.impl.core.skill.passive;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;

import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.SkillLevelResolver;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkill;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.AscensionResourceTypes;
import net.zic.ascension.impl.runtime.weapon.WeaponSwingSpec;
import net.zic.ascension.impl.runtime.weapon.WeaponTechniqueResolver;
import net.zic.ascension.impl.runtime.weapon.WeaponVfxUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class WeaponMasteryService {
    private static final Map<UUID, Long> LAST_SWING = new HashMap<>();
    private static final List<Identifier> DEFAULT_CLASSIFICATIONS = List.of(
            AscensionCraft.prefix("skill"),
            AscensionCraft.prefix("melee"),
            AscensionCraft.prefix("weapon")
    );

    private WeaponMasteryService() {
    }

    public static void clearRuntimeState() {
        LAST_SWING.clear();
    }

    public static boolean trySwing(ServerPlayer player) {
        if (player == null || player.isRemoved() || !player.isAlive() || player.isSpectator()) {
            return false;
        }

        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (source == null) {
            return false;
        }

        long gameTime = player.level().getGameTime();
        long lastSwing = LAST_SWING.getOrDefault(player.getUUID(), Long.MIN_VALUE / 2L);
        Candidate selected = null;

        for (Identifier skillId : AscensionOriginSourceHelper.getSkills(source)) {
            Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, player.registryAccess());
            if (!(skill instanceof ResourceModifierPassiveSkill passive)) {
                continue;
            }

            SkillData skillData = AscensionOriginSourceHelper.getSkillData(source, skillId);
            if (!(skillData instanceof ResourceModifierPassiveSkill.Data passiveData)
                    || skill instanceof ToggleableSkill && !passiveData.isEnabled()) {
                continue;
            }

            int level = Math.max(1, SkillLevelResolver.resolve(source, skillId).effectiveLevel());
            for (var passiveModule : passive.modules(level)) {
                if (!(passiveModule instanceof PassiveModules.WeaponSwing module)
                        || !WeaponVfxUtils.matchesWeapon(
                        player,
                        Optional.ofNullable(module.weaponTag()),
                        module.extras().allowEmptyHand()
                )
                        || gameTime - lastSwing < module.minimumInterval()) {
                    continue;
                }
                Candidate candidate = new Candidate(skillId, module);
                if (selected == null || CANDIDATE_ORDER.compare(candidate, selected) > 0) {
                    selected = candidate;
                }
            }
        }

        if (selected == null || !spawn(player, source, selected)) {
            return false;
        }
        LAST_SWING.put(player.getUUID(), gameTime);
        return true;
    }

    private static boolean spawn(ServerPlayer player, OriginSource source, Candidate candidate) {
        Identifier skillId = candidate.skillId();
        PassiveModules.WeaponSwing module = candidate.module();
        PathInstance pathData = AscensionOriginSourceHelper.hasPath(source, module.path())
                ? AscensionOriginSourceHelper.getPathInstance(source, module.path())
                : null;
        double multiplier = damageMultiplier(pathData, module.realmScaling());
        double qiCost = Math.max(0.0D, module.qiCost() * multiplier);

        if (qiCost > 1.0E-8D) {
            ResourceTransactionService.Result result = ResourceTransactionService.transact(
                    ResourceTransactionRequest.of(
                            player,
                            AscensionResourceTypes.QI.getId(),
                            ResourceOperation.CONSUME,
                            qiCost,
                            AscensionResourceSources.SKILL_CASTING
                    ).withSkill(skillId)
            );
            if (!result.succeeded() || result.appliedAmount() + 1.0E-8D < result.resolvedAmount()) {
                return false;
            }
        }

        WeaponTechniqueResolver.Resolution style = WeaponTechniqueResolver.resolve(
                source,
                module.path(),
                module.vfxType(),
                module.fallbackColor(),
                module.techniqueColors()
        );
        List<Identifier> classifications = module.classifications().isEmpty()
                ? DEFAULT_CLASSIFICATIONS
                : module.classifications();
        Optional<WeaponSwingSpec.HitEffect> hitEffect = module.extras().hitEffect()
                .filter(effect -> !effect.definition().isBlank())
                .map(effect -> new WeaponSwingSpec.HitEffect(
                        SkillDefinitions.localId(skillId, "effect", effect.definition()),
                        effect.duration(),
                        effect.potency()
                ));

        WeaponVfxUtils.spawnSwingVfxAhead(
                player.level(),
                player,
                module.rotationZ(),
                module.radius(),
                module.baseDamage() * multiplier,
                module.knockback(),
                module.duration(),
                module.vfxType(),
                skillId,
                module.path(),
                style.technique(),
                style.colorFolder(),
                module.movement(),
                module.extras().hitShape(),
                module.extras().blockImpact(),
                hitEffect,
                classifications
        );
        return true;
    }

    static double damageMultiplier(
            PathInstance pathData,
            PassiveModules.WeaponSwing.RealmScaling scaling
    ) {
        if (pathData == null) {
            return 1.0D;
        }
        double bonus = scaling.baseBonus()
                + pathData.getCurrentMajorRealm() * scaling.bonusPerMajorRealm()
                + pathData.getCurrentMinorRealm() * scaling.bonusPerMinorRealm();
        return 1.0D + Math.clamp(bonus, 0.0D, scaling.maximumBonus());
    }

    private static final Comparator<Candidate> CANDIDATE_ORDER = Comparator
            .comparingInt((Candidate candidate) -> candidate.module().extras().priority())
            .thenComparing(candidate -> candidate.skillId().toString());

    private record Candidate(Identifier skillId, PassiveModules.WeaponSwing module) {
    }
}
