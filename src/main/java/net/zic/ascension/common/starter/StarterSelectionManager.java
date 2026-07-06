package net.zic.ascension.common.starter;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.entity.SimpleAscensionEntityData;
import net.zic.ascension.network.OpenStarterSelectionPacket;
import net.zic.zenithlib.common.ZenithAttachments;

import java.util.List;
import java.util.Map;

public final class StarterSelectionManager {
    public static final Identifier HUMAN = AscensionCraft.prefix("human_bloodline");
    public static final Identifier BARBARIAN = AscensionCraft.prefix("barbarian_bloodline");
    public static final Identifier BEASTKIN = AscensionCraft.prefix("beastkin_bloodline");
    public static final Identifier SCALEKIN = AscensionCraft.prefix("scalekin_bloodline");
    public static final Identifier FROSTKIN = AscensionCraft.prefix("frostkin_bloodline");

    public static final List<Identifier> STARTER_BLOODLINES = List.of(
            HUMAN,
            BARBARIAN,
            BEASTKIN,
            SCALEKIN,
            FROSTKIN
    );

    private static final Map<Identifier, List<Identifier>> STARTER_PHYSIQUES = Map.of(
            HUMAN, List.of(
                    AscensionCraft.prefix("twin_root_physique"),
                    AscensionCraft.prefix("drifting_cloud_physique"),
                    AscensionCraft.prefix("iron_blossom_physique")
            ),
            BARBARIAN, List.of(
                    AscensionCraft.prefix("warblood_physique"),
                    AscensionCraft.prefix("mountain_breaker_physique"),
                    AscensionCraft.prefix("berserkers_fang_physique")
            ),
            BEASTKIN, List.of(
                    AscensionCraft.prefix("feral_moon_physique"),
                    AscensionCraft.prefix("primal_fang_physique"),
                    AscensionCraft.prefix("thousand_beast_root_physique")
            ),
            SCALEKIN, List.of(
                    AscensionCraft.prefix("jiao_scale_physique"),
                    AscensionCraft.prefix("tidecaller_root_physique"),
                    AscensionCraft.prefix("abyss_drifter_physique")
            ),
            FROSTKIN, List.of(
                    AscensionCraft.prefix("rime_bone_physique"),
                    AscensionCraft.prefix("winter_root_physique"),
                    AscensionCraft.prefix("blizzard_spirit_physique")
            )
    );

    private StarterSelectionManager() {
    }

    public static void openIfIncomplete(ServerPlayer player) {
        SimpleAscensionEntityData data = player.getData(AscensionAttachments.SIMPLE_ENTITY_DATA);
        if (data.isStarterSelectionComplete()) {
            return;
        }

        prepareCurrentStage(data);
        player.syncData(AscensionAttachments.SIMPLE_ENTITY_DATA);
        openCurrentScreen(player, data);
    }

    public static void handleChoice(
            ServerPlayer player,
            StarterSelectionStage stage,
            Identifier selectedId
    ) {
        if (selectedId == null || stage == null || !stage.isSelectable()) {
            return;
        }

        SimpleAscensionEntityData data = player.getData(AscensionAttachments.SIMPLE_ENTITY_DATA);
        if (data.isStarterSelectionComplete()) {
            return;
        }

        prepareCurrentStage(data);

        if (stage == StarterSelectionStage.BLOODLINE) {
            handleBloodlineChoice(player, data, selectedId);
            return;
        }

        handlePhysiqueChoice(player, data, selectedId);
    }

