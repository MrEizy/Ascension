package net.thejadeproject.ascension.refactor_packages.bloodlines;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.attributes.AttributeValueContainer;
import net.thejadeproject.ascension.refactor_packages.bloodlines.generic.GenericBloodline;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ModifierOperation;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainerModifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BloodlineSuppressionHandler {

    public static final double SUPPRESSION_STRENGTH = -0.15;

    private static final String MODIFIER_NAMESPACE = AscensionCraft.MOD_ID;
    private static final String MODIFIER_PREFIX = "bloodline_suppression_by_";

    private BloodlineSuppressionHandler() {}



    // ─────────────────────────────── tick ─────────────────────────────────────

    public static void tick(ServerPlayer player, ServerLevel level) {
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;
        IEntityData playerData = player.getData(ModAttachments.ENTITY_DATA);

        IBloodline playerBloodline = playerData.getBloodline();
        if (playerBloodline == null) return;

        ResourceLocation playerBloodlineKey = AscensionRegistries.Bloodlines.BLOODLINE_REGISTRY.getKey(playerBloodline);
        int playerTier = getTier(playerBloodline);

        List<ServerPlayer> nearbyPlayers = level.getPlayers(other ->
                other != player && other.distanceTo(player) <= BloodlineSuppression.SUPPRESSION_RANGE);

        for (ServerPlayer other : nearbyPlayers) {
            if (!other.hasData(ModAttachments.ENTITY_DATA)) continue;
            IEntityData otherData = other.getData(ModAttachments.ENTITY_DATA);

            IBloodline otherBloodline = otherData.getBloodline();
            if (otherBloodline == null) continue;

            ResourceLocation otherBloodlineKey = AscensionRegistries.Bloodlines.BLOODLINE_REGISTRY.getKey(otherBloodline);
            int otherTier = getTier(otherBloodline);

            // Check if player suppresses other
            if (playerBloodline instanceof GenericBloodline gb && gb.getSuppression().suppresses(otherBloodlineKey, otherTier)) {
                applySuppression(playerBloodlineKey, otherData);
            }

            // Check if other suppresses player
            if (otherBloodline instanceof GenericBloodline gb && gb.getSuppression().suppresses(playerBloodlineKey, playerTier)) {
                applySuppression(otherBloodlineKey, playerData);
            }
        }

        removeStaleSuppressions(player, playerData, playerBloodlineKey, nearbyPlayers, level);
    }

    // ─────────────────────────────── apply ────────────────────────────────────

    private static void applySuppression(ResourceLocation suppressorKey, IEntityData victimData) {
        ResourceLocation modifierKey = suppressionModifierKey(suppressorKey);

        for (var attributeHolder : suppressedAttributes()) {
            AttributeValueContainer container = victimData.getAscensionAttributeHolder().getAttribute(attributeHolder);
            if (container == null) continue;

            ValueContainerModifier existing = container.getAllModifiers()
                    .stream()
                    .filter(m -> m.getIdentifier().equals(modifierKey))
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                container.addModifier(new ValueContainerModifier(
                        SUPPRESSION_STRENGTH,
                        ModifierOperation.MULTIPLY_BASE,
                        modifierKey,
                        suppressionGroupKey(suppressorKey)
                ));
            }
        }
        victimData.getAscensionAttributeHolder().updateAttributes(victimData);
    }


    public static void removeSuppression(ResourceLocation suppressorKey, IEntityData victimData) {
        ResourceLocation modifierKey = suppressionModifierKey(suppressorKey);
        for (var attributeHolder : suppressedAttributes()) {
            AttributeValueContainer container = victimData.getAscensionAttributeHolder().getAttribute(attributeHolder);
            if (container == null) continue;
            container.removeModifier(modifierKey);
        }
        victimData.getAscensionAttributeHolder().updateAttributes(victimData);
    }

    // ─────────────────────────────── cleanup ──────────────────────────────────

    /**
     * Removes suppression modifiers from this player that were applied by suppressors
     * who are no longer nearby.
     */

    private static void removeStaleSuppressions(ServerPlayer player, IEntityData playerData, ResourceLocation playerBloodlineKey,
                                                List<ServerPlayer> nearbyPlayers, ServerLevel level) {
        Set<ResourceLocation> activeSupressorKeys = new HashSet<>();
        for (ServerPlayer other : nearbyPlayers) {
            if (!other.hasData(ModAttachments.ENTITY_DATA)) continue;
            IEntityData otherData = other.getData(ModAttachments.ENTITY_DATA);
            IBloodline otherBloodline = otherData.getBloodline();
            if (!(otherBloodline instanceof GenericBloodline gb)) continue;

            int playerTier = getTier(playerData.getBloodline());
            ResourceLocation otherKey = AscensionRegistries.Bloodlines.BLOODLINE_REGISTRY.getKey(otherBloodline);

            if (gb.getSuppression().suppresses(playerBloodlineKey, playerTier)) {
                activeSupressorKeys.add(otherKey);
            }
        }

        AttributeValueContainer sampleContainer = playerData.getAscensionAttributeHolder()
                .getAttribute(Attributes.MAX_HEALTH);
        if (sampleContainer == null) return;

        sampleContainer.getAllModifiers().stream()
                .filter(m -> m.getIdentifier().getPath().startsWith(MODIFIER_PREFIX))
                .map(m -> m.getIdentifier())
                .filter(id -> !activeSupressorKeys.contains(suppressorKeyFromModifier(id)))
                .toList()
                .forEach(staleId -> removeSuppression(suppressorKeyFromModifier(staleId), playerData));
    }

    // ─────────────────────────────── util ─────────────────────────────────────

    private static int getTier(IBloodline bloodline) {
        if (bloodline instanceof GenericBloodline gb) return gb.getSuppression().getTier();
        return BloodlineSuppression.NO_TIER;
    }

    private static ResourceLocation suppressionModifierKey(ResourceLocation suppressorKey) {
        return ResourceLocation.fromNamespaceAndPath(
                MODIFIER_NAMESPACE,
                MODIFIER_PREFIX + suppressorKey.getNamespace() + "_" + suppressorKey.getPath()
        );
    }

    private static ResourceLocation suppressionGroupKey(ResourceLocation suppressorKey) {
        return ResourceLocation.fromNamespaceAndPath(
                MODIFIER_NAMESPACE,
                "suppression_group_" + suppressorKey.getPath()
        );
    }


    /** Reconstructs the suppressor ResourceLocation from a modifier key. */
    private static ResourceLocation suppressorKeyFromModifier(ResourceLocation modifierKey) {
        // modifier path = "bloodline_suppression_by_{namespace}_{path}"
        String path = modifierKey.getPath().substring(MODIFIER_PREFIX.length());
        int sep = path.indexOf('_');
        if (sep < 0) return modifierKey;
        String namespace = path.substring(0, sep);
        String id = path.substring(sep + 1);
        return ResourceLocation.fromNamespaceAndPath(namespace, id);
    }

    /** The attributes suppression is applied to. Add or remove as needed. */
    private static java.util.List<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>> suppressedAttributes() {
        return List.of(
                Attributes.MAX_HEALTH,
                Attributes.ATTACK_DAMAGE,
                Attributes.MOVEMENT_SPEED
        );
    }
}
