package net.zic.ascension.capabilities;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.capabilities.damage_provider.SimpleEntityDamageSourceProvider;
import net.zic.ascension.capabilities.damage_provider.SimpleItemDamageSourceProvider;
import net.zic.ascension.capabilities.entity_holder.PlayerDataHolder;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class AscensionCapabilities {

    @SubscribeEvent // on the mod event bus
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(
                CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY,
                        EntityType.PLAYER,
                        (player,nul)->new PlayerDataHolder()
        );
        event.registerEntity(
                CoreCapabilities.ASCENSION_ENTITY_DAMAGE_SOURCE_PROVIDER,
                EntityType.ARROW,
                (entity,nul)->new SimpleEntityDamageSourceProvider(entity,
                        Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"bow")
                )
        );
        event.registerItem(
                CoreCapabilities.ASCENSION_ITEM_STACK_DAMAGE_SOURCE_PROVIDER,
                (item,nul)->new SimpleItemDamageSourceProvider(item,
                        Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"sword")),
                Items.DIAMOND_SWORD
        );
    }
}
