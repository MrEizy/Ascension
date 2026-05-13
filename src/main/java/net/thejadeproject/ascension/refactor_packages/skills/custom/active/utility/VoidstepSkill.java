package net.thejadeproject.ascension.refactor_packages.skills.custom.active.utility;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
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


public class VoidstepSkill implements ICastableSkill {

    private static final double BASE_RANGE      = 8.0;
    private static final double RANGE_PER_REALM = 2.0;
    private static final double STEP_SIZE       = 0.25;
    private static final float  SOUND_VOLUME    = 0.35f;
    private static final float  SOUND_PITCH     = 1.6f;

    private static final double BASE_QI_COST    = 30.0D;
    private static final double QI_COST_PER_REALM = 10.0D;
    private static final int    COOLDOWN_TICKS  = 20;

    // ── ISkill ────────────────────────────────────────────────────────────────

    @Override public void onAdded(IEntityData attachedEntityData) {}
    @Override public void onRemoved(IEntityData attachedEntityData, IPersistentSkillData persistentData) {}
    @Override public void onFormAdded(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {}
    @Override public void onFormRemoved(IEntityData heldEntity, ResourceLocation form, IPhysiqueData physiqueData) {}
    @Override public void finishedCooldown(IEntityData attachedEntityData, String identifier) {}
    @Override public IPersistentSkillData freshPersistentData(IEntityData heldEntity) { return null; }
    @Override public IPersistentSkillData fromCompound(CompoundTag tag, IEntityData heldEntity) { return null; }
    @Override public IPersistentSkillData fromNetwork(RegistryFriendlyByteBuf buf) { return null; }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/spells/icon/voidstep.png"),
                16, 16
        );
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ascension.skill.voidstep");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("ascension.skill.voidstep.description");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getInformationContainer(UIFrame frame) {
        return null;
    }

    // ── ICastableSkill ────────────────────────────────────────────────────────

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getCastElement(UIFrame frame) {
        return null;
    }

    @Override
    public CastResult canCast(Entity caster, IPreCastData preCastData) {
        if (caster.level().isClientSide()) return new CastResult(CastResult.Type.SUCCESS);
        if (!caster.hasData(ModAttachments.ENTITY_DATA)) return new CastResult(CastResult.Type.FAILURE);

        IEntityData entityData = caster.getData(ModAttachments.ENTITY_DATA);
        PathData pathData = entityData.getPathData(ModPaths.ESSENCE.getId());
        int majorRealm = (pathData != null) ? pathData.getMajorRealm() : 0;
        double qiCost = BASE_QI_COST + (majorRealm * QI_COST_PER_REALM);

        if (!entityData.getQiContainer().hasQi(qiCost)) {
            return new CastResult(CastResult.Type.FAILURE);
        }

        return new CastResult(CastResult.Type.SUCCESS);
    }

    @Override
    public void initialCast(Entity caster, IPreCastData preCastData) {
        if (caster.level().isClientSide()) return;
        if (!(caster instanceof Player player)) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        PathData pathData = entityData.getPathData(ModPaths.ESSENCE.getId());
        int majorRealm = (pathData != null) ? pathData.getMajorRealm() : 0;
        double qiCost = BASE_QI_COST + (majorRealm * QI_COST_PER_REALM);

        if (!entityData.getQiContainer().tryConsumeQi(qiCost)) return;

        double range = BASE_RANGE + (majorRealm * RANGE_PER_REALM);

        Vec3 direction = player.getLookAngle();

        Vec3 destination = resolveDestination(player, direction, range);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.teleportTo(destination.x, destination.y, destination.z);
        } else {
            player.setPos(destination.x, destination.y, destination.z);
        }

        player.level().playSound(
                null,
                destination.x, destination.y, destination.z,
                SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS,
                SOUND_VOLUME,
                SOUND_PITCH
        );
    }

    @Override public void finalCast(CastEndData reason, Entity caster, ICastData castData) {}

    @Override
    public boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData) {
        return false;
    }

    @Override
    public int getCooldown(CastEndData castEndData) {
        return COOLDOWN_TICKS;
    }

    private Vec3 resolveDestination(Player player, Vec3 direction, double range) {
        Level level = player.level();

        double halfWidth = player.getBbWidth() / 2.0;
        double height    = player.getBbHeight();

        Vec3 origin   = player.position();
        Vec3 lastSafe = origin;

        int steps = (int) Math.ceil(range / STEP_SIZE);

        for (int i = 1; i <= steps; i++) {
            double dist      = Math.min(i * STEP_SIZE, range);
            Vec3   candidate = origin.add(direction.scale(dist));

            AABB testBox = new AABB(
                    candidate.x - halfWidth, candidate.y,          candidate.z - halfWidth,
                    candidate.x + halfWidth, candidate.y + height, candidate.z + halfWidth
            );

            if (level.getBlockCollisions(player, testBox).iterator().hasNext()) {
                break;
            }

            lastSafe = candidate;
        }

        return lastSafe;
    }

    // ── Stubs ─────────────────────────────────────────────────────────────────

    @Override public void onEquip(IEntityData entityData) {}
    @Override public void onUnEquip(IEntityData entityData, IPreCastData preCastData) {}
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
}