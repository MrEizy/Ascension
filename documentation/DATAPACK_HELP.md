# Ascension Datapacks
This page is the short reference for Ascension datapack layout and the common JSON shapes shared by several systems.

For system-specific authoring, use:

- `ASCENSION_SKILLS.md` for skills, runtime conditions, states, and resources
- `ASCENSION_TECHNIQUES.md` for techniques and their `on_added` actions
- `ASCENSION_BLOODLINES.md` for purity-driven bloodlines
- `ASCENSION_PHYSIQUES.md` for persistent physique bonuses

## Quick links
- [Files and IDs](#files-and-ids)
- [Types](#types)
- [Components](#components)
- [Base stats](#base-stats)
- [Value modifiers](#value-modifiers)
- [Path bonuses](#path-bonuses)
- [Path interactions](#path-interactions)
- [Requirements](#requirements)
- [Progress actions](#progress-actions)
- [Datapack resources](#datapack-resources)
- [Item tooltips](#item-tooltips)
- [Compact registry reference](#compact-registry-reference)

# Files and IDs
Ascension uses normal Minecraft datapacks. Registry files live below your namespace:

```text
data/<namespace>/ascension/
```

Common registries:

| Content | Path |
|---|---|
| Skills | `data/<namespace>/ascension/skills/...` |
| Techniques | `data/<namespace>/ascension/techniques/...` |
| Bloodlines | `data/<namespace>/ascension/bloodlines/...` |
| Physiques | `data/<namespace>/ascension/physiques/...` |
| Paths | `data/<namespace>/ascension/paths/...` |
| Progress actions | `data/<namespace>/ascension/progress_actions/...` |
| Progress conditions | `data/<namespace>/ascension/progress_action_conditions/...` |
| Tribulations | `data/<namespace>/ascension/tribulations/...` |
| Runtime visuals | `data/<namespace>/ascension/skill_system/visuals/...` |
| Beam definitions | `data/<namespace>/ascension/skill_system/runtime/beams/...` |
| Datapack resources | `data/<namespace>/ascension/skill_system/resources/...` |

A file at:

```text
data/example/ascension/physiques/body/iron_bloom.json
```

has the registry ID:

```text
example:body/iron_bloom
```

# Types
Typed Ascension registry entries begin with a `type` field. The type decides how the rest of the file is decoded.

```json
{
  "type": "ascension:simple"
}
```

Different registries have different type IDs. Do not assume two systems with similar fields accept the same type.

Common author-facing types:

```text
Technique: ascension:simple_technique
Bloodline: ascension:simple
Physique:  ascension:simple
Skill:     ascension:active, ascension:passive
```

# Components
Names and descriptions use Minecraft Components.

Literal:

```json
{
  "name": "Example"
}
```

Translatable:

```json
{
  "name": {
    "translate": "example.content.name"
  }
}
```

Use translation keys for shipped content so text can be localized.

# Base stats
Physiques use a direct stat-to-value map:

```json
{
  "base_stats": {
    "ascension:vitality": 2.0,
    "ascension:strength": 1.0
  }
}
```

Progression actions (bloodlines, paths, techniques) use a list instead:

```json
{
  "type": "ascension:give_base_stats",
  "stats": [
    { "id": "ascension:vitality", "value": 2.0 },
    { "id": "ascension:strength", "value": 1.0 }
  ]
}
```

These are flat additions to the base value. Progress actions add them while progressing upward and remove them when the matching progression is reversed.

# Value modifiers
Physiques may attach modifiers to existing stat or path-bonus containers.

```json
{
  "value": 0.2,
  "operation": "MULTIPLY_FINAL",
  "id": "example:strong_body_vitality"
}
```

Operations:

```text
ADD_BASE
MULTIPLY_BASE
ADD_FINAL
MULTIPLY_FINAL
```

The value-container order is:

```text
(base * (1 + multiplyBase) + addBase) * (1 + multiplyFinal) + addFinal
```

`id` should be unique for that modifier so Ascension can remove it cleanly when the source is removed.

# Path bonuses
They are grouped by bonus category, then path.

Flat bonuses:

```json
"base_path_bonuses": {
  "ascension:affinity": {
    "ascension:foundation/body": 1.0,
    "ascension:elemental/earth": 2.0
  }
}
```

Modifier bonuses:

```json
"path_bonus_modifiers": {
  "ascension:affinity": {
    "ascension:elemental/earth": [
      {
        "value": 0.15,
        "operation": "ADD_BASE",
        "id": "example:earth_affinity_modifier"
      }
    ]
  }
}
```

The most common category is `ascension:affinity`, but the format is category-driven rather than affinity-specific.

Progression handlers use the same flat bonus shape through `ascension:give_path_bonuses`:

```json
{
  "type": "ascension:give_path_bonuses",
  "bonuses": {
    "ascension:affinity": {
      "ascension:elemental/fire": 0.05
    }
  }
}
```

## Path interactions
Files in `data/<namespace>/ascension/config/path_interactions/` hold a list of interactions. Each one says how a `source` path's **affinity** influences a `target` path's affinity.

```json
[
  { "source": "ascension:elemental/water", "target": "ascension:elemental/fire", "type": "destructive", "value": 0.5 },
  { "source": "ascension:elemental/wood",  "target": "ascension:elemental/fire", "type": "generative",  "value": 0.5 },
  { "source": "ascension:elemental/water", "target": "ascension:elemental/ice",  "type": "related",     "value": 0.5 }
]
```

Interactions only affect the `ascension:affinity` category. Every other path bonus category is a plain `1 + bonus` multiplier and never interacts with other paths.

Effective affinity `E` for a target path, where `A(p)` is the raw affinity (never below 0):

1. `related`: `E0 = A(target) + Σ value · A(source)`.
2. `generative` / `destructive`: each interaction applies pressure based on the **ratio** between the source and the base, `term = clamp(ln(1 + value · A(source) / (E0 + 1)), 0, 3)`.
3. `E = E0 · exp(Σ generative − Σ destructive)`.

A single term is capped at ×20 or ×0.05, so one interaction can never zero a path. Equal generative and destructive pressure cancel, so a strong generative source brings back a suppressed path. Water 1000 suppresses fire 10 far more than water 10 does, while water 1000 against fire 1000 suppresses about as much as 10 against 10.

### Combat
An attack tagged with a path compares the attacker's and defender's effective affinities only when it lands:

- each defender path that is `destructive` to the attack path resists: `−ln(1 + value · Ed / (Ea + 1))`;
- each defender path that the attack path is `destructive` to is overwhelmed: `+ln(1 + value · Ea / (Ed + 1))`.

Damage is multiplied by `exp(clamp(Σ, −3, 3))`, i.e. between ×0.05 and ×20.

# Requirements
Bloodlines, physiques, and techniques may use Ascension's shared requirement list.

```json
"requirements": [
  {
    "type": "ascension:has_physique",
    "physique": "example:iron_bloom"
  },
  {
    "type": "ascension:stat",
    "stat": "ascension:spirit",
    "comparison": "at_least",
    "value": 20
  }
]
```

Every entry in the list must pass. Use `ascension:any_of`, `ascension:all_of`, or `ascension:not` when more complex logic is needed.

Built-in requirement types:

```text
ascension:all_of
ascension:any_of
ascension:not
ascension:has_path
ascension:path_realm
ascension:has_technique
ascension:has_bloodline
ascension:bloodline_purity
ascension:has_physique
ascension:has_skill
ascension:skill_mastery
ascension:affinity
ascension:stat
```

Comparisons used by purity, affinity, and stat requirements:

```text
at_least, at_most, greater_than, less_than, equal
```

# Progress actions
Bloodlines, paths, and techniques share a small progression-action system.

Bloodline and path handlers are lists of condition/action pairs:

```json
[
  {
    "condition": "example:condition",
    "actions": [
      "example:action",
      {
        "type": "ascension:give_base_stats",
        "stats": [
          { "id": "ascension:spirit", "value": 1.0 }
        ]
      }
    ]
  }
]
```

Both conditions and actions may be written inline or referenced from their datapack registries.

Techniques use a plain action list (`on_added`) with no conditions. It runs upward when the technique is learned and downward when it is removed.

Built-in actions:

| Type | Purpose |
|---|---|
| `ascension:give_base_stats` | Add/remove flat base stats |
| `ascension:give_path_bonuses` | Add/remove flat path bonuses |
| `ascension:grant_skills` | Grant skills while the matching progression is active |
| `ascension:remove_skills` | Remove skills while the matching progression is active |

Useful shipped condition references:

```text
ascension:on_gained
ascension:all_purity
ascension:realm_change/on_gained
ascension:realm_change/all_realms
ascension:realm_change/all_major_realms
ascension:realm_change/all_minor_realms
```

Skill progression actions use a simple list:

```json
{
  "type": "ascension:grant_skills",
  "skills": [
    "example:passive/dragon_instinct"
  ]
}
```

`grant_skills` is reversed when progression moves backward. `remove_skills` does the opposite: it removes while progressing upward and restores when that progression is reversed.

See the bloodline and technique guides for the inline condition formats and their exact progression semantics. Realm conditions (`ascension:realm_change/...`, `ascension:every_realm`, etc.) only apply to path handlers.

# Datapack resources
Custom resources live in:

```text
data/<namespace>/ascension/skill_system/resources/...
```

A file at:

```text
data/example/ascension/skill_system/resources/resolve.json
```

creates the resource ID:

```text
example:resolve
```

Minimal definition:

```json
{
  "maximum": 100,
  "starting": 100,
  "regeneration": 2,
  "regeneration_interval": 20
}
```

`maximum`, `starting`, and `regeneration` accept ScaledValues. `regeneration_interval` is an integer measured in ticks.

Once defined, the resource works with normal skill costs, passive upkeep, resource modifiers, resource conditions, and `ascension:resource` actions:

```json
"cost": {
  "resource": "example:resolve",
  "amount": 15
}
```

# Skill runtime additions
The skill guide contains the full formats for v1.1 runtime features. The compact IDs are:

```text
Actions:
ascension:delay
ascension:repeat
ascension:variable
ascension:persistent_visual
ascension:beam

Passive modifier:
ascension:movement

Runtime visual elements:
ascension:energy_beam
ascension:aura

Aura styles:
flame
flowing
mist
storm
```

Gameplay beam definitions may be local under a skill's `definitions.beams` block or stored globally under:

```text
data/<namespace>/ascension/skill_system/runtime/beams/...
```

# Item tooltips
Techniques, bloodlines, and physiques may embed an `item_tooltip` definition.

```json
"item_tooltip": {
  "theme": "ascension:physique_essence",
  "template": "ascension:default_physique_essence",
  "rank": "ascension:ordinary"
}
```

Common fields:

| Field | Meaning |
|---|---|
| `theme` | Tooltip visual theme |
| `theme_overrides` | Per-entry theme changes |
| `template` | Tooltip layout/template |
| `rank` | Rank badge/style source |
| `pages` | Optional inline tooltip pages |
| `animation_presets` | Optional animation preset IDs |

Current default templates:

```text
ascension:default_bloodline_essence
ascension:default_physique_essence
ascension:default_cultivation_technique
ascension:default_battle_style
```

# Compact registry reference
```text
Skills:                    data/<namespace>/ascension/skills/
Techniques:                data/<namespace>/ascension/techniques/
Bloodlines:                data/<namespace>/ascension/bloodlines/
Physiques:                 data/<namespace>/ascension/physiques/
Paths:                     data/<namespace>/ascension/paths/
Progress actions:          data/<namespace>/ascension/progress_actions/
Progress conditions:       data/<namespace>/ascension/progress_action_conditions/
Tribulations:              data/<namespace>/ascension/tribulations/
Runtime visuals:           data/<namespace>/ascension/skill_system/visuals/
Beam definitions:           data/<namespace>/ascension/skill_system/runtime/beams/
Datapack resources:         data/<namespace>/ascension/skill_system/resources/
```
