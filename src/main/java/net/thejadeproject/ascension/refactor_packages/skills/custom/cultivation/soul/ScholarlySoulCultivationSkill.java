package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.soul;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
import net.thejadeproject.ascension.refactor_packages.techniques.custom.soul.ScholarlySoulTechnique;

import java.util.List;

public class ScholarlySoulCultivationSkill implements ICastableSkill {

    private static final ResourceLocation SOUL_PATH = ModPaths.SOUL.getId();
    private static final double BASE_RATE = 2.0D;

    private static final int BOOKSHELF_RANGE = 4;
    private static final int MAX_BOOKSHELF_COUNT = 24;
    private static final double BOOKSHELF_BONUS_PER_BLOCK = 0.025D;
    private static final double MAX_BOOKSHELF_MULTIPLIER = 1.60D;

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
        return new CastResult(CastResult.Type.SUCCESS);
    }

    @Override
    public boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData) {
        if (!caster.hasData(ModAttachments.INPUT_STATES)) return false;

        if (!caster.level().isClientSide()) {
            IEntityData entityData = caster.getData(ModAttachments.ENTITY_DATA);
            PathData pathData = entityData.getPathData(SOUL_PATH);

            if (pathData == null) return false;
            if (pathData.isBreakingThrough()) return false;
            if (pathData.getLastUsedTechnique() == null) return false;

            ITechnique rawTechnique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(pathData.getLastUsedTechnique());

            if (!(rawTechnique instanceof ScholarlySoulTechnique technique)) {
                return false;
            }

            ITechniqueData techniqueData = pathData.getTechniqueData(pathData.getLastUsedTechnique());

            if (!technique.canCultivateMajorRealm(techniqueData, pathData.getMajorRealm())) {
                return false;
            }

            double pathBonus = Math.max(1.0D, entityData.getPathBonusHandler().getPathBonus(SOUL_PATH));
            double bookshelfMultiplier = getBookshelfCultivationMultiplier(caster);
            double base = BASE_RATE * pathBonus * bookshelfMultiplier;

            CultivateEvent event = new CultivateEvent(
                    caster,
                    base,
                    SOUL_PATH,
                    List.of()
            );

            NeoForge.EVENT_BUS.post(event);

            if (pathData.getCurrentRealmProgress() + event.getRate() >= technique.getMaxQiForRealm(pathData.getMajorRealm(), pathData.getMinorRealm())) {
                pathData.setCurrentRealmProgress(
                        technique.getMaxQiForRealm(pathData.getMajorRealm(), pathData.getMinorRealm())
                );

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
                } else {
                    int nextMajorRealm = pathData.getMajorRealm() + 1;

                    if (
                            technique.canCultivateMajorRealm(techniqueData, nextMajorRealm)
                                    && pathData.getMajorRealm() < technique.getMaxMajorRealm()
                                    && technique.getStabilityHandler() != null
                                    && pathData.getCurrentRealmStability() < technique.getStabilityHandler().getMaxCultivationTicks()
                    ) {
                        IBreakthroughInstance instance = new NineHeavenlyTribulations(1);

                        pathData.setBreakthroughInstance(instance);
                        pathData.setBreakingThrough(true);
                    }
                }
            } else {
                pathData.setCurrentRealmProgress(pathData.getCurrentRealmProgress() + event.getRate());
            }

            if (caster instanceof Player player) {
                pathData.sync(player);
            }
        }

        return caster.getData(ModAttachments.INPUT_STATES).isHeld("skill_cast");
    }

    private static double getBookshelfCultivationMultiplier(Entity caster) {
        Level level = caster.level();
        BlockPos center = caster.blockPosition();

        int bookshelfCount = countNearbyBookshelves(level, center);

        return Math.min(
                MAX_BOOKSHELF_MULTIPLIER,
                1.0D + bookshelfCount * BOOKSHELF_BONUS_PER_BLOCK
        );
    }

    private static int countNearbyBookshelves(Level level, BlockPos center) {
        int count = 0;

        BlockPos min = center.offset(-BOOKSHELF_RANGE, -BOOKSHELF_RANGE, -BOOKSHELF_RANGE);
        BlockPos max = center.offset(BOOKSHELF_RANGE, BOOKSHELF_RANGE, BOOKSHELF_RANGE);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);

            if (isBookshelf(state)) {
                count++;

                if (count >= MAX_BOOKSHELF_COUNT) {
                    return MAX_BOOKSHELF_COUNT;
                }
            }
        }

        return count;
    }

    private static boolean isBookshelf(BlockState state) {
        return state.is(Blocks.BOOKSHELF)
                || state.is(Blocks.CHISELED_BOOKSHELF);
    }

    @Override
    public int getCooldown(CastEndData castEndData) {
        return 0;
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
                SOUL_PATH
        );
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
                ResourceLocation.fromNamespaceAndPath(
                        AscensionCraft.MOD_ID,
                        "textures/spells/icon/scholarly_soul_cultivation_skill.png"
                ),
                16,
                16
        );
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ascension.skill.scholarly_soul_cultivation_skill");
    }

    @Override
    public Component getDescription() {
        return Component.translatable(
                "ascension.skill.scholarly_soul_cultivation_skill.description"
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getInformationContainer(UIFrame frame) {
        return new DescriptionDisplayContainer(frame,getTitle(),getDescription());
    }
}