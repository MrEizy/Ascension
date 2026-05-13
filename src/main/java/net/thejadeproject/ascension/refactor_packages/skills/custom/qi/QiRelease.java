package net.thejadeproject.ascension.refactor_packages.skills.custom.qi;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.DescriptionDisplayContainer;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastEndData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastResult;
import net.thejadeproject.ascension.refactor_packages.skills.IPersistentSkillData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.CastType;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;
import net.thejadeproject.ascension.refactor_packages.skills.castable.IPreCastData;

import java.util.concurrent.ConcurrentHashMap;

public class QiRelease implements ICastableSkill {

    private static final double BASE_RANGE        = 6.0;
    private static final double RANGE_PER_REALM   = 2.0;
    private static final double BASE_QI_COST      = 20.0;
    private static final double QI_COST_PER_REALM = 4.0;
    private static final double BASE_PUSH_H       = 1.2;
    private static final double PUSH_H_PER_REALM  = 0.6;
    private static final double BASE_PUSH_V       = 0.3;
    private static final double PUSH_V_PER_REALM  = 0.1;
    private static final double CRASH_MULTIPLIER  = 5.0;
    private static final int    TRACK_TICKS       = 8;
    private static final int    COOLDOWN_TICKS    = 200;

    public static final ConcurrentHashMap<LivingEntity, Float> PUSH_CRASH_DAMAGE = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<LivingEntity, Long>  PUSH_EXPIRY       = new ConcurrentHashMap<>();

    @Override
    public CastResult canCast(Entity caster, IPreCastData preCastData) {
        if (caster.level().isClientSide()) return new CastResult(CastResult.Type.SUCCESS);
        if (!caster.hasData(ModAttachments.ENTITY_DATA)) return new CastResult(CastResult.Type.FAILURE);

        IEntityData data = caster.getData(ModAttachments.ENTITY_DATA);
        int majorRealm = getMajorRealm(caster);
        if (!data.getQiContainer().hasQi(BASE_QI_COST + majorRealm * QI_COST_PER_REALM)) {
            return new CastResult(CastResult.Type.FAILURE);
        }

        return new CastResult(CastResult.Type.SUCCESS);
    }

    @Override
    public void initialCast(Entity caster, IPreCastData preCastData) {
        if (caster.level().isClientSide()) return;
        if (!caster.hasData(ModAttachments.ENTITY_DATA)) return;

        IEntityData data = caster.getData(ModAttachments.ENTITY_DATA);
        int majorRealm = getMajorRealm(caster);
        if (!data.getQiContainer().tryConsumeQi(BASE_QI_COST + majorRealm * QI_COST_PER_REALM)) return;

        double range    = BASE_RANGE   + majorRealm * RANGE_PER_REALM;
        double pushH    = BASE_PUSH_H  + majorRealm * PUSH_H_PER_REALM;
        double pushV    = BASE_PUSH_V  + majorRealm * PUSH_V_PER_REALM;
        float  crashDmg = (float) (pushH * CRASH_MULTIPLIER);
        long   expiry   = caster.level().getGameTime() + TRACK_TICKS;

        pushNearbyEntities(caster, range, pushH, pushV, crashDmg, expiry);
    }

    private void pushNearbyEntities(Entity caster, double range, double pushH, double pushV, float crashDmg, long expiry) {
        AABB area = AABB.ofSize(caster.position(), range * 2, range * 2, range * 2);
        caster.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != caster && e.isAlive())
                .forEach(entity -> {
                    Vec3 dir = entity.position().subtract(caster.position()).normalize();
                    entity.push(dir.x * pushH, pushV, dir.z * pushH);
                    entity.hurtMarked = true;
                    PUSH_CRASH_DAMAGE.put(entity, crashDmg);
                    PUSH_EXPIRY.put(entity, expiry);
                });
    }

    private int getMajorRealm(Entity caster) {
        if (!caster.hasData(ModAttachments.ENTITY_DATA)) return 0;
        IEntityData data = caster.getData(ModAttachments.ENTITY_DATA);
        int highest = 0;
        for (PathData pathData : data.getAllPathData()) {
            if (pathData == null) continue;
            highest = Math.max(highest, pathData.getMajorRealm());
        }
        return highest;
    }

    @Override public boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData) { return false; }
    @Override public void finalCast(CastEndData reason, Entity caster, ICastData castData) {}
    @Override public void onEquip(IEntityData entityData) {}
    @Override public void onUnEquip(IEntityData entityData, IPreCastData preCastData) {}
    @Override public int getCooldown(CastEndData castEndData) { return COOLDOWN_TICKS; }
    @Override public void selected(IEntityData entityData) {}
    @Override public void unselected(IEntityData entityData) {}
    @Override public void onAdded(IEntityData attachedEntityData) {}
    @Override public void onRemoved(IEntityData attachedEntityData, IPersistentSkillData persistentData) {}
    @Override public void onFormAdded(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {}
    @Override public void onFormRemoved(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {}
    @Override public void finishedCooldown(IEntityData attachedEntityData, String identifier) {}

    @Override public IPreCastData freshPreCastData() { return null; }
    @Override public IPreCastData preCastDataFromCompound(CompoundTag tag) { return null; }
    @Override public IPreCastData preCastDataFromNetwork(RegistryFriendlyByteBuf buf) { return null; }
    @Override public ICastData freshCastData() { return null; }
    @Override public ICastData castDataFromCompound(CompoundTag tag) { return null; }
    @Override public ICastData castDataFromNetwork(RegistryFriendlyByteBuf buf) { return null; }
    @Override public IPersistentSkillData freshPersistentInstance() { return null; }
    @Override public IPersistentSkillData persistentInstanceFromCompound(CompoundTag tag) { return null; }
    @Override public IPersistentSkillData persistentInstanceFromNetwork(RegistryFriendlyByteBuf buf) { return null; }
    @Override public IPersistentSkillData freshPersistentData(IEntityData heldEntity) { return null; }
    @Override public IPersistentSkillData fromCompound(CompoundTag tag, IEntityData heldEntity) { return null; }
    @Override public IPersistentSkillData fromNetwork(RegistryFriendlyByteBuf buf) { return null; }

    @Override
    public CastType getCastType() { return CastType.INSTANT; }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getCastElement(UIFrame frame) { return null; }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/spells/icon/placeholder.png"),
                16, 16
        );
    }

    @Override
    public Component getTitle() { return Component.literal("Qi Release"); }

    @Override
    public Component getDescription() { return Component.literal("Releases a burst of Qi, pushing away nearby entities."); }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getInformationContainer(UIFrame frame) {
        return new DescriptionDisplayContainer(frame, getTitle(), getDescription());
    }
}
