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
This will give a basic overview of what a physique is expected to provide and how they are handled in Entity Data

#### Methods
| Fields                                    |  
|-------------------------------------------|
| [`getType()`](#getType)                   | 
| [`name()`](#name)                         | 
| [`description()`](#description)           | 
| [`onAdded()`](#onAdded)                   | 
| [`onRemoved()`](#onRemoved)               | 
| [`applyToEntity()`](#applyToEntity)       | 
| [`removeFromEntity()`](#removeFromEntity) | 
| [`newData()`](#newData)                   | 
| [`loadData()`](#loadData)                 |
| [`itemTooltip()`](#itemTooltip)           | 
##### `getType()`
One of the most important methods, this allows the codebase link your physique with the type that created it.
This is required by Neoforge and Minecraft for Datapack Registries.

##### `name()`
The name of the physique as a Component
##### ```description()```
The description of the physique as a Component
##### ```onAdded()```
ran when a physique is added to an OriginSource,
Expects a Collection of Identifiers, with each identifier referring to a path to be unlocked
##### ```onRemoved()```
ran when a physique is removed, and before the new physique is added
Expects a Collection of Identifiers, with each identifier referring to a path we want to TRY remove (try is key, since if a path has multiple sources it will not be removed)
##### ```applyToEntity()```
ran when an entity that listens to an OriginSource is loaded and attached to the source or a new physique was added, This allows you to apply entity only bonuses if you so wish
##### ```removeFromEntity()```
ran when an entity is either removed as a listener from a source or the physique was removed. Allows for the cleanup of anything added in `applyToEntity()`
##### ```newData()```
returns a fresh instance of the PhysiqueData
##### ```loadData()```
two versions, one for decoding physique data from a byteBuffer, and the other from an ValueInput
##### ```itemTooltip()```
handles the tooltip the physique transfer item should display
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
