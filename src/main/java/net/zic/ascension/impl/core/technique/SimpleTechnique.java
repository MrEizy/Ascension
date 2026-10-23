package net.zic.ascension.impl.core.technique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.progression.ProgressActionList;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.core.technique.TechniqueInstance;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.datapack.technique.AscensionTechniqueTypes;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class SimpleTechnique implements Technique<SimpleTechnique.Data> {
    private static final SerializerHandler<Data> SERIALIZER_HANDLER = new DataSerializerHandler();
    private static final SyncHandler<Data> SYNC_HANDLER = new SyncHandler.UnitSyncHandler<>();

    private final Component name;
    private final Component description;
    private final ProgressActionList onAddedActions;
    private final Optional<AscensionItemTooltipDefinition> itemTooltip;
    private final RequirementHolder requirements;

    public SimpleTechnique(
            Component name,
            Component description,
            ProgressActionList onAddedActions,
            Optional<AscensionItemTooltipDefinition> itemTooltip,
            RequirementHolder requirements
    ) {
        this.name = name;
        this.description = description;
        this.onAddedActions = onAddedActions == null ? ProgressActionList.EMPTY : onAddedActions;
        this.itemTooltip = itemTooltip == null ? Optional.empty() : itemTooltip;
        this.requirements = requirements == null ? RequirementHolder.EMPTY : requirements;
    }

    public ProgressActionList getOnAddedActions() {
        return onAddedActions;
    }

    @Override
    public RequirementHolder requirements() {
        return requirements;
    }

    @Override
    public TechniqueType getType() {
        return AscensionTechniqueTypes.SIMPLE_TECHNIQUE_TYPE.get();
    }

    @Override
    public Component getName(@Nullable Data data) {
        return name;
    }

    @Override
    public Component getDescription(@Nullable Data data) {
        return description;
    }

    @Override
    public Optional<AscensionItemTooltipDefinition> itemTooltip() {
        return itemTooltip;
    }

    @Override
    public void onAdded(OriginSource source, TechniqueInstance<Data> instance) {
        runActions(source, instance, ProgressDirection.UP);
    }

    @Override
    public void onRemoved(OriginSource source, TechniqueInstance<Data> instance) {
        runActions(source, instance, ProgressDirection.DOWN);
    }

    @Override
    public Data newData(RegistryAccess access) {
        return new Data();
    }

    @Override
    public SerializerHandler<Data> serializerHandler() {
        return SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<Data> syncHandler() {
        return SYNC_HANDLER;
    }

    private void runActions(OriginSource source, TechniqueInstance<Data> instance, ProgressDirection direction) {
        onAddedActions.run(source, instance.getTechniqueId(), instance.getData(), direction, instance.getTechniqueId().getPath());
    }

    //simple techniques hold no state
    public static final class Data implements TechniqueData {
    }

    private static final class DataSerializerHandler implements SerializerHandler<Data> {
        @Override
        public Data read(ValueInput input, RegistryAccess access) {
            return new Data();
        }

        @Override
        public void write(Data writable, ValueOutput output, RegistryAccess access) {
        }
    }
}
