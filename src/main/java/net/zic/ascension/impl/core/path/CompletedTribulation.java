package net.zic.ascension.impl.core.path;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.tribulation.Tribulation;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.core.tribulation.TribulationInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import org.jspecify.annotations.Nullable;

public record CompletedTribulation<T extends TribulationData>(Identifier id, Tribulation<T> tribulation, T data) {

    public static <T extends TribulationData> CompletedTribulation<T> of(TribulationInstance<T> instance) {
        return new CompletedTribulation<>(instance.getTribulationId(), instance.getTribulation(), instance.getData());
    }

    //creates completed data without the tribulation having been run
    public static <T extends TribulationData> CompletedTribulation<T> fresh(Identifier id, Tribulation<T> tribulation) {
        return new CompletedTribulation<>(id, tribulation, tribulation.newData());
    }

    //returns null if the data is missing or rejected by the tribulation
    //TODO update to use SERIALIZER for data reading/writing
    public static <T extends TribulationData> @Nullable CompletedTribulation<T> read(Identifier id, Tribulation<T> tribulation, ValueInput input) {
        try{
            T data = input.read("data", tribulation.getType().dataCodec()).map(tribulation::validateLoadedData).orElse(null);
            return data == null ? null : new CompletedTribulation<>(id, tribulation, data);
        } catch (Exception e){
            AscensionCraft.LOGGER.debug("unable to read tribulation");
        }
        return null;
    }

    //checks the data still matches the tribulation, replacing it with fresh data if not
    public CompletedTribulation<T> revalidate() {
        T validated = tribulation.validateLoadedData(data);
        return new CompletedTribulation<>(id, tribulation, validated == null ? tribulation.newData() : validated);
    }

    public void onAdded(OriginSource source) {
        tribulation.onAdded(source, data);
    }

    public void onRemoved(OriginSource source) {
        tribulation.onRemoved(source, data);
    }

    public void write(ValueOutput output) {
        output.store("tribulation", Identifier.CODEC, id);
        output.store("data", tribulation.getType().dataCodec(), data);
    }
}
