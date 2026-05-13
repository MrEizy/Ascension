package net.thejadeproject.ascension.datagen.loot.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.mob_cultivation.MobCultivationData;
import net.thejadeproject.ascension.mob_cultivation.MobCultivationList;

public record MobRankLootCondition(
        Mode mode,
        String realm,
        int stage,
        String otherRealm,
        int otherStage
) implements LootItemCondition {

    public enum Mode {
        AT_LEAST,
        EXACT,
        BETWEEN
    }

    public static final MapCodec<MobRankLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.xmap(Mode::valueOf, Mode::name)
                            .fieldOf("mode")
                            .forGetter(MobRankLootCondition::mode),
                    Codec.STRING.fieldOf("realm").forGetter(MobRankLootCondition::realm),
                    Codec.INT.optionalFieldOf("stage", 1).forGetter(MobRankLootCondition::stage),
                    Codec.STRING.optionalFieldOf("other_realm", "").forGetter(MobRankLootCondition::otherRealm),
                    Codec.INT.optionalFieldOf("other_stage", 3).forGetter(MobRankLootCondition::otherStage)
            ).apply(instance, MobRankLootCondition::new)
    );

    @Override
    public LootItemConditionType getType() {
        return ModLootConditions.MOB_RANK.get();
    }

    @Override
    public boolean test(LootContext context) {
        if (!(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity living)) {
            return false;
        }

        MobCultivationData data = living.getData(ModAttachments.MOB_RANK);
        if (data == null || !data.isInitialized()) {
            return false;
        }

        String mobRealm = data.getRealmId();
        int mobStage = data.getStage();

        return switch (mode) {
            case AT_LEAST -> MobCultivationList.isAtLeast(mobRealm, mobStage, realm, stage);
            case EXACT -> MobCultivationList.isExact(mobRealm, mobStage, realm, stage);
            case BETWEEN -> MobCultivationList.isBetweenInclusive(mobRealm, mobStage, realm, stage, otherRealm, otherStage);
        };
    }

    // checks exact
    public static Builder exact(String realmId, int stage) {
        return () -> new MobRankLootCondition(Mode.EXACT, realmId, stage, "", 3);
    }

    // checks inclusive range
    public static Builder between(String minRealmId, int minStage, String maxRealmId, int maxStage) {
        return () -> new MobRankLootCondition(Mode.BETWEEN, minRealmId, minStage, maxRealmId, maxStage);
    }

    // checks min
    public static Builder atLeast(String realmId, int stage) {
        return () -> new MobRankLootCondition(Mode.AT_LEAST, realmId, stage, "", 3);
    }
}