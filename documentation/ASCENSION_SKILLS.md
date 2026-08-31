# Ascension Skills
This page documents the datapack format used to create Ascension skills and techniques.

It is written for content authors first. The intended workflow is:

**idea → skill JSON → technique JSON → game**

Most normal content should require no Java. Active skills are assembled from targeting, cast behaviour, costs, and ordered actions. Passive skills are assembled from levels, modifiers, and optional triggers. Techniques own skill unlocks and progression caps.

## Quick links
- [Files and workflow](#files-and-workflow)
- [Active skills](#active-skills)
- [Cast modes and mastery](#cast-modes-and-mastery)
- [Passive skills](#passive-skills)
- [Runtime conditions](#runtime-conditions)
- [Scheduled actions and variables](#scheduled-actions-and-variables)
- [State and form passives](#state-and-form-passives)
- [Techniques](#techniques)
- [Scaled values](#scaled-values)
- [Targeting](#targeting)
- [Actions and definitions](#actions-and-definitions)
- [Damage](#damage)
- [Runtime objects](#runtime-objects)
- [Datapack resources](#datapack-resources)
- [Visuals and particle fields](#visuals-and-particle-fields)
- [Complete miniature technique](#complete-miniature-technique)
- [Glossary](#glossary)

# Files and workflow
| Content | Path |
|---|---|
| Skills | `data/<namespace>/ascension/skills/...` |
| Techniques | `data/<namespace>/ascension/techniques/...` |
| Runtime visuals | `data/<namespace>/ascension/skill_system/visuals/...` |
| Beam definitions | `data/<namespace>/ascension/skill_system/runtime/beams/...` |
| Datapack resources | `data/<namespace>/ascension/skill_system/resources/...` |
| Textures | `assets/<namespace>/textures/...` |
| Models | `assets/<namespace>/models/...` |

`data/example/ascension/skills/fire/sun_lance.json` becomes `example:fire/sun_lance`.

Useful shipped references:
```text
skills/castable/sword_projection.json
skills/castable/celestial_constellation_circulation/aquila_crossing.json
skills/castable/cultivation/celestial_constellation_circulation.json
skills/passive/sustained_spirit.json
techniques/3_heaven/celestial_constellation_circulation.json
```

Normal workflow:
1. Choose `ascension:active` or `ascension:passive`.
2. Add only the behaviour the skill actually needs.
3. Use fixed numbers until something genuinely needs scaling.
4. Put reusable gameplay objects in `definitions`.
5. Put reusable presentation in runtime visual files.
6. Add the skill to a technique's `skills` block.

| Skill type | Use it for |
|---|---|
| `ascension:active` | Instant, charge, channel, combat, movement, projectile, and cultivation skills |
| `ascension:passive` | Always-on, levelled, toggleable, defensive, resource, projectile, and attack-triggered passives |

# Active skills
Minimal active skill:
```json
{
  "type": "ascension:active",
  "name": "Pulse",
  "description": "Releases a short burst of force.",
  "actions": [{ "type": "ascension:damage", "base": 6 }]
}
```

Targeting defaults to the caster, cooldown defaults to `0`, and cost is optional.

Typical active skill:
```json
{
  "type": "ascension:active",
  "name": "Radiant Palm",
  "description": "Strike a nearby enemy with spiritual force.",
  "target": { "type": "ascension:ray", "range": 8 },
  "actions": [
    {
      "type": "ascension:damage",
      "base": { "base": 8, "per_mastery": 2 },
      "stats": { "ascension:spirit": 0.4 }
    },
    {
      "type": "ascension:sound",
      "subject": "target",
      "sound": "minecraft:block.amethyst_block.hit",
      "pitch": 1.3
    }
  ],
  "cost": { "resource": "ascension:qi", "amount": 12 },
  "cooldown": 40
}
```

| Root field | Default | Meaning |
|---|---|---|
| `name`, `description` | Required | Display text or translated components |
| `definitions` | Empty | Local reusable gameplay definitions |
| `cast` | Instant | Instant, charge, or channel behaviour |
| `target` | Self | Entity or position targeting |
| `require_targets` | `true` | Whether no target fails execution |
| `actions` | Empty | Ordered behaviour list |
| `cost` | Empty | One resource cost or a list |
| `cooldown` | `0` | Cooldown in ticks |
| `mastery_cap` | `initiate` | Base mastery ceiling |
| `mastery_xp` | Default curve | Optional XP overrides |

One cost:
```json
"cost": { "resource": "ascension:qi", "amount": 12 }
```
Multiple:
```json
"cost": [
  { "resource": "ascension:qi", "amount": 12 },
  { "resource": "ascension:stamina", "amount": 6 }
]
```
Costs default to `ascension:consume` from `ascension:skill_casting`. Actions execute in list order.

# Cast modes and mastery
All active skills share one cast runtime.

## Instant
Instant is the default, so omit `cast`.

## Charge
Charge builds progress from `0.0` to `1.0`.
```json
"cast": {
  "mode": "charge",
  "minimum_ticks": 10,
  "maximum_ticks": 60,
  "cost": {
    "resource": "ascension:qi",
    "cumulative_cost": { "base": 4, "charge": 16 },
    "release_on_failure": true
  },
  "movement": { "horizontal_drag": 0.7, "disable_sprinting": true },
  "interruption": {
    "damage_threshold": 12,
    "accumulation_window": 20,
    "release_after_minimum": false
  }
}
```
`cumulative_cost` is the total that should have been paid at the current charge; each tick pays only the increase.

## Channel
Channel repeatedly resolves target, root costs, and actions while held.
```json
"cast": {
  "mode": "channel",
  "maximum_ticks": 200,
  "movement": { "horizontal_drag": 0.35, "disable_sprinting": true }
}
```
If `maximum_ticks` is omitted or `0`, the channel may continue until released or stopped. Cultivation is a channelled active skill using `ascension:cultivate`.

Charge/channel stages may contain `threshold`, `particle_field`, and periodic `sounds`.

## Mastery
| Rank | JSON | Default XP |
|---|---|---:|
| Initiate | `initiate` | Starting rank |
| Minor Mastery | `minor_mastery` | 100 |
| Major Mastery | `major_mastery` | 300 |
| Perfection | `perfection` | 900 |
| Transcendence | `transcendence` | 2500 |

Techniques normally raise the accessible mastery cap; using the skill trains toward it.

Optional per-skill curve:
```json
"mastery_xp": {
  "minor_mastery": 80,
  "major_mastery": 250,
  "perfection": 700,
  "transcendence": 1800
}
```

`mastery` uses raw rank `1..5`; `per_mastery` counts ranks above Initiate:
```json
"base": { "base": 10, "per_mastery": 2 }
```
This resolves to `10, 12, 14, 16, 18`.

Use a mastery gate for new behaviour:
```json
{
  "type": "ascension:mastery_gate",
  "minimum": "major_mastery",
  "actions": [{ "type": "ascension:stagger", "amount": 20 }]
}
```

# Passive skills
All normal passives use `ascension:passive`.
```json
{
  "type": "ascension:passive",
  "name": "Stone-Bone Foundation",
  "description": "Reinforces the cultivator's body.",
  "modifiers": [
    {
      "type": "ascension:defense",
      "flat_reduction": 1,
      "percentage_reduction": 0.08
    }
  ]
}
```

| Field | Default | Meaning |
|---|---|---|
| `levels` | `1` | Maximum numeric level |
| `level_cap` | `1` | Base accessible level cap |
| `level_xp` | Empty | XP for each next level |
| `modifiers` | Empty | Persistent/queried behaviour |
| `triggers` | Empty | Event-driven actions |
| `definitions` | Empty | Definitions used by triggers |
| `toggleable` | `false` | Player may enable/disable |
| `enabled_by_default` | `true` | Initial state |
| `upkeep` | Empty | Periodic resource cost |
| `state_group` | None | Exclusive group shared with other toggleable states/forms |
| `granted_skills` | `[]` | Skills owned while the state is enabled |
| `enable_conditions` | `[]` | Runtime conditions required to enable the state |
| `on_enable` | `[]` | Actions run when enabled |
| `on_disable` | `[]` | Actions run when disabled |

`"levels": 3, "level_xp": [100, 300]` means level `1→2` costs `100 XP` and `2→3` costs `300 XP`.

Built-in modifiers:
| Modifier | Purpose |
|---|---|
| `ascension:stats` | Stats and affinities |
| `ascension:defense` | Damage/stagger mitigation |
| `ascension:resources` | Resource transactions |
| `ascension:projectiles` | Normal projectile profiles |
| `ascension:weapon_damage` | Weapon, empty-hand/tag, arrow, or trident damage |
| `ascension:movement` | Flight, flying-speed, and fall-damage state |

Movement example:
```json
{
  "type": "ascension:movement",
  "allow_flight": true,
  "flight_speed_multiplier": 1.25,
  "no_fall_damage": true
}
```

Movement modifiers are applied while the passive is active. They are especially useful on toggleable states/forms.

Example defense:
```json
{
  "type": "ascension:defense",
  "flat_reduction": { "base": 0.5, "per_level": 0.5 },
  "percentage_reduction": { "base": 0.03, "per_level": 0.02 },
  "stagger_resistance": 0.15
}
```

Weapon-damage matches:
```text
held_weapon
empty_hand_or_tag
arrow
trident
```

Passive triggers execute normal skill actions when their event fires.

Events:

```text
attack
damage_dealt
damage_taken
kill
skill_cast
resource_changed
```

Trigger fields:

| Field | Default | Meaning |
|---|---|---|
| `event` | Required | Event to react to |
| `weapon_tag` | None | Attack-only held-item tag filter |
| `allow_empty_hand` | `false` | Attack-only empty-hand match |
| `cooldown` | `0` | Trigger cooldown in ticks |
| `priority` | `0` | Higher-priority reactions run first |
| `cost` | Empty | Resource cost paid before actions |
| `conditions` | `[]` | Runtime conditions that must all pass |
| `actions` | `[]` | Ordered actions to execute |

```json
"triggers": [
  {
    "event": "damage_taken",
    "cooldown": 40,
    "conditions": [
      {
        "type": "ascension:health",
        "comparison": "at_most",
        "value": 0.5
      }
    ],
    "actions": [
      {
        "type": "ascension:resource",
        "resource": "example:resolve",
        "operation": "ascension:restore",
        "amount": 8
      }
    ]
  }
]
```

`attack` preserves the weapon-filter behaviour used by weapon passives and selects the highest-priority matching attack trigger. Other events run matching reactions in priority order and may expose a target and runtime variables to their conditions/actions.

Toggle/upkeep:
```json
"toggleable": true,
"enabled_by_default": false,
"upkeep": {
  "resource": "ascension:qi",
  "amount": { "base": 2, "per_level": 1 },
  "interval": 20
}
```
If upkeep cannot be paid, the passive disables.

# Runtime conditions
Runtime conditions are small tests used by `ascension:conditional`, passive triggers, and state/form enable checks.

Conditional action:

```json
{
  "type": "ascension:conditional",
  "condition": {
    "type": "ascension:health",
    "comparison": "at_most",
    "value": 0.5
  },
  "if_true": [
    { "type": "ascension:message", "message": "Low health branch." }
  ],
  "if_false": [
    { "type": "ascension:message", "message": "Normal branch." }
  ]
}
```

Built-in conditions:

| Type | Main fields |
|---|---|
| `ascension:all_of` | `conditions` |
| `ascension:any_of` | `conditions` |
| `ascension:not` | `condition` |
| `ascension:requirement` | `requirements` |
| `ascension:health` | `subject`, `comparison`, `value`, `percentage` |
| `ascension:resource` | `subject`, `resource`, `comparison`, `value`, `percentage` |
| `ascension:entity_state` | `subject`, `state`, `value` |
| `ascension:distance` | `comparison`, `value` |
| `ascension:variable` | `variable`, `comparison`, `value` |
| `ascension:random` | `chance` |

Comparisons:

```text
at_least, at_most, greater_than, less_than, equal
```

Entity states:

```text
sneaking, sprinting, airborne, on_fire, in_water
```

`health` uses a percentage by default. `resource` uses a raw amount by default; set `"percentage": true` to compare against `0.0..1.0` of the maximum. `random.chance` also uses `0.0..1.0`.

The requirement condition wraps the normal origin requirement system:

```json
{
  "type": "ascension:requirement",
  "requirements": [
    {
      "type": "ascension:has_physique",
      "physique": "example:iron_bloom"
    }
  ]
}
```

`ascension:variable` reads values already provided by the current runtime context. Current useful trigger values include:

```text
ascension:trigger/damage
ascension:trigger/resource_requested
ascension:trigger/resource_applied
ascension:trigger/resource_before
ascension:trigger/resource_after
ascension:trigger/resource_maximum
```

Damage triggers provide `trigger/damage`; resource-change triggers provide the resource transaction values. Cast contexts also expose normal cast variables such as `ascension:cast/progress`.

# Scheduled actions and variables
Actions may schedule normal nested actions for later execution.

Delay:
```json
{
  "type": "ascension:delay",
  "ticks": 20,
  "actions": [
    { "type": "ascension:message", "message": "One second later." }
  ]
}
```

Repeat:
```json
{
  "type": "ascension:repeat",
  "times": 4,
  "interval": 5,
  "actions": [
    { "type": "ascension:particles", "particle": "minecraft:electric_spark", "count": 4 }
  ]
}
```

`repeat` schedules the first run after one `interval`; it does not execute an immediate extra copy.

Runtime variables use an ID and may be set, added, or multiplied:

```json
{
  "type": "ascension:variable",
  "variable": "example:power",
  "operation": "set",
  "value": 4
}
```

Operations:
```text
set, add, multiply
```

Variables remain available to later actions in the same execution and are carried into delayed/repeated actions. Read one through a runtime condition or the normal `ascension:context` ScaledValue source:

```json
{
  "base": 0,
  "terms": [
    {
      "source": {
        "type": "ascension:context",
        "key": "example:power"
      },
      "scale": 2
    }
  ]
}
```

# State and form passives
Toggleable passives can act as persistent states, stances, transformations, or forms.

```json
{
  "type": "ascension:passive",
  "name": "Resolve Form",
  "description": "Maintains a focused combat state.",
  "toggleable": true,
  "enabled_by_default": false,
  "state_group": "example:combat_form",
  "granted_skills": ["example:resolve_burst"],
  "enable_conditions": [
    {
      "type": "ascension:resource",
      "resource": "example:resolve",
      "comparison": "at_least",
      "value": 10
    }
  ],
  "upkeep": {
    "resource": "example:resolve",
    "amount": 3,
    "interval": 20
  },
  "on_enable": [
    { "type": "ascension:message", "message": "Resolve Form enabled." }
  ],
  "on_disable": [
    { "type": "ascension:message", "message": "Resolve Form disabled." }
  ]
}
```

When one enabled passive has a `state_group`, enabling another passive with the same group disables the previous one. Modifiers and `granted_skills` remain active only while the state is enabled. `enable_conditions` are checked when enabling; upkeep failure disables the state normally.

Persistent form visuals use `ascension:persistent_visual`:

```json
"on_enable": [
  {
    "type": "ascension:persistent_visual",
    "key": "example:form_aura",
    "visual": "example:form_aura"
  }
],
"on_disable": [
  {
    "type": "ascension:persistent_visual",
    "action": "remove",
    "key": "example:form_aura"
  }
]
```

`key` identifies the owned visual instance. Persistent visuals are restored when an enabled passive is attached to the player again, without replaying unrelated enable actions.

# Techniques
A technique connects a cultivation path to skills and progression.
```json
{
  "type": "ascension:simple_technique",
  "name": "Ember Body Art",
  "description": "Tempered breath circulates through flesh and blood.",
  "path": "ascension:foundation/body",
  "max_realm": 6,
  "skills": {
    "example:ember_body/cultivation": {},
    "example:ember_body/iron_pulse": {
      "unlock": 1,
      "requirements": [
        {
          "type": "ascension:has_physique",
          "physique": "example:iron_bloom"
        }
      ]
    },
    "example:ember_body/scarlet_step": {
      "unlock": 2,
      "caps": { "4": "minor_mastery", "6": "major_mastery" }
    },
    "example:ember_body/tempered_skin": {
      "unlock": 3,
      "caps": { "5": 2 }
    }
  }
}
```

`unlock` is the **zero-indexed major realm** where the technique begins owning the skill. Omit it for realm `0`. Optional `requirements` use the shared Ascension requirement list and are re-evaluated as relevant origin progression changes.

Active caps use mastery names:
```json
"caps": { "3": "minor_mastery", "5": "major_mastery" }
```
Passive caps use levels:
```json
"caps": { "3": 2, "5": 3 }
```
An active cap raises the ceiling the player may train toward; it does not directly grant trained mastery.

| Technique field | Meaning |
|---|---|
| `path` | Cultivated path |
| `milestone_realms` | Notable major realms |
| `technique_families` | Technique family names |
| `min_realm`, `max_realm` | Major-realm limits |
| `max_minor_realm` | Minor-realm limit |
| `realm_overrides` | Technique-specific realm definitions |
| `skills` | Skill ownership and caps |
| `realm_change_handler` | Other realm-driven progression |
| `item_tooltip` | Technique manual presentation |

Use `skills` for normal skill progression and `realm_change_handler` for other progression such as stat/path bonuses.

# Scaled values
Many numeric fields accept a `ScaledValue`.

Fixed:
```json
"cooldown": 40
```
Common scaling:
```json
"cooldown": { "base": 80, "per_mastery": -10, "minimum": 20 }
```

| Shorthand | Reads |
|---|---|
| `mastery` | Raw active mastery `1..5` |
| `per_mastery` | Ranks above Initiate `0..4` |
| `level` | Raw passive level |
| `per_level` | Levels above 1 |
| `charge` | Cast progress `0..1` |
| `minimum`, `maximum` | Final clamps |

Use `terms` for another source, powers, multiplication, or explicit ordering:
```json
{
  "base": 8,
  "terms": [
    {
      "source": { "type": "ascension:stat", "stat": "ascension:spirit" },
      "scale": 0.4
    },
    {
      "source": {
        "type": "ascension:path_realm_multiplier",
        "path": "ascension:foundation/soul",
        "bonus_per_major_realm": 0.05
      },
      "operation": "multiply"
    }
  ]
}
```

A term computes `source^power × scale + offset`, then applies `add`, `multiply`, or `set`. Term order matters.

Built-in sources:
```text
ascension:constant
ascension:mastery
ascension:level
ascension:charge
ascension:path_realm_multiplier
ascension:stat
ascension:affinity
ascension:context
ascension:skill_effect
```

`ascension:context` reads runtime variables by ID:

```json
{
  "type": "ascension:context",
  "key": "example:power",
  "fallback": 0
}
```
`ascension:skill_effect` can read `potency`, `stacks`, `duration`, or `count`, aggregated with `sum`, `max`, or `min`.

# Targeting
If `target` is omitted, the caster is targeted. Explicit self is `"target": "self"`.

| Type | Main fields |
|---|---|
| `ascension:ray` | `range`, `width`, `filter` |
| `ascension:cone` | `range`, `angle`, `maximum_targets`, `sort`, `filter` |
| `ascension:radial` | `radius`, `maximum_targets`, `sort`, `filter` |
| `ascension:look_position` | `range`, `include_fluids`, `fallback_to_maximum_range` |

Examples:
```json
"target": {
  "type": "ascension:ray",
  "range": 16,
  "filter": { "relations": ["hostile"] }
}
```
```json
"target": {
  "type": "ascension:look_position",
  "range": 24,
  "fallback_to_maximum_range": true
}
```

`maximum_targets: 0` means unlimited.

Relations:
```text
self, ally, neutral, hostile
```
Sorts:
```text
nearest, furthest, lowest_health, highest_health, closest_to_view
```
Filter fields:
```text
relations, include_players, line_of_sight
```

Actions may operate on `caster`, `target`, `origin`, or `position`. Damage defaults to `target`; weapon swings to `caster`; visuals to `origin`.

# Actions and definitions
Built-in actions:
| Action | Purpose |
|---|---|
| `ascension:mastery_gate` | Rank-gated nested actions |
| `ascension:conditional` | Runtime condition with `if_true`/`if_false` actions |
| `ascension:delay` | Run nested actions after a delay |
| `ascension:repeat` | Schedule repeated nested actions |
| `ascension:variable` | Set/add/multiply a runtime variable |
| `ascension:message` | Overlay/chat text |
| `ascension:sound` | Sound |
| `ascension:particles` | Simple-particle burst |
| `ascension:resource` | Resource transaction |
| `ascension:cultivate` | Cultivation progress |
| `ascension:damage` | Damage profile |
| `ascension:effect` | Skill effect |
| `ascension:buildup` | Registered buildup |
| `ascension:stagger` | Stagger control |
| `ascension:barrier` | Barrier apply/repair/remove |
| `ascension:projectile` | Virtual projectile |
| `ascension:field` | Area field |
| `ascension:network` | Anchor network |
| `ascension:construct` | Owner-bound construct |
| `ascension:move` | Movement |
| `ascension:anchor` | Movement anchor |
| `ascension:visual` | Runtime visual |
| `ascension:persistent_visual` | Apply/remove an owned persistent visual |
| `ascension:beam` | Spawn a gameplay beam definition |
| `ascension:weapon_swing` | Projected weapon attack/VFX |

Common forms:
```json
{ "type": "ascension:particles", "particle": "minecraft:electric_spark", "count": 20, "spread": 0.5 }
```
```json
{
  "type": "ascension:cultivate",
  "path": "ascension:foundation/body",
  "secondary_path": "ascension:elemental/fire",
  "rate": 2.5
}
```
```json
{ "type": "ascension:effect", "definition": "#mark", "duration": 200, "potency": 1 }
```

A skill can define:
```json
"definitions": {
  "effects": {},
  "projectiles": {},
  "fields": {},
  "networks": {},
  "constructs": {},
  "barriers": {},
  "stagger": {},
  "beams": {}
}
```

References are:
```text
inline object
#local_name
namespace:global_definition
```
Use inline data once, `#local` inside one skill, and global definitions when unrelated content intentionally shares an object. Runtime visuals are separate registry-backed visual definitions.

# Damage
`ascension:damage` describes offensive inputs for the shared damage path:
```json
{
  "type": "ascension:damage",
  "base": { "base": 8, "per_mastery": 2 },
  "weapon": 1,
  "stats": { "ascension:strength": 0.5 },
  "attributes": { "example:spell_power": 0.25 },
  "classifications": ["ascension:skill", "ascension:weapon"],
  "path": "ascension:foundation/body"
}
```

| Field | Meaning |
|---|---|
| `base` | Skill base damage |
| `weapon` | Held-weapon contribution multiplier |
| `stats` | Stat ID → coefficient |
| `attributes` | Attribute ID → coefficient |
| `minimum`, `maximum` | Raw profile clamps |
| `damage_type` | Minecraft damage type |
| `classifications` | Semantic damage metadata |
| `path`, `technique` | Optional attribution |

Every coefficient may be a ScaledValue:
```json
"weapon": { "base": 0.75, "per_mastery": 0.1 }
```

The complete profile is composed before normal Minecraft damage processing. Ascension modifiers, passive defense, constructs, and barriers then participate through the shared RPG damage flow.

Weapon swings use the same profile:
```json
{
  "type": "ascension:weapon_swing",
  "weapon_tag": "minecraft:swords",
  "damage": { "base": 6, "weapon": 1 },
  "path": "ascension:weapon/sword",
  "classifications": ["ascension:skill", "ascension:weapon", "ascension:sword"]
}
```

# Runtime objects
Built-in resource operations:
```text
ascension:consume, ascension:drain, ascension:restore,
ascension:generate, ascension:accumulate
```
A direct transaction:
```json
{
  "type": "ascension:resource",
  "resource": "ascension:stamina",
  "operation": "ascension:restore",
  "amount": 10
}
```

Effects support stacking policies `refresh`, `stronger_replaces`, and `stack`; scopes include `definition`, `source_entity`, `source_skill`, `source_entity_and_skill`, and `independent`.

Frozen buildup:
```json
{
  "type": "ascension:buildup",
  "channel": "ascension:frozen",
  "amount": { "base": 0.1, "per_mastery": 0.04 }
}
```

Stagger profiles live in `definitions.stagger`; stagger actions are `apply`, `reduce`, `clear`, `immunity`, and `break`.

Projectile definition:
```json
"definitions": {
  "projectiles": {
    "bolt": {
      "speed": 2,
      "range": 32,
      "hit_radius": 0.3,
      "pierces": 1,
      "on_entity_hit": [{ "type": "ascension:damage", "base": 10 }]
    }
  }
}
```
Spawn with:
```json
{ "type": "ascension:projectile", "definition": "#bolt", "direction": "look" }
```
`pierces` counts additional hits after the first.

Other runtime definitions:
| Object | Main purpose |
|---|---|
| Field | Sphere/cylinder area with enter/tick/exit/expire actions |
| Barrier | Durability, absorption, filters, projectile response, callbacks |
| Construct | Owner-bound stability object with optional interception |
| Network | Positioned nodes/links, optionally carrying a child field |
| Beam | Continuous look-direction ray with repeated hit actions |

Beam definition:
```json
"definitions": {
  "beams": {
    "lance": {
      "range": 32,
      "width": 0.6,
      "duration": 20,
      "tick_interval": 2,
      "stop_on_block": true,
      "filter": { "relations": ["hostile"] },
      "on_hit": [
        { "type": "ascension:damage", "base": 6 }
      ],
      "visual": "example:energy_beam"
    }
  }
}
```

Spawn with:
```json
{ "type": "ascension:beam", "definition": "#lance" }
```

Beam fields include `range`, `width`, `duration`, `tick_interval`, `maximum_targets`, `stop_on_block`, `filter`, `knockback`, `on_hit`, `on_block`, `on_expire`, and optional `visual`. `maximum_targets: 0` means unlimited. Beam hit actions expose `ascension:beam/distance` and `ascension:beam/range_fraction` as runtime variables.

Runtime action values are `apply`, `repair`, and `remove`. Barrier projectile responses include `none`, `stop`, `discard`, and `deflect`.

Movement modes are `directional`, `target_position`, and `anchor`.
```json
{
  "type": "ascension:move",
  "mode": "target_position",
  "maximum_distance": { "base": 8, "per_mastery": 4 },
  "stopping_distance": 0.6,
  "collision": "stop_before_collision"
}
```
Collision values are `fail` and `stop_before_collision`.

# Datapack resources
Custom resources live in:

```text
data/<namespace>/ascension/skill_system/resources/
```

A resource definition needs no Java:

```json
{
  "maximum": 100,
  "starting": 100,
  "regeneration": 2,
  "regeneration_interval": 20
}
```

The file path is the resource ID. For example:

```text
data/example/ascension/skill_system/resources/resolve.json
-> example:resolve
```

Fields:

| Field | Default | Meaning |
|---|---:|---|
| `maximum` | `100` | Maximum stored amount |
| `starting` | `0` | Initial amount before the resource has stored data |
| `regeneration` | `0` | Amount restored each regeneration interval |
| `regeneration_interval` | `20` | Regeneration interval in ticks |

`maximum`, `starting`, and `regeneration` accept ScaledValues.

The resource then works everywhere normal resources do:

```json
"cost": { "resource": "example:resolve", "amount": 20 }
```

```json
"upkeep": {
  "resource": "example:resolve",
  "amount": 3,
  "interval": 20
}
```

```json
{
  "type": "ascension:resource",
  "resource": "example:resolve",
  "operation": "ascension:restore",
  "amount": 15
}
```

This uses the same transaction path as built-in resources, so passive resource modifiers and `resource_changed` triggers apply normally.

# Visuals and particle fields
Runtime visuals live in:
```text
data/<namespace>/ascension/skill_system/visuals/
```

Visual definition:
```json
{
  "elements": [
    {
      "type": "ascension:decal",
      "texture": "ascension:vfx/magic_circle_01",
      "radius": 1
    }
  ]
}
```

Spawn:
```json
{
  "type": "ascension:visual",
  "subject": "caster",
  "visual": "ascension:generic/magic_circle_01",
  "duration": 40,
  "scale": 2,
  "spin": 1.5,
  "tint": [120, 200, 255, 220],
  "follow": true
}
```

Element types:
```text
ascension:particles, ascension:sprite, ascension:decal, ascension:model,
ascension:ring, ascension:shell, ascension:beam, ascension:energy_beam,
ascension:aura, ascension:trail, ascension:afterimage,
ascension:entity_overlay, ascension:custom, ascension:composite
```

Common fields:
```text
position, offset, rotation, scale
tint, secondary_tint, line_width, filled, no_depth
radius, inner_radius, height, width, length, segments, count, style
spin, pulse, pulse_speed, bob, bob_speed, interval, history
texture, model, particle, visual, frames, frame_ticks
```

`ascension:energy_beam` is the volumetric beam visual intended for gameplay beams; the older `ascension:beam` element remains useful for thin links and network lines.

`ascension:aura` supports:

```text
flame, flowing, mist, storm
```

Example:
```json
{
  "type": "ascension:aura",
  "position": "owner",
  "radius": 1.1,
  "height": 3.2,
  "count": 7,
  "style": "flame",
  "tint": [255, 220, 80, 145]
}
```

Multiple aura elements may be layered in one visual definition for a larger, softer outer field or mixed styles.

Texture IDs omit `textures/` and `.png`:
```text
assets/ascension/textures/vfx/orb_01.png -> ascension:vfx/orb_01
```

The skill-side visual action can override duration, scale, spin, tint, follow/rotation, offset, point data, progress, primary/secondary values, and stage.

Channel stages may use particle fields:
```json
"particle_field": {
  "style": "gathering_ring",
  "particles": [
    "minecraft:end_rod",
    { "particle": "ascension:particle_field_wisp", "weight": 4 }
  ],
  "colours": ["#EAF7FF", "#9CCBFF"],
  "density": 18,
  "radius": { "min": 0.75, "max": 1.65 },
  "height": { "min": 0.05, "max": 1.8 },
  "full_bright": true
}
```

Styles:
```text
rising, inward_flow, spiral, meridian_flow, gathering_ring, breath_flow
```
Particles accept registered particle IDs. Range fields may be a fixed number or `{ "min": ..., "max": ... }`.

# Complete miniature technique
This miniature technique uses two skill files and one technique file.

## `example:ember_body/cultivation`
```json
{
  "type": "ascension:active",
  "name": "Ember Meridian Breathing",
  "description": "Draw fire-aspected energy through the meridians.",
  "cast": {
    "mode": "channel",
    "stages": [
      {
        "particle_field": {
          "style": "spiral",
          "particles": ["minecraft:flame", "ascension:particle_field_mote"],
          "colours": ["#FFB25A", "#FF694A"],
          "density": 16,
          "radius": { "min": 0.6, "max": 1.4 }
        }
      }
    ]
  },
  "require_targets": false,
  "actions": [
    {
      "type": "ascension:cultivate",
      "path": "ascension:foundation/body",
      "secondary_path": "ascension:elemental/fire",
      "rate": { "base": 1.5, "per_mastery": 0.25 }
    }
  ]
}
```

## `example:ember_body/falling_mountain_cut`
```json
{
  "type": "ascension:active",
  "name": "Falling Mountain Cut",
  "description": "Bring the weapon down with the weight of a mountain.",
  "target": {
    "type": "ascension:ray",
    "range": 5,
    "filter": { "relations": ["hostile"] }
  },
  "actions": [
    {
      "type": "ascension:damage",
      "base": { "base": 6, "per_mastery": 2 },
      "weapon": { "base": 1, "per_mastery": 0.1 },
      "stats": { "ascension:strength": 0.35 },
      "classifications": ["ascension:skill", "ascension:weapon", "ascension:body"],
      "path": "ascension:foundation/body"
    },
    {
      "type": "ascension:visual",
      "subject": "target",
      "visual": "ascension:generic/impact_ring_01",
      "duration": 10,
      "tint": [255, 180, 110, 230]
    }
  ],
  "cost": { "resource": "ascension:stamina", "amount": 12 },
  "cooldown": { "base": 60, "per_mastery": -5, "minimum": 30 }
}
```

## `example:ember_body_art`
```json
{
  "type": "ascension:simple_technique",
  "name": "Ember Body Art",
  "description": "Temper flesh with disciplined fire circulation.",
  "path": "ascension:foundation/body",
  "max_realm": 6,
  "skills": {
    "example:ember_body/cultivation": {},
    "example:ember_body/falling_mountain_cut": {
      "unlock": 1,
      "caps": { "3": "minor_mastery", "5": "major_mastery" }
    }
  },
  "item_tooltip": {
    "theme": "ascension:technique_manual",
    "template": "ascension:default_cultivation_technique",
    "rank": "ascension:profound"
  }
}
```

Result:
```text
Realm 0  Ember Meridian Breathing
Realm 1  Falling Mountain Cut
Realm 3  Falling Mountain Cut cap -> Minor Mastery
Realm 5  Falling Mountain Cut cap -> Major Mastery
```

# Glossary
**Action** — One ordered unit of active behaviour: damage, movement, projectile, visual, resource change, etc.

**Active skill** — A castable `ascension:active` skill. Instant, charge, channel, combat, movement, and cultivation all use this type.

**Cap** — The highest mastery rank or passive level currently accessible.

**Cast progress** — A `0.0..1.0` value used by charge/channel casts and `charge` scaling.

**Classification** — Semantic damage metadata used by combat filters and modifiers.

**Condition** — A runtime test used by conditional actions, passive triggers, or state enable checks.

**Definition** — Reusable gameplay data such as an effect, projectile, field, barrier, construct, network, or stagger profile.

**Definition reference** — An inline object, local `#name`, or global `namespace:path`.

**Mastery** — Active progression: Initiate → Minor Mastery → Major Mastery → Perfection → Transcendence.

**Modifier** — Persistent/queried passive behaviour such as defense, stats, resources, projectiles, or weapon damage.

**Passive level** — Numeric progression used by `ascension:passive`.

**Resource definition** — A datapack-defined resource with maximum, starting amount, and optional regeneration.

**Runtime object** — A longer-lived projectile, field, barrier, network, construct, beam, or visual created by a skill.

**ScaledValue** — A number derived from a base and optional mastery, level, charge, stats, affinity, realms, context, or effects.

**Runtime variable** — An ID/value stored in the current skill execution and readable by conditions or ScaledValues.

**State group** — An ID used to make toggleable passives mutually exclusive while enabled.

**Subject** — What an action operates on: `caster`, `target`, `origin`, or `position`.

**Technique** — A path-linked definition that owns skills and raises mastery/level caps.

**Trigger** — A passive event hook that executes ordinary skill actions.

**Visual definition** — Registry-backed asset-driven presentation spawned by `ascension:visual`.

## Compact ID reference
```text
Mastery: initiate, minor_mastery, major_mastery, perfection, transcendence
Targets: ascension:self, ascension:ray, ascension:cone, ascension:radial, ascension:look_position
Subjects: caster, target, origin, position
Relations: self, ally, neutral, hostile
Particle fields: rising, inward_flow, spiral, meridian_flow, gathering_ring, breath_flow
```
