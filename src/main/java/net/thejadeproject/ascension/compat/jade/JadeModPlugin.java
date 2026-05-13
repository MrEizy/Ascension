package net.thejadeproject.ascension.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.blocks.custom.CauldronPedestalBlock;
import net.thejadeproject.ascension.common.blocks.custom.FlameStandBlock;
import net.thejadeproject.ascension.common.blocks.custom.PillCauldronLowHumanBlock;
import net.thejadeproject.ascension.common.blocks.entity.CauldronPedestalBlockEntity;
import net.thejadeproject.ascension.common.blocks.entity.FlameStandBlockEntity;
import net.thejadeproject.ascension.common.blocks.entity.PillCauldronLowHumanEntity;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade (WTHIT/Waila) tooltip provider for the Pill Cauldron multiblock.
 *
 * Shows when looking at the Pill Cauldron:
 *   • Flame Stand temperature + bar + status
 *   • Pedestal contents (left / back / right)
 *   • Craft progress % and seconds remaining
 *
 * Shows when looking at the Flame Stand directly:
 *   • Temperature + bar + fan warning
 *
 * Shows when looking at a Cauldron Pedestal:
 *   • Item name and count, or "Empty" prompt
 *
 * NOTE on registerBlockComponent signature:
 *   Jade's IWailaClientRegistration.registerBlockComponent(IBlockComponentProvider, Class<? extends Block>)
 *   takes a Class, NOT a Block instance. We cast via .getClass() here.
 */
