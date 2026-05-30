package net.zic.ascension.datapack.bloodline;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeHandler;
import net.zic.ascension.api.core.technique.realm_change.RealmChangeHandler;
import net.zic.ascension.api.datapack.bloodline.BloodlineType;
import net.zic.ascension.core.bloodline.SimpleBloodline;
import net.zic.ascension.core.physique.SimplePhysique;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

public class SimpleBloodlineType extends BloodlineType {


    @Override
    public MapCodec<? extends Bloodline> codec() {
        return RecordCodecBuilder.<SimpleBloodline>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimpleBloodline::getName),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(SimpleBloodline::getDescription),
                        PurityChangeHandler.CODEC.fieldOf("purity_handler").forGetter(SimpleBloodline::getListeners)
                ).apply(instance, SimpleBloodline::new)
        );
    }
}
