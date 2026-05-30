package net.zic.ascension.api.core.bloodline.purity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.ProgressDirection;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.core.physique.SimplePhysique;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 *  while not required is part of the bloodline interface to make handling bloodline logic easier
 *
 *  TODO implement + Codec
 *
 *
 *  TODO NOTE does not directly store action and condition instead stores their keys
 *  TODO add a bloodline purity change event
 *
 */
public class PurityChangeHandler {

    private final UUID handlerId = UUID.randomUUID();

    public static final Codec<List<PurityChangeListener>> CODEC = PurityChangeListener.CODEC.listOf();

    public record PurityChangeListener(Identifier condition,Identifier action){
        //pray it aint null
        public boolean test(OriginSource source, Bloodline bloodline, BloodlineData data, int purity, ProgressDirection direction){
            return CoreRegistries.PURITY_CHANGE_ACTION_CONDITION_REGISTRY.get(source.getRegistryAccess()).getValue(condition)
                    .test(source,bloodline,data,purity,direction);
        }
        public void run(UUID handlerId,OriginSource source, Bloodline bloodline, BloodlineData data, int purity, ProgressDirection direction){
            CoreRegistries.PURITY_CHANGE_ACTION_REGISTRY.get(source.getRegistryAccess()).getValue(action)
                    .run(handlerId,source,bloodline,data,purity,direction);
        }

        public static final Codec<PurityChangeListener> CODEC = RecordCodecBuilder.create(instance->
                instance.group(
                        Identifier.CODEC.fieldOf("condition").forGetter(PurityChangeListener::condition),
                        Identifier.CODEC.fieldOf("action").forGetter(PurityChangeListener::action)
                        )
                .apply(instance, PurityChangeListener::new)
        );
    }

    public PurityChangeHandler(List<PurityChangeListener> listeners){
        this.listeners.addAll(listeners);
    }

    private final ArrayList<PurityChangeListener> listeners = new ArrayList<>();
    public void runPurityUp(OriginSource source, Bloodline bloodline, BloodlineData data){
        runListeners(source,bloodline,data,data.getPurity(),ProgressDirection.UP);
    }
    public void runPurityDown(OriginSource source, Bloodline bloodline, BloodlineData data){
        runListeners(source,bloodline,data,data.getPurity()+1,ProgressDirection.DOWN);
    }
    public void runListeners(OriginSource source, Bloodline bloodline, BloodlineData data, int purity, ProgressDirection direction){
        for(PurityChangeListener listener : listeners){
            if(listener.test(source,bloodline,data,purity,direction)) listener.run(handlerId,source,bloodline,data,purity,direction);
        }
    }
    public List<PurityChangeListener> getListeners(){
        return List.copyOf(listeners);
    }
}
