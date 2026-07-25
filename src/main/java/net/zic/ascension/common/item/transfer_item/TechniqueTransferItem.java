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
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.common.item.components.AscensionComponents;

public class TechniqueTransferItem  extends Item {
    public TechniqueTransferItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResult.PASS;

        if(!stack.has(AscensionComponents.REGISTRY_ID_HOLDER)) return InteractionResult.FAIL;

        Technique targetTechnique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,stack.get(AscensionComponents.REGISTRY_ID_HOLDER),level.registryAccess());

        if(targetTechnique == null) {
            player.sendSystemMessage(Component.literal("[technique does not exist :" +stack.get(AscensionComponents.REGISTRY_ID_HOLDER)+"]"));

            return InteractionResult.FAIL;
        }

        AscensionEntityDataProvider holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);

        if(holder == null || holder.getData(player) == null) return InteractionResult.FAIL;
        AscensionOriginSource source = holder.getData(player).getSource();
        Identifier path = targetTechnique.getPath();
        if(source.getPathData(path) == null){
            player.sendSystemMessage(Component.literal("[You are do not have path : "+path+"]"));
            return InteractionResult.FAIL;
        }
        if(!source.getPathData(path).setCurrentTechnique(
                stack.get(AscensionComponents.REGISTRY_ID_HOLDER),
                source
        )){
            player.sendSystemMessage(Component.literal("[Learned technique :" +stack.get(AscensionComponents.REGISTRY_ID_HOLDER)+"]"));
            return InteractionResult.FAIL;
        }
        source.markPathDirty(path);
        stack.shrink(1);
        AscensionCraft.LOGGER.info("Player {} has transferred their technique",player.getName().getString());
        return InteractionResult.SUCCESS;
    }
}
