package net.zic.ascension.impl.core.path.simple;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.path.realm.TribulationMode;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.tribulation.Tribulation;
import net.zic.ascension.api.ascension.core.tribulation.TribulationInstance;
import net.zic.ascension.api.ascension.core.tribulation.TribulationManager;
import net.zic.ascension.api.ascension.datapack.path.PathType;
import net.zic.ascension.api.ascension.datapack.path.realm.PathRealmChangeEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.impl.core.path.CompletedTribulation;
import net.zic.ascension.impl.core.path.realms.BreakthroughBehaviour;
import net.zic.ascension.impl.core.path.realms.MajorRealm;
import net.zic.ascension.impl.core.path.realms.MajorRealmDefinition;
import net.zic.ascension.impl.core.tribulation.action.EndBreakthroughResolutionAction;
import net.zic.ascension.impl.core.tribulation.action.RealmChangeResolutionAction;
import net.zic.ascension.impl.datapack.path.AscensionPathTypes;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class SimplePath implements Path<SimplePathData> {
    //the handlers read the realm definitions of this path, so every path has its own
    private final SerializerHandler<SimplePathData> serializerHandler = new DataSerializerHandler();
    private final SyncHandler<SimplePathData> syncHandler = new DataSyncHandler();

    private final Component name;
    private final Component description;
    private final List<MajorRealmDefinition> realmDefinitions;
    private final ProgressActionHolder actions;

    public SimplePath(Component name, Component description, List<MajorRealmDefinition> realmDefinitions, ProgressActionHolder actions) {
        this.name = name;
        this.description = description;
        this.realmDefinitions = realmDefinitions;
        this.actions = actions;
    }
    public List<MajorRealmDefinition> getMajorRealmDefinitions(){
        return realmDefinitions;
    }
    public ProgressActionHolder getProgressActionHolder(){
        return actions;
    }
    @Override
    public PathType getType() {
        return AscensionPathTypes.SIMPLE_PATH_TYPE.get();
    }

    @Override
    public Component getName(@Nullable SimplePathData data) {
        return name;
    }

    @Override
    public Component getDescription(@Nullable SimplePathData data) {
        return description;
    }

    //──Realms────────────────────────────────────────────────────────

    @Override
    public int getMaxMajorRealm() {
        return realmDefinitions.size();
    }

    @Override
    public int getMaxMinorRealm(int majorRealm) {
        return getRealmDefinition(majorRealm).getMaxRealm();
    }

    @Override
    public Component getMajorRealmName(int majorRealm) {
        return getRealmDefinition(majorRealm).getName();
    }

    @Override
    public Component getMinorRealmName(int majorRealm, int minorRealm) {
        return getRealmDefinition(majorRealm).getMinorRealmName(minorRealm);
    }

    @Override
    public Component getRealmName(int majorRealm, int minorRealm) {
        return majorRealm < 0 ? Component.literal("Mortal") :getRealmDefinition(majorRealm).getCompositeRealmName(minorRealm);
    }

    @Override
    public MajorRealmDefinition getRealmDefinition(int majorRealm) {

        return realmDefinitions.isEmpty() ? null : realmDefinitions.get(majorRealm);
    }

    @Override
    public double getMaxProgress(int majorRealm, int minorRealm) {
        return getRealmDefinition(majorRealm).getMaxProgress(minorRealm);
    }
    //──Tribulation────────────────────────────────────────────────────────
    @Override
    public Identifier getTribulationId(int majorRealm, int minorRealm) {
        return getRealmDefinition(majorRealm).getRealmTribulationId(minorRealm);
    }

    @Override
    public Tribulation<?> getTribulation(int majorRealm, int minorRealm, RegistryAccess access) {
        return CoreRegistries.safeAccess(CoreRegistries.TRIBULATION_REGISTRY, getTribulationId(majorRealm, minorRealm), access);
    }

    @Override
    public boolean hasTribulation(int majorRealm, int minorRealm) {
        return getTribulationId(majorRealm, minorRealm) != null;
    }

    @Override
    public TribulationMode getTribulationMode(int majorRealm, int minorRealm) {
        return getRealmDefinition(majorRealm).getRealmTribulationMode(minorRealm);
    }

    //──Progression────────────────────────────────────────────────────────
    @Override
    public void progress(OriginSource source, PathInstance<SimplePathData> instance, double amount, LivingEntity entitySource) {
        SimplePathData data = instance.getData();
        data.progress = Math.min(data.progress + amount, getMaxProgress(data));

        if (!canBreakthrough(data)) return;

        BreakthroughBehaviour behaviour = currentRealm(data).definition().getBreakthroughBehaviour(currentMinorRealm(data));

        if (behaviour == BreakthroughBehaviour.INSTANT) {
            tryBreakthrough(source,instance,entitySource);
        } else if (behaviour != BreakthroughBehaviour.NONE) {
            //TODO add timer
        }

    }

    @Override
    public double getMaxProgress(SimplePathData data) {
        return currentRealm(data).definition().getMaxProgress(currentMinorRealm(data));
    }

    @Override
    public boolean canProgress(SimplePathData data) {
        MajorRealm currentRealm = currentRealm(data);
        int minorRealm = currentRealm.getCurrentRealm();

        boolean isProgressFull = currentRealm.definition().getMaxProgress(minorRealm) <= data.progress;
        boolean maxMajorRealm = getMaxMajorRealm() - 1 == data.getCurrentMajorRealm();
        boolean maxMinorRealm = getMaxMinorRealm(data,data.getCurrentMajorRealm()) == currentMinorRealm(data);

        return !isProgressFull && !isBreakingThrough(data)  && !(maxMinorRealm && maxMajorRealm);
    }

    @Override
    public boolean canBreakthrough(SimplePathData data) {
        MajorRealm currentRealm = currentRealm(data);
        int minorRealm = currentRealm.getCurrentRealm();

        boolean isProgressFull = currentRealm.definition().getMaxProgress(minorRealm) <= data.progress;
        boolean maxMajorRealm = getMaxMajorRealm() - 1 == data.getCurrentMajorRealm();
        boolean maxMinorRealm = getMaxMinorRealm(data,data.getCurrentMajorRealm()) == currentMinorRealm(data);


        return isProgressFull && !isBreakingThrough(data) && !(maxMinorRealm && maxMajorRealm);
    }

    @Override
    public int getMaxMinorRealm(SimplePathData data, int majorRealm) {
        return data.getCurrentMajorRealm() < 0 ? 0 : getMaxMinorRealm(majorRealm);
    }

    @Override
    public Realm getPreviousRealm(SimplePathData data) {
        int majorRealm = data.getCurrentMajorRealm();
        int minorRealm = currentMinorRealm(data);
        if(minorRealm == 0 && majorRealm == 0) return data.getCurrentRealm();
        return minorRealm == 0 ?
                Realm.of(majorRealm-1,getMaxMinorRealm(data,majorRealm-1)) : Realm.of(majorRealm,minorRealm-1);
    }

    @Override
    public Realm getNextRealm(SimplePathData data) {
        int majorRealm = data.getCurrentMajorRealm();
        int minorRealm = currentMinorRealm(data);
        return minorRealm == getMaxMinorRealm(data,majorRealm) && !data.isLimitBroken() ?
                Realm.of(majorRealm+1,0) : Realm.of(majorRealm,minorRealm+1);
    }

    //the current major realm, creates the first major realm if none is held
    private MajorRealm currentRealm(SimplePathData data){
        if(data.realms.isEmpty()){
            data.realms.add(MajorRealm.of(getRealmDefinition(0)));
        }
        return data.realms.getLast();
    }
    private int currentMinorRealm(SimplePathData data){
        return currentRealm(data).getCurrentRealm();
    }

    //──Tribulations────────────────────────────────────────────────────────

    //the active breakthrough tribulation, cleared if the manager no longer has it (finished, discarded or failed to load)
    private @Nullable UUID getActiveTribulationId(SimplePathData data){
        if(data.activeTribulationId == null) return null;
        TribulationManager manager = TribulationManager.getInstance();
        if(manager != null && !manager.has(data.activeTribulationId)) data.activeTribulationId = null;
        return data.activeTribulationId;
    }

    @Override
    public @Nullable TribulationInstance<?> getActiveTribulation(SimplePathData data) {
        UUID id = getActiveTribulationId(data);
        TribulationManager manager = TribulationManager.getInstance();
        return id == null || manager == null ? null : manager.get(id);
    }

    public boolean isBreakingThrough(SimplePathData data){
        return getActiveTribulationId(data) != null;
    }

    @Override
    public boolean canTriggerTribulation(SimplePathData data, Identifier tribulation) {
        if(data.getCurrentMajorRealm() < 0 || tribulation == null) return false;
        int majorRealm = data.getCurrentMajorRealm();
        int minorRealm = currentMinorRealm(data);
        return tribulation.equals(getTribulationId(majorRealm, minorRealm))
                && getTribulationMode(majorRealm, minorRealm) == TribulationMode.MANUAL
                && canBreakthrough(data);
    }

    @Override
    public @Nullable TribulationInstance<?> triggerTribulation(OriginSource source, PathInstance<SimplePathData> instance, Identifier tribulation,
                                                               LivingEntity target, @Nullable Consumer<TribulationInstance<?>> beforeStart) {
        if(!canTriggerTribulation(instance.getData(), tribulation)) return null;
        return startTribulation(source, instance, tribulation, target, beforeStart);
    }

    private @Nullable TribulationInstance<?> startTribulation(OriginSource source, PathInstance<SimplePathData> instance, Identifier tribulation,
                                                             LivingEntity target, @Nullable Consumer<TribulationInstance<?>> beforeStart){
        SimplePathData data = instance.getData();
        if(getActiveTribulationId(data) != null) return null;
        TribulationManager manager = TribulationManager.getInstance();
        if(manager == null) return null;
        Identifier pathId = instance.getPathId();
        TribulationInstance<?> started = manager.create(tribulation)
                .targeting(target)
                .onSuccess(new RealmChangeResolutionAction(pathId, data.getCurrentRealm(), 1))
                .onFailure(new EndBreakthroughResolutionAction(pathId))
                .beforeStart(beforeStart)
                .startInstance();
        data.activeTribulationId = started == null ? null : started.getId();
        return started;
    }

    /**
     * called when a breakthrough tribulation succeeds, verifies we are still at the realm the tribulation was started at
     * and that the realm still expects this tribulation
     */
    public void completeBreakthrough(OriginSource source, PathInstance<SimplePathData> instance, Realm realm, TribulationInstance<?> tribulation, int realmChange){
        SimplePathData data = instance.getData();
        endBreakthrough(instance,tribulation.getId());
        if(!data.getCurrentRealm().equals(realm)) return;
        if(!tribulation.getTribulationId().equals(getTribulationId(realm.majorRealm(), realm.minorRealm()))) return;

        data.completedTribulations.put(realm, CompletedTribulation.of(tribulation));

        Realm target = realm;
        for(int i = 0; i < realmChange; i++){
            target = target.majorRealm() == data.getCurrentMajorRealm() && currentRealm(data).isLimitBroken()
                    ? Realm.of(target.majorRealm(), target.minorRealm()+1)
                    : Realm.getNext(target, this);
        }
        changeRealm(source,instance,target);
        data.progress = 0;
    }
    public void endBreakthrough(PathInstance<SimplePathData> instance, UUID tribulation){
        SimplePathData data = instance.getData();
        if(tribulation.equals(data.activeTribulationId)) data.activeTribulationId = null;
    }

    private void completeTribulationForRealm(SimplePathData data, Realm realm, OriginSource source){
        if(realm.majorRealm() < 0) return;
        CompletedTribulation<?> completedTribulation = data.completedTribulations.get(realm);
        Identifier tribulationId = getTribulationId(realm.majorRealm(),realm.minorRealm());
        Tribulation<?> tribulation = CoreRegistries.safeAccess(CoreRegistries.TRIBULATION_REGISTRY, tribulationId, source.getRegistryAccess());
        if(tribulation == null){
            data.completedTribulations.remove(realm);
            return;
        }
        if(completedTribulation == null || !completedTribulation.id().equals(tribulationId)){
            //no data present for this tribulation, create new data
            completedTribulation = CompletedTribulation.fresh(tribulationId, tribulation);
        }else{
            completedTribulation = completedTribulation.revalidate();
        }
        data.completedTribulations.put(realm, completedTribulation);

        completedTribulation.onAdded(source);
    }
    private void removeTribulationForRealm(SimplePathData data, Realm realm, OriginSource source){
        CompletedTribulation<?> completedTribulation = data.completedTribulations.remove(realm);
        if(completedTribulation == null) return;
        completedTribulation.onRemoved(source);
    }


    //──Realm Change Logic────────────────────────────────────────────────────────

    public void limitBreakRealm(OriginSource source, PathInstance<SimplePathData> instance, int realm, Identifier limitBreakSource){
        SimplePathData data = instance.getData();
        if(realm >= data.realms.size()) return;
        //TODO if realm != current realm handle realm change
        data.realms.get(realm).setLimitBroken(true,limitBreakSource);
    }
    public void removeRealmLimitBreak(OriginSource source, PathInstance<SimplePathData> instance, int realm, Identifier limitBreakSource){
        SimplePathData data = instance.getData();
        if(realm >= data.realms.size()) return;
        if(data.realms.get(realm).setLimitBroken(false,limitBreakSource)) return;

        changeRealm(source,instance,Realm.of(data.getCurrentMajorRealm(),0));
    }

    private void tryBreakthrough(OriginSource source, PathInstance<SimplePathData> instance, LivingEntity entitySource){
        SimplePathData data = instance.getData();
        int majorRealm = data.getCurrentMajorRealm();
        int minorRealm = currentMinorRealm(data);
        Identifier tribulation = getTribulationId(majorRealm,minorRealm);
        if(tribulation == null){
            if(currentRealm(data).isLimitBroken()){
                changeRealm(source,instance,Realm.of(majorRealm,minorRealm+1));
            }else  changeRealm(source,instance,Realm.getNext(Realm.of(majorRealm,minorRealm),this));
        }else if(getTribulationMode(majorRealm,minorRealm) == TribulationMode.AUTO){
            startTribulation(source,instance,tribulation,entitySource,null);
        }
        //MANUAL tribulations wait at full progress until triggered through triggerTribulation
    }

    private void onRealmUp(OriginSource source, PathInstance<SimplePathData> instance) {
        actions.run(source,instance.getPathId(),instance,ProgressDirection.UP,instance.getData().getCurrentRealm().asSalt());
    }
    private void broadcastRealmUp(OriginSource source, PathInstance<SimplePathData> instance, Realm oldRealm){
        SimplePathData data = instance.getData();
        PathRealmChangeEvent.PathRealmUpEvent event = new PathRealmChangeEvent.PathRealmUpEvent(source,Realm.of(data.getCurrentMajorRealm(),currentMinorRealm(data)),oldRealm,instance.getPathId(),instance);
        NeoForge.EVENT_BUS.post(event);
    }

    private void onRealmDown(OriginSource source, PathInstance<SimplePathData> instance) {
        actions.run(source,instance.getPathId(),instance,ProgressDirection.DOWN,getNextRealm(instance.getData()).asSalt());
    }
    private void broadcastRealmDown(OriginSource source, PathInstance<SimplePathData> instance, Realm oldRealm){
        SimplePathData data = instance.getData();
        PathRealmChangeEvent.PathRealmDownEvent event = new PathRealmChangeEvent.PathRealmDownEvent(source,Realm.of(data.getCurrentMajorRealm(),currentMinorRealm(data)),oldRealm,instance.getPathId(),instance);
        NeoForge.EVENT_BUS.post(event);
    }
    private void addNewMajorRealm(SimplePathData data){
        if(data.cachedRealms.isEmpty()){
            data.realms.add(MajorRealm.of(getRealmDefinition(data.getCurrentMajorRealm()+1)));
        }else{
            data.realms.add(data.cachedRealms.removeFirst());
        }
    }

    @Override
    public void changeRealm(OriginSource source, PathInstance<SimplePathData> instance, Realm newRealm) {
        SimplePathData data = instance.getData();

        boolean fromStart = data.getCurrentMajorRealm() < 0;
        Realm oldRealm = fromStart ? Realm.of(0,0) :  Realm.of(data.getCurrentMajorRealm(),currentMinorRealm(data));
        List<Realm> traversedRealms = Realm.getRange(oldRealm,newRealm,this);


        ProgressDirection direction= ProgressDirection.UP;
        if(oldRealm.compareTo(newRealm) >0){
            direction = ProgressDirection.DOWN;
            traversedRealms = traversedRealms.reversed();
        }
        //we do not traverse the start realm
        if(!fromStart) traversedRealms.removeFirst();
        while(!traversedRealms.isEmpty()){
            Realm realm = traversedRealms.removeFirst();
            Realm previousRealm = data.getCurrentMajorRealm() < 0?Realm.of(-1,0) : Realm.of(data.getCurrentMajorRealm(),currentMinorRealm(data));
            if(direction == ProgressDirection.UP){
                if(realm.majorRealm() == data.getCurrentMajorRealm()){
                    completeTribulationForRealm(data,previousRealm,source);
                    currentRealm(data).setCurrentRealm(realm.minorRealm());
                    onRealmUp(source,instance);
                    broadcastRealmUp(source,instance,previousRealm);
                }else{
                    completeTribulationForRealm(data,previousRealm,source);
                    addNewMajorRealm(data);

                    if(currentRealm(data).isLimitBroken()){

                        //this only happens when simulating from a cached majorRealm
                        //we are basically telling it to simulate extra realms

                        int index = 0;
                        for(int i = 0;i<=traversedRealms.size();i++){
                            index = i;
                            if(i >= traversedRealms.size()) break;
                            if(traversedRealms.get(i).majorRealm() != data.getCurrentMajorRealm())break;
                        }

                        for(int minorRealm = currentRealm(data).definition().getMaxRealm()+1;
                            minorRealm <= currentRealm(data).getCurrentRealm();
                            minorRealm ++){

                            traversedRealms.add(index,Realm.of(data.getCurrentMajorRealm(),minorRealm));
                            index++;
                        }


                    }
                    if(currentRealm(data).getCurrentRealm() != 0){
                        currentRealm(data).setCurrentRealm(0);
                    }
                    onRealmUp(source,instance);
                    broadcastRealmUp(source,instance,previousRealm);
                }
            }else{
                if(realm.majorRealm() == data.getCurrentMajorRealm()) {
                    removeTribulationForRealm(data,previousRealm,source);
                    currentRealm(data).setCurrentRealm(realm.minorRealm());
                    onRealmDown(source,instance);
                    broadcastRealmDown(source,instance,previousRealm);
                }else{
                    removeTribulationForRealm(data,previousRealm,source);
                    data.realms.removeLast();
                    if(currentRealm(data).isLimitBroken() && currentRealm(data).getCurrentRealm() > currentRealm(data).definition().getMaxRealm()){
                        //the realm we have loaded has extra realms we need to include
                        for(int minorRealm = currentRealm(data).definition().getMaxRealm()+1;
                            minorRealm <= currentRealm(data).getCurrentRealm();
                            minorRealm ++){
                            traversedRealms.addFirst(Realm.of(data.getCurrentMajorRealm(),minorRealm));
                        }
                    }
                    onRealmDown(source,instance);
                    broadcastRealmDown(source,instance,previousRealm);
                }
            }
            data.progress = 0;
        }

    }

    //──Data Simulation────────────────────────────────────────────────────────

    @Override
    public void simulateProgression(OriginSource source, PathInstance<SimplePathData> instance) {
        SimplePathData data = instance.getData();
        boolean realmsDiscarded = false;
        while(!data.realms.isEmpty()){
            MajorRealm realm = data.realms.removeFirst();
            data.cachedRealms.add(realm);
            if(realm.definition().getMaxRealm() < realm.getCurrentRealm() && !realm.isLimitBroken()) {
                //limit broken realm is no longer limit broken so return cultivation to this realm
                data.realms.clear();
                realm.setCurrentRealm(0);
                data.progress = 0;
                realmsDiscarded = true;
            }
        }


        double cachedProgress = data.progress;
        if(data.cachedRealms.isEmpty()) return;

        MajorRealm finalRealm = data.cachedRealms.getLast();
        //logic is already in place to handle limit broken from cached inside handleRealm change
        //so we just need to tell it to go to the max of the realm and ensure it is limit broken
        int minorRealm = finalRealm.definition().getMaxRealm() < finalRealm.getCurrentRealm() ? finalRealm.definition().getMaxRealm() : finalRealm.getCurrentRealm();
        changeRealm(source,instance,Realm.of(data.cachedRealms.size()-1,minorRealm));

        data.cachedRealms.clear();
        data.progress = cachedProgress;

        UUID activeTribulation = getActiveTribulationId(data);
        if(realmsDiscarded && activeTribulation != null){
            //the realm the tribulation was for no longer exists
            TribulationManager manager = TribulationManager.getInstance();
            if(manager != null) manager.cancel(activeTribulation);
            data.activeTribulationId = null;
        }
    }

    @Override
    public void removeFromSource(OriginSource source, PathInstance<SimplePathData> instance) {
        SimplePathData data = instance.getData();
        if(data.getCurrentMajorRealm() < 0) return;
        //create a deep cached copy, this way we can properly maintain limit broken info
        for(MajorRealm realm : data.realms){
            data.cachedRealms.add(MajorRealm.of(realm));
        }
        Map<Realm, CompletedTribulation<?>> cachedTribulations = new HashMap<>(data.completedTribulations);
        double cachedProgress = data.progress;

        changeRealm(source,instance,Realm.of(0,0));
        data.realms.removeFirst();
        actions.run(source,instance.getPathId(),instance,ProgressDirection.DOWN, "0_0");

        data.realms.clear();
        data.realms.addAll(data.cachedRealms);
        data.cachedRealms.clear();

        data.completedTribulations.clear(); //just in case there are any stragglers
        data.completedTribulations.putAll(cachedTribulations);

        data.progress = cachedProgress;

    }

    //──Data────────────────────────────────────────────────────────

    @Override
    public SimplePathData newData(RegistryAccess access) {
        SimplePathData data = new SimplePathData();
        //ensure we have realms
        currentRealm(data);
        return data;
    }

    @Override
    public SerializerHandler<SimplePathData> serializerHandler() {
        return serializerHandler;
    }

    @Override
    public SyncHandler<SimplePathData> syncHandler() {
        return syncHandler;
    }

    private final class DataSerializerHandler implements SerializerHandler<SimplePathData> {
        @Override
        public SimplePathData read(ValueInput input, RegistryAccess access) {
            SimplePathData data = new SimplePathData();
            data.progress = input.getDoubleOr("progress",0);
            ValueInput.ValueInputList realmsInput = input.childrenListOrEmpty("realms");
            for(ValueInput realmInput : realmsInput){
                MajorRealm realm = MajorRealm.of(getRealmDefinition(data.getCurrentMajorRealm()+1));
                realm.setCurrentRealm(realmInput.getIntOr("minor_realm",0));
                data.realms.add(realm);
            }
            ValueInput.ValueInputList completedTribulationsInput = input.childrenListOrEmpty("completed_tribulations");
            for(ValueInput completedTribulationInput : completedTribulationsInput){
                Realm realm = Realm.of(completedTribulationInput.getIntOr("major_realm",0),completedTribulationInput.getIntOr("minor_realm",0));
                Identifier tribulationId = completedTribulationInput.read("tribulation", Identifier.CODEC).orElse(null);
                Tribulation<?> tribulation = CoreRegistries.safeAccess(CoreRegistries.TRIBULATION_REGISTRY, tribulationId, access);
                CompletedTribulation<?> completedTribulation = tribulation == null ? null : CompletedTribulation.read(tribulationId, tribulation, completedTribulationInput);
                if(completedTribulation == null){
                    AscensionCraft.LOGGER.debug("Invalid completed tribulation {} discarding", tribulationId);
                    continue;
                }
                data.completedTribulations.put(realm,completedTribulation);
            }
            Optional<String> tribulationId = input.getString("tribulation");
            tribulationId.ifPresent(s -> data.activeTribulationId = UUID.fromString(s));
            return data;
        }

        @Override
        public void write(SimplePathData data, ValueOutput output, RegistryAccess access) {
            output.putDouble("progress",data.getProgress());

            ValueOutput.ValueOutputList realmsOutput = output.childrenList("realms");
            for(MajorRealm majorRealm : data.realms){
                ValueOutput majorRealmOutput = realmsOutput.addChild();
                majorRealmOutput.putInt("minor_realm",majorRealm.getCurrentRealm());
            }
            ValueOutput.ValueOutputList completedTribulationsOutput = output.childrenList("completed_tribulations");
            for(Realm realm : data.completedTribulations.keySet()){
                ValueOutput completedTribulationOutput = completedTribulationsOutput.addChild();
                completedTribulationOutput.putInt("major_realm",realm.majorRealm());
                completedTribulationOutput.putInt("minor_realm",realm.minorRealm());
                data.completedTribulations.get(realm).write(completedTribulationOutput);
            }
            if(data.activeTribulationId != null) output.putString("tribulation",data.activeTribulationId.toString());
        }
    }

    private final class DataSyncHandler implements SyncHandler<SimplePathData> {
        @Override
        public void decode(SimplePathData data, ByteBuf buf, RegistryAccess access) {
            data.realms.clear();
            int size = buf.readInt();
            for(int i = 0;i<size;i++){
                data.realms.add(MajorRealm.of(buf,getRealmDefinition(i)));
            }
            data.progress = buf.readDouble();
        }

        @Override
        public void encode(SimplePathData data, ByteBuf buf, RegistryAccess access) {
            buf.writeInt(data.realms.size());
            for(MajorRealm realm : data.realms){
                realm.encode(buf);
            }
            buf.writeDouble(data.progress);
        }
    }
}
