package net.zic.ascension.client.visual.runtime;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.client.renderer.ModRenderTypes;

public final class BarrierShellVisualController implements RuntimeVisualController {
    private final int red;
    private final int green;
    private final int blue;
    private final Shape shape;
    private final int damageStage;

    public BarrierShellVisualController(int red, int green, int blue, Shape shape, int damageStage) {
        this.red = Math.clamp(red, 0, 255);
        this.green = Math.clamp(green, 0, 255);
        this.blue = Math.clamp(blue, 0, 255);
        this.shape = shape;
        this.damageStage = Math.clamp(damageStage, 0, 2);
    }

    public static void registerDefaults() {
        ClientRuntimeVisuals.register(
                AscensionCraft.prefix("barrier_shell"),
                new BarrierShellVisualController(255, 218, 92, Shape.SPHERE, 0)
        );
        ClientRuntimeVisuals.register(
                AscensionCraft.prefix("barrier_shell_fractured"),
                new BarrierShellVisualController(255, 148, 58, Shape.SPHERE, 1)
        );
        ClientRuntimeVisuals.register(
                AscensionCraft.prefix("barrier_shell_critical"),
                new BarrierShellVisualController(255, 78, 46, Shape.SPHERE, 2)
        );
        ClientRuntimeVisuals.register(
                AscensionCraft.prefix("golden_bell_barrier"),
                new BarrierShellVisualController(255, 215, 78, Shape.BELL, 0)
        );
        ClientRuntimeVisuals.register(
                AscensionCraft.prefix("golden_bell_barrier_fractured"),
                new BarrierShellVisualController(255, 145, 48, Shape.BELL, 1)
        );
        ClientRuntimeVisuals.register(
                AscensionCraft.prefix("golden_bell_barrier_critical"),
                new BarrierShellVisualController(255, 72, 38, Shape.BELL, 2)
        );
    }

    @Override
    public void render(RuntimeVisualState state, RenderLevelStageEvent.AfterTranslucentFeatures event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        float partialTick = ClientRuntimeVisuals.partialTick();
        double radius = Math.max(0.1D, state.primaryValue());
        double height = Math.max(0.2D, state.secondaryValue());
        double time = minecraft.level.getGameTime() + partialTick + (state.seed() & 31L);
        double pulse = 1.0D + Math.sin(time * 0.12D) * 0.018D;
        double rotation = time * 0.008D;
        float durability = Math.clamp(state.progress(), 0.0F, 1.0F);
        int surfaceAlpha = Math.clamp(20 + Math.round(durability * 28.0F), 16, 48);
        int patternAlpha = Math.clamp(105 + Math.round(durability * 90.0F), 90, 195);

        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        Vec3 center = ClientRuntimeVisuals.position(state, partialTick)
                .add(0.0D, height * 0.5D, 0.0D)
                .subtract(camera);

        PoseStack.Pose pose = event.getPoseStack().last();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();

        VertexConsumer surface = buffers.getBuffer(RenderTypes.debugQuads());
        drawSurface(surface, pose, center, radius * pulse, height * 0.5D * pulse, rotation, surfaceAlpha, state.seed());
        drawSurface(
                surface,
                pose,
                center,
                radius * pulse * 0.965D,
                height * 0.5D * pulse * 0.965D,
                -rotation * 0.7D,
                Math.max(6, surfaceAlpha / 3),
                state.seed() + 17L
        );
        buffers.endLastBatch();

        VertexConsumer lines = buffers.getBuffer(ModRenderTypes.energyLines());
        drawPattern(
                lines,
                pose,
                center,
                radius * pulse * 1.006D,
                height * 0.5D * pulse * 1.006D,
                rotation,
                patternAlpha,
                state.seed()
        );
        buffers.endLastBatch();
    }

