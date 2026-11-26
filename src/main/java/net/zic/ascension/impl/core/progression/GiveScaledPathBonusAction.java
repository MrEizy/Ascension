package net.zic.ascension.impl.core.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.progression.*;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionTypes;
import net.zic.zenithlib.value_containers.typed.Modifier;

import java.util.List;
import java.util.UUID;

public record GiveScaledPathBonusAction(UUID uuid, Identifier id, Identifier category, double perStage) implements ProgressAction {
    public static final MapCodec<GiveScaledPathBonusAction> CODEC = RecordCodecBuilder.<GiveScaledPathBonusAction>mapCodec(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("id", Identifier.parse("ascension:default")).forGetter(GiveScaledPathBonusAction::id),
            Identifier.CODEC.fieldOf("category").forGetter(GiveScaledPathBonusAction::category),
            Codec.DOUBLE.validate(value -> Double.isFinite(value)
                    ? DataResult.success(value) : DataResult.error(() -> "Path bonus per stage must be finite")).fieldOf("per_stage").forGetter(GiveScaledPathBonusAction::perStage)
    ).apply(instance, (id, category, perStage) -> new GiveScaledPathBonusAction(UUID.randomUUID(), id, category, perStage)));

    @Override public UUID getUniqueId() { return uuid; }
    @Override public ProgressActionType getType() { return AscensionProgressActionTypes.GIVE_SCALED_PATH_BONUS.get(); }

    @Override
    public void run(UUID holderId, OriginSource source, Identifier contextIdentifier, Object contextData,
                    ProgressDirection direction, String salt) {
        if (!(contextData instanceof PathInstance<?> path)) return;
        int major = path.getCurrentMajorRealm();
        long stages = 0;
        if (major >= 0) {
            stages = 1L + Math.min(path.getCurrentMinorRealm(), path.getPath().getMaxMinorRealm(major));
            for (int r = 0; r < major; r++) stages += 1L + path.getPath().getMaxMinorRealm(r);
        }
        double value = stages * perStage;
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite path bonus: " + contextIdentifier);
        Identifier modifierId = ProgressionModifiers.scaledId("path_bonus", contextIdentifier, id, category, "flat");
        AscensionOriginSourceHelper.removeBonusModifier(source, category, contextIdentifier, modifierId);
        if (value != 0) AscensionOriginSourceHelper.addBonusFlatModifier(source, category, contextIdentifier, Modifier.flat(modifierId, 0, value));
    }

    @Override
    public List<ProgressActionDescription> getDescriptions(RegistryAccess access) {
        return List.of(ProgressActionDescription.numeric(Component.literal("Matching path " + category.getPath() + " (per standard stage)"),
                Component.literal(ProgressionDescriptionUtil.signedNumber(perStage)), perStage));
    }
}
