package net.zic.ascension.common.gui.elements.skill_casting;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

import java.util.HashMap;
import java.util.Map;

final class SkillIconResolver {
    private static final Identifier FALLBACK = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/spells/icon/placeholder.png"
    );

    private static final Map<Identifier, Identifier> CACHE = new HashMap<>();

    private SkillIconResolver() {
    }

    static Identifier resolve(Identifier skillId) {
        if (skillId == null) {
            return FALLBACK;
        }

        return CACHE.computeIfAbsent(skillId, id -> {
            Identifier candidate = Identifier.fromNamespaceAndPath(
                    id.getNamespace(),
                    "textures/spells/icon/" + id.getPath() + ".png"
            );

            return Minecraft.getInstance()
                    .getResourceManager()
                    .getResource(candidate)
                    .isPresent()
                    ? candidate
                    : FALLBACK;
        });
    }
}
