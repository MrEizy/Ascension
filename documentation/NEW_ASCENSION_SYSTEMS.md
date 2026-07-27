# Ascension Systems by sortOfSmart?

<details>
<summary>Skill Leveling and Scaled Values</summary>

## Relevant Classes

### Skill Leveling

* `LevelledSkill`: Marks skills that support levels and defines their maximum level
* `LevelledSkillData`: Skill data containing progression information
* `SkillProgressionData`: Stores trained levels, experience, level floors and level caps
* `SkillProgressionService`: Handles skill experience and permanent progression changes
* `SkillLevelResolver`: Calculates the current usable level of a skill
* `SkillLevelSnapshot`: Stores the result of a level calculation
* `SkillLevelChangedEvent`: Fires when a skill's effective level changes
* `SkillLevelResolveEvent`: Allows temporary level modifiers
* `SetSkillLevelAction`: Adds or removes level floor and cap contributions
* `SetSkillLevelActionType`: Codec for the `set_skill_level` progression action

### Scaled Values

* `ScaledValue`: A reusable datapack value that scales from different sources
* `ScaledValueContext`: Contains the information used during value calculation
* `ScaledValueTerm`: One contribution to a scaled value
* `ScaledValueOperation`: Determines whether a term adds, multiplies or replaces
* `ScaledValueSource`: Base interface for scaling sources
* `CodecType<ScaledValueSource>`: Shared codec holder used by the scaling-source registry
* `ConstantScaledValueSource`: Uses a fixed value
* `SkillLevelScaledValueSource`: Scales from skill level
* `ChargeScaledValueSource`: Scales from held-cast charge
* `StatScaledValueSource`: Scales from Ascension stats
* `AffinityScaledValueSource`: Scales from path affinities
* `ContextScaledValueSource`: Uses a value provided by the calling system
* `AscensionScaledValueSourceTypes`: Registers the default scaling sources

---

## Skill Levels

`SkillProgressionData` holds the trained level, skill experience, and both the level floor and level cap contributions for a skill.
The usable level a player currently has isn't just read off that data directly. `SkillLevelResolver` calculates it, firing `SkillLevelResolveEvent` along the way so buffs, debuffs, or anything else that needs to temporarily nudge a skill's effective level can hook in without touching the stored progression.
If you're adding anything that changes a skill's level, e.g. a new manual, a command, a future training system, go through `SkillProgressionService` rather than writing to `SkillProgressionData` directly. That's also what keeps a trained level intact even when a player's current realm temporarily locks them out of using the skill: the realm restricts access, not progression.

## Scaled Values

`ScaledValue` is the general-purpose way to define a number that scales off multiple things at once; constants, skill level, held-cast charge, Ascension stats, path affinities, or a context value the calling system provides. Each contributing term is ordered and can add, multiply, or outright replace the running value, including powered curves (quadratic charge scaling, for instance) and min/max clamps.
This backs damage, radius, duration, resource costs and modifiers, cultivation rate, projectile speed, effect potency, and frozen buildup. Basically anywhere a number in a skill needs to grow with the player instead of being hardcoded.

</details>

---

<details>
<summary>Resource Transactions and Modifiers</summary>

## Relevant Classes

### Resource Transactions

* `ResourceType`: Defines how a resource is read and changed
* `ResourceRegistries`: Registry for resource types
* `ResourceOperation`: The type of resource change
* `ResourceTransactionRequest`: A requested resource change
* `ResourceTransactionContext`: Extra information about a transaction
* `ResourceTransactionResult`: The final result of a transaction
* `ResourceTransactionStatus`: Whether a transaction succeeded, failed, was cancelled etc
* `ResourceTransactionFlag`: Extra transaction rules and recursion protection
* `ResourceTransactionSelector`: Selects transactions by resource, operation or source
* `ResourceTransactionService`: Handles validation, modifiers and final application
* `ResourceTransactions`: Helper methods for common resource changes
* `ResourceApplicationResult`: The result returned by a resource type
* `ResourceTransactionEvent`: Events for validating, modifying and reacting to transactions

### Resource Sources

* `ResourceSourceIdentity`: Base interface for transaction sources
* `SimpleResourceSourceIdentity`: Basic resource source implementation
* `ResourceSourceSelector`: Selects exact sources or broader source tags
* `AscensionResourceSourceTags`: Public source tags such as movement, combat and survival
* `AscensionResourceSources`: Defines the built-in transaction sources

### Resource Modifiers

