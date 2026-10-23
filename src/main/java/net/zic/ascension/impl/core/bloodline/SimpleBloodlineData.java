package net.zic.ascension.impl.core.bloodline;

import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;

public class SimpleBloodlineData implements BloodlineData {

    private int purity;

    public SimpleBloodlineData(int purity){
        this.purity =  purity;
    }
    public SimpleBloodlineData(){
        this.purity = 1;
    }


    @Override
    public int getPurity() {
        return purity;
    }

    @Override
    public void setPurity(int newPurity) {
        this.purity = newPurity;
    }
}
