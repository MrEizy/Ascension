package net.thejadeproject.ascension.refactor_packages.bloodlines.generic;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;

/**
 * A ready-to-use evolvable bloodline that grows purity through kills.
 *
 * Extend {@link GenericEvolvableBloodline} directly if you want a completely
 * different evolution trigger (time-based, quest-based, realm-based, etc.).
 *
 * This class just wires the LivingDeathEvent and provides a concrete type
 * for registrations that want the standard kill-purity behaviour.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class PurityBloodline extends GenericEvolvableBloodline {

    public PurityBloodline(Component title, ResourceLocation evolvesInto) {
        super(title, evolvesInto);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;
        IEntityData data = player.getData(ModAttachments.ENTITY_DATA);
        if (data.getBloodline() instanceof GenericEvolvableBloodline evolvable) {
            evolvable.onKill(player, data, event.getEntity());
        }
    }
}