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
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.item.components.AscensionComponents;

public class TechniqueTransferItem extends Item {
    public TechniqueTransferItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResult.PASS;

        if(!stack.has(AscensionComponents.REGISTRY_ID_HOLDER)) return InteractionResult.FAIL;

        Identifier techniqueId = stack.get(AscensionComponents.REGISTRY_ID_HOLDER);
        Technique targetTechnique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, techniqueId, level.registryAccess());

        if(targetTechnique == null) {
            player.sendSystemMessage(Component.literal("[technique does not exist :" + techniqueId + "]"));
            return InteractionResult.FAIL;
        }

        AscensionEntityDataProvider holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);

        if(holder == null || holder.getData() == null) return InteractionResult.FAIL;
        OriginSource source = holder.getData().getSource();

        if (!AscensionOriginSourceHelper.addTechnique(source, techniqueId)) return InteractionResult.FAIL;

        stack.shrink(1);
        AscensionCraft.LOGGER.info("Player {} has transferred their technique", player.getName().getString());
        return InteractionResult.SUCCESS;
    }
}
