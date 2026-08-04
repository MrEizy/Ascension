package net.zic.ascension.client.visual.runtime;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;

public final class GuardianDharmaVisualController implements RuntimeVisualController {
    private static final Identifier TEXTURE = AscensionCraft.prefix("textures/block/guardian_dharma.png");
    private static final double TEXTURE_SIZE = 48.0D;
    private static final double MODEL_SCALE = 1.35D;
    private static final Element[] ELEMENTS = new Element[]{
        element(
                point(8.0D, -8.0D, 8.0D),
                point(0.0D, 0.0D, 0.0D),
                new Face[]{
                        face(new Vec3[]{point(12.0D, 4.0D, 6.0D), point(4.0D, 4.0D, 6.0D), point(4.0D, 16.0D, 6.0D), point(12.0D, 16.0D, 6.0D)}, new Uv[]{uv(1.333330D, 10.666670D), uv(4.0D, 10.666670D), uv(4.0D, 6.666670D), uv(1.333330D, 6.666670D)}, point(0.0D, 0.0D, -1.0D)),
                        face(new Vec3[]{point(12.0D, 4.0D, 10.0D), point(12.0D, 4.0D, 6.0D), point(12.0D, 16.0D, 6.0D), point(12.0D, 16.0D, 10.0D)}, new Uv[]{uv(0.0D, 10.666670D), uv(1.333330D, 10.666670D), uv(1.333330D, 6.666670D), uv(0.0D, 6.666670D)}, point(1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(4.0D, 4.0D, 10.0D), point(12.0D, 4.0D, 10.0D), point(12.0D, 16.0D, 10.0D), point(4.0D, 16.0D, 10.0D)}, new Uv[]{uv(5.333330D, 10.666670D), uv(8.0D, 10.666670D), uv(8.0D, 6.666670D), uv(5.333330D, 6.666670D)}, point(0.0D, 0.0D, 1.0D)),
                        face(new Vec3[]{point(4.0D, 4.0D, 6.0D), point(4.0D, 4.0D, 10.0D), point(4.0D, 16.0D, 10.0D), point(4.0D, 16.0D, 6.0D)}, new Uv[]{uv(4.0D, 10.666670D), uv(5.333330D, 10.666670D), uv(5.333330D, 6.666670D), uv(4.0D, 6.666670D)}, point(-1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(4.0D, 16.0D, 10.0D), point(12.0D, 16.0D, 10.0D), point(12.0D, 16.0D, 6.0D), point(4.0D, 16.0D, 6.0D)}, new Uv[]{uv(4.0D, 5.333330D), uv(1.333330D, 5.333330D), uv(1.333330D, 6.666670D), uv(4.0D, 6.666670D)}, point(0.0D, 1.0D, 0.0D)),
                        face(new Vec3[]{point(4.0D, 4.0D, 6.0D), point(12.0D, 4.0D, 6.0D), point(12.0D, 4.0D, 10.0D), point(4.0D, 4.0D, 10.0D)}, new Uv[]{uv(6.666670D, 6.666670D), uv(4.0D, 6.666670D), uv(4.0D, 5.333330D), uv(6.666670D, 5.333330D)}, point(0.0D, -1.0D, 0.0D))
                }
        ),
        element(
                point(8.0D, 19.500000D, 6.500000D),
                point(-17.500000D, 0.0D, 0.0D),
                new Face[]{
                        face(new Vec3[]{point(12.0D, 15.500000D, 2.500000D), point(4.0D, 15.500000D, 2.500000D), point(4.0D, 23.500000D, 2.500000D), point(12.0D, 23.500000D, 2.500000D)}, new Uv[]{uv(2.666670D, 5.333330D), uv(5.333330D, 5.333330D), uv(5.333330D, 2.666670D), uv(2.666670D, 2.666670D)}, point(0.0D, 0.0D, -1.0D)),
                        face(new Vec3[]{point(12.0D, 15.500000D, 10.500000D), point(12.0D, 15.500000D, 2.500000D), point(12.0D, 23.500000D, 2.500000D), point(12.0D, 23.500000D, 10.500000D)}, new Uv[]{uv(0.0D, 5.333330D), uv(2.666670D, 5.333330D), uv(2.666670D, 2.666670D), uv(0.0D, 2.666670D)}, point(1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(4.0D, 15.500000D, 10.500000D), point(12.0D, 15.500000D, 10.500000D), point(12.0D, 23.500000D, 10.500000D), point(4.0D, 23.500000D, 10.500000D)}, new Uv[]{uv(8.0D, 5.333330D), uv(10.666670D, 5.333330D), uv(10.666670D, 2.666670D), uv(8.0D, 2.666670D)}, point(0.0D, 0.0D, 1.0D)),
                        face(new Vec3[]{point(4.0D, 15.500000D, 2.500000D), point(4.0D, 15.500000D, 10.500000D), point(4.0D, 23.500000D, 10.500000D), point(4.0D, 23.500000D, 2.500000D)}, new Uv[]{uv(5.333330D, 5.333330D), uv(8.0D, 5.333330D), uv(8.0D, 2.666670D), uv(5.333330D, 2.666670D)}, point(-1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(4.0D, 23.500000D, 10.500000D), point(12.0D, 23.500000D, 10.500000D), point(12.0D, 23.500000D, 2.500000D), point(4.0D, 23.500000D, 2.500000D)}, new Uv[]{uv(5.333330D, 0.0D), uv(2.666670D, 0.0D), uv(2.666670D, 2.666670D), uv(5.333330D, 2.666670D)}, point(0.0D, 1.0D, 0.0D)),
                        face(new Vec3[]{point(4.0D, 15.500000D, 2.500000D), point(12.0D, 15.500000D, 2.500000D), point(12.0D, 15.500000D, 10.500000D), point(4.0D, 15.500000D, 10.500000D)}, new Uv[]{uv(8.0D, 2.666670D), uv(5.333330D, 2.666670D), uv(5.333330D, 0.0D), uv(8.0D, 0.0D)}, point(0.0D, -1.0D, 0.0D))
                }
        ),
        element(
                point(4.0D, 19.0D, 6.800000D),
                point(-17.500000D, 0.0D, 0.0D),
                new Face[]{
                        face(new Vec3[]{point(4.0D, 18.400000D, 5.800000D), point(3.0D, 18.400000D, 5.800000D), point(3.0D, 21.600000D, 5.800000D), point(4.0D, 21.600000D, 5.800000D)}, new Uv[]{uv(3.666670D, 5.0D), uv(4.0D, 5.0D), uv(4.0D, 4.0D), uv(3.666670D, 4.0D)}, point(0.0D, 0.0D, -1.0D)),
                        face(new Vec3[]{point(4.0D, 18.400000D, 7.800000D), point(4.0D, 18.400000D, 5.800000D), point(4.0D, 21.600000D, 5.800000D), point(4.0D, 21.600000D, 7.800000D)}, new Uv[]{uv(3.0D, 5.0D), uv(3.666670D, 5.0D), uv(3.666670D, 4.0D), uv(3.0D, 4.0D)}, point(1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(3.0D, 18.400000D, 7.800000D), point(4.0D, 18.400000D, 7.800000D), point(4.0D, 21.600000D, 7.800000D), point(3.0D, 21.600000D, 7.800000D)}, new Uv[]{uv(4.666670D, 5.0D), uv(5.0D, 5.0D), uv(5.0D, 4.0D), uv(4.666670D, 4.0D)}, point(0.0D, 0.0D, 1.0D)),
                        face(new Vec3[]{point(3.0D, 18.400000D, 5.800000D), point(3.0D, 18.400000D, 7.800000D), point(3.0D, 21.600000D, 7.800000D), point(3.0D, 21.600000D, 5.800000D)}, new Uv[]{uv(4.0D, 5.0D), uv(4.666670D, 5.0D), uv(4.666670D, 4.0D), uv(4.0D, 4.0D)}, point(-1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(3.0D, 21.600000D, 7.800000D), point(4.0D, 21.600000D, 7.800000D), point(4.0D, 21.600000D, 5.800000D), point(3.0D, 21.600000D, 5.800000D)}, new Uv[]{uv(4.0D, 3.333330D), uv(3.666670D, 3.333330D), uv(3.666670D, 4.0D), uv(4.0D, 4.0D)}, point(0.0D, 1.0D, 0.0D)),
                        face(new Vec3[]{point(3.0D, 18.400000D, 5.800000D), point(4.0D, 18.400000D, 5.800000D), point(4.0D, 18.400000D, 7.800000D), point(3.0D, 18.400000D, 7.800000D)}, new Uv[]{uv(4.333330D, 4.0D), uv(4.0D, 4.0D), uv(4.0D, 3.333330D), uv(4.333330D, 3.333330D)}, point(0.0D, -1.0D, 0.0D))
                }
        ),
        element(
                point(13.0D, 19.0D, 6.800000D),
                point(-17.500000D, 0.0D, 0.0D),
                new Face[]{
                        face(new Vec3[]{point(13.0D, 18.400000D, 5.800000D), point(12.0D, 18.400000D, 5.800000D), point(12.0D, 21.600000D, 5.800000D), point(13.0D, 21.600000D, 5.800000D)}, new Uv[]{uv(6.333330D, 5.0D), uv(6.666670D, 5.0D), uv(6.666670D, 4.0D), uv(6.333330D, 4.0D)}, point(0.0D, 0.0D, -1.0D)),
                        face(new Vec3[]{point(13.0D, 18.400000D, 7.800000D), point(13.0D, 18.400000D, 5.800000D), point(13.0D, 21.600000D, 5.800000D), point(13.0D, 21.600000D, 7.800000D)}, new Uv[]{uv(5.666670D, 5.0D), uv(6.333330D, 5.0D), uv(6.333330D, 4.0D), uv(5.666670D, 4.0D)}, point(1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(12.0D, 18.400000D, 7.800000D), point(13.0D, 18.400000D, 7.800000D), point(13.0D, 21.600000D, 7.800000D), point(12.0D, 21.600000D, 7.800000D)}, new Uv[]{uv(7.333330D, 5.0D), uv(7.666670D, 5.0D), uv(7.666670D, 4.0D), uv(7.333330D, 4.0D)}, point(0.0D, 0.0D, 1.0D)),
                        face(new Vec3[]{point(12.0D, 18.400000D, 5.800000D), point(12.0D, 18.400000D, 7.800000D), point(12.0D, 21.600000D, 7.800000D), point(12.0D, 21.600000D, 5.800000D)}, new Uv[]{uv(6.666670D, 5.0D), uv(7.333330D, 5.0D), uv(7.333330D, 4.0D), uv(6.666670D, 4.0D)}, point(-1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(12.0D, 21.600000D, 7.800000D), point(13.0D, 21.600000D, 7.800000D), point(13.0D, 21.600000D, 5.800000D), point(12.0D, 21.600000D, 5.800000D)}, new Uv[]{uv(6.666670D, 3.333330D), uv(6.333330D, 3.333330D), uv(6.333330D, 4.0D), uv(6.666670D, 4.0D)}, point(0.0D, 1.0D, 0.0D)),
                        face(new Vec3[]{point(12.0D, 18.400000D, 5.800000D), point(13.0D, 18.400000D, 5.800000D), point(13.0D, 18.400000D, 7.800000D), point(12.0D, 18.400000D, 7.800000D)}, new Uv[]{uv(7.0D, 4.0D), uv(6.666670D, 4.0D), uv(6.666670D, 3.333330D), uv(7.0D, 3.333330D)}, point(0.0D, -1.0D, 0.0D))
                }
        ),
        element(
                point(12.0D, 15.0D, 9.0D),
                point(35.0D, 0.0D, 0.0D),
                new Face[]{
                        face(new Vec3[]{point(16.0D, 3.0D, 7.0D), point(12.0D, 3.0D, 7.0D), point(12.0D, 15.0D, 7.0D), point(16.0D, 15.0D, 7.0D)}, new Uv[]{uv(9.333330D, 10.666670D), uv(10.666670D, 10.666670D), uv(10.666670D, 6.666670D), uv(9.333330D, 6.666670D)}, point(0.0D, 0.0D, -1.0D)),
                        face(new Vec3[]{point(16.0D, 3.0D, 11.0D), point(16.0D, 3.0D, 7.0D), point(16.0D, 15.0D, 7.0D), point(16.0D, 15.0D, 11.0D)}, new Uv[]{uv(8.0D, 10.666670D), uv(9.333330D, 10.666670D), uv(9.333330D, 6.666670D), uv(8.0D, 6.666670D)}, point(1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(12.0D, 3.0D, 11.0D), point(16.0D, 3.0D, 11.0D), point(16.0D, 15.0D, 11.0D), point(12.0D, 15.0D, 11.0D)}, new Uv[]{uv(12.0D, 10.666670D), uv(13.333330D, 10.666670D), uv(13.333330D, 6.666670D), uv(12.0D, 6.666670D)}, point(0.0D, 0.0D, 1.0D)),
                        face(new Vec3[]{point(12.0D, 3.0D, 7.0D), point(12.0D, 3.0D, 11.0D), point(12.0D, 15.0D, 11.0D), point(12.0D, 15.0D, 7.0D)}, new Uv[]{uv(10.666670D, 10.666670D), uv(12.0D, 10.666670D), uv(12.0D, 6.666670D), uv(10.666670D, 6.666670D)}, point(-1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(12.0D, 15.0D, 11.0D), point(16.0D, 15.0D, 11.0D), point(16.0D, 15.0D, 7.0D), point(12.0D, 15.0D, 7.0D)}, new Uv[]{uv(10.666670D, 5.333330D), uv(9.333330D, 5.333330D), uv(9.333330D, 6.666670D), uv(10.666670D, 6.666670D)}, point(0.0D, 1.0D, 0.0D)),
                        face(new Vec3[]{point(12.0D, 3.0D, 7.0D), point(16.0D, 3.0D, 7.0D), point(16.0D, 3.0D, 11.0D), point(12.0D, 3.0D, 11.0D)}, new Uv[]{uv(12.0D, 6.666670D), uv(10.666670D, 6.666670D), uv(10.666670D, 5.333330D), uv(12.0D, 5.333330D)}, point(0.0D, -1.0D, 0.0D))
                }
        ),
        element(
                point(4.0D, 15.0D, 9.0D),
                point(35.0D, 0.0D, 0.0D),
                new Face[]{
                        face(new Vec3[]{point(4.0D, 3.0D, 7.0D), point(0.0D, 3.0D, 7.0D), point(0.0D, 15.0D, 7.0D), point(4.0D, 15.0D, 7.0D)}, new Uv[]{uv(1.333330D, 16.0D), uv(2.666670D, 16.0D), uv(2.666670D, 12.0D), uv(1.333330D, 12.0D)}, point(0.0D, 0.0D, -1.0D)),
                        face(new Vec3[]{point(4.0D, 3.0D, 11.0D), point(4.0D, 3.0D, 7.0D), point(4.0D, 15.0D, 7.0D), point(4.0D, 15.0D, 11.0D)}, new Uv[]{uv(0.0D, 16.0D), uv(1.333330D, 16.0D), uv(1.333330D, 12.0D), uv(0.0D, 12.0D)}, point(1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(0.0D, 3.0D, 11.0D), point(4.0D, 3.0D, 11.0D), point(4.0D, 15.0D, 11.0D), point(0.0D, 15.0D, 11.0D)}, new Uv[]{uv(4.0D, 16.0D), uv(5.333330D, 16.0D), uv(5.333330D, 12.0D), uv(4.0D, 12.0D)}, point(0.0D, 0.0D, 1.0D)),
                        face(new Vec3[]{point(0.0D, 3.0D, 7.0D), point(0.0D, 3.0D, 11.0D), point(0.0D, 15.0D, 11.0D), point(0.0D, 15.0D, 7.0D)}, new Uv[]{uv(2.666670D, 16.0D), uv(4.0D, 16.0D), uv(4.0D, 12.0D), uv(2.666670D, 12.0D)}, point(-1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(0.0D, 15.0D, 11.0D), point(4.0D, 15.0D, 11.0D), point(4.0D, 15.0D, 7.0D), point(0.0D, 15.0D, 7.0D)}, new Uv[]{uv(2.666670D, 10.666670D), uv(1.333330D, 10.666670D), uv(1.333330D, 12.0D), uv(2.666670D, 12.0D)}, point(0.0D, 1.0D, 0.0D)),
                        face(new Vec3[]{point(0.0D, 3.0D, 7.0D), point(4.0D, 3.0D, 7.0D), point(4.0D, 3.0D, 11.0D), point(0.0D, 3.0D, 11.0D)}, new Uv[]{uv(4.0D, 12.0D), uv(2.666670D, 12.0D), uv(2.666670D, 10.666670D), uv(4.0D, 10.666670D)}, point(0.0D, -1.0D, 0.0D))
                }
        ),
        element(
                point(14.0D, 4.0D, 8.0D),
                point(31.282230D, 9.297980D, -77.391490D),
                new Face[]{
                        face(new Vec3[]{point(15.900000D, -8.0D, 5.750000D), point(11.900000D, -8.0D, 5.750000D), point(11.900000D, 4.0D, 5.750000D), point(15.900000D, 4.0D, 5.750000D)}, new Uv[]{uv(12.0D, 5.333330D), uv(13.333330D, 5.333330D), uv(13.333330D, 1.333330D), uv(12.0D, 1.333330D)}, point(0.0D, 0.0D, -1.0D)),
                        face(new Vec3[]{point(15.900000D, -8.0D, 10.250000D), point(15.900000D, -8.0D, 5.750000D), point(15.900000D, 4.0D, 5.750000D), point(15.900000D, 4.0D, 10.250000D)}, new Uv[]{uv(10.666670D, 5.333330D), uv(12.0D, 5.333330D), uv(12.0D, 1.333330D), uv(10.666670D, 1.333330D)}, point(1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(11.900000D, -8.0D, 10.250000D), point(15.900000D, -8.0D, 10.250000D), point(15.900000D, 4.0D, 10.250000D), point(11.900000D, 4.0D, 10.250000D)}, new Uv[]{uv(14.666670D, 5.333330D), uv(16.0D, 5.333330D), uv(16.0D, 1.333330D), uv(14.666670D, 1.333330D)}, point(0.0D, 0.0D, 1.0D)),
                        face(new Vec3[]{point(11.900000D, -8.0D, 5.750000D), point(11.900000D, -8.0D, 10.250000D), point(11.900000D, 4.0D, 10.250000D), point(11.900000D, 4.0D, 5.750000D)}, new Uv[]{uv(13.333330D, 5.333330D), uv(14.666670D, 5.333330D), uv(14.666670D, 1.333330D), uv(13.333330D, 1.333330D)}, point(-1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(11.900000D, 4.0D, 10.250000D), point(15.900000D, 4.0D, 10.250000D), point(15.900000D, 4.0D, 5.750000D), point(11.900000D, 4.0D, 5.750000D)}, new Uv[]{uv(13.333330D, 0.0D), uv(12.0D, 0.0D), uv(12.0D, 1.333330D), uv(13.333330D, 1.333330D)}, point(0.0D, 1.0D, 0.0D)),
                        face(new Vec3[]{point(11.900000D, -8.0D, 5.750000D), point(15.900000D, -8.0D, 5.750000D), point(15.900000D, -8.0D, 10.250000D), point(11.900000D, -8.0D, 10.250000D)}, new Uv[]{uv(14.666670D, 1.333330D), uv(13.333330D, 1.333330D), uv(13.333330D, 0.0D), uv(14.666670D, 0.0D)}, point(0.0D, -1.0D, 0.0D))
                }
        ),
        element(
                point(2.250000D, 4.0D, 8.0D),
                point(31.282230D, -9.297980D, 77.391490D),
                new Face[]{
                        face(new Vec3[]{point(4.100000D, -8.0D, 5.750000D), point(0.100000D, -8.0D, 5.750000D), point(0.100000D, 4.0D, 5.750000D), point(4.100000D, 4.0D, 5.750000D)}, new Uv[]{uv(6.666670D, 16.0D), uv(8.0D, 16.0D), uv(8.0D, 12.0D), uv(6.666670D, 12.0D)}, point(0.0D, 0.0D, -1.0D)),
                        face(new Vec3[]{point(4.100000D, -8.0D, 10.250000D), point(4.100000D, -8.0D, 5.750000D), point(4.100000D, 4.0D, 5.750000D), point(4.100000D, 4.0D, 10.250000D)}, new Uv[]{uv(5.333330D, 16.0D), uv(6.666670D, 16.0D), uv(6.666670D, 12.0D), uv(5.333330D, 12.0D)}, point(1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(0.100000D, -8.0D, 10.250000D), point(4.100000D, -8.0D, 10.250000D), point(4.100000D, 4.0D, 10.250000D), point(0.100000D, 4.0D, 10.250000D)}, new Uv[]{uv(9.333330D, 16.0D), uv(10.666670D, 16.0D), uv(10.666670D, 12.0D), uv(9.333330D, 12.0D)}, point(0.0D, 0.0D, 1.0D)),
                        face(new Vec3[]{point(0.100000D, -8.0D, 5.750000D), point(0.100000D, -8.0D, 10.250000D), point(0.100000D, 4.0D, 10.250000D), point(0.100000D, 4.0D, 5.750000D)}, new Uv[]{uv(8.0D, 16.0D), uv(9.333330D, 16.0D), uv(9.333330D, 12.0D), uv(8.0D, 12.0D)}, point(-1.0D, 0.0D, 0.0D)),
                        face(new Vec3[]{point(0.100000D, 4.0D, 10.250000D), point(4.100000D, 4.0D, 10.250000D), point(4.100000D, 4.0D, 5.750000D), point(0.100000D, 4.0D, 5.750000D)}, new Uv[]{uv(8.0D, 10.666670D), uv(6.666670D, 10.666670D), uv(6.666670D, 12.0D), uv(8.0D, 12.0D)}, point(0.0D, 1.0D, 0.0D)),
                        face(new Vec3[]{point(0.100000D, -8.0D, 5.750000D), point(4.100000D, -8.0D, 5.750000D), point(4.100000D, -8.0D, 10.250000D), point(0.100000D, -8.0D, 10.250000D)}, new Uv[]{uv(9.333330D, 12.0D), uv(8.0D, 12.0D), uv(8.0D, 10.666670D), uv(9.333330D, 10.666670D)}, point(0.0D, -1.0D, 0.0D))
                }
        )
    };

