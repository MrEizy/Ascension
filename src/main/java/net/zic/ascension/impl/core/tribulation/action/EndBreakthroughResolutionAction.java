package net.zic.ascension.impl.core.tribulation.action;

import net.zic.ascension.impl.core.path.simple.SimplePathData;
import net.zic.ascension.impl.core.path.simple.SimplePath;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.tribulation.TribulationResolutionAction;
import net.zic.ascension.api.ascension.core.tribulation.TribulationResolutionContext;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationResolutionActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationResolutionActionTypes;

//ends a paths breakthrough attempt without progressing, allowing it to be retried
public record EndBreakthroughResolutionAction(Identifier path) implements TribulationResolutionAction {
    public static final MapCodec<EndBreakthroughResolutionAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("path").forGetter(EndBreakthroughResolutionAction::path)
    ).apply(instance, EndBreakthroughResolutionAction::new));

    @Override
    public TribulationResolutionActionType getType() {
        return AscensionTribulationResolutionActionTypes.END_BREAKTHROUGH.get();
    }

    @Override
    public void resolve(TribulationResolutionContext context) {
        OriginSource source = context.targetSource();
        if (source == null || !AscensionOriginSourceHelper.hasPath(source, path)) return;
        PathInstance<?> pathInstance = AscensionOriginSourceHelper.getPathInstance(source, path);
        PathInstance<SimplePathData> simplePathInstance = pathInstance == null ? null : pathInstance.as(SimplePath.class);
        if (simplePathInstance == null) return;
        SimplePath simplePath = (SimplePath) simplePathInstance.getPath();

        simplePath.endBreakthrough(simplePathInstance, context.instance().getId());
        AscensionOriginSourceHelper.markPathDirty(source, path);
    }
}
