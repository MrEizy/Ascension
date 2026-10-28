package net.zic.ascension.common.item.transfer_item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.item.components.AscensionComponents;

import java.util.List;

public class BloodlineTransferItem extends Item {
    private static final Identifier DEV_BLOODLINE = AscensionCraft.prefix("1_ordinary/qi_coatings_dev_bloodline");

    public BloodlineTransferItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResult.PASS;

        if(!stack.has(AscensionComponents.REGISTRY_ID_HOLDER)) return InteractionResult.FAIL;

        Identifier targetBloodline = stack.get(AscensionComponents.REGISTRY_ID_HOLDER);
        if(targetBloodline == null)return InteractionResult.FAIL;
        BloodlineInstance<?> bloodline = BloodlineInstance.create(targetBloodline,level.registryAccess());
        if(bloodline == null)return InteractionResult.FAIL;

        int purity = stack.getOrDefault(AscensionComponents.PURITY,1);
        bloodline.getData().setPurity(purity);

        AscensionEntityDataProvider holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);

        if(holder == null || holder.getData() == null) return InteractionResult.FAIL;
        OriginSource source = holder.getData().getSource();
        // The Qi Coatings Dev Bloodline (testing only) replaces every bloodline held, itself included, so it is the only one and
        // its purity actions run fresh each time instead of merging into a copy already held.
        if(targetBloodline.equals(DEV_BLOODLINE)){
            for(Identifier held : List.copyOf(AscensionOriginSourceHelper.getBloodlines(source))){
                AscensionOriginSourceHelper.removeBloodline(source,held);
            }
        }
        if(!AscensionOriginSourceHelper.addBloodline(source,bloodline,true)){
            //TODO return error message to player here
            return InteractionResult.FAIL;
        }
        stack.shrink(1);
        AscensionCraft.LOGGER.info("Player {} has transferred their bloodline",player.getName().getString());
        player.sendSystemMessage(Component.literal("Purity: "+AscensionOriginSourceHelper.getBloodlineData(source,targetBloodline).getPurity()));
        return InteractionResult.SUCCESS;
    }
}
