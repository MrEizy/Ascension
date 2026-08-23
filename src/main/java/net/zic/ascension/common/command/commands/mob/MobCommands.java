package net.zic.ascension.common.command.commands.mob;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.common.command.commands.AffinityCommand;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.configuration.ConfigurationDataMaps;
import net.zic.ascension.configuration.mob_traits.MobTraitReference;
import net.zic.ascension.configuration.mobs.MobConfiguration;
import net.zic.ascension.configuration.mobs.PotentialTrait;
import net.zic.ascension.mob_cultivation.MobCultivationManager;
import net.zic.ascension.mob_cultivation.oliver_rewrite.MobConfigurationHolder;
import net.zic.ascension.mob_cultivation.oliver_rewrite.MobConfigurationInstance;

public class MobCommands {
    private static final double MAX_INSPECTION_RANGE = 10;
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("view_details")
                .executes(MobCommands::viewDetails);
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
    public static Component buildMobDetails(MobConfigurationInstance configurationInstance, RegistryAccess access){
        MutableComponent tier = Component.literal("==="+configurationInstance.getTier().name()+"===");
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
