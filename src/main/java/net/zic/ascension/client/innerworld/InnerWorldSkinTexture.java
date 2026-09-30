package net.zic.ascension.client.innerworld;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

import java.lang.reflect.Method;

/**
 * Isolated on purpose: I don't have PlayerSkin's decompiled source for this build, and both
 * `.texture()` and `RenderType.entityTranslucent` came back wrong last round, so rather than
 * guess a third time I'm resolving the accessor once via reflection (tries the common candidate
 * names) and caching whichever one actually exists on your PlayerSkin class.
 *
 * If you paste PlayerSkin.java (or just tell me the real field/method name), I'll replace this
 * whole file with a direct, non-reflective call — reflection here is a stopgap, not the fix.
 */
final class InnerWorldSkinTexture {
    private InnerWorldSkinTexture() {
    }

    private static final String[] CANDIDATE_METHODS = {"texture", "getTexture", "mainTexture", "body"};
    private static Method resolved;
    private static boolean attempted;

    static Identifier resolve(PlayerSkin skin) {
        if (!attempted) {
            attempted = true;
            for (String name : CANDIDATE_METHODS) {
                try {
                    Method m = PlayerSkin.class.getMethod(name);
                    if (Identifier.class.isAssignableFrom(m.getReturnType())) {
                        resolved = m;
                        break;
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
        }
        if (resolved == null) {
            return null;
        }
        try {
            return (Identifier) resolved.invoke(skin);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}