# Developers

This documentation is Targeted at Developers, and assumes a minimum understanding of Java, OOP and the neoforge ecosystem

## Topics



### Datapack Types

If you have read through the datapack documentation or already had a peek at the code
you may have discovered the existence of Physique/Bloodline/Path types etc. These are objects 
that tell Minecraft how to decode a specific java file. this means 2 physiques can have completely different 
JSON formats, yet still produce a valid Physique object.

If one of the built-in datapack types does not suit your use case you will have to make your own.
This is the exact same for all Types except Tribulations, see [Tribulations](#tribulations).


The basic process.

1. Create a new Type, and a Class your type Provides
2. Provide a [Codec](https://docs.neoforged.net/docs/datastorage/codecs/)
3. Register your new type in the correct registry

You can view a List of possible Types in `TypeRegistries.java`

#### Example

for this example I will just show How SimplePhysiqueType is handeld

```java

public class SimplePhysiqueType extends PhysiqueType {
    @Override
    public MapCodec<? extends Physique> codec() {
        return RecordCodecBuilder.<SimplePhysique>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimplePhysique::name),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(SimplePhysique::description),
                        Identifier.CODEC.listOf().fieldOf("paths").forGetter(SimplePhysique::unlockedPaths),
                        Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(SimplePhysique::skills),
                        ValueContainer.BASE_MODIFIER_CODEC.listOf().optionalFieldOf("base_stats",List.of()).forGetter(SimplePhysique::baseStats),
                        ValueContainerModifier.MAP_CODEC.optionalFieldOf("stat_modifiers", Map.of()).forGetter(SimplePhysique::statModifiers),
                        BaseAffinity.CODEC.listOf().optionalFieldOf("base_affinity",List.of()).forGetter(SimplePhysique::baseAffinities),
                        Codec.unboundedMap(Identifier.CODEC,AffinityModifier.CODEC.listOf()).optionalFieldOf("affinity_modifiers",Map.of()).forGetter(SimplePhysique::affinityModifiers),
                        AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(SimplePhysique::itemTooltip)
                ).apply(instance, SimplePhysique::new)
        );
    }


}
```

When extending the type you want to make, You must override `codec()` This must return a codec that creates an instance of your desired class

It is that simple.

For details on Codecs look [here](https://docs.neoforged.net/docs/datastorage/codecs/)

### Physiques
A physique is a registry entry (`data/<namespace>/ascension/physiques/`) that holds the configuration and logic, the per-origin state lives in its `PhysiqueData`. It follows the same pattern as skills, an origin holds at most one physique.

A physique is split into 3 parts
- `Physique<D>` the registry entry, `D` is the `PhysiqueData` it creates and runs on. It provides `newData()`, a `SerializerHandler<D>` and a `SyncHandler<D>`. Physiques without state use `EmptyPhysiqueData` and its shared handlers
- `PhysiqueData` the per-origin state, it holds no logic or serialization
- `PhysiqueInstance<D>` held by the `PhysiqueHolder`, wraps a Physique, its id and its data. Every hook receives the instance

#### Methods
| Method | Purpose |
|---|---|
| `getType()` | links the physique with the type that created it, required for datapack registries |
| `getName(data)` / `getDescription(data)` | display Components, `data` is null when shown without an owner (physique essences, tooltips) |
| `onAdded(source, instance)` | ran when the physique is set on an origin source. Returns the paths to try and unlock, they are owned by the physique's id |
| `onRemoved(source, instance)` | ran when the physique is removed, before the new physique is added. Returns the paths to try and remove (a path with other owners is kept) |
| `applyToEntity(entity, instance)` | ran when an entity attaches to the source or the physique was added, for entity only bonuses |
| `removeFromEntity(entity, instance)` | ran when an entity detaches or the physique was removed, cleans up `applyToEntity` |
| `newData` / `serializerHandler` / `syncHandler` | create, save and sync the data. `syncHandler().decode` writes into `newData()` |
| `itemTooltip()` | the tooltip the physique essence displays |

#### Changing the physique
```java
AscensionOriginSourceHelper.setPhysique(source, physiqueId);           //fresh instance, checks requirements
AscensionOriginSourceHelper.setPhysique(source, instance, false);      //an instance you created
PhysiqueInstance<?> current = AscensionOriginSourceHelper.getPhysique(source);
```
`PhysiqueChangedEvent.Pre` carries the old and new `PhysiqueInstance` and can swap the new one with `setNewPhysique(id, access)` or `setNewPhysiqueInstance(instance)`. New paths are added before the old ones are removed, so a path both physiques unlock keeps an owner. After changing a physique's data call `markPhysiqueDirty`.

### Tribulations
Tribulations differ from other datapack types in that the registry entry holds the logic as well as the configuration.
A tribulation is always defined in the tribulation registry (`data/<namespace>/ascension/tribulations/`) and is referenced by its registry id, it cannot be defined inline.

A tribulation is split into 4 parts
- `TribulationType` provides the codec for the Tribulation, the codec for its TribulationData, and the `StreamCodec` used to sync that data to clients
- `Tribulation` the registry entry. Contains the configuration (strikes, damage, delay etc.) and the logic (`tick`, `onStart`, `onFinished`, `onTargetDeath`)
- `TribulationData` created when a tribulation starts. It is saved with the active tribulation and can be kept after it is completed (paths keep it per realm, using `onAdded`/`onRemoved` to apply bonuses)
- `TribulationInstance` created by the `TribulationManager`, wraps a Tribulation, its data, its position and an optional target. You never create these directly

#### Starting a tribulation
```java
UUID id = TribulationManager.get(server)
        .create(Identifier.parse("ascension:test_tribulation"))
        .targeting(entity, DimensionChangeBehaviour.STAY) //optional, use at(level, pos) for a tribulation without a target
        .onSuccess(new RealmChangeResolutionAction(pathId, realm, 1))
        .onFailure(new EndBreakthroughResolutionAction(pathId))
        .start();
```
Inside `tick` call `instance.succeed()` or `instance.fail()` to finish the tribulation.
If the tribulation tracks an entity, extend `EntityTargetingTribulation`, which splits ticking into `tickWithTarget` and `tickWithoutTarget`.

#### Resolution actions
`TribulationResolutionAction`s are saved alongside the active tribulation, and run once it finishes, only the success or the failure list runs.
Since the world or datapack may have changed between starting and finishing, an action should verify any state it depends on.
For example `RealmChangeResolutionAction` checks the target is still at the realm the tribulation started at, and that the realm still has the same tribulation.
Actions are registered in `TRIBULATION_RESOLUTION_ACTION_TYPE_REGISTRY` and need a codec.
If an action `requiresLoadedTarget()` and the target is not loaded when the tribulation finishes, resolution waits for the target to join the world again.

#### Lifecycle
- tribulations stay in memory while the server runs, but only tick while the chunk they are in is loaded
- an entity tracking tribulation follows its target, `DimensionChangeBehaviour` decides what happens when the target changes dimension (`STAY` by default, `FOLLOW` or `FAIL`)
- on load an active tribulation is discarded without running any logic if its id is no longer in the registry, or its data/actions fail to load. `Tribulation.validateLoadedData` can also reject or adjust loaded data
- `/reload` does not affect running tribulations, restart the server for datapack changes to apply

#### Client
Tribulations are synced to every player tracking the chunk they are in. Register a `TribulationRenderer` for your type with `TribulationRenderers.register(type, renderer)` during client setup to get `clientTick` and `render` hooks. Clients receive the same `TribulationData` as the server instance (`ClientTribulation.getData()`). It is encoded with `TribulationType.dataStreamCodec()`, which defaults to the data codec, override it for a more compact network format. Call `instance.markDataDirty()` when the data changes so it is resent.

### Skills
A skill is a registry entry (`data/<namespace>/ascension/skills/`) that holds the configuration and logic, the per-origin state lives in its `SkillData`.

A skill is split into 3 parts
- `Skill<D>` the registry entry, `D` is the `SkillData` it creates and runs on. It provides `newData()`, a `SerializerHandler<D>` (saving through ValueInput/ValueOutput) and a `SyncHandler<D>` (syncing through a ByteBuf)
- `SkillData` the per-origin state, it holds no logic or serialization
- `SkillInstance<D>` held by the `SkillHolder`, wraps a Skill, its id and its data. Every hook (`onAdded`, `onRemoved`, `applyToEntity`, `removeFromEntity`) receives the instance, so a skill always gets its own typed data and its id

#### Data handlers
- `serializerHandler().read` is expected to return new data
- `syncHandler().decode` writes into existing data, so always call `newData()` first and decode into that. Do not use `SyncHandler.StreamCodecSyncHandler` for skills, its decode does nothing
- each skill's data is length prefixed when synced, so a skill the client does not know is skipped instead of breaking the rest of the sync

#### Accessing skills
```java
SkillInstance<?> instance = AscensionOriginSourceHelper.getSkill(source, skillId);
//narrow to a concrete skill class to get typed data
SkillInstance<PassiveSkill.Data> passive = instance.as(PassiveSkill.class);
```
For toggleable skills use `ToggleableSkills` (`isEnabled`, `enable`, `disable`, `tick`), it handles the typing for you.

Skills are added and removed with an owner (`AscensionOriginSourceHelper.addSkill(source, skill, owner)`), a skill is only removed once every owner has removed it.
After changing a skill's data call `AscensionOriginSourceHelper.markSkillDirty(source, skillId)`, partial syncs only send dirty and removed skills.

#### Castable skills
A `CastableSkill<D, C, E>` can be slotted in the hot bar and cast. `C` is the `CastData` of a single cast, `E` is the `EquipSkillData` kept while the skill is slotted (use `EquipSkillData.Empty` for none).

- `SkillSlot<D, E>` wraps a slotted skill. It receives `onEquip`/`onUnequip`, and `onSelected`/`onUnselected` when the slot gains or loses selection (both sides, useful for UI). `getSkillData()`/`markSkillDataDirty()` reach the owner's skill data, `markEquipDataDirty()` resyncs the hot bar
- `CastingSkillInstance<D, C, E>` wraps a cast. It holds the cast data, the equip data of the slot it was cast from, the current phase and the ticks into that phase
- a phase is just an `Identifier`. `startCast` sets the first phase (or ends the cast for an instant skill), `tickCast` decides what each phase does and moves between phases with `setPhase`, and the cast ends with `end(reason)`. `endCast` runs once it has ended, for any reason
- `getPhaseTicks()` is the number of ticks completed in the current phase, it only advances while the cast continues. It is tracked by the server and synced with the phase, so clients render from it
- read the cast input with `isInputHeld()` rather than the action manager directly. It reads the caster's zenithlib `ActionManager`, which every living entity has: players hold it with the cast key, a boss or mob AI calls `actionStart`/`actionEnd` with `CastingSkillInstance.CAST_INPUT` itself
- `SkillCastEvent` (`Start.Pre` cancellable, `Start.Post`, `PhaseChanged`, `End`) is posted on the server only
- any `LivingEntity` can cast through its `SkillCaster` (`SkillCaster.of(entity).startCast(...)`). Players additionally have a `SkillCastHandler` holding the hot bar, synced only to that player
- the cast is synced to everyone tracking the caster when it starts, ends, changes phase or its cast data changes. Clients advance the phase ticks between syncs, only the casting player's own client runs skill logic (prediction), other clients only display the cast

### Techniques
A technique is a registry entry (`data/<namespace>/ascension/techniques/`) that holds the configuration and logic, the per-origin state lives in its `TechniqueData`. It follows the same pattern as skills.

A technique is split into 3 parts
- `Technique<D>` the registry entry, `D` is the `TechniqueData` it creates and runs on. It provides `newData()`, a `SerializerHandler<D>` (saving through ValueInput/ValueOutput) and a `SyncHandler<D>` (syncing through a ByteBuf)
- `TechniqueData` the per-origin state, it holds no logic or serialization
- `TechniqueInstance<D>` held by the `TechniqueHolder`, wraps a Technique, its id and its data. Every hook (`onAdded`, `onRemoved`, `applyToEntity`, `removeFromEntity`) receives the instance, so a technique always gets its own typed data and its id

`getName` and `getDescription` receive the typed data so they can change with custom progression. The data is `null` when the technique is shown without an owner (technique manuals, tooltips), so always handle that case. Use `instance.getName()` when you have an instance.

#### Data handlers
- `serializerHandler().read` is expected to return new data
- `syncHandler().decode` writes into existing data, so always call `newData()` first and decode into that. Do not use `SyncHandler.StreamCodecSyncHandler`, its decode does nothing
- each technique's data is length prefixed when synced, so a technique the client does not know is skipped instead of breaking the rest of the sync

#### Accessing techniques
```java
TechniqueInstance<?> instance = AscensionOriginSourceHelper.getTechnique(source, techniqueId);
//narrow to a concrete technique class to get typed data
TechniqueInstance<SimpleTechnique.Data> simple = instance.as(SimpleTechnique.class);
```
Techniques are added with `AscensionOriginSourceHelper.addTechnique(source, techniqueId)`, which checks the technique's requirements, or `addTechnique(source, instance, checkRequirements)` to add an instance you created yourself.
After changing a technique's data call `AscensionOriginSourceHelper.markTechniqueDirty(source, techniqueId)`, partial syncs only send dirty and removed techniques.

`TechniqueEvent` (`Added.Pre`/`Removed.Pre` cancellable, `Added.Post`, `Removed.Post`) carries the `TechniqueInstance`.

### Bloodlines
Bloodlines follow the same pattern as skills and techniques.
- `Bloodline<D>` the registry entry (`data/<namespace>/ascension/bloodlines/`), provides `newData()`, a `SerializerHandler<D>` and a `SyncHandler<D>`
- `BloodlineData` the per-origin state. Every bloodline tracks a purity (`getPurity`/`setPurity`), purity progression actions and conditions rely on it
- `BloodlineInstance<D>` held by the `BloodlineHolder`, every hook (`onAdded`, `onRemoved`, `purityUp`, `purityDown`, `applyToEntity`, `removeFromEntity`) receives it. `onAdded`/`onRemoved` return the paths the bloodline wants to add or remove, they are owned by the bloodline's id

Change purity with `instance.handlePurityChange(source, newPurity)`, it clamps the value and steps through every purity in between so each step runs its actions.

```java
BloodlineInstance<?> instance = AscensionOriginSourceHelper.getBloodline(source, bloodlineId);
BloodlineInstance<SimpleBloodlineData> simple = instance.as(SimpleBloodline.class);
```
`AscensionOriginSourceHelper.addBloodline(source, instance, checkRequirements)` adds the instance, if the bloodline is already held its purity is merged into the held one instead. After changing a bloodline's data call `markBloodlineDirty`.

### Paths
Paths follow the same pattern, but the instance also exposes the realm and progression queries everything else uses.
- `Path<D>` the registry entry (`data/<namespace>/ascension/paths/`). It holds the realm definitions **and all the progression logic**: `progress`, `changeRealm`, `simulateProgression`, `removeFromSource` and the queries that depend on its definitions (`getMaxProgress`, `canProgress`, `canBreakthrough`, `getNextRealm` ...)
- `PathData` the per-origin state, it exposes the read only realm and progress getters (`getCurrentMajorRealm`, `getCurrentMinorRealm`, `getCurrentRealm`, `getProgress`)
- `PathInstance<D>` held by the `PathHolder`. It forwards every query and action to its path, so callers use `instance.progressPath(...)`, `instance.getNextRealm()`, `instance.handleRealmChange(...)` without knowing the path type

The instance is passed as the context data of the path's progress actions, `RealmChangeAction`/`RealmChangeActionCondition` read the realm from it.

`SimplePath` keeps its state in `SimplePathData`, whose fields and mutators are package private: only `SimplePath` can change them. Code outside the package narrows the instance and calls the path:
```java
PathInstance<SimplePathData> simple = instance.as(SimplePath.class);
((SimplePath) simple.getPath()).limitBreakRealm(source, simple, majorRealm, ownerId);
```
Paths are ownership counted (`addPath(source, pathId, owner)`), saved paths are cached until an owner adds them again and are simulated once the source finishes loading. After changing a path's data call `markPathDirty`.

#### Path interactions and affinity
Raw affinity lives in the `ascension:affinity` path bonus category. Use `PathInteractionUtil` instead of reading it directly when the interactions should apply:
- `getEffectiveAffinity(entity | PathBonusProvider | ToDoubleFunction<Identifier>, path)` applies related, generative and destructive interactions (formula in `DATAPACK_HELP.md#path-interactions`). It works for any provider, including chunk affinity.
- `getPathMultiplier(provider, category, path)` returns `1 + bonus` for every other category. These never interact with other paths.
- `getCollisionMultiplier(attacker, defender, path)` compares both sides' effective affinities when a path-tagged attack lands. `AscensionDamageHandler` applies it automatically to any damage carrying an `AscensionDamageTypeHolders.PATH` holder.

The tuning constants (`C`, `W`, `T_MAX`) are at the top of `PathInteractionUtil`.
