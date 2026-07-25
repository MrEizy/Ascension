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
<summary>Resource Transactions and Modifiers</summary>

## Added Classes:

### Resource Transaction API
* `ResourceType`: Defines how a specific resource is read, validated, and modified.
* `ResourceRegistries`: Contains the registry used for resource type adapters.
* `ResourceOperation`: Defines supported transaction operations such as consumption, accumulation, restoration, generation, and draining.
* `ResourceTransactionRequest`: Represents a requested resource transaction.
* `ResourceTransactionContext`: Stores the entity, source, skill, target, flags, and scaled-value context associated with a transaction.
* `ResourceTransactionResult`: Contains the requested, modified, and applied transaction values.
* `ResourceTransactionStatus`: Represents the final outcome of a transaction.
* `ResourceTransactionFlag`: Provides transaction behaviour flags and recursion protection.
* `ResourceTransactionSelector`: Selects transactions by resource, operation, source ID, or source tag.
* `ResourceTransactionService`: Validates, modifies, resolves, and applies resource transactions.
* `ResourceTransactions`: Provides simplified helper methods for common transaction operations.
* `ResourceApplicationResult`: Represents the result returned by a resource adapter after applying a transaction.

### Resource Sources
* `ResourceSourceIdentity`: Base interface for registered or adapted transaction sources.
* `SimpleResourceSourceIdentity`: Provides the current lightweight source implementation.
* `ResourceSourceSelector`: Matches exact source IDs, source tags, and excluded sources.

### Resource Modifiers
* `ResourceModifier`: Represents a resolved modifier applied to a transaction.
* `ResourceModifierDefinition`: Defines a datapack-configured resource modifier.
* `ResourceModifierCollector`: Collects and replaces modifiers using their IDs and priorities.
* `ResourceModifierOperation`: Defines flat, multiplicative, cancellation, immunity, and limiting modifier operations.
* `ResourceModifierResolution`: Resolves collected modifiers into a final transaction amount.

### Resource Events
* `ResourceTransactionEvent`: Provides pre-validation, modifier collection, and post-transaction events for compatibility and extension.

### Built-in Resources
* `AbstractBoundedResourceType`: Shared implementation support for bounded resources.
* `QiResourceType`: Adapts Ascension Qi to the resource transaction system.
* `PlayerExhaustionResourceType`: Adapts player exhaustion accumulation and removal.
* `PlayerHungerResourceType`: Adapts discrete player hunger changes.
* `PlayerSaturationResourceType`: Adapts player saturation changes.
* `AscensionResourceTypes`: Registers the built-in resource types.

### Built-in Sources
* `AscensionResourceSources`: Defines the initial transaction sources and resolves movement-specific sources.
* `AscensionResourceSourceTags`: Defines broad source categories such as movement, combat, survival, regeneration, skills, cultivation, and environmental effects.

### Resource Modifier Passives
* `ResourceModifierPassiveSkill`: Generic levelled passive skill that contributes resource modifiers.
* `ResourceModifierPassiveSkillData`: Stores the passive’s skill progression data.
* `ResourceModifierLevelDefinition`: Defines the modifiers provided by each skill level.
* `ResourceModifierPassiveSkillType`: Provides the datapack codec for `ascension:resource_modifier_passive`.
* `ResourceModifierPassiveHandler`: Collects matching modifiers from owned passive skills during transactions.

### Vanilla Exhaustion Hooks
* `PlayerTravelExhaustionMixin`: Routes walking, sprinting, swimming, and elytra exhaustion through registered movement sources.
* `PlayerJumpExhaustionMixin`: Routes jumping exhaustion through the jumping source.
* `PlayerAttackExhaustionMixin`: Routes attack exhaustion through the attacking source.
* `FoodDataRegenerationExhaustionMixin`: Routes natural-regeneration exhaustion through the regeneration source.
* `PlayerExhaustionFallbackMixin`: Routes otherwise unclassified exhaustion calls through a fallback source.
* `FoodDataAccessor`: Provides controlled access to the private vanilla exhaustion value.

## Other Changed Classes:
* `AscensionCraft`: Registers built-in resource type adapters.
* `AscensionSkillTypes`: Registers `ascension:resource_modifier_passive`.
* `SimpleEntityQiProvider`: Routes Qi consumption and restoration through resource transactions while retaining the existing Qi capability API.
* `ToggleablePassiveSkill`: Routes passive Qi upkeep through the `ascension:skill_casting` transaction source.
* `ResourceModifierDefinition`: Updated optional scaled-value codec handling.
* `ResourceTransactionSelector`: Updated optional source-selector codec handling.
* `PlayerExhaustionResourceType`: Uses the `FoodDataAccessor` to read and modify vanilla exhaustion.
* `ascension.mixins.json`: Registers the resource hooks and FoodData accessor.

