package net.thejadeproject.ascension.refactor_packages.skills.custom.active.attack.weapon;

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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
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
import java.util.List;

public class SwordDraw implements ICastableSkill {
    private static final double QI_COST = 28.0D;
    private static final double DASH_DISTANCE = 6.0D;
    private static final double HIT_RADIUS = 1.25D;
    private static final float BASE_DAMAGE = 18.0F;
    private static final int COOLDOWN_TICKS = 180;

    @Override
    public CastResult canCast(Entity caster, IPreCastData preCastData) {
        if (!(caster instanceof ServerPlayer player)) return new CastResult(CastResult.Type.FAILURE);
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return new CastResult(CastResult.Type.FAILURE);
        if (!player.getMainHandItem().is(ItemTags.SWORDS)) return new CastResult(CastResult.Type.FAILURE);

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        return entityData.getQiContainer().hasQi(QI_COST)
                ? new CastResult(CastResult.Type.SUCCESS)
                : new CastResult(CastResult.Type.FAILURE);
    }

    @Override
    public boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData) {
        return false;
    }

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
        if (!(caster instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;
        if (!player.getMainHandItem().is(ItemTags.SWORDS)) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        if (!entityData.getQiContainer().tryConsumeQi(QI_COST)) return;

        Vec3 start = player.position();
        Vec3 direction = player.getLookAngle().normalize();


        HitResult blockHit = player.pick(DASH_DISTANCE, 0.0F, true);

        Vec3 end;
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation().subtract(direction.scale(0.8D));
        } else {
            end = start.add(direction.scale(DASH_DISTANCE));
        }

        damageTargetsInPath(player, start, end);

        spawnSwordTrail(player, start, end);

        player.teleportTo(end.x, end.y, end.z);
        player.setDeltaMovement(direction.scale(0.2D));
        player.hurtMarked = true;
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    private void damageTargetsInPath(ServerPlayer player, Vec3 start, Vec3 end) {
        AABB area = new AABB(start, end).inflate(HIT_RADIUS);

        List<LivingEntity> targets = player.serverLevel().getEntitiesOfClass(
                LivingEntity.class,
                area,
                target -> target.isAlive() && target != player && !target.isSpectator()
        );

        float damage = calculateDamage(player);

        AscensionDamageHandler.AscensionDamageSource source =
                new AscensionDamageHandler.AscensionDamageSource(
                        new HashSet<>() {{ add(ModPaths.SWORD.getId()); }},
                        player.damageSources().playerAttack(player)
                );

        for (LivingEntity target : targets) {
            target.hurt(source, damage);

            Vec3 push = player.getLookAngle().normalize().scale(0.5D);
            target.push(push.x, 0.12D, push.z);
            target.hurtMarked = true;
        }
    }

    private float calculateDamage(ServerPlayer player) {
        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        PathData swordData = entityData.getPathData(ModPaths.SWORD.getId());
        int major = swordData != null ? swordData.getMajorRealm() : 0;
        int minor = swordData != null ? swordData.getMinorRealm() : 0;

        float attackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);

        return BASE_DAMAGE + attackDamage * (1.8F + major * 0.35F + minor * 0.04F);
    }

    private void spawnSwordTrail(ServerPlayer player, Vec3 start, Vec3 end) {
        ServerLevel level = player.serverLevel();

        Vec3 diff = end.subtract(start);
        int steps = 14;

        for (int i = 0; i <= steps; i++) {
            double progress = i / (double) steps;
            Vec3 pos = start.add(diff.scale(progress)).add(0.0D, 1.0D, 0.0D);

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.x, pos.y, pos.z,
                    1,
                    0.015D, 0.015D, 0.015D,
                    0.0D
            );
        }
    }

    @Override public int getCooldown(CastEndData castEndData) { return COOLDOWN_TICKS; }

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

    @Override public CastType getCastType() { return CastType.INSTANT; }

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
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/spells/icon/placeholder.png"),
                16,
                16
        );
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ascension.skill.sword_draw");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("ascension.skill.sword_draw.description");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getInformationContainer(UIFrame frame) {
        return new DescriptionDisplayContainer(frame, getTitle(), getDescription());
    }


}