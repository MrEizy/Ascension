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
import net.zic.ascension.common.command.CommandInspectionTarget;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.stats.EntityStatHolder;
import net.zic.zenithlib.stats.Stat;


public class ViewStats {
    private static final double MAX_INSPECTION_RANGE = 10;
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return buildAs("view_stats");
    }
    public static LiteralArgumentBuilder<CommandSourceStack> buildAs(String name) {
        return Commands.literal(name)
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

        EntityHitResult hitResult = CommandInspectionTarget.find(cause, MAX_INSPECTION_RANGE, entity -> true);
        if(hitResult == null) {
            context.getSource().sendFailure(Component.literal("did not hit anything"));
            return 0;
        };

        EntityStatHolder holder = hitResult.getEntity().getData(ZenithAttachments.STAT_HOLDER);



        context.getSource().sendSuccess(() -> buildResponse(holder), false);
        return Command.SINGLE_SUCCESS;
    }
    private static int viewEntityTargetDetails(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "entity");
        EntityStatHolder holder = entity.getData(ZenithAttachments.STAT_HOLDER);
        context.getSource().sendSuccess(() -> buildResponse(holder), false);
        return Command.SINGLE_SUCCESS;
    }

    private static Component buildResponse(EntityStatHolder holder){
        MutableComponent response = Component.literal("=== STATS ===");
        for(Stat stat : holder.getStats()){
            response.append("\n");
            response.append(stat.getName());
            response.append(" : "+holder.getStat(stat));
        }
        return response;
    }
}
