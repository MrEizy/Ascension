package net.zic.ascension.chunks.atmospheric_qi;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.zenithlib.util.Processable;
import net.zic.zenithlib.value_containers.ValueContainerModifier;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.*;
import java.util.stream.Collectors;

//TODO change to use new value containers
public class ChunkPathAffinityProvider extends Processable implements PathBonusProvider{
    public static final Identifier AFFINITY_CATEGORY = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"affinity");
    private final ChunkAccess access;

    private final PathBonusHolder internalHolder = new PathBonusHolder();
    private final Set<LivingEntity> internalEntities = new HashSet<>();
    public ChunkPathAffinityProvider(ChunkAccess access) {
        this.access = access;
        setOnResolved(this::updateEntities);
    }
    public void trackEntity(LivingEntity entity){
        internalEntities.add(entity);
    }
    public void untrackEntity(LivingEntity entity){

    }

    public void updateEntities(){
        Collection<PathBonus> toUpdate = new ArrayList<>(internalHolder.dirtyBonuses());
        toUpdate.addAll(internalHolder.removedBonuses());
        for(LivingEntity entity: internalEntities) entity.getData(CoreAttachments.PATH_BONUS_HOLDER).updatePathBonuses(toUpdate);
        internalHolder.clearCache();
    }

    public void addFlatModifier(Identifier path, Modifier<Double> modifier){
        internalHolder.addFlatModifier(AFFINITY_CATEGORY,path,modifier);
        startAndResolveProcess();
    }
    public void addMultiplierModifier(Identifier path,Modifier<Double> modifier){
        internalHolder.addMultiplierModifier(AFFINITY_CATEGORY,path,modifier);
        startAndResolveProcess();

    }
    public void removeModifier(Identifier path,Identifier modifier){
        internalHolder.removeModifier(AFFINITY_CATEGORY,path,modifier);
        startAndResolveProcess();

    }


    @Override
    public ValueContainer<Double> getPathBonusContainer(Identifier category, Identifier path) {
        return internalHolder.getContainer(category,path);
    }

    @Override
    public double getPathBonus(Identifier category, Identifier path) {
        return internalHolder.getPathBonus(category,path);
    }

    @Override
    public Collection<PathBonus> getAllPathBonuses() {
        return internalHolder.getPathBonuses();
    }

    @Override
    public Collection<Identifier> getAllPathBonusesInCategory(Identifier category) {
        return getAllPathBonuses().stream().filter(bonus->bonus.category().equals(category)).map(PathBonus::path).collect(Collectors.toSet());
    }

}
