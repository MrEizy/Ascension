package net.thejadeproject.ascension.common.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thejadeproject.ascension.entity.ModEntities;
import net.thejadeproject.ascension.entity.custom.shaders.RiftEntity;

public class ShaderSummonerItem extends Item {
    public ShaderSummonerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        //System.out.println("[ShaderSummonerItem] Attempting to spawn rift...");
        //System.out.println("[ShaderSummonerItem] Is client side: " + level.isClientSide);

        if (!level.isClientSide) {
            // Calculate position 2 blocks in front of the player
            Vec3 lookDirection = player.getLookAngle();
            double distance = 2.0;

            double spawnX = player.getX() + lookDirection.x * distance;
            double spawnY = player.getY() + player.getEyeHeight() + lookDirection.y * distance;
            double spawnZ = player.getZ() + lookDirection.z * distance;

            //System.out.println("[ShaderSummonerItem] Spawning at: " + spawnX + ", " + spawnY + ", " + spawnZ);

            RiftEntity rift = new RiftEntity(ModEntities.RIFT.get(), level);
            rift.setPos(spawnX, spawnY, spawnZ);

//            if (level.addFreshEntity(rift)) {
//                //System.out.println("[ShaderSummonerItem] Rift spawned successfully! Entity ID: " + rift.getId());
//            } else {
//                //System.out.println("[ShaderSummonerItem] FAILED to spawn rift!");
//            }
//        } else {
//            //System.out.println("[ShaderSummonerItem] Called on client side - not spawning");
        }

        return InteractionResultHolder.success(itemStack);
    }
}