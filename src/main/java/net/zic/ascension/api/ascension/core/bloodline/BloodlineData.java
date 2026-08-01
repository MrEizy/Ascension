package net.zic.ascension.api.ascension.core.bloodline;

import net.zic.ascension.api.ascension.core.RegistryObjectData;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;

public interface BloodlineData extends RegistryObjectData {
    int getPurity();
    void setPurity(int newPurity);


    BloodlineType getType();

}
