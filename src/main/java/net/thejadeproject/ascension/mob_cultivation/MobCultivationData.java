package net.thejadeproject.ascension.mob_cultivation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class MobCultivationData {
    public static final Codec<MobCultivationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("realm_id").forGetter(MobCultivationData::getRealmId),
            Codec.INT.fieldOf("stage").forGetter(MobCultivationData::getStage),
            Codec.BOOL.fieldOf("initialized").forGetter(MobCultivationData::isInitialized)
    ).apply(instance, MobCultivationData::new));

    private String realmId;
    private int stage;
    private boolean initialized;

    public MobCultivationData() {
        this("unranked", 0, false);
    }

    public MobCultivationData(String realmId, int stage, boolean initialized) {
        this.realmId = realmId;
        this.stage = stage;
        this.initialized = initialized;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isUnranked() {
        return "unranked".equals(realmId);
    }
}