    private void drawSurface(
            VertexConsumer surface,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double halfHeight,
            double rotation,
            int alpha,
            long seed
    ) {
        int verticalSegments = shape == Shape.BELL ? 16 : 18;
        int radialSegments = 32;
        for (int vertical = 0; vertical < verticalSegments; vertical++) {
            double lower = vertical / (double) verticalSegments;
            double upper = (vertical + 1) / (double) verticalSegments;
            for (int radial = 0; radial < radialSegments; radial++) {
                double first = rotation + Math.PI * 2.0D * radial / radialSegments;
                double second = rotation + Math.PI * 2.0D * (radial + 1) / radialSegments;
                Vec3 lowerFirst = point(center, radius, halfHeight, lower, first);
                Vec3 lowerSecond = point(center, radius, halfHeight, lower, second);
                Vec3 upperSecond = point(center, radius, halfHeight, upper, second);
                Vec3 upperFirst = point(center, radius, halfHeight, upper, first);
                int shimmer = Math.floorMod((int) (seed + vertical * 7L + radial * 13L), 4);
                int cellAlpha = Math.clamp(alpha + shimmer * 2 - 3, 3, 255);
                int shade = 92 + shimmer * 2;
                quad(surface, pose, lowerFirst, lowerSecond, upperSecond, upperFirst, cellAlpha, shade);
            }
        }

        if (shape == Shape.BELL) {
            drawBellBase(surface, pose, center, radius, halfHeight, rotation, alpha);
        }
    }

    private void drawBellBase(
            VertexConsumer surface,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double halfHeight,
            double rotation,
            int alpha
    ) {
        int segments = 32;
        Vec3 baseCenter = center.add(0.0D, -halfHeight, 0.0D);
        for (int index = 0; index < segments; index++) {
            double first = rotation + Math.PI * 2.0D * index / segments;
            double second = rotation + Math.PI * 2.0D * (index + 1) / segments;
            Vec3 outerFirst = point(center, radius, halfHeight, 0.0D, first);
            Vec3 outerSecond = point(center, radius, halfHeight, 0.0D, second);
            quad(surface, pose, baseCenter, outerFirst, outerSecond, baseCenter, Math.max(8, alpha / 2), 96);
        }
    }

    private void drawPattern(
            VertexConsumer lines,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double halfHeight,
            double rotation,
            int alpha,
            long seed
    ) {
        if (shape == Shape.BELL) {
            drawBellPattern(lines, pose, center, radius, halfHeight, rotation, alpha, seed);
        } else {
            drawSpherePattern(lines, pose, center, radius, halfHeight, rotation, alpha, seed);
        }
        drawCracks(lines, pose, center, radius, halfHeight, rotation, alpha, seed);
    }

    private void drawSpherePattern(
            VertexConsumer lines,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double halfHeight,
            double rotation,
            int alpha,
            long seed
    ) {
        for (int ring = 1; ring <= 7; ring++) {
            drawSurfaceRing(lines, pose, center, radius, halfHeight, ring / 8.0D, rotation, 40, alpha, ring == 4 ? 2.1F : 1.25F);
        }
        for (int rib = 0; rib < 10; rib++) {
            drawRib(
                    lines,
                    pose,
                    center,
                    radius,
                    halfHeight,
                    rotation + Math.PI * 2.0D * rib / 10.0D,
                    36,
                    alpha,
                    1.15F
            );
        }
        drawSpiral(lines, pose, center, radius, halfHeight, rotation, 1.8D, 70, alpha / 2, 1.0F);
        drawSpiral(lines, pose, center, radius, halfHeight, rotation + Math.PI, -1.8D, 70, alpha / 2, 1.0F);
    }

    private void drawBellPattern(
            VertexConsumer lines,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double halfHeight,
            double rotation,
            int alpha,
            long seed
    ) {
        double[] rings = {0.04D, 0.12D, 0.26D, 0.44D, 0.63D, 0.79D, 0.91D};
        for (int index = 0; index < rings.length; index++) {
            float width = index <= 1 ? 2.4F : index == 4 ? 2.0F : 1.35F;
            drawSurfaceRing(lines, pose, center, radius, halfHeight, rings[index], rotation, 40, alpha, width);
        }
        for (int rib = 0; rib < 8; rib++) {
            drawRib(
                    lines,
                    pose,
                    center,
                    radius,
                    halfHeight,
                    rotation + Math.PI * 2.0D * rib / 8.0D,
                    34,
                    alpha,
                    1.25F
            );
        }
        drawSpiral(lines, pose, center, radius, halfHeight, rotation, 1.25D, 68, alpha / 2, 1.05F);
        drawSpiral(lines, pose, center, radius, halfHeight, rotation + Math.PI, -1.25D, 68, alpha / 2, 1.05F);
        for (int face = 0; face < 4; face++) {
            drawSeal(
                    lines,
                    pose,
                    center,
                    radius,
                    halfHeight,
                    rotation + Math.PI * 2.0D * face / 4.0D,
                    0.49D,
                    alpha,
                    1.65F
            );
        }
    }

