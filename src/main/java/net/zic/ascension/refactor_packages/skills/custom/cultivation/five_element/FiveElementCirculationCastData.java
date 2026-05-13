package net.zic.ascension.refactor_packages.skills.custom.cultivation.five_element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.refactor_packages.paths.ModPaths;
import net.zic.ascension.refactor_packages.skills.castable.ICastData;

import java.util.List;

public class FiveElementCirculationCastData implements ICastData {

    private int currentElement = 0;
    private int ticksSinceSwitch = 0;
    private final int TICKS_BETWEEN_SWITCH = 40;
    private final List<ResourceLocation> elements = List.of(
            ModPaths.FIRE.getId(),
            ModPaths.EARTH.getId(),
            ModPaths.METAL.getId(),
            ModPaths.WATER.getId(),
            ModPaths.WOOD.getId()
    );
    public FiveElementCirculationCastData(){}
    public FiveElementCirculationCastData(RegistryFriendlyByteBuf buf){
        currentElement = buf.readInt();
        ticksSinceSwitch = buf.readInt();
    }
    public void tick(){
        if(ticksSinceSwitch >= TICKS_BETWEEN_SWITCH){
            ticksSinceSwitch = 0;
            currentElement = (currentElement+1)%elements.size();
        }else ticksSinceSwitch += 1;
    }
    public ResourceLocation getElement(){
        return elements.get(currentElement);
    }



    @Override
    public CompoundTag write() {
        return new CompoundTag();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeInt(currentElement);
        buf.writeInt(ticksSinceSwitch);
    }
}
