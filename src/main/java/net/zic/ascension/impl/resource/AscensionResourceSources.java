package net.zic.ascension.impl.resource;

import net.minecraft.world.entity.player.Player;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.resource.source.AscensionResourceSourceTags;
import net.zic.ascension.api.core.resource.source.ResourceSourceIdentity;
import net.zic.ascension.api.core.resource.source.SimpleResourceSourceIdentity;

public final class AscensionResourceSources {
    public static final ResourceSourceIdentity DIRECT = SimpleResourceSourceIdentity.of(AscensionCraft.prefix("direct"));
    public static final ResourceSourceIdentity TRAVEL = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("travel"),
            AscensionResourceSourceTags.MOVEMENT
    );
    public static final ResourceSourceIdentity SPRINTING = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("sprinting"),
            AscensionResourceSourceTags.MOVEMENT
    );
    public static final ResourceSourceIdentity JUMPING = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("jumping"),
            AscensionResourceSourceTags.MOVEMENT
    );
    public static final ResourceSourceIdentity SWIMMING = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("swimming"),
            AscensionResourceSourceTags.MOVEMENT
    );
    public static final ResourceSourceIdentity ELYTRA = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("elytra"),
            AscensionResourceSourceTags.MOVEMENT
    );
    public static final ResourceSourceIdentity CLIMBING = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("climbing"),
            AscensionResourceSourceTags.MOVEMENT
    );
    public static final ResourceSourceIdentity CRAWLING = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("crawling"),
            AscensionResourceSourceTags.MOVEMENT
    );
    public static final ResourceSourceIdentity ATTACKING = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("attacking"),
            AscensionResourceSourceTags.COMBAT
    );
    public static final ResourceSourceIdentity DAMAGE = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("damage"),
            AscensionResourceSourceTags.COMBAT
    );
    public static final ResourceSourceIdentity NATURAL_REGENERATION = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("natural_regeneration"),
            AscensionResourceSourceTags.REGENERATION,
            AscensionResourceSourceTags.SURVIVAL
    );
    public static final ResourceSourceIdentity SKILL_CASTING = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("skill_casting"),
            AscensionResourceSourceTags.SKILL
    );
    public static final ResourceSourceIdentity CULTIVATION = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("cultivation"),
            AscensionResourceSourceTags.CULTIVATION,
            AscensionResourceSourceTags.SKILL
    );
    public static final ResourceSourceIdentity ENVIRONMENTAL = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("environmental"),
            AscensionResourceSourceTags.ENVIRONMENTAL,
            AscensionResourceSourceTags.SURVIVAL
    );
    public static final ResourceSourceIdentity STARVATION = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("starvation"),
            AscensionResourceSourceTags.SURVIVAL
    );
    public static final ResourceSourceIdentity HOSTILE_HUNGER = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("hostile_hunger"),
            AscensionResourceSourceTags.SURVIVAL
    );
    public static final ResourceSourceIdentity UNCLASSIFIED = SimpleResourceSourceIdentity.of(
            AscensionCraft.prefix("unclassified")
    );

    private AscensionResourceSources() {
    }

    public static ResourceSourceIdentity movementSource(Player player) {
        if (player.isSwimming()) {
            return SWIMMING;
        }
        if (player.isFallFlying()) {
            return ELYTRA;
        }
        if (player.isSprinting()) {
            return SPRINTING;
        }
        return TRAVEL;
    }
}
