package net.zic.ascension.client.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.zenithlib.classification.ZenithClassification;
import net.zic.zenithlib.classification.ZenithClassifications;

import java.util.Optional;

/**
 * Provides category/rank data for transfer items whose concrete registry entry is stored on the stack.
 */
public final class AscensionTransferItemClassifications {
    public static final Identifier ID = AscensionCraft.prefix("registry_transfer_items");

    public static final Identifier BLOODLINE_ESSENCE = AscensionCraft.prefix("bloodline_essence");
    public static final Identifier PHYSIQUE_ESSENCE = AscensionCraft.prefix("physique_essence");
    public static final Identifier TECHNIQUE_MANUAL = AscensionCraft.prefix("technique_manual");

    private static boolean registered;

    private AscensionTransferItemClassifications() {}

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        ZenithClassifications.registerProvider(ID, AscensionTransferItemClassifications::resolve);
    }

    private static Optional<ZenithClassification> resolve(ItemStack stack, Identifier itemId) {
        if (!stack.has(AscensionComponents.REGISTRY_ID_HOLDER)) {
            return Optional.empty();
        }

        Identifier registryId = stack.get(AscensionComponents.REGISTRY_ID_HOLDER);
        if (registryId == null) {
            return Optional.empty();
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return Optional.empty();
        }

        RegistryAccess access = level.registryAccess();

        if (stack.getItem() == ModItems.BLOODLINE_ESSENCE.get()) {
            Bloodline bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY, registryId, access);
            return bloodline == null
                    ? Optional.empty()
                    : classification(BLOODLINE_ESSENCE, bloodline.itemTooltip());
        }

        if (stack.getItem() == ModItems.PHYSIQUE_ESSENCE.get()) {
            Physique physique = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, registryId, access);
            return physique == null
                    ? Optional.empty()
                    : classification(PHYSIQUE_ESSENCE, physique.itemTooltip());
        }

        if (stack.getItem() == ModItems.TECHNIQUE_MANUAL.get()) {
            Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, registryId, access);
            return technique == null
                    ? Optional.empty()
                    : classification(TECHNIQUE_MANUAL, technique.itemTooltip());
        }

        return Optional.empty();
    }

    private static Optional<ZenithClassification> classification(
            Identifier categoryId,
            Optional<AscensionItemTooltipDefinition> tooltip
    ) {
        Optional<ZenithClassification.Category> category = Optional.ofNullable(
                ZenithClassifications.categoriesView().get(categoryId)
        );
        Optional<Identifier> rankId = tooltip.flatMap(AscensionItemTooltipDefinition::rank);
        Optional<ZenithClassification.Rank> rank = rankId.flatMap(id -> Optional.ofNullable(
                ZenithClassifications.ranksView().get(id)
        ));

        if (category.isEmpty() && rank.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ZenithClassification(
                Optional.of(categoryId),
                category,
                rankId,
                rank
        ));
    }
}
