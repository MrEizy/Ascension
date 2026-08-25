package net.zic.ascension.impl.resource;

import net.minecraft.world.entity.player.Player;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;

public final class AscensionResourceSources {
    public static final ResourceSourceIdentity DIRECT = ResourceSourceIdentity.of(AscensionCraft.prefix("direct"));
    public static final ResourceSourceIdentity TRAVEL = ResourceSourceIdentity.of(
            AscensionCraft.prefix("travel"),
            ResourceSourceIdentity.Tags.MOVEMENT
    );
    public static final ResourceSourceIdentity SPRINTING = ResourceSourceIdentity.of(
            AscensionCraft.prefix("sprinting"),
            ResourceSourceIdentity.Tags.MOVEMENT
    );
    public static final ResourceSourceIdentity JUMPING = ResourceSourceIdentity.of(
            AscensionCraft.prefix("jumping"),
            ResourceSourceIdentity.Tags.MOVEMENT
    );
    public static final ResourceSourceIdentity SWIMMING = ResourceSourceIdentity.of(
            AscensionCraft.prefix("swimming"),
            ResourceSourceIdentity.Tags.MOVEMENT
    );
    public static final ResourceSourceIdentity ELYTRA = ResourceSourceIdentity.of(
            AscensionCraft.prefix("elytra"),
            ResourceSourceIdentity.Tags.MOVEMENT
    );
    public static final ResourceSourceIdentity CLIMBING = ResourceSourceIdentity.of(
            AscensionCraft.prefix("climbing"),
            ResourceSourceIdentity.Tags.MOVEMENT
    );
    public static final ResourceSourceIdentity CRAWLING = ResourceSourceIdentity.of(
            AscensionCraft.prefix("crawling"),
            ResourceSourceIdentity.Tags.MOVEMENT
    );
    public static final ResourceSourceIdentity ATTACKING = ResourceSourceIdentity.of(
            AscensionCraft.prefix("attacking"),
            ResourceSourceIdentity.Tags.COMBAT
    );
    public static final ResourceSourceIdentity DAMAGE = ResourceSourceIdentity.of(
            AscensionCraft.prefix("damage"),
            ResourceSourceIdentity.Tags.COMBAT
    );
    public static final ResourceSourceIdentity NATURAL_REGENERATION = ResourceSourceIdentity.of(
            AscensionCraft.prefix("natural_regeneration"),
            ResourceSourceIdentity.Tags.REGENERATION,
            ResourceSourceIdentity.Tags.SURVIVAL
    );
    public static final ResourceSourceIdentity SKILL_CASTING = ResourceSourceIdentity.of(
            AscensionCraft.prefix("skill_casting"),
            ResourceSourceIdentity.Tags.SKILL
    );
    public static final ResourceSourceIdentity CULTIVATION = ResourceSourceIdentity.of(
            AscensionCraft.prefix("cultivation"),
            ResourceSourceIdentity.Tags.CULTIVATION,
            ResourceSourceIdentity.Tags.SKILL
    );
    public static final ResourceSourceIdentity BODY_CULTIVATION = ResourceSourceIdentity.of(
            AscensionCraft.prefix("body_cultivation"),
            ResourceSourceIdentity.Tags.CULTIVATION,
            ResourceSourceIdentity.Tags.SKILL
    );
    public static final ResourceSourceIdentity ENVIRONMENTAL = ResourceSourceIdentity.of(
            AscensionCraft.prefix("environmental"),
            ResourceSourceIdentity.Tags.ENVIRONMENTAL,
            ResourceSourceIdentity.Tags.SURVIVAL
    );
    public static final ResourceSourceIdentity STARVATION = ResourceSourceIdentity.of(
            AscensionCraft.prefix("starvation"),
            ResourceSourceIdentity.Tags.SURVIVAL
    );
    public static final ResourceSourceIdentity HOSTILE_HUNGER = ResourceSourceIdentity.of(
            AscensionCraft.prefix("hostile_hunger"),
            ResourceSourceIdentity.Tags.SURVIVAL
    );
    public static final ResourceSourceIdentity UNCLASSIFIED = ResourceSourceIdentity.of(
            AscensionCraft.prefix("unclassified")
    );

    private AscensionResourceSources() {
    }

    public static ResourceSourceIdentity movementSource(Player player) {
        if (player.isFallFlying()) {
            return ELYTRA;
        }
        if (player.isSwimming()) {
            return SWIMMING;
        }
        if (player.onClimbable()) {
            return CLIMBING;
        }
        if (player.isVisuallyCrawling()) {
            return CRAWLING;
        }
        if (player.isSprinting()) {
            return SPRINTING;
        }
        return TRAVEL;
    }
}
