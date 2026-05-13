package net.thejadeproject.ascension.refactor_packages.skills.custom.active.attack.body;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

import java.util.HashSet;

public class WhiteLightningFist implements ICastableSkill {

    private static final double QI_COST = 10.0D;
    private static final double REACH = 4.5D;
    private static final float BASE_DAMAGE = 15.0F;
    private static final int COOLDOWN_TICKS = 120;

    @Override
    public CastResult canCast(Entity caster, IPreCastData preCastData) {
        if (!(caster instanceof ServerPlayer player)) {
            return new CastResult(CastResult.Type.FAILURE);
        }

        if (!player.hasData(ModAttachments.ENTITY_DATA)) {
            return new CastResult(CastResult.Type.FAILURE);
        }

        if (!player.getMainHandItem().isEmpty()) {
            return new CastResult(CastResult.Type.FAILURE);
        }

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        if (!entityData.getQiContainer().hasQi(QI_COST)) {
            return new CastResult(CastResult.Type.FAILURE);
        }

        return new CastResult(CastResult.Type.SUCCESS);
    }

    @Override
    public void initialCast(Entity caster, IPreCastData preCastData) {
        if (!(caster instanceof ServerPlayer player)) return;
        if (!player.getMainHandItem().isEmpty()) return;
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;
        if (player.level().isClientSide()) return;

        LivingEntity target = findTarget(player);
        if (target == null) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        if (!entityData.getQiContainer().tryConsumeQi(QI_COST)) {
            return;
        }

        float damage = calculateDamage(player);

        AscensionDamageHandler.AscensionDamageSource source = new AscensionDamageHandler.AscensionDamageSource(
                new HashSet<>(){{add(ModPaths.BODY.getId());}},
                target.damageSources().source(target.damageSources().magic().typeHolder().getKey(), caster)
        );

        //System.out.println("dealing " + damage + " base damage");

        target.hurt(source, damage);

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0));

        Vec3 push = player.getViewVector(1.0F).scale(0.45D);
        target.push(push.x, 0.12D, push.z);
        target.hurtMarked = true;

        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        playCastEffects(player, target);
    }


    private LivingEntity findTarget(Player player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1.0F);
        Vec3 searchEnd = eyePosition.add(viewVector.scale(REACH));

        HitResult blockHit = player.pick(REACH, 0.0F, false);

        AABB searchBox = player.getBoundingBox()
                .expandTowards(viewVector.scale(REACH))
                .inflate(1.0D);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                eyePosition,
                searchEnd,
                searchBox,
                entity -> entity instanceof LivingEntity
                        && entity != player
                        && !entity.isSpectator()
                        && entity.isPickable()
        );

        if (entityHit == null) return null;

        if (
                blockHit.getType() != HitResult.Type.MISS
                        && eyePosition.distanceToSqr(blockHit.getLocation()) < eyePosition.distanceToSqr(entityHit.getLocation())
        ) {
            return null;
        }

        return (LivingEntity) entityHit.getEntity();
    }


    private float getBodyBonus(Player player) {
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return 0.0F;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        PathData bodyData = entityData.getPathData(ModPaths.BODY.getId());
        if (bodyData == null) return 0.0F;

        int majorRealm = bodyData.getMajorRealm();
        int minorRealm = bodyData.getMinorRealm();

        float majorBonus = (majorRealm + 1) * 1.5F;
        float minorBonus = minorRealm * 0.15F;

        return majorBonus + minorBonus;
    }

    private void playCastEffects(Player player, LivingEntity target) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        net.minecraft.world.phys.Vec3 handPos = player.getEyePosition().add(player.getViewVector(1.0F).scale(0.8D));
        net.minecraft.world.phys.Vec3 targetPos = target.getBoundingBox().getCenter();

        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                handPos.x, handPos.y, handPos.z,
                7,
                0.15D, 0.15D, 0.15D,
                0.05D
        );

        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                handPos.x, handPos.y, handPos.z,
                4,
                0.12D, 0.12D, 0.12D,
                0.01D
        );

        spawnLightningTrail(serverLevel, handPos, targetPos);

        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                targetPos.x, targetPos.y, targetPos.z,
                12,
                0.25D, 0.25D, 0.25D,
                0.08D
        );

        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.CRIT,
                targetPos.x, targetPos.y, targetPos.z,
                7,
                0.2D, 0.2D, 0.2D,
                0.1D
        );
    }

    private void spawnLightningTrail(
            net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.phys.Vec3 start,
            net.minecraft.world.phys.Vec3 end
    ) {
        net.minecraft.world.phys.Vec3 diff = end.subtract(start);
        int steps = 7;

        for (int i = 0; i <= steps; i++) {
            double progress = i / (double) steps;

            net.minecraft.world.phys.Vec3 pos = start.add(diff.scale(progress));

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                    pos.x, pos.y, pos.z,
                    1,
                    0.03D, 0.03D, 0.03D,
                    0.0D
            );
        }
    }

    private float calculateDamage(ServerPlayer player) {
        if (!player.hasData(ModAttachments.ENTITY_DATA)) {
            return BASE_DAMAGE;
        }

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        PathData bodyData = entityData.getPathData(ModPaths.BODY.getId());
        int majorRealm = bodyData != null ? bodyData.getMajorRealm() : 0;
        int minorRealm = bodyData != null ? bodyData.getMinorRealm() : 0;

        float punchDamage = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);

        float skillMultiplier = 1.75F + majorRealm * 0.35F + minorRealm * 0.035F;

        return BASE_DAMAGE + punchDamage * skillMultiplier;
    }

    @Override
    public int getCooldown(CastEndData castEndData) {
        return COOLDOWN_TICKS;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(
                        AscensionCraft.MOD_ID,
                        "textures/spells/icon/white_lightning_placeholder.png"
                ),
                16,
                16
        );
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ascension.skill.white_lightning_fist");
    }

    @Override
    public Component getDescription() {
        return Component.translatable(
                "ascension.skill.white_lightning_fist.description"
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getInformationContainer(UIFrame frame) {
        return new DescriptionDisplayContainer(frame,getTitle(),getDescription());
    }

    @Override public void onEquip(IEntityData entityData) {}
    @Override public void onUnEquip(IEntityData entityData, IPreCastData preCastData) {}
    @Override public void finalCast(CastEndData reason, Entity caster, ICastData castData) {}
    @Override public boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData) {
        return false;
    }
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