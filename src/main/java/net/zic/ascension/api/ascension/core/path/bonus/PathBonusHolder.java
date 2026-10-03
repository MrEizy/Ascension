package net.zic.ascension.api.ascension.core.path.bonus;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.zic.ascension.api.ascension.core.CoreHolderProviders;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatProvider;
import net.zic.zenithlib.stats.ZenithStatHolder;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.RangedValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

//TODO:
// set up such that on changes we notify attached entities
// then those entities trigger an update, and use a combined value container
public class PathBonusHolder {
    private final LivingEntity attachedEntity;
    private final HashSet<PathBonusProvider> providers = new HashSet<>();

    //maps a category -> category bonus holder
    private final HashMap<Identifier,PathBonusCategoryHolder> categories = new HashMap<>();

    private final HashSet<Identifier> dirtyCategories = new HashSet<>();
    private String process = null;
    private final Random random  = new Random();


    public PathBonusHolder(LivingEntity attachedEntity) {
        this.attachedEntity = attachedEntity;
    }
    public void registerPathBonusProvider(PathBonusProvider provider){
        providers.add(provider);
        updateBonuses(provider.getAllPathBonuses());

    }
    public void removePathBonusProvider(PathBonusProvider provider){
        providers.remove(provider);
        updateBonuses(provider.getAllPathBonuses());
    }

    public void startProcess(String process){
        if(this.process == null) this.process = process;
    }
    public boolean resolveProcess(String process){

        if(this.process == null) return false;
        if(!this.process.equals(process)) return false;
        this.process = null;

        sync();
        return true;
    }
    public void sync(){
        if(attachedEntity == null) return;
        attachedEntity.syncData(AscensionAttachments.PATH_BONUS_HOLDER);
    }
    public void updateBonus(PathBonus bonus){
        String processId = "small_path_bonus_update"+random.nextLong();
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

        resolveProcess(process);
    }

    public void updateBonuses(Collection<PathBonus> pathBonuses){
        String processId = "bulk_path_bonus_update"+random.nextLong();
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

    public static class SyncHandler implements AttachmentSyncHandler<PathBonusHolder> {

        @Override
        public void write(@NonNull RegistryFriendlyByteBuf buf, PathBonusHolder attachment, boolean initialSync) {

            attachment.encode(buf);
        }

        @Override
        public @Nullable PathBonusHolder read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, @Nullable PathBonusHolder previousValue) {
            if(!(holder instanceof LivingEntity entity)) return null;
            if(previousValue == null) previousValue = new PathBonusHolder(entity);
            previousValue.decode(buf);
            return previousValue;
        }

        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return true;
        }
    }
}