---

# Planned Implementation

### Resource Operations
* `consume`: Spends a resource from an available pool.
* `accumulate`: Adds an accumulating burden such as exhaustion.
* `restore`: Restores a resource or removes an accumulated burden.
* `generate`: Produces additional resource.
* `drain`: Removes a resource without treating it as an ordinary cost.
Resources determine which operations they support.

### Resolution
* Modifier values are resolved in the following order:
    1. Flat additions
    2. Base multipliers
    3. Total multipliers
    4. Minimum and maximum limits
    5. Mechanical application
* Transactions can also be cancelled or made immune.
* Final negative costs are prevented.
* Non-finite values are rejected.

### Post-Transaction Events
* Completed transactions produce a result containing:
    * Original requested amount
    * Modified amount
    * Actual applied amount
    * Resource values before and after
    * Final status
* External systems can react without replacing the core transaction implementation.

## Passive Modifier Performance
* Profile modifier collection during movement-heavy gameplay.
* Cache or index active modifier providers if skill scanning becomes expensive.
* Invalidate caches when:
    * Skills are added or removed
    * Skill levels change
    * Passives are toggled
    * Techniques are replaced
    * Relevant effects begin or expire

## Compatibility
* Provide documented events for other mods to:
    * Validate transactions
    * Add modifiers
    * Cancel transactions
    * Grant immunity
    * React after transactions

## Vanilla Exhaustion Integration
* Movement, jumping, attacking, and natural regeneration are assigned distinct sources.
* Walking, sprinting, swimming, and elytra movement share the broader movement tag.
* Unknown exhaustion calls use the unclassified source.
* A movement passive will not accidentally affect:
    * Attacking
    * Natural regeneration
    * Skill casting
    * Starvation
    * Hostile hunger effects
    * Unknown exhaustion sources

</details>

---

<details>
<summary>Stamina System</summary>

## Added Classes:

### Stamina
* `StaminaService`: Provides the shared API for reading, modifying, regenerating, and spending stamina.
* `StaminaResourceType`: Adapts stamina to the resource transaction system.
* `StaminaTicker`: Handles movement costs, physical-action costs, regeneration delays, regeneration, and maximum-stamina clamping.
* `StaminaRegenerationPolicy`: Calculates hunger-sensitive stamina regeneration using the player’s current hunger and saturation.

### HUD
* `StaminaBar`: Displays the player’s current and maximum stamina beneath the Qi bar.

## Other Changed Classes:
* `AscensionCraft`: Adds the stamina attributes to players.
* `AscensionAttachments`: Registers attachments for current stamina and the remaining regeneration delay.
* `AscensionAttributes`: Registers maximum stamina, stamina regeneration rate, and stamina regeneration delay.
* `SimpleAscensionEntityData`: Adds Vitality, Strength, and Agility scaling to stamina attributes.
* `AscensionResourceTypes`: Registers `ascension:stamina`.
* `AscensionResourceSources`: Adds climbing and crawling as movement expenditure sources.
* `PlayerJumpExhaustionMixin`: Charges stamina when players jump.
* `PlayerAttackExhaustionMixin`: Charges stamina when players attack.
* `ClientAscensionData`: Exposes synced stamina values to the client HUD.
* `HudContainer`: Adds the stamina bar beneath the Qi bar and expands the HUD frame.
* `AscensionClientConfig`: Updates the exact-value HUD setting to include stamina.
* `AscLangProvider`: Adds stamina attribute names and updates the Sustained Spirit description.

---

# Planned Implementation

## Datapack Configuration
* Move stamina costs into datapack-configured action or movement profiles.
* Allow packs to configure:
    * Base cost
    * Resource source
    * Cost interval
    * Minimum stamina requirement
    * Behaviour when stamina is insufficient
    * Stat and affinity scaling

## Body-Path Integration
* Allow Body-path progression to improve:
    * Maximum stamina
    * Stamina regeneration
    * Regeneration delay
    * Movement efficiency
    * Physical skill efficiency
* Use the same stamina resource for non-Qi Body skills.

## Stamina Regeneration
* Add configurable regeneration conditions.

## Networking
* Profile attachment syncing during rapid stamina expenditure and regeneration.
* Add update thresholds or packet throttling if stamina produces excessive network traffic.
* Consider synchronizing:
    * Current stamina
    * Maximum stamina changes
    * Regeneration state

## Compatibility
* Allow other mods to consume and restore stamina through the public resource transaction API.
* Provide documented action-source IDs and tags.
* Allow compatibility modules to register additional movement and physical-action sources.
* Avoid requiring external mods to directly access stamina attachments.

</details>

---

<details>
<summary>  </summary>

</details>