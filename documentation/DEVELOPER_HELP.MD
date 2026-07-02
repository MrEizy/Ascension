# Developers

This documentation is Targeted at Developers, and assumes a minimum understanding of Java, OOP and the neoforge ecosystem

## Topics



### Datapack Types

If you have read through the datapack documentation or already had a peek at the code
you may have discovered the existence of Physique/Bloodline/Path types etc. These are objects 
that tell Minecraft how to decode a specific java file. this means 2 physiques can have completely different 
JSON formats, yet still produce a valid Physique object.

If one of the built-in datapack types does not suit your use case you will have to make your own.
This is the exact same for all Types except Tribulations, which we will get to later.


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