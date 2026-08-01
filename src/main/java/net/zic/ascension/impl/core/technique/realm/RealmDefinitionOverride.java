package net.zic.ascension.impl.core.technique.realm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinitionReference;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RealmDefinitionOverride {

    private final boolean nameOverride;
    private final Component name;
    private final boolean progressOverride;
    private final double progress;
    private final boolean tribulationReferenceOverride;
    private final TribulationDefinitionReference tribulation;

    public static Codec<RealmDefinitionOverride> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(
                            obj->Optional.ofNullable(obj.hasNameOverride()?obj.name : null)),
                    Codec.DOUBLE.optionalFieldOf("progress").forGetter(
                            obj->Optional.ofNullable(obj.hasProgressOverride()?obj.getProgress():null)
                    ),
                    TribulationDefinitionReference.CODEC.optionalFieldOf("tribulation").forGetter(
                            obj->Optional.ofNullable(obj.hasTribulationOverride()?obj.tribulation:null)
                        )
            ).apply(instance, RealmDefinitionOverride::new));

    public RealmDefinitionOverride(@NotNull Optional<Component> name, @NotNull Optional<Double> progress, @NotNull Optional<TribulationDefinitionReference> tribulation) {
        nameOverride = name.isPresent();
        this.name = name.orElse(Component.empty());

        progressOverride = progress.isPresent();
        this.progress = progress.orElse(0.0);

        tribulationReferenceOverride = tribulation.isPresent();
        this.tribulation = tribulation.orElse(null);
    }

    public boolean hasNameOverride(){
        return nameOverride;
    }
    public boolean hasProgressOverride(){
        return progressOverride;
    }
    public boolean hasTribulationOverride(){
        return tribulationReferenceOverride;
    }



    public Component getName(){
        return name;
    }
    public double getProgress(){
        return progress;
    }
    public TribulationDefinition getTribulation(RegistryAccess access){
        return tribulation.resolve(access);
    }
}
