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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.DescriptionDisplayContainer;
import net.thejadeproject.ascension.refactor_packages.handlers.AscensionDamageHandler;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastEndData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastResult;
import net.thejadeproject.ascension.refactor_packages.skills.IPersistentSkillData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.CastType;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;
import net.thejadeproject.ascension.refactor_packages.skills.castable.IPreCastData;
import net.thejadeproject.ascension.refactor_packages.skills.custom.SkillTargetingHelper;

import java.util.HashSet;

public class SoulNeedle implements ICastableSkill {
    private static final double QI_COST = 8.0D;
    private static final double RANGE = 18.0D;
    private static final float BASE_DAMAGE = 7.0F;
    private static final int COOLDOWN_TICKS = 80;

    @Override
    public CastResult canCast(Entity caster, IPreCastData preCastData) {
        if (!(caster instanceof ServerPlayer player)) return new CastResult(CastResult.Type.FAILURE);
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return new CastResult(CastResult.Type.FAILURE);

        LivingEntity target = SkillTargetingHelper.findLookTarget(player, RANGE, 0.45D);
        if (target == null) return new CastResult(CastResult.Type.FAILURE);

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        if (!entityData.getQiContainer().hasQi(QI_COST)) return new CastResult(CastResult.Type.FAILURE);

        return new CastResult(CastResult.Type.SUCCESS);
    }

    @Override
    public void initialCast(Entity caster, IPreCastData preCastData) {
        if (!(caster instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;

        LivingEntity target = SkillTargetingHelper.findLookTarget(player, RANGE, 0.45D);
        if (target == null) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        if (!entityData.getQiContainer().tryConsumeQi(QI_COST)) return;

        float damage = calculateDamage(player);

        AscensionDamageHandler.AscensionDamageSource source =
                new AscensionDamageHandler.AscensionDamageSource(
                        new HashSet<>() {{ add(ModPaths.SOUL.getId()); }},
                        target.damageSources().source(target.damageSources().magic().typeHolder().getKey(), caster)
                );

        target.hurt(source, damage);

        ServerLevel level = player.serverLevel();

        level.playSound(
                null,
                target.blockPosition(),
                SoundEvents.SOUL_ESCAPE.value(),
                SoundSource.PLAYERS,
                0.8F,
                1.4F
        );

        level.sendParticles(
                ParticleTypes.SOUL,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                8,
                0.25D,
                0.35D,
                0.25D,
                0.02D
        );
    }

    private float calculateDamage(ServerPlayer player) {
        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        PathData soulData = entityData.getPathData(ModPaths.SOUL.getId());
        int majorRealm = soulData != null ? soulData.getMajorRealm() : 0;
        int minorRealm = soulData != null ? soulData.getMinorRealm() : 0;

        return BASE_DAMAGE + majorRealm * 2.0F + minorRealm * 0.25F;
    }

    @Override public int getCooldown(CastEndData castEndData) { return COOLDOWN_TICKS; }
    @Override public CastType getCastType() { return CastType.INSTANT; }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/spells/icon/placeholder.png"),
                16,
                16
        );
    }

    @Override public Component getTitle() { return Component.translatable("ascension.skill.soul_needle"); }
    @Override public Component getDescription() { return Component.translatable("ascension.skill.soul_needle.description"); }

    @OnlyIn(Dist.CLIENT)
    @Override public RenderableElement getInformationContainer(UIFrame frame) {
        return new DescriptionDisplayContainer(frame, getTitle(), getDescription());
    }

    @Override public void onEquip(IEntityData entityData) {}
    @Override public void onUnEquip(IEntityData entityData, IPreCastData preCastData) {}
    @Override public void finalCast(CastEndData reason, Entity caster, ICastData castData) {}
    @Override public boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData) { return false; }
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
    @OnlyIn(Dist.CLIENT)
    @Override public RenderableElement getCastElement(UIFrame frame) { return null; }
    @Override public void onAdded(IEntityData attachedEntityData) {}
    @Override public void onRemoved(IEntityData attachedEntityData, IPersistentSkillData persistentData) {}
    @Override public void onFormAdded(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {}
    @Override public void onFormRemoved(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {}
    @Override public void finishedCooldown(IEntityData attachedEntityData, String identifier) {}
    @Override public IPersistentSkillData freshPersistentData(IEntityData heldEntity) { return null; }
    @Override public IPersistentSkillData fromCompound(CompoundTag tag, IEntityData heldEntity) { return null; }
    @Override public IPersistentSkillData fromNetwork(RegistryFriendlyByteBuf buf) { return null; }
}