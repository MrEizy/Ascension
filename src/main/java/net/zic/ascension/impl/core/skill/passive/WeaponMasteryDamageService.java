package net.zic.ascension.impl.core.skill.passive;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.damage.AscensionDamageTypeHolders;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillLevelResolver;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkill;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class WeaponMasteryDamageService {
    private WeaponMasteryDamageService() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDamage(RPGEngineEntityDamagedEvent.Pre event) {
        if (event.getDamage() <= 0.0D
                || event.getSource().hasDamageTypeHolder(AscensionDamageTypeHolders.ATTRIBUTION)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (source == null) {
            return;
        }

        double bestMultiplier = 1.0D;
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
                if (!(passiveModule instanceof PassiveModules.WeaponDamage module)
                        || !matches(player, event.getSource().getDirectEntity(), module)) {
                    continue;
                }
                PathData pathData = AscensionOriginSourceHelper.hasPath(source, module.path())
                        ? AscensionOriginSourceHelper.getPathData(source, module.path())
                        : null;
                bestMultiplier = Math.max(
                        bestMultiplier,
                        WeaponMasteryService.damageMultiplier(pathData, module.realmScaling())
                );
            }
        }

        if (bestMultiplier > 1.0D + 1.0E-8D) {
            event.setDamage(event.getDamage() * bestMultiplier);
        }
    }

    private static boolean matches(
            ServerPlayer player,
            Entity direct,
            PassiveModules.WeaponDamage module
    ) {
        ItemStack stack = player.getMainHandItem();
        boolean tagMatch = module.weaponTag().isPresent()
                && !stack.isEmpty()
                && stack.is(TagKey.create(Registries.ITEM, module.weaponTag().get()));

        return switch (module.match()) {
            case HELD_WEAPON -> direct == player && tagMatch;
            case EMPTY_HAND_OR_TAG -> direct == player && (stack.isEmpty() || tagMatch);
            case ARROW -> direct instanceof AbstractArrow && !(direct instanceof ThrownTrident);
            case TRIDENT -> direct instanceof ThrownTrident || direct == player && tagMatch;
        };
    }
}
