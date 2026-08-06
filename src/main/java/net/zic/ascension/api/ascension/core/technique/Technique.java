package net.zic.ascension.api.ascension.core.technique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;

public interface Technique {

    TechniqueType getType();

    Component getName(@Nullable TechniqueData techniqueData);
    Component getDescription(@Nullable TechniqueData techniqueData);

    Identifier getPath();

    /**
     * A toggle for techniques to opt out of cultivation from the cultivation util pretty much
     */
    default boolean allowsCultivationProgress() {
        return true;
    }
    /**
     * called when the technique is added to an origin source
     * @param source the origin source it is being added to
     * @param data the data for this technique
     */
    void onAdded(OriginSource source, TechniqueData data);

    /**
     * Called when the technique is removed from a source
     * @param source the source it is removed from
     * @param data the data of this technique
     */
    void onRemoved(OriginSource source, TechniqueData data);



    /**
     * a milestone realm is used when removing a technique,
     * when removed we decrease the major realm until we either hit a different technique or a milestone realm
     * @return a Collection of milestone major realms
     */
    Collection<Integer> getMilestoneRealms();

    /**
     * a technique family refers to if 2 techniques are compatible or not
     * when trying to learn a technique ALL previous techniques must share at least 1 common family
     * @return a Collection of families this technique has
     */
    Collection<String> getTechniqueFamilies();
    //──Realms────────────────────────────────────────────────────────

    Component getMajorRealmName(int majorRealm,@Nullable TechniqueData techniqueData, RegistryAccess registryAccess);
    Component getMinorRealmName(int majorRealm,int minorRealm,@Nullable TechniqueData techniqueData, RegistryAccess registryAccess);

    // returns the formatted name of the realm when both major and minor realm are displayed together
    Component getRealmName(int majorRealm, int minorRealm,@Nullable TechniqueData techniqueData, RegistryAccess registryAccess);

    //gives the default max minor realm and major realm a technique can cultivate
    int getMaxMajorRealm(@Nullable TechniqueData techniqueData, RegistryAccess registryAccess);
    int getMaxMinorRealm(int majorRealm,@Nullable TechniqueData techniqueData, RegistryAccess registryAccess);

    //the minimum realm the user needs to be at to learn
    int getMinMajorRealm(RegistryAccess registryAccess);

    //gives the progress needed to progress a given realm
    double getMaxProgress(int majorRealm,int minorRealm,@Nullable TechniqueData techniqueData, RegistryAccess registryAccess);

    TribulationDefinition getTribulation(int majorRealm,int minorRealm,RegistryAccess access);
    //is used for both minor and major realm breakthroughs
    boolean tryBreakthrough(LivingEntity entity, OriginSource source, int majorRealm, int minorRealm, double progress, @Nullable TechniqueData techniqueData);


    void onRealmUp(OriginSource source, TechniqueData techniqueData);
    void onRealmDown(OriginSource source, TechniqueData techniqueData);
    TechniqueData newData();
    TechniqueData loadData(ValueInput input);
    TechniqueData loadData(ByteBuf buf);

    default Optional<AscensionItemTooltipDefinition> itemTooltip() {
        return Optional.empty();
    }



}
