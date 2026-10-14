package net.zic.ascension.common.command.commands.mob;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.configuration.mob_traits.MobTraitReference;

import net.zic.ascension.mob_cultivation.oliver_rewrite.MobConfigurationInstance;

import java.util.List;

public class MobCommands {
    private static final double MAX_INSPECTION_RANGE = 10;
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("view_details")
                .executes(MobCommands::viewDetails)
                .then(Commands.literal("cultivation")
                        .executes(MobCommands::viewCultivation)
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(MobCommands::viewTargetCultivation)));
    }
    private static int viewDetails(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player cause = context.getSource().getPlayer();
        if(cause == null) {
            context.getSource().sendFailure(Component.literal("command not used by player"));
            return 0;
        }

        Vec3 pos = cause.getEyePosition();
        Vec3 dir = cause.getViewVector(1).scale(MAX_INSPECTION_RANGE);
        Vec3 targetPos = pos.add(dir);

        AABB boundingBox = cause.getBoundingBox()
                .expandTowards(dir)
                .inflate(1.0D);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                cause, pos, targetPos, boundingBox, entity -> !entity.isSpectator(), MAX_INSPECTION_RANGE * MAX_INSPECTION_RANGE);
        if(hitResult == null) {
            context.getSource().sendFailure(Component.literal("did not hit anything"));
            return 0;
        };
        if(!(hitResult.getEntity() instanceof Mob mob)) {
            context.getSource().sendFailure(Component.literal("did not hit a mob"));
            return 0;
        }

        if(!mob.hasData(AscensionAttachments.MOB_CONFIG_HOLDER)) {
            context.getSource().sendFailure(Component.literal("entity does not have a config holder"));
            return 0;
        };

        MobConfigurationInstance instance = mob.getData(AscensionAttachments.MOB_CONFIG_HOLDER).getConfigurationInstance();


        context.getSource().sendSuccess(() -> buildMobDetails(instance,cause.registryAccess()), false);
        return Command.SINGLE_SUCCESS;
    }
    private static int viewCultivation(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("Look at an entity or supply a target selector."));
            return 0;
        }

        Vec3 pos = player.getEyePosition();
        Vec3 direction = player.getViewVector(1).scale(MAX_INSPECTION_RANGE);
        AABB area = player.getBoundingBox().expandTowards(direction).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player, pos, pos.add(direction), area,
                entity -> entity instanceof LivingEntity && !entity.isSpectator(),
                MAX_INSPECTION_RANGE * MAX_INSPECTION_RANGE
        );
        if (hit == null) {
            context.getSource().sendFailure(Component.literal("No living entity in sight within 10 blocks. Try the target selector instead."));
            return 0;
        }
        return showCultivation(context.getSource(), hit.getEntity());
    }

    private static int viewTargetCultivation(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return showCultivation(context.getSource(), EntityArgument.getEntity(context, "target"));
    }

    private static int showCultivation(CommandSourceStack sender, Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            sender.sendFailure(Component.literal("Target is not a living entity."));
            return 0;
        }

        AscensionEntityDataProvider provider = living.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if (provider == null) {
            sender.sendFailure(Component.literal("Entity has no Ascension RPG data capability."));
            return 0;
        }
        if (!provider.hasData()) {
            sender.sendSuccess(() -> Component.literal("=== " + living.getName().getString() + " ===\nRPG data attachment has not been initialised."), false);
            return Command.SINGLE_SUCCESS;
        }
        if (provider.getData() == null || provider.getData().getSource() == null) {
            sender.sendFailure(Component.literal("Entity has no origin source."));
            return 0;
        }

        OriginSource source = provider.getData().getSource();
        PathHolder paths = source.getDataSource(CoreDataSources.PATH_DATA_SOURCE.get());
        MutableComponent message = Component.literal("=== CULTIVATION: " + living.getName().getString() + " ===");
        message.append("\nOrigin source attached: " + source.isAttachedTo(living));
        if (paths == null || paths.getPaths().isEmpty()) {
            message.append("\nNo cultivation paths assigned.");
        } else {
            List<Identifier> ids = paths.getPaths().stream().sorted((a, b) -> a.toString().compareTo(b.toString())).toList();
            for (Identifier id : ids) {
                PathInstance data = paths.getPath(id);
                if (data == null) continue;
                int major = data.getCurrentMajorRealm();
                int minor = data.getCurrentMinorRealm();
                Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, id, sender.registryAccess());
                message.append("\n" + id + " : ");
                if (path != null) message.append(path.getRealmName(major, minor));
                else message.append("Unknown path");
                message.append(" (" + major + "." + minor + ")");
                double maximum = data.getMaxProgress();
                if (maximum > 0 && Double.isFinite(maximum)) {
                    double percent = data.getProgress() / maximum * 100.0;
                    if (Double.isFinite(percent)) message.append(String.format(java.util.Locale.ROOT, " | %.1f%%", percent));
                }
            }
        }
        sender.sendSuccess(() -> message, false);
        return Command.SINGLE_SUCCESS;
    }

    public static Component buildMobDetails(MobConfigurationInstance configurationInstance, RegistryAccess access){
        MutableComponent tier = Component.literal("==="+configurationInstance.getTier()+"===");
        MutableComponent traits = Component.empty();
        for(MobTraitReference reference : configurationInstance.getTraits()){
            if(!reference.isValid(access)) continue;
            traits.append(
                    Component.literal("\n"+reference.id()+"(").append(reference.getTrait(access).name()).append(")")
            );
        }
        return tier.append(traits);
    }
}
