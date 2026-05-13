package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.elemental;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.particle.aura.AuraColourHelper;
import net.thejadeproject.ascension.particle.aura.AuraParticleColor;
import net.thejadeproject.ascension.particle.aura.AuraParticlePresets;
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
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.ElementalEssenceTechnique;

import java.util.List;

public abstract class ElementalEssenceCultivationSkill implements ICastableSkill {

    private static final ResourceLocation ESSENCE_PATH = ModPaths.ESSENCE.getId();
    private static final double STANDARD_ESSENCE_RATE = 2.0D;

    protected abstract ResourceLocation getElementPath();

    protected abstract double getEnvironmentMultiplier(Entity caster);

    protected abstract Class<? extends ElementalEssenceTechnique> getTechniqueClass();

    protected Component getElementTitle() {
        var pathObj = AscensionRegistries.Paths.PATHS_REGISTRY.get(getElementPath());

        return pathObj != null ? pathObj.getDisplayTitle() : Component.literal(getElementPath().toString());
    }

    protected String getTranslationName() {
        return getElementPath().getPath() + "_essence_cultivation";
    }

    @Override
    public CastResult canCast(Entity caster, IPreCastData preCastData) {
        return new CastResult(CastResult.Type.SUCCESS);
    }

    @Override
    public boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData) {
        if (!caster.hasData(ModAttachments.INPUT_STATES)) return false;

//        if (caster.level() instanceof ServerLevel serverLevel && ticksElapsed % 2 == 0) {
//            spawnTemporaryCultivationAura(serverLevel, caster, ticksElapsed);
//        }

        if (!caster.level().isClientSide()) {
            IEntityData entityData = caster.getData(ModAttachments.ENTITY_DATA);
            PathData pathData = entityData.getPathData(ESSENCE_PATH);

            if (pathData == null) return false;
            if (pathData.isBreakingThrough()) return false;
            if (pathData.getLastUsedTechnique() == null) return false;

            ITechnique rawTechnique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(
                    pathData.getLastUsedTechnique()
            );

            if (!getTechniqueClass().isInstance(rawTechnique)) {
                return false;
            }

            ElementalEssenceTechnique technique = getTechniqueClass().cast(rawTechnique);

            double essenceBonus = Math.max(
                    1.0D,
                    entityData.getPathBonusHandler().getPathBonus(ESSENCE_PATH)
            );

            double elementBonus = Math.max(
                    1.0D,
                    entityData.getPathBonusHandler().getPathBonus(getElementPath())
            );

            double base = STANDARD_ESSENCE_RATE
                    * getEnvironmentMultiplier(caster)
                    * essenceBonus
                    * elementBonus;

            CultivateEvent event = new CultivateEvent(
                    caster,
                    base,
                    ESSENCE_PATH,
                    List.of(getElementPath())
            );

            NeoForge.EVENT_BUS.post(event);

            double maxQi = technique.getMaxQiForRealm(
                    pathData.getMajorRealm(),
                    pathData.getMinorRealm()
            );

            if (pathData.getCurrentRealmProgress() + event.getRate() >= maxQi) {
                pathData.setCurrentRealmProgress(maxQi);

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
                } else if (
                        pathData.getMajorRealm() < technique.getMaxMajorRealm()
                                && technique.getStabilityHandler() != null
                                && pathData.getCurrentRealmStability() < technique.getStabilityHandler().getMaxCultivationTicks()
                ) {
                    IBreakthroughInstance instance = new NineHeavenlyTribulations(1);

                    pathData.setBreakthroughInstance(instance);
                    pathData.setBreakingThrough(true);
                }
            } else {
                pathData.setCurrentRealmProgress(
                        pathData.getCurrentRealmProgress() + event.getRate()
                );
            }

            if (caster instanceof Player player) {
                pathData.sync(player);
            }
        }

        return caster.getData(ModAttachments.INPUT_STATES).isHeld("skill_cast");
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