    private void drawSurfaceRing(
            VertexConsumer lines,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double halfHeight,
            double vertical,
            double rotation,
            int segments,
            int alpha,
            float width
    ) {
        for (int index = 0; index < segments; index++) {
            double first = rotation + Math.PI * 2.0D * index / segments;
            double second = rotation + Math.PI * 2.0D * (index + 1) / segments;
            line(
                    lines,
                    pose,
                    point(center, radius, halfHeight, vertical, first),
                    point(center, radius, halfHeight, vertical, second),
                    alpha,
                    width
            );
        }
    }

    private void drawRib(
            VertexConsumer lines,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double halfHeight,
            double longitude,
            int segments,
            int alpha,
            float width
    ) {
        for (int index = 0; index < segments; index++) {
            double lower = index / (double) segments;
            double upper = (index + 1) / (double) segments;
            line(
                    lines,
                    pose,
                    point(center, radius, halfHeight, lower, longitude),
                    point(center, radius, halfHeight, upper, longitude),
                    alpha,
                    width
            );
        }
    }

    private void drawSpiral(
            VertexConsumer lines,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double halfHeight,
            double rotation,
            double turns,
            int segments,
            int alpha,
            float width
    ) {
        for (int index = 0; index < segments; index++) {
            double lower = 0.06D + 0.88D * index / segments;
            double upper = 0.06D + 0.88D * (index + 1) / segments;
            double first = rotation + turns * Math.PI * 2.0D * lower;
            double second = rotation + turns * Math.PI * 2.0D * upper;
            line(
                    lines,
                    pose,
                    point(center, radius, halfHeight, lower, first),
                    point(center, radius, halfHeight, upper, second),
                    alpha,
                    width
            );
        }
    }

    private void drawSeal(
            VertexConsumer lines,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double halfHeight,
            double longitude,
            double vertical,
            int alpha,
            float width
    ) {
        drawDiamond(lines, pose, center, radius, halfHeight, longitude, vertical, 0.12D, 0.18D, alpha, width);
        drawDiamond(lines, pose, center, radius, halfHeight, longitude, vertical, 0.065D, 0.095D, alpha, width);
        Vec3 left = point(center, radius, halfHeight, vertical, longitude - 0.18D);
        Vec3 right = point(center, radius, halfHeight, vertical, longitude + 0.18D);
        Vec3 top = point(center, radius, halfHeight, vertical + 0.12D, longitude);
        Vec3 bottom = point(center, radius, halfHeight, vertical - 0.12D, longitude);
        line(lines, pose, left, right, alpha, 1.1F);
        line(lines, pose, top, bottom, alpha, 1.1F);
    }

    private void drawDiamond(
            VertexConsumer lines,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double halfHeight,
            double longitude,
            double vertical,
            double verticalSize,
            double radialSize,
            int alpha,
            float width
    ) {
        Vec3 top = point(center, radius, halfHeight, vertical + verticalSize, longitude);
        Vec3 right = point(center, radius, halfHeight, vertical, longitude + radialSize);
        Vec3 bottom = point(center, radius, halfHeight, vertical - verticalSize, longitude);
        Vec3 left = point(center, radius, halfHeight, vertical, longitude - radialSize);
        line(lines, pose, top, right, alpha, width);
        line(lines, pose, right, bottom, alpha, width);
        line(lines, pose, bottom, left, alpha, width);
        line(lines, pose, left, top, alpha, width);
    }

