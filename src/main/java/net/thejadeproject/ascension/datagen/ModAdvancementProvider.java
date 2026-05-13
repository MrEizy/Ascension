package net.thejadeproject.ascension.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.AdvancementProvider; // Correct import
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.thejadeproject.ascension.common.items.ModItems;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends AdvancementProvider {
    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new QiGeneration()));
    }

    public static class QiGeneration implements AdvancementGenerator {

        @Override
        public void generate(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer, ExistingFileHelper existingFileHelper) {
            Advancement.Builder.advancement()
                    .display(
                            ModItems.TABLET_OF_DESTRUCTION_HEAVEN,
                            Component.translatable("advancements.ascension.root.install"),
                            Component.translatable("advancements.ascension.root.welcome"),
                            ResourceLocation.fromNamespaceAndPath("ascension", "textures/gui/advancements/backgrounds/marble.png"),
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    ).addCriterion("joined_world", PlayerTrigger.TriggerInstance.tick())
                    .save(consumer, ResourceLocation.fromNamespaceAndPath("ascension", "ascension/root"), existingFileHelper);
        }
    }
}