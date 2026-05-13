package net.thejadeproject.ascension.refactor_packages.entity_data;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.attributes.AscensionAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.attributes.AttributeValueContainer;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodline;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodlineData;
import net.thejadeproject.ascension.refactor_packages.bloodlines.ModBloodlines;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.IEntityDataSource;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.IEntityDataSourceContainer;
import net.thejadeproject.ascension.refactor_packages.events.PhysiqueChangeEvent;
import net.thejadeproject.ascension.refactor_packages.events.path_data.TryRemovePathDataEvent;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityForm;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.SyncEntityForm;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncCurrentHealth;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.bloodline.SyncBloodline;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.path_data.SyncPathData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.physique.SyncPhysique;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.skills.SyncHeldSkills;
import net.thejadeproject.ascension.refactor_packages.paths.IPath;
import net.thejadeproject.ascension.refactor_packages.paths.PathBonusHandler;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysiqueData;

import net.thejadeproject.ascension.refactor_packages.physiques.ModPhysiques;
import net.thejadeproject.ascension.refactor_packages.qi.EntityQiContainer;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skill_casting.SkillCastHandler;
import net.thejadeproject.ascension.refactor_packages.skills.*;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;

import java.util.*;
//TODO set up some sort of tick handler for entity data and all its parts that need it
public class GenericEntityData implements IEntityData {
    private ResourceLocation activeForm = ModForms.MORTAL_VESSEL.getId();
    private Entity attachedEntity;

    private final SkillCastHandler skillCastHandler = new SkillCastHandler();
    private final HashMap<ResourceLocation, IEntityFormData> heldFormData = new HashMap<>();

    private final HashMap<ResourceLocation,ResourceLocation> pathDataLocation = new HashMap<>();

    private ResourceLocation physiqueForm; //the form holding the data of the physique
    private ResourceLocation bloodlineForm;//almost always body (but might have some rare circumstances where it is not)

    //not final since in the future we will be able to change it
    private AscensionAttributeHolder ascensionAttributeHolder;
    private final PathBonusHandler pathBonusHandler = new PathBonusHandler();
    private final EntityQiContainer entityQiContainer = new EntityQiContainer(this);
    boolean attachedEntityLoaded;

    private HashMap<ResourceLocation,IEntityDataSourceContainer> sourceContainers = new HashMap<>();

    //used during loading to temporarily store data, and when we save ignore.
    //important because during simulation something might exist before we actually made any data for it
    private HashMap<ResourceLocation,IEntityFormData> cachedFormData = new HashMap<>();
    private HashMap<ResourceLocation, IPersistentSkillData> cachedSkillData = new HashMap<>();
    private HashMap<ResourceLocation,CompoundTag> cachedPathDataTags = new HashMap<>();
    //for when a physique or something adds path to a form that is not yet initialized
    //when a form is added if it has cached paths add the cached paths(which in turn draw from their cached data)
    private HashMap<ResourceLocation,HashSet<ResourceLocation>> pathDataForFormCache = new HashMap<>();
    private double currentHealth = 0;
    private boolean loading = false;

    @Override
    public boolean isLoading() { return loading; }
    //========================== SAVE DATA HANDLING ==========================
    public GenericEntityData(Entity attachedEntity){
        this.attachedEntity = attachedEntity;

        //TODO add mortal vessel
        heldFormData.put(ModForms.MORTAL_VESSEL.getId(),ModForms.MORTAL_VESSEL.get().freshEntityFormData(this));

        //TODO add soul form
        heldFormData.put(ModForms.SOUL_FORM.getId(),ModForms.SOUL_FORM.get().freshEntityFormData(this));

        if(attachedEntity instanceof LivingEntity entity){
            ascensionAttributeHolder = new AscensionAttributeHolder(entity);
            addDefaultAttributes(entity);

        }
        getQiContainer().fullFillQi();
        currentHealth = getAscensionAttributeHolder().getAttribute(Attributes.MAX_HEALTH).getValue();

        setPhysique(ModPhysiques.MORTAL.getId());//give default physique
        //setBloodline(ModBloodlines.MORTAL_BLOODLINE.getId());

    }

