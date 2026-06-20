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
    "translatable" : "lang.example.name"
  }
}
```
### Base Value
everything from stats to affinities to attributes uses value containers,
to provide a base value to a container you use
```json
{
  "[field_name]": {
    "id": "ascension:vitality",
    "value": 2
  }
}
```
the example above provides +2 to the base of vitality,
`id` in this context refers to a specific value container.

the available ID's depends on the context

### Value Modifier
this is used to provide a modifier to a value container

the formula for value containers is as such
$(base*(1+multiplyBase)+addBase)*(1+multiplyFinal)+addFinal$

addBase and addFinal are the sum of all modifiers of that type

for multiplyBase and multiplyFinal it is a bid different, it first sums
all multipliers of the same group, then multiplies it with all the other groups of the same type
then applies it to the formula

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

There are 2 different formats

<b>EITHER</b>
```json
{
  "[field_name]": {
    "id": "ascension:vitality",
    "value": 2
  }
}
```
<b>OR</b>

### Affinity Modifier

```json
{
  "[field_name]": {
    "base": {
      "id": "ascension:vitality",
      "value": 2
    },
    "category": "ascension:damage"
  }
}
```
### Physiques
| Built In                            |  
|-------------------------------------|
| [Simple Physique](#simple-physique) | 

[comment]: <> (add extra columns for things like optional and type and default?)

#### Simple Physique
| Fields                               |  
|--------------------------------------|
| [`name`](#name)                      | 
| [`description`](#description)        | 
| [`paths`](#paths)                    | 
| [`skills`](#skills)                  | 
| [`base_stats`](#simple-physique)     | 
| [`stat_modifiers`](#simple-physique) | 
| [`base_affinity`](#simple-physique)  | 
| [`item_tooltip`](#simple-physique)   | 

##### `name`
Uses a Component see [Components](#component)
##### `description`
Uses a Component see [Components](#component)
#### `paths`
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

#### `skills`
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

#### `base stats`

For JSON see [Base Value](#base-value)

The context is stats, so for a list of available stats view the datapack or check the wiki

#### `stat_modifiers`

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