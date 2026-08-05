# Technique Skill Systems

This page documents the reusable datapack systems currently used to build instant skills, held casts, targeting, movement, projectiles, fields, anchor networks, constructs and temporary effects.

## Registry Folders

Within a datapack namespace, Ascension content uses these registry folders:

| Content | Folder |
|---|---|
| Skills | `ascension/skills/` |
| Temporary effects | `ascension/skill_effects/` |
| Virtual projectiles | `ascension/virtual_projectiles/` |
| Area fields | `ascension/area_fields/` |
| Anchor networks | `ascension/anchor_networks/` |
| Owner-bound constructs | `ascension/constructs/` |

For example, `data/example/ascension/skills/fire_bolt.json` registers `example:fire_bolt`.

## Choosing a Skill Type

Use `ascension:active_skill` for an instant cast with level-specific execution, upfront costs and cooldowns.

Use `ascension:held_cast` when charging, cumulative cost, movement restriction, interruption or charge stages are part of the skill.

Both types release through the same `execution` object.

| Built In |
|---|
| [Active Skill](#active-skill) |
| [Held Cast](#held-cast) |

## Common Skill Fields

Both built-in skill types share these fields:

| Field | Meaning |
|---|---|
| `type` | Namespaced skill type |
| `name` | Display name as a Component |
| `description` | Description as a Component |

Example translatable Component:

```json
{
  "translate": "ascension.skill.example.name"
}
```

## Scaled Values

Most numeric skill fields use a `ScaledValue` rather than a raw number.

```json
{
  "base": 10.0,
  "terms": [
    {
      "source": {
        "type": "ascension:stat",
        "stat": "ascension:spirit"
      },
      "operation": "add",
      "scale": 0.5
    }
  ],
  "minimum": 0.0,
  "maximum": 100.0
}
```

A term resolves its source, raises it to `power`, multiplies it by `scale`, adds `offset`, then applies `operation` to the running value.

Operations are:

- `add`
- `multiply`
- `set`

Built-in sources are:

| Type | Important fields |
|---|---|
| `ascension:constant` | `value` |
| `ascension:skill_level` | Optional `skill`; omitted means the executing skill |
| `ascension:charge` | No additional fields; returns held-charge progress from `0.0` to `1.0` |
| `ascension:stat` | `stat`, optional `base` |
| `ascension:affinity` | `path`, optional `category`, optional `base` |
| `ascension:context` | `key`, optional `fallback` |

Term order matters.

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

- `targeting` resolves entity or position targets.
- `require_targets` defaults to `true`.
- `caster_features` run once at the cast origin.
- `target_features` run once for every resolved entity or position.

For active skills, targeting is resolved before costs are paid. Held casts pay their cumulative charging cost while charging, then resolve this execution when released.

## Active Skill

```json
{
  "type": "ascension:active_skill",
  "name": "Example Skill",
  "description": "Example description",
  "default_accessible_level": 1,
  "experience_requirements": [
    100.0
  ],
  "levels": [
    {
      "execution": {
        "targeting": {
          "type": "ascension:ray",
          "range": {
            "base": 16.0
          },
          "width": {
            "base": 0.25
          },
          "filter": {
            "relations": [
              "hostile"
            ],
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
          "amount": {
            "base": 10.0
          }
        }
      ],
      "cooldown": {
        "base": 100.0
      }
    }
  ]
}
```

### Active Skill Fields

| Field | Meaning |
|---|---|
| `default_accessible_level` | Default **level cap**, clamped to the number of `levels` |
| `experience_requirements` | Experience required to advance from each current level |
| `levels` | Complete effective-level definitions |

Each level contains:

- `execution`
- Optional `costs`
- Optional `cooldown`

Costs use the selected resource's units. Cooldown resolves to ticks and is rounded to a non-negative integer.

### Making an Active Skill Usable

`default_accessible_level` does **not** grant a trained level or level floor. A newly created active-skill data object begins with trained level `0` and floor `0`.

The permanent usable level is:

```text
min(max(trained level, level floor), accessible level cap)
```

A technique that grants an immediately usable active skill should normally add a floor and cap contribution:

```json
{
  "type": "ascension:set_skill_level",
  "skill": "example:fire_bolt",
  "contribution": "example:manual/realm_1",
  "level": 1,
  "set_floor": true,
  "set_cap": true
}
```

Without a trained level or floor of at least `1`, an active skill whose first definition is level `1` will report that its skill level is not accessible.

Level contributions are identified independently. Removing one contribution does not erase unrelated floors or caps.

## Held Cast

Held casts are not currently levelled. Their values may still scale from charge, stats, affinity, context, or the level of another explicitly referenced skill.

```json
{
  "type": "ascension:held_cast",
  "name": "Example Held Skill",
  "description": "Example description",
  "cast": {
    "minimum_charge": 20,
    "maximum_charge": 80,
    "cooldown": 100,
    "cost": {
      "resource": "ascension:qi",
      "source": "ascension:skill_casting",
      "release_on_failure": true,
      "cumulative_cost": {
        "base": 4.0,
        "terms": [
          {
            "source": {
              "type": "ascension:charge"
            },
            "operation": "add",
            "scale": 16.0
          }
        ]
      }
    },
    "movement": {
      "horizontal_drag": {
        "base": 0.65
      },
      "vertical_drag": {
        "base": 1.0
      },
      "disable_sprinting": true
    },
    "interruption": {
      "damage_threshold": {
        "base": 8.0
      },
      "accumulation_window": 20,
      "release_after_minimum": false
    },
    "stages": []
  },
  "execution": {
    "targeting": {
      "type": "ascension:radial",
      "radius": {
        "base": 5.0
      },
      "filter": {
        "relations": [
          "hostile"
        ],
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

### Held Cast Fields

`cast` supports:

| Field | Default | Meaning |
|---|---:|---|
| `minimum_charge` | `0` | Minimum ticks required before release can execute |
| `maximum_charge` | `40` | Charge ticks corresponding to charge `1.0` |
| `cooldown` | `0` | Cooldown ticks after a successful release |
| `cost` | Omitted | Optional cumulative resource cost |
| `movement` | No drag | Per-tick movement restrictions |
| `interruption` | Omitted | Optional damage-based interruption |
| `stages` | Empty | Charge presentation stages |

Cumulative cost is paid incrementally. Each tick pays only the difference between the newly resolved cumulative target and the amount already paid. Cancelling does not refund paid cost.

`release_on_failure` allows an out-of-resource cast to release after the minimum charge has been reached.

Movement multipliers are clamped between `0.0` and `1.0`.

Interruption supports:

- `damage_threshold`
- `accumulation_window`
- `release_after_minimum`

Charge stages support:

- `threshold`, from `0.0` to `1.0`
- Optional `particle_field`
- Optional periodic `sounds`

Stages are sorted by threshold. Duplicate thresholds keep the last decoded entry, and a stage at threshold `0.0` is inserted when absent.

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
{
  "type": "ascension:self"
}
```

Returns the caster as an entity target.

### Ray

```json
{
  "type": "ascension:ray",
  "range": {
    "base": 16.0
  },
  "width": {
    "base": 0.25
  },
  "filter": {
    "relations": [
      "hostile"
    ]
  }
}
```

A ray returns at most one entity target.

### Cone

```json
{
  "type": "ascension:cone",
  "range": {
    "base": 12.0
  },
  "angle": {
    "base": 45.0
  },
  "maximum_targets": 0,
  "sort": "closest_to_view",
  "filter": {
    "relations": [
      "hostile"
    ]
  }
}
```

### Radial

```json
{
  "type": "ascension:radial",
  "radius": {
    "base": 6.0
  },
  "maximum_targets": 0,
  "sort": "nearest",
  "filter": {
    "relations": [
      "hostile"
    ]
  }
}
```

### Looked-at Position

```json
{
  "type": "ascension:look_position",
  "range": {
    "base": 24.0
  },
  "include_fluids": false,
  "fallback_to_maximum_range": false
}
```

When `fallback_to_maximum_range` is false, missing the world produces no target.

`maximum_targets: 0` means unlimited.

Sort values are:

- `nearest`
- `furthest`
- `lowest_health`
- `highest_health`
- `closest_to_view`

### Target Filters

```json
{
  "relations": [
    "hostile",
    "neutral"
  ],
  "include_players": true,
  "line_of_sight": true
}
```

Relations are:

- `self`
- `ally`
- `neutral`
- `hostile`

When the entire `filter` field is omitted from a built-in targeting definition, the targeting definition defaults to hostile entities, includes players, and requires line of sight.

When `filter` is present as an empty object, `TargetFilterDefinition` itself defaults to neutral and hostile entities. Write `relations` explicitly when the distinction matters.

## Execution Features

All features use a `type` field.

| Type | Purpose |
|---|---|
| `ascension:message` | Send a chat or overlay Component |
| `ascension:sound` | Play a sound |
| `ascension:particle_burst` | Spawn a one-off particle burst |
| `ascension:resource_transaction` | Consume, restore, generate, drain, or accumulate a resource |
| `ascension:frozen_buildup` | Add frozen buildup |
| `ascension:skill_effect` | Apply a registered temporary effect |
| `ascension:movement` | Move the caster or target |
| `ascension:movement_anchor` | Set or clear a saved position |
| `ascension:spawn_projectile` | Spawn a virtual projectile |
| `ascension:runtime_object` | Spawn, remove, or restore a runtime object |

### Message

```json
{
  "type": "ascension:message",
  "message": "Calibration complete",
  "overlay": true
}
```

### Sound

```json
{
  "type": "ascension:sound",
  "sound": "minecraft:block.amethyst_block.chime",
  "volume": {
    "base": 1.0
  },
  "pitch": {
    "base": 1.0
  }
}
```

### Particle Burst

```json
{
  "type": "ascension:particle_burst",
  "particle": "minecraft:end_rod",
  "count": {
    "base": 12.0
  },
  "spread": {
    "base": 0.5
  },
  "speed": {
    "base": 0.05
  }
}
```

### Resource Transaction

```json
{
  "type": "ascension:resource_transaction",
  "target": "caster",
  "resource": "ascension:qi",
  "operation": "restore",
  "source": "ascension:skill_casting",
  "amount": {
    "base": 10.0
  }
}
```

Targets are `caster` and `target`.

Operations are:

- `consume`
- `accumulate`
- `restore`
- `generate`
- `drain`

### Frozen Buildup

```json
{
  "type": "ascension:frozen_buildup",
  "amount": {
    "base": 0.25
  },
  "decay_delay": 40
}
```

### Skill Effect

```json
{
  "type": "ascension:skill_effect",
  "effect": "ascension:frozen_form",
  "duration": {
    "base": 100.0
  },
  "potency": {
    "base": 1.0
  }
}
```

Duration resolves in ticks.

### Movement

```json
{
  "type": "ascension:movement",
  "mode": "directional",
  "subject": "caster",
  "distance": {
    "base": 6.0
  },
  "direction": "look",
  "include_vertical": true,
  "collision": "stop_before_collision",
  "preserve_velocity": false
}
```

Modes are:

- `directional`: uses `distance`, `direction`, and `include_vertical`
- `target_position`: uses `maximum_distance`, `stopping_distance`, and `vertical_offset`
- `anchor`: uses `anchor`, `consume_anchor`, and `restore_rotation`

Subjects are:

- `caster`
- `target`

Directions are:

- `look`
- `toward_target`
- `away_from_target`

Collision values are:

- `fail`
- `stop_before_collision`

Anchor movement only succeeds in the same dimension as the saved anchor.

### Movement Anchor

```json
{
  "type": "ascension:movement_anchor",
  "action": "set",
  "anchor": "ascension:aquila_echo",
  "duration": {
    "base": 100.0
  }
}
```

Actions are `set` and `clear`.

A non-positive duration creates an anchor without automatic expiry.

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

Kinds are:

- `area_field`
- `anchor_network`
- `construct`

Actions are:

- `spawn`
- `remove`
- `restore`

`remove` removes runtime objects owned by the caster that match `definition`.

`restore` currently applies only to constructs. It resolves `amount` and adds that value to each matching construct's stability. Negative values reduce stability.

## Virtual Projectiles

```json
{
  "speed": {
    "base": 2.0
  },
  "range": {
    "base": 32.0
  },
  "gravity": 0.0,
  "hit_radius": 0.3,
  "pierces": 0,
  "filter": {
    "relations": [
      "hostile"
    ],
    "include_players": true,
    "line_of_sight": false
  },
  "flight_particle": "minecraft:end_rod",
  "behaviors": [],
  "on_entity_hit": [],
  "on_block_hit": [],
  "on_expire": [],
  "visual": "ascension:example_projectile"
}
```

- `speed` is blocks per tick.
- `range` is the maximum travel distance.
- `gravity` is clamped between `-4.0` and `4.0`.
- `hit_radius` is clamped between `0.0` and `4.0`.
- `pierces: 0` stops after the first entity hit. Higher values allow that many additional pierces.
- `flight_particle` works without a runtime visual controller.
- Hit and expiry lists contain normal execution features.

The built-in `ascension:homing` behaviour accepts:

```json
{
  "type": "ascension:homing",
  "turn_rate": {
    "base": 0.12
  },
  "reacquire": true,
  "acquisition": {
    "type": "ascension:radial",
    "radius": {
      "base": 12.0
    },
    "maximum_targets": 1,
    "sort": "nearest",
    "filter": {
      "relations": [
        "hostile"
      ],
      "line_of_sight": false
    }
  }
}
```

`turn_rate` is the maximum steering angle in radians per tick. Reacquisition only occurs when `reacquire` is true and an `acquisition` definition is supplied.

## Area Fields

```json
{
  "shape": "cylinder",
  "radius": {
    "base": 5.0
  },
  "height": {
    "base": 4.0
  },
  "duration": {
    "base": 160.0
  },
  "tick_interval": 20,
  "filter": {
    "relations": [
      "hostile"
    ]
  },
  "on_enter": [],
  "on_tick": [],
  "on_exit": [],
  "visual": "ascension:example_field"
}
```

Shapes are:

- `sphere`
- `cylinder`

Duration resolves in ticks.

`on_exit` runs when a tracked entity leaves the field and when the field reaches its natural expiry. Explicit removal currently cleans up the field without running `on_exit`.

## Anchor Networks

Anchor networks are the technical runtime for linked nodes. Technique names may still call them formations, arrays or constellations.

```json
{
  "duration": {
    "base": 200.0
  },
  "rotate_with_caster": true,
  "nodes": [
    {
      "id": "ascension:north",
      "offset": [
        0.0,
        0.0,
        -3.0
      ]
    },
    {
      "id": "ascension:south",
      "offset": [
        0.0,
        0.0,
        3.0
      ]
    }
  ],
  "links": [
    {
      "from": "ascension:north",
      "to": "ascension:south"
    }
  ],
  "field": "ascension:example_field",
  "visual": "ascension:example_network"
}
```

`field` is optional and creates one child area field at the network centre.

`rotate_with_caster` rotates node offsets using the caster's facing when the network is spawned. It does not continuously rotate the deployed network afterward.

Removing or expiring the network also removes its child field.

## Owner-bound Constructs

```json
{
  "duration": {
    "base": 240.0
  },
  "stability": {
    "base": 100.0
  },
  "offset": [
    0.0,
    1.5,
    -0.75
  ],
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

Constructs follow their owner and expose current and maximum stability.

Visual stages are sorted by `maximum_stability_fraction`. The first stage whose threshold is greater than or equal to the current stability fraction is selected. The base `visual` is used above all stage thresholds.

Constructs are runtime state, not normal mob entities. Their current implementation provides ownership, duration, position, stability, restoration, removal and visual-state synchronization. Defensive interception, attacks, collision and other martial behaviour require additional shared systems.

## Temporary Effect Ownership

Temporary effect definitions may define how repeated applications are grouped:

```json
{
  "stacking": "refresh",
  "stacking_scope": "source_entity",
  "max_stacks": 3,
  "modules": []
}
```

Stacking policies are:

- `refresh`
- `stronger_replaces`
- `stack`

Scopes are:

- `definition`
- `source_entity`
- `source_skill`
- `source_entity_and_skill`
- `independent`

The default policy is `stronger_replaces`, the default scope is `definition`, and the default maximum stack count is `1`.

## Runtime Visuals

The `visual` fields on projectiles, fields, networks and constructs are client controller identifiers, not datapack visual definitions in the current source.

The server synchronizes compact `RuntimeVisualState` data. A client renderer appears only when code registers a matching controller through `ClientRuntimeVisuals.register(...)`.

At present, the shared synchronization pipeline exists, but there is no built-in datapack-driven visual-layer registry. A `visual` identifier with no registered controller produces no persistent rendering. Normal feature particles, sounds and projectile `flight_particle` still work.

## Reloads and Authority

Runtime objects store registry IDs and re-resolve their definitions.

When a referenced definition disappears:

- Active temporary effects are removed safely.
- In-progress or released behaviour that can no longer resolve ends safely.
- Projectiles, fields, networks and constructs stop rather than retaining stale definition objects.

Targeting, costs, movement, projectile collision, field membership, construct stability and effect state are server-authoritative.

Clients receive synchronized attachments and compact presentation packets only.
