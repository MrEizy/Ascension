package net.zic.ascension.mixins.accessor;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public interface FoodDataAccessor {
    @Accessor("exhaustionLevel")
    float ascension$getExhaustionLevel();

    @Accessor("exhaustionLevel")
    void ascension$setExhaustionLevel(float exhaustionLevel);
}
