package net.zic.ascension.impl.core.skill.castable.held;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class HeldCastInterruptionHandler {
    private HeldCastInterruptionHandler() {
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getNewDamage() <= 0.0F) {
            return;
        }
        player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER)
                .recordDamage(event.getNewDamage());
    }
}
