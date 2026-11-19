package net.zic.ascension.impl.core.connection;

/**
 * Implemented by the connected_technique, connected_physique and connected_bloodline definitions.
 * The transfer items check these before the definition is given to a player.
 */
public interface Connected {
    ConnectionRequirements connections();
}
