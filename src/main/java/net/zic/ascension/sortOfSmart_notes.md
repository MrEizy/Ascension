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
<summary>Skill Leveling and Scaled Values</summary>

## Added Classes:

### Skill Progression
* `LevelledSkill`: Marks skills that support level-based behaviour and defines their maximum level.
* `LevelledSkillData`: Base interface for skill data containing persistent progression information.
* `SkillProgressionData`: Stores trained levels, experience, level floors, and accessible-level contributions.
* `SkillProgressionService`: Provides the shared API for granting experience, changing trained levels, and managing level contributions.
* `SkillLevelResolver`: Calculates a skill’s effective level from permanent progression, realm restrictions, and temporary modifiers.
* `SkillLevelSnapshot`: Represents the resolved state of a skill’s current level.
* `SkillLevelChangedEvent`: Fires when a skill’s effective level changes.
* `SkillLevelResolveEvent`: Allows temporary modifiers and external systems to affect a skill’s resolved level.

### Progression Actions
* `SetSkillLevelAction`: Adds or removes level-floor and accessible-level contributions through progression handlers.
* `SetSkillLevelActionType`: Provides the datapack codec for the `ascension:set_skill_level` progression action.

### Scaled Values
* `ScaledValue`: Resolves a datapack-defined value from a base value and an ordered collection of scaling terms.
* `ScaledValueContext`: Supplies the player, skill, target, charge, and other contextual values used during scaling.
* `ScaledValueTerm`: Defines a single contribution to a scaled value.
* `ScaledValueOperation`: Determines whether a contribution adds, multiplies, or replaces the current value.
* `ScaledValueSource`: Base interface for reusable scaling sources.
* `ScaledValueSourceType`: Provides polymorphic codec registration for scaled-value sources.

### Scaled Value Sources
* `ConstantScaledValueSource`: Supplies a fixed value.
* `SkillLevelScaledValueSource`: Scales using the effective level of a skill.
* `ChargeScaledValueSource`: Scales using normalised held-cast charge.
* `StatScaledValueSource`: Scales using an Ascension stat.
* `AffinityScaledValueSource`: Scales using a path affinity.
* `ContextScaledValueSource`: Reads arbitrary values supplied by the calling system.
* `AscensionScaledValueSourceTypes`: Registers the built-in scaled-value source types.

## Other Changed Classes:
* `AscensionCraft`: Registers the new scaled-value source types.
* `TypeRegistries`: Added the `scaled_value_source_type` registry.
* `AscensionProgressActionTypes`: Registered the `ascension:set_skill_level` progression action.

---

# Planned Implementation

## Skill Manuals
* Connect skill manuals to `SkillProgressionService`.
* Allow manuals to:
    * Unlock skills
    * Grant skill experience
    * Increase trained levels
    * Apply progression requirements
* Preserve trained progression when access is temporarily lost.
* Avoid allowing item logic to directly modify stored skill data.

## Technique and Realm Integration
* Use level-floor and accessible-level contributions for technique milestones.
* Allow realm progression to upgrade skills without hardcoded realm checks inside the skills.
* Correctly downgrade skills during realm regression.
* Remove progression contributions when techniques are forgotten or replaced.
* Restore previously trained levels when their requirements are regained.

## Resource Modifiers
* Allow levelled passive skills to provide different resource modifiers at each level.
* Sustained Spirit will use:
    * Level 1 movement-exhaustion reduction
    * Level 1 movement-stamina reduction
    * Improved versions of both modifiers at level 2

## Held Skills
* Use scaled values for:
    * Charge cost
    * Radius
    * Duration
    * Effect potency
    * Frozen buildup
    * Visual intensity
* Use powered charge terms for quadratic or other configurable cost curves.

## Future Scaling Sources
* Current and maximum resources
* Major and minor realms
* Target health and attributes
* Target classifications
* Environmental conditions
* Biome and dimension
* Time of day
* Active buffs and debuffs
* Equipment and artefacts

</details>


---

<details>
<summary>Exhaustion Resource Transaction System</summary>


</details>

---

<details>
<summary>Stamina System</summary>


</details>