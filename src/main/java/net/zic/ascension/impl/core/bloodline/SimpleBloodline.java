package net.zic.ascension.impl.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.source.OriginSource;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.datapack.bloodline.AscensionBloodlineTypes;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SimpleBloodline implements Bloodline {

    private final Component name;
    private final Component description;
    private final ProgressActionHolder holder;
    private final Optional<AscensionItemTooltipDefinition> itemTooltip;

    public SimpleBloodline(
            Component name,
            Component description,
            ProgressActionHolder holder,
            Optional<AscensionItemTooltipDefinition> itemTooltip
    ) {
        this.name = name;
        this.description = description;
        this.itemTooltip = itemTooltip == null ? Optional.empty() : itemTooltip;
        this.holder = holder;
    }

    @Override
    public BloodlineType getType() {
        return AscensionBloodlineTypes.SIMPLE_BLOODLINE_TYPE.get();
    }

    public ProgressActionHolder getHolder(){return holder;}

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public Optional<AscensionItemTooltipDefinition> itemTooltip() {
        return itemTooltip;
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
    public BloodlineData newData(RegistryAccess access) {
        return new SimpleBloodlineData();
    }

    @Override
    public BloodlineData loadData(ValueInput input,RegistryAccess access) {
        return new SimpleBloodlineData(input);
    }

    @Override
    public BloodlineData loadData(ByteBuf buf) {
        return new SimpleBloodlineData(buf);
    }
}
