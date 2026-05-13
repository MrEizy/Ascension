package net.thejadeproject.ascension.common.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.attributes.AscensionAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.SyncEntityForm;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncAttributeHolder;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.attributes.SyncCurrentHealth;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.HeldSkill;
import net.thejadeproject.ascension.util.ModAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ResetAscensionCommand {



    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("reset")

                        .then(Commands.literal("all")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ResetAscensionCommand::resetAll)
                                )
                        )

                        .then(Commands.literal("attributes")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ResetAscensionCommand::resetAttributesOnly)
                                )
                        )

                        .then(Commands.literal("paths")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ResetAscensionCommand::resetAllPaths)
                                )
                        )

                        .then(Commands.literal("path")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("path", ResourceLocationArgument.id())
                                                .suggests(ResetAscensionCommand::suggestPaths)
                                                .executes(ResetAscensionCommand::resetSpecificPath)
                                        )
                                )
                        )

                        .then(Commands.literal("skills")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ResetAscensionCommand::resetAllSkills)
                                )
                        )

                        .then(Commands.literal("techniques")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ResetAscensionCommand::resetAllTechniques)
                                )
                        )

                        .then(Commands.literal("technique")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("path", ResourceLocationArgument.id())
                                                .suggests(ResetAscensionCommand::suggestPaths)
                                                .executes(ResetAscensionCommand::resetSpecificTechnique)
                                        )
                                )
                        )

                        .then(Commands.literal("physique")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ResetAscensionCommand::resetPhysique)
                                )
        );
    }

    // handlers
    private static int resetAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        int successCount = 0;

        for (ServerPlayer player : players) {
            IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

            resetPlayerSkills(entityData);
            resetPlayerPaths(entityData);
            resetPlayerPhysique(entityData);
            resetPlayerAscensionAttributes(player, entityData);

            syncAllForms(player, entityData);

            player.sendSystemMessage(Component.translatable("command.ascension.reset.all"));
            successCount++;
        }

        sendCount(context, "command.ascension.reset.count.all", successCount);
        return successCount;
    }

    private static int resetAttributesOnly(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        int successCount = 0;

        for (ServerPlayer player : players) {
            IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

            resetPlayerAscensionAttributes(player, entityData);

            player.sendSystemMessage(Component.translatable("command.ascension.reset.attributes"));
            successCount++;
        }

        sendCount(context, "command.ascension.reset.count.attributes", successCount);
        return successCount;
    }

    private static int resetAllPaths(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        int successCount = 0;

        for (ServerPlayer player : players) {
            IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

            resetPlayerPaths(entityData);
            syncAllForms(player, entityData);

            player.sendSystemMessage(Component.translatable("command.ascension.reset.paths"));
            successCount++;
        }

        sendCount(context, "command.ascension.reset.count.paths", successCount);
        return successCount;
    }

    private static int resetSpecificPath(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");

        ResourceLocation pathId = ResourceLocationArgument.getId(context, "path");
        String inputPath = pathId.toString();

        if (!isValidPath(pathId)) {
            context.getSource().sendFailure(
                    Component.translatable("command.ascension.reset.error.invalid_path", inputPath)
            );            return 0;
        }

        int successCount = 0;

        for (ServerPlayer player : players) {
            IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

            if (!entityData.hasPath(pathId)) {
                context.getSource().sendFailure(
                        Component.translatable(
                                "command.ascension.reset.error.player_no_path",
                                player.getDisplayName(),
                                pathId.toString()
                        )
                );
                continue;
            }

            entityData.removePath(pathId);
            syncAllForms(player, entityData);

            player.sendSystemMessage(
                    Component.translatable("command.ascension.reset.path", pathId.toString())
            );
            successCount++;
        }

        sendCount(context, "command.ascension.reset.count.path", pathId.toString(), successCount);
        return successCount;
    }

    private static int resetAllSkills(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        int successCount = 0;

        for (ServerPlayer player : players) {
            IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

            resetPlayerSkills(entityData);
            syncAllForms(player, entityData);

            player.sendSystemMessage(Component.translatable("command.ascension.reset.skills"));
            successCount++;
        }

        sendCount(context, "command.ascension.reset.count.skills", successCount);
        return successCount;
    }

    private static int resetAllTechniques(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        int successCount = 0;

        for (ServerPlayer player : players) {
            IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

            resetPlayerTechniques(entityData);
            syncAllForms(player, entityData);

            player.sendSystemMessage(Component.translatable("command.ascension.reset.techniques"));
            successCount++;
        }

        sendCount(context, "command.ascension.reset.count.techniques", successCount);
        return successCount;
    }

    private static int resetSpecificTechnique(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        ResourceLocation pathId = ResourceLocationArgument.getId(context, "path");
        String inputPath = pathId.toString();

        if (!isValidPath(pathId)) {
            context.getSource().sendFailure(
                    Component.translatable("command.ascension.reset.error.invalid_path", String.valueOf(inputPath))
            );
            return 0;
        }

        int successCount = 0;

        for (ServerPlayer player : players) {
            IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

            if (entityData.getTechnique(pathId) == null) {
                context.getSource().sendFailure(
                        Component.translatable(
                                "command.ascension.reset.error.player_no_technique",
                                player.getDisplayName(),
                                pathId.toString()
                        )
                );
                continue;
            }

            entityData.removeTechnique(pathId);
            syncAllForms(player, entityData);

            player.sendSystemMessage(
                    Component.translatable("command.ascension.reset.technique", pathId.toString())
            );
            successCount++;
        }

        sendCount(context, "command.ascension.reset.count.technique", pathId.toString(), successCount);
        return successCount;
    }

    private static int resetPhysique(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        int successCount = 0;

        for (ServerPlayer player : players) {
            IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

            resetPlayerPhysique(entityData);
            syncAllForms(player, entityData);

            player.sendSystemMessage(Component.translatable("command.ascension.reset.physique"));
            successCount++;
        }

        sendCount(context, "command.ascension.reset.count.physique", successCount);
        return successCount;
    }

    // helpers
    private static void resetPlayerSkills(IEntityData entityData) {
        for (IEntityFormData formData : new ArrayList<>(entityData.getFormData())) {
            ResourceLocation formId = formData.getEntityFormId();

            List<ResourceLocation> skillsToRemove = new ArrayList<>();

            for (HeldSkill heldSkill : formData.getHeldSkills().getSkills()) {
                skillsToRemove.add(heldSkill.getKey());
            }

            for (ResourceLocation skillId : skillsToRemove) {
                entityData.removeSkill(skillId, formId);
            }
        }
    }

    private static void resetPlayerTechniques(IEntityData entityData) {
        List<ResourceLocation> pathsToReset = new ArrayList<>();

        for (PathData pathData : entityData.getAllPathData()) {
            if (pathData != null && entityData.getTechnique(pathData.getPath()) != null) {
                pathsToReset.add(pathData.getPath());
            }
        }

        for (ResourceLocation pathId : pathsToReset) {
            entityData.removeTechnique(pathId);
        }
    }

    private static void resetPlayerPaths(IEntityData entityData) {
        List<ResourceLocation> pathsToRemove = new ArrayList<>();

        for (PathData pathData : entityData.getAllPathData()) {
            if (pathData == null) continue;

            ResourceLocation pathId = pathData.getPath();
            if (pathId == null) continue;

            if (!isValidPath(pathId)) {
                continue;
            }

            pathsToRemove.add(pathId);
        }

        for (ResourceLocation pathId : pathsToRemove) {
            entityData.removePath(pathId);
        }
    }

    private static void resetPlayerPhysique(IEntityData entityData) {
        if (entityData.getPhysique() != null) {
            entityData.removePhysique();
        }
    }

    private static void resetPlayerAscensionAttributes(ServerPlayer player, IEntityData entityData) {
        if (entityData.getActiveFormData() == null) {
            player.sendSystemMessage(Component.translatable("command.ascension.reset.attributes.no_form"));
            return;
        }

        resetPlayerAttributeShell(player);

        AscensionAttributeHolder freshHolder = new AscensionAttributeHolder(player);
        entityData.setAscensionAttributeHolder(player, freshHolder);

        entityData.addDefaultAttributes(player);
        entityData.getAscensionAttributeHolder().updateAttributes(entityData);

        double maxHealth = entityData.getAttributeValue(Attributes.MAX_HEALTH);
        if (maxHealth > 0) {
            entityData.setHealth(maxHealth);
        } else {
            player.setHealth(player.getMaxHealth());
        }

        player.setAbsorptionAmount(0.0F);

        syncAscensionAttributes(player, entityData);
    }

    private static void resetPlayerAttributeShell(ServerPlayer player) {
        resetAttributeShell(player, Attributes.MAX_HEALTH);
        resetAttributeShell(player, Attributes.JUMP_STRENGTH);
        resetAttributeShell(player, Attributes.KNOCKBACK_RESISTANCE);
        resetAttributeShell(player, Attributes.MOVEMENT_SPEED);
        resetAttributeShell(player, Attributes.FLYING_SPEED);
        resetAttributeShell(player, Attributes.ATTACK_DAMAGE);
        resetAttributeShell(player, Attributes.ATTACK_KNOCKBACK);
        resetAttributeShell(player, Attributes.ATTACK_SPEED);
        resetAttributeShell(player, Attributes.ARMOR);
        resetAttributeShell(player, Attributes.ARMOR_TOUGHNESS);
        resetAttributeShell(player, Attributes.LUCK);
        resetAttributeShell(player, Attributes.MAX_ABSORPTION);
        resetAttributeShell(player, Attributes.WATER_MOVEMENT_EFFICIENCY);
        resetAttributeShell(player, Attributes.STEP_HEIGHT);
        resetAttributeShell(player, Attributes.MINING_EFFICIENCY);
        resetAttributeShell(player, Attributes.SAFE_FALL_DISTANCE);

        resetAttributeShell(player, ModAttributes.MAX_QI);
        resetAttributeShell(player, ModAttributes.QI_REGEN_RATE);
    }

    private static void resetAttributeShell(ServerPlayer player, Holder<Attribute> attributeHolder) {
        AttributeInstance instance = player.getAttribute(attributeHolder);
        if (instance == null) return;

        for (AttributeModifier modifier : new ArrayList<>(instance.getModifiers())) {
            instance.removeModifier(modifier);
        }

        instance.setBaseValue(getPlayerAttributeDefault(attributeHolder));
    }

    private static double getPlayerAttributeDefault(Holder<Attribute> attributeHolder) {
        try {
            return DefaultAttributes.getSupplier(EntityType.PLAYER).getBaseValue(attributeHolder);
        } catch (Exception ignored) {
            return attributeHolder.value().getDefaultValue();
        }
    }

    // sync
    private static void syncAllForms(ServerPlayer player, IEntityData entityData) {
        for (IEntityFormData formData : entityData.getFormData()) {
            PacketDistributor.sendToPlayer(player, new SyncEntityForm(formData));
        }
    }

    private static void syncAscensionAttributes(ServerPlayer player, IEntityData entityData) {
        PacketDistributor.sendToPlayer(
                player,
                new SyncAttributeHolder(entityData.getAscensionAttributeHolder())
        );

        PacketDistributor.sendToPlayer(
                player,
                new SyncCurrentHealth(entityData.getHealth())
        );
    }

    private static CompletableFuture<Suggestions> suggestPaths(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        List<String> suggestions = new ArrayList<>();

        AscensionRegistries.Paths.PATHS_REGISTRY.keySet()
                .forEach(loc -> suggestions.add(loc.toString()));

        return SharedSuggestionProvider.suggest(suggestions, builder);
    }


    private static boolean isValidPath(ResourceLocation pathId) {
        if (pathId == null) return false;

        return AscensionRegistries.Paths.PATHS_REGISTRY.containsKey(
                ResourceKey.create(AscensionRegistries.Paths.PATHS_REGISTRY_KEY, pathId)
        );
    }

    private static void sendCount(CommandContext<CommandSourceStack> context, String translationKey, Object... args) {
        context.getSource().sendSuccess(() ->
                Component.translatable(translationKey, args), true);
    }
}