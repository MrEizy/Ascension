package net.zic.ascension.api.core.resource.source;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

public final class AscensionResourceSourceTags {
    public static final Identifier MOVEMENT = AscensionCraft.prefix("movement");
    public static final Identifier COMBAT = AscensionCraft.prefix("combat");
    public static final Identifier SURVIVAL = AscensionCraft.prefix("survival");
    public static final Identifier REGENERATION = AscensionCraft.prefix("regeneration");
    public static final Identifier SKILL = AscensionCraft.prefix("skill");
    public static final Identifier CULTIVATION = AscensionCraft.prefix("cultivation");
    public static final Identifier ENVIRONMENTAL = AscensionCraft.prefix("environmental");

    private AscensionResourceSourceTags() {
    }
}
