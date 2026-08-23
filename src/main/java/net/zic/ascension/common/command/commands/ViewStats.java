package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.common.command.commands.mob.MobCommands;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.ZenithStatHolder;

public class ViewStats {
    private static final double MAX_INSPECTION_RANGE = 10;
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("view_stats")
                .then(Commands.literal("target")
                        .executes(ViewStats::viewTargetDetails)
                ).then(Commands.argument("entity", EntityArgument.entity())
                        .executes(ViewStats::viewEntityTargetDetails)
                );
    }
    private static int viewTargetDetails(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
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

        ZenithStatHolder holder = hitResult.getEntity().getData(ZenithAttachments.STAT_HOLDER);



        context.getSource().sendSuccess(() -> buildResponse(holder), false);
        return Command.SINGLE_SUCCESS;
    }
    private static int viewEntityTargetDetails(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "entity");
        ZenithStatHolder holder = entity.getData(ZenithAttachments.STAT_HOLDER);
        context.getSource().sendSuccess(() -> buildResponse(holder), false);
        return Command.SINGLE_SUCCESS;
    }

    private static Component buildResponse(ZenithStatHolder holder){
        MutableComponent response = Component.literal("=== STATS ===");
        for(Stat stat : holder.getStats()){
            response.append("\n");
            response.append(stat.getName());
            response.append(" : "+holder.getStat(stat));
        }
        return response;
    }
}
