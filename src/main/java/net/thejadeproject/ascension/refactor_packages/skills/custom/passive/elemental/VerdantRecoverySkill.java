package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.elemental;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.state.BlockState;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.skills.ITickingSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimplePassiveSkill;

public class VerdantRecoverySkill extends SimplePassiveSkill implements ITickingSkill {

    private static final int WOOD_SCAN_RADIUS = 4;
    private static final int WOOD_REQUIRED_PLANTS = 8;

    @Override
    protected String getTitleKey() {
        return "ascension.skill.verdant_recovery";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.verdant_recovery.description";
    }

    @Override
    protected String getIconPath() {
        return "textures/spells/icon/placeholder.png";
    }

    @Override
    public void onPlayerTick(ServerPlayer player, IEntityData entityData) {
        if (player.tickCount % 40 != 0) return;
        if (player.getHealth() >= player.getMaxHealth()) return;
        if (!isNearPlantLife(player, WOOD_SCAN_RADIUS)) return;

        player.heal(4.0F);
    }

    private static boolean isNearPlantLife(ServerPlayer player, int radius) {
        BlockPos center = player.blockPosition();
        int plants = 0;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )) {
            BlockState state = player.level().getBlockState(pos);

            if (isPlantLife(state)) {
                plants++;

                if (plants >= WOOD_REQUIRED_PLANTS) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isPlantLife(BlockState state) {
        return state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.CROPS)
                || state.is(BlockTags.LEAVES);
    }
}