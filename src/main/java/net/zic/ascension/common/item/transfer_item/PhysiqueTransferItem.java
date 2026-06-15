package net.zic.ascension.common.item.transfer_item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.capabilities.AscensionCapabilities;
import net.zic.ascension.capabilities.entity_holder.PlayerDataHolder;
import net.zic.ascension.common.item.components.AscensionComponents;

public class PhysiqueTransferItem extends Item {
    public PhysiqueTransferItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResult.PASS;

        if(!stack.has(AscensionComponents.REGISTRY_ID_HOLDER)) return InteractionResult.FAIL;

        Identifier targetPhysique = stack.get(AscensionComponents.REGISTRY_ID_HOLDER);

        if(targetPhysique == null)return InteractionResult.FAIL;

        AscensionEntityDataHolder holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);

        if(holder == null || holder.getData(player) == null) return InteractionResult.FAIL;

        if(!holder.getData(player).getSource().setPhysique(targetPhysique)){
            //TODO return error message to player here
            return InteractionResult.FAIL;
        }
        stack.shrink(1);
        AscensionCraft.LOGGER.info("Player {} has transferred their physique",player.getName().getString());
        return InteractionResult.SUCCESS;
    }
}