    /**
     * reads and loads an initial data state, then "simulates" progression, whilst keeping a cache of any data that was not added
     * during the initialization
     * other than the built-in forms nothing else is added unless it has a source:
     * a source can either be:
     * a form
     * a physique
     * a bloodline
     * a technique
     * an entity_data_source <- a catch-all for sources not hardcoded into the system,
     *
     * load order:
     * forms
     * cached skill data
     * cached path data tag (to read it we simulate it so we store it as a data tag until its path is added)
     *
     *
     *
     * @param attachedEntity
     * @param tag
     */
    public GenericEntityData(Entity attachedEntity, CompoundTag tag){

        AscensionCraft.LOGGER.info("loading data for entity("+attachedEntity.getType()+") :"+attachedEntity.getName());
        this.attachedEntity = attachedEntity;

        ListTag formDataTags = tag.getList("form_data", Tag.TAG_COMPOUND);
        ListTag skillDataTags = tag.getList("skill_data",Tag.TAG_COMPOUND);
        ListTag pathDataTags = tag.getList("path_progress",Tag.TAG_COMPOUND);
        loading = true;
        try {
            for(int i=0;i<formDataTags.size();i++){
                try{
                    CompoundTag formDataTag = formDataTags.getCompound(i);
                    ResourceLocation formId = ResourceLocation.bySeparator(formDataTag.getString("form"),':');
                    IEntityForm form = AscensionRegistries.getRegistryObject(formId,AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY);
                    IEntityFormData formData = IEntityForm.getFromCompound(this,form,formDataTag.getCompound("data"));
                    cachedFormData.put(formId,formData);
                }catch (Exception e){
                    AscensionCraft.LOGGER.error("error trying to load individual form",e);
                }
            }
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error trying to load all forms",e);
            //TODO make sure to add back in MORTAL VESSEL AND SOUL_FORM
        }
        try{
            for(int i=0;i<skillDataTags.size();i++){
                try{
                    CompoundTag skillDataTag = skillDataTags.getCompound(i);
                    ResourceLocation skillId = ResourceLocation.bySeparator(skillDataTag.getString("skill"),':');
                    ISkill skill = AscensionRegistries.getRegistryObject(skillId,AscensionRegistries.Skills.SKILL_REGISTRY);
                    IPersistentSkillData skillData = ISkill.getFromCompound(this,skill,skillDataTag.getCompound("data"));
                    cachedSkillData.put(skillId,skillData);
                }catch (Exception e){
                    AscensionCraft.LOGGER.error("error trying to load individual skill",e);
                }
            }
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error trying to load all skill data",e);
        }

        try{
            if(tag.getBoolean("vessel_flag")){
                heldFormData.put(ModForms.MORTAL_VESSEL.getId(),
                        cachedFormData.containsKey(ModForms.MORTAL_VESSEL.getId()) ?
                                cachedFormData.get(ModForms.MORTAL_VESSEL.getId()) : ModForms.MORTAL_VESSEL.get().freshEntityFormData(this));
            }
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error trying to read lost mortal vessel flag",e);
        }

        heldFormData.put(ModForms.SOUL_FORM.getId(),
                cachedFormData.containsKey(ModForms.SOUL_FORM.getId()) ?
                        cachedFormData.get(ModForms.SOUL_FORM.getId()) : ModForms.SOUL_FORM.get().freshEntityFormData(this));

        if(attachedEntity instanceof LivingEntity entity){
            ascensionAttributeHolder = new AscensionAttributeHolder(entity);
            addDefaultAttributes(entity);
        }
        try {
            String rawPhysique = tag.getString("physique");
            //the user has no physique
            if(!rawPhysique.equals("none")){
                ResourceLocation physique = ResourceLocation.bySeparator(rawPhysique,':');
                IPhysique physiqueInstance = AscensionRegistries.getRegistryObject(physique,AscensionRegistries.Physiques.PHSIQUES_REGISTRY);
                IPhysiqueData physiqueData = IPhysique.getFromCompound(this,physiqueInstance,tag.getCompound("physique_data"));
                setPhysique(physique,physiqueData);
            }
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error when trying to load physique",e);
            //TODO make sure to set physique to default
            //if mortal vessel is present -> mortal
            //if soul is present -> smth?
        }
//        try {
//            String rawBloodline = tag.getString("bloodline");
//
//            if (rawBloodline.isBlank() || rawBloodline.equals("none")) {
//                setBloodline(ModBloodlines.MORTAL_BLOODLINE.getId());
//            } else {
//                ResourceLocation bloodline = ResourceLocation.bySeparator(rawBloodline, ':');
//                IBloodline bloodlineInstance = AscensionRegistries.getRegistryObject(
//                        bloodline,
//                        AscensionRegistries.Bloodlines.BLOODLINE_REGISTRY
//                );
//
//                IBloodlineData bloodlineData = IBloodline.getFromCompound(
//                        this,
//                        bloodlineInstance,
//                        tag.getCompound("bloodline_data")
//                );
//
//                setBloodline(bloodline, bloodlineData);
//            }
//        } catch (Exception e) {
//            AscensionCraft.LOGGER.error("error when trying to load bloodline", e);
//            setBloodline(ModBloodlines.MORTAL_BLOODLINE.getId());
//        }
        bloodlineForm = null;
        try{
            ListTag dataSources = tag.getList("entity_data_sources",Tag.TAG_COMPOUND);
            for(int i = 0;i<dataSources.size();i++){
                try {
                    CompoundTag dataSource = dataSources.getCompound(i);
                    ResourceLocation key = ResourceLocation.parse(dataSource.getString("type"));
                    IEntityDataSource source = AscensionRegistries.getRegistryObject(key,AscensionRegistries.EntityDataSources.ENTITY_DATA_SOURCES_REGISTRY);
                    IEntityDataSourceContainer container = source.fromCompound(dataSource);
                    sourceContainers.put(container.getInstanceIdentifier(),container);
                    container.getDataSource().onAdded(this,container);
                }catch (Exception e){
                    AscensionCraft.LOGGER.error("error loading entity data source container",e);
                }
            }
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error loading entity data sources",e);
        }

        try {
            for(int i = 0;i<pathDataTags.size();i++){
                try {
                    CompoundTag pathTag = pathDataTags.getCompound(i);
                    ResourceLocation pathId = ResourceLocation.parse(pathTag.getString("path"));

                    CompoundTag pathDataTag = pathTag.getCompound("data");

                    IPath path = AscensionRegistries.getRegistryObject(pathId,AscensionRegistries.Paths.PATHS_REGISTRY);

                    if (pathDataLocation.containsKey(pathId)){
                        //valid path was added
                        getEntityFormData(pathDataLocation.get(pathId)).getPathData(pathId).load(pathDataTag,this);
                    }else{
                        //either the path has not been added yet OR the form does nto exist
                        if(!hasForm(path.defaultForm())) {
                            pathDataForFormCache.computeIfAbsent(path.defaultForm(),(key)->new HashSet<>());
                            pathDataForFormCache.get(path.defaultForm()).add(pathId);
                        }
                        cachedPathDataTags.put(pathId,pathDataTag);
                    }

                }catch (Exception e){
                    AscensionCraft.LOGGER.error("error logging path",e);
                }


            }
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error loading all paths",e);
        }


        try {
            getSkillCastHandler().read(tag.getCompound("skill_cast_handler"));
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error loading skill cast handler");
        }
        getAscensionAttributeHolder().updateAttributes(this);
        if(tag.contains("qi")) entityQiContainer.setCurrentQi(tag.getDouble("qi"));
        else getQiContainer().fullFillQi();

        currentHealth = tag.getDouble("current_health");

        try {
            ListTag suppressors = tag.getList("suppressed_values",Tag.TAG_COMPOUND);
            for(int i = 0;i<suppressors.size();i++){
                CompoundTag attributeTag = suppressors.getCompound(i);

                Holder<Attribute> attributeHolder = BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(
                        ResourceKey.create(BuiltInRegistries.ATTRIBUTE.key(), ResourceLocation.parse(attributeTag.getString("attribute")))
                );

                getAscensionAttributeHolder().getAttribute(attributeHolder).setSuppressedValue(attributeTag.getDouble("value"));
                if (getAttachedEntity() instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
                    PacketDistributor.sendToPlayer(serverPlayer,new SyncAttributeHolder(getAscensionAttributeHolder()));
                }
            }
        }catch (Exception e){
            AscensionCraft.LOGGER.error("error loading suppressed values",e);
        }

        loading = false;
        clearCache();
    }


