package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.five_element;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.FiveElementCultivationTechnique;

import java.util.List;

public class FiveElementCirculation implements ICastableSkill {
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
        if(!caster.hasData(ModAttachments.INPUT_STATES)) return false;
        if(castData == null) return false;
        if(!(castData instanceof FiveElementCirculationCastData fiveElementCirculationCastData)) return false;
        fiveElementCirculationCastData.tick();
        if(!caster.level().isClientSide()){

            IEntityData entityData = caster.getData(ModAttachments.ENTITY_DATA);
            //System.out.println("Player is trying to cultivate");
            PathData pathData = caster.getData(ModAttachments.ENTITY_DATA).getPathData(ModPaths.ESSENCE.getId());

            if(pathData == null) return false;
            if(pathData.isBreakingThrough()) return false;

            //TODO add a cultivate event
            ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(pathData.getLastUsedTechnique());
            if(!(technique instanceof FiveElementCultivationTechnique)) return false;

            ResourceLocation element = fiveElementCirculationCastData.getElement();

            //TODO add a cultivate event
            double base = 2.0*(entityData.getPathBonusHandler().getPathBonus(ModPaths.ESSENCE.getId()))*(entityData.getPathBonusHandler().getPathBonus(element)); //TODO set up with some sort of variable
            CultivateEvent event = new CultivateEvent(caster,base,ModPaths.ESSENCE.getId(), List.of(element));
            NeoForge.EVENT_BUS.post(event);
            event.log();
            if(pathData.getCurrentRealmProgress()+event.getRate() >= technique.getMaxQiForRealm(pathData.getMajorRealm(),pathData.getMinorRealm())){
                //TODO minor/major realm breakthrough shenanigans here
                pathData.setCurrentRealmProgress(technique.getMaxQiForRealm(pathData.getMajorRealm(),pathData.getMinorRealm()));

                //TODO for now just force breakthrough
                if(pathData.getMinorRealm() < technique.getMaxMinorRealm(pathData.getMajorRealm()) && technique.canBreakthroughMinorRealm(
                        caster.getData(ModAttachments.ENTITY_DATA),
                        pathData.getMajorRealm(),
                        pathData.getMinorRealm(),
                        pathData.getCurrentRealmProgress()
                )){
                    pathData.handleRealmChange(pathData.getMajorRealm(),pathData.getMinorRealm()+1,caster.getData(ModAttachments.ENTITY_DATA));
                } else if(pathData.getMajorRealm()<technique.getMaxMajorRealm() && technique.getStabilityHandler() != null && pathData.getCurrentRealmStability() < technique.getStabilityHandler().getMaxCultivationTicks()) {
                    IBreakthroughInstance instance = new NineHeavenlyTribulations(1);
                    pathData.setBreakthroughInstance(instance);
                    pathData.setBreakingThrough(true);
                    //pathData.handleRealmChange(pathData.getMajorRealm()+1,0,entityData);
                    //pathData.setCurrentRealmStability(pathData.getCurrentRealmStability()+1);
                }
            }else {
                pathData.setCurrentRealmProgress(pathData.getCurrentRealmProgress()+event.getRate());
            }

            if(caster instanceof Player player) pathData.sync(player);

        }

        return caster.getData(ModAttachments.INPUT_STATES).isHeld("skill_cast");
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
        return new FiveElementCirculationCastData();
    }

    @Override
    public ICastData castDataFromCompound(CompoundTag tag) {
        return new FiveElementCirculationCastData();
    }

    @Override
    public ICastData castDataFromNetwork(RegistryFriendlyByteBuf buf) {
        return new FiveElementCirculationCastData(buf);
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
        return new CultivationProgressBar(frame,
                new TextureDataSubsection(
                        ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"textures/gui/overlay/overlays_all.png"),
                        256,256,
                        0,0,
                        65,7
                ),ModPaths.ESSENCE.getId());
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
    @OnlyIn(Dist.CLIENT)
    @Override
    public RenderableElement getInformationContainer(UIFrame frame) {
        return new DescriptionDisplayContainer(frame,
                getTitle(),
                getDescription());
    }
    @Override
    public Component getTitle() {
        return Component.translatable("ascension.skill.five_element_circulation");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("ascension.skill.five_element_circulation.description");
    }
}