//    private void spawnTemporaryCultivationAura(ServerLevel level, Entity caster, int ticksElapsed) {
//        AuraParticleColor baseColor = AuraParticleColor.forPath(getElementPath());
//        AuraParticleColor pulsedBaseColor = AuraColourHelper.pulse(baseColor, ticksElapsed, 0.12f, 0.18f);
//
//        if (ticksElapsed % 4 == 0) {
//            int smokeCount = 1 + level.random.nextInt(2);
//
//            for (int i = 0; i < smokeCount; i++) {
//                double angle = (Math.random() * Math.PI * 2.0) + Math.sin(ticksElapsed * 0.04) * 0.6;
//                double radius = 0.35 + Math.random() * 0.75;
//                if (Math.random() < 0.35) {
//                    radius *= 0.45;
//                }
//
//                double x = caster.getX() + Math.cos(angle) * radius;
//                double y = caster.getY() + 0.02 + Math.random() * 0.20;
//                double z = caster.getZ() + Math.sin(angle) * radius;
//
//                AuraParticleColor smokeColor = AuraColourHelper.vary(
//                        pulsedBaseColor,
//                        0.025f,
//                        0.14f,
//                        0.22f
//                );
//
//                float smokeScale = 1.8f + level.random.nextFloat() * 1.2f;
//                int smokeLifetime = 55 + level.random.nextInt(25);
//
//                level.sendParticles(
//                        AuraParticlePresets.customSmoke(smokeColor, smokeScale, smokeLifetime),
//                        x, y, z,
//                        1,
//                        Math.cos(angle) * 0.004,
//                        0.008 + Math.random() * 0.006,
//                        Math.sin(angle) * 0.004,
//                        0.006
//                );
//            }
//        }
//
//        if (ticksElapsed % 4 == 0) {
//            int flameCount = 2;
//
//            for (int i = 0; i < flameCount; i++) {
//                double angle = Math.random() * Math.PI * 2.0;
//                double radius = 0.45 + Math.random() * 0.55;
//
//                double x = caster.getX() + Math.cos(angle) * radius;
//                double y = caster.getY() + 0.04 + Math.random() * 0.15;
//                double z = caster.getZ() + Math.sin(angle) * radius;
//
//                AuraParticleColor flameColor = AuraColourHelper.vary(
//                        pulsedBaseColor,
//                        0.045f,
//                        0.16f,
//                        0.25f
//                );
//
//                double targetX = caster.getX();
//                double targetY = caster.getY() + 0.55;
//                double targetZ = caster.getZ();
//
//                level.sendParticles(
//                        AuraParticlePresets.customFlame(flameColor, 1.6f, 36),
//                        x, y, z,
//                        1,
//                        (targetX - x) * 0.10,
//                        0.035 + (targetY - y) * 0.06,
//                        (targetZ - z) * 0.10,
//                        0.0
//                );
//            }
//        }
//
//        if (ticksElapsed % 6 == 0) {
//            int sparkCount = 1;
//
//            for (int i = 0; i < sparkCount; i++) {
//                double angle = Math.random() * Math.PI * 2.0;
//                double radius = 0.55 + Math.random() * 0.45;
//
//                double x = caster.getX() + Math.cos(angle) * radius;
//                double y = caster.getY() + 0.08 + Math.random() * 0.45;
//                double z = caster.getZ() + Math.sin(angle) * radius;
//
//                double targetX = caster.getX();
//                double targetY = caster.getY() + 0.55;
//                double targetZ = caster.getZ();
//
//                AuraParticleColor sparkColor = AuraColourHelper.vary(
//                        pulsedBaseColor,
//                        0.065f,
//                        0.18f,
//                        0.30f
//                );
//
//                level.sendParticles(
//                        AuraParticlePresets.customSpark(sparkColor, 1.9f, 28),
//                        x, y, z,
//                        1,
//                        (targetX - x) * 0.28,
//                        (targetY - y) * 0.22,
//                        (targetZ - z) * 0.28,
//                        0.0
//                );
//            }
//        }
//    }

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
                        256,
                        256,
                        0,
                        0,
                        65,
                        7
                ),
                ESSENCE_PATH
        );
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(
                        AscensionCraft.MOD_ID,
                        "textures/spells/icon/placeholder.png"
                ),
                16,
                16
        );
    }

    @Override
    public Component getTitle() {
        return Component.translatable(
                "ascension.skill.elemental_essence_cultivation",
                getElementTitle()
        );
    }

    @Override
    public Component getDescription() {
        return Component.translatable(
                "ascension.skill." + getTranslationName() + ".description"
        );
    }

    protected int countNearbyBlocks(Entity caster, int radius, java.util.function.Predicate<net.minecraft.world.level.block.state.BlockState> predicate) {
        net.minecraft.world.level.Level level = caster.level();
        net.minecraft.core.BlockPos center = caster.blockPosition();

        int count = 0;

        for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )) {
            if (predicate.test(level.getBlockState(pos))) {
                count++;
            }
        }

        return count;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getInformationContainer(UIFrame frame) {
        return new DescriptionDisplayContainer(frame,
                getTitle(),
                getDescription());
    }
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