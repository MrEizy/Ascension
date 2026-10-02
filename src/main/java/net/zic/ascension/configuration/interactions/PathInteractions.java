package net.zic.ascension.configuration.interactions;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteraction;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteractionType;
import net.zic.ascension.configuration.ConfigurationRegistries;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//TODO consider setting it up to also create a version on player log in
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public abstract class PathInteractions{

    private static FrozenPathInteractions instance = null;

    public static FrozenPathInteractions getInstance(){
        return instance;
    }
    private static FrozenPathInteractions createInstance(RegistryAccess access){
        Builder builder = new Builder();
        Collection<Map.Entry<ResourceKey<List<PathInteraction>>,List<PathInteraction>>>
                interactions = ConfigurationRegistries.PATH_INTERACTION_REGISTRY.get(access).entrySet();
        for(Map.Entry<ResourceKey<List<PathInteraction>>,List<PathInteraction>> entry : interactions){
            for(PathInteraction interaction : entry.getValue()) builder.addInteraction(interaction);
        }
        return new FrozenPathInteractions(builder.sourceToTargetsMap,builder.targetToSourcesMap);
    }
    @SubscribeEvent
    public static void onServerStarting(ServerAboutToStartEvent event) {
        instance = createInstance(event.getServer().registryAccess());
        System.out.println("SERVER STARTING");
    }
    @SubscribeEvent
    public static void onServerStopped(ServerStoppingEvent event){
        instance = null;
    }
    public static class FrozenPathInteractions {
        private final Map<Identifier, Map<Identifier,PathInteraction>> sourceToTargetsMap;
        private final Map<Identifier, Map<Identifier,PathInteraction>> targetToSourcesMap;

        public FrozenPathInteractions(Map<Identifier, Map<Identifier, PathInteraction>> sourceToTargetsMap, Map<Identifier, Map<Identifier, PathInteraction>> targetToSourcesMap) {
            this.sourceToTargetsMap = Map.copyOf(sourceToTargetsMap);
            this.targetToSourcesMap = Map.copyOf(targetToSourcesMap);
        }


        public boolean hasInteraction(Identifier source, Identifier target) {
            return sourceToTargetsMap.containsKey(source) && sourceToTargetsMap.get(source).containsKey(target);
        }


        public boolean hasInteraction(Identifier source, Identifier target, PathInteractionType type) {
            return sourceToTargetsMap.containsKey(source) && sourceToTargetsMap.get(source).containsKey(target) && sourceToTargetsMap.get(source).get(target).type() == type;
        }


        public PathInteraction getInteraction(Identifier source, Identifier target) {
            return hasInteraction(source,target) ?sourceToTargetsMap.get(source).get(target) :null;
        }


        public Collection<PathInteraction> getInteractionsForSource(Identifier source) {
            return sourceToTargetsMap.containsKey(source)?sourceToTargetsMap.get(source).values() : List.of();
        }


        public Collection<PathInteraction> getInteractionsForTarget(Identifier target) {
            return targetToSourcesMap.containsKey(target)?targetToSourcesMap.get(target).values() : List.of();

        }

    }
    private static class Builder{
        final Map<Identifier, Map<Identifier,PathInteraction>> sourceToTargetsMap = new HashMap<>();
        final Map<Identifier, Map<Identifier,PathInteraction>> targetToSourcesMap = new HashMap<>();

        public void addInteraction(PathInteraction interaction){
            Map<Identifier,PathInteraction> targetMap = sourceToTargetsMap.computeIfAbsent(interaction.source(),key->new HashMap<>());
            Map<Identifier,PathInteraction> sourceMap = targetToSourcesMap.computeIfAbsent(interaction.target(),key->new HashMap<>());


            targetMap.put(interaction.target(),interaction);
            sourceMap.put(interaction.source(),interaction);

            AscensionCraft.LOGGER.debug("created path interaction : {}",interaction);
        }
    }


    public static boolean hasInteraction(Identifier source,Identifier target){
        return getInstance().hasInteraction(source,target);
    }
    public static boolean hasInteraction(Identifier source, Identifier target, PathInteractionType type){
        return getInstance().hasInteraction(source,target,type);
    }

    public static PathInteraction getInteraction(Identifier source,Identifier target){
        return getInstance().getInteraction(source,target);
    }

    public static Collection<PathInteraction> getInteractionsForSource(Identifier source){
        return getInstance().getInteractionsForSource(source);
    }
    public static Collection<PathInteraction> getInteractionsForTarget(Identifier target){
        return getInstance().getInteractionsForTarget(target);
    }
}