@WailaPlugin
public class JadeModPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        // No server-side data needed — all data is read client-side from the BE
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // The API requires Class<? extends Block>, not a Block instance.
        registration.registerBlockComponent(
                CauldronProvider.INSTANCE,
                PillCauldronLowHumanBlock.class);

        registration.registerBlockComponent(
                FlameStandProvider.INSTANCE,
                FlameStandBlock.class);

        registration.registerBlockComponent(
                PedestalProvider.INSTANCE,
                CauldronPedestalBlock.class);
    }

    // ── Cauldron tooltip ──────────────────────────────────────────

    public enum CauldronProvider implements IBlockComponentProvider {
        INSTANCE;

        private static final ResourceLocation UID =
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pill_cauldron");

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            BlockEntity be = accessor.getBlockEntity();
            if (!(be instanceof PillCauldronLowHumanEntity cauldron)) return;

            FlameStandBlockEntity fs = cauldron.getFlameStand();

            // ── Flame status ──────────────────────────────────────
            if (fs != null && fs.isLit()) {
                int temp    = fs.getTemperature();
                int minTemp = cauldron.getRecipeMinTemp();
                int maxTemp = cauldron.getRecipeMaxTemp();
                tooltip.add(buildTempLine(temp, minTemp, maxTemp));
                tooltip.add(Component.literal(buildTempBar(temp)));

                if (fs.isCritical())
                    tooltip.add(Component.literal("§c⚠ Critical! Fan immediately!"));
                else if (fs.needsFanning())
                    tooltip.add(Component.literal("§e⚡ Fan the flame!"));
            } else {
                tooltip.add(Component.literal("§7Flame Stand: §cUnlit"));
            }

            // ── Pedestal contents ─────────────────────────────────
            ItemStack left  = cauldron.itemHandler.getStackInSlot(0);
            ItemStack back  = cauldron.itemHandler.getStackInSlot(1);
            ItemStack right = cauldron.itemHandler.getStackInSlot(2);

            if (!left.isEmpty() || !back.isEmpty() || !right.isEmpty()) {
                tooltip.add(Component.literal("§6Ingredients:"));
                if (!left.isEmpty())
                    tooltip.add(Component.literal(
                            "  §7Left: §f" + left.getCount() + "× " + left.getHoverName().getString()));
                if (!back.isEmpty())
                    tooltip.add(Component.literal(
                            "  §7Back: §f" + back.getCount() + "× " + back.getHoverName().getString()));
                if (!right.isEmpty())
                    tooltip.add(Component.literal(
                            "  §7Right: §f" + right.getCount() + "× " + right.getHoverName().getString()));
            }

            // ── Craft progress ────────────────────────────────────
            int progress    = cauldron.data.get(0);
            int maxProgress = cauldron.data.get(1);
            if (maxProgress > 0 && progress > 0) {
                int pct     = (progress * 100) / maxProgress;
                int secLeft = Math.max(0, (maxProgress - progress) / 20);
                tooltip.add(Component.literal(
                        "§aCrafting: §f" + pct + "% §7(" + secLeft + "s left)"));
                tooltip.add(Component.literal(buildProgressBar(pct)));
            }
        }

        @Override
        public ResourceLocation getUid() { return UID; }
    }

    // ── Flame Stand tooltip ───────────────────────────────────────

    public enum FlameStandProvider implements IBlockComponentProvider {
        INSTANCE;

        private static final ResourceLocation UID =
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "flame_stand");

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            BlockEntity be = accessor.getBlockEntity();
            if (!(be instanceof FlameStandBlockEntity fs)) return;

            if (!fs.isLit()) {
                tooltip.add(Component.literal("§7Unlit — right-click with a fire item to light"));
                return;
            }

            int temp = fs.getTemperature();
            tooltip.add(buildTempLine(temp, 0, 0));
            tooltip.add(Component.literal(buildTempBar(temp)));

            if (fs.isCritical())
                tooltip.add(Component.literal("§c⚠ Critical! Fan immediately!"));
            else if (fs.needsFanning())
                tooltip.add(Component.literal("§e⚡ Fan the flame!"));
            else
                tooltip.add(Component.literal("§a✔ Burning steadily"));
        }

        @Override
        public ResourceLocation getUid() { return UID; }
    }

    // ── Pedestal tooltip ──────────────────────────────────────────

    public enum PedestalProvider implements IBlockComponentProvider {
        INSTANCE;

        private static final ResourceLocation UID =
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron_pedestal");

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            BlockEntity be = accessor.getBlockEntity();
            if (!(be instanceof CauldronPedestalBlockEntity pedestal)) return;

            ItemStack item = pedestal.getItem();
            if (item.isEmpty()) {
                tooltip.add(Component.literal("§7Empty — right-click with an item to place"));
            } else {
                tooltip.add(Component.literal(
                        "§6" + item.getCount() + "× §f" + item.getHoverName().getString()));
            }
        }

        @Override
        public ResourceLocation getUid() { return UID; }
    }

    // ── Shared helpers ────────────────────────────────────────────

    private static Component buildTempLine(int temp, int minTemp, int maxTemp) {
        String base = "§6Temperature: §f" + temp + "°";
        if (minTemp > 0 && maxTemp > minTemp) {
            String status;
            if      (temp < minTemp) status = " §9(too cold)";
            else if (temp > maxTemp) status = " §c(too hot)";
            else                     status = " §a(in range)";
            return Component.literal(base + status + " §7[" + minTemp + "–" + maxTemp + "]");
        }
        return Component.literal(base);
    }

    private static String buildTempBar(int temp) {
        int segments = 10;
        int filled   = Math.min(segments,
                (int)((temp / (float) FlameStandBlockEntity.MAX_TEMP) * segments));

        String fillColor;
        if      (temp < FlameStandBlockEntity.WARN_THRESH)       fillColor = "§9";
        else if (temp < FlameStandBlockEntity.MAX_TEMP / 2)      fillColor = "§e";
        else                                                       fillColor = "§c";

        StringBuilder bar = new StringBuilder(fillColor);
        for (int i = 0; i < segments; i++) {
            bar.append(i < filled ? "█" : "§7█");
        }
        return bar.toString();
    }

    private static String buildProgressBar(int pct) {
        int filled = pct / 10;
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "█" : "§7█");
        }
        return bar.toString();
    }
}