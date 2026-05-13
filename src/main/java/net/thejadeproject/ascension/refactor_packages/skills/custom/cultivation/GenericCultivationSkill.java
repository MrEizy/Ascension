package net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation;

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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.breakthroughs.IBreakthroughInstance;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.DescriptionDisplayContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.skills.cultivation.CultivationProgressBar;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastEndData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastEndReason;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastResult;
import net.thejadeproject.ascension.refactor_packages.skills.IPersistentSkillData;
import net.thejadeproject.ascension.refactor_packages.skills.ISkill;
import net.thejadeproject.ascension.refactor_packages.skills.castable.CastType;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastData;
import net.thejadeproject.ascension.refactor_packages.skills.castable.ICastableSkill;
import net.thejadeproject.ascension.refactor_packages.skills.castable.IPreCastData;
import net.thejadeproject.ascension.refactor_packages.skills.custom.cultivation.skill_data.GenericCultivationSkillData;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;
import org.checkerframework.checker.guieffect.qual.UI;

import java.util.Set;

public class GenericCultivationSkill implements ICastableSkill {


    private double baseRate;
    private ResourceLocation path;

    public GenericCultivationSkill(double baseRate,ResourceLocation path){
        this.baseRate = baseRate;
        this.path = path;
    }

    @Override
    public void onAdded(IEntityData attachedEntityData) {
        //System.out.println("added skill : "+ AscensionRegistries.Skills.SKILL_REGISTRY.getKey(this).toString());
    }

    @Override
    public void onRemoved(IEntityData attachedEntityData, IPersistentSkillData persistentData) {
        //System.out.println("removed skill : "+ AscensionRegistries.Skills.SKILL_REGISTRY.getKey(this).toString());
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
        return new GenericCultivationSkillData(baseRate,Set.of());
    }

    @Override
    public IPersistentSkillData fromCompound(CompoundTag tag, IEntityData heldEntity) {
        return new GenericCultivationSkillData(tag);
    }

    @Override
    public IPersistentSkillData fromNetwork(RegistryFriendlyByteBuf buf) {
        return new GenericCultivationSkillData(buf);
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextureData getIcon() {
        return new TextureData(
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"textures/spells/icon/placeholder.png"),
                16,16
        );
    }

    private Component getPathTitle() {
        var pathObj = AscensionRegistries.Paths.PATHS_REGISTRY.get(path);
        return pathObj != null ? pathObj.getDisplayTitle() : Component.literal(path.toString());
    }

    @Override
    public Component getTitle() {
        return Component.translatable(
                "ascension.skill.cultivation_skill",
                getPathTitle()
        );
    }

    @Override
    public Component getDescription() {
        return Component.translatable(
                "ascension.skill.cultivation_skill.description",
                getPathTitle()
        );
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
        //TODO add slow

    }

    @Override
    public CastResult canCast(Entity caster, IPreCastData preCastData) {

        return new CastResult(CastResult.Type.SUCCESS);
    }

    protected double getEffectiveRate(Entity caster) {
        return baseRate;
    }

    @Override
    public boolean continueCasting(int ticksElapsed, Entity caster, ICastData castData) {
        if(!caster.hasData(ModAttachments.INPUT_STATES)) return false;

        if(!caster.level().isClientSide()){

            //System.out.println("Player is trying to cultivate");
            PathData pathData = caster.getData(ModAttachments.ENTITY_DATA).getPathData(path);

            if (pathData == null || pathData.getLastUsedTechnique() == null) return false;

            //TODO add a cultivate event
            ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(pathData.getLastUsedTechnique());
            if (technique == null) return false;
            double amount = getEffectiveRate(caster);


            if(pathData.getCurrentRealmProgress()+amount >= technique.getMaxQiForRealm(pathData.getMajorRealm(),pathData.getMinorRealm())){
                //TODO minor/major realm breakthrough shenanigans here
                pathData.setCurrentRealmProgress(technique.getMaxQiForRealm(pathData.getMajorRealm(),pathData.getMinorRealm()));

                if(pathData.getMinorRealm() < technique.getMaxMinorRealm(pathData.getMajorRealm()) && technique.canBreakthroughMinorRealm(
                        caster.getData(ModAttachments.ENTITY_DATA),
                        pathData.getMajorRealm(),
                        pathData.getMinorRealm(),
                        pathData.getCurrentRealmProgress()
                )){
                    pathData.handleRealmChange(pathData.getMajorRealm(),pathData.getMinorRealm()+1,caster.getData(ModAttachments.ENTITY_DATA));
                } else if (
                        pathData.getMajorRealm() < technique.getMaxMajorRealm()
                                && technique.canBreakthrough(
                                caster.getData(ModAttachments.ENTITY_DATA),
                                pathData.getMajorRealm(),
                                pathData.getMinorRealm(),
                                pathData.getCurrentRealmProgress()
                        )
                ) {
                    IBreakthroughInstance instance = technique.freshBreakthroughData(
                            caster.getData(ModAttachments.ENTITY_DATA)
                    );

                    if (instance != null) {
                        pathData.setBreakthroughInstance(instance);
                        pathData.setBreakingThrough(true);
                    }
                }
            }else {
                pathData.setCurrentRealmProgress(pathData.getCurrentRealmProgress()+amount);
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
    public RenderableElement getInformationContainer(UIFrame frame) {
        return new DescriptionDisplayContainer(frame,
                getTitle(),
                getDescription());
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
                ),path);
    }
}