* `ResourceModifier`: A resolved transaction modifier
* `ResourceModifierDefinition`: Datapack definition for a modifier
* `ResourceModifierCollector`: Collects and replaces modifiers
* `ResourceModifierOperation`: The different ways a modifier can change a transaction
* `ResourceModifierResolution`: Calculates the final modified amount

### Built-in Resources

* `AbstractBoundedResourceType`: Shared support for bounded resources
* `QiResourceType`: Connects Qi to resource transactions
* `PlayerExhaustionResourceType`: Connects exhaustion to resource transactions
* `PlayerHungerResourceType`: Connects hunger to resource transactions
* `PlayerSaturationResourceType`: Connects saturation to resource transactions
* `AscensionResourceTypes`: Registers Ascension resource types

### Resource Modifier Passives

* `ResourceModifierPassiveSkill`: Generic passive that modifies resource transactions
* `ResourceModifierPassiveSkillData`: Skill data for resource modifier passives
* `ResourceModifierLevelDefinition`: Modifiers provided by each passive level
* `ResourceModifierPassiveSkillType`: Codec for resource modifier passives
* `ResourceModifierPassiveHandler`: Collects modifiers from owned passive skills

### Vanilla Exhaustion Hooks

* `PlayerTravelExhaustionMixin`: Routes movement exhaustion through resource transactions
* `PlayerJumpExhaustionMixin`: Routes jump exhaustion through resource transactions
* `PlayerAttackExhaustionMixin`: Routes attack exhaustion through resource transactions
* `FoodDataRegenerationExhaustionMixin`: Routes regeneration exhaustion through resource transactions
* `PlayerExhaustionFallbackMixin`: Handles unclassified exhaustion calls
* `FoodDataAccessor`: Provides access to vanilla exhaustion data

---

## Using Resource Transactions