    public void clearCache(){
        cachedPathDataTags.clear();
        cachedFormData.clear();
        cachedSkillData.clear();
        pathDataForFormCache.clear();
    }

    public void sync(Player player){
        for(ResourceLocation form:heldFormData.keySet()){

            PacketDistributor.sendToPlayer((ServerPlayer) player,new SyncEntityForm(heldFormData.get(form)));
        }
    }


    @Override
    public void write(CompoundTag tag) {
        //if the player losses their vessel we do not want to accidentally make a new one
        tag.putBoolean("vessel_flag",heldFormData.containsKey(ModForms.MORTAL_VESSEL.getId()));
        tag.putDouble("current_health",currentHealth);
        tag.putString("physique",physiqueForm == null ? "none" : heldFormData.get(physiqueForm).getPhysiqueKey().toString());
        if (physiqueForm != null && heldFormData.get(physiqueForm).getPhysiqueData() != null) {
            tag.put("physique_data",heldFormData.get(physiqueForm).getPhysiqueData().write());
        }

//        tag.putString("bloodline", bloodlineForm == null ? "none" : heldFormData.get(bloodlineForm).getBloodlineKey().toString());
//
//        if (bloodlineForm != null && heldFormData.get(bloodlineForm).getBloodlineData() != null) {
//            tag.put("bloodline_data", heldFormData.get(bloodlineForm).getBloodlineData().write());
//        }

        tag.putString("bloodline", "none");

        ListTag formTags = new ListTag();
        //since this is only for caching only cache forms that have data
        //for skills keep track of what i have already saved, so i don't accidentally do dupes
        //only write skills that have skill data, otherwise don't
        HashSet<ResourceLocation> visitedSkills = new HashSet<>();
        ListTag skillTags = new ListTag();

        ListTag pathDataTags = new ListTag();
        for(ResourceLocation form : heldFormData.keySet()){
            CompoundTag formTag = new CompoundTag();
            formTag.putString("form",form.toString());
            formTag.put("data",heldFormData.get(form).write());
            formTags.add(formTag);

            HeldSkills skills = heldFormData.get(form).getHeldSkills();
            if(skills != null){
                for(HeldSkill heldSkill : skills.getSkills()){

                    if(heldSkill.getPersistentData() == null) continue;
                    if(visitedSkills.contains(heldSkill.getKey())) continue;

                    visitedSkills.add(heldSkill.getKey());
                    CompoundTag skillTag = new CompoundTag();
                    skillTag.putString("skill",heldSkill.getKey().toString());
                    skillTag.put("data",heldSkill.getPersistentData() == null ? new CompoundTag() :heldSkill.getPersistentData().write());
                    skillTags.add(skillTag);
                }
            }

            for(PathData pathData:heldFormData.get(form).getAllPathData()){
                CompoundTag pathDataTag = new CompoundTag();
                pathDataTag.putString("path",pathData.getPath().toString());
                pathDataTag.put("data",pathData.write());

                pathDataTags.add(pathDataTag);
            }
        }
        tag.put("form_data",formTags);
        tag.put("skill_data",skillTags);
        tag.put("path_progress",pathDataTags);

        ListTag dataSources = new ListTag();
        for(ResourceLocation key : sourceContainers.keySet()){
            CompoundTag dataSource = new CompoundTag();
            dataSource.putString("type",AscensionRegistries.EntityDataSources.ENTITY_DATA_SOURCES_REGISTRY.getKey(sourceContainers.get(key).getDataSource()).toString());
            sourceContainers.get(key).write(dataSource);
            //System.out.println("writing data source:"+sourceContainers.get(key).getInstanceIdentifier());
            dataSources.add(dataSource);
        }
        tag.put("entity_data_sources",dataSources);

        tag.put("skill_cast_handler",getSkillCastHandler().write());
        ListTag suppressorTag = new ListTag();
        for(AttributeValueContainer valueContainer : getAscensionAttributeHolder().getContainers()){
            if(valueContainer.isSuppressed()){
                CompoundTag attributeTag = new CompoundTag();
                attributeTag.putString("attribute",valueContainer.getAttributeHolder().getKey().location().toString());
                attributeTag.putDouble("value",valueContainer.getSuppressedValue());
                suppressorTag.add(attributeTag);
            }
        }
        tag.put("suppressed_values",suppressorTag);
        tag.putDouble("qi",entityQiContainer.getCurrentQi());
        //path data, make sure to also hold the path
    }

    //========================== FORM DATA HANDLING ==========================
    @Override
    public Entity getAttachedEntity() {
        return attachedEntity;
    }


