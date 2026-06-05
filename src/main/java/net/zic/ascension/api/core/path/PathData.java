package net.zic.ascension.api.core.path;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.technique.TechniqueData;

import java.util.ArrayList;
import java.util.Collection;

//TODO decide if i want to pass source or entity wrapper
public interface PathData {
    //──Getters────────────────────────────────────────────────────────
    Identifier getPath();

    int getMajorRealm();
    int getMinorRealm();

    int getMaxMinorRealm(int majorRealm, RegistryAccess access);
    int getMaxMajorRealm(RegistryAccess access);

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

    void setCurrentTechnique(Identifier technique,OriginSource source, RegistryAccess access);
    void setCurrentTechnique(Identifier technique,TechniqueData data,OriginSource source,RegistryAccess access);

    //──Logic────────────────────────────────────────────────────────
    void onMajorRealmUp(OriginSource source, RegistryAccess access);
    void onMajorRealmDown(OriginSource source, RegistryAccess access);
    void onMinorRealmUp(OriginSource source, RegistryAccess access);
    void onMinorRealmDown(OriginSource source, RegistryAccess access);

    //takes in a potential realm change, and breaks it down into individual steps
    //TODO write default implementation
    void handlerRealmChange(OriginSource source,int newMajorRealm,int newMinorRealm, RegistryAccess access);

    //caches the current state then simulates applying it
    void simulateProgression(OriginSource source, RegistryAccess access);

    //removes it from a specific source but should still save its data (mainly used when transferring path data)
    void removeFromSource(OriginSource source,RegistryAccess access);

    //for implementations like foundation that might have non exposed behaviour
    void applyToEntity(LivingEntity entity);
    void removeFromEntity(LivingEntity entity);
    //──Save Data────────────────────────────────────────────────────────

    void write(ValueOutput output);
    //──Network────────────────────────────────────────────────────────
    void encode(ByteBuf buf);
}
