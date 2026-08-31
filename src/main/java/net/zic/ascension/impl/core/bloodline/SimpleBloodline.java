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
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.datapack.bloodline.AscensionBloodlineTypes;
import net.zic.ascension.impl.core.technique.TechniqueSkillService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SimpleBloodline implements Bloodline {

    private final Component name;
    private final Component description;
    private final ProgressActionHolder holder;
    private final List<Identifier> unlockedPaths;
    private final Optional<AscensionItemTooltipDefinition> itemTooltip;
    private final RequirementHolder requirements;

    public SimpleBloodline(
            Component name,
            Component description,
            List<Identifier> unlockedPaths,
            ProgressActionHolder holder,
            Optional<AscensionItemTooltipDefinition> itemTooltip,
            RequirementHolder requirements
    ) {
        this.name = name;
        this.description = description;
        this.unlockedPaths = unlockedPaths == null ? List.of() : List.copyOf(unlockedPaths);
        this.itemTooltip = itemTooltip == null ? Optional.empty() : itemTooltip;
        this.holder = holder;
        this.requirements = requirements == null ? RequirementHolder.EMPTY : requirements;
    }

    @Override
    public RequirementHolder requirements() {
        return requirements;
    }

    @Override
    public BloodlineType getType() {
        return AscensionBloodlineTypes.SIMPLE_BLOODLINE_TYPE.get();
    }

    public ProgressActionHolder getHolder(){return holder;}

    public List<Identifier> unlockedPaths() {
        return unlockedPaths;
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
    public Optional<AscensionItemTooltipDefinition> itemTooltip() {
        return itemTooltip;
    }

    @Override
    public Collection<Identifier> onAdded(OriginSource source, BloodlineData data) {

        holder.run(source, CoreRegistries.BLOODLINE_REGISTRY.get(source.getRegistryAccess()).getKey(this), data,ProgressDirection.UP);
        return unlockedPaths;
    }

    @Override
    public Collection<Identifier> onRemoved(OriginSource source, BloodlineData data) {
        Identifier bloodlineId = CoreRegistries.BLOODLINE_REGISTRY.get(source.getRegistryAccess()).getKey(this);
        for (int purity = data.getPurity(); purity >= 1; purity--) {
            data.setPurity(purity);
            holder.run(source, bloodlineId, data, ProgressDirection.DOWN);
        }
        data.setPurity(0);
        TechniqueSkillService.reconcileAll(source);
        return unlockedPaths;
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
        TechniqueSkillService.reconcileAll(source);
    }

    @Override
    public void purityUp(OriginSource source, BloodlineData data) {
        holder.run(source, CoreRegistries.BLOODLINE_REGISTRY.get(source.getRegistryAccess()).getKey(this),data, ProgressDirection.UP);
        TechniqueSkillService.reconcileAll(source);
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