Anything that spends, restores, or otherwise touches a resource should go through `ResourceTransactionService.transact(...)`, or the shortcuts in `ResourceTransactions` for the common cases (don't mutate resource values directly).

A transaction is a bundle of information: which resource, which operation, an exact source identity plus its tags, the acting entity, and optionally a skill, a target, context values, or flags. That gets run through a fixed pipeline: validate, collect modifiers, resolve the final modifier, apply it to the resource, then fire a post-transaction event.

## Resource Operations

There are five operations right now:

* `consume`: spend a resource from an available pool
* `accumulate`: add an accumulating burden, like exhaustion building up
* `restore`: restore a resource or remove an accumulated burden
* `generate`: create additional resource
* `drain`: remove a resource without it counting as a normal cost

Not every resource supports every operation, instead each `ResourceType` decides what's valid for it.

## Sources and Tags

Every transaction carries an exact source ID describing why it happened. Current sources include:

* Sprinting
* Jumping
* Swimming
* Attacking
* Natural regeneration
* Skill casting
* Cultivation
* Environmental effects

Source tags then group related sources together, so a modifier can target a whole category instead of listing every source by hand:

* Movement
* Combat
* Survival
* Regeneration
* Skill
* Cultivation
* Environmental

A modifier can restrict itself to an exact source, a tag, or a mix of both.

## Resource Modifiers

A modifier can do one of a few things to a transaction:

* Add or remove a flat amount
* Multiply the base amount
* Multiply the final amount
* Clamp with a minimum or maximum
* Cancel the transaction outright
* Grant immunity

They're collected from passive skills, temporary skill effects, and events, so more than one of these can weigh in on the same transaction before it resolves.

</details>

---

<details>
<summary>Stamina System</summary>

## Relevant Classes

* `StaminaService`: Shared access for reading, spending and restoring stamina
* `StaminaResourceType`: Connects stamina to the resource transaction system
* `StaminaTicker`: Handles stamina costs, regeneration and regeneration delay
* `StaminaRegenerationPolicy`: Changes stamina regeneration based on hunger and saturation
* `StaminaBar`: Displays stamina underneath the Qi bar

---

## Stamina

`StaminaService` lives in the common package as runtime code. Stamina itself is stored as an entity attachment and exposed to everything else through resource transactions, same as any other resource. It's tracked separately from Qi, hunger, saturation, and exhaustion, and isn't meant to replace any of them.

## Current Uses

Right now stamina gets consumed by sprinting, swimming, elytra travel, climbing, crawling, jumping, and attacking. Normal walking is free.

## Regeneration

Regeneration kicks back in after a short delay following any stamina spend. The rate then depends on hunger and saturation:

* Saturation gives slightly faster regeneration
* High hunger gives normal regeneration
* Low hunger slows regeneration
* Zero hunger stops regeneration

Spending stamina doesn't consume hunger.

</details>

---

<details>
<summary>Held Cast System</summary>

## Relevant Classes

* `HeldCastSpec`: Contains charge, cost, movement, interruption, stage and cooldown settings
* `HeldCastData`: Stores charge, paid cost and interruption data
* `HeldCastSkill`: Handles the held-cast lifecycle
* `HeldCastSkillType`: Codec for the `held_cast` skill type
* `SkillExecutionDefinition`: Shared targeting and feature definition used on release
* `SkillExecutions`: Resolves targets and applies shared execution features
* `HeldCastVisualStatePacket`: Syncs held-cast presentation
* `HeldCastVisualSyncManager`: Handles observer visual updates

---

## Held Casts

`ascension:held_cast` owns charging, cumulative costs, cancellation, interruption, movement restrictions, stages and cooldowns. Its release is a normal shared skill execution, so held and instant skills use the same targeting and feature system.

Costs are cumulative. Each tick only pays the difference between the current cumulative cost and the amount already paid, so cancelling a nearly complete cast does not refund its preparation cost.

The server decides charge, costs, release timing, targeting and execution. Clients receive compact presentation state only.

</details>

---

<details>
<summary>Active Skills, Executions and Targeting</summary>

## Relevant Classes

### Active Skills

* `ActiveSkill`: Handles instant costs, execution and cooldowns
* `ActiveSkillData`: Stores active-skill progression data
* `ActiveSkillLevelDefinition`: Defines execution, costs and cooldown at one effective level
* `ActiveSkillCostDefinition`: Defines an upfront resource cost
* `ActiveSkillType`: Codec for the `active_skill` skill type

### Shared Executions

* `SkillExecutionDefinition`: Defines targeting, target requirements, caster features and target features
* `SkillExecutionContext`: Contains caster, target, skill, charge, position and context values
* `SkillExecutionFeature`: Base interface for reusable execution behaviour
* `SkillExecutions`: Resolves and applies shared executions
* `AscensionSkillExecutionFeatureTypes`: Registers built-in execution features

### Targeting

* `TargetingDefinition`: Base interface for datapack targeting definitions
* `TargetingContext`: Information used while resolving targets
* `TargetingResult`: Stores resolved entity or position targets
* `TargetFilterDefinition`: Filters targets by relation, player status and line of sight
* `TargetSort`: Defines target ordering
* `TargetingService`: Shared server-side targeting helpers
* `AscensionTargetingTypes`: Registers self, ray, cone, radial and looked-at-position targeting

---

## Active Skills

`ascension:active_skill` is the instant counterpart to held casts. Each effective level provides a complete execution, cost list and cooldown. Targeting is resolved before resources are spent, and cooldown is applied after a successful execution.

## Shared Executions

Caster features run once at the skill origin. Target features run once for every selected entity or position. Message, sound, particles, resource transactions, frozen buildup, temporary effects, movement, projectile spawning and runtime-object operations all use this feature layer.

## Targeting

Targeting is server-authoritative. Built-in definitions support self, ray, cone, radial and looked-at-position targeting. Shared filters use the explicit relations `self`, `ally`, `neutral` and `hostile` rather than separate inclusion flags.

</details>

---

<details>
<summary>Movement, Projectiles and Runtime Objects</summary>

## Relevant Classes

### Movement

* `MovementFeature`: Handles directional, target-position and anchor movement
* `MovementAnchorFeature`: Creates or clears saved movement anchors
* `MovementService`: Validates destinations, collision and anchor access
* `MovementAnchorContainer`: Stores persistent anchors on an entity

### Virtual Projectiles

* `VirtualProjectileDefinition`: Datapack projectile definition
* `VirtualProjectileInstance`: Lightweight runtime projectile state
* `VirtualProjectiles`: Handles spawning, ticking, collision and removal
* `ProjectileBehavior`: Base interface for composable projectile behaviour
* `HomingProjectileBehavior`: Handles steering and optional reacquisition

### Runtime Objects

* `AreaFieldDefinition`: Defines persistent target-filtered areas
* `AreaFields`: Handles field lifecycles and enter, tick and exit features
* `AnchorNetworkDefinition`: Defines linked nodes and optional child fields
* `AnchorNetworks`: Handles linked-node runtime state
* `OwnerBoundConstructDefinition`: Defines owner-relative constructs and visual stages
* `OwnerBoundConstructs`: Handles construct duration, stability and syncing
* `RuntimeObjectFeature`: Spawns, removes or restores shared runtime objects

### Presentation

* `RuntimeVisualState`: Compact shared visual state
* `RuntimeVisualController`: Client renderer lifecycle
* `ClientRuntimeVisuals`: Stores states, controllers and owner-relative transforms
* `RuntimeVisualSync`: Sends state changes to nearby observers
* `RuntimeVisualPacket`: Encodes shared runtime presentation

---

## Runtime Objects

Area fields, anchor networks and owner-bound constructs are datapack registries. Runtime instances store registry IDs and re-resolve their definitions, so removed definitions end safely after reloads.

Anchor networks are the technical linked-node system. Individual techniques may still describe them as formations, constellations, arrays or seals without conflicting with the separate formations mod.

The server owns movement, collision, field membership, projectile hits and construct stability. Clients receive only compact visual state and render it through registered controllers.

</details>

---

<details>
<summary>Frozen State and Temporary Skill Effects</summary>

## Relevant Classes

### Temporary Effect API

* `SkillEffectDefinition`: Datapack definition for a temporary effect
* `SkillEffectContext`: Read-only information passed to effect modules
* `SkillEffectModule`: Base interface for reusable effect behaviour
* `CodecType<SkillEffectModule>`: Shared codec holder used by the effect-module registry
* `SkillEffectStackingPolicy`: Defines repeated application behaviour
* `SkillEffectRemovalReason`: The reason an effect was removed
* `SkillEffectEvent`: Events for applying, updating and removing effects

### Effect Runtime

* `SkillEffectInstance`: Stores one active effect's source, duration, potency and stacks
* `SkillEffectContainer`: Stores active temporary effects on an entity
* `SkillEffectManager`: Handles ticking, stacking and removing effects
* `SkillEffectService`: Shared access for applying and removing effects
* `SkillEffectTicker`: Ticks active temporary effects and frozen state

### Effect Modules

* `FrozenFormEffectModule`: Handles Frozen Form movement and frozen-state behaviour
* `ResourceModifierEffectModule`: Allows effects to modify resource transactions
* `SkillEffectResourceModifierHandler`: Collects resource modifiers from active effects
* `AscensionSkillEffectModuleTypes`: Registers effect module types

### Frozen State

* `FrozenStateData`: Stores frozen buildup, decay delay and applied visual ticks
* `FrozenStateService`: Handles buildup, decay, resistance, immunity and thawing
* `AscEntityTypeTagProvider`: Generates frozen immunity, resistance and boss profile tags

---

## Temporary Effects

Temporary effect definitions live in datapacks. Each one specifies:

* A stacking policy
* A maximum stack count
* The effect modules it's built from

At runtime, an active instance tracks:

* Definition ID
* Source entity
* Source skill
* Remaining duration
* Potency
* Stack count

If you're writing a new effect module, you only get read access to this through `SkillEffectContext`. The actual mutable instances and containers stay internal, so a module can't reach in and mutate state it doesn't own.

## Stacking Policies

There are three stacking policies so far: refreshing an existing effect, replacing it only if the new one is stronger, and capped stacking up to a max. `SkillEffectEvent` fires for application, updates, and removal if you need to hook into any of those.

If an effect's definition goes missing or gets emptied out (say, after a `/reload`), any active runtime instance of it gets removed safely rather than left in a broken state. Effects also only tick while their entity is loaded.

## Resource Modifier Effects

`ResourceModifierEffectModule` is the generic building block for effects that need to modify resource transactions; Qi, stamina, hunger, saturation, exhaustion, or whatever else gets registered later. It's meant to cover most of what buffs, poison, fear, bleeding, seals, curses, cultivation suppression, and overexertion would need, without each one needing its own bespoke module.

## Frozen State

Frozen buildup and Frozen Form are two different things; ordinary cold sources can build up frozen stacks without triggering the named Frozen Form debuff at all. The frozen state system handles buildup, decay delay, gradual thawing (faster while burning), immunity, resistance, boss resistance, and the vanilla frozen visual ticks.

Ascension keeps track of which frozen ticks it applied itself, and won't reduce freezing that's stronger. So if something else has frozen the entity harder than we have, we don't accidentally thaw it out early.

## Frozen Form

Frozen Form itself maintains a minimum frozen buildup, reduces horizontal movement, and raises movement and combat stamina costs, with weaker versions of the effect for players, resistant entities, and bosses. It also ends early if the target's on fire.

</details>

---

<details>
<summary>Datapack Reloads and Networking</summary>

## Datapack Reloads

Runtime casts, projectiles, and effects hold onto registry IDs rather than the datapack definition objects themselves, and re-resolve the actual definition each time it's needed. That means if a definition disappears, active effects get removed safely, in-progress casts get invalidated, and virtual projectiles stop, instead of them holding a reference to an object that no longer exists.

## Networking

Same rule as everywhere else: the server is authoritative over

* Resource values
* Stamina
* Held-cast charge
* Cast execution results
* Effect duration
* Frozen buildup
* Target selection

Clients only receive synced attachment data, compact held-cast visual packets, vanilla frozen visual state, and other purely presentational information.

</details>

---

<details>
<summary>Other Classes Changed</summary>

## Registration and Registries

* `AscensionCraft`: Registered scaled values, resources, held cast types, effect modules, stamina attributes and network payloads
* `CoreRegistries`: Registers skill effects, virtual projectiles, area fields, anchor networks and constructs
* `TypeRegistries`: Registers shared codec families for scaled values, execution features, targeting, effect modules and projectile behaviours
* `AscensionProgressActionTypes`: Registered the `set_skill_level` action
* `AscensionSkillTypes`: Registered resource modifier passives, held casts and active skills
* `AscensionSkillExecutionFeatureTypes`: Registered the shared execution features
* `AscensionTargetingTypes`: Registered self, ray, cone, radial and looked-at-position targeting
* `AscensionSkillEffectModuleTypes`: Registered Frozen Form and resource modifier effect modules

## Casting

* `CastData`: Added dirty state support for cast syncing
* `CastStatus`: Added release, cancellation, interruption and resource failure reasons
* `CastingInstance`: Added held cast transitions, interruption and syncing
* `SkillCastHandler`: Added held cast syncing, release handling and recast protection
* `AscensionSkillListener`: Clears the held input latch when the cast key is released
* `ParticleFieldController`: Added held cast charge particle support
* `ResourceTransactionContext`: Added held charge values to scaled value contexts
* `HeldCastSpec`: Removes duplicate charge stages during loading
* `HeldCastCostDefinition`: Uses the public resource source tags

## Resources and Qi

* `SimpleEntityQiProvider`: Routes Qi changes through resource transactions
* `ToggleablePassiveSkill`: Routes passive Qi upkeep through resource transactions
* `ResourceModifierDefinition`: Fixed optional scaled value decoding
* `ResourceTransactionSelector`: Fixed optional source selector decoding
* `PlayerExhaustionResourceType`: Uses the FoodData accessor
* `AscensionResourceSources`: Added climbing and crawling movement sources
* `AscensionResourceTypes`: Registered the stamina resource
* `ascension.mixins.json`: Registered the resource mixins and FoodData accessor

## Stamina and HUD

* `AscensionAttachments`: Added stamina, regeneration delay, frozen state and active effect attachments
* `AscensionAttributes`: Registered maximum stamina, regeneration rate and regeneration delay
* `SimpleAscensionEntityData`: Added stat scaling for stamina
* `PlayerJumpExhaustionMixin`: Added jumping stamina costs
* `PlayerAttackExhaustionMixin`: Added attacking stamina costs
* `ClientAscensionData`: Exposes stamina values to the HUD
* `HudContainer`: Added the stamina bar beneath Qi
* `AscensionClientConfig`: Updated the exact HUD value option
* `StaminaTicker`: Uses the reorganised stamina service

## Temporary Effects and Frozen State

* `SkillEffectDefinition`: Added maximum stack limits
* `SkillEffectModule`: Added update and conditional removal hooks
* `SkillEffectTicker`: Skips entities without active effect or frozen data
* `FrozenFormEffectModule`: Added player, resistant and boss profiles
* `FrozenStateService`: Avoids overwriting stronger vanilla or modded freezing
* `SkillEffectFeature`: Uses the shared execution context's skill ID
* `FrozenBuildupFeature`: Uses the reorganised frozen-state service
* `ResourceTransactionFeature`: Uses the public resource source tags

## Datagen and Content

* `AscDataGen`: Registered entity type tag datagen
* `ModTags`: Added frozen immune, resistant and boss-profile tags
* `AscLangProvider`: Added stamina, Sustained Spirit and Frostbound Stillness translations

</details>