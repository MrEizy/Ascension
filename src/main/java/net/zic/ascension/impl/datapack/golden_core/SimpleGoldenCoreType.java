package net.zic.ascension.impl.datapack.golden_core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.golden_core.GoldenCore;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.progression.ProgressActionList;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.golden_core.GoldenCoreType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.golden_core.SimpleGoldenCore;
import net.zic.zenithlib.value_containers.typed.ValueContainerCodecHelper;

import java.util.List;
import java.util.Map;

public class SimpleGoldenCoreType extends GoldenCoreType {
    @Override
    public MapCodec<? extends GoldenCore<?>> codec() {
        return RecordCodecBuilder.<SimpleGoldenCore>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimpleGoldenCore::name),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(SimpleGoldenCore::description),
                        Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(SimpleGoldenCore::skills),
                        ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).optionalFieldOf("stats", Map.of()).forGetter(SimpleGoldenCore::statModifiers),
                        PathBonusHolder.MODIFIER_CODEC.optionalFieldOf("path_bonus", Map.of()).forGetter(SimpleGoldenCore::pathBonusModifiers),
                        ProgressActionList.CODEC.optionalFieldOf("on_added", ProgressActionList.EMPTY).forGetter(SimpleGoldenCore::onAddedActions),
                        ProgressActionHolder.PROGRESS_HOLDER_CODEC.optionalFieldOf("realm_actions", SimpleGoldenCore.EMPTY_REALM_ACTIONS).forGetter(SimpleGoldenCore::realmActions),
                        AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(SimpleGoldenCore::itemTooltip),
                        RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(SimpleGoldenCore::requirements)
                ).apply(instance, SimpleGoldenCore::new)
        );
    }
}
