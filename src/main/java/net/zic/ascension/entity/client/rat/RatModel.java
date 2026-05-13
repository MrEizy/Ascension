package net.zic.ascension.entity.client.rat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.entity.custom.TreasureRatEntity;

public class RatModel<T extends TreasureRatEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "treasure_rat"), "main");
    private final ModelPart body;
    private final ModelPart main_bod;
    private final ModelPart lower_body;
    private final ModelPart upper_body;
    private final ModelPart head;
    private final ModelPart face;
    private final ModelPart whisker;
    private final ModelPart fur;
    private final ModelPart right_front_leg;
    private final ModelPart left_front_leg;
    private final ModelPart tail;
    private final ModelPart end_tail;
    private final ModelPart right_back_leg;
    private final ModelPart left_back_leg;

    public RatModel(ModelPart root) {
        this.body = root.getChild("body");
        this.main_bod = this.body.getChild("main_bod");
        this.lower_body = this.main_bod.getChild("lower_body");
        this.upper_body = this.lower_body.getChild("upper_body");
        this.head = this.upper_body.getChild("head");
        this.face = this.head.getChild("face");
        this.whisker = this.face.getChild("whisker");
        this.fur = this.upper_body.getChild("fur");
        this.right_front_leg = this.upper_body.getChild("right_front_leg");
        this.left_front_leg = this.upper_body.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
        this.end_tail = this.tail.getChild("end_tail");
        this.right_back_leg = this.body.getChild("right_back_leg");
        this.left_back_leg = this.body.getChild("left_back_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 1.0F));

        PartDefinition main_bod = body.addOrReplaceChild("main_bod", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 5.0F));

        PartDefinition lower_body = main_bod.addOrReplaceChild("lower_body", CubeListBuilder.create().texOffs(19, 52).addBox(-3.0F, -5.0F, -5.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition upper_body = lower_body.addOrReplaceChild("upper_body", CubeListBuilder.create().texOffs(22, 41).addBox(-2.0F, -5.0F, -4.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.525F, -6.0F));

        PartDefinition head = upper_body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 34).addBox(-2.0F, -5.0F, -8.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(26, 29).addBox(-1.5F, -4.5F, -10.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(29, 26).addBox(-0.5F, -4.75F, -10.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.475F, 1.0F));

        PartDefinition face = head.addOrReplaceChild("face", CubeListBuilder.create().texOffs(22, 34).addBox(-2.375F, -4.575F, -7.575F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 34).addBox(1.375F, -4.575F, -7.575F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = face.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(35, 22).addBox(-1.0F, -2.0F, 1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2F, -4.5F, -6.5F, -0.2705F, -0.1745F, 0.0F));

        PartDefinition cube_r2 = face.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(25, 22).addBox(-1.0F, -2.0F, 1.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -4.5F, -6.5F, -0.2705F, 0.1745F, 0.0F));

        PartDefinition whisker = face.addOrReplaceChild("whisker", CubeListBuilder.create().texOffs(36, 29).addBox(1.0F, -4.0F, -9.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(37, 28).addBox(2.0F, -5.0F, -9.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(37, 30).addBox(2.0F, -3.0F, -9.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(24, 30).addBox(-3.0F, -3.0F, -9.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(24, 28).addBox(-3.0F, -5.0F, -9.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(25, 29).addBox(-2.0F, -4.0F, -9.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition fur = upper_body.addOrReplaceChild("fur", CubeListBuilder.create(), PartPose.offset(0.0F, -0.525F, 6.0F));

        PartDefinition fur_r1 = fur.addOrReplaceChild("fur_r1", CubeListBuilder.create().texOffs(55, 15).addBox(-1.0F, 0.0F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(51, 12).addBox(-1.0F, -1.0F, -1.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(51, 19).addBox(-1.0F, 1.0F, -1.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -3.475F, -7.175F, 0.0F, 0.1745F, 0.0F));

        PartDefinition fur_r2 = fur.addOrReplaceChild("fur_r2", CubeListBuilder.create().texOffs(51, 14).addBox(-1.0F, -3.0F, -1.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.15F, -1.475F, -9.125F, 0.0F, 0.1745F, 0.0F));

        PartDefinition fur_r3 = fur.addOrReplaceChild("fur_r3", CubeListBuilder.create().texOffs(51, 21).addBox(-1.0F, -3.0F, -1.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(55, 17).addBox(-1.0F, -2.0F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.525F, -0.475F, -6.9F, 0.0F, -0.1745F, 0.0F));

        PartDefinition fur_r4 = fur.addOrReplaceChild("fur_r4", CubeListBuilder.create().texOffs(51, 27).addBox(-1.0F, -3.0F, -1.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.175F, -1.475F, -8.85F, 0.0F, -0.1745F, 0.0F));

        PartDefinition fur_r5 = fur.addOrReplaceChild("fur_r5", CubeListBuilder.create().texOffs(31, 20).addBox(1.5F, 0.0F, -2.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(57, 31).addBox(0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.001F))
                .texOffs(40, 10).addBox(2.5F, 0.0F, -2.5F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-1.5F, -5.2779F, -5.5866F, 0.1833F, 0.0F, 0.0F));

        PartDefinition fur_r6 = fur.addOrReplaceChild("fur_r6", CubeListBuilder.create().texOffs(39, 6).addBox(-1.5F, 0.0F, -3.5F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-0.5F, -5.1029F, -6.5616F, 0.1833F, 0.0F, 0.0F));

        PartDefinition fur_r7 = fur.addOrReplaceChild("fur_r7", CubeListBuilder.create().texOffs(58, 4).addBox(2.5F, 0.0F, -3.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(3.95F, 1.4721F, 0.4634F, -0.1745F, 0.0087F, -1.5708F));

        PartDefinition fur_r8 = fur.addOrReplaceChild("fur_r8", CubeListBuilder.create().texOffs(58, 35).addBox(2.5F, 0.0F, -3.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(3.95F, -0.5279F, 0.4634F, -0.1745F, 0.0087F, -1.5708F));

        PartDefinition fur_r9 = fur.addOrReplaceChild("fur_r9", CubeListBuilder.create().texOffs(57, 10).addBox(2.5F, 0.0F, -3.5F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(3.95F, 0.4721F, 0.4634F, -0.1745F, 0.0087F, -1.5708F));

        PartDefinition fur_r10 = fur.addOrReplaceChild("fur_r10", CubeListBuilder.create().texOffs(57, 25).addBox(2.5F, 0.0F, -3.5F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(3.95F, -1.5279F, 0.4634F, -0.1745F, 0.0087F, -1.5708F));

        PartDefinition fur_r11 = fur.addOrReplaceChild("fur_r11", CubeListBuilder.create().texOffs(52, 43).addBox(-1.5F, 0.0F, -3.5F, 5.0F, 0.0F, 2.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(3.6F, -1.5279F, -1.4866F, -0.1745F, 0.0087F, -1.5708F));

        PartDefinition fur_r12 = fur.addOrReplaceChild("fur_r12", CubeListBuilder.create().texOffs(58, 29).addBox(-1.5F, 0.0F, -3.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-3.975F, -2.5279F, 0.4634F, 0.1745F, 0.0087F, -1.5708F));

        PartDefinition fur_r13 = fur.addOrReplaceChild("fur_r13", CubeListBuilder.create().texOffs(58, 8).addBox(-1.5F, 0.0F, -3.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-3.975F, -4.5279F, 0.4634F, 0.1745F, 0.0087F, -1.5708F));

        PartDefinition fur_r14 = fur.addOrReplaceChild("fur_r14", CubeListBuilder.create().texOffs(57, 22).addBox(-1.5F, 0.0F, -3.5F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-3.975F, -5.5279F, 0.4634F, 0.1745F, 0.0087F, -1.5708F));

        PartDefinition fur_r15 = fur.addOrReplaceChild("fur_r15", CubeListBuilder.create().texOffs(57, 13).addBox(-1.5F, 0.0F, -3.5F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-3.975F, -3.5279F, 0.4634F, 0.1745F, 0.0087F, -1.5708F));

        PartDefinition fur_r16 = fur.addOrReplaceChild("fur_r16", CubeListBuilder.create().texOffs(52, 41).addBox(-1.5F, 0.0F, -3.5F, 5.0F, 0.0F, 2.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-3.625F, -1.5279F, -1.4616F, 0.1745F, 0.0087F, -1.5708F));

        PartDefinition fur_r17 = fur.addOrReplaceChild("fur_r17", CubeListBuilder.create().texOffs(57, 19).addBox(1.5F, 0.0F, -3.5F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.001F))
                .texOffs(57, 16).addBox(3.5F, 0.0F, -3.5F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.001F))
                .texOffs(58, 6).addBox(2.5F, 0.0F, -3.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.001F))
                .texOffs(60, 18).addBox(4.5F, 0.0F, -3.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-3.5F, -5.9779F, 0.3884F, 0.1833F, 0.0F, 0.0F));

        PartDefinition fur_r18 = fur.addOrReplaceChild("fur_r18", CubeListBuilder.create().texOffs(39, 2).addBox(-2.5F, 0.0F, -3.5F, 6.0F, 0.0F, 2.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-0.5F, -5.6279F, -1.5616F, 0.1833F, 0.0F, 0.0F));

        PartDefinition right_front_leg = upper_body.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(41, 39).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(42, 35).addBox(-0.5F, 2.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -1.525F, -2.0F));

        PartDefinition left_front_leg = upper_body.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(13, 39).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(14, 35).addBox(-0.5F, 2.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -1.525F, -2.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(1, 16).addBox(-1.0F, -1.5F, 1.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 4.0F));

        PartDefinition cube_r3 = tail.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 22).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.6433F, 4.6375F, -0.1745F, 0.0F, 0.0F));

        PartDefinition end_tail = tail.addOrReplaceChild("end_tail", CubeListBuilder.create().texOffs(0, 29).addBox(-1.0F, 0.0F, 0.7F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.002F)), PartPose.offset(0.0F, -0.8356F, 6.5938F));

        PartDefinition right_back_leg = body.addOrReplaceChild("right_back_leg", CubeListBuilder.create().texOffs(44, 57).addBox(-2.0F, 1.0F, 0.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(52, 52).addBox(-1.5F, 4.0F, -1.25F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -4.0F, 4.0F));

        PartDefinition left_back_leg = body.addOrReplaceChild("left_back_leg", CubeListBuilder.create().texOffs(8, 57).addBox(0.0F, 0.0F, 0.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(1, 51).addBox(0.5F, 3.0F, -1.25F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -3.0F, 4.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(TreasureRatEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.applyHeadRotation(netHeadYaw, headPitch);

        this.animateWalk(RatAnimations.ANIM_RAT_WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
        this.animate(entity.idleAnimationState, RatAnimations.ANIM_RAT_IDLE, ageInTicks, 1f);

    }

    private void applyHeadRotation(float headYaw, float headPitch) {
        headYaw = Mth.clamp(headYaw, -30f, 30f);
        headPitch = Mth.clamp(headPitch, -25f, 45f);

        this.head.yRot = headYaw * ((float)Math.PI / 180f);
        this.head.xRot = headPitch * ((float)Math.PI / 180f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return body;
    }
}
