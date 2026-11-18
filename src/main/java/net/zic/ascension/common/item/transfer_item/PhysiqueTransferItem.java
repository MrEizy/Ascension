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
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.impl.core.connection.Connected;

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

        AscensionEntityDataProvider holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);

        if(holder == null || holder.getData() == null) return InteractionResult.FAIL;
        OriginSource source = holder.getData().getSource();

        // connected_physique: the technique / bloodline it is connected to has to be held first
        Physique<?> definition = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, targetPhysique, level.registryAccess());
        if (definition instanceof Connected connected) {
            Component failure = connected.connections().failureMessage(source, level.registryAccess());
            if (failure != null) {
                player.sendOverlayMessage(failure);
                return InteractionResult.FAIL;
            }
        }

        if(!AscensionOriginSourceHelper.setPhysique(source,targetPhysique)){
            //TODO return error message to player here
            return InteractionResult.FAIL;
        }
        stack.shrink(1);
        AscensionCraft.LOGGER.info("Player {} has transferred their physique",player.getName().getString());
        return InteractionResult.SUCCESS;
    }
}