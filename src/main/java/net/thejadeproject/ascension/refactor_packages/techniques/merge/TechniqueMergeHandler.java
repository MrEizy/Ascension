package net.thejadeproject.ascension.refactor_packages.techniques.merge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.techniques.ModTechniques;

import java.util.*;

public class TechniqueMergeHandler {

    // Maps {currentTechnique, candidateTechnique} → merged result.
    // Only sequential generative-cycle pairs are valid; history is NOT used.
    private static final Map<Set<ResourceLocation>, ResourceLocation> MERGE_TABLE = new HashMap<>();

    // Maps result technique ID → element count, used for realm gating.
    private static final Map<ResourceLocation, Integer> RESULT_ELEMENT_COUNT = new HashMap<>();

    static {
        // 2-element merges (adjacent generative-cycle singles)
        MERGE_TABLE.put(Set.of(
            ModTechniques.LIVER_WOOD_TECHNIQUE.getId(),
            ModTechniques.HEART_FIRE_TECHNIQUE.getId()
        ), ModTechniques.WOOD_FIRE_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.HEART_FIRE_TECHNIQUE.getId(),
            ModTechniques.SPLEEN_EARTH_TECHNIQUE.getId()
        ), ModTechniques.FIRE_EARTH_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.SPLEEN_EARTH_TECHNIQUE.getId(),
            ModTechniques.LUNG_METAL_TECHNIQUE.getId()
        ), ModTechniques.EARTH_METAL_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.LUNG_METAL_TECHNIQUE.getId(),
            ModTechniques.KIDNEY_WATER_TECHNIQUE.getId()
        ), ModTechniques.METAL_WATER_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.KIDNEY_WATER_TECHNIQUE.getId(),
            ModTechniques.LIVER_WOOD_TECHNIQUE.getId()
        ), ModTechniques.WATER_WOOD_BODY_TECHNIQUE.getId());

        // 3-element merges: 2-element combined + next adjacent single
        MERGE_TABLE.put(Set.of(
            ModTechniques.WOOD_FIRE_BODY_TECHNIQUE.getId(),
            ModTechniques.SPLEEN_EARTH_TECHNIQUE.getId()
        ), ModTechniques.WOOD_FIRE_EARTH_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.FIRE_EARTH_BODY_TECHNIQUE.getId(),
            ModTechniques.LUNG_METAL_TECHNIQUE.getId()
        ), ModTechniques.FIRE_EARTH_METAL_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.EARTH_METAL_BODY_TECHNIQUE.getId(),
            ModTechniques.KIDNEY_WATER_TECHNIQUE.getId()
        ), ModTechniques.EARTH_METAL_WATER_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.METAL_WATER_BODY_TECHNIQUE.getId(),
            ModTechniques.LIVER_WOOD_TECHNIQUE.getId()
        ), ModTechniques.METAL_WATER_WOOD_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.WATER_WOOD_BODY_TECHNIQUE.getId(),
            ModTechniques.HEART_FIRE_TECHNIQUE.getId()
        ), ModTechniques.WATER_WOOD_FIRE_BODY_TECHNIQUE.getId());

        // 4-element merges: 3-element combined + next adjacent single
        MERGE_TABLE.put(Set.of(
            ModTechniques.WOOD_FIRE_EARTH_BODY_TECHNIQUE.getId(),
            ModTechniques.LUNG_METAL_TECHNIQUE.getId()
        ), ModTechniques.WOOD_FIRE_EARTH_METAL_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.FIRE_EARTH_METAL_BODY_TECHNIQUE.getId(),
            ModTechniques.KIDNEY_WATER_TECHNIQUE.getId()
        ), ModTechniques.FIRE_EARTH_METAL_WATER_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.EARTH_METAL_WATER_BODY_TECHNIQUE.getId(),
            ModTechniques.LIVER_WOOD_TECHNIQUE.getId()
        ), ModTechniques.EARTH_METAL_WATER_WOOD_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.METAL_WATER_WOOD_BODY_TECHNIQUE.getId(),
            ModTechniques.HEART_FIRE_TECHNIQUE.getId()
        ), ModTechniques.METAL_WATER_WOOD_FIRE_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.WATER_WOOD_FIRE_BODY_TECHNIQUE.getId(),
            ModTechniques.SPLEEN_EARTH_TECHNIQUE.getId()
        ), ModTechniques.WATER_WOOD_FIRE_EARTH_BODY_TECHNIQUE.getId());

        // 5-element merges: 4-element combined + next adjacent single
        MERGE_TABLE.put(Set.of(
            ModTechniques.WOOD_FIRE_EARTH_METAL_BODY_TECHNIQUE.getId(),
            ModTechniques.KIDNEY_WATER_TECHNIQUE.getId()
        ), ModTechniques.FIVE_ELEMENT_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.FIRE_EARTH_METAL_WATER_BODY_TECHNIQUE.getId(),
            ModTechniques.LIVER_WOOD_TECHNIQUE.getId()
        ), ModTechniques.FIVE_ELEMENT_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.EARTH_METAL_WATER_WOOD_BODY_TECHNIQUE.getId(),
            ModTechniques.HEART_FIRE_TECHNIQUE.getId()
        ), ModTechniques.FIVE_ELEMENT_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.METAL_WATER_WOOD_FIRE_BODY_TECHNIQUE.getId(),
            ModTechniques.SPLEEN_EARTH_TECHNIQUE.getId()
        ), ModTechniques.FIVE_ELEMENT_BODY_TECHNIQUE.getId());

        MERGE_TABLE.put(Set.of(
            ModTechniques.WATER_WOOD_FIRE_EARTH_BODY_TECHNIQUE.getId(),
            ModTechniques.LUNG_METAL_TECHNIQUE.getId()
        ), ModTechniques.FIVE_ELEMENT_BODY_TECHNIQUE.getId());

        // Element counts for realm gating
        RESULT_ELEMENT_COUNT.put(ModTechniques.WOOD_FIRE_BODY_TECHNIQUE.getId(), 2);
        RESULT_ELEMENT_COUNT.put(ModTechniques.FIRE_EARTH_BODY_TECHNIQUE.getId(), 2);
        RESULT_ELEMENT_COUNT.put(ModTechniques.EARTH_METAL_BODY_TECHNIQUE.getId(), 2);
        RESULT_ELEMENT_COUNT.put(ModTechniques.METAL_WATER_BODY_TECHNIQUE.getId(), 2);
        RESULT_ELEMENT_COUNT.put(ModTechniques.WATER_WOOD_BODY_TECHNIQUE.getId(), 2);

        RESULT_ELEMENT_COUNT.put(ModTechniques.WOOD_FIRE_EARTH_BODY_TECHNIQUE.getId(), 3);
        RESULT_ELEMENT_COUNT.put(ModTechniques.FIRE_EARTH_METAL_BODY_TECHNIQUE.getId(), 3);
        RESULT_ELEMENT_COUNT.put(ModTechniques.EARTH_METAL_WATER_BODY_TECHNIQUE.getId(), 3);
        RESULT_ELEMENT_COUNT.put(ModTechniques.METAL_WATER_WOOD_BODY_TECHNIQUE.getId(), 3);
        RESULT_ELEMENT_COUNT.put(ModTechniques.WATER_WOOD_FIRE_BODY_TECHNIQUE.getId(), 3);

        RESULT_ELEMENT_COUNT.put(ModTechniques.WOOD_FIRE_EARTH_METAL_BODY_TECHNIQUE.getId(), 4);
        RESULT_ELEMENT_COUNT.put(ModTechniques.FIRE_EARTH_METAL_WATER_BODY_TECHNIQUE.getId(), 4);
        RESULT_ELEMENT_COUNT.put(ModTechniques.EARTH_METAL_WATER_WOOD_BODY_TECHNIQUE.getId(), 4);
        RESULT_ELEMENT_COUNT.put(ModTechniques.METAL_WATER_WOOD_FIRE_BODY_TECHNIQUE.getId(), 4);
        RESULT_ELEMENT_COUNT.put(ModTechniques.WATER_WOOD_FIRE_EARTH_BODY_TECHNIQUE.getId(), 4);

        RESULT_ELEMENT_COUNT.put(ModTechniques.FIVE_ELEMENT_BODY_TECHNIQUE.getId(), 5);
    }

    /**
     * Given a candidate technique the player is about to use, returns the merged technique ID
     * if the player's current technique forms a valid generative-cycle pair with it AND the
     * player meets the realm requirement. Returns null otherwise.
     */
    public static ResourceLocation findMergeResult(IEntityData entityData, ResourceLocation candidateId) {
        ResourceLocation result = findMatchingResult(entityData, candidateId);
        if (result == null) return null;
        if (!meetsRealmRequirement(entityData, candidateId, result)) return null;
        return result;
    }

    /**
     * Returns the element count of the merge result if a combo exists but realm blocks it,
     * or -1 if no combo exists at all. Used for error messaging.
     */
    public static int findBlockedComboSize(IEntityData entityData, ResourceLocation candidateId) {
        ResourceLocation result = findMatchingResult(entityData, candidateId);
        if (result == null) return -1;
        if (meetsRealmRequirement(entityData, candidateId, result)) return -1;
        return RESULT_ELEMENT_COUNT.getOrDefault(result, -1);
    }

    private static ResourceLocation findMatchingResult(IEntityData entityData, ResourceLocation candidateId) {
        ResourceLocation currentTechnique = getCurrentTechnique(entityData);
        if (currentTechnique == null) return null;
        return MERGE_TABLE.get(Set.of(currentTechnique, candidateId));
    }

    private static boolean meetsRealmRequirement(IEntityData entityData, ResourceLocation candidateId, ResourceLocation resultId) {
        var technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(candidateId);
        if (technique == null) return false;
        PathData pathData = entityData.getPathData(technique.getPath());
        int majorRealm = pathData != null ? pathData.getMajorRealm() : 0;
        int minorRealm = pathData != null ? pathData.getMinorRealm() : 0;
        int elementCount = RESULT_ELEMENT_COUNT.getOrDefault(resultId, 0);
        return switch (elementCount) {
            case 2 -> majorRealm >= 2;
            case 3 -> majorRealm >= 3;
            case 4 -> majorRealm >= 4;
            case 5 -> majorRealm >= 4 && minorRealm >= 9;
            default -> false;
        };
    }

    private static ResourceLocation getCurrentTechnique(IEntityData entityData) {
        PathData pathData = entityData.getPathData(ModPaths.BODY.getId());
        if (pathData == null) return null;
        return pathData.getLastUsedTechnique();
    }

    public static void applyMerge(ServerPlayer player, ResourceLocation mergedTechniqueId) {
        player.getData(ModAttachments.ENTITY_DATA).setTechnique(mergedTechniqueId);
    }

    public static ResourceLocation getMergeResult(Set<ResourceLocation> techniqueIds) {
        return MERGE_TABLE.get(techniqueIds);
    }
}
