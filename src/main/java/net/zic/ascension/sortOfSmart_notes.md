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
* `LevelledSkill`: Marks skills that can have levels and defines their maximum level
* `LevelledSkillData`: Skill data that contains leveling information
* `SkillProgressionData`: Stores skill levels, experience, level floors and level caps
* `SkillProgressionService`: Handles changing skill levels and experience
* `SkillLevelResolver`: Works out the actual level of a skill
* `SkillLevelSnapshot`: Stores the result of a skill level calculation
* `SkillLevelChangedEvent`: Event for when a skill level changes
* `SkillLevelResolveEvent`: Allows other systems to temporarily modify skill levels
* `SetSkillLevelAction`: Progression action for adding or removing skill level contributions
* `SetSkillLevelActionType`: Codec for the `set_skill_level` action
* `ScaledValue`: A reusable datapack value that can scale from different sources
* `ScaledValueContext`: Contains the information used when calculating a scaled value
* `ScaledValueTerm`: One part of a scaled value calculation
* `ScaledValueOperation`: Determines how a value is added, multiplied or replaced
* `ScaledValueSource`: Base interface for different scaling sources
* `ScaledValueSourceType`: Codec type for scaling sources
* `ConstantScaledValueSource`: Uses a fixed value
* `SkillLevelScaledValueSource`: Scales from skill level
* `ChargeScaledValueSource`: Scales from held-cast charge
* `StatScaledValueSource`: Scales from stats
* `AffinityScaledValueSource`: Scales from path affinities
* `ContextScaledValueSource`: Uses a value provided by the system calculating it
* `AscensionScaledValueSourceTypes`: Registers the default scaling sources

## Other changed classes:
* `AscensionCraft`: Registered the scaled value source types
* `TypeRegistries`: Added the scaled value source type registry
* `AscensionProgressActionTypes`: Registered the `set_skill_level` action

---

# Planned Implementation

## Skill Leveling
* Connect skill manuals to the shared skill progression system.
* Let skills gain levels through manuals, experience, realms and other sources.
* Keep trained levels when a skill is temporarily restricted.
* Correctly lower or restore skill levels during realm regression and technique swapping.
* Add commands and proper tests for skill leveling.

## Scaled Values
* Use scaled values for damage, costs, radius, duration, cultivation rate, effects etc.
* Add more scaling sources when they are actually needed, such as:
    * Realms
    * Resources
    * Targets
    * Biomes and dimensions
    * Equipment
    * Active effects
* Add global scaling presets later if a lot of skills start repeating the same values.

</details>

---

<details>
<summary>Resource Transactions and Modifiers</summary>

## Added Classes:
* `ResourceType`: Defines how a resource is stored and changed
* `ResourceRegistries`: Registry for resource types
* `ResourceOperation`: The type of resource change, such as consuming, restoring or accumulating
* `ResourceTransactionRequest`: A requested resource change
* `ResourceTransactionContext`: Extra information about the transaction
* `ResourceTransactionResult`: The final result of a transaction
* `ResourceTransactionStatus`: Whether a transaction succeeded, failed, was cancelled etc
* `ResourceTransactionFlag`: Extra transaction rules and recursion protection
* `ResourceTransactionSelector`: Selects transactions by resource, operation or source
* `ResourceTransactionService`: Handles the full transaction process
* `ResourceTransactions`: Helper methods for common resource changes
* `ResourceApplicationResult`: The result returned by a resource type
* `ResourceSourceIdentity`: Base interface for transaction sources
* `SimpleResourceSourceIdentity`: Basic implementation of a resource source
* `ResourceSourceSelector`: Selects exact sources or broader source tags
* `ResourceModifier`: A modifier being applied to a transaction
* `ResourceModifierDefinition`: Datapack definition for a resource modifier
* `ResourceModifierCollector`: Collects and replaces modifiers
* `ResourceModifierOperation`: The different ways a modifier can change a transaction
* `ResourceModifierResolution`: Calculates the final modified value
* `ResourceTransactionEvent`: Events for validating, modifying and reacting to transactions
* `AbstractBoundedResourceType`: Shared code for resources with minimum and maximum values
* `QiResourceType`: Connects Qi to the transaction system
* `PlayerExhaustionResourceType`: Connects exhaustion to the transaction system
* `PlayerHungerResourceType`: Connects hunger to the transaction system
* `PlayerSaturationResourceType`: Connects saturation to the transaction system
* `AscensionResourceTypes`: Registers Ascension resource types
* `AscensionResourceSources`: Defines the current transaction sources
* `AscensionResourceSourceTags`: Defines source categories such as movement and combat
* `ResourceModifierPassiveSkill`: Generic passive skill that modifies resource transactions
* `ResourceModifierPassiveSkillData`: Skill data for resource modifier passives
* `ResourceModifierLevelDefinition`: Resource modifiers provided by each skill level
* `ResourceModifierPassiveSkillType`: Codec for resource modifier passives
* `ResourceModifierPassiveHandler`: Collects modifiers from owned passive skills
* `PlayerTravelExhaustionMixin`: Routes movement exhaustion through the transaction system
* `PlayerJumpExhaustionMixin`: Routes jump exhaustion through the transaction system
* `PlayerAttackExhaustionMixin`: Routes attack exhaustion through the transaction system
* `FoodDataRegenerationExhaustionMixin`: Routes regeneration exhaustion through the transaction system
* `PlayerExhaustionFallbackMixin`: Handles exhaustion sources that are not classified yet
* `FoodDataAccessor`: Provides access to vanilla exhaustion data

