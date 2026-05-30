package net.zic.ascension.common.item.transfer_item;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
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
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.common.item.components.AscensionComponents;

public class BloodlineTransferItem extends Item {
    public BloodlineTransferItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResult.PASS;

        if(!stack.has(AscensionComponents.REGISTRY_ID_HOLDER)) return InteractionResult.FAIL;

        Identifier targetBloodline = stack.get(AscensionComponents.REGISTRY_ID_HOLDER);
        Bloodline bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY,targetBloodline,level.registryAccess());
        BloodlineData data = bloodline.newData();

        int purity = stack.getOrDefault(AscensionComponents.PURITY,1);
        data.setPurity(purity);
        if(targetBloodline == null)return InteractionResult.FAIL;

        AscensionEntityDataHolder holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);

        if(holder == null || holder.getData(player) == null) return InteractionResult.FAIL;

        if(!holder.getData(player).getSource().addBloodline(targetBloodline,data,level.registryAccess())){
            //TODO return error message to player here
            return InteractionResult.FAIL;
        }
        stack.shrink(1);
        AscensionCraft.LOGGER.info("Player {} has transferred their bloodline",player.getName().getString());
        player.sendSystemMessage(Component.literal("Purity: "+holder.getData(player).getSource().getBloodlineData(targetBloodline).getPurity()));
        return InteractionResult.SUCCESS;
    }
}
