package net.zic.ascension.handler;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.datapack.path.realm.PathRealmChangeEvent;
import net.zic.ascension.api.ascension.event.bloodline.BloodlineEvent;
import net.zic.ascension.api.ascension.event.physique.PhysiqueChangedEvent;
import net.zic.ascension.api.ascension.event.technique.TechniqueEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.item.ModItems;
import net.zic.zenithlib.toast.ZenithToasts;

/**
 * Toasts on events go in this class btw, since most of the events we've made actually have events to hook into :)
 * Otherwise just use the show() method in a class that needs it, it requires a source, a title, a message and an icon.
 */

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AscensionToastHandler {
    private AscensionToastHandler() {
    }

    @SubscribeEvent
    public static void onPhysiqueChanged(PhysiqueChangedEvent.Post event) {
        Physique physique = event.getNewPhysique(event.getSource().getRegistryAccess());
        if (physique == null) {
            return;
        }

        show(
                event.getSource(),
                Component.translatable("toast.ascension.physique_changed"),
                physique.name(),
                ModItems.PHYSIQUE_ESSENCE.get().getDefaultInstance()
        );
    }

    @SubscribeEvent
    public static void onBloodlineGained(BloodlineEvent.Added.Post event) {
        Bloodline bloodline = event.getBloodline(event.getSource().getRegistryAccess());
        if (bloodline == null) {
            return;
        }

        show(
                event.getSource(),
                Component.translatable("toast.ascension.bloodline_gained"),
                bloodline.getName(),
                ModItems.BLOODLINE_ESSENCE.get().getDefaultInstance()
        );
    }

    @SubscribeEvent
    public static void onTechniqueLearned(TechniqueEvent.Added.Post event) {
        Technique technique = event.getTechnique(event.getSource().getRegistryAccess());
        if (technique == null) {
            return;
        }

        show(
                event.getSource(),
                Component.translatable("toast.ascension.technique_learned"),
                technique.getName(event.getTechniqueData()),
                ModItems.TECHNIQUE_MANUAL.get().getDefaultInstance()
        );
    }

    @SubscribeEvent
    public static void onRealmAdvanced(PathRealmChangeEvent.PathRealmUpEvent event) {
        if (event.getOldRealm().majorRealm() < 0) {
            return;
        }

        Path path = event.getPath();
        if (path == null) {
            return;
        }

        show(
                event.getSource(),
                Component.translatable("toast.ascension.realm_advanced"),
                Component.translatable(
                        "toast.ascension.realm_change_message",
                        path.name(),
                        path.getRealmName(event.getRealm().majorRealm(), event.getRealm().minorRealm())
                ),
                ItemStack.EMPTY
        );
    }

    @SubscribeEvent
    public static void onRealmRegressed(PathRealmChangeEvent.PathRealmDownEvent event) {
        Path path = event.getPath();
        if (path == null) {
            return;
        }

        show(
                event.getSource(),
                Component.translatable("toast.ascension.realm_regressed"),
                Component.translatable(
                        "toast.ascension.realm_change_message",
                        path.name(),
                        path.getRealmName(event.getRealm().majorRealm(), event.getRealm().minorRealm())
                ),
                ItemStack.EMPTY
        );
    }

    private static void show(OriginSource source, Component title, Component message, ItemStack icon) {
        for (LivingEntity entity : source.getAttachedEntities()) {
            if (entity instanceof ServerPlayer player) {
                ZenithToasts.show(player, title, message, icon);
            }
        }
    }
}