## Other changed classes:
* `AscensionCraft`: Registered the resource types
* `AscensionSkillTypes`: Registered resource modifier passives
* `SimpleEntityQiProvider`: Routes Qi changes through resource transactions
* `ToggleablePassiveSkill`: Routes passive Qi upkeep through resource transactions
* `ResourceModifierDefinition`: Fixed optional scaled value decoding
* `ResourceTransactionSelector`: Fixed optional source selector decoding
* `PlayerExhaustionResourceType`: Uses the FoodData accessor
* `ascension.mixins.json`: Registered the new resource mixins

---

# Planned Implementation

## Resource System
* Keep Qi, stamina, exhaustion, hunger and saturation mechanically separate.
* Let them share the same modifier and event pipeline.
* Add more resource types and sources when they are needed.
* Integrate the system with Olli's central source API once it is ready.

## Modifiers
* Support exact source IDs and broader source tags.
* Allow modifiers to increase, decrease, cancel or limit transactions.
* Improve modifier caching later if checking every passive becomes expensive.
* Add clearer datapack errors and documentation.

## Compatibility
* Let other mods add resource types, sources and modifiers.
* Provide events for validating, modifying, cancelling and reacting to transactions.
* Test compatibility with mods that change hunger, movement, regeneration or exhaustion.

## Vanilla Exhaustion
* Keep movement, jumping, attacking and natural regeneration as separate sources.
* Add more specific sources when needed.
* Keep unknown exhaustion under an unclassified fallback source.

</details>

---

<details>
<summary>Stamina System</summary>

## Added Classes:
* `StaminaService`: Shared API for reading, spending and restoring stamina
* `StaminaResourceType`: Connects stamina to the resource transaction system
* `StaminaTicker`: Handles stamina costs, regeneration and regeneration delay
* `StaminaRegenerationPolicy`: Changes stamina regeneration based on hunger and saturation
* `StaminaBar`: Displays stamina underneath the Qi bar

## Other changed classes:
* `AscensionCraft`: Added stamina attributes to players
* `AscensionAttachments`: Added stamina and regeneration delay attachments
* `AscensionAttributes`: Registered maximum stamina, regeneration rate and regeneration delay
* `SimpleAscensionEntityData`: Added stat scaling for stamina
* `AscensionResourceTypes`: Registered the stamina resource
* `AscensionResourceSources`: Added climbing and crawling movement sources
* `PlayerJumpExhaustionMixin`: Added jumping stamina costs
* `PlayerAttackExhaustionMixin`: Added attacking stamina costs
* `ClientAscensionData`: Syncs stamina values to the HUD
* `HudContainer`: Added the stamina bar below Qi
* `AscensionClientConfig`: Updated the exact HUD value option
* `AscLangProvider`: Added stamina translations and updated Sustained Spirit

---

# Planned Implementation

## Core System
* Move stamina costs into datapack definitions.
* Allow different actions to decide what happens when stamina is too low.
* Add stamina costs for more actions and physical skills.
* Keep stamina separate from exhaustion rather than replacing it.

## Body Path
* Use stamina for Body-path and other physical skills.
* Let Body progression improve maximum stamina, regeneration and efficiency.
* Rebalance stamina scaling when the main player stats are rebalanced.
* Add a universal Body-path passive such as Sustained Body.

## Hunger and Regeneration
* Keep stamina spending separate from hunger consumption.
* Let hunger and saturation affect stamina regeneration:
    * Saturation gives slightly faster regeneration.
    * Low hunger slows regeneration.
    * Zero hunger stops regeneration.
* Let Sustained Body reduce hunger use and eventually restore hunger and saturation using Qi.
* Keep Qi nourishment as a passive skill rather than part of the base stamina system.

## HUD and Networking
* Improve stamina syncing if frequent updates become expensive.
* Add smoother bar animations and low-stamina warnings.
* Add separate visibility and positioning options later.
* Consider hiding the bar while it is full and inactive.

## Compatibility
* Let other mods spend and restore stamina through resource transactions.
* Allow new movement and physical-action source IDs.
* Avoid requiring direct access to stamina attachments.

