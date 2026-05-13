package net.thejadeproject.ascension.common.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.thejadeproject.ascension.mob_cultivation.MobCultivationList;
import net.thejadeproject.ascension.mob_cultivation.util.MobCultivationCommandHelper;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public final class MobCultivationCommand {
    private MobCultivationCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("mob")

                .then(Commands.literal("summon")
                        .then(Commands.argument("entity", ResourceLocationArgument.id())
                                .suggests((context, builder) -> {
                                    BuiltInRegistries.ENTITY_TYPE.keySet().forEach(id ->
                                            builder.suggest(id.toString())
                                    );
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("realm", word())
                                        .suggests((context, builder) -> {
                                            MobCultivationList.getRealmIds().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("stage", integer(1, 3))
                                                .executes(ctx -> summon(
                                                        ctx.getSource(),
                                                        ResourceLocationArgument.getId(ctx, "entity"),
                                                        getString(ctx, "realm"),
                                                        getInteger(ctx, "stage"),
                                                        ctx.getSource().getPosition()
                                                ))
                                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                                        .executes(ctx -> summon(
                                                                ctx.getSource(),
                                                                ResourceLocationArgument.getId(ctx, "entity"),
                                                                getString(ctx, "realm"),
                                                                getInteger(ctx, "stage"),
                                                                Vec3Argument.getVec3(ctx, "pos")
                                                        ))
                                                )
                                        )
                                )
                        )
                )

                .then(Commands.literal("set")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.argument("realm", word())
                                        .suggests((context, builder) -> {
                                            MobCultivationList.getRealmIds().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("stage", integer(1, 3))
                                                .executes(ctx -> set(
                                                        ctx.getSource(),
                                                        EntityArgument.getEntity(ctx, "target"),
                                                        getString(ctx, "realm"),
                                                        getInteger(ctx, "stage")
                                                ))
                                        )
                                )
                        )
                )

                .then(Commands.literal("stats")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(ctx -> getStats(
                                        ctx.getSource(),
                                        EntityArgument.getEntity(ctx, "target")
                                ))
                        )
                );
    }

    private static int summon(
            CommandSourceStack source,
            ResourceLocation entityId,
            String realmId,
            int stage,
            Vec3 pos
    ) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);

        if (type == null) {
            source.sendFailure(Component.literal("Unknown entity type: " + entityId));
            return 0;
        }

        Entity entity = type.create(source.getLevel());

        if (!(entity instanceof LivingEntity living)) {
            source.sendFailure(Component.literal("Entity is not a LivingEntity: " + entityId));
            return 0;
        }

        living.moveTo(pos.x, pos.y, pos.z, source.getRotation().y, 0.0F);

        if (!MobCultivationCommandHelper.applyCultivation(living, realmId, stage)) {
            source.sendFailure(Component.literal("Failed to apply mob cultivation."));
            return 0;
        }

        source.getLevel().addFreshEntity(living);

        source.sendSuccess(
                () -> Component.literal("Summoned " + entityId + " with " + realmId + " stage " + stage),
                true
        );

        return 1;
    }

    private static int set(CommandSourceStack source, Entity entity, String realmId, int stage) {
        if (!(entity instanceof LivingEntity living)) {
            source.sendFailure(Component.literal("Target is not a LivingEntity."));
            return 0;
        }

        if (!MobCultivationCommandHelper.applyCultivation(living, realmId, stage)) {
            source.sendFailure(Component.literal("Failed to apply mob cultivation."));
            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("Set " + living.getName().getString() + " to " + realmId + " stage " + stage),
                true
        );

        return 1;
    }

    private static int getStats(CommandSourceStack source, Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            source.sendFailure(Component.literal("Target is not a LivingEntity."));
            return 0;
        }

        source.sendSuccess(
                () -> MobCultivationCommandHelper.getStatsMessage(living),
                false
        );

        return 1;
    }
}