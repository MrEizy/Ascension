//package net.thejadeproject.ascension.clients.renderer.skills;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.*;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.GameRenderer;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.phys.Vec3;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.api.distmarker.OnlyIn;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
//import net.thejadeproject.ascension.data_attachments.ModAttachments;
//import net.thejadeproject.ascension.data_attachments.attachments.PlayerInputStates;
//import net.thejadeproject.ascension.refactor_packages.handlers.player.InputHandler;
//import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
//import net.thejadeproject.ascension.refactor_packages.paths.PathData;
//import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
//import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
//import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.BloodfeastSoulRefiningTechnique;
//import org.joml.Matrix4f;
//
//
//@OnlyIn(Dist.CLIENT)
//public class BloodfeastAoeRenderer {
//
//    // ── Geometry ──────────────────────────────────────────────────────────────
//    private static final int   SEGMENTS    = 64;
//    private static final float RING_WIDTH  = 0.55f;
//    private static final float MIST_HEIGHT = 2.0f;
//
//    // ── Colour ────────────────────────────────────────────────────────────────
//    private static final float R = 0.75f;
//    private static final float G = 0.02f;
//    private static final float B = 0.04f;
//
//    // ── Alpha ─────────────────────────────────────────────────────────────────
//    private static final float RING_OUTER_ALPHA = 0.88f;
//    private static final float MIST_BASE_ALPHA  = 0.40f;
//
//    // ── Pulse ─────────────────────────────────────────────────────────────────
//    private static final float PULSE_SPEED     = 1.2f;
//    private static final float PULSE_AMPLITUDE = 0.12f;
//
//    private long  lastNanos  = System.nanoTime();
//    private float pulsePhase = 0.0f;
//
//    @SubscribeEvent
//    public void onRenderLevel(RenderLevelStageEvent event) {
//        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
//
//        Minecraft mc     = Minecraft.getInstance();
//        Player    player = mc.player;
//        if (player == null) return;
//
//
//        if (!InputHandler.state.contains(InputHandler.CAST_SKILL_KEY)) return;
//
//        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;
//        var entityData = player.getData(ModAttachments.ENTITY_DATA);
//
//        ResourceLocation skillIdentifier = AscensionRegistries.Skills.SKILL_REGISTRY.getKey(entityData.getSkillCastHandler().getCastingInstance().getCastableSkill());
//        if(!ModSkills.BLOODFEAST_BANQUET_SKILL.getKey().equals(skillIdentifier)){
//            return;
//        }
//
//
//        if (!entityData.hasSkill(ModSkills.BLOODFEAST_BANQUET_SKILL.getId())) return;
//
//        PathData pathData = entityData.getPathData(ModPaths.ESSENCE.getId());
//        if (pathData == null || pathData.getLastUsedTechnique() == null) return;
//
//        Object technique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(
//                pathData.getLastUsedTechnique()
//        );
//        if (!(technique instanceof BloodfeastSoulRefiningTechnique)) return;
//
//        // ── Radius from realm ─────────────────────────────────────────────────
//        float radius = BloodfeastSoulRefiningTechnique.getRangeForRealm(pathData.getMajorRealm());
//
//        // ── Pulse ─────────────────────────────────────────────────────────────
//        long  now  = System.nanoTime();
//        float dt   = (now - lastNanos) / 1_000_000_000.0f;
//        lastNanos  = now;
//        pulsePhase = (pulsePhase + PULSE_SPEED * dt) % ((float) (Math.PI * 2.0));
//        float pulse = (float) Math.sin(pulsePhase) * PULSE_AMPLITUDE;
//
//        // ── Camera-relative player feet ───────────────────────────────────────
//        Vec3  cam = mc.gameRenderer.getMainCamera().getPosition();
//        float ox  = (float) (player.getX() - cam.x);
//        float oy  = (float) (player.getY() - cam.y);
//        float oz  = (float) (player.getZ() - cam.z);
//
//        // ── Matrix ────────────────────────────────────────────────────────────
//        PoseStack ps = event.getPoseStack();
//        ps.pushPose();
//        ps.translate(ox, oy, oz);
//        Matrix4f mat = ps.last().pose();
//
//        // ── GL state ──────────────────────────────────────────────────────────
//        RenderSystem.setShader(GameRenderer::getPositionColorShader);
//        RenderSystem.enableBlend();
//        RenderSystem.blendFunc(
//                org.lwjgl.opengl.GL11.GL_SRC_ALPHA,
//                org.lwjgl.opengl.GL11.GL_ONE
//        );
//        RenderSystem.disableDepthTest();
//        RenderSystem.depthMask(false);
//        RenderSystem.disableCull();
//
//        Tesselator tes = Tesselator.getInstance();
//        drawFloorRing(tes, mat, radius, pulse);
//        drawMistColumn(tes, mat, radius, pulse);
//
//        // ── Restore ───────────────────────────────────────────────────────────
//        RenderSystem.enableCull();
//        RenderSystem.depthMask(true);
//        RenderSystem.enableDepthTest();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.disableBlend();
//
//        ps.popPose();
//    }
//
//    // ── Floor ring ────────────────────────────────────────────────────────────
//
//    private void drawFloorRing(Tesselator tes, Matrix4f mat, float radius, float pulse) {
//        float outerAlpha  = clamp(RING_OUTER_ALPHA + pulse);
//        float innerRadius = Math.max(0f, radius - RING_WIDTH);
//
//        BufferBuilder buf = tes.begin(
//                VertexFormat.Mode.TRIANGLE_STRIP,
//                DefaultVertexFormat.POSITION_COLOR
//        );
//
//        for (int i = 0; i <= SEGMENTS; i++) {
//            double angle = (2.0 * Math.PI * i) / SEGMENTS;
//            float  c     = (float) Math.cos(angle);
//            float  s     = (float) Math.sin(angle);
//
//            // inner — transparent so only the edge glows
//            buf.addVertex(mat, c * innerRadius, 0f, s * innerRadius)
//                    .setColor(R, G, B, 0f);
//
//            // outer — glowing edge
//            buf.addVertex(mat, c * radius, 0f, s * radius)
//                    .setColor(R, G, B, outerAlpha);
//        }
//
//        BufferUploader.drawWithShader(buf.build());
//    }
//
//    // ── Mist column ───────────────────────────────────────────────────────────
//
//    private void drawMistColumn(Tesselator tes, Matrix4f mat, float radius, float pulse) {
//        float bottomAlpha = clamp(MIST_BASE_ALPHA + pulse * 0.6f);
//
//        BufferBuilder buf = tes.begin(
//                VertexFormat.Mode.TRIANGLE_STRIP,
//                DefaultVertexFormat.POSITION_COLOR
//        );
//
//        for (int i = 0; i <= SEGMENTS; i++) {
//            double angle = (2.0 * Math.PI * i) / SEGMENTS;
//            float  c     = (float) Math.cos(angle);
//            float  s     = (float) Math.sin(angle);
//
//            // bottom — coloured
//            buf.addVertex(mat, c * radius, 0f, s * radius)
//                    .setColor(R, G, B, bottomAlpha);
//
//            // top — transparent, GPU lerps the smooth gradient
//            buf.addVertex(mat, c * radius, MIST_HEIGHT, s * radius)
//                    .setColor(R, G, B, 0f);
//        }
//
//        BufferUploader.drawWithShader(buf.build());
//    }
//
//    // ── Util ──────────────────────────────────────────────────────────────────
//
//    private static float clamp(float v) {
//        return Math.max(0f, Math.min(1f, v));
//    }
//}