    private void drawCracks(
            VertexConsumer lines,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double halfHeight,
            double rotation,
            int alpha,
            long seed
    ) {
        int cracks = damageStage * 3;
        for (int crack = 0; crack < cracks; crack++) {
            double longitude = rotation + Math.PI * 2.0D * Math.floorMod(seed + crack * 19L, 97L) / 97.0D;
            double vertical = 0.27D + 0.46D * Math.floorMod(seed + crack * 31L, 83L) / 83.0D;
            Vec3 start = point(center, radius, halfHeight, vertical + 0.12D, longitude - 0.04D);
            Vec3 middle = point(center, radius, halfHeight, vertical, longitude + 0.08D);
            Vec3 end = point(center, radius, halfHeight, vertical - 0.13D, longitude - 0.03D);
            line(lines, pose, start, middle, Math.min(255, alpha + 35), 2.2F);
            line(lines, pose, middle, end, Math.min(255, alpha + 35), 2.2F);
            if (damageStage > 1) {
                Vec3 branch = point(center, radius, halfHeight, vertical - 0.04D, longitude + 0.2D);
                line(lines, pose, middle, branch, Math.min(255, alpha + 35), 1.8F);
            }
        }
    }

    private Vec3 point(
            Vec3 center,
            double radius,
            double halfHeight,
            double vertical,
            double longitude
    ) {
        double clamped = Math.clamp(vertical, 0.0D, 1.0D);
        double y = center.y - halfHeight + clamped * halfHeight * 2.0D;
        double radial = shape == Shape.BELL
                ? radius * bellProfile(clamped)
                : radius * Math.sin(Math.PI * clamped);
        return new Vec3(
                center.x + Math.cos(longitude) * radial,
                y,
                center.z + Math.sin(longitude) * radial
        );
    }

    private double bellProfile(double vertical) {
        double[] profile = {1.08D, 1.06D, 1.01D, 0.96D, 0.90D, 0.82D, 0.70D, 0.54D, 0.34D, 0.08D};
        double scaled = vertical * (profile.length - 1);
        int lower = Math.min((int) Math.floor(scaled), profile.length - 2);
        double progress = scaled - lower;
        double eased = progress * progress * (3.0D - 2.0D * progress);
        return profile[lower] + (profile[lower + 1] - profile[lower]) * eased;
    }

    private void quad(
            VertexConsumer surface,
            PoseStack.Pose pose,
            Vec3 first,
            Vec3 second,
            Vec3 third,
            Vec3 fourth,
            int alpha,
            int shade
    ) {
        int shadedRed = red * shade / 100;
        int shadedGreen = green * shade / 100;
        int shadedBlue = blue * shade / 100;
        vertex(surface, pose, first, shadedRed, shadedGreen, shadedBlue, alpha);
        vertex(surface, pose, second, shadedRed, shadedGreen, shadedBlue, alpha);
        vertex(surface, pose, third, shadedRed, shadedGreen, shadedBlue, alpha);
        vertex(surface, pose, fourth, shadedRed, shadedGreen, shadedBlue, alpha);
        vertex(surface, pose, fourth, shadedRed, shadedGreen, shadedBlue, alpha);
        vertex(surface, pose, third, shadedRed, shadedGreen, shadedBlue, alpha);
        vertex(surface, pose, second, shadedRed, shadedGreen, shadedBlue, alpha);
        vertex(surface, pose, first, shadedRed, shadedGreen, shadedBlue, alpha);
    }

    private void vertex(
            VertexConsumer surface,
            PoseStack.Pose pose,
            Vec3 point,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        surface.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .setColor(red, green, blue, alpha);
    }

    private void line(VertexConsumer lines, PoseStack.Pose pose, Vec3 first, Vec3 second, int alpha, float width) {
        float nx = (float) (second.x - first.x);
        float ny = (float) (second.y - first.y);
        float nz = (float) (second.z - first.z);
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 0.0F) {
            nx /= length;
            ny /= length;
            nz /= length;
        }
        lines.addVertex(pose, (float) first.x, (float) first.y, (float) first.z)
                .setColor(red, green, blue, alpha)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(width);
        lines.addVertex(pose, (float) second.x, (float) second.y, (float) second.z)
                .setColor(red, green, blue, alpha)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(width);
    }

    public enum Shape {
        SPHERE,
        BELL
    }
}