## Possible Future Systems
* Guard
* Posture
* Poise
* Encumbrance
* Equipment weight
* Injuries
* Overexertion
* Mob stamina
These should remain separate systems rather than all being stuffed into stamina.

</details>

---

<details>
<summary>Held Cast System</summary>

## Added Classes:
* `HeldCastSpec`: Contains the general settings for a held cast
* `HeldCastData`: Stores the current charge, paid cost and interruption data
* `HeldCastCostDefinition`: Defines the resource cost and cumulative cost curve
* `HeldCastMovementDefinition`: Defines movement restrictions while charging
* `HeldCastInterruptionDefinition`: Defines how damage can interrupt a cast
* `HeldCastChargeStage`: Defines charge stages, particles and sounds
* `HeldCastVisualState`: Stores the visual state sent to nearby players
* `HeldCastVisualPhase`: The current phase of a held cast
* `HeldCastExecution`: Base interface for different held cast results
* `HeldCastExecutionContext`: Contains the information used when executing a held cast
* `HeldCastExecutionType`: Codec type for held cast executions
* `RadialTargetingDefinition`: Defines radial target filtering and selection
* `HeldCastReleaseFeature`: Base interface for reusable release effects
* `HeldCastReleaseContext`: Contains the caster, target, charge and release position
* `HeldCastReleaseFeatureType`: Codec type for release features
* `HeldCastSkill`: Handles the shared held cast lifecycle
* `HeldCastSkillData`: Skill data used by held casts
* `HeldCastInterruptionHandler`: Tracks damage and interrupts active casts
* `HeldCastProjectileManager`: Handles lightweight server-side projectiles
* `SelfReleaseExecution`: Executes features on the caster
* `RadialReleaseExecution`: Executes features on nearby targets
* `ProjectileReleaseExecution`: Executes features through a virtual projectile
* `MessageReleaseFeature`: Sends a message when a held cast releases
* `SoundReleaseFeature`: Plays a sound when a held cast releases
* `ParticleBurstReleaseFeature`: Spawns particles when a held cast releases
* `ResourceTransactionReleaseFeature`: Applies a resource transaction on release
* `HeldCastSkillType`: Codec for the `held_cast` skill type
* `AscensionHeldCastExecutionTypes`: Registers held cast execution types
* `AscensionHeldCastReleaseFeatureTypes`: Registers held cast release features
* `HeldCastVisualStatePacket`: Syncs held cast visuals to nearby players
* `HeldCastVisualSyncManager`: Handles held cast visual updates

## Other changed classes:
* `AscensionCraft`: Registered held cast types and network payloads
* `TypeRegistries`: Added registries for held cast executions and release features
* `AscensionSkillTypes`: Registered the `held_cast` skill type
* `CastData`: Added dirty state support for cast syncing
* `CastStatus`: Added release, cancellation, interruption and resource failure reasons
* `CastingInstance`: Added held cast transitions, interruption and syncing
* `SkillCastHandler`: Added held cast syncing, input release handling and recast protection
* `AscensionSkillListener`: Clears the held input latch when the cast key is released
* `ResourceTransactionContext`: Added held charge values to resource scaling context
* `ParticleFieldController`: Added held cast charge particle support

---

# Planned Implementation

## Execution Types
* Keep separate execution types for different delivery methods.
* Current execution types:
    * Self release
    * Radial release
    * Projectile release
* Potential execution types:
    * Beams
    * Barriers
    * Transformations
    * Targeted releases
    * Area channels
    * Multiple projectiles

## Release Features
* Build held skills from reusable release features.
* Current features:
    * Messages
    * Sounds
    * Particle bursts
    * Resource transactions
* Add more features when needed, such as:
    * Damage
    * Buffs and debuffs
    * Frozen buildup
    * Teleportation
    * Knockback
    * Summoning
    * Skill restrictions

## Costs
* Pay held cast costs gradually while charging.
* Use cumulative costs so cancelling cannot avoid payment.
* Support Qi, Stamina and more.
* Use scaled values for linear, quadratic and stat-based costs.
* Add clearer failure behaviour for different resource types later.

## Targeting
* Allow radial casts to configure:
    * Radius
    * Maximum targets
    * Self
    * Allies
    * Neutral mobs
    * Hostile mobs
    * Players
    * Line of sight
* Keep allies excluded by default.
* Add party and faction integration later via guildengine.

## Projectiles
* Add proper projectile entities
* Add more collision and targeting options later.
* Consider projectile speed, range and piercing scaling.


## Networking
* Sync charge stages and important state changes instead of sending updates every tick.
* Use client interpolation for charge visuals.
* Profile packet traffic before adding more frequent updates.
* Keep resource changes and final cast results server-authoritative.

</details>


---

<details>
<summary>  </summary>

</details>
