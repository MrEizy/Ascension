package net.zic.ascension.api.core.path.interactions;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.path.Path;

import java.util.*;


public class PathInteractionHolder {
    private static final Identifier KEY= Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"path_interaction_listener");

    private static class ServerListener extends SimplePreparableReloadListener<Void>  {


        @Override
        protected Void prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            return null;
        }

        @Override
        protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if(server == null)return;

            AscensionCraft.getPathInteractionHolder().clear();

            Collection<Path> paths = CoreRegistries.PATH_REGISTRY.get(server.registryAccess()).stream().toList();

            for(Path path : paths){
                path.registerInteractions(AscensionCraft.getPathInteractionHolder());
            }
        }
    }
    //interactions where the path is on the receiving end of the interaction
    private static final HashMap<Identifier, Map<Identifier,PathInteraction>> targetPathInteractions  = new HashMap<>();


    //interactions where th path is giving the interaction
    private static final HashMap<Identifier,Map<Identifier,PathInteraction>> sourcePathInteractions = new HashMap<>();


    public PathInteractionHolder(){
        NeoForge.EVENT_BUS.addListener(this::addServerListener);
        NeoForge.EVENT_BUS.addListener(this::addClientListener);
    }

    private void clear(){
        targetPathInteractions.clear();
        sourcePathInteractions.clear();
    }
    /**
     * @param path the path we are getting interactions for
     * @return all interactions where path is the target of the interaction
     */
    public Collection<PathInteraction> getTargetInteractionsForPath(Identifier path){
        return targetPathInteractions.containsKey(path) ? targetPathInteractions.get(path).values() : List.of();
    }

    /**
     * @param path the path we are getting interactions for
     * @return all interactions where path is the source of the interaction
     */
    public Collection<PathInteraction> getSourceInteractionsForPath(Identifier path){
        return sourcePathInteractions.containsKey(path) ? sourcePathInteractions.get(path).values() : List.of();
    }

    /**
     *
     * @param path the target path
     * @param paths a set of potential source paths
     * @return all valid interactions between target path and source in paths
     */
    public Collection<PathInteraction> getTargetInteractionsFrom(Identifier path,Set<Identifier> paths){
        Map<Identifier, PathInteraction> map = targetPathInteractions.get(path);
        if (map == null) return List.of();

        return map.entrySet().stream()
                .filter(e -> paths.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }


    //TODO add logging for self referencing and duplicates
    public static void registerInteraction(PathInteraction interaction){
        Identifier sourcePath = interaction.pathA();
        Identifier targetPath = interaction.pathB();
        if(sourcePath.equals(targetPath)) {
            AscensionCraft.LOGGER.warn("path cannot interact with itself for path {}",sourcePath);
            return;
        };
        sourcePathInteractions.computeIfAbsent(sourcePath,key->new HashMap<>());

        if(sourcePathInteractions.containsKey(sourcePath) && sourcePathInteractions.get(sourcePath).containsKey(targetPath)){
            AscensionCraft.LOGGER.warn("duplicate path interaction between {} and {} found",sourcePath,targetPath);
            return;
        }

        targetPathInteractions.computeIfAbsent(targetPath,key->new HashMap<>());

        targetPathInteractions .get(targetPath).put(sourcePath,interaction);
        sourcePathInteractions.get(sourcePath).put(targetPath,interaction);

    }

    @SubscribeEvent
    public void addServerListener(AddServerReloadListenersEvent event){

        event.addListener(KEY,new ServerListener());
        event.addDependency(CoreRegistries.PATH_REGISTRY.key().identifier(),KEY);
    }
    @SubscribeEvent
    public void addClientListener(AddClientReloadListenersEvent event){

    }


}
