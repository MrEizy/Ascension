package net.zic.ascension.impl.core.technique.realm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class MajorRealmDefinitionOverride {

    private final boolean nameOverride;
    private final Component name;

    private final Map<Integer,RealmDefinitionOverride> realmOverrides;
    public static Codec<MajorRealmDefinitionOverride> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(
                            obj->Optional.ofNullable(obj.hasNameOverride()?obj.name : null)),
                    Codec.unboundedMap(
                            Codec.STRING.xmap(
                                    Integer::parseInt,Object::toString
                            ),
                            RealmDefinitionOverride.CODEC
                            ).optionalFieldOf("realm_overrides",Map.of())
                            .forGetter(MajorRealmDefinitionOverride::getRealmOverrides)
            ).apply(instance, MajorRealmDefinitionOverride::new));

    public MajorRealmDefinitionOverride(@NotNull Optional<Component> name, Map<Integer, RealmDefinitionOverride> realmOverrides) {
        nameOverride = name.isPresent();
        this.name = name.orElse(Component.empty());
        this.realmOverrides = realmOverrides;
    }


    public boolean hasNameOverride(){
        return nameOverride;
    }
    public boolean hasRealmOverride(int realm){
        return realmOverrides.containsKey(realm);
    }


    public Component getName(){
        return name;
    }

    public RealmDefinitionOverride getRealmOverride(int realm){
        return realmOverrides.get(realm);
    }
    public Map<Integer,RealmDefinitionOverride> getRealmOverrides(){
        return realmOverrides;
    }
}