    public void setAttachedEntity(Entity attachedEntity) {
        this.attachedEntity = attachedEntity;
    }
    @Override
    public IEntityFormData getActiveFormData() {
        return heldFormData.get(activeForm);
    }

    /*
        the way it works
        the form data is STILL ON THE ENTITY WHEN EVERYTHING ELSE IS FIRST REMOVED

        then remove the form and re add all the data
        this way when re adding it to a different entity we do not need to worry about double-dipping

     */
    @Override
    public IEntityFormData removeEntityForm(ResourceLocation form) {
        IEntityFormData removedForm = heldFormData.get(form);
        if(removedForm == null) return  null;
        AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.get(form).onRemoved(this,removedForm);
        if(physiqueForm.equals(form)){
            ResourceLocation oldPhysique = removedForm.getPhysiqueKey();
            IPhysiqueData oldPhysiqueData = removedForm.getPhysiqueData();
            //TODO handle physique removed
            if(form.equals(ModForms.MORTAL_VESSEL)) setPhysique(ModPhysiques.MORTAL.getId(),null,ModForms.SOUL_FORM.getId());
            else setPhysique(ModPhysiques.MORTAL.getId());
            removedForm.setPhysique(oldPhysique,oldPhysiqueData);
        }
        if (bloodlineForm.equals(form)) {
            //TODO
        }
        for(PathData pathData : removedForm.getAllPathData()){
            //TODO handle realm change to 0,0 then remove technique
            pathDataLocation.remove(pathData.getPath());
            pathData.remove(this);
        }
        //TODO trigger onFormRemoved of everything else
        for(Map.Entry<ResourceLocation,IEntityFormData> heldForm : heldFormData.entrySet()){
            IEntityForm heldFormFactory = AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.get(heldForm.getKey());
            IEntityFormData heldFormData = heldForm.getValue();
            heldFormFactory.onFormRemoved(this,heldFormData,removedForm);

            if(heldFormData.getPhysiqueKey() != null){
                heldFormData.getPhysique().onFormRemoved(this,form,heldFormData.getPhysiqueData());
            }
            if(heldFormData.getBloodlineKey() != null){
                heldFormData.getBloodline().onFormRemoved(this,form,heldFormData.getBloodlineData());
            }
            for(PathData pathData : heldFormData.getAllPathData()) pathData.onFormRemoved(this,removedForm);
            heldFormData.getHeldSkills().onFormRemoved(this,form);
        }
        heldFormData.remove(form);
        return removedForm;
    }

    @Override
    public IEntityFormData getEntityFormData(ResourceLocation form) {
        return heldFormData.get(form);
    }

    @Override
    public IEntityFormData getEntityFormData(IEntityForm form) {
        return getEntityFormData(AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.getKey(form));
    }

    @Override
    public Collection<IEntityFormData> getFormData() {
        return heldFormData.values();
    }

    @Override
    public boolean hasForm(ResourceLocation form) {
        return heldFormData.containsKey(form);
    }

    @Override
    public void addEntityForm(ResourceLocation form) {
        IEntityForm formFactory = AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.get(form);
        IEntityFormData formData = formFactory.freshEntityFormData(this);
        addEntityForm(form,formData);



    }

    @Override
    public void addEntityForm(ResourceLocation form, IEntityFormData formData) {
        IEntityForm formFactory = AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.get(form);
        Set<Map.Entry<ResourceLocation,IEntityFormData>> forms = heldFormData.entrySet();
        heldFormData.put(form,formData);
        formFactory.onAdded(this);

        //TODO handle addition of existing data on formData
        //TODO make sure to fully run re add stuff, essentially simulate it

        if(pathDataForFormCache.containsKey(form)){
            for(ResourceLocation pathId : pathDataForFormCache.get(form)){
                IPath path = AscensionRegistries.getRegistryObject(pathId,AscensionRegistries.Paths.PATHS_REGISTRY);
                addPathData(pathId,path.freshPathData(this));
                path.fromCompound(cachedPathDataTags.get(pathId),this);
            }
        }

        for(Map.Entry<ResourceLocation,IEntityFormData> heldForm : forms){
            IEntityForm heldFormFactory = AscensionRegistries.EntityForms.ENTITY_FORMS_REGISTRY.get(heldForm.getKey());
            heldFormFactory.onFormAdded(this,heldForm.getValue(),formData);
        }

        //TODO send event here so others can listen to it
    }


    @Override
    public void setActiveForm(ResourceLocation activeForm) {
        this.activeForm = activeForm;
        getAscensionAttributeHolder().updateAttributes(this);
    }

    @Override
    public void setFormData(ResourceLocation form, IEntityFormData formData) {
        heldFormData.put(form,formData);
        if(formData.getPhysiqueKey() != null) physiqueForm = form;
        if(formData.getBloodlineKey() != null) bloodlineForm = form;
        for(ResourceLocation path : formData.getPaths()){
            pathDataLocation.put(path,form);
        }
    }

    //============================ PHYSIQUE HANDLING =======================================
    @Override
    public boolean setPhysique(ResourceLocation physique) {
        return setPhysique(physique,AscensionRegistries.Physiques.PHSIQUES_REGISTRY.get(physique).freshPhysiqueData(this));
    }

    @Override
    public boolean setPhysique(ResourceLocation physique, IPhysiqueData existingData) {
        return setPhysique(physique,existingData,ModForms.MORTAL_VESSEL.getId());
    }

    @Override
    public boolean setPhysique(ResourceLocation physique, IPhysiqueData physiqueData,ResourceLocation form) {
        return setPhysique(physique,physiqueData,form,heldFormData.get(physiqueForm));

    }