    private static void handleBloodlineChoice(
            ServerPlayer player,
            SimpleAscensionEntityData data,
            Identifier selectedId
    ) {
        if (data.getStarterSelectionStage() != StarterSelectionStage.BLOODLINE) {
            openCurrentScreen(player, data);
            return;
        }
        if (data.getSelectedStarterBloodline() != null) {
            openCurrentScreen(player, data);
            return;
        }
        if (!data.getOfferedStarterBloodlines().contains(selectedId)) {
            return;
        }
        if (CoreRegistries.safeAccess(
                CoreRegistries.BLOODLINE_REGISTRY,
                selectedId,
                player.registryAccess()
        ) == null) {
            return;
        }

        data.setSelectedStarterBloodline(selectedId);
        data.setOfferedStarterPhysiques(physiquesFor(selectedId));
        data.setStarterSelectionStage(StarterSelectionStage.PHYSIQUE);

        player.syncData(AscensionAttachments.SIMPLE_ENTITY_DATA);
        openCurrentScreen(player, data);
    }

    private static void handlePhysiqueChoice(
            ServerPlayer player,
            SimpleAscensionEntityData data,
            Identifier selectedId
    ) {
        if (data.getStarterSelectionStage() != StarterSelectionStage.PHYSIQUE) {
            openCurrentScreen(player, data);
            return;
        }
        if (data.getSelectedStarterBloodline() == null) {
            data.setStarterSelectionStage(StarterSelectionStage.BLOODLINE);
            openCurrentScreen(player, data);
            return;
        }
        if (!data.getOfferedStarterPhysiques().contains(selectedId)) {
            return;
        }
        if (CoreRegistries.safeAccess(
                CoreRegistries.PHYSIQUE_REGISTRY,
                selectedId,
                player.registryAccess()
        ) == null) {
            return;
        }

        OriginSource source = data.getSource();
        source.setRegistryAccess(player.registryAccess());

        boolean bloodlineApplied = source.hasBloodline(data.getSelectedStarterBloodline())
                || source.addBloodline(data.getSelectedStarterBloodline());
        boolean physiqueApplied = selectedId.equals(source.getPhysique())
                || source.setPhysique(selectedId);

        if (!bloodlineApplied || !physiqueApplied) {
            openCurrentScreen(player, data);
            return;
        }

        data.setSelectedStarterPhysique(selectedId);
        data.setStarterSelectionComplete(true);
        data.setStarterSelectionStage(StarterSelectionStage.COMPLETE);

        source.updateAttributes(player.getData(ZenithAttachments.ATTRIBUTE_HOLDER));
        data.applyAllAttributeSuppressions();

        player.syncData(AscensionAttachments.SIMPLE_ENTITY_DATA);
        PacketDistributor.sendToPlayer(player, new OpenStarterSelectionPacket(
                StarterSelectionStage.COMPLETE,
                List.of(),
                data.getSelectedStarterBloodline()
        ));
    }

    private static void prepareCurrentStage(SimpleAscensionEntityData data) {
        if (data.isStarterSelectionComplete()) {
            data.setStarterSelectionStage(StarterSelectionStage.COMPLETE);
            return;
        }

        if (data.getOfferedStarterBloodlines().isEmpty()) {
            data.setOfferedStarterBloodlines(STARTER_BLOODLINES);
        }

        if (data.getSelectedStarterBloodline() == null) {
            data.setStarterSelectionStage(StarterSelectionStage.BLOODLINE);
            return;
        }

        if (data.getOfferedStarterPhysiques().isEmpty()) {
            data.setOfferedStarterPhysiques(physiquesFor(data.getSelectedStarterBloodline()));
        }

        data.setStarterSelectionStage(StarterSelectionStage.PHYSIQUE);
    }

    private static List<Identifier> physiquesFor(Identifier bloodline) {
        return STARTER_PHYSIQUES.getOrDefault(bloodline, List.of());
    }

    private static void openCurrentScreen(ServerPlayer player, SimpleAscensionEntityData data) {
        StarterSelectionStage stage = data.getStarterSelectionStage();
        if (stage == StarterSelectionStage.COMPLETE) {
            return;
        }

        List<Identifier> options = stage == StarterSelectionStage.BLOODLINE
                ? data.getOfferedStarterBloodlines()
                : data.getOfferedStarterPhysiques();

        PacketDistributor.sendToPlayer(player, new OpenStarterSelectionPacket(
                stage,
                options,
                data.getSelectedStarterBloodline()
        ));
    }
}
