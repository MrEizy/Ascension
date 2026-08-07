package net.zic.ascension.impl.core.path.realms;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.path.realm.CompositeRealmDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinitionReference;
import net.zic.ascension.impl.core.path.RealmDefinition;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record MajorRealmDefinition(
        Component name,
        List<MinorRealmDefinition> minorRealms
        ) implements CompositeRealmDefinition {
    @Override
    public Component getCompositeRealmName(int realm) {
        return Component.empty().append(name).append(" (").append(getMinorRealmName(realm)).append(")");
    }

    @Override
    public Component getMinorRealmName(int realm) {
        if(realm>=minorRealms.size()){
            return Component.empty().append(minorRealms.getLast().name()).append("+"+(realm-(minorRealms.size()-1)));
        }
        return minorRealms.get(realm).name();
    }

    @Override
    public TribulationDefinitionReference getRealmTribulation(int realm) {
        return realm >= minorRealms.size() ? null : minorRealms.get(realm).getTribulation();
    }

    public BreakthroughBehaviour getBreakthroughBehaviour(int realm){
        return realm >= minorRealms.size() ? BreakthroughBehaviour.INSTANT : minorRealms.get(realm).breakthroughBehaviour();
    }
    @Override
    public double getMaxProgress(int realm) {
        return realm >= minorRealms.size() ? minorRealms.getLast().getMaxProgression()*(realm-(minorRealms.size()-2)) : minorRealms.get(realm).getMaxProgression();
    }

    @Override
    public MinorRealmDefinition realmDefinition(int realm) {
        return realm >= minorRealms.size() ?  null : minorRealms.get(realm);
    }

    @Override
    public int getMaxRealm() {
        return minorRealms.size()-1;
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public @Nullable TribulationDefinitionReference getTribulation() {
        return null;
    }

    @Override
    public double getMaxProgression() {
        return 0;
    }
    public static Codec<MajorRealmDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ComponentSerialization.CODEC.fieldOf("name").forGetter(MajorRealmDefinition::name),
                    MinorRealmDefinition.CODEC
                            .listOf()
                            .fieldOf("minor_realms")
                            .forGetter(MajorRealmDefinition::minorRealms)
            ).apply(instance,MajorRealmDefinition::new));
}
