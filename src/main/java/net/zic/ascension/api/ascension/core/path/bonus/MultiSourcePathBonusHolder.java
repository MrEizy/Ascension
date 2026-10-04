package net.zic.ascension.api.ascension.core.path.bonus;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.ZenithStatHolder;
import net.zic.zenithlib.util.Processable;
import net.zic.zenithlib.value_containers.typed.ValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

//TODO:
// Consider adding a calculatedCategories and a categories.
// doing it as such lets us attach bonuses directly to an entity
// without going through ascension entity data.(also removes bloat, do something similar with stats)

public class MultiSourcePathBonusHolder extends Processable {
    private final LivingEntity attachedEntity;
    private final HashSet<PathBonusProvider> providers = new HashSet<>();

    //maps a category -> category bonus holder
    private final HashMap<Identifier,PathBonusCategoryHolder> categories = new HashMap<>();

    private final HashSet<Identifier> dirtyCategories = new HashSet<>();


    public MultiSourcePathBonusHolder(LivingEntity attachedEntity) {
        this.attachedEntity = attachedEntity;
        setOnResolved(this::sync);
    }
    public void registerPathBonusProvider(PathBonusProvider provider){
        providers.add(provider);
        updateBonuses(provider.getAllPathBonuses());

    }
    public void removePathBonusProvider(PathBonusProvider provider){
        providers.remove(provider);
        updateBonuses(provider.getAllPathBonuses());
    }

    public void sync(){
        if(attachedEntity == null) return;
        attachedEntity.syncData(AscensionAttachments.PATH_BONUS_HOLDER);
    }
    public void updateBonus(PathBonus bonus){
        String processId = "small_path_bonus_update"+UUID.randomUUID();
        startProcess(processId);
        List<ValueContainer<Double>> containers = new ArrayList<>();
        for(PathBonusProvider provider : providers){
            ValueContainer<Double> instance = provider.getPathBonusContainer(bonus.category(),bonus.path());
            if(instance == null) continue;
            containers.add(instance);
        }
        ValueContainer<Double> container = ValueContainer.from(
                base->ValueContainerHelpers.doubleValueContainer(bonus.path(),base),
                containers
        );
        if(container == null && !categories.containsKey(bonus.category())) getCategoryHolder(bonus.category()).removePath(bonus.path());
        else getCategoryHolder(bonus.category()).setPathBonusContainer(bonus.path(),container);

        resolveProcess(processId);
    }

    public void updateBonuses(Collection<PathBonus> pathBonuses){
        String processId = "bulk_path_bonus_update"+UUID.randomUUID();
        startProcess(processId);
        for(PathBonus pathBonus:pathBonuses) updateBonus(pathBonus);
        resolveProcess(processId);
    }

    protected PathBonusCategoryHolder getCategoryHolder(Identifier category){
        categories.computeIfAbsent(category,key->new PathBonusCategoryHolder());
        return categories.get(category);
    }



    public double getBonus(Identifier category,Identifier path){
        return categories.containsKey(category) ? getCategoryHolder(category).getBonus(path) : 0;
    }



    public Collection<PathBonus> getAllPathBonuses(){
        HashSet<PathBonus> pathBonuses = new HashSet<>();
        for(Identifier category:categories.keySet()){
            getCategoryHolder(category).getAllPaths().forEach(
                    path->pathBonuses.add(new PathBonus(category,path))
            );
        }
        return pathBonuses;
    }
    public Collection<Identifier> getAllPathBonusesInCategory(Identifier category){
        return getCategoryHolder(category).getAllPaths();
    }
    public Collection<PathBonus> getDirtyPathBonuses(){
        HashSet<PathBonus> pathBonuses = new HashSet<>();
        for(Identifier category:categories.keySet()){
            getCategoryHolder(category).getDirtyPaths().forEach(
                    path->pathBonuses.add(new PathBonus(category,path))
            );
        }
        return pathBonuses;
    }

    protected void encode(ByteBuf buf){
        buf.writeInt(categories.size());
        for(Identifier category : categories.keySet()){
            ByteBufHelpers.encodeIdentifier(category,buf);
            categories.get(category).encode(buf,true);
        }
    }
    public void decode(ByteBuf buf){

        if(buf.readBoolean()) categories.clear();
        int size = buf.readInt();
        for(int i = 0;i<size; i++){
            Identifier category = ByteBufHelpers.decodeIdentifier(buf);
            PathBonusCategoryHolder holder = getCategoryHolder(category);
            holder.decode(buf);
        }
        dirtyCategories.clear();

    }

    public static class SyncHandler implements AttachmentSyncHandler<MultiSourcePathBonusHolder> {

        @Override
        public void write(@NonNull RegistryFriendlyByteBuf buf, MultiSourcePathBonusHolder attachment, boolean initialSync) {

            attachment.encode(buf);
        }

        @Override
        public @Nullable MultiSourcePathBonusHolder read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, @Nullable MultiSourcePathBonusHolder previousValue) {
            if(!(holder instanceof LivingEntity entity)) return null;
            if(previousValue == null) previousValue = new MultiSourcePathBonusHolder(entity);
            previousValue.decode(buf);
            return previousValue;
        }

        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return true;
        }
    }
}
