# Ascension Technique and Skill Systems

This page documents the datapack format used to create Ascension skills and technique content.

It is written for content authors first. Most skills should be contained in **one JSON file**. 
You should not need to create separate effect, projectile, field, barrier, stagger, or visual files unless the definition is deliberately shared by unrelated skills.

The basic workflow is:

1. Choose a skill type.
2. Add targeting when the skill needs a target or position.
3. Add an ordered `features` list.
4. Put skill-specific runtime definitions directly inside the skill.
5. Use compact numbers for fixed values and full `ScaledValue` objects only when scaling is needed.

---

## Contents

- [Choosing a Skill Type](#choosing-a-skill-type)
- [One-File Definitions](#one-file-definitions)
- [Active Skills](#active-skills)
- [Held Casts](#held-casts)
- [Passive Skills](#passive-skills)
- [Scaled Values](#scaled-values)
- [Targeting](#targeting)
- [Execution Subjects](#execution-subjects)
- [Execution Features](#execution-features)
- [Resources](#resources)
- [Effects and Buildup](#effects-and-buildup)
- [Damage, Stagger, and Defences](#damage-stagger-and-defences)
- [Projectiles and Runtime Objects](#projectiles-and-runtime-objects)
- [Runtime Visuals](#runtime-visuals)
- [Make A Levelled Skill Usable](#make-a-levelled-skill-usable)
- [Complete Examples](#complete-examples)


# Choosing a Skill Type

| Type | Use it for |
|---|---|
| `ascension:active_skill` | Instant casts with targeting, costs, cooldowns, features, and optional levels |
| `ascension:held_cast` | Charged or channelled casts with cumulative cost, movement restriction, stages, and interruption |
| `ascension:simple_passive` | A small always-on stat, affinity, or defensive passive |
| `ascension:resource_modifier_passive` | Composable and optionally levelled passives, including stats, defence, resources, projectiles, toggles, and upkeep |
| `ascension:simple_cultivation_skill` | Cultivation progress, particle fields, and periodic cultivation sounds |

- Press once and resolve immediately: `active_skill`.
- Hold to charge or channel: `held_cast`.
- Apply while owned: passive.
- Cultivate a path: `simple_cultivation_skill`.

---

# One-File Definitions

A skill may contain named local definitions:_

```json
{
  "definitions": {
    "effects": {},
    "projectiles": {},
    "fields": {},
    "networks": {},
    "constructs": {},
    "barriers": {},
    "stagger": {},
    "visuals": {}
  }
}
```

Each definition can be supplied in three ways.

## Inline

Best for a definition used once:

```json
{
  "type": "ascension:projectile",
  "definition": {
    "speed": 2,
    "range": 30,
    "on_entity_hit": [
      {
        "type": "ascension:damage",
        "amount": 8
      }
    ]
  }
}
```

## Local reference

Best when the same skill reuses the definition:

```json
{
  "definitions": {
    "visuals": {
      "impact": {
        "layers": [
          {
            "type": "ascension:ring",
            "geometry": {
              "radius": 1.5
            }
          }
        ]
      }
    }
  },
  "targeting": "self",
  "features": [
    {
      "type": "ascension:visual",
      "definition": "#impact",
      "duration": 12
    }
  ]
}
```

Local references begin with `#` and only search the current skill's matching definition group.

## Global reference

Best for a definition intentionally shared by unrelated skills or addons:

```json
{
  "type": "ascension:effect",
  "definition": "example:shared/hunter_mark",
  "duration": 200
}
```

Use this rule:

- Used once: inline it.
- Reused inside one skill: local `#name`.
- Reused across unrelated skills: global identifier.

---

# Active Skills

An active skill may place its level fields directly at the root. This creates a one-level skill without wrapping everything in `levels`.

```json
{
  "type": "ascension:active_skill",
  "name": "Radiant Step",
  "description": "Dash in the direction you are looking.",
  "default_accessible_level": 1,
  "targeting": "self",
  "require_targets": true,
  "costs": [
    {
      "resource": "ascension:stamina",
      "operation": "ascension:consume",
      "amount": 6
    }
  ],
  "cooldown": 30,
  "features": [
    {
      "type": "ascension:move",
      "subject": "caster",
      "mode": "directional",
      "direction": "look",
      "distance": 5,
      "include_vertical": false
    }
  ]
}
```

## Root fields

| Field | Meaning |
|---|---|
| `targeting` | How targets or positions are resolved |
| `require_targets` | Whether an empty target list fails the cast; default `true` |
| `features` | Ordered list of actions |
| `costs` | Resource transactions paid before execution |
| `cooldown` | Cooldown in ticks |

## Levels and inheritance

Root fields act as defaults for every level. Later levels inherit omitted values from the previous resolved level.

```json
{
  "type": "ascension:active_skill",
  "name": "Radiant Palm",
  "description": "Strike with increasing spiritual force.",
  "default_accessible_level": 3,
  "targeting": {
    "type": "ascension:ray",
    "range": 8
  },
  "costs": [
    {
      "resource": "ascension:qi",
      "operation": "ascension:consume",
      "amount": 10
    }
  ],
  "cooldown": 50,
  "levels": [
    {
      "features": [
        {
          "type": "ascension:damage",
          "amount": 8
        }
      ]
    },
    {
      "features": [
        {
          "type": "ascension:damage",
          "amount": 12
        }
      ]
    },
    {
      "cooldown": 40,
      "features": [
        {
          "type": "ascension:damage",
          "amount": 17
        }
      ]
    }
  ]
}
```

Level 2 inherits targeting, costs, and cooldown. Level 3 also inherits targeting and costs, but replaces the cooldown and feature list.

`features`, `costs`, and other lists are replaced when supplied. They are not appended automatically.

## Costs

```json
{
  "resource": "ascension:qi",
  "operation": "ascension:consume",
  "source": "ascension:skill_casting",
  "amount": 12
}
```

`source` defaults to `ascension:skill_casting`.

All costs must be payable before the cast executes. `ascension:consume` rejects the transaction when the full amount cannot be paid.

---

# Held Casts

Held casts use a `cast` object plus the same root `targeting`, `require_targets`, and `features` fields used by active skills.

```json
{
  "type": "ascension:held_cast",
  "name": "Star Arrow",
  "description": "Charge and release a stellar projectile.",
  "cast": {
    "minimum_charge": 10,
    "maximum_charge": 60,
    "cooldown": 100,
    "cost": {
      "resource": "ascension:qi",
      "operation": "ascension:consume",
      "cumulative_cost": {
        "base": 4,
        "terms": [
          {
            "source": {
              "type": "ascension:charge"
            },
            "scale": 16
          }
        ]
      },
      "release_on_failure": true
    },
    "movement": {
      "horizontal_drag": 0.7,
      "vertical_drag": 1,
      "disable_sprinting": true
    },
    "interruption": {
      "damage_threshold": 10,
      "accumulation_window": 20,
      "release_after_minimum": false
    },
    "stages": []
  },
  "targeting": {
    "type": "ascension:look_position",
    "range": 32,
    "fallback_to_maximum_range": true
  },
  "features": [
    {
      "type": "ascension:projectile",
      "direction": "target",
      "definition": {
        "speed": {
          "base": 1.5,
          "terms": [
            {
              "source": {
                "type": "ascension:charge"
              },
              "scale": 0.8
            }
          ]
        },
        "range": 36,
        "on_entity_hit": [
          {
            "type": "ascension:damage",
            "amount": {
              "base": 6,
              "terms": [
                {
                  "source": {
                    "type": "ascension:charge"
                  },
                  "scale": 18
                }
              ]
            }
          }
        ]
      }
    }
  ]
}
```

## Held-cast fields

| Field | Default | Meaning |
|---|---:|---|
| `minimum_charge` | `0` | Required charge ticks before release |
| `maximum_charge` | `40` | Charge ticks that equal charge `1.0` |
| `cooldown` | `0` | Cooldown after a successful release |
| `cost` | Omitted | Optional cumulative resource cost |
| `movement` | No restriction | Per-tick drag and sprint control |
| `interruption` | Omitted | Damage-based interruption |
| `stages` | Empty | Particle fields and periodic sounds by charge threshold |

A cumulative cost is the **total** amount that should have been paid by the current charge. Each tick only pays the difference from the amount already paid.

Charge stages still support `particle_field` and periodic `sounds`. Thresholds are between `0.0` and `1.0`.

---

# Passive Skills

## Simple passive

Use `ascension:simple_passive` for a small, always-on passive.

```json
{
  "type": "ascension:simple_passive",
  "name": "Stone-Bone Foundation",
  "description": "Strengthens the cultivator's vitality.",
  "stat_modifiers": {
    "ascension:vitality": [
      {
        "value": 0.15,
        "operation": "MULTIPLY_FINAL",
        "id": "example:stone_bone_vitality"
      }
    ]
  }
}
```

It supports base stats, stat modifiers, base affinity, affinity modifiers, and an optional defence definition.

## Composable passive

Use `ascension:resource_modifier_passive` for most passives. It supports several module types:

| Module | Purpose |
|---|---|
| `ascension:stats` | Base stats, stat modifiers, affinities, and affinity modifiers |
| `ascension:defense` | Flat mitigation, percentage mitigation, stagger resistance, and damage filters |
| `ascension:resources` | Resource transaction modifiers |
| `ascension:projectiles` | Normal projectile profiles for arrows, tridents, and matching modded projectiles |

```json
{
  "type": "ascension:resource_modifier_passive",
  "name": "Ordered Body",
  "description": "Reinforces the body and steadies the cultivator.",
  "modules": [
    {
      "type": "ascension:stats",
      "stat_modifiers": {
        "ascension:vitality": [
          {
            "value": 0.1,
            "operation": "MULTIPLY_FINAL",
            "id": "example:ordered_body_vitality"
          }
        ]
      }
    },
    {
      "type": "ascension:defense",
      "flat_reduction": 0.5,
      "percentage_reduction": 0.08,
      "stagger_resistance": 0.2,
      "filter": {
        "exclude_damage_types": [
          "minecraft:generic_kill"
        ]
      }
    }
  ]
}
```

## Levelled passive modules

```json
{
  "type": "ascension:resource_modifier_passive",
  "name": "Sustained Spirit",
  "description": "Reduces movement costs.",
  "default_accessible_level": 2,
  "levels": [
    {
      "modules": [
        {
          "type": "ascension:resources",
          "modifiers": []
        }
      ]
    },
    {
      "modules": [
        {
          "type": "ascension:resources",
          "modifiers": []
        }
      ]
    }
  ]
}
```

When a level omits `modules`, it inherits the previous module list. When it supplies `modules`, the list is replaced.

## Toggle and upkeep

```json
{
  "toggleable": true,
  "enabled_by_default": false,
  "upkeep": {
    "resource": "ascension:qi",
    "operation": "ascension:consume",
    "amount": 2,
    "interval": 20
  }
}
```

The passive automatically disables when its upkeep cannot be paid.

---

# Scaled Values

Any field using `ScaledValue.COMPACT_CODEC` accepts either a number or a full object.

Fixed value:

```json
{
  "amount": 10
}
```

Scaled value:

```json
{
  "amount": {
    "base": 10,
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
    "minimum": 0,
    "maximum": 100
  }
}
```

The term is calculated as:

```text
source^power × scale + offset
```

It is then applied to the running value with `add`, `multiply`, or `set`.

Term order matters.

## Built-in sources

| Source | Main fields | Reads |
|---|---|---|
| `ascension:constant` | `value` | Fixed number |
| `ascension:skill_level` | Optional `skill` | Effective current or named skill level |
| `ascension:charge` | None | Held charge from `0.0` to `1.0` |
| `ascension:stat` | `stat`, optional `base` | Caster stat |
| `ascension:affinity` | `path`, optional `category`, optional `base` | Path-bonus value |
| `ascension:context` | `key`, optional `fallback` | Value supplied by execution |
| `ascension:skill_effect` | `effect` and filters | Matching mark/effect data on the target |

Owner-specific mark scaling:

```json
{
  "source": {
    "type": "ascension:skill_effect",
    "effect": "example:skill/hunter_mark/effect/mark",
    "owner_scoped": true,
    "metric": "stacks",
    "aggregation": "sum"
  },
  "operation": "add",
  "scale": 2
}
```

Useful execution context keys include:

```text
ascension:skill/effective_level
ascension:execution/target_count
ascension:execution/target_distance
ascension:execution/charge_ticks
ascension:execution/maximum_charge_ticks
ascension:execution/projectile_travelled
ascension:execution/projectile_speed
ascension:execution/projectile_pierce_index
ascension:execution/projectile_range_fraction
ascension:execution/projectile_ticks_lived
ascension:effect_potency
ascension:effect_stacks
```

Use a fallback when zero is not already the correct missing value.

---

# Targeting

## Self

The only targeting shorthand is:

```json
{
  "targeting": "self"
}
```

The full form also works:

```json
{
  "targeting": {
    "type": "ascension:self"
  }
}
```

## Ray

```json
{
  "targeting": {
    "type": "ascension:ray",
    "range": 16,
    "width": 0.25,
    "filter": {
      "relations": [
        "hostile"
      ]
    }
  }
}
```

Returns at most one entity.

## Cone

```json
{
  "targeting": {
    "type": "ascension:cone",
    "range": 12,
    "angle": 45,
    "maximum_targets": 4,
    "sort": "closest_to_view",
    "filter": {
      "relations": [
        "hostile"
      ]
    }
  }
}
```

## Radial

```json
{
  "targeting": {
    "type": "ascension:radial",
    "radius": 6,
    "maximum_targets": 0,
    "sort": "nearest",
    "filter": {
      "relations": [
        "neutral",
        "hostile"
      ]
    }
  }
}
```

`maximum_targets: 0` means unlimited.

## Looked-at position

```json
{
  "targeting": {
    "type": "ascension:look_position",
    "range": 24,
    "include_fluids": false,
    "fallback_to_maximum_range": true
  }
}
```

This returns a position rather than an entity.

## Filters

Relations:

```text
self
ally
neutral
hostile
```

Sorts:

```text
nearest
furthest
lowest_health
highest_health
closest_to_view
```

If a built-in targeting definition omits `filter`, it normally defaults to hostile entities. An explicitly empty filter object defaults to neutral and hostile, so write `relations` when the difference matters.

---

# Execution Subjects

Every feature uses the same optional `subject` field.

| Subject | Meaning |
|---|---|
| `caster` | The player casting the skill |
| `target` | The current resolved entity target |
| `origin` | The original execution position |
| `position` | The current resolved execution position |

The default depends on the feature. For example, damage defaults to `target`, sound defaults to `origin`, and projectiles default to `caster`.

Features run in list order. Target-bound features run once for every resolved target. Caster and origin features are deduplicated so they do not repeat once per target.

---

# Execution Features

| Type | Purpose |
|---|---|
| `ascension:message` | Send overlay or chat text to the caster |
| `ascension:sound` | Play a sound at the resolved position |
| `ascension:particles` | Spawn a one-off simple-particle burst |
| `ascension:resource` | Run a registered resource operation |
| `ascension:damage` | Deal shared RPG damage |
| `ascension:effect` | Apply an inline, local, or global skill effect |
| `ascension:buildup` | Add to a registered buildup channel; defaults to Frozen |
| `ascension:stagger` | Apply, reduce, clear, force-break, or grant stagger immunity |
| `ascension:barrier` | Apply, repair, or remove a barrier |
| `ascension:projectile` | Spawn a virtual projectile |
| `ascension:field` | Spawn or remove an area field |
| `ascension:network` | Spawn or remove an anchor network |
| `ascension:construct` | Spawn, remove, or repair an owner-bound construct |
| `ascension:move` | Directional, target-position, or anchor movement |
| `ascension:anchor` | Set or clear a movement anchor |
| `ascension:visual` | Spawn a temporary runtime visual |

## Message

```json
{
  "type": "ascension:message",
  "message": "Calibration complete",
  "overlay": true
}
```

## Sound

```json
{
  "type": "ascension:sound",
  "subject": "target",
  "sound": "minecraft:block.amethyst_block.chime",
  "volume": 1,
  "pitch": 1.2
}
```

## Particles

```json
{
  "type": "ascension:particles",
  "subject": "target",
  "particle": "minecraft:electric_spark",
  "count": 20,
  "spread": 0.5,
  "speed": 0.06
}
```

Only simple particle types are accepted by this feature.

## Resource

```json
{
  "type": "ascension:resource",
  "subject": "target",
  "resource": "ascension:stamina",
  "operation": "ascension:drain",
  "source": "ascension:skill_casting",
  "amount": 12
}
```

## Effect

```json
{
  "type": "ascension:effect",
  "subject": "target",
  "definition": "#mark",
  "duration": 200,
  "potency": 1
}
```

## Buildup

```json
{
  "type": "ascension:buildup",
  "subject": "target",
  "channel": "ascension:frozen",
  "amount": 0.2,
  "decay_delay": 40
}
```

## Stagger

Apply buildup:

```json
{
  "type": "ascension:stagger",
  "subject": "target",
  "action": "apply",
  "definition": "#heavy_impact",
  "amount": 25
}
```

Other actions are:

```text
reduce
clear
immunity
break
```

`immunity` uses `duration`. `break` requires a stagger definition.

## Barrier, field, network, and construct

```json
{
  "type": "ascension:barrier",
  "subject": "caster",
  "action": "apply",
  "definition": "#bell"
}
```

```json
{
  "type": "ascension:field",
  "subject": "position",
  "action": "apply",
  "definition": "#domain"
}
```

```json
{
  "type": "ascension:network",
  "subject": "position",
  "action": "apply",
  "definition": "#formation"
}
```

```json
{
  "type": "ascension:construct",
  "subject": "caster",
  "action": "repair",
  "definition": "#idol",
  "amount": 20
}
```

## Movement and anchors

```json
{
  "type": "ascension:anchor",
  "subject": "caster",
  "action": "set",
  "anchor": "example:return_point",
  "duration": 100
}
```

```json
{
  "type": "ascension:move",
  "subject": "caster",
  "mode": "anchor",
  "anchor": "example:return_point",
  "consume_anchor": true,
  "restore_rotation": true,
  "collision": "stop_before_collision"
}
```

Movement modes:

```text
directional
target_position
anchor
```

Collision policies:

```text
fail
stop_before_collision
```

---

# Resources

Built-in resource operations are registered IDs:

| Operation | Behaviour |
|---|---|
| `ascension:consume` | Requires the entire amount or rejects the transaction |
| `ascension:drain` | Removes as much as possible and may partially succeed |
| `ascension:restore` | Adds up to the resource maximum |
| `ascension:generate` | Adds up to the resource maximum, with different semantic meaning for selectors |
| `ascension:accumulate` | Adds up to the maximum, commonly used for exhaustion-like resources |

Do not use bare values such as `"consume"`. Use the namespaced registry ID.

Addon developers can register new resource operations without editing Ascension's operation class.

## Resource modifiers

A resource modifier belongs inside an `ascension:resources` passive module or an effect module.

```json
{
  "type": "ascension:resources",
  "modifiers": [
    {
      "id": "example:light_step_stamina",
      "selector": {
        "resources": [
          "ascension:stamina"
        ],
        "operations": [
          "ascension:consume",
          "ascension:drain"
        ],
        "source": {
          "source_tags": [
            "ascension:movement"
          ]
        }
      },
      "operation": "multiply_total",
      "value": -0.2,
      "priority": 100
    }
  ]
}
```

Modifier operations:

```text
add
multiply_base
multiply_total
minimum
maximum
cancel
immunity
```

`multiply_total: -0.2` makes the matching transaction 80% of normal.

Common source tags include:

```text
ascension:movement
ascension:combat
ascension:survival
ascension:regeneration
ascension:skill
ascension:cultivation
ascension:environmental
```

---

# Effects and Buildup

Effects are normally stored under the current skill's `definitions.effects` object.

```json
{
  "definitions": {
    "effects": {
      "hunter_mark": {
        "stacking": "stack",
        "stacking_scope": "source_entity",
        "max_stacks": 3,
        "modules": []
      }
    }
  }
}
```

Stacking policies:

```text
refresh
stronger_replaces
stack
```

Stacking scopes:

```text
definition
source_entity
source_skill
source_entity_and_skill
independent
```

Use `source_entity` for owner-specific marks.

Current effect modules are:

- `ascension:resource_modifier`
- `ascension:frozen_form`

Frozen buildup itself is a registered buildup channel, used through `ascension:buildup`.

---

# Damage, Stagger, and Defences

## Damage

```json
{
  "type": "ascension:damage",
  "subject": "target",
  "amount": {
    "base": 6,
    "terms": [
      {
        "source": {
          "type": "ascension:stat",
          "stat": "ascension:spirit"
        },
        "scale": 0.4
      }
    ]
  },
  "damage_type": "minecraft:indirect_magic",
  "classifications": [
    "ascension:skill",
    "ascension:projectile",
    "ascension:soul"
  ],
  "path": "ascension:foundation/soul",
  "technique": "example:soul_manual"
}
```

Classifications are metadata that barriers, passives, bosses, artifacts, and other systems can filter. Keep them meaningful rather than describing every tiny detail of the attack.

## Stagger profile

```json
{
  "definitions": {
    "stagger": {
      "heavy_impact": {
        "threshold": 100,
        "resistance": {
          "base": 0,
          "terms": [
            {
              "source": {
                "type": "ascension:stat",
                "stat": "ascension:vitality"
              },
              "scale": 0.004
            }
          ],
          "maximum": 0.6
        },
        "decay_per_second": 10,
        "decay_delay": 40,
        "guard_break_duration": 30,
        "immunity_duration": 40,
        "movement_multiplier": 0.2,
        "interrupt_held_casts": true,
        "on_guard_break": []
      }
    }
  }
}
```

## Barrier

```json
{
  "definitions": {
    "barriers": {
      "ward": {
        "duration": 200,
        "durability": {
          "base": 30,
          "terms": [
            {
              "source": {
                "type": "ascension:stat",
                "stat": "ascension:vitality"
              },
              "scale": 2
            }
          ]
        },
        "absorption": 1,
        "overflow": true,
        "priority": 100,
        "replace_existing": true,
        "projectile_response": "discard",
        "on_absorb": [],
        "on_break": [],
        "on_expire": []
      }
    }
  }
}
```

Projectile responses:

```text
none
stop
discard
deflect
```

Damage filters may include or exclude classifications and vanilla damage-type IDs.

## Construct interception

```json
{
  "definitions": {
    "constructs": {
      "guardian": {
        "duration": 300,
        "stability": 100,
        "offset": [
          0,
          1.5,
          -0.75
        ],
        "rotate_with_owner": true,
        "interception": {
          "absorption": 0.8,
          "stability_cost": 1,
          "overflow": true,
          "priority": 200,
          "projectile_response": "deflect",
          "on_intercept": []
        },
        "on_break": [],
        "on_expire": []
      }
    }
  }
}
```

Incoming damage currently flows through a shared combat pipeline, including passive mitigation, construct interception, barrier absorption, health damage, and reactions.

---

# Projectiles and Runtime Objects

## Virtual projectile

```json
{
  "definitions": {
    "projectiles": {
      "bolt": {
        "speed": 2,
        "range": 32,
        "gravity": 0,
        "hit_radius": 0.3,
        "pierces": 1,
        "filter": {
          "relations": [
            "hostile"
          ],
          "line_of_sight": false
        },
        "behaviors": [
          {
            "type": "ascension:homing",
            "turn_rate": 0.1,
            "reacquire": true,
            "acquisition": {
              "type": "ascension:radial",
              "radius": 12,
              "maximum_targets": 1,
              "sort": "nearest"
            }
          }
        ],
        "on_entity_hit": [
          {
            "type": "ascension:damage",
            "amount": 10
          }
        ],
        "on_block_hit": [],
        "on_expire": []
      }
    }
  }
}
```

`pierces` counts additional hits after the first. `pierces: 1` allows two entity hits in total.

Projectile hit features inherit the original caster, skill, charge, variables, and attribution.

## Area field

```json
{
  "definitions": {
    "fields": {
      "domain": {
        "shape": "cylinder",
        "radius": 5,
        "height": 4,
        "duration": 120,
        "tick_interval": 20,
        "filter": {
          "relations": [
            "hostile"
          ],
          "line_of_sight": false
        },
        "on_enter": [],
        "on_tick": [
          {
            "type": "ascension:damage",
            "amount": 3
          }
        ],
        "on_exit": [],
        "on_expire": []
      }
    }
  }
}
```

Shapes are `sphere` and `cylinder`.

## Anchor network

```json
{
  "definitions": {
    "networks": {
      "formation": {
        "duration": 200,
        "rotate_with_caster": true,
        "nodes": [
          {
            "id": "example:north",
            "offset": [
              0,
              0,
              -3
            ]
          },
          {
            "id": "example:south",
            "offset": [
              0,
              0,
              3
            ]
          }
        ],
        "links": [
          {
            "from": "example:north",
            "to": "example:south"
          }
        ],
        "field": "#domain"
      }
    }
  }
}
```

The optional child field is removed with the network.

## Owner-bound construct

Constructs follow their owner and use stability rather than normal mob health. They can be repaired through the `ascension:construct` feature and may intercept damage through their `interception` block.

---

# Runtime Visuals

Visuals may be inline, local, or global like other definitions.

```json
{
  "definitions": {
    "visuals": {
      "impact": {
        "layers": [
          {
            "type": "ascension:ring",
            "position": "origin",
            "transform": {
              "rotation": [
                90,
                0,
                0
              ]
            },
            "appearance": {
              "color": [
                120,
                200,
                255,
                180
              ],
              "secondary_color": [
                255,
                240,
                170,
                240
              ],
              "line_width": 2,
              "no_depth": true
            },
            "geometry": {
              "radius": 1.5,
              "segments": 24
            },
            "motion": {
              "spin": 2,
              "pulse": 0.1,
              "pulse_speed": 0.3
            }
          }
        ]
      }
    }
  },
  "targeting": "self",
  "features": [
    {
      "type": "ascension:visual",
      "definition": "#impact",
      "duration": 15,
      "follow": false,
      "progress": 1
    }
  ]
}
```

Built-in primitive IDs:

```text
ascension:particle_emitter
ascension:billboard
ascension:ring
ascension:shell
ascension:beam
ascension:ground_glyph
ascension:ribbon
ascension:afterimage
ascension:model_layer
ascension:living_entity_overlay
ascension:composite
```

Layer position modes:

```text
origin
owner
each_point
```

Visual values can be fixed numbers or can read runtime state:

```text
constant
progress
primary
secondary
stage
time
speed
point_count
link_count
```

Persistent visuals should be tested in-game rather than judged only from JSON.

---

# Make a levelled skill usable

Levelled skills use trained level, level floor, and accessible cap.

```text
min(max(trained level, level floor), accessible level cap)
```

A technique should usually grant a floor and cap together:

```json
{
  "type": "ascension:set_skill_level",
  "skill": "example:spirit_palm",
  "contribution": "example:manual/realm_1",
  "level": 1,
  "set_floor": true,
  "set_cap": true
}
```

`default_accessible_level` is only a default cap. It does not train the skill or create a floor by itself.

Keep technique progression focused on ownership, levels, and growth. Keep the actual skill behaviour inside the skill JSON.

---

# Complete Examples

## One-file marked homing projectile

```json
{
  "type": "ascension:active_skill",
  "name": "Hunting Star",
  "description": "Mark an enemy and release a star that seeks your marks.",
  "default_accessible_level": 1,
  "definitions": {
    "effects": {
      "mark": {
        "stacking": "stack",
        "stacking_scope": "source_entity",
        "max_stacks": 3,
        "modules": []
      }
    },
    "projectiles": {
      "star": {
        "speed": 1.8,
        "range": 36,
        "hit_radius": 0.35,
        "behaviors": [
          {
            "type": "ascension:homing",
            "turn_rate": 0.1,
            "reacquire": true,
            "acquisition": {
              "type": "ascension:radial",
              "radius": 14,
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
        ],
        "on_entity_hit": [
          {
            "type": "ascension:damage",
            "amount": {
              "base": 7,
              "terms": [
                {
                  "source": {
                    "type": "ascension:skill_effect",
                    "effect": "example:skill/hunting_star/effect/mark",
                    "owner_scoped": true,
                    "metric": "stacks",
                    "aggregation": "sum"
                  },
                  "scale": 2
                }
              ]
            }
          }
        ]
      }
    }
  },
  "targeting": {
    "type": "ascension:ray",
    "range": 24,
    "filter": {
      "relations": [
        "hostile"
      ]
    }
  },
  "costs": [
    {
      "resource": "ascension:qi",
      "operation": "ascension:consume",
      "amount": 12
    }
  ],
  "cooldown": 60,
  "features": [
    {
      "type": "ascension:effect",
      "definition": "#mark",
      "duration": 200,
      "potency": 1
    },
    {
      "type": "ascension:projectile",
      "subject": "target",
      "definition": "#star",
      "direction": "target"
    }
  ]
}
```

## One-file barrier skill

```json
{
  "type": "ascension:active_skill",
  "name": "Amber Ward",
  "description": "Raise a spiritual ward around yourself.",
  "default_accessible_level": 1,
  "definitions": {
    "barriers": {
      "ward": {
        "duration": 200,
        "durability": {
          "base": 25,
          "terms": [
            {
              "source": {
                "type": "ascension:stat",
                "stat": "ascension:vitality"
              },
              "scale": 2
            }
          ]
        },
        "absorption": 1,
        "overflow": true,
        "projectile_response": "discard",
        "on_break": [
          {
            "type": "ascension:sound",
            "sound": "minecraft:block.glass.break",
            "volume": 1,
            "pitch": 0.7
          }
        ]
      }
    }
  },
  "targeting": "self",
  "costs": [
    {
      "resource": "ascension:qi",
      "operation": "ascension:consume",
      "amount": 15
    }
  ],
  "cooldown": 160,
  "features": [
    {
      "type": "ascension:barrier",
      "subject": "caster",
      "definition": "#ward",
      "action": "apply"
    },
    {
      "type": "ascension:sound",
      "subject": "caster",
      "sound": "minecraft:block.amethyst_block.chime",
      "volume": 1,
      "pitch": 0.8
    }
  ]
}
```

---