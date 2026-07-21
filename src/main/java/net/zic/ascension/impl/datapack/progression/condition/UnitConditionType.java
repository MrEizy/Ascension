package net.zic.ascension.impl.datapack.progression.condition;


import com.mojang.serialization.MapCodec;
import net.zic.ascension.api.ascension.core.progression.ProgressActionCondition;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;

import java.util.function.Supplier;

public class UnitConditionType<T extends ProgressActionCondition> extends ProgressActionConditionType {
    private final Supplier<T> supplier;

    public UnitConditionType(Supplier<T> supplier){
        this.supplier = supplier;
    }

    @Override
    public MapCodec<? extends ProgressActionCondition> codec() {
        return MapCodec.unit(supplier);
    }
}
