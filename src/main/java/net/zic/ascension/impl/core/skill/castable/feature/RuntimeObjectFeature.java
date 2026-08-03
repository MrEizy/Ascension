package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.zic.ascension.api.ascension.core.runtime.OwnerBoundConstructDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.runtime.object.AnchorNetworks;
import net.zic.ascension.impl.runtime.object.OwnerBoundConstructs;
import net.zic.ascension.impl.runtime.object.AreaFields;
import net.zic.ascension.impl.datapack.skill.AscensionSkillExecutionFeatureTypes;

public record RuntimeObjectFeature(
        Kind kind,
        Action action,
        Identifier definition,
        ScaledValue amount
) implements SkillExecutionFeature {
    public static final MapCodec<RuntimeObjectFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Kind.CODEC.fieldOf("kind").forGetter(RuntimeObjectFeature::kind),
            Action.CODEC.optionalFieldOf("action", Action.SPAWN).forGetter(RuntimeObjectFeature::action),
            Identifier.CODEC.fieldOf("definition").forGetter(RuntimeObjectFeature::definition),
            ScaledValue.CODEC.codec().optionalFieldOf("amount", ScaledValue.constant(0.0D))
                    .forGetter(RuntimeObjectFeature::amount)
    ).apply(instance, RuntimeObjectFeature::new));

    @Override
    public CodecType<SkillExecutionFeature> getType() {
        return AscensionSkillExecutionFeatureTypes.RUNTIME_OBJECT.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        switch (action) {
            case SPAWN -> spawn(context);
            case REMOVE -> remove(context);
            case RESTORE -> restore(context);
        }
    }

    private void spawn(SkillExecutionContext context) {
        switch (kind) {
            case AREA_FIELD -> AreaFields.spawn(context, definition, context.position());
            case ANCHOR_NETWORK -> AnchorNetworks.spawn(context, definition, context.position());
            case CONSTRUCT -> OwnerBoundConstructs.spawn(context, definition);
        }
    }

    private void remove(SkillExecutionContext context) {
        switch (kind) {
            case AREA_FIELD -> AreaFields.removeOwned(context.level(), context.caster().getUUID(), definition);
            case ANCHOR_NETWORK -> AnchorNetworks.removeOwned(context.level(), context.caster().getUUID(), definition);
            case CONSTRUCT -> OwnerBoundConstructs.removeOwned(context.level(), context.caster().getUUID(), definition);
        }
    }

    private void restore(SkillExecutionContext context) {
        if (kind != Kind.CONSTRUCT) {
            return;
        }
        double resolved = amount.resolve(context.scaledValueContext());
        if (!Double.isFinite(resolved) || resolved == 0.0D) {
            return;
        }
        for (OwnerBoundConstructDefinition.View construct : OwnerBoundConstructs.findOwned(
                context.caster().getUUID(),
                definition
        )) {
            OwnerBoundConstructs.modifyStability(construct.runtimeId(), resolved);
        }
    }

    public enum Kind implements StringRepresentable {
        AREA_FIELD("area_field"),
        ANCHOR_NETWORK("anchor_network"),
        CONSTRUCT("construct");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);
        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum Action implements StringRepresentable {
        SPAWN("spawn"),
        REMOVE("remove"),
        RESTORE("restore");

        public static final Codec<Action> CODEC = StringRepresentable.fromEnum(Action::values);
        private final String name;

        Action(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
