package net.thejadeproject.ascension.clients.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.thejadeproject.ascension.common.blocks.entity.TechniqueStandBlockEntity;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.common.items.techniques.TechniqueBinderItem;
import net.thejadeproject.ascension.common.items.techniques.TechniquePageItem;
import net.thejadeproject.ascension.common.items.techniques.TechniqueTransferItem;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.helpers.TechniqueManualRegistry;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class TechniqueStandRenderer implements BlockEntityRenderer<TechniqueStandBlockEntity> {

    private static final int MAX_VISIBLE_BODY_LINES = 7;
    private static final float BODY_LINE_HEIGHT = 10f;
    private static final float HEADER_SCALE = 1.4f;
    private static final float HEADER_LINE_HEIGHT = BODY_LINE_HEIGHT * HEADER_SCALE;
    private static final float SCROLL_SPEED = 0.4f;

    private final ItemRenderer itemRenderer;
    private final Font font;

    public TechniqueStandRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public boolean shouldRenderOffScreen(TechniqueStandBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 16;
    }

    @Override
    public void render(TechniqueStandBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = blockEntity.getStoredItem();
        if (stack.isEmpty()) return;

        renderItem(stack, blockEntity, poseStack, bufferSource);
        renderHolographicText(stack, poseStack, bufferSource);
    }

    private void renderItem(ItemStack stack, TechniqueStandBlockEntity blockEntity,
                            PoseStack poseStack, MultiBufferSource bufferSource) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.88, 0.5);

        net.minecraft.core.Direction facing = blockEntity.getBlockState()
                .getValue(net.thejadeproject.ascension.common.blocks.custom.TechniqueStandBlock.FACING);
        float yRot = switch (facing) {
            case SOUTH -> 180f;
            case WEST  -> 90f;
            case EAST  -> -90f;
            default    -> 0f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(65f));
        poseStack.scale(0.6f, 0.6f, 0.6f);

        itemRenderer.renderStatic(
                stack, ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, blockEntity.getLevel(), 0);

        poseStack.popPose();
    }

    private void renderHolographicText(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource) {
        Component header = buildHeader(stack);
        Component progressLine = buildProgressLine(stack);
        List<Component> bodyLines = buildBodyLines(stack);

        if (header == null && progressLine == null && bodyLines.isEmpty()) return;

        boolean needsScroll = bodyLines.size() > MAX_VISIBLE_BODY_LINES;

        float totalHeight = 0;
        if (header != null) totalHeight += HEADER_LINE_HEIGHT + 4f;
        if (progressLine != null) totalHeight += BODY_LINE_HEIGHT + 4f;
        int visibleCount = needsScroll ? MAX_VISIBLE_BODY_LINES : bodyLines.size();
        totalHeight += visibleCount * BODY_LINE_HEIGHT;

        poseStack.pushPose();
        poseStack.translate(0.5, 2.4 + totalHeight * 0.5 * 0.025, 0);

        Quaternionf q = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
        poseStack.mulPose(q);
        poseStack.scale(0.025f, -0.025f, 0.025f);

        Matrix4f pose = poseStack.last().pose();

        float cursorY = 0f;

        if (header != null) {
            poseStack.pushPose();
            poseStack.scale(HEADER_SCALE, HEADER_SCALE, 1f);
            Matrix4f headerPose = poseStack.last().pose();
            float hx = -font.width(header) / 2.0f;
            float hy = cursorY / HEADER_SCALE;
            font.drawInBatch(header, hx, hy, 0xFFFFFFFF, false, headerPose, bufferSource,
                    Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
            cursorY += HEADER_LINE_HEIGHT + 4f;
        }

        if (progressLine != null) {
            float px = -font.width(progressLine) / 2.0f;
            font.drawInBatch(progressLine, px, cursorY, 0xFFFFFFFF, false, pose, bufferSource,
                    Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            cursorY += BODY_LINE_HEIGHT + 4f;
        }

        if (!bodyLines.isEmpty()) {
            if (needsScroll) {
                float totalScrollHeight = bodyLines.size() * BODY_LINE_HEIGHT;
                float scrollPeriod = bodyLines.size() / SCROLL_SPEED;
                float timeSec = (System.nanoTime() / 1_000_000_000.0f) % scrollPeriod;
                float rawOffset = timeSec * SCROLL_SPEED * BODY_LINE_HEIGHT;

                float wrappedOffset = rawOffset % totalScrollHeight;

                for (int i = 0; i < bodyLines.size(); i++) {
                    float lineY = cursorY + (i * BODY_LINE_HEIGHT) - wrappedOffset;

                    float windowTop = cursorY;
                    float windowBottom = cursorY + MAX_VISIBLE_BODY_LINES * BODY_LINE_HEIGHT;

                    if (lineY < windowTop - BODY_LINE_HEIGHT || lineY >= windowBottom) {
                        float wrappedY = lineY + totalScrollHeight;
                        if (wrappedY < windowTop - BODY_LINE_HEIGHT || wrappedY >= windowBottom) {
                            continue;
                        }
                        lineY = wrappedY;
                    }

                    float alpha = 1.0f;
                    float fadeZone = BODY_LINE_HEIGHT;
                    if (lineY < windowTop + fadeZone) {
                        alpha = Math.max(0f, (lineY - windowTop) / fadeZone);
                    } else if (lineY > windowBottom - fadeZone * 2) {
                        alpha = Math.max(0f, (windowBottom - lineY) / (fadeZone * 2));
                    }

                    int a = (int)(alpha * 255) << 24;
                    int color = a | 0x00FFFFFF;

                    Component line = bodyLines.get(i);
                    float x = -font.width(line) / 2.0f;
                    font.drawInBatch(line, x, lineY, color, false, pose, bufferSource,
                            Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
                }
            } else {
                for (int i = 0; i < bodyLines.size(); i++) {
                    Component line = bodyLines.get(i);
                    float x = -font.width(line) / 2.0f;
                    float y = cursorY + i * BODY_LINE_HEIGHT;
                    font.drawInBatch(line, x, y, 0xFFFFFFFF, false, pose, bufferSource,
                            Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
                }
            }
        }

        poseStack.popPose();
    }

    private Component buildHeader(ItemStack stack) {
        if (stack.getItem() instanceof TechniqueTransferItem) {
            String id = stack.get(ModDataComponents.TECHNIQUE_ID.get());
            if (id != null) {
                ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY
                        .get(ResourceLocation.parse(id));
                if (technique != null) {
                    return technique.getDisplayTitle().copy().withStyle(ChatFormatting.GOLD);
                }
            }
            return Component.literal("Manual").withStyle(ChatFormatting.GOLD);

        } else if (stack.getItem() instanceof TechniqueBinderItem) {
            String id = stack.get(ModDataComponents.TECHNIQUE_ID.get());
            if (id != null) {
                ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY
                        .get(ResourceLocation.parse(id));
                if (technique != null) {
                    return technique.getDisplayTitle().copy().withStyle(ChatFormatting.GOLD);
                }
            }
            return Component.translatable("ascension.binder.empty").withStyle(ChatFormatting.GOLD);

        } else if (stack.getItem() instanceof TechniquePageItem) {
            String id = stack.get(ModDataComponents.TECHNIQUE_ID.get());
            Integer pageIndex = stack.get(ModDataComponents.PAGE_INDEX.get());
            if (id != null && pageIndex != null) {
                ITechnique technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY
                        .get(ResourceLocation.parse(id));
                if (technique != null) {
                    return Component.translatable("ascension.item.technique_page", technique.getDisplayTitle())
                            .withStyle(ChatFormatting.GOLD);
                }
            }
            return Component.translatable("ascension.item.technique_page.blank").withStyle(ChatFormatting.GOLD);
        }

        return null;
    }

    private Component buildProgressLine(ItemStack stack) {
        if (stack.getItem() instanceof TechniqueBinderItem) {
            String id = stack.get(ModDataComponents.TECHNIQUE_ID.get());
            List<Integer> collected = stack.getOrDefault(
                    ModDataComponents.COLLECTED_PAGE_INDICES.get(), new ArrayList<>());

            if (id != null) {
                ResourceLocation rId = ResourceLocation.parse(id);
                var manualData = TechniqueManualRegistry.get(rId);
                if (manualData.isPresent()) {
                    int required = manualData.get().requiredPages();
                    return Component.translatable("ascension.binder.progress", collected.size(), required)
                            .withStyle(ChatFormatting.GRAY);
                }
            }
        }
        return null;
    }

    private List<Component> buildBodyLines(ItemStack stack) {
        List<Component> lines = new ArrayList<>();

        if (stack.getItem() instanceof TechniqueTransferItem) {

        } else if (stack.getItem() instanceof TechniqueBinderItem) {
            String id = stack.get(ModDataComponents.TECHNIQUE_ID.get());
            List<Integer> collected = stack.getOrDefault(
                    ModDataComponents.COLLECTED_PAGE_INDICES.get(), new ArrayList<>());

            if (id != null) {
                ResourceLocation rId = ResourceLocation.parse(id);
                var manualData = TechniqueManualRegistry.get(rId);
                if (manualData.isPresent()) {
                    List<String> chapters = manualData.get().chapterTranslationKeys();
                    for (int i = 0; i < chapters.size(); i++) {
                        boolean hasPage = collected.contains(i);
                        ChatFormatting color = hasPage ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY;
                        String symbol = hasPage ? "✓" : "✗";
                        lines.add(Component.literal("  " + symbol + " ")
                                .append(Component.translatable(chapters.get(i)))
                                .withStyle(color));
                    }
                }
            }

        } else if (stack.getItem() instanceof TechniquePageItem) {
            String id = stack.get(ModDataComponents.TECHNIQUE_ID.get());
            Integer pageIndex = stack.get(ModDataComponents.PAGE_INDEX.get());
            if (id != null && pageIndex != null) {
                ResourceLocation rId = ResourceLocation.parse(id);
                var manualData = TechniqueManualRegistry.get(rId);
                if (manualData.isPresent()) {
                    List<String> chapters = manualData.get().chapterTranslationKeys();
                    if (pageIndex >= 0 && pageIndex < chapters.size()) {
                        lines.add(Component.translatable(chapters.get(pageIndex)).withStyle(ChatFormatting.GRAY));
                    }
                }
            }
        }

        return lines;
    }
}