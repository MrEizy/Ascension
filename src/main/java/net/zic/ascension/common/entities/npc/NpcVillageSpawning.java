package net.zic.ascension.common.entities.npc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.entities.AscEntities;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class NpcVillageSpawning {
    private static ResourceManager cachedManager;
    private static Rules cachedRules = Rules.DEFAULT;

    private NpcVillageSpawning() { }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Rules rules = rules(event.getServer().getResourceManager());
        if (event.getServer().getTickCount() % rules.intervalTicks() != 0) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.dimension().equals(Level.OVERWORLD)) continue;
            Set<BlockPos> checkedVillages = new HashSet<>();
            for (ServerPlayer player : level.players()) {
                if (player.isSpectator() || !player.isAlive()) continue;
                var nearbyPoi = level.getPoiManager().findClosest(
                        holder -> holder.is(PoiTypeTags.VILLAGE), player.blockPosition(),
                        rules.searchRadius(), PoiManager.Occupancy.ANY);
                if (nearbyPoi.isEmpty()) continue;
                BlockPos poi = nearbyPoi.get();
                StructureStart village = level.structureManager().getStructureWithPieceAt(poi, StructureTags.VILLAGE);
                if (!village.isValid()) continue;
                BlockPos villageCenter = village.getBoundingBox().getCenter();
                if (!checkedVillages.add(villageCenter)) continue;
                if (level.getRandom().nextInt(100) >= rules.spawnChancePercent()) continue;
                attemptSpawn(level, player, poi, village, villageCenter, rules);
            }
        }
    }

    private static void attemptSpawn(ServerLevel level, ServerPlayer player, BlockPos poi,
                                     StructureStart village, BlockPos villageCenter, Rules rules) {
        AABB area = new AABB(villageCenter).inflate(rules.populationRadius(), 64.0, rules.populationRadius());
        List<CultivatorNpcEntity> residents = level.getEntitiesOfClass(CultivatorNpcEntity.class, area,
                npc -> npc.isAlive() && !npc.isSummonedGuard() &&
                        (villageCenter.equals(npc.getVillageAnchor()) ||
                                (npc.getVillageAnchor() == null && npc.blockPosition().distSqr(villageCenter) <=
                                        (double) rules.populationRadius() * rules.populationRadius())));
        if (residents.size() >= rules.maxPerVillage()) return;
        List<Variant> variants = new ArrayList<>();
        int totalWeight = 0;
        for (Variant variant : List.of(
                new Variant("male_cultivator", AscEntities.MALE_CULTIVATOR.get()),
                new Variant("female_cultivator", AscEntities.FEMALE_CULTIVATOR.get()),
                new Variant("young_master", AscEntities.YOUNG_MASTER.get()),
                new Variant("sect_elder", AscEntities.SECT_ELDER.get()),
                new Variant("sect_master", AscEntities.SECT_MASTER.get()))) {
            NpcProfile.VillageSpawn settings = NpcProfile.load(level, variant.id()).villageSpawn();
            if (settings.weight() <= 0 || settings.maxPerVillage() <= 0) continue;
            long count = residents.stream().filter(npc -> npc.getNpcType().equals(variant.id())).count();
            if (count >= settings.maxPerVillage()) continue;
            variants.add(variant);
            totalWeight += settings.weight();
        }
        if (totalWeight <= 0) return;
        int roll = level.getRandom().nextInt(totalWeight);
        Variant selected = variants.getFirst();
        for (Variant variant : variants) {
            selected = variant;
            roll -= NpcProfile.load(level, variant.id()).villageSpawn().weight();
            if (roll < 0) break;
        }
        for (int attempt = 0; attempt < rules.attemptsPerSpawn(); attempt++) {
            int x = poi.getX() + level.getRandom().nextInt(rules.spawnRadius() * 2 + 1) - rules.spawnRadius();
            int z = poi.getZ() + level.getRandom().nextInt(rules.spawnRadius() * 2 + 1) - rules.spawnRadius();
            int bound = 4;
            if (x < village.getBoundingBox().minX() - bound || x > village.getBoundingBox().maxX() + bound
                    || z < village.getBoundingBox().minZ() - bound || z > village.getBoundingBox().maxZ() + bound) continue;
            if (!level.getChunkSource().hasChunk(x >> 4, z >> 4)) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (pos.getY() <= level.getMinY() + 1 || pos.getY() >= level.getMaxY() - 2) continue;
            if (pos.distSqr(player.blockPosition()) < rules.minPlayerDistance() * rules.minPlayerDistance()) continue;
            if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()
                    || !level.getFluidState(pos).isEmpty()
                    || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) continue;
            CultivatorNpcEntity npc = selected.type().create(level, EntitySpawnReason.NATURAL);
            if (npc == null) return;
            npc.snapTo(x + 0.5, y, z + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
            if (!level.noCollision(npc, npc.getBoundingBox())) continue;
            EventHooks.finalizeMobSpawn(npc, level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.NATURAL, null);
            npc.setVillageHome(pos, villageCenter, NpcProfile.load(level, selected.id()).villageSpawn().homeRadius());
            level.addFreshEntity(npc);
            return;
        }
    }

    private static synchronized Rules rules(ResourceManager manager) {
        if (manager == cachedManager) return cachedRules;
        cachedManager = manager;
        cachedRules = Rules.DEFAULT;
        try {
            var resource = manager.getResource(AscensionCraft.prefix("npcs/village_spawn_rules.json"));
            if (resource.isPresent()) {
                try (var reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    cachedRules = new Rules(Math.max(200, number(json, "interval_ticks", 1000, 72000)),
                            number(json, "spawn_chance_percent", 65, 100),
                            Math.max(1, number(json, "max_per_village", 5, 24)),
                            Math.max(16, number(json, "search_radius", 48, 128)),
                            Math.max(48, number(json, "population_radius", 96, 192)),
                            Math.max(4, number(json, "spawn_radius", 19, 48)),
                            number(json, "min_player_distance", 8, 40),
                            Math.max(1, number(json, "attempts_per_spawn", 16, 64)));
                }
            }
        } catch (Exception exception) {
            AscensionCraft.LOGGER.warn("Unable to load NPC village spawning rules", exception);
        }
        return cachedRules;
    }

    private static int number(JsonObject json, String name, int fallback, int max) {
        try {
            return json.has(name) ? Math.clamp(json.get(name).getAsInt(), 0, max) : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private record Variant(String id, EntityType<CultivatorNpcEntity> type) { }

    private record Rules(int intervalTicks, int spawnChancePercent, int maxPerVillage,
                         int searchRadius, int populationRadius, int spawnRadius,
                         int minPlayerDistance, int attemptsPerSpawn) {
        private static final Rules DEFAULT = new Rules(1000, 65, 5, 48, 96, 19, 8, 16);
    }
}
