<details>
<summary>Mob Cultivation</summary>

## Added Classes:
- `MobCultivationCategory`: The type of mob, as well as some multipliers for them
- `MobCultivationCommands`: A bunch of commands for things relating to mob cultivation
- `MobCultivationData`: Additional Data for mob cultivation outside of OriginSource
- `MobCultivationEvents`: Events that utilize MobCultivationManager for various outcomes
- `MobCultivationManager`: The core of this implementation (will be split into multiple classes in the proper implementation) which handles mob cultivation growth, loot drops, ai events etc

## Other changed classes:
- `AscensionCommands`: Registered the mob cultivation commands
- `AscensionAttachments`: Added a mob cultivation data attachment for MobCultivationData
- `SimpleAscensionEntityData`: Updated Origin Source watcher registration so it safely supports non-player living entities
- `OriginSource`: Updated physique serialization so sources without a physique can save and load without producing null-related errors

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

</details>


---

<details>
<summary>  </summary>

</details>
