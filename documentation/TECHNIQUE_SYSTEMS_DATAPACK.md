# Technique Skill Systems

This page covers the reusable datapack systems used to build active and held skills.

## Registry Folders

| Content | Folder |
|---|---|
| Skills | `ascension/skills/` |
| Temporary effects | `ascension/skill_effects/` |
| Virtual projectiles | `ascension/virtual_projectiles/` |
| Area fields | `ascension/area_fields/` |
| Anchor networks | `ascension/anchor_networks/` |
| Owner-bound constructs | `ascension/constructs/` |

## Choosing a Skill Type

Use `ascension:active_skill` for an instant cast with an upfront cost and cooldown.
Use `ascension:held_cast` when charge time, cumulative cost, interruption or movement restrictions are part of the skill.
Both types use the same `execution` object.

| Built In |
|---|
| [Active Skill](#active-skill) |
| [Held Cast](#held-cast) |

## Common Skill Fields

Both skill types below share these top-level fields.

| Fields |
|---|
| [`type`](#type) |
| [`name`](#name) |
| [`description`](#description) |

##### `type`
The namespaced identifier for the skill type, e.g. `ascension:active_skill` or `ascension:held_cast`. This is what tells the codebase which codec should read the rest of the file — see Basics in `DATAPACK_HELP.md`.

##### `name`
The name of the skill as a Component.

##### `description`
The description of the skill as a Component.

## Shared Execution

```json
{
  "execution": {
    "targeting": {
      "type": "ascension:self"
    },
    "require_targets": true,
    "caster_features": [],
    "target_features": []
  }
}
```

`caster_features` run once at the caster. `target_features` run once for every resolved entity or position. Targeting is resolved before active-skill costs are paid.

## Active Skill

```json
{
  "type": "ascension:active_skill",
  "name": "Example Skill",
  "description": "Example description",
  "default_accessible_level": 1,
  "levels": [
    {
      "execution": {
        "targeting": {
          "type": "ascension:ray",
          "range": { "base": 16.0 },
          "width": { "base": 0.25 },
          "filter": {
            "relations": ["hostile"],
            "include_players": true,
            "line_of_sight": true
          }
        },
        "require_targets": true,
        "caster_features": [],
        "target_features": []
      },
      "costs": [
        {
          "resource": "ascension:qi",
          "source": "ascension:skill_casting",
          "amount": { "base": 10.0 }
        }
      ],
      "cooldown": { "base": 100.0 }
    }
  ]
}
```

| Fields |
|---|
| [`type`](#type) |
| [`name`](#name) |
| [`description`](#description) |
| [`default_accessible_level`](#default_accessible_level) |
| [`levels`](#levels) |

##### `default_accessible_level`
The level of the skill a player can access by default.

##### `levels`
Each entry in `levels` is a complete effective-level definition, using the same shape as [Shared Execution](#shared-execution) plus `costs` and `cooldown`. Costs and cooldowns use ticks.

## Held Cast

```json
{
  "type": "ascension:held_cast",
  "name": "Example Held Skill",
  "description": "Example description",
  "cast": {
    "minimum_charge": 20,
    "maximum_charge": 80,
    "cooldown": 100,
    "stages": []
  },
  "execution": {
    "targeting": {
      "type": "ascension:radial",
      "radius": { "base": 5.0 },
      "filter": {
        "relations": ["hostile"],
        "include_players": true,
        "line_of_sight": true
      }
    },
    "require_targets": false,
    "caster_features": [],
    "target_features": []
  }
}
```

| Fields |
|---|
| [`type`](#type) |
| [`name`](#name) |
| [`description`](#description) |
| [`cast`](#cast) |
| [`execution`](#execution) |

##### `cast`
Controls charging behaviour: `minimum_charge`, `maximum_charge`, `cooldown` and `stages`, all in ticks. Not individually broken out yet in this reference.

##### `execution`
Uses the same targeting and features as [Shared Execution](#shared-execution). Held casts own charging, cumulative costs, interruptions and charge visuals — their release uses the same targeting and features as active skills.

## Targeting

| Built In |
|---|
| [Self](#self) |
| [Ray](#ray) |
| [Cone](#cone) |
| [Radial](#radial) |
| [Looked-at Position](#looked-at-position) |

### Self

```json
{ "type": "ascension:self" }
```

### Ray

```json
{
  "type": "ascension:ray",
  "range": { "base": 16.0 },
  "width": { "base": 0.25 },
  "filter": {}
}
```

### Cone

```json
{
  "type": "ascension:cone",
  "range": { "base": 12.0 },
  "angle": { "base": 45.0 },
  "maximum_targets": 0,
  "sort": "closest_to_view",
  "filter": {}
}
```

### Radial

```json
{
  "type": "ascension:radial",
  "radius": { "base": 6.0 },
  "maximum_targets": 0,
  "sort": "nearest",
  "filter": {}
}
```

### Looked-at Position

```json
{
  "type": "ascension:look_position",
  "range": { "base": 24.0 },
  "include_fluids": false,
  "fallback_to_maximum_range": false
}
```

`maximum_targets: 0` means unlimited. Sort values are `nearest`, `furthest`, `lowest_health`, `highest_health` and `closest_to_view`.

### Target Filters

```json
{
  "relations": ["hostile", "neutral"],
  "include_players": true,
  "line_of_sight": true
}
```

Relations are `self`, `ally`, `neutral` and `hostile`. Omitted filters default to hostile targets with line of sight.

## Execution Features

All features use a `type` field.

| Built In |
|---|
| [Movement](#movement) |
| [Movement Anchor](#movement-anchor) |
| [Spawn Projectile](#spawn-projectile) |
| [Runtime Object](#runtime-object) |

### Movement

```json
{
  "type": "ascension:movement",
  "mode": "directional",
  "subject": "caster",
  "distance": { "base": 6.0 },
  "direction": "look",
  "include_vertical": true,
  "collision": "stop_before_collision",
  "preserve_velocity": false
}
```

Modes are:

- `directional`: uses `distance` and `direction`
- `target_position`: uses `maximum_distance`, `stopping_distance` and `vertical_offset`
- `anchor`: uses `anchor`, `consume_anchor` and `restore_rotation`

Subjects are `caster` and `target`. Directions are `look`, `toward_target` and `away_from_target`. Collision values are `fail` and `stop_before_collision`.

### Movement Anchor

```json
{
  "type": "ascension:movement_anchor",
  "action": "set",
  "anchor": "ascension:aquila_echo",
  "duration": { "base": 100.0 }
}
```

Actions are `set` and `clear`. A non-positive duration creates an anchor without automatic expiry.

### Spawn Projectile

```json
{
  "type": "ascension:spawn_projectile",
  "definition": "ascension:example_projectile",
  "direction": "look"
}
```

Direction is `look` or `target`.

### Runtime Object

```json
{
  "type": "ascension:runtime_object",
  "kind": "area_field",
  "action": "spawn",
  "definition": "ascension:example_field"
}
```

Kinds are `area_field`, `anchor_network` and `construct`. Actions are `spawn`, `remove` and `restore`. `restore` currently applies only to constructs and requires an `amount` scaled value.

The existing message, sound, particle burst, resource transaction, frozen buildup and skill-effect features remain available.

## Virtual Projectiles

```json
{
  "speed": { "base": 2.0 },
  "range": { "base": 32.0 },
  "gravity": 0.0,
  "hit_radius": 0.3,
  "pierces": 0,
  "filter": {
    "relations": ["hostile"],
    "include_players": true,
    "line_of_sight": false
  },
  "behaviors": [],
  "on_entity_hit": [],
  "on_block_hit": [],
  "on_expire": [],
  "visual": "ascension:example_projectile"
}
```

The built-in `ascension:homing` behaviour accepts `turn_rate`, optional `acquisition` targeting and `reacquire`.

## Area Fields

```json
{
  "shape": "cylinder",
  "radius": { "base": 5.0 },
  "height": { "base": 4.0 },
  "duration": { "base": 160.0 },
  "tick_interval": 20,
  "filter": {
    "relations": ["hostile"]
  },
  "on_enter": [],
  "on_tick": [],
  "on_exit": [],
  "visual": "ascension:example_field"
}
```

Shapes are `sphere` and `cylinder`.

## Anchor Networks

Anchor networks are the technical runtime for linked nodes. Technique names may still call them formations, arrays or constellations.

```json
{
  "duration": { "base": 200.0 },
  "rotate_with_caster": true,
  "nodes": [
    { "id": "ascension:north", "offset": [0.0, 0.0, -3.0] },
    { "id": "ascension:south", "offset": [0.0, 0.0, 3.0] }
  ],
  "links": [
    { "from": "ascension:north", "to": "ascension:south" }
  ],
  "field": "ascension:example_field",
  "visual": "ascension:example_network"
}
```

`field` is optional and creates one child area field at the network centre.

## Owner-bound Constructs

```json
{
  "duration": { "base": 240.0 },
  "stability": { "base": 100.0 },
  "offset": [0.0, 1.5, -0.75],
  "rotate_with_owner": true,
  "visual": "ascension:example_construct",
  "visual_stages": [
    {
      "maximum_stability_fraction": 0.35,
      "visual": "ascension:example_construct_cracked"
    }
  ]
}
```

Constructs follow their owner and expose stability for later defensive and martial modules. They are not normal mob entities.

## Temporary Effect Ownership

Temporary effects may define how repeated applications are grouped:

```json
{
  "stacking": "refresh",
  "stacking_scope": "source_entity",
  "max_stacks": 3,
  "modules": []
}
```

Scopes are `definition`, `source_entity`, `source_skill`, `source_entity_and_skill` and `independent`.

## Reloads and Authority

Runtime objects store registry IDs and re-resolve definitions. Missing definitions end safely after a datapack reload. Targeting, costs, movement, projectile collision, fields, constructs and effect state are server-authoritative. Clients receive only visual state.