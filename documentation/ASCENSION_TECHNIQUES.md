# Ascension Techniques
This page documents the normal datapack format used to create Ascension techniques.

A technique belongs to one cultivation path, controls how far that technique can progress, owns skill unlocks, raises skill progression caps, and may apply bonuses when realms change.

The intended workflow is:

**path + skills -> technique JSON -> technique manual -> game**

## Quick links
- [Files and IDs](#files-and-ids)
- [Minimal technique](#minimal-technique)
- [Root fields](#root-fields)
- [Skill unlocks and caps](#skill-unlocks-and-caps)
- [Requirements](#requirements)
- [Realm limits](#realm-limits)
- [Realm overrides](#realm-overrides)
- [Realm change handlers](#realm-change-handlers)
- [Technique manuals](#technique-manuals)
- [Complete example](#complete-example)
- [Compact reference](#compact-reference)

# Files and IDs
Technique files live in:

```text
data/<namespace>/ascension/techniques/...
```

For example:

```text
data/example/ascension/techniques/body/ember_body_art.json
```

becomes:

```text
example:body/ember_body_art
```

For normal datapack techniques, use:

```json
"type": "ascension:simple_technique"
```

# Minimal technique
```json
{
  "type": "ascension:simple_technique",
  "name": "Ember Body Art",
  "description": "Temper the body through disciplined fire circulation.",
  "path": "ascension:foundation/body",
  "max_realm": 3,
  "skills": {}
}
```

# Root fields
| Field | Default | Meaning |
|---|---|---|
| `name` | Required | Display name Component |
| `description` | Required | Description Component |
| `path` | Required | Path cultivated by this technique |
| `milestone_realms` | `[]` | Optional notable major-realm indices |
| `technique_families` | `[]` | Optional string metadata such as `star` or `bow` |
| `min_realm` | `0` | Minimum major realm |
| `max_realm` | Path maximum | Maximum major realm |
| `max_minor_realm` | Path value | Minor-realm cap at the technique's final major realm |
| `realm_overrides` | `{}` | Technique-specific realm names/progress requirements |
| `skills` | `{}` | Skills owned and progressed by the technique |
| `realm_change_handler` | `[]` | Realm-driven stat/path-bonus/skill actions |
| `requirements` | `[]` | Requirements that must pass before the technique can be acquired |
| `item_tooltip` | None | Technique manual presentation |

Realm numbers in technique JSON are zero-indexed.

# Skill unlocks and caps
`skills` maps a skill ID to its technique progression definition.

```json
"skills": {
  "example:ember_body/cultivation": {},
  "example:ember_body/iron_pulse": {
    "unlock": 1
  },
  "example:ember_body/scarlet_step": {
    "unlock": 2,
    "requirements": [
      {
        "type": "ascension:stat",
        "stat": "ascension:agility",
        "comparison": "at_least",
        "value": 20
      }
    ],
    "caps": {
      "4": "minor_mastery",
      "6": "major_mastery"
    }
  }
}
```

`unlock` is the zero-indexed major realm where the technique begins owning the skill. It defaults to `0`.

Caps are cumulative. The latest cap at or below the current major realm is used.

Active skills use mastery names:

```json
"caps": {
  "3": "minor_mastery",
  "5": "major_mastery"
}
```

Passive skills use numeric levels:

```json
"caps": {
  "3": 2,
  "5": 3
}
```

A cap raises the highest progression the player may train toward. It does not directly grant that mastery rank or passive level.

Each skill entry may also use `requirements`. The technique owns the skill only when both its `unlock` realm and all of its requirements pass. If a requirement later stops passing, the technique removes its ownership/cap for that skill; it is restored when the requirement passes again.

Active mastery IDs:

```text
initiate
minor_mastery
major_mastery
perfection
transcendence
```

# Requirements
The technique root and individual skill entries use the same shared requirement system.

Technique acquisition:

```json
"requirements": [
  {
    "type": "ascension:has_physique",
    "physique": "example:iron_bloom"
  }
]
```

Technique skill gate:

```json
"skills": {
  "example:ember_body/iron_pulse": {
    "unlock": 1,
    "requirements": [
      {
        "type": "ascension:skill_mastery",
        "skill": "example:ember_body/cultivation",
        "mastery": "minor_mastery"
      }
    ]
  }
}
```

See `DATAPACK_HELP.md` for the complete compact requirement list.

# Realm limits
`max_realm` limits the maximum major realm reachable with the technique.

```json
"max_realm": 6
```

`max_minor_realm` only overrides the minor-realm maximum at the technique's final major realm.

```json
"max_realm": 6,
"max_minor_realm": 2
```

`min_realm` defaults to `0`:

```json
"min_realm": 2
```

If `max_realm` is omitted, the technique uses the underlying path's normal maximum.

# Realm overrides
A technique may rename major realms or override individual minor-realm names and progress requirements.

```json
"realm_overrides": {
  "0": {
    "name": "Kindled Flesh",
    "realm_overrides": {
      "2": {
        "name": "Red Meridian Stage",
        "progress": 150
      }
    }
  }
}
```

Major keys are major-realm indices. Nested keys are minor-realm indices.

Supported author-facing overrides:

| Level | Fields |
|---|---|
| Major realm | `name`, `realm_overrides` |
| Minor realm | `name`, `progress` |

Anything not overridden falls back to the cultivated path.

# Realm change handlers
`realm_change_handler` uses Ascension's shared progression-action format.

```json
"realm_change_handler": [
  {
    "condition": "ascension:realm_change/all_realms",
    "actions": [
      {
        "type": "ascension:give_base_stats",
        "stats": [
          { "id": "ascension:vitality", "value": 1.0 },
          { "id": "ascension:strength", "value": 0.5 }
        ]
      }
    ]
  }
]
```

Useful shipped condition references:

```text
ascension:realm_change/on_gained
ascension:realm_change/all_realms
ascension:realm_change/all_major_realms
ascension:realm_change/all_minor_realms
```

Inline realm conditions:

```json
{
  "type": "ascension:major_realms_in",
  "realms": [1, 3, 5]
}
```

```json
{
  "type": "ascension:minor_realms_in",
  "realms": [1, 2]
}
```

```json
{
  "type": "ascension:realms_in",
  "realms": {
    "2": [0, 2],
    "4": [1]
  }
}
```

Built-in actions:

```text
ascension:give_base_stats
ascension:give_path_bonuses
ascension:grant_skills
ascension:remove_skills
```

Path bonus example:

```json
{
  "type": "ascension:give_path_bonuses",
  "bonuses": {
    "ascension:affinity": {
      "ascension:foundation/body": 0.1,
      "ascension:elemental/fire": 0.15
    }
  }
}
```

Actions are added when progression moves upward and removed when the matching realm is lost or the technique is removed.

# Technique manuals
Current technique manuals use the technique theme plus one of two default templates.

Cultivation technique:

```json
"item_tooltip": {
  "theme": "ascension:technique_manual",
  "template": "ascension:default_cultivation_technique",
  "rank": "ascension:profound"
}
```

Battle style:

```json
"item_tooltip": {
  "theme": "ascension:technique_manual",
  "template": "ascension:default_battle_style",
  "rank": "ascension:profound"
}
```

Use `default_cultivation_technique` when the technique includes a cultivation active/passive skill. Use `default_battle_style` for skill-only combat styles.

`theme_overrides` may customize individual manuals without creating a new theme.

# Complete example
```json
{
  "type": "ascension:simple_technique",
  "name": "Ember Body Art",
  "description": "Temper flesh through disciplined fire circulation.",
  "path": "ascension:foundation/body",
  "milestone_realms": [0, 2, 4, 6],
  "technique_families": ["fire", "body"],
  "max_realm": 6,
  "max_minor_realm": 2,
  "skills": {
    "example:ember_body/cultivation": {},
    "example:ember_body/falling_mountain_cut": {
      "unlock": 1,
      "caps": {
        "3": "minor_mastery",
        "5": "major_mastery"
      }
    },
    "example:ember_body/tempered_skin": {
      "unlock": 2,
      "caps": {
        "4": 2,
        "6": 3
      }
    }
  },
  "realm_change_handler": [
    {
      "condition": "ascension:realm_change/all_major_realms",
      "actions": [
        {
          "type": "ascension:give_base_stats",
          "stats": [
            { "id": "ascension:vitality", "value": 2.0 },
            { "id": "ascension:strength", "value": 1.0 }
          ]
        }
      ]
    }
  ],
  "item_tooltip": {
    "theme": "ascension:technique_manual",
    "template": "ascension:default_cultivation_technique",
    "rank": "ascension:profound"
  }
}
```

# Compact reference
```text
Type: ascension:simple_technique
Path: data/<namespace>/ascension/techniques/

Skill unlock default: 0
Active caps: initiate, minor_mastery, major_mastery, perfection, transcendence
Passive caps: positive integers

Realm conditions:
ascension:every_realm
ascension:every_minor_realm
ascension:every_major_realm
ascension:major_realms_in
ascension:minor_realms_in
ascension:realms_in

Progress actions:
ascension:give_base_stats
ascension:give_path_bonuses
ascension:grant_skills
ascension:remove_skills

Skill fields:
unlock
requirements
caps
```
