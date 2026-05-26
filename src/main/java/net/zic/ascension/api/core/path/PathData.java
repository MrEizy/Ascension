package net.zic.ascension.api.core.path;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.OriginSource;
import net.zic.ascension.api.core.technique.TechniqueData;

import java.util.ArrayList;
import java.util.Collection;

//TODO decide if i want to pass source or entity wrapper
public interface PathData {
    //──Getters────────────────────────────────────────────────────────
    int getMajorRealm();
    int getMinorRealm();

    int getMaxMinorRealm(int majorRealm);
    int getMaxMajorRealm();

    double getProgress();

    boolean isCultivating();

    boolean isBreakingThrough();

    Identifier getCurrentTechnique();

    TechniqueData getCurrentTechniqueData();

    ArrayList<Identifier> getTechniqueHistory();

    Collection<Identifier> getUniqueTechniques();

    boolean hasCultivatedTechnique(Identifier technique);

    Identifier getTechniqueForRealm(int majorRealm);

    TechniqueData getTechniqueData(Identifier technique);

    Collection<Integer> getCultivatedRealms(Identifier technique);

    //──Setters────────────────────────────────────────────────────────

    void setMajorRealm(int majorRealm);
    void setMinorRealm(int minorRealm);

    void setProgress(double progress);

    void setCurrentTechnique(Identifier technique);

    void setTechniqueData(Identifier technique,TechniqueData data);

    //──Logic────────────────────────────────────────────────────────
    void onMajorRealmUp(OriginSource source);
    void onMajorRealmDown(OriginSource source);
    void onMinorRealmUp(OriginSource source);
    void onMinorRealmDown(OriginSource source);

    //takes in a potential realm change, and breaks it down into individual steps
    //TODO write default implementation
    void handlerRealmChange(OriginSource source,int newMajorRealm,int newMinorRealm);

    //caches the current state then simulates applying it
    void simulateProgression(OriginSource source);
    //──Save Data────────────────────────────────────────────────────────

    void write(ValueOutput output);
    //──Network────────────────────────────────────────────────────────
    void encode(RegistryFriendlyByteBuf buf);
}