    public static void registerDefault() {
        ClientRuntimeVisuals.register(
                AscensionCraft.prefix("guardian_dharma_idol"),
                new GuardianDharmaVisualController()
        );
    }

    @Override
    public void render(RuntimeVisualState state, RenderLevelStageEvent.AfterTranslucentFeatures event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        Vec3 origin = ClientRuntimeVisuals.position(state).subtract(camera);
        Player owner = state.ownerId() == null
                ? null
                : minecraft.level.getPlayerByUUID(state.ownerId());
        double yaw = owner == null ? 0.0D : Math.toRadians(-owner.getYRot());
        double time = minecraft.level.getGameTime() + (state.seed() & 63L);
        double pulse = 1.0D + Math.sin(time * 0.11D) * 0.018D;
        double hover = Math.sin(time * 0.075D) * 0.035D;
        float stability = Math.clamp(state.progress(), 0.0F, 1.0F);
        int red = 255;
        int green = Math.clamp(Math.round(150.0F + stability * 105.0F), 0, 255);
        int blue = Math.clamp(Math.round(55.0F + stability * 155.0F), 0, 255);
        int alpha = Math.clamp(Math.round(145.0F + stability * 95.0F), 80, 240);

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(RenderTypes.entityTranslucent(TEXTURE));
        PoseStack.Pose pose = event.getPoseStack().last();

        for (Element element : ELEMENTS) {
            for (Face face : element.faces()) {
                Vec3 normal = transformDirection(face.normal(), element.rotation(), yaw);
                for (int index = 0; index < 4; index++) {
                    Vec3 position = transformPoint(
                            face.vertices()[index],
                            element.origin(),
                            element.rotation(),
                            yaw,
                            pulse
                    ).add(origin.x, origin.y + hover, origin.z);
                    Uv uv = face.uvs()[index];
                    vertices.addVertex(pose, (float) position.x, (float) position.y, (float) position.z)
                            .setColor(red, green, blue, alpha)
                            .setUv((float) (uv.u() / TEXTURE_SIZE), (float) (uv.v() / TEXTURE_SIZE))
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(0x00F000F0)
                            .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
                }
            }
        }
        buffers.endLastBatch();
    }

