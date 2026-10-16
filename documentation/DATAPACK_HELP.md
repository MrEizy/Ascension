# Ascension Datapacks
This page is the short reference for Ascension datapack layout and the common JSON shapes shared by several systems.

For system-specific authoring, use:

- `ASCENSION_SKILLS.md` for skills, runtime conditions, states, and resources
- `ASCENSION_TECHNIQUES.md` for techniques and realm-driven progression
- `ASCENSION_BLOODLINES.md` for purity-driven bloodlines
- `ASCENSION_PHYSIQUES.md` for persistent physique bonuses

## Quick links
- [Files and IDs](#files-and-ids)
- [Types](#types)
- [Components](#components)
- [Base stats](#base-stats)
- [Value modifiers](#value-modifiers)
- [Path bonuses](#path-bonuses)
- [Requirements](#requirements)
- [Progress actions](#progress-actions)
- [Datapack resources](#datapack-resources)
- [Tribulations](#tribulations)
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

Bloodline and technique progression actions use a list instead:

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

# Requirements
Bloodlines, physiques, techniques, and individual technique skills may use Ascension's shared requirement list.

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

Technique skill requirements use the same format. If their requirements stop passing, the technique stops owning that skill until the requirements pass again.

# Progress actions
Bloodlines and techniques share a small progression-action system.

A handler is a list of condition/action pairs:

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

See the bloodline and technique guides for the inline condition formats and their exact progression semantics.

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

# Tribulations
Tribulations live in:

```text
data/<namespace>/ascension/tribulations/...
```

`ascension:lightning` strikes the target with lightning `strikes` times, waiting `tick_delay` ticks between strikes. The storm first gathers (`clouds.forming`) before the first strike, and after the last strike (or the target's death) it breaks up (`clouds.dissipating`) before the tribulation succeeds (or fails):

```json
{
  "type": "ascension:lightning",
  "strikes": 4,
  "damage": 2.0,
  "tick_delay": 10,
  "clouds": {
    "radius": 72,
    "rotation_speed": 0.8,
    "render_height": 140,
    "tint": "#FF4040",
    "pixel_size": 3
  }
}
```

`clouds` is optional and controls the storm clouds rendered above the tribulation: a pixelated hurricane of banded, lumpy storm clouds around a tall eyewall, with a glowing eye in the middle. Small bolts crawl along the spiral bands all over the cloud, and the eye flares on every strike. Every field in it is optional:

| Field | Default | Meaning |
| --- | --- | --- |
| `radius` | `64` | Radius of the cloud whirlpool in blocks (1 to 512). |
| `rotation_speed` | `0.5` | Degrees rotated per tick. Negative values rotate the other way. |
| `render_height` | `200` | The world Y value of the cloud base. The storm is flat underneath and thickest at the eyewall, and follows the tribulation on X and Z. |
| `tint` | none | Colour multiplied onto the cloud colours, as `"#RRGGBB"`, an int, or `[r, g, b]` floats. A quick way to recolour the clouds. Does not affect the eye. |
| `pixel_size` | `3` | Size of a cloud pixel in blocks at the outer edge (0.25 to 64). Pixels shrink towards the eye along with the billows. |
| `cloud_colors` | greys | The `shadow`, `base` and `highlight` colours of the clouds. Thick undersides use the shadow, thin cloud the base, and puff tops and the top of the storm the highlight. Defaults `#333333`, `#8C8C8C`, `#E6E6E6`. |
| `eye_colors` | dim purple | The `shadow`, `base` and `highlight` colours of the glowing eye, from dim to flaring. The highlight also colours the lightning: the bolts in the clouds and the glow they cast. Defaults `#110F1D`, `#24213F`, `#9999BF`. |
| `forming` | 100 ticks, `cores` | How the storm gathers before the first strike. A transition object, see below. |
| `dissipating` | 60 ticks, `cores` | How the storm breaks up after the last strike or the target's death, before the tribulation resolves. A transition object, see below. |

`forming` and `dissipating` take the same fields, and every one of them is optional:

```json
"forming": { "ticks": 200, "style": "rim", "easing": "ease_out" },
"dissipating": { "ticks": 40, "style": "eye", "easing": "ease_in" }
```

| Field | Default | Meaning |
| --- | --- | --- |
| `ticks` | `100` / `60` | How long it takes (0 to 6000). `0` is instant. |
| `style` | `cores` | Which part of the storm exists first while forming, and last while dissipating. Dissipating plays the style backwards. |
| `easing` | `linear` | The curve the progress follows: `linear`, `ease_in` (slow start), `ease_out` (slow finish) or `smooth` (slow at both ends). |
| `patchiness` | `0.25` | How broken up and ragged the edge of the gathered cloud is (0 to 1). |
| `closed_at` | `0.85` | The point (0 to 1 of the way formed) where the cloud is complete with no holes left. |
| `eye_at` | `0.6` | The point where the eye starts to glow. Below it the eye is open sky. |
| `lightning_at` | `0.8` | The point above which lightning crawls through the clouds. |
| `spin_start` | `0.3` | How fast the storm turns when it starts forming, as a fraction of `rotation_speed`. It speeds up to full as it forms. |

Styles:

| Style | Forming | Dissipating |
| --- | --- | --- |
| `cores` | The eyewall and band cores condense first, then the thin cloud between them fills in. | The thin cloud goes first, the cores last. |
| `rim` | Gathers from the outer edge and sweeps inward, the eye last. | Retreats outward, the rim last. |
| `eye` | Spreads outward from the eye, the rim last. | Collapses back into the eye. |
| `scatter` | Random patches appear everywhere and merge. | Breaks into random patches that fade. |

Every colour in `cloud_colors` and `eye_colors` is optional and takes the same formats as `tint`. For a red tribulation with a fiery eye:

```json
"cloud_colors": { "shadow": "#1E1214", "base": "#5A3A3E", "highlight": "#C8A0A0" },
"eye_colors": { "shadow": "#200808", "base": "#7A1410", "highlight": "#FF8060" }
```

`ascension:v2_tribulation_type` takes exactly the same fields and strikes the same way as `ascension:lightning`, but draws its storm as a vortex of wispy, see-through streaks swirling into the glowing eye instead of the pixelated hurricane. The streaks are drawn in three thin stacked layers that turn at slightly different speeds, and the vortex rises gently towards the eye. `render_height` is the height of the rim, and `pixel_size` is ignored. In the colours, the `shadow` is used for faint haze, the `base` for the streaks, and the `highlight` for the bright filaments. The eye's highlight also lights up the clouds around the eye.

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
