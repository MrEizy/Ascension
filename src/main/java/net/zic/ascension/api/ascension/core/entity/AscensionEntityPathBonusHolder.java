package net.zic.ascension.api.ascension.core.entity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusProvider;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.StatInstance;
import net.zic.zenithlib.stats.StatProvider;
import net.zic.zenithlib.stats.ZenithStatHolder;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class AscensionEntityPathBonusHolder implements PathBonusProvider {
    private final LivingEntity attachedEntity;
    private final HashSet<PathBonusProvider> providers = new HashSet<>();

    private PathBonusHolder cachedHolder = new PathBonusHolder();

    public AscensionEntityPathBonusHolder(LivingEntity attachedEntity) {
        this.attachedEntity = attachedEntity;
    }

    @Override
    public ValueContainer getPathBonusContainer(Identifier category, Identifier path) {
        return cachedHolder.getPathBonusContainer(category,path);
    }

    @Override
    public double getPathBonus(Identifier category, Identifier path) {
        return cachedHolder.getBonus(category,path);
    }

    @Override
    public Collection<PathBonus> getAllPathBonuses() {
        return cachedHolder.getAllPathBonuses();
    }

    @Override
    public Collection<Identifier> getAllPathBonusesInCategory(Identifier category) {
        return cachedHolder.getAllPathBonusesInCategory(category);
    }

    public void updatePathBonus(PathBonus bonus){
        //temp
        if(! (attachedEntity instanceof Player)) return;
        ValueContainer container = new ValueContainer(bonus.path(),0);
        for(PathBonusProvider provider : providers){
            ValueContainer providerContainer = provider.getPathBonusContainer(bonus.category(),bonus.path());
            if(providerContainer == null) continue;
            container.setBaseValue(container.getBaseValue()+providerContainer.getBaseValue());
            for(ValueContainerModifier modifier : providerContainer.getAllModifiers()){
                container.addModifierNoCacheUpdate(modifier);
            }
        }
        container.calculateCachedVal();
        cachedHolder.setPathBonusContainer(bonus.category(),bonus.path(),container);
    }
    public void updatePathBonuses(Collection<PathBonus> bonuses){
        for(PathBonus bonus : bonuses) updatePathBonus(bonus);
    }

    public void registerPathBonusProvider(PathBonusProvider provider){
        if(providers.contains(provider)) return;
        providers.add(provider);
        for(PathBonus bonus : provider.getAllPathBonuses()){
            updatePathBonus(bonus);
        }
    }
    public void removePathBonusProvider(PathBonusProvider provider){
        if(!providers.contains(provider)) return;
        providers.remove(provider);
        for(PathBonus bonus : provider.getAllPathBonuses()){
            updatePathBonus(bonus);
        }
    }
    public static class SyncHandler implements AttachmentSyncHandler<AscensionEntityPathBonusHolder> {

        @Override
        public void write(@NonNull RegistryFriendlyByteBuf buf, AscensionEntityPathBonusHolder attachment, boolean initialSync) {


        }

        @Override
        public @Nullable AscensionEntityPathBonusHolder read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, @Nullable AscensionEntityPathBonusHolder previousValue) {
            return null;
        }

        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return true;
        }
    }
}
