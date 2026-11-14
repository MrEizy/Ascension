package net.zic.ascension.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingDefinition;

/**
 * The boxes shell looks are drawn on (QiCoatingLayer, WindRingsEffect): built in a part's local pixels, with u running round the
 * box and v along it for qi_coating.fsh, whole or split into pixels for a PATCHES gather.
 */
final class ShellGeometry {
    // How much of the shader's v range a box cap spans across its depth, so its pattern matches the faces next to it.
    static final float CAP_V_SPAN = 0.15F;

    private ShellGeometry() {
    }

    /**
     * One shell layer round an arm in `color`: from `reach` pixels up from the hand tip down to `overhang` past it.
     */
    static void emitShell(PoseStack.Pose pose, VertexConsumer buffer, float minX, float maxX, float styleOffset,
                                  QiCoatingDefinition.Layer layer, int color, Patches patches) {
        float topY = QiCoatingLayer.HAND_TIP_Y - layer.reach();
        float maxY = QiCoatingLayer.HAND_TIP_Y + layer.overhang();
        float minZ = QiCoatingLayer.ARM_MIN_Z - layer.inflate();
        float maxZ = QiCoatingLayer.ARM_MAX_Z + layer.inflate();
        // A square cap spans as much v across its depth as the sides do over the same distance, so square texels stay square on it.
        float capVSpan = layer.squareCap() ? (maxZ - minZ) / (maxY - topY) : CAP_V_SPAN;
        emitBox(pose, buffer, minX - layer.inflate(), maxX + layer.inflate(), topY, maxY, minZ, maxZ,
                styleOffset, color, false, capVSpan, patches);
    }

    /**
     * A PATCHES gather's landings round an arm (QiCoatingGather.landings) and how far the coating has grown: a shell drawn with
     * them is split into pixels, and only those round a landing the specks have reached are drawn.
     */
    record Patches(float[][] landings, float growth) {
    }

    static void emitBox(PoseStack.Pose pose, VertexConsumer buffer, float minX, float maxX, float minY, float maxY,
                        float minZ, float maxZ, float styleOffset, int color, boolean topCap, float capVSpan) {
        emitBox(pose, buffer, minX, maxX, minY, maxY, minZ, maxZ, styleOffset, color, topCap, capVSpan, null);
    }

    /**
     * Emits a box in part-local pixels, capped at maxY and optionally at minY; v runs from 0 at maxY (the hand end) to 1 at minY.
     *
     * @param capVSpan how much of the v range each cap spans across its depth
     * @param patches  if set, only the pixels round its landings are drawn
     */
    static void emitBox(PoseStack.Pose pose, VertexConsumer buffer, float minX, float maxX, float minY, float maxY,
                        float minZ, float maxZ, float styleOffset, int color, boolean topCap, float capVSpan, Patches patches) {
        float x0 = minX / 16.0F;
        float x1 = maxX / 16.0F;
        float z0 = minZ / 16.0F;
        float z1 = maxZ / 16.0F;
        float top = minY / 16.0F;
        float bottom = maxY / 16.0F;

        // u runs continuously around the perimeter so the shader's noise wraps without a seam. Each face gets an equal
        // quarter, so fract(u * 4) is the position across a face (exact for wide arms, slightly stretched on slim ones).
        float u1 = styleOffset + 0.25F;
        float u2 = styleOffset + 0.5F;
        float u3 = styleOffset + 0.75F;
        float u4 = styleOffset + 1.0F;

        if (patches == null) {
            side(pose, buffer, x0, z0, x1, z0, styleOffset, u1, top, bottom, color);
            side(pose, buffer, x1, z0, x1, z1, u1, u2, top, bottom, color);
            side(pose, buffer, x1, z1, x0, z1, u2, u3, top, bottom, color);
            side(pose, buffer, x0, z1, x0, z0, u3, u4, top, bottom, color);
        } else {
            float[][] cornersX = {{x0, x1}, {x1, x1}, {x1, x0}, {x0, x0}};
            float[][] cornersZ = {{z0, z0}, {z0, z1}, {z1, z1}, {z1, z0}};
            float[] us = {styleOffset, u1, u2, u3, u4};
            for (int face = 0; face < 4; face++) {
                float ax = cornersX[face][0], bx = cornersX[face][1], az = cornersZ[face][0], bz = cornersZ[face][1];
                patchedQuad(pose, buffer, patches, color,
                        new float[]{ax, top, az, us[face], 1.0F}, new float[]{bx, top, bz, us[face + 1], 1.0F},
                        new float[]{bx, bottom, bz, us[face + 1], 0.0F}, new float[]{ax, bottom, az, us[face], 0.0F});
            }
        }

        // Caps are mapped flat (u along x, v along z), carrying on from the first face's edge. Reusing the four corner u
        // values from the sides would make the two triangles of the quad interpolate differently and leave a V-shaped seam.
        // Cap over the maxY end (the fist, on arms).
        patchedQuad(pose, buffer, patches, color,
                new float[]{x0, bottom, z0, styleOffset, 0.0F}, new float[]{x1, bottom, z0, u1, 0.0F},
                new float[]{x1, bottom, z1, u1, capVSpan}, new float[]{x0, bottom, z1, styleOffset, capVSpan});

        if (topCap) {
            patchedQuad(pose, buffer, patches, color,
                    new float[]{x0, top, z0, styleOffset, 1.0F}, new float[]{x1, top, z0, u1, 1.0F},
                    new float[]{x1, top, z1, u1, 1.0F - capVSpan}, new float[]{x0, top, z1, styleOffset, 1.0F - capVSpan});
        }
    }

