package net.zic.ascension.client.visual.runtime;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.client.renderer.ModRenderTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DatapackRuntimeVisualController implements RuntimeVisualController {
    private static final Map<UUID, Deque<Vec3>> HISTORY = new HashMap<>();
    private static final Map<Identifier, RuntimeAssetModel> MODELS = new HashMap<>();
    private static final Map<Identifier, PrimitiveHandler> PRIMITIVES = new HashMap<>();

    static {
        registerBuiltIns();
    }

    public static void registerPrimitive(Identifier id, PrimitiveHandler handler) {
        if (id != null && handler != null) {
            PRIMITIVES.put(id, handler);
        }
    }

    @Override
    public void onSpawn(RuntimeVisualState state) {
        remember(state, historyLimit(state));
    }

    @Override
    public void onUpdate(RuntimeVisualState previous, RuntimeVisualState current) {
        remember(current, historyLimit(current));
    }

    @Override
    public void onRemove(RuntimeVisualState state) {
        HISTORY.remove(state.runtimeId());
    }

    @Override
    public void tick(RuntimeVisualState state) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || state.visual() == null) {
            return;
        }

        RuntimeVisualDefinition definition = definition(state);
        if (definition == null) {
            return;
        }

        remember(state, historyLimit(definition));
        tickDefinition(definition, state, minecraft.level, new HashSet<>());
    }

    @Override
    public void render(RuntimeVisualState state, RenderLevelStageEvent.AfterTranslucentFeatures event) {
        if (state.visual() == null) {
            return;
        }

        RuntimeVisualDefinition definition = definition(state);
        if (definition != null) {
            renderDefinition(definition, state, event, new HashSet<>());
        }
    }

    private void tickDefinition(
            RuntimeVisualDefinition definition,
            RuntimeVisualState state,
            ClientLevel level,
            Set<Identifier> visited
    ) {
        Identifier visual = state.visual();
        if (visual != null && !visited.add(visual)) {
            return;
        }

        try {
            double time = level.getGameTime() + (state.seed() & 1023L);
            for (RuntimeVisualDefinition.Element layer : definition.elements()) {
                PrimitiveHandler handler = PRIMITIVES.get(layer.type());
                if (handler != null) {
                    handler.tick(this, layer, state, level, time, visited);
                }
            }
        } finally {
            if (visual != null) {
                visited.remove(visual);
            }
        }
    }

    private void renderDefinition(
            RuntimeVisualDefinition definition,
            RuntimeVisualState state,
            RenderLevelStageEvent.AfterTranslucentFeatures event,
            Set<Identifier> visited
    ) {
        Identifier visual = state.visual();
        if (visual != null && !visited.add(visual)) {
            return;
        }

        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }

            double time = minecraft.level.getGameTime()
                    + ClientRuntimeVisuals.partialTick()
                    + (state.seed() & 1023L);

            for (RuntimeVisualDefinition.Element layer : definition.elements()) {
                renderLayer(layer, state, event, time, visited);
            }
        } finally {
            if (visual != null) {
                visited.remove(visual);
            }
        }
    }

    private void renderLayer(
            RuntimeVisualDefinition.Element layer,
            RuntimeVisualState state,
            RenderLevelStageEvent.AfterTranslucentFeatures event,
            double time,
            Set<Identifier> visited
    ) {
        PrimitiveHandler handler = PRIMITIVES.get(layer.type());
        if (handler != null) {
            handler.render(this, layer, state, event, time, visited);
        }
    }

    private void renderGeometry(
            Identifier primitive,
            RuntimeVisualDefinition.Element layer,
            RuntimeVisualState state,
            RenderLevelStageEvent.AfterTranslucentFeatures event,
            double time
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        float partialTick = ClientRuntimeVisuals.partialTick();
        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        PoseStack.Pose pose = event.getPoseStack().last();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        RuntimeVisualDefinition.VisualColor color = layer.appearance().resolved(state);
        double pulse = 1.0D + Math.sin(time * layer.motion().pulseSpeed()) * layer.motion().pulse();
        double scale = Math.max(0.0001D, layer.transform().scale().resolve(state, time) * state.scale() * pulse);
        double radius = Math.max(0.0D, layer.geometry().radius().resolve(state, time) * scale);
        double innerRadius = Math.max(0.0D, layer.geometry().innerRadius().resolve(state, time) * scale);
        double height = Math.max(0.0D, layer.geometry().height().resolve(state, time) * scale);
        double width = Math.max(0.0D, layer.geometry().width().resolve(state, time) * scale);
        double length = Math.max(0.0D, layer.geometry().length().resolve(state, time) * scale);
        double spin = layer.motion().spin() + state.spin();
        Vec3 rotation = layer.transform().rotation().add(0.0D, time * spin, 0.0D);
        List<Vec3> centers = centers(layer, state, time, partialTick);

        if (primitive.equals(RuntimeVisualDefinition.Types.SPRITE)) {
            for (Vec3 center : centers) {
                drawBillboard(
                        buffers,
                        pose,
                        center.subtract(camera),
                        camera.subtract(center),
                        width,
                        height,
                        color,
                        layer.resources().textureResource(time),
                        layer.transform().rotation().z + time * spin
                );
            }
        } else if (primitive.equals(RuntimeVisualDefinition.Types.MODEL)) {
            Identifier modelId = layer.resources().modelResource();
            if (modelId != null) {
                RuntimeAssetModel model = MODELS.computeIfAbsent(modelId, RuntimeAssetModel::new);
                for (Vec3 center : centers) {
                    model.render(
                            pose,
                            buffers,
                            center.subtract(camera),
                            rotation,
                            scale,
                            color.red(),
                            color.green(),
                            color.blue(),
                            color.alpha(),
                            0x00F000F0
                    );
                }
            }
        } else if (primitive.equals(RuntimeVisualDefinition.Types.RING)) {
            for (Vec3 center : centers) {
                drawRing(
                        buffers,
                        pose,
                        center.subtract(camera),
                        radius,
                        innerRadius,
                        layer.geometry().segments(),
                        rotation,
                        color,
                        layer.appearance()
                );
            }
        } else if (primitive.equals(RuntimeVisualDefinition.Types.SHELL)) {
            for (Vec3 center : centers) {
                drawShell(
                        buffers,
                        pose,
                        center.subtract(camera),
                        radius,
                        height,
                        layer.geometry().segments(),
                        rotation,
                        color,
                        layer.appearance()
                );
            }
        } else if (primitive.equals(RuntimeVisualDefinition.Types.BEAM)) {
            VertexConsumer lines = lineBuffer(buffers, layer.appearance().noDepth());
            drawBeams(lines, pose, state, camera, layer, length, color, partialTick);
        } else if (primitive.equals(RuntimeVisualDefinition.Types.ENERGY_BEAM)) {
            VertexConsumer surface = energySurfaceBuffer(buffers, layer.appearance().noDepth());
            drawEnergyBeams(
                    surface,
                    pose,
                    state,
                    camera,
                    layer,
                    width,
                    length,
                    layer.geometry().segments(),
                    color,
                    partialTick
            );
        } else if (primitive.equals(RuntimeVisualDefinition.Types.AURA)) {
            if (hideAuraInFirstPerson(state)) {
                return;
            }
            VertexConsumer surface = auraSurfaceBuffer(buffers, layer.appearance().noDepth());
            for (Vec3 center : centers) {
                drawAura(
                        surface,
                        pose,
                        center.subtract(camera),
                        radius,
                        height,
                        layer.geometry().count(),
                        layer.geometry().style(),
                        color,
                        time,
                        state.seed()
                );
            }
        } else if (primitive.equals(RuntimeVisualDefinition.Types.DECAL)) {
            for (Vec3 center : centers) {
                drawDecal(
                        buffers,
                        pose,
                        center.subtract(camera),
                        radius,
                        innerRadius,
                        layer.geometry().segments(),
                        rotation,
                        color,
                        layer,
                        time
                );
            }
        } else if (primitive.equals(RuntimeVisualDefinition.Types.TRAIL)) {
            VertexConsumer lines = lineBuffer(buffers, layer.appearance().noDepth());
            drawRibbon(
                    lines,
                    pose,
                    state,
                    camera,
                    color,
                    layer.appearance().lineWidth(),
                    layer.motion().history(),
                    partialTick
            );
        } else if (primitive.equals(RuntimeVisualDefinition.Types.AFTERIMAGE)) {
            drawAfterimages(
                    buffers,
                    pose,
                    state,
                    camera,
                    width,
                    height,
                    color,
                    layer.resources().textureResource(time),
                    layer.motion().history(),
                    partialTick
            );
        } else if (primitive.equals(RuntimeVisualDefinition.Types.ENTITY_OVERLAY)) {
            for (Vec3 center : centers) {
                drawEntityOverlay(
                        buffers,
                        pose,
                        center.subtract(camera),
                        radius,
                        height,
                        layer.geometry().segments(),
                        rotation,
                        color,
                        layer.appearance()
                );
            }
        }

        buffers.endLastBatch();
    }

    private void emitParticles(
            RuntimeVisualDefinition.Element layer,
            RuntimeVisualState state,
            ClientLevel level,
            double time
    ) {
        if (level.getGameTime() % layer.motion().interval() != 0L) {
            return;
        }

        Identifier particleId = layer.resources().particle().orElse(null);
        if (particleId == null) {
            return;
        }

        ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getValue(particleId);
        if (!(type instanceof SimpleParticleType particle)) {
            return;
        }

        RandomSource random = level.getRandom();
        double pulse = 1.0D + Math.sin(time * layer.motion().pulseSpeed()) * layer.motion().pulse();
        double scale = Math.max(0.0001D, layer.transform().scale().resolve(state, time) * state.scale() * pulse);
        double radius = Math.max(0.0D, layer.geometry().radius().resolve(state, time) * scale);
        double height = Math.max(0.0D, layer.geometry().height().resolve(state, time) * scale);
        double speed = Math.max(0.0D, layer.geometry().length().resolve(state, time) * scale);
        int count = Math.clamp(layer.geometry().count(), 1, 64);

        for (Vec3 center : centers(layer, state, time, 1.0F)) {
            for (int index = 0; index < count; index++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double distance = Math.sqrt(random.nextDouble()) * radius;
                double x = center.x + Math.cos(angle) * distance;
                double y = center.y + (random.nextDouble() - 0.5D) * height;
                double z = center.z + Math.sin(angle) * distance;
                double dx = (random.nextDouble() - 0.5D) * speed;
                double dy = (random.nextDouble() - 0.5D) * speed;
                double dz = (random.nextDouble() - 0.5D) * speed;
                level.addParticle(particle, x, y, z, dx, dy, dz);
            }
        }
    }

    private List<Vec3> centers(
            RuntimeVisualDefinition.Element layer,
            RuntimeVisualState state,
            double time,
            float partialTick
    ) {
        List<Vec3> values = new ArrayList<>();

        if (layer.position() == RuntimeVisualDefinition.PositionMode.EACH_POINT
                && !state.points().isEmpty()) {
            values.addAll(state.points());
        } else if (layer.position() == RuntimeVisualDefinition.PositionMode.OWNER) {
            Minecraft minecraft = Minecraft.getInstance();
            Player owner = state.ownerId() == null || minecraft.level == null ? null : minecraft.level.getPlayerByUUID(state.ownerId());

            values.add(owner == null ? ClientRuntimeVisuals.position(state, partialTick) : ClientRuntimeVisuals.interpolatedPosition(owner, partialTick).add(0.0D, owner.getBbHeight() * 0.5D, 0.0D));
        } else {
            values.add(ClientRuntimeVisuals.position(state, partialTick));
        }

        double bob = Math.sin(time * layer.motion().bobSpeed()) * layer.motion().bob();
        Vec3 offset = layer.transform().offset().add(0.0D, bob, 0.0D);
        return values.stream().map(value -> value.add(offset)).toList();
    }

    private void drawRing(
            MultiBufferSource.BufferSource buffers,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double innerRadius,
            int segments,
            Vec3 rotation,
            RuntimeVisualDefinition.VisualColor color,
            RuntimeVisualDefinition.Appearance appearance
    ) {
        if (appearance.filled() && radius > 0.0D) {
            VertexConsumer surface = buffers.getBuffer(RenderTypes.debugQuads());
            double inner = Math.clamp(innerRadius, 0.0D, radius);
            for (int index = 0; index < segments; index++) {
                double first = Math.PI * 2.0D * index / segments;
                double second = Math.PI * 2.0D * (index + 1) / segments;
                Vec3 outerFirst = transform(
                        new Vec3(Math.cos(first) * radius, 0.0D, Math.sin(first) * radius),
                        rotation
                ).add(center);
                Vec3 outerSecond = transform(
                        new Vec3(Math.cos(second) * radius, 0.0D, Math.sin(second) * radius),
                        rotation
                ).add(center);
                Vec3 innerSecond = transform(
                        new Vec3(Math.cos(second) * inner, 0.0D, Math.sin(second) * inner),
                        rotation
                ).add(center);
                Vec3 innerFirst = transform(
                        new Vec3(Math.cos(first) * inner, 0.0D, Math.sin(first) * inner),
                        rotation
                ).add(center);
                quad(surface, pose, outerFirst, outerSecond, innerSecond, innerFirst, color);
            }
            buffers.endLastBatch();
        }

        VertexConsumer lines = lineBuffer(buffers, appearance.noDepth());
        for (int index = 0; index < segments; index++) {
            double first = Math.PI * 2.0D * index / segments;
            double second = Math.PI * 2.0D * (index + 1) / segments;
            Vec3 start = transform(
                    new Vec3(Math.cos(first) * radius, 0.0D, Math.sin(first) * radius),
                    rotation
            ).add(center);
            Vec3 end = transform(
                    new Vec3(Math.cos(second) * radius, 0.0D, Math.sin(second) * radius),
                    rotation
            ).add(center);
            line(lines, pose, start, end, color, appearance.lineWidth());
        }
        buffers.endLastBatch();
    }

    private void drawShell(
            MultiBufferSource.BufferSource buffers,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double height,
            int segments,
            Vec3 rotation,
            RuntimeVisualDefinition.VisualColor color,
            RuntimeVisualDefinition.Appearance appearance
    ) {
        int verticalSegments = Math.max(6, segments / 2);

        if (appearance.filled()) {
            VertexConsumer surface = buffers.getBuffer(RenderTypes.debugQuads());
            for (int vertical = 0; vertical < verticalSegments; vertical++) {
                double lower = Math.PI * vertical / verticalSegments - Math.PI * 0.5D;
                double upper = Math.PI * (vertical + 1) / verticalSegments - Math.PI * 0.5D;
                for (int radial = 0; radial < segments; radial++) {
                    double first = Math.PI * 2.0D * radial / segments;
                    double second = Math.PI * 2.0D * (radial + 1) / segments;
                    Vec3 a = shellPoint(radius, height, lower, first, rotation).add(center);
                    Vec3 b = shellPoint(radius, height, lower, second, rotation).add(center);
                    Vec3 c = shellPoint(radius, height, upper, second, rotation).add(center);
                    Vec3 d = shellPoint(radius, height, upper, first, rotation).add(center);
                    quad(surface, pose, a, b, c, d, color);
                }
            }
            buffers.endLastBatch();
        }

        VertexConsumer lines = lineBuffer(buffers, appearance.noDepth());
        for (int vertical = 1; vertical < verticalSegments; vertical += 2) {
            double latitude = Math.PI * vertical / verticalSegments - Math.PI * 0.5D;
            for (int radial = 0; radial < segments; radial++) {
                double first = Math.PI * 2.0D * radial / segments;
                double second = Math.PI * 2.0D * (radial + 1) / segments;
                line(
                        lines,
                        pose,
                        shellPoint(radius, height, latitude, first, rotation).add(center),
                        shellPoint(radius, height, latitude, second, rotation).add(center),
                        color,
                        appearance.lineWidth()
                );
            }
        }

        for (int radial = 0; radial < segments; radial += Math.max(1, segments / 8)) {
            double longitude = Math.PI * 2.0D * radial / segments;
            for (int vertical = 0; vertical < verticalSegments; vertical++) {
                double lower = Math.PI * vertical / verticalSegments - Math.PI * 0.5D;
                double upper = Math.PI * (vertical + 1) / verticalSegments - Math.PI * 0.5D;
                line(
                        lines,
                        pose,
                        shellPoint(radius, height, lower, longitude, rotation).add(center),
                        shellPoint(radius, height, upper, longitude, rotation).add(center),
                        color,
                        appearance.lineWidth()
                );
            }
        }
    }

    private void drawBeams(
            VertexConsumer lines,
            PoseStack.Pose pose,
            RuntimeVisualState state,
            Vec3 camera,
            RuntimeVisualDefinition.Element layer,
            double length,
            RuntimeVisualDefinition.VisualColor color,
            float partialTick
    ) {
        Vec3 offset = layer.transform().offset();
        if (!state.links().isEmpty() && !state.points().isEmpty()) {
            for (RuntimeVisualState.Link link : state.links()) {
                if (link.from() >= state.points().size() || link.to() >= state.points().size()) {
                    continue;
                }
                line(
                        lines,
                        pose,
                        state.points().get(link.from()).add(offset).subtract(camera),
                        state.points().get(link.to()).add(offset).subtract(camera),
                        color,
                        layer.appearance().lineWidth()
                );
            }
            return;
        }

        Vec3 start = ClientRuntimeVisuals.position(state, partialTick).add(offset);
        Vec3 direction = state.offset().lengthSqr() <= 1.0E-8D
                ? new Vec3(0.0D, 1.0D, 0.0D)
                : state.offset().normalize();
        line(
                lines,
                pose,
                start.subtract(camera),
                start.add(direction.scale(length)).subtract(camera),
                color,
                layer.appearance().lineWidth()
        );
    }

    private void drawEnergyBeams(
            VertexConsumer surface,
            PoseStack.Pose pose,
            RuntimeVisualState state,
            Vec3 camera,
            RuntimeVisualDefinition.Element layer,
            double width,
            double length,
            int segments,
            RuntimeVisualDefinition.VisualColor color,
            float partialTick
    ) {
        Vec3 offset = layer.transform().offset();
        double radius = Math.max(0.005D, width * 0.5D);
        int radialSegments = Math.clamp(segments, 4, 32);

        if (!state.links().isEmpty() && !state.points().isEmpty()) {
            for (RuntimeVisualState.Link link : state.links()) {
                if (link.from() >= state.points().size() || link.to() >= state.points().size()) {
                    continue;
                }
                Vec3 start = state.points().get(link.from()).add(offset);
                Vec3 end = state.points().get(link.to()).add(offset);
                start = firstPersonBeamStart(state, start, end);
                drawEnergyBeamSegment(
                        surface,
                        pose,
                        start.subtract(camera),
                        end.subtract(camera),
                        radius,
                        radialSegments,
                        color
                );
            }
            return;
        }

        Vec3 start = ClientRuntimeVisuals.position(state, partialTick).add(offset);
        Vec3 direction = state.offset().lengthSqr() <= 1.0E-8D
                ? new Vec3(0.0D, 1.0D, 0.0D)
                : state.offset().normalize();
        Vec3 end = start.add(direction.scale(length));
        start = firstPersonBeamStart(state, start, end);
        drawEnergyBeamSegment(
                surface,
                pose,
                start.subtract(camera),
                end.subtract(camera),
                radius,
                radialSegments,
                color
        );
    }

    private void drawEnergyBeamSegment(
            VertexConsumer surface,
            PoseStack.Pose pose,
            Vec3 start,
            Vec3 end,
            double radius,
            int segments,
            RuntimeVisualDefinition.VisualColor color
    ) {
        Vec3 axis = end.subtract(start);
        if (axis.lengthSqr() <= 1.0E-8D) {
            return;
        }

        Vec3 direction = axis.normalize();
        Vec3 reference = Math.abs(direction.y) < 0.95D
                ? new Vec3(0.0D, 1.0D, 0.0D)
                : new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 right = direction.cross(reference).normalize();
        Vec3 up = right.cross(direction).normalize();

        for (int index = 0; index < segments; index++) {
            double firstAngle = Math.PI * 2.0D * index / segments;
            double secondAngle = Math.PI * 2.0D * (index + 1) / segments;
            Vec3 firstOffset = right.scale(Math.cos(firstAngle) * radius).add(up.scale(Math.sin(firstAngle) * radius));
            Vec3 secondOffset = right.scale(Math.cos(secondAngle) * radius).add(up.scale(Math.sin(secondAngle) * radius));
            energyQuad(
                    surface,
                    pose,
                    start.add(firstOffset),
                    end.add(firstOffset),
                    end.add(secondOffset),
                    start.add(secondOffset),
                    color
            );
        }

        RuntimeVisualDefinition.VisualColor cap = color.withAlpha(Math.min(255, Math.round(color.alpha() * 1.2F)));
        for (int index = 1; index < segments - 1; index++) {
            Vec3 first = right.scale(radius);
            double secondAngle = Math.PI * 2.0D * index / segments;
            double thirdAngle = Math.PI * 2.0D * (index + 1) / segments;
            Vec3 second = right.scale(Math.cos(secondAngle) * radius).add(up.scale(Math.sin(secondAngle) * radius));
            Vec3 third = right.scale(Math.cos(thirdAngle) * radius).add(up.scale(Math.sin(thirdAngle) * radius));
            energyQuad(surface, pose, start.add(first), start.add(second), start.add(third), start.add(third), cap);
            energyQuad(surface, pose, end.add(first), end.add(third), end.add(second), end.add(second), cap);
        }
    }

    private void drawAura(
            VertexConsumer surface,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double height,
            int count,
            RuntimeVisualDefinition.AuraStyle style,
            RuntimeVisualDefinition.VisualColor color,
            double time,
            long seed
    ) {
        int segments = Math.clamp(Math.max(12, count), 12, 48);
        int verticalSegments = style == RuntimeVisualDefinition.AuraStyle.MIST ? 9 : 7;
        double scroll = time * switch (style) {
            case FLAME -> 0.006D;
            case FLOWING -> 0.003D;
            case MIST -> 0.0018D;
            case STORM -> 0.009D;
        } % 1.0D;
        float styleOffset = style.ordinal() * 8.0F;

        for (int radial = 0; radial < segments; radial++) {
            double firstAngle = Math.PI * 2.0D * radial / segments;
            double secondAngle = Math.PI * 2.0D * (radial + 1) / segments;
            float firstU = styleOffset + (float) radial / segments + (float) scroll;
            float secondU = styleOffset + (float) (radial + 1) / segments + (float) scroll;

            for (int vertical = 0; vertical < verticalSegments; vertical++) {
                double lower = (double) vertical / verticalSegments;
                double upper = (double) (vertical + 1) / verticalSegments;
                Vec3 a = auraPoint(center, radius, height, firstAngle, lower, time, seed, style);
                Vec3 b = auraPoint(center, radius, height, secondAngle, lower, time, seed, style);
                Vec3 c = auraPoint(center, radius, height, secondAngle, upper, time, seed, style);
                Vec3 d = auraPoint(center, radius, height, firstAngle, upper, time, seed, style);

                auraVertex(surface, pose, a, firstU, (float) lower, color);
                auraVertex(surface, pose, b, secondU, (float) lower, color);
                auraVertex(surface, pose, c, secondU, (float) upper, color);
                auraVertex(surface, pose, d, firstU, (float) upper, color);
            }
        }
    }

    private Vec3 auraPoint(
            Vec3 center,
            double radius,
            double height,
            double angle,
            double vertical,
            double time,
            long seed,
            RuntimeVisualDefinition.AuraStyle style
    ) {
        double noise = unitNoise(seed, (int) Math.round(angle * 1000.0D));
        double phase = angle + noise * switch (style) {
            case FLAME -> 0.35D;
            case FLOWING -> 0.18D;
            case MIST -> 0.12D;
            case STORM -> 0.48D;
        };
        double angularWave = switch (style) {
            case FLAME -> Math.sin(phase * 3.0D + time * 0.075D) * 0.045D
                    + Math.sin(phase * 7.0D - time * 0.052D) * 0.028D;
            case FLOWING -> Math.sin(phase * 2.0D + time * 0.032D) * 0.025D
                    + Math.sin(phase * 4.0D - time * 0.021D) * 0.014D;
            case MIST -> Math.sin(phase * 2.0D + time * 0.018D) * 0.035D
                    + Math.sin(phase * 5.0D - time * 0.014D) * 0.018D;
            case STORM -> Math.sin(phase * 5.0D + time * 0.12D) * 0.065D
                    + Math.sin(phase * 11.0D - time * 0.095D) * 0.038D;
        };
        double upperFlicker = switch (style) {
            case FLAME -> Math.sin(phase * 5.0D + time * 0.11D) * 0.05D * vertical;
            case FLOWING -> Math.sin(phase * 3.0D + time * 0.045D) * 0.025D * vertical;
            case MIST -> Math.sin(phase * 2.0D + time * 0.025D) * 0.035D * vertical;
            case STORM -> Math.sin(phase * 9.0D + time * 0.17D) * 0.085D * vertical;
        };
        double shape = auraRadiusProfile(style, vertical);
        double currentRadius = radius * Math.max(0.02D, shape + angularWave + upperFlicker);
        double tipVariation = switch (style) {
            case FLAME -> 0.88D
                    + unitNoise(seed ^ 0x6A09E667F3BCC909L, (int) Math.round(angle * 1000.0D)) * 0.22D
                    + Math.sin(phase * 4.0D + time * 0.09D) * 0.035D;
            case FLOWING -> 0.94D
                    + unitNoise(seed ^ 0x6A09E667F3BCC909L, (int) Math.round(angle * 1000.0D)) * 0.10D
                    + Math.sin(phase * 2.0D + time * 0.035D) * 0.02D;
            case MIST -> 0.91D
                    + unitNoise(seed ^ 0x6A09E667F3BCC909L, (int) Math.round(angle * 1000.0D)) * 0.08D
                    + Math.sin(phase * 2.0D + time * 0.02D) * 0.025D;
            case STORM -> 0.82D
                    + unitNoise(seed ^ 0x6A09E667F3BCC909L, (int) Math.round(angle * 1000.0D)) * 0.30D
                    + Math.sin(phase * 7.0D + time * 0.15D) * 0.055D;
        };
        double y = center.y - height * 0.32D + height * vertical * tipVariation;
        double sway = Math.sin(time * switch (style) {
            case FLAME -> 0.045D;
            case FLOWING -> 0.026D;
            case MIST -> 0.018D;
            case STORM -> 0.075D;
        } + phase * 2.0D) * radius * switch (style) {
            case FLAME -> 0.035D;
            case FLOWING -> 0.055D;
            case MIST -> 0.075D;
            case STORM -> 0.05D;
        } * vertical;

        return new Vec3(
                center.x + Math.cos(angle) * currentRadius + Math.cos(angle + Math.PI * 0.5D) * sway,
                y,
                center.z + Math.sin(angle) * currentRadius + Math.sin(angle + Math.PI * 0.5D) * sway
        );
    }

    private double auraRadiusProfile(RuntimeVisualDefinition.AuraStyle style, double vertical) {
        return switch (style) {
            case FLAME -> {
                if (vertical < 0.12D) {
                    yield 0.56D + vertical / 0.12D * 0.44D;
                }
                if (vertical < 0.48D) {
                    yield 1.0D - (vertical - 0.12D) / 0.36D * 0.08D;
                }
                if (vertical < 0.76D) {
                    yield 0.92D - (vertical - 0.48D) / 0.28D * 0.32D;
                }
                yield 0.60D - (vertical - 0.76D) / 0.24D * 0.56D;
            }
            case FLOWING -> {
                if (vertical < 0.16D) {
                    yield 0.62D + vertical / 0.16D * 0.28D;
                }
                if (vertical < 0.62D) {
                    yield 0.90D - (vertical - 0.16D) / 0.46D * 0.12D;
                }
                yield 0.78D - (vertical - 0.62D) / 0.38D * 0.50D;
            }
            case MIST -> {
                if (vertical < 0.18D) {
                    yield 0.72D + vertical / 0.18D * 0.22D;
                }
                if (vertical < 0.72D) {
                    yield 0.94D - (vertical - 0.18D) / 0.54D * 0.10D;
                }
                yield 0.84D - (vertical - 0.72D) / 0.28D * 0.28D;
            }
            case STORM -> {
                if (vertical < 0.10D) {
                    yield 0.52D + vertical / 0.10D * 0.50D;
                }
                if (vertical < 0.42D) {
                    yield 1.02D - (vertical - 0.10D) / 0.32D * 0.08D;
                }
                if (vertical < 0.70D) {
                    yield 0.94D - (vertical - 0.42D) / 0.28D * 0.38D;
                }
                yield 0.56D - (vertical - 0.70D) / 0.30D * 0.54D;
            }
        };
    }

    private double unitNoise(long seed, int index) {
        long value = seed + 0x9E3779B97F4A7C15L * (index + 1L);
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return (value >>> 11) * 0x1.0p-53;
    }

    private void drawDecal(
            MultiBufferSource.BufferSource buffers,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double innerRadius,
            int segments,
            Vec3 rotation,
            RuntimeVisualDefinition.VisualColor color,
            RuntimeVisualDefinition.Element layer,
            double time
    ) {
        Identifier texture = layer.resources().textureResource(time);

        if (texture != null) {
            VertexConsumer textured = buffers.getBuffer(RenderTypes.entityTranslucent(texture));
            Vec3 right = transform(new Vec3(radius, 0.0D, 0.0D), rotation);
            Vec3 forward = transform(new Vec3(0.0D, 0.0D, radius), rotation);
            texturedQuad(
                    textured,
                    pose,
                    center.subtract(right).subtract(forward),
                    center.add(right).subtract(forward),
                    center.add(right).add(forward),
                    center.subtract(right).add(forward),
                    color,
                    new Vec3(0.0D, 1.0D, 0.0D)
            );
            return;
        }

        drawRing(buffers, pose, center, radius, innerRadius, segments, rotation, color, layer.appearance());
        VertexConsumer lines = lineBuffer(buffers, layer.appearance().noDepth());
        double inner = innerRadius > 0.0D ? innerRadius : radius * 0.35D;
        int spokes = Math.max(4, layer.geometry().count());

        for (int index = 0; index < spokes; index++) {
            double angle = Math.PI * 2.0D * index / spokes;
            Vec3 start = transform(
                    new Vec3(Math.cos(angle) * inner, 0.0D, Math.sin(angle) * inner),
                    rotation
            ).add(center);
            Vec3 end = transform(
                    new Vec3(Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius),
                    rotation
            ).add(center);
            line(lines, pose, start, end, color, layer.appearance().lineWidth());
        }
    }

    private void drawRibbon(
            VertexConsumer lines,
            PoseStack.Pose pose,
            RuntimeVisualState state,
            Vec3 camera,
            RuntimeVisualDefinition.VisualColor color,
            float width,
            int limit,
            float partialTick
    ) {
        List<Vec3> points = renderHistory(state, limit, partialTick);
        for (int index = 1; index < points.size(); index++) {
            float fraction = index / (float) points.size();
            RuntimeVisualDefinition.VisualColor faded = color.withAlpha(
                    Math.max(1, Math.round(color.alpha() * fraction))
            );
            line(
                    lines,
                    pose,
                    points.get(index - 1).subtract(camera),
                    points.get(index).subtract(camera),
                    faded,
                    width
            );
        }
    }

    private void drawAfterimages(
            MultiBufferSource.BufferSource buffers,
            PoseStack.Pose pose,
            RuntimeVisualState state,
            Vec3 camera,
            double width,
            double height,
            RuntimeVisualDefinition.VisualColor color,
            Identifier texture,
            int limit,
            float partialTick
    ) {
        List<Vec3> points = renderHistory(state, limit, partialTick);
        for (int index = 0; index < points.size(); index += 2) {
            float fraction = (index + 1) / (float) points.size();
            RuntimeVisualDefinition.VisualColor faded = color.withAlpha(
                    Math.max(1, Math.round(color.alpha() * fraction * 0.7F))
            );
            Vec3 center = points.get(index);
            drawBillboard(
                    buffers,
                    pose,
                    center.subtract(camera),
                    camera.subtract(center),
                    width,
                    height,
                    faded,
                    texture,
                    0.0D
            );
        }
    }

    private void drawEntityOverlay(
            MultiBufferSource.BufferSource buffers,
            PoseStack.Pose pose,
            Vec3 center,
            double radius,
            double height,
            int segments,
            Vec3 rotation,
            RuntimeVisualDefinition.VisualColor color,
            RuntimeVisualDefinition.Appearance appearance
    ) {
        RuntimeVisualDefinition.VisualColor softened = color.withAlpha(Math.max(1, color.alpha() / 2));
        drawShell(
                buffers,
                pose,
                center,
                Math.max(0.2D, radius),
                Math.max(0.4D, height),
                Math.max(8, segments),
                rotation,
                softened,
                appearance
        );
    }

    private void drawBillboard(
            MultiBufferSource.BufferSource buffers,
            PoseStack.Pose pose,
            Vec3 center,
            Vec3 toCamera,
            double width,
            double height,
            RuntimeVisualDefinition.VisualColor color,
            Identifier texture,
            double rollDegrees
    ) {
        Vec3 forward = toCamera.lengthSqr() <= 1.0E-8D
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : toCamera.normalize();
        Vec3 right = new Vec3(0.0D, 1.0D, 0.0D).cross(forward);
        if (right.lengthSqr() <= 1.0E-8D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

        Vec3 up = forward.cross(right).normalize();
        if (rollDegrees != 0.0D) {
            double angle = Math.toRadians(rollDegrees);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            Vec3 rolledRight = right.scale(cos).add(up.scale(sin));
            Vec3 rolledUp = up.scale(cos).subtract(right.scale(sin));
            right = rolledRight;
            up = rolledUp;
        }
        Vec3 halfRight = right.scale(width * 0.5D);
        Vec3 halfUp = up.scale(height * 0.5D);
        Vec3 a = center.subtract(halfRight).subtract(halfUp);
        Vec3 b = center.add(halfRight).subtract(halfUp);
        Vec3 c = center.add(halfRight).add(halfUp);
        Vec3 d = center.subtract(halfRight).add(halfUp);

        if (texture == null) {
            VertexConsumer surface = buffers.getBuffer(RenderTypes.debugQuads());
            quad(surface, pose, a, b, c, d, color);
        } else {
            VertexConsumer textured = buffers.getBuffer(RenderTypes.entityTranslucent(texture));
            texturedQuad(textured, pose, a, b, c, d, color, forward);
        }
    }

    private Vec3 shellPoint(
            double radius,
            double height,
            double latitude,
            double longitude,
            Vec3 rotation
    ) {
        double cos = Math.cos(latitude);
        return transform(
                new Vec3(
                        Math.cos(longitude) * cos * radius,
                        Math.sin(latitude) * height * 0.5D,
                        Math.sin(longitude) * cos * radius
                ),
                rotation
        );
    }

    private Vec3 transform(Vec3 value, Vec3 degrees) {
        Vec3 result = value;
        if (degrees.x != 0.0D) {
            double angle = Math.toRadians(degrees.x);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            result = new Vec3(
                    result.x,
                    result.y * cos - result.z * sin,
                    result.y * sin + result.z * cos
            );
        }
        if (degrees.y != 0.0D) {
            double angle = Math.toRadians(degrees.y);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            result = new Vec3(
                    result.x * cos - result.z * sin,
                    result.y,
                    result.x * sin + result.z * cos
            );
        }
        if (degrees.z != 0.0D) {
            double angle = Math.toRadians(degrees.z);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            result = new Vec3(
                    result.x * cos - result.y * sin,
                    result.x * sin + result.y * cos,
                    result.z
            );
        }
        return result;
    }

    private Vec3 firstPersonBeamStart(RuntimeVisualState state, Vec3 start, Vec3 end) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || state.ownerId() == null
                || !state.ownerId().equals(minecraft.player.getUUID())
                || !minecraft.options.getCameraType().isFirstPerson()) {
            return start;
        }
        Vec3 axis = end.subtract(start);
        double length = axis.length();
        if (length <= 1.0E-8D) {
            return start;
        }
        double offset = Math.min(length * 0.45D, Math.max(1.35D, state.primaryValue() * 2.8D));
        return start.add(axis.scale(offset / length));
    }

    private boolean hideAuraInFirstPerson(RuntimeVisualState state) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && state.ownerId() != null
                && state.ownerId().equals(minecraft.player.getUUID())
                && minecraft.options.getCameraType().isFirstPerson();
    }

    private VertexConsumer auraSurfaceBuffer(MultiBufferSource.BufferSource buffers, boolean noDepth) {
        return buffers.getBuffer(noDepth ? ModRenderTypes.auraSurfaceNoDepth() : ModRenderTypes.auraSurface());
    }

    private VertexConsumer energySurfaceBuffer(MultiBufferSource.BufferSource buffers, boolean noDepth) {
        return buffers.getBuffer(noDepth ? ModRenderTypes.energySurfaceNoDepth() : ModRenderTypes.energySurface());
    }

    private void energyQuad(
            VertexConsumer surface,
            PoseStack.Pose pose,
            Vec3 a,
            Vec3 b,
            Vec3 c,
            Vec3 d,
            RuntimeVisualDefinition.VisualColor color
    ) {
        vertex(surface, pose, a, color);
        vertex(surface, pose, b, color);
        vertex(surface, pose, c, color);
        vertex(surface, pose, d, color);
    }

    private void gradientEnergyQuad(
            VertexConsumer surface,
            PoseStack.Pose pose,
            Vec3 a,
            Vec3 b,
            Vec3 c,
            Vec3 d,
            RuntimeVisualDefinition.VisualColor lower,
            RuntimeVisualDefinition.VisualColor upper
    ) {
        vertex(surface, pose, a, lower);
        vertex(surface, pose, b, lower);
        vertex(surface, pose, c, upper);
        vertex(surface, pose, d, upper);
    }

    private VertexConsumer lineBuffer(MultiBufferSource.BufferSource buffers, boolean noDepth) {
        return buffers.getBuffer(noDepth ? ModRenderTypes.linesNoDepth() : ModRenderTypes.energyLines());
    }

    private void line(
            VertexConsumer lines,
            PoseStack.Pose pose,
            Vec3 first,
            Vec3 second,
            RuntimeVisualDefinition.VisualColor color,
            float width
    ) {
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
                .setColor(color.red(), color.green(), color.blue(), color.alpha())
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(width);
        lines.addVertex(pose, (float) second.x, (float) second.y, (float) second.z)
                .setColor(color.red(), color.green(), color.blue(), color.alpha())
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(width);
    }

    private void quad(
            VertexConsumer surface,
            PoseStack.Pose pose,
            Vec3 a,
            Vec3 b,
            Vec3 c,
            Vec3 d,
            RuntimeVisualDefinition.VisualColor color
    ) {
        vertex(surface, pose, a, color);
        vertex(surface, pose, b, color);
        vertex(surface, pose, c, color);
        vertex(surface, pose, d, color);
        vertex(surface, pose, d, color);
        vertex(surface, pose, c, color);
        vertex(surface, pose, b, color);
        vertex(surface, pose, a, color);
    }

    private void vertex(
            VertexConsumer surface,
            PoseStack.Pose pose,
            Vec3 point,
            RuntimeVisualDefinition.VisualColor color
    ) {
        surface.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .setColor(color.red(), color.green(), color.blue(), color.alpha());
    }

    private void texturedQuad(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            Vec3 a,
            Vec3 b,
            Vec3 c,
            Vec3 d,
            RuntimeVisualDefinition.VisualColor color,
            Vec3 normal
    ) {
        texturedVertex(vertices, pose, a, 0.0F, 1.0F, color, normal);
        texturedVertex(vertices, pose, b, 1.0F, 1.0F, color, normal);
        texturedVertex(vertices, pose, c, 1.0F, 0.0F, color, normal);
        texturedVertex(vertices, pose, d, 0.0F, 0.0F, color, normal);
    }

    private void auraVertex(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            Vec3 point,
            float u,
            float v,
            RuntimeVisualDefinition.VisualColor color
    ) {
        vertices.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .setColor(color.red(), color.green(), color.blue(), color.alpha())
                .setUv(u, v);
    }

    private void texturedVertex(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            Vec3 point,
            float u,
            float v,
            RuntimeVisualDefinition.VisualColor color,
            Vec3 normal
    ) {
        vertices.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .setColor(color.red(), color.green(), color.blue(), color.alpha())
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0x00F000F0)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    private void remember(RuntimeVisualState state, int limit) {
        Vec3 position = ClientRuntimeVisuals.position(state, 1.0F);
        Deque<Vec3> history = HISTORY.computeIfAbsent(state.runtimeId(), key -> new ArrayDeque<>());
        Vec3 last = history.peekLast();
        if (last == null || last.distanceToSqr(position) > 1.0E-5D) {
            history.addLast(position);
        }
        while (history.size() > Math.max(2, limit)) {
            history.removeFirst();
        }
    }

    private List<Vec3> history(RuntimeVisualState state, int limit) {
        Deque<Vec3> values = HISTORY.get(state.runtimeId());
        if (values == null || values.isEmpty()) {
            return List.of(ClientRuntimeVisuals.position(state, 1.0F));
        }

        List<Vec3> points = new ArrayList<>(values);
        if (points.size() <= limit) {
            return points;
        }
        return new ArrayList<>(points.subList(points.size() - limit, points.size()));
    }

    private List<Vec3> renderHistory(RuntimeVisualState state, int limit, float partialTick) {
        int resolvedLimit = Math.max(2, limit);
        List<Vec3> points = new ArrayList<>(history(state, resolvedLimit));
        Vec3 current = ClientRuntimeVisuals.position(state, partialTick);
        Vec3 last = points.isEmpty() ? null : points.getLast();

        if (last == null || last.distanceToSqr(current) > 1.0E-8D) {
            points.add(current);
        } else if (!points.isEmpty()) {
            points.set(points.size() - 1, current);
        }

        if (points.size() > resolvedLimit) {
            return new ArrayList<>(points.subList(points.size() - resolvedLimit, points.size()));
        }
        return points;
    }

    private int historyLimit(RuntimeVisualState state) {
        RuntimeVisualDefinition definition = definition(state);
        return definition == null ? 12 : historyLimit(definition);
    }

    private int historyLimit(RuntimeVisualDefinition definition) {
        return definition.elements().stream()
                .mapToInt(layer -> layer.motion().history())
                .max()
                .orElse(12);
    }

    private RuntimeVisualDefinition definition(RuntimeVisualState state) {
        if (state == null) {
            return null;
        }
        return state.definition() == null ? definition(state.visual()) : state.definition();
    }

    private RuntimeVisualDefinition definition(Identifier id) {
        Minecraft minecraft = Minecraft.getInstance();
        if (id == null || minecraft.level == null) {
            return null;
        }
        return CoreRegistries.safeAccess(
                CoreRegistries.RUNTIME_VISUAL_REGISTRY,
                id,
                minecraft.level.registryAccess()
        );
    }

    private RuntimeVisualState withVisual(RuntimeVisualState state, Identifier visual) {
        return new RuntimeVisualState(
                state.runtimeId(),
                visual,
                state.ownerId(),
                state.position(),
                state.offset(),
                state.points(),
                state.links(),
                state.expiresAt(),
                state.stage(),
                state.flags(),
                state.progress(),
                state.seed(),
                state.primaryValue(),
                state.secondaryValue(),
                state.scale(),
                state.spin(),
                state.tint(),
                state.secondaryTint(),
                null
        );
    }

    private static void registerBuiltIns() {
        registerPrimitive(RuntimeVisualDefinition.Types.PARTICLES, new PrimitiveHandler() {
            @Override
            public void tick(
                    DatapackRuntimeVisualController controller,
                    RuntimeVisualDefinition.Element layer,
                    RuntimeVisualState state,
                    ClientLevel level,
                    double time,
                    Set<Identifier> visited
            ) {
                controller.emitParticles(layer, state, level, time);
            }
        });

        registerPrimitive(RuntimeVisualDefinition.Types.COMPOSITE, new PrimitiveHandler() {
            @Override
            public void tick(
                    DatapackRuntimeVisualController controller,
                    RuntimeVisualDefinition.Element layer,
                    RuntimeVisualState state,
                    ClientLevel level,
                    double time,
                    Set<Identifier> visited
            ) {
                layer.resources().visual().ifPresent(id -> {
                    RuntimeVisualDefinition nested = controller.definition(id);
                    if (nested != null) {
                        controller.tickDefinition(nested, controller.withVisual(state, id), level, visited);
                    }
                });
            }

            @Override
            public void render(
                    DatapackRuntimeVisualController controller,
                    RuntimeVisualDefinition.Element layer,
                    RuntimeVisualState state,
                    RenderLevelStageEvent.AfterTranslucentFeatures event,
                    double time,
                    Set<Identifier> visited
            ) {
                layer.resources().visual().ifPresent(id -> {
                    RuntimeVisualDefinition nested = controller.definition(id);
                    if (nested != null) {
                        controller.renderDefinition(nested, controller.withVisual(state, id), event, visited);
                    }
                });
            }
        });

        registerPrimitive(RuntimeVisualDefinition.Types.CUSTOM, new PrimitiveHandler() {
            @Override
            public void render(
                    DatapackRuntimeVisualController controller,
                    RuntimeVisualDefinition.Element layer,
                    RuntimeVisualState state,
                    RenderLevelStageEvent.AfterTranslucentFeatures event,
                    double time,
                    Set<Identifier> visited
            ) {
                layer.resources().visual().ifPresent(id -> {
                    RuntimeVisualController visualController = ClientRuntimeVisuals.registeredController(id);
                    if (visualController != null && visualController != controller) {
                        visualController.render(controller.withVisual(state, id), event);
                    }
                });
            }
        });

        List<Identifier> geometry = List.of(
                RuntimeVisualDefinition.Types.SPRITE,
                RuntimeVisualDefinition.Types.MODEL,
                RuntimeVisualDefinition.Types.RING,
                RuntimeVisualDefinition.Types.SHELL,
                RuntimeVisualDefinition.Types.BEAM,
                RuntimeVisualDefinition.Types.ENERGY_BEAM,
                RuntimeVisualDefinition.Types.AURA,
                RuntimeVisualDefinition.Types.DECAL,
                RuntimeVisualDefinition.Types.TRAIL,
                RuntimeVisualDefinition.Types.AFTERIMAGE,
                RuntimeVisualDefinition.Types.ENTITY_OVERLAY
        );

        for (Identifier id : geometry) {
            registerPrimitive(id, new PrimitiveHandler() {
                @Override
                public void render(
                        DatapackRuntimeVisualController controller,
                        RuntimeVisualDefinition.Element layer,
                        RuntimeVisualState state,
                        RenderLevelStageEvent.AfterTranslucentFeatures event,
                        double time,
                        Set<Identifier> visited
                ) {
                    controller.renderGeometry(id, layer, state, event, time);
                }
            });
        }
    }

    public interface PrimitiveHandler {
        default void tick(
                DatapackRuntimeVisualController controller,
                RuntimeVisualDefinition.Element layer,
                RuntimeVisualState state,
                ClientLevel level,
                double time,
                Set<Identifier> visited
        ) {
        }

        default void render(
                DatapackRuntimeVisualController controller,
                RuntimeVisualDefinition.Element layer,
                RuntimeVisualState state,
                RenderLevelStageEvent.AfterTranslucentFeatures event,
                double time,
                Set<Identifier> visited
        ) {
        }
    }
}