    private static Vec3 transformPoint(Vec3 point, Vec3 rotationOrigin, Vec3 rotation, double yaw, double pulse) {
        Vec3 local = new Vec3((point.x - 8.0D) / 16.0D, point.y / 16.0D, (point.z - 8.0D) / 16.0D).scale(MODEL_SCALE * pulse);
        return rotateY(local, yaw);
    }

    private static Vec3 transformDirection(Vec3 direction, Vec3 rotation, double yaw) {
        return rotateY(direction, yaw).normalize();
    }

    private static Vec3 rotate(Vec3 value, Vec3 degrees) {
        Vec3 result = value;
        if (degrees.x != 0.0D) {
            double angle = Math.toRadians(degrees.x);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            result = new Vec3(result.x, result.y * cos - result.z * sin, result.y * sin + result.z * cos);
        }
        if (degrees.y != 0.0D) {
            result = rotateY(result, Math.toRadians(degrees.y));
        }
        if (degrees.z != 0.0D) {
            double angle = Math.toRadians(degrees.z);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            result = new Vec3(result.x * cos - result.y * sin, result.x * sin + result.y * cos, result.z);
        }
        return result;
    }

    private static Vec3 rotateY(Vec3 value, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(
                value.x * cos - value.z * sin,
                value.y,
                value.x * sin + value.z * cos
        );
    }

    private static Vec3 point(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    private static Uv uv(double u, double v) {
        return new Uv(u, v);
    }

    private static Face face(Vec3[] vertices, Uv[] uvs, Vec3 normal) {
        return new Face(vertices, uvs, normal);
    }

    private static Element element(Vec3 origin, Vec3 rotation, Face[] faces) {
        return new Element(origin, rotation, faces);
    }

    private record Element(Vec3 origin, Vec3 rotation, Face[] faces) {
    }

    private record Face(Vec3[] vertices, Uv[] uvs, Vec3 normal) {
    }

    private record Uv(double u, double v) {
    }
}