    /**
     * A quad from corners a, b, c, d ({x, y, z, u, v}, positions in blocks), whole, or with patches split into one cell per
     * pixel (a to b, and a to d) with just the cells round its landings drawn.
     */
    private static void patchedQuad(PoseStack.Pose pose, VertexConsumer buffer, Patches patches, int color,
                                    float[] a, float[] b, float[] c, float[] d) {
        if (patches == null) {
            vertex(pose, buffer, a[0], a[1], a[2], a[3], a[4], color);
            vertex(pose, buffer, b[0], b[1], b[2], b[3], b[4], color);
            vertex(pose, buffer, c[0], c[1], c[2], c[3], c[4], color);
            vertex(pose, buffer, d[0], d[1], d[2], d[3], d[4], color);
            return;
        }
        int cellsS = Math.max(1, Math.round(distance(a, b) * 16.0F));
        int cellsT = Math.max(1, Math.round(distance(a, d) * 16.0F));
        float[] point = new float[5];
        for (int i = 0; i < cellsS; i++) {
            for (int j = 0; j < cellsT; j++) {
                float s0 = (float) i / cellsS;
                float s1 = (float) (i + 1) / cellsS;
                float t0 = (float) j / cellsT;
                float t1 = (float) (j + 1) / cellsT;
                quadPoint(a, b, c, d, (s0 + s1) * 0.5F, (t0 + t1) * 0.5F, point);
                float x = point[0] * 16.0F;
                float y = point[1] * 16.0F;
                float z = point[2] * 16.0F;
                if (!QiCoatingGather.revealed(patches.landings(), patches.growth(), x, y, z, QiCoatingGather.pixelJitter(x, y, z))) {
                    continue;
                }
                quadPoint(a, b, c, d, s0, t0, point);
                vertex(pose, buffer, point[0], point[1], point[2], point[3], point[4], color);
                quadPoint(a, b, c, d, s1, t0, point);
                vertex(pose, buffer, point[0], point[1], point[2], point[3], point[4], color);
                quadPoint(a, b, c, d, s1, t1, point);
                vertex(pose, buffer, point[0], point[1], point[2], point[3], point[4], color);
                quadPoint(a, b, c, d, s0, t1, point);
                vertex(pose, buffer, point[0], point[1], point[2], point[3], point[4], color);
            }
        }
    }

    private static float distance(float[] from, float[] to) {
        float dx = to[0] - from[0];
        float dy = to[1] - from[1];
        float dz = to[2] - from[2];
        return Mth.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * The point s along a to b (and d to c) and t along a to d, every component blended.
     */
    static void quadPoint(float[] a, float[] b, float[] c, float[] d, float s, float t, float[] out) {
        for (int k = 0; k < 5; k++) {
            float near = a[k] + (b[k] - a[k]) * s;
            float far = d[k] + (c[k] - d[k]) * s;
            out[k] = near + (far - near) * t;
        }
    }

    /**
     * One upright face from (ax, az) to (bx, bz), between top and bottom (blocks), u from ua to ub and v from 1 at the top to 0.
     */
    static void side(PoseStack.Pose pose, VertexConsumer buffer,
                     float ax, float az, float bx, float bz, float ua, float ub,
                     float top, float bottom, int color) {
        vertex(pose, buffer, ax, top, az, ua, 1.0F, color);
        vertex(pose, buffer, bx, top, bz, ub, 1.0F, color);
        vertex(pose, buffer, bx, bottom, bz, ub, 0.0F, color);
        vertex(pose, buffer, ax, bottom, az, ua, 0.0F, color);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(color);
    }
}
