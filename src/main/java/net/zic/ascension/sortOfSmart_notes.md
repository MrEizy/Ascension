<details>
<summary>Mob Cultivation</summary>

## Added Classes:
- `MobCultivationCategory`: The type of mob, as well as some multipliers for them
- `MobCultivationCommands`: A bunch of commands for things relating to mob cultivation
- `MobCultivationData`: Additional Data for mob cultivation outside of OriginSource
- `MobCultivationEvents`: Events that utilize MobCultivationManager for various outcomes
- `MobCultivationManager`: The core of this implementation (will be split into multiple classes
  in the proper implementation) which handles mob cultivation growth, loot drops, ai events etc

## Other changed classes:
- `AscensionCommands`: Registered the mob cultivation commands
- `AscensionAttachments`: Added a mob cultivation data attachment for MobCultivationData
- `SimpleAscensionEntityData`: Updated Origin Source watcher registration so it safely 
supports non-player living entities
- `OriginSource`: Updated physique serialization so sources without a physique can save and load 
without producing null-related errors

---

# Planned Implementation

## Mob Cultivation

### Core System
* Reuse Origin Sources for mobs.
* Give mobs access to the same foundation paths, major realms, and minor realms as players.
* Randomly assign cultivated mobs a foundation path.
* Give mobs vitality, strength, agility, and spirit, using the same stat-to-attribute formulas as players.
* Allow mobs to cultivate and advance through realms over time.

### Mob Scaling
* Passive mobs use base generated stats.
* Hostile mobs have approximately `1.5×` passive mob stats.
* Bosses have approximately `2×` hostile mob stats.
* World difficulty might provide an additional small stat multiplier.
* Hostile mobs and bosses will receive extra combat-focused attribute bonuses.

Exact values will depend on an eventual player stat rebalance 
(we really need to rebalance these, because they are kind of weak rn)

### Sub-Paths
* Give mobs elemental, weapon, or other sub-paths based on:
    * Mob type
    * Biome and dimension
    * Equipment
    * Existing abilities
* Sub-paths will influence skills, particles, attributes, weaknesses, and loot.

### AI and Spirituality
* Increase mob spirituality based on realm and spirit.
* Higher-spirituality mobs may gain more advanced behaviour.
* Possible behaviours include:
    * Cultivated passive mobs retaliating when attacked.
    * Weaker mobs fleeing from much stronger cultivators.
    * Mobs gathering near herbs or areas with high atmospheric qi.
    * Powerful mobs guarding territory or resources.
    * Intelligent mobs using improved combat tactics.

### Mob Skills
* Allow higher-realm mobs to rarely obtain skills.
* Skill availability will depend on path, realm, mob type, equipment, and spirit.
* Bosses and unique mobs may use predefined skill sets (optional thought)

### Cultivation Growth
* Base cultivation growth on atmospheric qi and time alive.
* Biome, dimension, species, and nearby resources also affect growth.
* Unusual mobs may use alternative cultivation methods.

#### Potential Undead Pool
* Undead mobs may contribute cultivation to a regional pool.
* Newly spawned undead can receive part of the stored cultivation.
* Cultivation may return to the pool when undead mobs die.

### Breakthroughs
* Allow mobs to progress through minor and major realms.
* Major breakthroughs should produce particles, sounds, and nearby announcements.
* High-realm breakthroughs require tribulations.

### Loot
* Give cultivated mobs additional loot based on realm, paths, category, and skills.
* Higher-realm mobs should have larger and rarer loot pools.


## Beast Taming Path

### Taming
* Allow players to tame and command cultivated beasts.
* Possible commands include follow, stay, guard, patrol, retreat, attack, and cultivate.
* Obedience may depend on the player’s Beast Taming ability, realm, spirit, and bond with the beast.

### Techniques and Skills
* Add a dedicated Beast Taming path with skills for:
    * Taming or calming beasts
    * Healing and restoring beast qi
    * Increasing beast stats
    * Improving cultivation speed
    * Temporarily empowering beasts
    * Sharing cultivation progress

### Beast Space
* Add a space for storing contracted beasts.
* Beasts inside may regenerate health and qi and receive a small cultivation bonus.
* Capacity may increase through realms, techniques, artefacts, or upgrades.

### Beast Enhancement and Merging
* Allow players to improve beast stats, affinities, bloodlines, or species.
* Add advanced beast-merging or cultivator-beast fusion skills.
* These systems should require significant resources and carry meaningful limitations.

### Possible Future Features
* Beast evolution
* Beast bloodlines and physiques
* Breeding and inherited traits
* Beast equipment
* Loyalty and personality
* Beast-specific techniques
* Multiple contracted beasts
* Sect guardian beasts
* Wild high-realm spiritual beasts

</details>

---

<details>
<summary>Other Things</summary>

I got nothing lol...

</details>