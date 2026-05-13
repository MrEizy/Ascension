package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.essence;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.breakthroughs.IBreakthroughInstance;
import net.thejadeproject.ascension.refactor_packages.breakthroughs.NineHeavenlyTribulations;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.events.CultivateEvent;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.DescriptionDisplayContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.skills.cultivation.CultivationProgressBar;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastEndData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastResult;
import net.thejadeproject.ascension.refactor_packages.skills.IPersistentSkillData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.CastType;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;
import net.thejadeproject.ascension.refactor_packages.skills.castable.IPreCastData;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.BloodfeastSoulRefiningTechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.technique_data.BloodfeastTechniqueData;

import java.util.List;


public class BloodfeastBanquetSkill implements ICastableSkill {

    private static final ResourceLocation ESSENCE_PATH = ModPaths.ESSENCE.getId();

    private static final double BASE_QI_COST_PER_SECOND   = 40.0D;
    private static final double QI_COST_PER_REALM          = 20.0D;
    private static final double BASE_QI_COST_PER_TICK      = BASE_QI_COST_PER_SECOND / 20.0D;
    private static final double QI_COST_PER_REALM_PER_TICK = QI_COST_PER_REALM / 20.0D;

    /** Base HP drained from each target per tick. */
    private static final float BASE_DRAIN = 0.5f;

    /** Cultivation gained per entity drained per tick (before bonuses). */
    private static final double CULTIVATION_PER_ENTITY = 0.8D;

    /**
     * Number of particle spawn points along the mob→player line per target.
     * More = denser, smoother-looking stream.
     */
    private static final int STREAM_POINTS = 5;
    private static final double STREAM_JITTER = 0.15D;
    private static final double PARTICLE_SPEED = 0.06D;

    // ── Core cast loop ────────────────────────────────────────────────────────

    @Override
    public CastResult canCast(Entity caster, IPreCastData preCastData) {
        if (caster.level().isClientSide()) return new CastResult(CastResult.Type.SUCCESS);
        if (!caster.hasData(ModAttachments.ENTITY_DATA)) return new CastResult(CastResult.Type.FAILURE);

        IEntityData entityData = caster.getData(ModAttachments.ENTITY_DATA);
        PathData pathData = entityData.getPathData(ESSENCE_PATH);
        int majorRealm = (pathData != null) ? pathData.getMajorRealm() : 0;
        double qiCost = BASE_QI_COST_PER_TICK + (majorRealm * QI_COST_PER_REALM_PER_TICK);

        if (!entityData.getQiContainer().hasQi(qiCost)) {
            return new CastResult(CastResult.Type.FAILURE);
        }

        return new CastResult(CastResult.Type.SUCCESS);
    }

