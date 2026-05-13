package net.thejadeproject.ascension.common.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.path_data.SyncPathData;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.qi.EntityQiContainer;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.ElementalBodyPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.ElementalPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;


public class SetCultivationCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("cultivation")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument("path", ResourceLocationArgument.id())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                                AscensionRegistries.Paths.PATHS_REGISTRY.keySet(), builder))
                                        .then(Commands.argument("majorRealm", IntegerArgumentType.integer(0, 11))
                                                .then(Commands.argument("minorRealm", IntegerArgumentType.integer(0, 9))
                                                        .executes(SetCultivationCommand::setCultivationRealm)
                                                        .then(Commands.argument("progress", IntegerArgumentType.integer(0, 100))
                                                                .executes(SetCultivationCommand::setCultivationRealm)
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("get")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(SetCultivationCommand::getCultivationInfo)
                                .then(Commands.literal("physique")
                                        .executes(SetCultivationCommand::getPhysiqueInfo)
                                )
                        )
                );

    }

    private static int setCultivationRealm(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var players = EntityArgument.getPlayers(context, "target");
        ResourceLocation pathId = ResourceLocationArgument.getId(context, "path");
        int majorRealm = IntegerArgumentType.getInteger(context, "majorRealm");
        int minorRealm = IntegerArgumentType.getInteger(context, "minorRealm");

        int progressPercent = -1;
        try {
            progressPercent = IntegerArgumentType.getInteger(context, "progress");
        } catch (IllegalArgumentException e) {
            // Not provided — fine
        }

        if (!isValidPath(pathId)) {
            context.getSource().sendFailure(
                    Component.literal("Unknown path '" + pathId + "'. Use a registered path ID (e.g. ascension:body).")
            );
            return 0;
        }

        int successCount = 0;
        for (ServerPlayer player : players) {
            if (setPlayerCultivationRealm(player, pathId, majorRealm, minorRealm, progressPercent, context.getSource())) {
                successCount++;
            }
        }
        return successCount;
    }

    private static int getCultivationInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "target");
        var entityData = player.getData(ModAttachments.ENTITY_DATA);

        // Header
        context.getSource().sendSuccess(() ->
                Component.translatable("command.ascension.cultivation.info.header",
                        player.getName().getString()), false);

        // Physique display — get ID from registry key
        IPhysique physique = entityData.getPhysique();
        if (physique != null) {
            ResourceLocation physiqueId = AscensionRegistries.Physiques.PHSIQUES_REGISTRY.getKey(physique);
            if (physiqueId != null && !physiqueId.toString().equals("minecraft:none")) {
                var physiqueData = entityData.getActiveFormData().getPhysiqueData();
                Component physiqueName = (physique instanceof ElementalBodyPhysique ebp && physiqueData instanceof ElementalPhysiqueData ep)
                        ? ebp.getDisplayTitle(ep)
                        : Component.translatable("ascension.physiques." + physiqueId.getPath());
                context.getSource().sendSuccess(() ->
                        Component.translatable("command.ascension.cultivation.info.physique",
                                physiqueName), false);
            }
        }

        boolean hasAnyPath = false;

        // Iterate all registered paths — new paths automatically appear here
        for (var entry : AscensionRegistries.Paths.PATHS_REGISTRY.entrySet()) {
            ResourceLocation pathId = entry.getKey().location();
            PathData data = entityData.getPathData(pathId);

            if (data == null || data.getLastUsedTechnique() == null) continue;

            hasAnyPath = true;
            var path = entry.getValue();

            // Path name from lang (e.g., ascension.path.essence)
            Component pathName = Component.translatable(
                    "ascension.path." + pathId.getPath());

            // Realm name from the path's registered realm keys
            Component realmName = path.getMajorRealmName(data.getMajorRealm());

            // Technique name from lang (e.g., ascension.technique.basic_cultivation_technique)
            ResourceLocation techniqueId = data.getLastUsedTechnique();
            Component techniqueName = Component.translatable(
                    "ascension.technique." + techniqueId.getPath());

            // Path header: "Essence Path — Qi Condensation 1.3"
            context.getSource().sendSuccess(() ->
                    Component.translatable("command.ascension.cultivation.info.path_header",
                            pathName, realmName, data.getMajorRealm(), data.getMinorRealm()), false);

            // Technique
            context.getSource().sendSuccess(() ->
                    Component.translatable("command.ascension.cultivation.info.technique",
                            techniqueName), false);

            // Progress
            context.getSource().sendSuccess(() ->
                    Component.translatable("command.ascension.cultivation.info.progress",
                            String.format("%.2f", data.getCurrentRealmProgress())), false);

            // Status
            context.getSource().sendSuccess(() ->
                    Component.translatable(data.isCultivating()
                            ? "command.ascension.cultivation.info.cultivating.yes"
                            : "command.ascension.cultivation.info.cultivating.no"), false);
        }

        if (!hasAnyPath) {
            context.getSource().sendSuccess(() ->
                    Component.translatable("command.ascension.cultivation.info.no_paths"), false);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(player.getName().getString()).append(" — Cultivation ===\n");

        EntityQiContainer qi = entityData.getQiContainer();
        sb.append(String.format("  Qi: %.1f / %.1f (regen: %.2f/s)\n",
                qi.getCurrentQi(),
                qi.getMaxQi(),
                entityData.getAscensionAttributeHolder().getAttribute(net.thejadeproject.ascension.util.ModAttributes.QI_REGEN_RATE).getValue()
        ));

        for (ResourceLocation pathId : AscensionRegistries.Paths.PATHS_REGISTRY.keySet()) {
            PathData data = entityData.getPathData(pathId);
            if (data == null) continue;

            String pathName = AscensionRegistries.Paths.PATHS_REGISTRY.get(pathId).getDisplayTitle().getString();
            String realmStr = data.getMajorRealm() + "." + data.getMinorRealm();

            String techniqueStr = "none";
            if (data.getLastUsedTechnique() != null) {
                ITechnique tech = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(data.getLastUsedTechnique());
                techniqueStr = (tech != null) ? tech.getDisplayTitle().getString() : data.getLastUsedTechnique().toString();
            }

            sb.append("  ").append(pathName).append(": realm ").append(realmStr)
              .append(" | technique: ").append(techniqueStr).append("\n");
        }

        context.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int getPhysiqueInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "target");
        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        IPhysique physique = entityData.getPhysique();
        if (physique == null) {
            context.getSource().sendFailure(Component.literal(player.getName().getString() + " has no physique."));
            return 0;
        }

        ResourceLocation physiqueId = AscensionRegistries.Physiques.PHSIQUES_REGISTRY.getKey(physique);
        var physiqueData = entityData.getActiveFormData().getPhysiqueData();

        String name;
        if (physique instanceof ElementalBodyPhysique ebp && physiqueData instanceof ElementalPhysiqueData ep) {
            name = ebp.getDisplayTitle(ep).getString();
        } else {
            name = physique.getDisplayTitle().getString();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(player.getName().getString()).append(" — Physique ===\n");
        sb.append("  Name: ").append(name).append("\n");
        sb.append("  ID:   ").append(physiqueId != null ? physiqueId : "unknown").append("\n");

        if (physiqueData instanceof ElementalPhysiqueData ep) {
            sb.append("  Elements (").append(ep.getActiveCount()).append("/5): ");
            sb.append(ep.getActiveElements().stream()
                    .map(el -> AscensionRegistries.Paths.PATHS_REGISTRY.get(el).getDisplayTitle().getString())
                    .reduce((a, b) -> a + ", " + b).orElse("none"));
            sb.append("\n");
        }

        context.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    /** Validates against the live registry so any registered path is accepted. */
    private static boolean isValidPath(ResourceLocation path) {
        return AscensionRegistries.Paths.PATHS_REGISTRY.containsKey(
                ResourceKey.create(AscensionRegistries.Paths.PATHS_REGISTRY_KEY, path)
        );
    }

    private static boolean setPlayerCultivationRealm(ServerPlayer player, ResourceLocation pathId,
                                                     int newMajorRealm, int newMinorRealm,
                                                     int progressPercent,
                                                     CommandSourceStack source) {
        try {
            PathData data = player.getData(ModAttachments.ENTITY_DATA).getPathData(pathId);

            if (data == null) {
                source.sendFailure(Component.literal(
                        player.getName().getString() + " has no data for path " + pathId));
                return false;
            }

            // handleRealmChange is a no-op when lastUsedTechnique is null —
            // warn the operator rather than silently doing nothing.
            if (data.getLastUsedTechnique() == null) {
                source.sendFailure(Component.literal(
                        player.getName().getString() + " has no technique set on path " + pathId
                                + ". Assign a technique first before setting realm."));
                return false;
            }

            // Capture old values before mutation for feedback message
            int oldMajor = data.getMajorRealm();
            int oldMinor = data.getMinorRealm();

            data.handleRealmChange(newMajorRealm, newMinorRealm,
                    player.getData(ModAttachments.ENTITY_DATA));

            // Set progress if provided — scaled against the technique's max Qi for the
            // realm the player actually ended up at (may be bounded by technique max)
            if (progressPercent >= 0) {
                var technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY
                        .get(data.getLastUsedTechnique());
                double maxQi = technique.getMaxQiForRealm(data.getMajorRealm(), data.getMinorRealm());
                data.setCurrentRealmProgress(maxQi * (progressPercent / 100.0));
            }
            data.sync(player);
            String progressStr = (progressPercent >= 0)
                    ? String.format(" with %d%% progress", progressPercent) : "";

            String feedbackToSource = String.format(
                    "Set %s's %s cultivation to realm %d.%d (was %d.%d)%s",
                    player.getName().getString(),
                    pathId,
                    data.getMajorRealm(), data.getMinorRealm(),
                    oldMajor, oldMinor,
                    progressStr
            );
            source.sendSuccess(() -> Component.literal(feedbackToSource), true);

            if (source.getPlayer() != player) {
                String feedbackToPlayer = String.format(
                        "Your %s cultivation has been set to realm %d.%d%s",
                        pathId, data.getMajorRealm(), data.getMinorRealm(), progressStr
                );
                player.sendSystemMessage(Component.literal(feedbackToPlayer));
            }

            return true;

        } catch (Exception e) {
            source.sendFailure(Component.literal(
                    "Failed to set cultivation for " + player.getName().getString()
                            + ": " + e.getMessage()));
            return false;
        }
    }
}