    public boolean setPhysique(ResourceLocation physique,IPhysiqueData physiqueData,ResourceLocation form,IEntityFormData oldPhysiqueForm){
        if(!heldFormData.containsKey(form)) return false;


        ResourceLocation oldPhysique = null;
        IPhysiqueData oldPhysiqueData = null;
        if(physiqueForm != null){
            //no old physique just replace directly
            oldPhysique = oldPhysiqueForm.getPhysiqueKey();
            oldPhysiqueData = oldPhysiqueForm.getPhysiqueData();

        }
        //System.out.println("trying to replace :"+(oldPhysique == null ? "none" : oldPhysique.toString()));

        PhysiqueChangeEvent.Pre preEvent = new PhysiqueChangeEvent.Pre(oldPhysique,oldPhysiqueData,physique,this);
        NeoForge.EVENT_BUS.post(preEvent);
        if(preEvent.isCanceled()) return false;
        physique = preEvent.getNewPhysique();
        IPhysique newPhysiqueInstance = AscensionRegistries.getRegistryObject(physique,AscensionRegistries.Physiques.PHSIQUES_REGISTRY);
        if(oldPhysique != null){
            IPhysique physiqueInstance = oldPhysiqueForm.getPhysique();

            physiqueInstance.onPhysiqueRemoved(this,oldPhysiqueData,physique);
            oldPhysiqueForm.setPhysique(null);
            //try removing paths it added
            for(ResourceLocation path : physiqueInstance.paths()){
                TryRemovePathDataEvent event = new TryRemovePathDataEvent(this,path);
                NeoForge.EVENT_BUS.post(event);
                if(!event.isCanceled() && !newPhysiqueInstance.paths().contains(path)){
                    removePath(path);
                }
            }
        }


        physiqueForm = form;

        heldFormData.get(physiqueForm).setPhysique(physique,physiqueData);
        heldFormData.get(physiqueForm).getPhysique().onPhysiqueAdded(this,oldPhysique,oldPhysiqueData);

        for(ResourceLocation path : heldFormData.get(physiqueForm).getPhysique().paths()){

            IPath pathInstance = AscensionRegistries.Paths.PATHS_REGISTRY.get(path);
            PathData pathData = pathInstance.freshPathData(this);
            if(heldFormData.containsKey(pathInstance.defaultForm())){
                addPathData(path,pathData);
            }
        }

        PhysiqueChangeEvent.Post event = new PhysiqueChangeEvent.Post(preEvent,heldFormData.get(physiqueForm).getPhysiqueData());
        //System.out.println("changed physique to : "+heldFormData.get(physiqueForm).getPhysique().getDisplayTitle().getString());
        NeoForge.EVENT_BUS.post(event);
        if(getAttachedEntity() instanceof ServerPlayer serverPlayer  && serverPlayer.connection != null)PacketDistributor.sendToPlayer(serverPlayer,new SyncPhysique(physiqueForm,physique,physiqueData));
        return true;
    }

    @Override
    public IPhysiqueData getPhysiqueData() {

        if (physiqueForm == null) return null;
        if (!heldFormData.containsKey(physiqueForm)) return null;

        return heldFormData.get(physiqueForm).getPhysiqueData();

    }

    @Override
    public ResourceLocation getPhysiqueForm() {
        return physiqueForm;

    }

    @Override
    public IPhysiqueData removePhysique() {
        IPhysiqueData data = getPhysiqueData();
        setPhysique(ModPhysiques.MORTAL.getId());
        return data;
    }

    @Override
    public IPhysique getPhysique() {
        if (physiqueForm == null) return null;
        IEntityFormData formData = heldFormData.get(physiqueForm);
        if (formData == null) return null;
        return formData.getPhysique();
    }

    @Override
    public void movePhysique(ResourceLocation form) {
        if(!heldFormData.containsKey(form))return;
        ResourceLocation physique = heldFormData.get(physiqueForm).getPhysiqueKey();
        IPhysiqueData physiqueData = heldFormData.get(physique).getPhysiqueData();
        heldFormData.get(physiqueForm).setPhysique(null);
        heldFormData.get(form).setPhysique(physique,physiqueData);
        physiqueForm = form;
    }

    //============================ BLOODLINE HANDLING =======================================
    @Override
    public void setBloodline(ResourceLocation bloodline) {
        IBloodline bloodlineInstance = AscensionRegistries.Bloodlines.BLOODLINE_REGISTRY.get(bloodline);
        if (bloodlineInstance == null) return;

        setBloodline(bloodline, bloodlineInstance.freshBloodlineData(this));
    }

    @Override
    public void setBloodline(ResourceLocation bloodline, IBloodlineData existingData) {
        setBloodline(bloodline, existingData, ModForms.MORTAL_VESSEL.getId());
    }

