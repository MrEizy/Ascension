package net.thejadeproject.ascension.refactor_packages.skills.custom.active.attack.soul;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.DescriptionDisplayContainer;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastEndData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastResult;
import net.thejadeproject.ascension.refactor_packages.skills.IPersistentSkillData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.*;
import net.thejadeproject.ascension.refactor_packages.skills.custom.SkillTargetingHelper;

public class SoulLanternSkill implements ICastableSkill {
    private static final double BASE_QI_COST = 35.0D;
    private static final double RANGE = 18.0D;
    private static final int DURATION_TICKS = 160;
    private static final int COOLDOWN_TICKS = 140;

    @Override
    public CastResult canCast(Entity caster, IPreCastData preCastData) {
        if (!(caster instanceof ServerPlayer player)) return new CastResult(CastResult.Type.FAILURE);
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return new CastResult(CastResult.Type.FAILURE);

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        return entityData.getQiContainer().hasQi(BASE_QI_COST)
                ? new CastResult(CastResult.Type.SUCCESS)
                : new CastResult(CastResult.Type.FAILURE);
    }

    @Override
    public void initialCast(Entity caster, IPreCastData preCastData) {
        if (!(caster instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        if (!entityData.getQiContainer().tryConsumeQi(BASE_QI_COST)) return;

        LivingEntity target = SkillTargetingHelper.findLookTarget(player, RANGE, 1.0D);
        if (target == null) return;

        PathData soulPath = entityData.getPathData(ModPaths.SOUL.getId());
        int major = soulPath != null ? soulPath.getMajorRealm() : 0;

        int duration = DURATION_TICKS + major * 30;
        int weaknessAmp = Math.min(2, major / 2);

        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0, true, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, weaknessAmp, true, false, false));

        spawnLanternParticles(player, target);
    }

    private void spawnLanternParticles(ServerPlayer player, LivingEntity target) {
        Vec3 start = player.getEyePosition();
        Vec3 end = target.getEyePosition();
        Vec3 diff = end.subtract(start);

        for (int i = 0; i <= 12; i++) {
            double t = i / 12.0D;
            Vec3 pos = start.add(diff.scale(t));

            player.serverLevel().sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    pos.x, pos.y, pos.z,
                    1,
                    0.02D, 0.02D, 0.02D,
                    0.0D
            );
        }
    }

    @Override public boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData) { return false; }
    @Override public int getCooldown(CastEndData castEndData) { return COOLDOWN_TICKS; }
    @Override public CastType getCastType() { return CastType.INSTANT; }

    @Override public Component getTitle() {
        return Component.translatable("ascension.skill.soul_lantern");
    }

    @Override public Component getDescription() {
        return Component.translatable("ascension.skill.soul_lantern.description");
    }

    @OnlyIn(Dist.CLIENT)
    @Override public ITextureData getIcon() {
        return new TextureData(ResourceLocation.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                "textures/spells/icon/placeholder.png"
        ), 16, 16);
    }

    @OnlyIn(Dist.CLIENT)
    @Override public RenderableElement getInformationContainer(UIFrame frame) {
        return new DescriptionDisplayContainer(frame, getTitle(), getDescription());
    }

    @OnlyIn(Dist.CLIENT)
    @Override public RenderableElement getCastElement(UIFrame frame) { return null; }

    @Override public void onEquip(IEntityData entityData) {}
    @Override public void onUnEquip(IEntityData entityData, IPreCastData preCastData) {}
    @Override public void finalCast(CastEndData reason, Entity caster, ICastData castData) {}
    @Override public void selected(IEntityData entityData) {}
    @Override public void unselected(IEntityData entityData) {}

    @Override public IPreCastData freshPreCastData() { return null; }
    @Override public IPreCastData preCastDataFromCompound(CompoundTag tag) { return null; }
    @Override public IPreCastData preCastDataFromNetwork(RegistryFriendlyByteBuf buf) { return null; }

    @Override public ICastData freshCastData() { return null; }
    @Override public ICastData castDataFromCompound(CompoundTag tag) { return null; }
    @Override public ICastData castDataFromNetwork(RegistryFriendlyByteBuf buf) { return null; }

    @Override public IPersistentSkillData freshPersistentInstance() { return null; }
    @Override public IPersistentSkillData persistentInstanceFromCompound(CompoundTag tag) { return null; }
    @Override public IPersistentSkillData persistentInstanceFromNetwork(RegistryFriendlyByteBuf buf) { return null; }

    @Override public void onAdded(IEntityData attachedEntityData) {}
    @Override public void onRemoved(IEntityData attachedEntityData, IPersistentSkillData persistentData) {}
    @Override public void onFormAdded(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {}
    @Override public void onFormRemoved(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {}
    @Override public void finishedCooldown(IEntityData attachedEntityData, String identifier) {}

    @Override public IPersistentSkillData freshPersistentData(IEntityData heldEntity) { return null; }
    @Override public IPersistentSkillData fromCompound(CompoundTag tag, IEntityData heldEntity) { return null; }
    @Override public IPersistentSkillData fromNetwork(RegistryFriendlyByteBuf buf) { return null; }
}