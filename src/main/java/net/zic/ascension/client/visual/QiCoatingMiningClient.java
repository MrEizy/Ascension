package net.zic.ascension.client.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingChannel;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingElement;
import net.zic.ascension.impl.core.skill.castable.QiCoatingMining;
import net.zic.ascension.network.ServerboundToolChargeReleasePacket;
import org.jspecify.annotations.Nullable;

/**
 * The client's half of an External Qi Coating element's faster mining (QiCoatingMining): the client decides when a block
 * breaks, so it needs the same speed as the server. It also holds back a hoe's tilling or shovel's path on a click the server
 * charges one with. The element is the player's picked one, while their External Qi Coating is on.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class QiCoatingMiningClient {
    // Set while the use key holds a coated hoe or shovel charging, until it comes up.
    private static boolean charging;

    private QiCoatingMiningClient() {
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            return;
        }
        QiCoatingMining.applySpeed(event, externalElement(player));
    }

    /**
     * A hoe or shovel click the server turns into a charging till or path (QiCoatingMining.onChargeArea) mustn't change the
     * block or sound here either: it's cancelled without a swing, and the click still reaches the server.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND
                || !event.getItemStack().is(ItemTags.HOES) && !event.getItemStack().is(ItemTags.SHOVELS)) {
            return;
        }
        if (QiCoatingMining.chargesArea(externalElement(player), player, event.getItemStack(), event.getHitVec())) {
            charging = true;
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
        }
    }

    /**
     * Tells the server the moment the use key comes up after charging a till or path, so it's made then rather than waiting for the
     * held clicks to stop.
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!charging || Minecraft.getInstance().options.keyUse.isDown()) {
            return;
        }
        charging = false;
        if (Minecraft.getInstance().getConnection() != null) {
            ClientPacketDistributor.sendToServer(new ServerboundToolChargeReleasePacket());
        }
    }

    // The player's picked External Qi Coating element while that coating is on, or null.
    private static @Nullable QiCoatingElement externalElement(Player player) {
        if (QiCoatingClientState.live(player.getUUID(), QiCoatingChannel.EXTERNAL) == null) {
            return null;
        }
        return player.getData(CoreAttachments.QI_COATING_SELECTION).get(QiCoatingChannel.EXTERNAL)
                .map(id -> QiCoatingElement.byId(id, player.registryAccess()))
                .orElse(null);
    }
}
