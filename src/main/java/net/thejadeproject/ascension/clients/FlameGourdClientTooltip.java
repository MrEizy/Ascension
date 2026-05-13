package net.thejadeproject.ascension.clients;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.thejadeproject.ascension.common.items.artifacts.FlameGourd.FlameGourdTooltip;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.PoseStack;

public class FlameGourdClientTooltip implements ClientTooltipComponent {
    private final FlameGourdTooltip tooltip;

    public FlameGourdClientTooltip(FlameGourdTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public int getHeight() {
        return 16; // Standard sprite height
    }

    @Override
    public int getWidth(Font font) {
        // Calculate the width needed for fire sprite + arrow + item sprite
        int fireSpriteWidth = 16; // Standard block sprite width
        int arrowWidth = font.width(" → "); // Width of the arrow
        int itemSpriteWidth = 16; // Standard item sprite width

        return fireSpriteWidth + arrowWidth + itemSpriteWidth + 4; // Add some padding
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();

        // Render the fire block using its texture
        Block fireBlock = tooltip.fireBlock();
        BlockState fireState = fireBlock.defaultBlockState();

        // Get the block's texture sprite
        BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
        BakedModel model = blockRenderer.getBlockModel(fireState);
        TextureAtlasSprite sprite = model.getParticleIcon();

        // Render the fire block texture
        if (sprite != null) {
            guiGraphics.blit(x, y, 0, 16, 16, sprite);
        } else {
            // Fallback: use the block's item form
            ItemStack fireBlockItem = getBlockItemStack(fireBlock);
            guiGraphics.renderFakeItem(fireBlockItem, x, y);
            guiGraphics.renderItemDecorations(font, fireBlockItem, x, y);
        }

        // Calculate positions for arrow and item sprite
        int arrowX = x + 16 + 2; // Position arrow after fire sprite with small gap
        int itemSpriteX = arrowX + font.width(" → ") + 2; // Position item sprite after arrow with small gap

        // Render the arrow
        guiGraphics.drawString(font, " → ", arrowX, y + 4, 0xFFFFFF, false);

        // Render the converted item sprite
        ItemStack convertedItemStack = new ItemStack(tooltip.convertedItem());
        guiGraphics.renderFakeItem(convertedItemStack, itemSpriteX, y);
        guiGraphics.renderItemDecorations(font, convertedItemStack, itemSpriteX, y);
    }

    private ItemStack getBlockItemStack(Block block) {
        // Get the item form of the block, with fallbacks for blocks without items
        ItemStack itemStack = new ItemStack(block.asItem());

        if (itemStack.isEmpty()) {
            // Use appropriate fallback items for different fire types
            if (block == Blocks.FIRE) {
                return new ItemStack(net.minecraft.world.item.Items.FIRE_CHARGE);
            } else if (block == Blocks.SOUL_FIRE) {
                return new ItemStack(net.minecraft.world.item.Items.SOUL_TORCH);
            } else {
                return new ItemStack(net.minecraft.world.item.Items.FIRE_CHARGE);
            }
        }

        return itemStack;
    }
}