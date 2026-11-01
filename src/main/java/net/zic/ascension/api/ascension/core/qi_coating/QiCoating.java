package net.zic.ascension.api.ascension.core.qi_coating;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;

/**
 * A loaded Qi Coating: its registry id and definition. Two are the same coating when their ids match, so a datapack reload that
 * changes a coating's definition doesn't make it count as a different coating (its grow-in doesn't restart).
 */
public record QiCoating(Identifier id, QiCoatingDefinition definition) {

    /**
     * The coating with this id, or null if no datapack defines it.
     */
    public static QiCoating byId(Identifier id, RegistryAccess access) {
        QiCoatingDefinition definition = CoreRegistries.safeAccess(CoreRegistries.QI_COATING_REGISTRY, id, access);
        return definition == null ? null : new QiCoating(id, definition);
    }

    /**
     * Whether this covers the whole player model rather than just the hands.
     */
    public boolean wholeBody() {
        return definition.coverage() == QiCoatingDefinition.Coverage.BODY
                || definition.coverage() == QiCoatingDefinition.Coverage.WHOLE_SKIN;
    }

    /**
     * Whether a skin look also covers the head and torso, not just the arms and legs.
     */
    public boolean coversHeadAndTorso() {
        return definition.coverage() == QiCoatingDefinition.Coverage.WHOLE_SKIN;
    }

    /**
     * Whether this coats the held item rather than the player.
     */
    public boolean swordQi() {
        return definition.coverage() == QiCoatingDefinition.Coverage.WEAPON;
    }

    public long growMillis() {
        return definition.growMillis();
    }

    /**
     * Whether this grows in and out (growMillis).
     */
    public boolean grows() {
        return definition.growMillis() > 0;
    }

    public boolean echoes() {
        return definition.echoes();
    }

    /**
     * Whether this reacts to anything the player does (reacts_to).
     */
    public boolean reacts() {
        return !definition.reactsTo().isEmpty();
    }

    public boolean reactsTo(QiCoatingDefinition.Reaction reaction) {
        return definition.reactsTo().contains(reaction);
    }

    public boolean hasEffect(Identifier effect) {
        return definition.effects().contains(effect);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof QiCoating coating && coating.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
