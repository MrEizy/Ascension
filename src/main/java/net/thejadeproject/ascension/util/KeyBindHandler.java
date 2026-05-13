package net.thejadeproject.ascension.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import net.thejadeproject.ascension.data_attachments.ModAttachments;

import net.thejadeproject.ascension.common.items.artifacts.TabletOfDestructionEarth;
import net.thejadeproject.ascension.common.items.artifacts.TabletOfDestructionHeaven;

import net.thejadeproject.ascension.network.serverBound.ToggleTabletDropModePayload;
import net.thejadeproject.ascension.particle.ModParticles;

import java.util.HashSet;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class KeyBindHandler {

    public static final String CULTIVATION_CATEGORY = "category.ascension.cultivation";
    private static boolean wasCultivating = false;

    private KeyBindHandler() {
    }



    private static void spawnCultivationParticles(Player player) {
        // Spawn particles less frequently
        if (player.level().getGameTime() % 5 != 0) return;

        double centerX = player.getX();
        double centerY = player.getY() + 0.1; // At feet level
        double centerZ = player.getZ();

        // Fixed radius - choose one that works for you
        double circleRadius = 2.0; // 2 blocks radius
        double pentagramRadius = circleRadius * 0.6; // Smaller pentagram

        // Reduced number of circle particles
        int circlePoints = 8; // Only 8 points in the circle
        for (int i = 0; i < circlePoints; i++) {
            double angle = 2 * Math.PI * i / circlePoints;
            double pointX = centerX + circleRadius * Math.cos(angle);
            double pointZ = centerZ + circleRadius * Math.sin(angle);

            // Spawn particle with ZERO upward motion
            player.level().addParticle(
                    ModParticles.CULTIVATION_PARTICLES.get(),
                    pointX,
                    centerY, // Same Y level
                    pointZ,
                    0.0, 0.0, 0.0 // No movement at all
            );
        }

        // Pentagram vertices only (no connecting lines)
        int pentagramPoints = 5;
        double rotation = player.level().getGameTime() * 0.015; // Very slow rotation

        // Only spawn pentagram vertices, not connecting lines
        for (int i = 0; i < pentagramPoints; i++) {
            double angle = (2 * Math.PI * i / pentagramPoints) - (Math.PI / 2) + rotation;
            double pointX = centerX + pentagramRadius * Math.cos(angle);
            double pointZ = centerZ + pentagramRadius * Math.sin(angle);

            // Spawn vertex particle with minimal or no movement
            player.level().addParticle(
                    ModParticles.CULTIVATION_PARTICLES.get(),
                    pointX,
                    centerY + 0.02, // Slightly above circle for visibility
                    pointZ,
                    0.0, 0.0, 0.0 // No movement
            );
        }

        // Optional: One simple central particle
        if (player.level().getGameTime() % 10 == 0) {
            player.level().addParticle(
                    ModParticles.CULTIVATION_PARTICLES.get(),
                    centerX,
                    centerY + 0.01,
                    centerZ,
                    0.0, 0.0, 0.0
            );
        }
    }

    public static final KeyMapping TOGGLE_ARTIFACT_MODE_KEY = new KeyMapping("key.ascension.toggle_artifact_mode", 77, CULTIVATION_CATEGORY);



    public static void register() {
        IEventBus eventBus = NeoForge.EVENT_BUS;
        eventBus.addListener(EventPriority.HIGH, KeyBindHandler::handleKeyInputEvent);
        eventBus.addListener(EventPriority.HIGHEST, KeyBindHandler::keyInputEvents);

    }

    public static void keyInputEvents(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null && minecraft.getConnection() == null) return;
        /*
        if (CAST_SKILL_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new ServerCastSkillPayload());
        }


         */



    }
    public static Set<KeyMapping> state = new HashSet<>();
    public static void handleKeyInputEvent(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null && minecraft.getConnection() == null) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;




        // Handle Toggle Tablet Mode key
        if (TOGGLE_ARTIFACT_MODE_KEY.consumeClick()) {
            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();

            // Check if player is holding either tablet in main hand or offhand
            if (mainHandItem.getItem() instanceof TabletOfDestructionHeaven ||
                    mainHandItem.getItem() instanceof TabletOfDestructionEarth ||
                    offHandItem.getItem() instanceof TabletOfDestructionHeaven ||
                    offHandItem.getItem() instanceof TabletOfDestructionEarth) {

                // Send packet to server to toggle drop mode
                PacketDistributor.sendToServer(new ToggleTabletDropModePayload());
            }
        }


    }
}