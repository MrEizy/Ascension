package net.thejadeproject.ascension.refactor_packages.forms.forms.generic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodline;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodlineData;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityForm;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.physique.SyncPhysique;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.skills.SyncHeldSkills;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.HeldSkills;
import net.thejadeproject.ascension.refactor_packages.stats.StatSheet;
import net.thejadeproject.ascension.refactor_packages.stats.custom.ModStats;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class GenericFormData implements IEntityFormData {
    private UUID attachedEntity;
    private final ResourceLocation formId;
    private final HashMap<ResourceLocation,PathData> pathData = new HashMap<>();

    private ResourceLocation physique;
    private IPhysiqueData physiqueData;

    private ResourceLocation bloodline;
    private IBloodlineData bloodlineData;

    private StatSheet statSheet;

    private HeldSkills heldSkills;
    public GenericFormData(ResourceLocation formId){

        this.formId = formId;
        heldSkills  = new HeldSkills();
        initStatSheet();
    }

    public void initStatSheet(){
        statSheet = new StatSheet();
        statSheet.addStat(ModStats.VITALITY.get(),5);
        statSheet.addStat(ModStats.AGILITY.get(),5);
        statSheet.addStat(ModStats.STRENGTH.get(),5);
        statSheet.addStat(ModStats.INTELLIGENCE.get(),5);
    }

    @Override
    public UUID getAttachedEntity() {
        return attachedEntity;
    }

    @Override
    public void setAttachedEntity(UUID entity) {
        this.attachedEntity = entity;
    }

    @Override
    public ResourceLocation getEntityFormId() {
        return formId;
    }

    @Override
    public IEntityForm getEntityForm() {
        return AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.get(formId);
    }

    @Override
    public void addPathData(ResourceLocation path, PathData pathData) {
        this.pathData.put(path,pathData);
    }

    @Override
    public PathData getPathData(ResourceLocation path) {
        return pathData.get(path);
    }

    @Override
    public void removePathData(ResourceLocation path) {
        pathData.remove(path);
    }

    @Override
    public Collection<PathData> getAllPathData() {
        return pathData.values();
    }

    @Override
    public Collection<ResourceLocation> getPaths() {
        return pathData.keySet();
    }

    @Override
    public boolean hasPathData(ResourceLocation path) {
        return pathData.containsKey(path);
    }

    @Override
    public HeldSkills getHeldSkills() {
        return heldSkills;
    }

    @Override
    public void setHeldSkills(HeldSkills heldSkills) {
        this.heldSkills = heldSkills;
    }


    @Override
    public ResourceLocation getPhysiqueKey() {
        return physique;
    }

    @Override
    public IPhysique getPhysique() {
        return AscensionRegistries.Physiques.PHSIQUES_REGISTRY.get(getPhysiqueKey());
    }

    @Override
    public IPhysiqueData getPhysiqueData() {
        return physiqueData;
    }

    @Override
    public void setPhysique(ResourceLocation physique) {
        setPhysique(physique,null);
    }

    @Override
    public void setPhysique(ResourceLocation physique, IPhysiqueData physiqueData) {
        this.physique = physique;
        this.physiqueData = physiqueData;
    }


    @Override
    public ResourceLocation getBloodlineKey() {
        return bloodline;
    }

    @Override
    public IBloodline getBloodline() {
        return AscensionRegistries.Bloodlines.BLOODLINE_REGISTRY.get(getBloodlineKey());
    }

    @Override
    public IBloodlineData getBloodlineData() {
        return bloodlineData;
    }

    @Override
    public void setBloodline(ResourceLocation bloodline) {
        setBloodline(bloodline,null);
    }

    @Override
    public void setBloodline(ResourceLocation bloodline, IBloodlineData bloodlineData) {
        this.bloodline = bloodline;
        this.bloodlineData = bloodlineData;
    }

    @Override
    public StatSheet getStatSheet() {
        return statSheet;
    }

    @Override
    public CompoundTag write() {
        return new CompoundTag();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        //TODO meant to write EVERYTHING. including the stats, physiques and bloodlines
    }

    public void syncHeldSkills(Player player){
        PacketDistributor.sendToPlayer((ServerPlayer) player,new SyncHeldSkills(getEntityFormId().toString(),heldSkills));
    }
    public void syncPhysique(Player player){
        PacketDistributor.sendToPlayer((ServerPlayer) player,new SyncPhysique(getEntityFormId(),physique,physiqueData));
    }
    public void syncStatSheet(Player player){

    }
    //for now also syncs all the skills and such
    public void syncForm(Player player){

    }
    public void syncPathData(Player player){

    }


}
