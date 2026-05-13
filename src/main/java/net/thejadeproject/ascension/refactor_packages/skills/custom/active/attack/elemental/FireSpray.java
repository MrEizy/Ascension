package net.thejadeproject.ascension.refactor_packages.skills.custom.active.attack.elemental;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.particle.ModParticles;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.DescriptionDisplayContainer;
import net.thejadeproject.ascension.refactor_packages.handlers.AscensionDamageHandler;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastEndData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastResult;
import net.thejadeproject.ascension.refactor_packages.skills.IPersistentSkillData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.CastType;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;
import net.thejadeproject.ascension.refactor_packages.skills.castable.IPreCastData;

import java.util.HashSet;

public class FireSpray implements ICastableSkill {

    private static final double QI_COST_PER_SECOND = 5.0D;
    private static final int COOLDOWN_TICKS = 100;

    @Override
    public void onEquip(IEntityData entityData) {

    }

    @Override
    public void onUnEquip(IEntityData entityData, IPreCastData preCastData) {

    }

    @Override
    public void finalCast(CastEndData reason, Entity caster, ICastData castData) {

    }

    @Override
    public void initialCast(Entity caster, IPreCastData preCastData) {

    }

    @Override
    public CastResult canCast(Entity caster, IPreCastData preCastData) {
        if (caster.level().isClientSide()) return new CastResult(CastResult.Type.SUCCESS);
        if (!caster.hasData(ModAttachments.ENTITY_DATA)) return new CastResult(CastResult.Type.FAILURE);

        IEntityData entityData = caster.getData(ModAttachments.ENTITY_DATA);
        if (!entityData.getQiContainer().hasQi(QI_COST_PER_SECOND)) {
            return new CastResult(CastResult.Type.FAILURE);
        }

        return new CastResult(CastResult.Type.SUCCESS);
    }


    @Override
    public boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData) {
        //System.out.println("casting skill");
        final double reach = 8;
        double travelled = reach;
        HitResult blockHit = caster.pick(reach, 0.0F, true);
        Vec3 eyePosition = caster.getEyePosition();
        Vec3 viewVector = caster.getViewVector(0.0F);
        Vec3 searchVec = eyePosition.add(viewVector.scale(reach));

        AABB searchBox = caster.getBoundingBox().expandTowards(viewVector.scale(reach)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                caster.level(),
                caster,
                eyePosition,
                searchVec,
                searchBox,
                (entity) -> !entity.isSpectator() && entity.isPickable()
        );
        if(entityHit != null && blockHit.getType() == HitResult.Type.MISS && !caster.level().isClientSide()){

            if(caster.distanceToSqr(blockHit.getLocation()) < caster.distanceToSqr(entityHit.getLocation())){
                travelled = Math.sqrt( caster.distanceToSqr(blockHit.getLocation()));
            }else {
                //System.out.println("hit target");
                Entity targetedEntity = entityHit.getEntity();

                AscensionDamageHandler.AscensionDamageSource source = new AscensionDamageHandler.AscensionDamageSource(
                        new HashSet<>(){{add(ModPaths.FIRE.getId());}},
                        targetedEntity.damageSources().source(targetedEntity.damageSources().magic().typeHolder().getKey(),caster)
                );

                targetedEntity.hurt(source,2);
                targetedEntity.igniteForSeconds(4);
                travelled = Math.sqrt( caster.distanceToSqr(entityHit.getLocation()));
            }
        }

        Vec3 vec3 = caster.getPosition(0.0f).add(0,0.5,0);
        Vec3 vec31 = caster.getViewVector(0.0f);



        caster.level().addParticle(ModParticles.CULTIVATION_PARTICLES.get(),
                eyePosition.x,eyePosition.y-0.2,eyePosition.z,
                viewVector.x,viewVector.y,viewVector.z);

        if (!caster.level().isClientSide() && ticksElapsed > 0 && ticksElapsed % 20 == 0) {
            IEntityData entityData = caster.getData(ModAttachments.ENTITY_DATA);
            if (!entityData.getQiContainer().tryConsumeQi(QI_COST_PER_SECOND)) return false;
        }

        if(caster.level().isClientSide) return ticksElapsed < 100;
        return caster.getData(ModAttachments.INPUT_STATES).isHeld("skill_cast") && ticksElapsed < 100;
   }

    @Override
    public int getCooldown(CastEndData castEndData) {
        return COOLDOWN_TICKS;
    }

    @Override
    public void selected(IEntityData entityData) {

    }

    @Override
    public void unselected(IEntityData entityData) {

    }

    @Override
    public IPreCastData freshPreCastData() {
        return null;
    }

    @Override
    public IPreCastData preCastDataFromCompound(CompoundTag tag) {
        return null;
    }

    @Override
    public IPreCastData preCastDataFromNetwork(RegistryFriendlyByteBuf buf) {
        return null;
    }

    @Override
    public ICastData freshCastData() {
        return null;
    }

    @Override
    public ICastData castDataFromCompound(CompoundTag tag) {
        return null;
    }

    @Override
    public ICastData castDataFromNetwork(RegistryFriendlyByteBuf buf) {
        return null;
    }

    @Override
    public IPersistentSkillData freshPersistentInstance() {
        return null;
    }

    @Override
    public IPersistentSkillData persistentInstanceFromCompound(CompoundTag tag) {
        return null;
    }

    @Override
    public IPersistentSkillData persistentInstanceFromNetwork(RegistryFriendlyByteBuf buf) {
        return null;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getCastElement(UIFrame frame) {
        return null;
    }

    @Override
    public void onAdded(IEntityData attachedEntityData) {

    }

    @Override
    public void onRemoved(IEntityData attachedEntityData, IPersistentSkillData persistentData) {

    }

    @Override
    public void onFormAdded(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {

    }

    @Override
    public void onFormRemoved(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {

    }

    @Override
    public void finishedCooldown(IEntityData attachedEntityData, String identifier) {

    }

    @Override
    public IPersistentSkillData freshPersistentData(IEntityData heldEntity) {
        return null;
    }

    @Override
    public IPersistentSkillData fromCompound(CompoundTag tag, IEntityData heldEntity) {
        return null;
    }

    @Override
    public IPersistentSkillData fromNetwork(RegistryFriendlyByteBuf buf) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"textures/spells/icon/placeholder.png"),
                16,16
        );
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ascension.skill.fire_spray");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("ascension.skill.fire_spray.description");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getInformationContainer(UIFrame frame) {
        return new DescriptionDisplayContainer(frame,getTitle(),getDescription());
    }
}
