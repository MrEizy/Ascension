package net.zic.ascension.common.appearance;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineInstance;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkills;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class PlayerAppearanceRules {
    private static ResourceManager cachedManager;
    private static List<Rule> rules = List.of();

    private PlayerAppearanceRules() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 10 != 0) {
            return;
        }
        List<Rule> available = rules(event.getServer().getResourceManager());
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
            List<Identifier> appearances = new ArrayList<>();
            Set<String> claimedGroups = new HashSet<>();
            if (source != null) {
                for (Rule rule : available) {
                    if (!rule.group().isEmpty() && claimedGroups.contains(rule.group())) {
                        continue;
                    }
                    if (!rule.matches(source)) {
                        continue;
                    }
                    if (!rule.group().isEmpty()) {
                        claimedGroups.add(rule.group());
                    }
                    if (!appearances.contains(rule.appearance())) {
                        appearances.add(rule.appearance());
                    }
                }
            }
            PlayerAppearanceState state = player.getData(AscensionAttachments.PLAYER_APPEARANCES);
            if (state.update(appearances)) {
                player.syncData(AscensionAttachments.PLAYER_APPEARANCES);
            }
        }
    }

    private static synchronized List<Rule> rules(ResourceManager manager) {
        if (manager == cachedManager) {
            return rules;
        }
        cachedManager = manager;
        List<Rule> loaded = new ArrayList<>();
        Map<Identifier, net.minecraft.server.packs.resources.Resource> resources = manager.listResources(
                "ascension/player_appearances", id -> id.getPath().endsWith(".json")
        );
        for (var entry : resources.entrySet()) {
            try (var reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
                JsonObject source = object.getAsJsonObject("source");
                String type = source.get("type").getAsString();
                if (!type.equals("bloodline") && !type.equals("skill")) {
                    throw new IllegalArgumentException("Unsupported source type " + type);
                }
                int minimum = source.has("min_purity") ? source.get("min_purity").getAsInt() : 1;
                int maximum = source.has("max_purity") ? source.get("max_purity").getAsInt() : Integer.MAX_VALUE;
                if (minimum < 0 || maximum < minimum) {
                    throw new IllegalArgumentException("Invalid purity range");
                }
                loaded.add(new Rule(
                        Identifier.parse(object.get("appearance").getAsString()),
                        type,
                        Identifier.parse(source.get("id").getAsString()),
                        minimum,
                        maximum,
                        !source.has("active") || source.get("active").getAsBoolean(),
                        object.has("group") ? object.get("group").getAsString() : "",
                        object.has("priority") ? object.get("priority").getAsInt() : 0,
                        entry.getKey().toString()
                ));
            } catch (Exception exception) {
                AscensionCraft.LOGGER.warn("Unable to load player appearance rule {}", entry.getKey(), exception);
            }
        }
        loaded.sort(Comparator.comparingInt(Rule::priority).reversed().thenComparing(Rule::key));
        rules = List.copyOf(loaded);
        return rules;
    }

    private record Rule(Identifier appearance, String type, Identifier sourceId, int minPurity, int maxPurity,
                        boolean requireActive, String group, int priority, String key) {
        boolean matches(OriginSource source) {
            if (type.equals("bloodline")) {
                BloodlineInstance<?> instance = AscensionOriginSourceHelper.getBloodline(source, sourceId);
                return instance != null && instance.getData().getPurity() >= minPurity
                        && instance.getData().getPurity() <= maxPurity;
            }
            SkillInstance<?> instance = AscensionOriginSourceHelper.getSkill(source, sourceId);
            return instance != null && (!requireActive || !ToggleableSkills.isToggleable(instance)
                    || ToggleableSkills.isEnabled(instance));
        }
    }
}
