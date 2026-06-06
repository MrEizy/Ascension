package net.zic.ascension.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.progression.ProgressDirection;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.progression.ProgressActionHolder;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.bloodline.BloodlineType;
import net.zic.ascension.datapack.bloodline.AscensionBloodlineTypes;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SimpleBloodline implements Bloodline {

    private final Component name;
    private final Component description;
    private final ProgressActionHolder holder;

    public SimpleBloodline(Component name, Component description, Map<Identifier,List<Identifier>> listeners){
        this.name = name;
        this.description = description;
        this.holder = ProgressActionHolder.fromMap(listeners);
    }

    @Override
    public BloodlineType getType() {
        return AscensionBloodlineTypes.SIMPLE_BLOODLINE_TYPE.get();
    }

    public Map<Identifier,List<Identifier>> getListeners(){
        return holder.listeners();
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public Collection<Identifier> onAdded(OriginSource source, BloodlineData data) {

        holder.run(source, CoreRegistries.BLOODLINE_REGISTRY.get(source.getRegistryAccess()).getKey(this), data,ProgressDirection.UP);
        return List.of();
    }

    @Override
    public Collection<Identifier> onRemoved(OriginSource source, BloodlineData data) {

        data.setPurity(0);
        holder.run(source, CoreRegistries.BLOODLINE_REGISTRY.get(source.getRegistryAccess()).getKey(this),data, ProgressDirection.DOWN);
        return List.of();
    }

    @Override
    public void applyToEntity(LivingEntity entity, BloodlineData data) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, BloodlineData data) {

    }

    @Override
    public void purityDown(OriginSource source, BloodlineData data) {
        holder.run(source, CoreRegistries.BLOODLINE_REGISTRY.get(source.getRegistryAccess()).getKey(this),data, ProgressDirection.DOWN);
    }

    @Override
    public void purityUp(OriginSource source, BloodlineData data) {
        holder.run(source, CoreRegistries.BLOODLINE_REGISTRY.get(source.getRegistryAccess()).getKey(this),data, ProgressDirection.UP);

    }

    @Override
    public BloodlineData newData() {
        return new SimpleBloodlineData();
    }

    @Override
    public BloodlineData loadData(ValueInput input) {
        return new SimpleBloodlineData(input);
    }

    @Override
    public BloodlineData loadData(ByteBuf buf) {
        return new SimpleBloodlineData(buf);
    }
}
