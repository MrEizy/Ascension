package net.zic.ascension.capabilities;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.damage_provider.AscensionDamageSourceProvider;
import net.zic.ascension.capabilities.entity_holder.PlayerDataProvider;
import net.zic.ascension.capabilities.qi_provider.SimpleEntityQiProvider;
import net.zic.ascension.mob_cultivation.oliver_rewrite.MobAscensionDataProvider;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class AscensionCapabilities {

    @SubscribeEvent // on the mod event bus
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        //a bit hacky but eh it is what it is
        for(EntityType<?> type : BuiltInRegistries.ENTITY_TYPE.stream().toList()){
            event.registerEntity(
                    CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY,
                    type,
                    (entity,nul)->(entity instanceof Mob mob) ? new MobAscensionDataProvider(mob) :null
            );

        }
        event.registerEntity(
                CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY,
                        EntityType.PLAYER,
                        (player,nul)->new PlayerDataProvider(player)
        );
        event.registerEntity(
                CoreCapabilities.ASCENSION_ENTITY_DAMAGE_SOURCE_PROVIDER,
                EntityType.ARROW,
                (entity,nul)->new AscensionDamageSourceProvider.EntitySource(entity,
                        Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"bow")
                )
        );
        event.registerItem(
                CoreCapabilities.ASCENSION_ITEM_STACK_DAMAGE_SOURCE_PROVIDER,
                (item,nul)->new AscensionDamageSourceProvider.ItemSource(item,
                        Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"sword")),
                Items.DIAMOND_SWORD
        );
        event.registerEntity(
                CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER,
                EntityType.PLAYER,
                (entity,nul)->new SimpleEntityQiProvider(entity)
        );

        System.out.println("registering capabilities");

    }
}
