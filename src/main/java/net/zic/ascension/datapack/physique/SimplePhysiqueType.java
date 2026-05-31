package net.zic.ascension.datapack.physique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.ascension.core.physique.SimplePhysique;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

public class SimplePhysiqueType extends PhysiqueType {
    @Override
    public MapCodec<? extends Physique> codec() {
        return RecordCodecBuilder.<SimplePhysique>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimplePhysique::getName),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(SimplePhysique::getDescription),
                        Identifier.CODEC.listOf().fieldOf("paths").forGetter(SimplePhysique::getUnlockedPaths),
                        Identifier.CODEC.listOf().fieldOf("skills").forGetter(SimplePhysique::getSkills),
                        ValueContainer.BASE_MODIFIER_CODEC.listOf().fieldOf("base_stats").forGetter(SimplePhysique::getBaseStats),
                        ValueContainerModifier.MAP_CODEC.fieldOf("stat_modifiers").forGetter(SimplePhysique::getStatModifiers),
                        ValueContainer.BASE_MODIFIER_CODEC.listOf().fieldOf("base_affinity").forGetter(SimplePhysique::getBaseAffinities),
                        ValueContainerModifier.MAP_CODEC.fieldOf("affinity_modifiers").forGetter(SimplePhysique::getAffinityModifiers)
                        ).apply(instance, SimplePhysique::new)
        );
    }
}