    @Override
    public boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData) {
        if (!caster.hasData(ModAttachments.INPUT_STATES)) return false;

        if (!caster.level().isClientSide()) {
            IEntityData entityData = caster.getData(ModAttachments.ENTITY_DATA);
            PathData pathData      = entityData.getPathData(ESSENCE_PATH);

            if (pathData == null)                        return false;

            double qiCost = BASE_QI_COST_PER_TICK + (pathData.getMajorRealm() * QI_COST_PER_REALM_PER_TICK);
            if (!entityData.getQiContainer().tryConsumeQi(qiCost)) return false;
            if (pathData.isBreakingThrough())            return false;
            if (pathData.getLastUsedTechnique() == null) return false;

            ITechnique rawTechnique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(
                    pathData.getLastUsedTechnique()
            );
            if (!(rawTechnique instanceof BloodfeastSoulRefiningTechnique technique)) return false;

            ITechniqueData rawData = pathData.getTechniqueData(pathData.getLastUsedTechnique());

            if (!technique.canCultivateMajorRealm(rawData, pathData.getMajorRealm())) {
                return caster.getData(ModAttachments.INPUT_STATES).isHeld("skill_cast");
            }

            // ── Gather targets ─────────────────────────────────────────────────
            int    majorRealm = pathData.getMajorRealm();
            double range      = BloodfeastSoulRefiningTechnique.getRangeForRealm(majorRealm);

            List<LivingEntity> targets = caster.level().getEntitiesOfClass(
                    LivingEntity.class,
                    AABB.ofSize(caster.position(), range * 2, range * 2, range * 2),
                    e -> e != caster && e.isAlive()
            );

            if (!targets.isEmpty()) {
                float  drainPerTarget = BASE_DRAIN + (majorRealm * 0.1f);
                double rawCultivation = 0.0D;

                Vec3 casterChest = caster.position().add(0, caster.getBbHeight() * 0.75, 0);

                for (LivingEntity target : targets) {
                    target.hurt(caster.damageSources().magic(), drainPerTarget);
                    streamDrainParticles(caster.level(), target, casterChest);
                    rawCultivation += CULTIVATION_PER_ENTITY;
                }

                double pathBonus = Math.max(
                        1.0D, entityData.getPathBonusHandler().getPathBonus(ESSENCE_PATH)
                );
                CultivateEvent event = new CultivateEvent(
                        caster, rawCultivation * pathBonus, ESSENCE_PATH, List.of()
                );
                NeoForge.EVENT_BUS.post(event);

                advanceCultivation(entityData, pathData, technique, rawData, event.getRate());
            }

            if (caster instanceof Player player) {
                pathData.sync(player);
            }
        }

        return caster.getData(ModAttachments.INPUT_STATES).isHeld("skill_cast");
    }


    private void streamDrainParticles(
            net.minecraft.world.level.Level level,
            LivingEntity target,
            Vec3 casterChest
    ) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 src = target.position().add(0, target.getBbHeight() * 0.5, 0);

        // Full vector from target to caster
        Vec3 toPlayer   = casterChest.subtract(src);
        double distance = toPlayer.length();
        if (distance < 0.5) return;

        Vec3 dir = toPlayer.normalize();
        double vx = dir.x * PARTICLE_SPEED;
        double vy = dir.y * PARTICLE_SPEED;
        double vz = dir.z * PARTICLE_SPEED;

        for (int i = 1; i <= STREAM_POINTS; i++) {
            double t = (double) i / (STREAM_POINTS + 1);

            double px = src.x + toPlayer.x * t + jitter();
            double py = src.y + toPlayer.y * t + jitter() * 0.5;
            double pz = src.z + toPlayer.z * t + jitter();


            serverLevel.sendParticles(
                    ParticleTypes.CRIMSON_SPORE,
                    px, py, pz,
                    1,
                    vx, vy, vz,
                    0.0
            );
        }
    }

    private double jitter() {
        return (Math.random() - 0.5) * 2.0 * STREAM_JITTER;
    }

    // ── Cultivation advancement ───────────────────────────────────────────────

    private void advanceCultivation(
            IEntityData entityData,
            PathData pathData,
            BloodfeastSoulRefiningTechnique technique,
            ITechniqueData rawData,
            double cultivationGain
    ) {
        double maxQi = technique.getMaxQiForRealm(pathData.getMajorRealm(), pathData.getMinorRealm());

        if (pathData.getCurrentRealmProgress() + cultivationGain >= maxQi) {
            pathData.setCurrentRealmProgress(maxQi);

            // Minor realm breakthrough
            if (
                    pathData.getMinorRealm() < technique.getMaxMinorRealm(pathData.getMajorRealm())
                            && technique.canBreakthroughMinorRealm(
                            entityData,
                            pathData.getMajorRealm(),
                            pathData.getMinorRealm(),
                            pathData.getCurrentRealmProgress()
                    )
            ) {
                pathData.handleRealmChange(
                        pathData.getMajorRealm(),
                        pathData.getMinorRealm() + 1,
                        entityData
                );
            }
            // Major realm breakthrough — gate-gated
            else if (
                    pathData.getMajorRealm() < technique.getMaxMajorRealm()
                            && technique.getStabilityHandler() != null
                            && pathData.getCurrentRealmStability() < technique.getStabilityHandler().getMaxCultivationTicks()
            ) {
                int nextMajor = pathData.getMajorRealm() + 1;

                if (technique.canCultivateMajorRealm(rawData, nextMajor)) {
                    // Snapshot the player-only window NOW, before the
                    // breakthrough fires and realm numbers change.
                    if (rawData instanceof BloodfeastTechniqueData data) {
                        data.onGateCleared(nextMajor);
                    }

                    IBreakthroughInstance instance = new NineHeavenlyTribulations(1);
                    pathData.setBreakthroughInstance(instance);
                    pathData.setBreakingThrough(true);
                }
                // Kill gate not met → cultivation stalls at cap, no breakthrough.
            }
        } else {
            pathData.setCurrentRealmProgress(pathData.getCurrentRealmProgress() + cultivationGain);
        }
    }

    // ── Metadata ─────────────────────────────────────────────────────────────

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public Component getTitle() {
        return Component.translatable("ascension.skill.bloodfeast_banquet");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("ascension.skill.bloodfeast_banquet.description");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getInformationContainer(UIFrame frame) {
        return new DescriptionDisplayContainer(frame, getTitle(), getDescription());
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(
                        AscensionCraft.MOD_ID,
                        "textures/spells/icon/bloodfeast_banquet.png"
                ),
                16, 16
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getCastElement(UIFrame frame) {
        return new CultivationProgressBar(
                frame,
                new TextureDataSubsection(
                        ResourceLocation.fromNamespaceAndPath(
                                AscensionCraft.MOD_ID,
                                "textures/gui/overlay/overlays_all.png"
                        ),
                        256, 256, 0, 0, 65, 7
                ),
                ESSENCE_PATH
        );
    }

    // ── No-op stubs ───────────────────────────────────────────────────────────
    @Override public void onEquip(IEntityData entityData) {}
    @Override public void onUnEquip(IEntityData entityData, IPreCastData preCastData) {}
    @Override public void finalCast(CastEndData reason, Entity caster, ICastData castData) {}
    @Override public void initialCast(Entity caster, IPreCastData preCastData) {}
    @Override public int getCooldown(CastEndData castEndData) { return 0; }
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

