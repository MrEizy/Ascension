package net.zic.ascension.entity.client.form;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.entity.custom.form.PlayerBodyEntity;

public class PlayerBodyEntityRenderer extends LivingEntityRenderer<PlayerBodyEntity, PlayerModel<PlayerBodyEntity>> {
    public PlayerBodyEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(PlayerBodyEntity playerBodyEntity) {
        return  getSkin(playerBodyEntity);
    }
    private ResourceLocation getSkin(PlayerBodyEntity entity) {
        GameProfile profile = entity.getProfile();

        if (profile == null) {
            return DefaultPlayerSkin.getDefaultTexture();
        }

        Minecraft mc = Minecraft.getInstance();
        SkinManager skinManager = mc.getSkinManager();

        PlayerSkin textures =
                skinManager.getInsecureSkin(profile);
        return textures.texture();
    }
}
