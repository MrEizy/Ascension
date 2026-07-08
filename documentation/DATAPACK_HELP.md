# Datapacks

This page will introduce how to create new techniques, paths, bloodlines, physiques, skills, actions+conditions and tribulations using only datapacks

It will also provide documentation for all built in types

## Setup
For this to work you must add an ascension folder to your datapack
```text
<datapack>/ascension/
```

## Basics
In every datapack registry you must always reference a type, this is required by every file

this type is used to read the file itself.

For more information see Types for developers [Add link]

for example in physiques the top of your file should have this

```json
{
  "type": "ascension:simple_physique"
}
```

this will be a namespace followed by a path.

The rest of the fields for each JSON file is fully determined by the type you are using,
so make sure to have a reference documentation up, not all types will use the same fields or names


### Component
This is a built-in Minecraft codec and has many options, here i will only introduce literal and Translatable

<b>Literal</b>
```json
{
  "name": "Example"
}
```
<b>Translatable</b>
```json
{
  "name": {
    "translate" : "lang.example.name"
  }
}
```
### Base Values
everything from stats to affinities to attributes uses value containers,
all base value fields will expect a map of the container name -> base value
```json
{
  "[field_name]": {
    "ascension:vitality": 2,
    "ascension:strength": 1
  }
}
```
the example above provides +2 to the base of vitality and +1 to the base of strength,
`container` in this context refers to a specific value container.

the available containers depends on the context

### Value Modifier
this is used to provide a modifier to a value container

the formula for value containers is as such
$(base*(1+multiplyBase)+addBase)*(1+multiplyFinal)+addFinal$

addBase and addFinal are the sum of all modifiers of that type

for multiplyBase and multiplyFinal it is a bit different, it first sums
all multipliers of the same group, then multiplies it with all the other groups of the same type
then applies it to the formula

so 20% final multiplier group 1 and 30% final multiplier group 2 becomes 1.2*1.3=1.56
but if they were of the same group it would be 1+(0.2+0.3) = 1.5

Example

```json
{
  "value": 0.2,
  "operation": "MULTIPLY_FINAL",
  "id": "ascension:example",
  "group": "ascension:example_group"
}
```
operation is one of:
- ADD_BASE
- MULTIPLY_BASE
- ADD_FINAL
- MULTIPLY_FINAL

Id is the unique identifier for this modifier, which can be used to easily remove individual modifiers

Group refers the which group this modifier is part of

### Base Affinity
Base affinity is a specialised Base Value for affinity, which includes a <b>category</b>.

Here a category allows us to only apply that affinity in specific circumstances

For Example `ascension:none` would be applied everywhere while `ascension:damage` is only applied on damage

Below you can see the 2 different implementations
```json
{
  "[field_name]": {
    "ascension:essence": 3,
    "ascension:sword": {
      "ascension:none": 1,
      "ascension:damage": 0.5
    }
  }
}
```
above we added 300% affinity to the default essence category,100% to default sword and 50% to damage sword 

For a list of Categories view the wiki
### Affinity Modifier
Similar to Base Affinity, an extension of Value Container Modifier that has an optional category

<b>EITHER</b>
```json
{
  "value": 0.2,
  "operation": "MULTIPLY_FINAL",
  "id": "ascension:example",
  "group": "ascension:example_group"
}
```
<b>OR</b>

```json
{
  "modifier": {
    "value": 0.2,
    "operation": "MULTIPLY_FINAL",
    "id": "ascension:example",
    "group": "ascension:example_group"
  },
  "category": "ascension:damage"
}
```
### Physiques
| Built In                            |  
|-------------------------------------|
| [Simple Physique](#simple-physique) | 

[comment]: <> (add extra columns for things like optional and type and default?)

#### Simple Physique
| Fields                                      |  
|---------------------------------------------|
| [`name`](#name)                             | 
| [`description`](#description)               | 
| [`paths`](#paths)                           | 
| [`skills`](#skills)                         | 
| [`base_stats`](#base_stats)                 | 
| [`stat_modifiers`](#stat_modifiers)         | 
| [`base_affinity`](#base_affinity)           | 
| [`affinity_modifiers`](#affinity_modifiers) | 
| [`item_tooltip`](#simple-physique)          | 

##### `name`
Uses a Component see [Components](#component)
##### `description`
Uses a Component see [Components](#component)
##### `paths`
Takes in a List of Paths as Identifiers, once the player gains the physique they will unlock these paths

Example
```json
{
  "paths": [
    "ascension:foundation/essence",
    "ascension:elemental/water"
  ]
}
```

For a list of available paths either view the datapack or check the wiki

##### `skills`
Takes in a list of skills as Identifiers, once the player gains the physique they will unlock these skills

Example

```json
{
  "skills": [
    "ascension:flight",
    "ascension:iron_skin"
  ]
}
```
For a list of available skills either view the datapack or check the wiki

##### `base_stats`

```json
{
  "base_stats": [
    ...
  ]
  
}
```
Takes in a List of Base Values
For Base Value JSON see [Base Value](#base-value)

The context is stats, so for a list of available stats view the datapack or check the wiki

##### `stat_modifiers`

Takes in a map of stat as an Identifier -> list of modifiers

For Modifier JSON see [Value Modifier](#value-modifier)

Example

```json
{
  "stat_modifiers": {
    "ascension:vitality": [
      {
        "value": 0.2,
        "operation": "MULTIPLY_FINAL",
        "id": "ascension:physique_vitality_multiplier"
      }
    ],
    "ascension:strength": [
      {
        "value": 3,
        "operation": "ADD_BASE",
        "id": "ascension:physique_strength_base"
      }
    ]
  }
}
```
In the example above we provide a 20% final multiplier to Vitality and a +3 to base Strength

##### `base_affinity`

```json
{
  "base_affinity": [
    ...
  ]
  
}
```
Takes in a List of Base Affinities
For Base Affinity JSON see [Base Affinity](#base-affinity)

##### `affinity_modifiers`
```json
{
  "affinity_modifiers": {
    "ascension:water": [
      ...
    ],
    "ascension:essence": [
      ...
    ]
  }
}
```
Takes in an unbounded map with each key being a `path`, each key path takes in a list
of [Affinity Modifiers](#affinity-modifier)
