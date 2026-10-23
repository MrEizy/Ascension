package net.zic.ascension.impl.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineInstance;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.datapack.bloodline.AscensionBloodlineTypes;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SimpleBloodline implements Bloodline<SimpleBloodlineData> {
    private static final SerializerHandler<SimpleBloodlineData> SERIALIZER_HANDLER = new DataSerializerHandler();
    private static final SyncHandler<SimpleBloodlineData> SYNC_HANDLER = new DataSyncHandler();


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
    public Component getName(@Nullable SimpleBloodlineData data) {
        return name;
    }

    @Override
    public Component getDescription(@Nullable SimpleBloodlineData data) {
        return description;
    }

    @Override
    public Optional<AscensionItemTooltipDefinition> itemTooltip() {
        return itemTooltip;
    }

    @Override
    public Collection<Identifier> onAdded(OriginSource source, BloodlineInstance<SimpleBloodlineData> instance) {
        holder.run(source, instance.getBloodlineId(), instance.getData(), ProgressDirection.UP,"_0");
        return unlockedPaths;
    }

    @Override
    public Collection<Identifier> onRemoved(OriginSource source, BloodlineInstance<SimpleBloodlineData> instance) {
        SimpleBloodlineData data = instance.getData();
        for (int purity = data.getPurity(); purity >= 1; purity--) {
            data.setPurity(purity - 1);
            holder.run(source, instance.getBloodlineId(), data, ProgressDirection.DOWN,"_0");
        }
        data.setPurity(0);
        return unlockedPaths;
    }

    @Override
    public void applyToEntity(LivingEntity entity, BloodlineInstance<SimpleBloodlineData> instance) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, BloodlineInstance<SimpleBloodlineData> instance) {

    }

    @Override
    public void purityDown(OriginSource source, BloodlineInstance<SimpleBloodlineData> instance) {
        SimpleBloodlineData data = instance.getData();
        holder.run(source, instance.getBloodlineId(), data, ProgressDirection.DOWN,String.valueOf(data.getPurity()+1));
    }

    @Override
    public void purityUp(OriginSource source, BloodlineInstance<SimpleBloodlineData> instance) {
        SimpleBloodlineData data = instance.getData();
        holder.run(source, instance.getBloodlineId(), data, ProgressDirection.UP,String.valueOf(data.getPurity()));
    }

    @Override
    public SimpleBloodlineData newData(RegistryAccess access) {
        return new SimpleBloodlineData();
    }

    @Override
    public SerializerHandler<SimpleBloodlineData> serializerHandler() {
        return SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<SimpleBloodlineData> syncHandler() {
        return SYNC_HANDLER;
    }

    private static final class DataSerializerHandler implements SerializerHandler<SimpleBloodlineData> {
        @Override
        public SimpleBloodlineData read(ValueInput input, RegistryAccess access) {
            return new SimpleBloodlineData(input.getIntOr("purity", 1));
        }

        @Override
        public void write(SimpleBloodlineData writable, ValueOutput output, RegistryAccess access) {
            output.putInt("purity", writable.getPurity());
        }
    }

    private static final class DataSyncHandler implements SyncHandler<SimpleBloodlineData> {
        @Override
        public void decode(SimpleBloodlineData existingEncodable, ByteBuf buf, RegistryAccess access) {
            existingEncodable.setPurity(buf.readInt());
        }

        @Override
        public void encode(SimpleBloodlineData encodable, ByteBuf buf, RegistryAccess access) {
            buf.writeInt(encodable.getPurity());
        }
    }
}
