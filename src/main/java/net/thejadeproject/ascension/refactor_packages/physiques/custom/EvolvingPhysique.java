package net.thejadeproject.ascension.refactor_packages.physiques.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.toast.ShowAscensionToast;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

import java.util.HashSet;
import java.util.Set;

import static net.thejadeproject.ascension.clients.toast.AscensionToastInterface.DEFAULT_BACKGROUND;

public class EvolvingPhysique extends GenericPhysique {

    private final Set<ResourceLocation> possibleEvolutions = new HashSet<>();

    public EvolvingPhysique(Component title) {
        super(title);
    }

    public EvolvingPhysique addEvolution(ResourceLocation evolvesInto) {
        possibleEvolutions.add(evolvesInto);
        return this;
    }

    public boolean canEvolveInto(ResourceLocation evolvesInto) {
        return possibleEvolutions.contains(evolvesInto);
    }

    public Set<ResourceLocation> getPossibleEvolutions() {
        return possibleEvolutions;
    }

    public boolean tryEvolveInto(ServerPlayer player, IEntityData entityData, ResourceLocation evolvesInto) {
        if (player == null) return false;
        if (entityData == null) return false;
        if (evolvesInto == null) return false;

        if (entityData.getPhysique() != this) return false;

        if (!possibleEvolutions.contains(evolvesInto)) return false;

        if (!meetsEvolutionRequirements(player, entityData, evolvesInto)) return false;

        IPhysique newPhysique = AscensionRegistries.Physiques.PHSIQUES_REGISTRY.get(evolvesInto);
        if (newPhysique == null) return false;

        boolean changed = entityData.setPhysique(evolvesInto);

        if (changed) {
            Component physiqueName = newPhysique.getDisplayTitle();

            if (player.connection != null) {
                PacketDistributor.sendToPlayer(
                        player,
                        new ShowAscensionToast(
                                physiqueName.getString(),
                                "Physique Evolved",
                                getEvolutionToastIcon(evolvesInto),
                                DEFAULT_BACKGROUND
                        )
                );
            }
        }

        return changed;
    }

    protected ItemStack getEvolutionToastIcon(ResourceLocation evolvesInto) {
        ItemStack stack = new ItemStack(ModItems.PHYSIQUE_ESSENCE.get());
        stack.setCount(1);
        return stack;
    }

    // Override in Physique Registration to change evolution conditions
    protected boolean meetsEvolutionRequirements(ServerPlayer player, IEntityData entityData, ResourceLocation evolvesInto) {
        return true;
    }
}