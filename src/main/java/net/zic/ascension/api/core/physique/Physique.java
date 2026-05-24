package net.zic.ascension.api.core.physique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.physique.PhysiqueType;

public interface  Physique {


    PhysiqueType getType();


    PhysiqueData newData();
}