    private void setBloodline(ResourceLocation bloodline, IBloodlineData bloodlineData, ResourceLocation form) {
        if (!heldFormData.containsKey(form)) return;

        ResourceLocation oldBloodline = null;

        if (bloodlineForm != null && heldFormData.containsKey(bloodlineForm)) {
            IEntityFormData oldFormData = heldFormData.get(bloodlineForm);
            oldBloodline = oldFormData.getBloodlineKey();

            oldFormData.setBloodline(null);
        }

        IBloodline bloodlineInstance = AscensionRegistries.Bloodlines.BLOODLINE_REGISTRY.get(bloodline);
        if (bloodlineInstance == null) return;

        if (bloodlineData == null) {
            bloodlineData = bloodlineInstance.freshBloodlineData(this);
        }

        bloodlineForm = form;

        heldFormData.get(bloodlineForm).setBloodline(bloodline, bloodlineData);
        bloodlineInstance.onBloodlineAdded(this, bloodlineData, oldBloodline);

        if (getAttachedEntity() instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
            PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new SyncBloodline(bloodlineForm, bloodline, bloodlineData)
            );
        }

    }

    @Override
    public IBloodlineData getBloodlineData() {
        if (bloodlineForm == null) return null;
        if (!heldFormData.containsKey(bloodlineForm)) return null;

        return heldFormData.get(bloodlineForm).getBloodlineData();
    }

    @Override
    public ResourceLocation getBloodlineForm() {
        return bloodlineForm;
    }

    @Override
    public IBloodlineData removeBloodline() {
        IBloodlineData data = getBloodlineData();
        setBloodline(ModBloodlines.HUMAN_BLOODLINE.getId());
        return data;
    }

    @Override
    public IBloodline getBloodline() {
        if (bloodlineForm == null) return null;

        IEntityFormData formData = heldFormData.get(bloodlineForm);
        if (formData == null) return null;

        return formData.getBloodline();
    }

    @Override
    public void moveBloodline(ResourceLocation form) {
        if (bloodlineForm == null) return;
        if (!heldFormData.containsKey(form)) return;
        if (!heldFormData.containsKey(bloodlineForm)) return;

        ResourceLocation bloodline = heldFormData.get(bloodlineForm).getBloodlineKey();
        IBloodlineData bloodlineData = heldFormData.get(bloodlineForm).getBloodlineData();

        heldFormData.get(bloodlineForm).setBloodline(null);
        heldFormData.get(form).setBloodline(bloodline, bloodlineData);

        bloodlineForm = form;
    }

    //============================ CULTIVATION DATA HANDLING ==================================
    @Override
    public boolean hasPath(ResourceLocation path) {
        return pathDataLocation.containsKey(path);
    }

    @Override
    public boolean isCultivating() {
        for(ResourceLocation path : pathDataLocation.keySet()){
            if(getPathData(path).isCultivating()) return true;
        }
        return false;
    }

    @Override
    public boolean isCultivating(ResourceLocation path) {
        if(!pathDataLocation.containsKey(path)) return false;

        return getPathData(path).isCultivating();
    }

    @Override
    public ResourceLocation getTechnique(ResourceLocation path) {
        if(!pathDataLocation.containsKey(path)) return null;
        return getPathData(path).getLastUsedTechnique();
    }

    @Override
    public ITechniqueData getTechniqueData(ResourceLocation path) {
        if(!pathDataLocation.containsKey(path)) return null;
        if(getPathData(path).getLastUsedTechnique() == null) return null;
        return getPathData(path).getTechniqueData(getPathData(path).getLastUsedTechnique());
    }

    @Override
    public PathData getPathData(ResourceLocation path) {
        if(!pathDataLocation.containsKey(path)) return null;
        return heldFormData.get(pathDataLocation.get(path)).getPathData(path);//TODO
    }

    @Override
    public Collection<ResourceLocation> getPathDataForms(ResourceLocation path) {
        ArrayList<ResourceLocation> forms = new ArrayList<>();
        for(ResourceLocation form : heldFormData.keySet()){
            if(heldFormData.get(form).hasPathData(path)) forms.add(form);
        }
        return forms;
    }

    @Override
    public Collection<PathData> getAllPathData() {
        HashSet<PathData> data = new HashSet<>();
        for(IEntityFormData formData : heldFormData.values()){
            data.addAll(formData.getAllPathData());
        }
        return data;
    }

    @Override
    public ITechniqueData removeTechnique(ResourceLocation path) {
        if(!pathDataLocation.containsKey(path)) return null;
        if(getPathData(path).getLastUsedTechnique() == null) return null;

        ITechniqueData techniqueData = getPathData(path).getTechniqueData(getPathData(path).getLastUsedTechnique());

        PathData pathData = getPathData(path);
        pathData.handleRealmChange(pathData.getMajorRealm(),0,this);
        ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(pathData.getLastUsedTechnique());
        technique.onTechniqueRemoved(this,techniqueData);
        pathData.removeLastUsedTechnique();

        if(getAttachedEntity() instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncPathData(pathDataLocation.get(path), pathData));
        }
        return techniqueData;

    }

    @Override
    public void setPathForm(ResourceLocation path, ResourceLocation form) {
        if(pathDataLocation.containsKey(path) || !heldFormData.containsKey(form)) return;

        pathDataLocation.put(path,form);
    }

    @Override
    public boolean setTechnique(ResourceLocation technique) {
        return setTechnique(technique,AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(technique).freshTechniqueData(this));
    }

    @Override
    public boolean setTechnique(ResourceLocation technique, ITechniqueData techniqueData) {
        //System.out.println("trying to set technique");
        ITechnique techniqueInstance = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(technique);
        ResourceLocation path = techniqueInstance.getPath();
        if(!pathDataLocation.containsKey(path)) return false;
        PathData pathData = heldFormData.get(pathDataLocation.get(path)).getPathData(path);
        if(pathData == null) return false;
        ITechnique oldTechnique = null;
        if(pathData.getLastUsedTechnique() != null){

            if(technique.equals(pathData.getLastUsedTechnique())){
                return false; //we have already learned this technique
            }
            oldTechnique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(pathData.getLastUsedTechnique());
            oldTechnique.onTechniqueRemoved(this,pathData.getTechniqueData(pathData.getLastUsedTechnique()));
        }

        //TODO then check compatibility with technique history, if even 1 is not compatible we reset cultivation data
        if(oldTechnique != null){
            //there was a previous technique so check for compatibility
            pathData.removeLastUsedTechnique();
            for(ResourceLocation usedTechnique : pathData.getTechniqueHistory()){
                if(techniqueInstance.isCompatibleWith(usedTechnique)) continue;

                pathData.resetCultivation();

                break;
            }

        }
        pathData.setLastUsedTechnique(technique);
        pathData.addTechniqueData(technique,techniqueData);

        techniqueInstance.onTechniqueAdded(this);
        //System.out.println("technique changed to: "+technique.toString());
        if(getAttachedEntity() instanceof ServerPlayer serverPlayer  && serverPlayer.connection != null){
            PacketDistributor.sendToPlayer(serverPlayer,new SyncPathData(pathDataLocation.get(path),pathData));
        }
        return true;
    }

    @Override
    public void addPathData(ResourceLocation path, PathData pathData, ResourceLocation form) {
        if(pathDataLocation.containsKey(path)) return;


        IPath pathInstance = AscensionRegistries.Paths.PATHS_REGISTRY.get(path);
        if(!heldFormData.containsKey(pathInstance.defaultForm())) return;

        pathDataLocation.put(path,pathInstance.defaultForm());
        heldFormData.get(pathInstance.defaultForm()).addPathData(path,pathData);
        if(pathData.getLastUsedTechnique() != null && !pathData.getLastUsedTechnique().toString().equals("ascension:none")){
            AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(pathData.getLastUsedTechnique()).onTechniqueAdded(this);
        }
        if(getAttachedEntity() instanceof ServerPlayer serverPlayer  && serverPlayer.connection != null){
            //System.out.println("sending sync for path: "+path);
            PacketDistributor.sendToPlayer(serverPlayer,new SyncPathData(pathDataLocation.get(path),pathData));
        }
    }

    /*TODO need to consider a circumstance where the player said merged essence onto spirit then lost mortal vessel
        in this situation when reloading they would lose said cultivation, so i should create a "cached" version of the form data?
        and then when the player tries to move it later on we check if the form is in the cached form data?
        should work.
        basically when player calls getPathData(). check if we have any cached path data
        and when we do move pathData to form we check if that path is cached, if so we use that

     */


    @Override
    public void addPathData(ResourceLocation path, PathData pathData) {
        addPathData(path,pathData,AscensionRegistries.getRegistryObject(path,AscensionRegistries.Paths.PATHS_REGISTRY).defaultForm());
    }

    //TODO no scenario where this would happen yet so pretend it does not exist
    @Override
    public void removePath(ResourceLocation path) {
        if(!pathDataLocation.containsKey(path)) return;

        ResourceLocation form = pathDataLocation.get(path);

        if(!heldFormData.containsKey(form)) {
            pathDataLocation.remove(path);
            return;
        }

        IEntityFormData formData = heldFormData.get(form);
        PathData pathData = formData.getPathData(path);

        if(pathData == null) {
            pathDataLocation.remove(path);
            return;
        }

        pathData.handleRealmChange(0,0,this);
        ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(pathData.getLastUsedTechnique());
        if(technique != null) {
            ITechniqueData techniqueData = pathData.getTechniqueData(pathData.getLastUsedTechnique());
            pathData.removeLastUsedTechnique();
            technique.onTechniqueRemoved(this,techniqueData);

        }
        pathData.remove(this);
        formData.removePathData(path);
        pathDataLocation.remove(path);

        if(getAttachedEntity() instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncEntityForm(formData));
        }
    }


    @Override
    public PathBonusHandler getPathBonusHandler() {
        return pathBonusHandler;
    }
    //============================ BREAKTHROUGH HANDLING ==================================


    @Override
    public boolean isBreakingThrough(ResourceLocation path) {
        if(!pathDataLocation.containsKey(path)) return false;
        if(!heldFormData.containsKey(pathDataLocation.get(path))) return false;

        PathData data = heldFormData.get(pathDataLocation.get(path)).getPathData(path);
        if(data == null) return false;
        return data.isBreakingThrough();
    }

    //============================ SKILL DATA HANDLING ==================================
    @Override
    public void giveSkill(ResourceLocation skill, ResourceLocation form) {
        IPersistentSkillData skillData = AscensionRegistries.Skills.SKILL_REGISTRY.get(skill).freshPersistentData(this);
        giveSkill(skill,skillData,form);
    }

    @Override
    public void giveSkill(ResourceLocation skill, IPersistentSkillData skillData, ResourceLocation form) {

        IPersistentSkillData existingSkillData = getSkillData(skill);
        if(existingSkillData != null) skillData =existingSkillData;

        if(!heldFormData.containsKey(form)) return;

        HeldSkills skills = heldFormData.get(form).getHeldSkills();
        if(skills.hasSkill(skill)) return;


        boolean hasSkill = hasSkill(skill);
        skills.addSkill(skill,skillData);
        if(!hasSkill){
            ISkill skillInstance = AscensionRegistries.Skills.SKILL_REGISTRY.get(skill);
            skillInstance.onAdded(this);
        }
        //TODO update to sync only changes
        if(getAttachedEntity() instanceof ServerPlayer serverPlayer && serverPlayer.connection != null){
            PacketDistributor.sendToPlayer(serverPlayer,new SyncHeldSkills(form.toString(),heldFormData.get(form).getHeldSkills()));
        }
    }

    @Override
    public void removeSkill(ResourceLocation skill, ResourceLocation form) {
        if(!hasSkill(skill)) return;
        if(!heldFormData.containsKey(form)) return;
        if(!heldFormData.get(form).getHeldSkills().hasSkill(skill)) return;

        IPersistentSkillData skillData = heldFormData.get(form).getHeldSkills().removeSkill(skill);
        if(hasSkill(skill)) return;

        ISkill skillInstance = AscensionRegistries.Skills.SKILL_REGISTRY.get(skill);
        skillInstance.onRemoved(this,skillData);
        //TODO update to sync only changes
        if(getAttachedEntity() instanceof ServerPlayer serverPlayer && serverPlayer.connection != null){
            PacketDistributor.sendToPlayer(serverPlayer,new SyncHeldSkills(form.toString(),heldFormData.get(form).getHeldSkills()));
        }
    }

    @Override
    public void removeSkill(ResourceLocation skill) {
        if(!hasSkill(skill)) return;
        IPersistentSkillData skillData = null;
        HashSet<ResourceLocation> modifiedForms = new HashSet<>();
        for(ResourceLocation formKey : heldFormData.keySet()){
            IEntityFormData formData = heldFormData.get(formKey);
            if(!formData.getHeldSkills().hasSkill(skill)) continue;
            skillData = formData.getHeldSkills().removeSkill(skill);
            modifiedForms.add(formKey);
        }
        ISkill skillInstance = AscensionRegistries.Skills.SKILL_REGISTRY.get(skill);
        skillInstance.onRemoved(this,skillData);
        if(getAttachedEntity() instanceof ServerPlayer serverPlayer && serverPlayer.connection != null){
            for(ResourceLocation modifiedForm : modifiedForms){
                PacketDistributor.sendToPlayer(serverPlayer,new SyncHeldSkills(modifiedForm.toString(),heldFormData.get(modifiedForm).getHeldSkills()));
            }

        }
    }

    @Override
    public boolean hasSkill(ResourceLocation skill) {
        for(IEntityFormData formData : heldFormData.values()){
            if(formData.getHeldSkills() != null && formData.getHeldSkills().hasSkill(skill)) return true;
        }
        return false;
    }

    @Override
    public IPersistentSkillData getSkillData(ResourceLocation skill) {
        if(cachedSkillData.containsKey(skill)) return cachedSkillData.remove(skill);
        for(IEntityFormData formData : heldFormData.values()){
            if(formData.getHeldSkills().getHeldSkill(skill) != null){
                return formData.getHeldSkills().getHeldSkill(skill).getPersistentData();
            }
        }
        return null;
    }

    @Override
    public Set<ResourceLocation> getAllSkills() {
        HashSet<ResourceLocation> skills = new HashSet<>();

        for(IEntityFormData formData : heldFormData.values()){
            for(HeldSkill heldSkill : formData.getHeldSkills().getSkills()){
                skills.add(heldSkill.getKey());
            }
        }
        return skills;
    }


    //============================= SKILL CASTING ====================================
    @Override
    public SkillCastHandler getSkillCastHandler() {
        return skillCastHandler;
    }

    @Override
    public EntityQiContainer getQiContainer() {
        return entityQiContainer;
    }

    @Override
    public void tick() {
        if (((LivingEntity) attachedEntity).tickCount % 20 == 0) {
            entityQiContainer.tryRegenQi();
        }

        Collection<IEntityDataSourceContainer> containers = sourceContainers.values();
        for(IEntityDataSourceContainer container : containers){
            container.getDataSource().tick(this,container);
        }

    }
    //============================= ENTITY DATA SOURCES ===============================
    @Override
    public void addEntityDataSource(IEntityDataSourceContainer container) {
        if(sourceContainers.containsKey(container.getInstanceIdentifier())){
            IEntityDataSourceContainer other = sourceContainers.remove(container.getInstanceIdentifier());
            other.getDataSource().onRemoved(this,other);
        }
        sourceContainers.put(container.getInstanceIdentifier(),container);
        container.getDataSource().onAdded(this,container);
    }

    @Override
    public IEntityDataSourceContainer getSourceContainer(ResourceLocation identifier) {
        return sourceContainers.get(identifier);
    }

    @Override
    public IEntityDataSourceContainer removeEntitySource(ResourceLocation identifier) {
        if(sourceContainers.containsKey(identifier)){
            IEntityDataSourceContainer container = sourceContainers.remove(identifier);
            container.getDataSource().onRemoved(this,container);
            return container;
        }
        return null;
    }

    @Override
    public Collection<IEntityDataSourceContainer> getContainersOfType(IEntityDataSource source) {
        ArrayList<IEntityDataSourceContainer> containers = new ArrayList<>();
        for(ResourceLocation key : sourceContainers.keySet()){
            if(sourceContainers.get(key).getDataSource() == source) containers.add(sourceContainers.get(key));
        }
        return containers;
    }

    //============================= ATTRIBUTES =======================================
    @Override
    public AscensionAttributeHolder getAscensionAttributeHolder() {
        return ascensionAttributeHolder;
    }

    @Override
    public void setAscensionAttributeHolder(LivingEntity entity,AscensionAttributeHolder holder) {
        this.ascensionAttributeHolder = holder;
        if(entity != null && holder != null) holder.setAttachedEntity(entity);
    }

    /*
        should mainly be used for healing, instances of damage should include a source
     */
    @Override
    public void setHealth(double val) {
        val = Math.min(val,getAscensionAttributeHolder().getAttribute(Attributes.MAX_HEALTH).getValue());
        this.currentHealth = val;
        if(currentHealth <= 0 && getAttachedEntity() != null && getAttachedEntity() instanceof LivingEntity entity) {

            if(val != 0) entity.setHealth(0);
            currentHealth = 0;
        }
        if(getAttachedEntity() instanceof  ServerPlayer serverPlayer && serverPlayer.connection != null){
            PacketDistributor.sendToPlayer(serverPlayer,new SyncCurrentHealth(currentHealth));

        }
    }

    @Override
    public void setHealth(double val, DamageSource source) {
        this.currentHealth = val;
        if(currentHealth <= 0 && getAttachedEntity() != null && getAttachedEntity() instanceof  LivingEntity entity) {
            if(val != 0) entity.setHealth(0);
            currentHealth = 0;
            //entity.die(source);


        }
        if(getAttachedEntity() instanceof  ServerPlayer serverPlayer && serverPlayer.connection != null){
            PacketDistributor.sendToPlayer(serverPlayer,new SyncCurrentHealth(currentHealth));

        }
    }


    @Override
    public double getHealth() {
        return this.currentHealth;
    }

}