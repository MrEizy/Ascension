package net.thejadeproject.ascension.clients.toast;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static net.thejadeproject.ascension.clients.toast.AscensionToastInterface.DEFAULT_BACKGROUND;

public final class AscensionToastHelper {

    private AscensionToastHelper() {}

    public static void show(Component title, Component message, ItemStack icon) {
        show(title, message, icon, DEFAULT_BACKGROUND);
    }

    public static void show(Component title, Component message, ItemStack icon, ResourceLocation background) {
        Minecraft minecraft = Minecraft.getInstance();

        minecraft.execute(() -> {
            minecraft.getToasts().addToast(new AscensionToast(title, message, icon, background));
        });
    }
}