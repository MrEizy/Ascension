package net.zic.ascension.impl.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.impl.datapack.bloodline.AscensionBloodlineTypes;

public class SimpleBloodlineData implements BloodlineData {

    private int purity;

    public SimpleBloodlineData(int purity){
        this.purity =  purity;
    }
    public SimpleBloodlineData(){
        this.purity = 1;
    }

    public SimpleBloodlineData(ValueInput input){
        this.purity = input.getIntOr("purity",1);
    }
    public SimpleBloodlineData(ByteBuf buf){
        this.purity = buf.readInt();
    }


    @Override
    public int getPurity() {
        return purity;
    }

    @Override
    public void setPurity(int newPurity) {
        this.purity = newPurity;
    }

    @Override
    public BloodlineType getType() {
        return AscensionBloodlineTypes.SIMPLE_BLOODLINE_TYPE.get();
    }

    @Override
    public void write(ValueOutput output) {
        output.putInt("purity",purity);
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(purity);
    }
}
