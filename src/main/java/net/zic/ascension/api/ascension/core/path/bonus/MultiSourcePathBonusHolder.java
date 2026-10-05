package net.zic.ascension.api.ascension.core.path.bonus;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatProvider;

import net.zic.zenithlib.util.Processable;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

//TODO:
// Consider adding a calculatedCategories and a categories.
// doing it as such lets us attach bonuses directly to an entity
// without going through ascension entity data.(also removes bloat, do something similar with stats)

public class MultiSourcePathBonusHolder extends Processable implements PathBonusProvider {

    private final PathBonusHolder internalHolder = new PathBonusHolder();
    private final PathBonusHolder cachedPathBonusHolder = new PathBonusHolder();

    private final Set<PathBonusProvider> providers = new HashSet<>();

    private boolean cachedHolderDirty = false;

    public MultiSourcePathBonusHolder(){
        setOnResolved(this::onUpdate);
    }

    public void onUpdate(){

        if(cachedHolderDirty){
            cachedHolderUpdated();
            cachedHolderDirty = false;
        }else{
            Collection<PathBonus> toUpdate = new ArrayList<>(internalHolder.dirtyBonuses());
            toUpdate.addAll(internalHolder.removedBonuses());
            updatePathBonuses(toUpdate);
            internalHolder.clearCache();
        }
    }


    //====================== INTERNAL HOLDER ======================
    public void addFlatModifier(Identifier category, Identifier path, Modifier<Double> modifier){
        internalHolder.addFlatModifier(category,path,modifier);
        startAndResolveProcess();
    }
    public void addMultiplierModifier(Identifier category,Identifier path,Modifier<Double> modifier){
        internalHolder.addMultiplierModifier(category,path,modifier);
        startAndResolveProcess();

    }
    public void removeModifier(Identifier category,Identifier path,Identifier modifier){
        internalHolder.removeModifier(category,path,modifier);
        startAndResolveProcess();

    }



    //====================== CACHED HOLDER ======================
    public void registerProvider(PathBonusProvider provider){
        providers.add(provider);
        updatePathBonuses(provider.getAllPathBonuses());

    }
    public void removeProvider(PathBonusProvider provider){
        providers.remove(provider);
        updatePathBonuses(provider.getAllPathBonuses());
    }

    public void cachedHolderUpdated(){
        //TODO run some code in here mainly sync
    }

    public void updatePathBonus(PathBonus bonus){
        cachedHolderDirty = true;
        String processId = String.valueOf(UUID.randomUUID());
        startProcess(processId);

        List<ValueContainer<Double>> containers = new ArrayList<>();
        ValueContainer<Double> internal = internalHolder.getContainer(bonus.category(),bonus.path());
        if(internal != null) containers.add(internal);

        for(PathBonusProvider provider : providers){
            ValueContainer<Double> instance = provider.getPathBonusContainer(bonus.category(),bonus.path());
            if(instance == null) continue;
            containers.add(instance);
        }
        ValueContainer<Double> container = ValueContainer.from(
                ()-> ValueContainerHelpers.doubleValueContainer(bonus.path()),
                containers
        );
        if(container == null) cachedPathBonusHolder.removePathBonusContainer(bonus.category(),bonus.path());
        else cachedPathBonusHolder.setPathBonusContainer(bonus.category(),container);
        resolveProcess(processId);
    }
    public void updatePathBonuses(Collection<PathBonus> bonuses){
        String processId = String.valueOf(UUID.randomUUID());
        startProcess(processId);

        for(PathBonus bonus : bonuses) updatePathBonus(bonus);
        cachedHolderDirty = true;
        resolveProcess(processId);
    }

    protected PathBonusHolder getCachedPathBonusHolder(){
        return cachedPathBonusHolder;
    }
    //====================== PATH BONUS PROVIDER ======================

    @Override
    public ValueContainer<Double> getPathBonusContainer(Identifier category, Identifier path) {
        return cachedPathBonusHolder.getContainer(category,path);
    }

    @Override
    public double getPathBonus(Identifier category, Identifier path) {
        return cachedPathBonusHolder.getPathBonus(category,path);
    }

    @Override
    public Collection<PathBonus> getAllPathBonuses() {
        return cachedPathBonusHolder.getPathBonuses();
    }

    @Override
    public Collection<Identifier> getAllPathBonusesInCategory(Identifier category) {
        return getAllPathBonuses().stream().filter(bonus->bonus.category().equals(category)).map(PathBonus::path).collect(Collectors.toSet());
    }


}
