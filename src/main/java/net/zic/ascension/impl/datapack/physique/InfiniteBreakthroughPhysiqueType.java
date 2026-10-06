package net.zic.ascension.impl.datapack.physique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.path.PathBonusBase;
import net.zic.ascension.api.ascension.datapack.path.PathBonusModifier;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.physique.InfiniteBreakthroughPhysique;
import net.zic.ascension.impl.core.physique.SimplePhysique;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import net.zic.zenithlib.value_containers.typed.ValueContainerCodecHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InfiniteBreakthroughPhysiqueType extends PhysiqueType {
    @Override
    public MapCodec<? extends Physique> codec() {
        return RecordCodecBuilder.<InfiniteBreakthroughPhysique>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(InfiniteBreakthroughPhysique::name),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(InfiniteBreakthroughPhysique::description),
                        Identifier.CODEC.listOf().fieldOf("paths").forGetter(InfiniteBreakthroughPhysique::unlockedPaths),
                        Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(InfiniteBreakthroughPhysique::skills),
                        ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).optionalFieldOf("stats",Map.of()).forGetter(InfiniteBreakthroughPhysique::statModifiers),
                        PathBonusHolder.MODIFIER_CODEC.optionalFieldOf("path_bonus",Map.of()).forGetter(InfiniteBreakthroughPhysique::pathBonusModifiers),
                        Identifier.CODEC.fieldOf("infinite_path").forGetter(InfiniteBreakthroughPhysique::path),
                        Codec.INT.fieldOf("infinite_realm").forGetter(InfiniteBreakthroughPhysique::infiniteRealm),
                        AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(InfiniteBreakthroughPhysique::itemTooltip),
                        RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(InfiniteBreakthroughPhysique::requirements)
                ).apply(instance, InfiniteBreakthroughPhysique::new)
        );
    }


}