package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.soul;

import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastResult;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.IPreCastData;
import net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.GenericCultivationSkill;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.soul.ZenithSunTechnique;

public class ZenithSunCultivationSkill extends GenericCultivationSkill {

    private static final double SUN_MULTIPLIER = 2.0D;

    public ZenithSunCultivationSkill() {
        super(ZenithSunTechnique.BASE_RATE, ModPaths.SOUL.getId());
    }

    @Override
    public CastResult canCast(Entity caster, IPreCastData preCastData) {
        if (!caster.level().canSeeSky(caster.blockPosition())) {
            return new CastResult(CastResult.Type.FAILURE,
                    Component.translatable("ascension.skill.zenith_sun_cultivation_skill.blocked_indoors"));
        }
        return new CastResult(CastResult.Type.SUCCESS);
    }

    @Override
    protected double getEffectiveRate(Entity caster) {
        double rate = super.getEffectiveRate(caster);
        if (DawningSunCultivationSkill.isLookingAtSun(caster)) {
            rate *= SUN_MULTIPLIER;
        }
        return rate;
    }

    @Override
    public boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData) {
        boolean continuing = super.continueCasting(ticksElapsed, caster, castData);
        if (continuing && !caster.level().isClientSide()) {
            if (DawningSunCultivationSkill.isMoonExposed(caster)) {
                if (caster instanceof LivingEntity living) {
                    float damage = living.getMaxHealth() * 0.01f;
                    DamageSource source = new DamageSource(
                            caster.level().registryAccess()
                                    .registryOrThrow(Registries.DAMAGE_TYPE)
                                    .getHolderOrThrow(DamageTypes.FREEZE)
                    );
                    living.hurt(source, damage);
                }
            }
        }
        return continuing;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(
                        AscensionCraft.MOD_ID,
                        "textures/spells/icon/placeholder.png"
                ),
                16, 16
        );
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ascension.skill.zenith_sun_cultivation_skill");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("ascension.skill.zenith_sun_cultivation_skill.description");
    }
}
