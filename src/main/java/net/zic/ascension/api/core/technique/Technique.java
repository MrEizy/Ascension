package net.zic.ascension.api.core.technique;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.technique.TechniqueType;

public interface Technique {

    TechniqueType getType();

    Component getName();
    Component getDescription();

    Identifier getPath();
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

    //called when an entity that owns an origin detects the technique was changed
    void applyToEntity(LivingEntity entity, TechniqueData data);

    //called when either an entity is detached from an origin or the technique is removed from the origin
    void removeFromEntity(LivingEntity entity,TechniqueData data);


    //──Realms────────────────────────────────────────────────────────

    Component getMajorRealmName();
    Component getMinorRealmName();

    // returns the formatted name of the realm when both major and minor realm are displayed together
    Component getRealmName(int majorRealm, int minorRealm);

    //gives the default max minor realm and major realm a technique can cultivate
    int getMaxMajorRealm();
    int getMaxMinorRealm(int majorRealm);

    //gives the progress needed to progress a given realm
    double getMaxProgress(int majorRealm,int minorRealm);

    //is used for both minor and major realm breakthroughs
    boolean canBreakthrough(OriginSource source,int majorRealm,int minorRealm,double progress);

    TechniqueData newData();
    TechniqueData loadData(ValueInput input);
    TechniqueData loadData(ByteBuf buf);

}
