package net.zic.ascension.datapack.bloodline.purity.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.bloodline.purity.PurityChangeAction;
import net.zic.ascension.api.datapack.bloodline.purity.PurityChangeActionType;
import net.zic.ascension.core.bloodline.purity.action.GiveBaseStatsAction;
import net.zic.ascension.core.bloodline.purity.action.GiveSkillsAction;
import net.zic.zenithlib.value_containers.ValueContainer;

public class GiveSkillsActionType extends PurityChangeActionType {
    @Override
    public MapCodec<? extends PurityChangeAction> codec() {
        return RecordCodecBuilder.<GiveSkillsAction>mapCodec(instance ->
                instance.group(
                        Identifier.CODEC.listOf().fieldOf("skills").forGetter(GiveSkillsAction::skills)
                ).apply(instance, GiveSkillsAction::new)
        );
    }
}
