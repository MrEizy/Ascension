package net.zic.ascension.client.innerworld;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;


final class InnerWorldSkinTexture {
    private InnerWorldSkinTexture() {
    }

    static Identifier resolve(PlayerSkin skin) {
        if (skin == null || skin.body() == null) {
            return null;
        }
        return skin.body().texturePath();
    }
}