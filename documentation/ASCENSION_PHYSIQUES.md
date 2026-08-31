# Ascension Physiques
This page documents the datapack format used to create Ascension physiques.

Physiques are persistent origin traits. They may unlock paths and skills, add flat stats, attach stat modifiers, and change path-bonus containers such as affinities while the physique is owned.

The intended workflow is:

**physique identity -> paths/skills -> stats and path bonuses -> physique essence -> game**

## Quick links
- [Files and IDs](#files-and-ids)
- [Minimal physique](#minimal-physique)
- [Root fields](#root-fields)
- [Base stats](#base-stats)
- [Stat modifiers](#stat-modifiers)
- [Path bonuses](#path-bonuses)
- [Requirements](#requirements)
- [Physique essences](#physique-essences)
- [Complete example](#complete-example)
- [Compact reference](#compact-reference)

# Files and IDs
Physique files live in:

```text
data/<namespace>/ascension/physiques/...
```

For example:

```text
data/example/ascension/physiques/body/iron_bloom.json
```

becomes:

```text
example:body/iron_bloom
```

Normal physiques use:

```json
"type": "ascension:simple"
```

# Minimal physique
`paths` is required, but it may be an empty list if the physique should not unlock a path.

```json
{
  "type": "ascension:simple",
  "name": "Iron Bloom Physique",
  "description": "Dense bones and resilient meridians support body cultivation.",
  "paths": [],
  "skills": []
}
```

# Root fields
| Field | Default | Meaning |
|---|---|---|
| `name` | Required | Display name Component |
| `description` | Required | Description Component |
| `paths` | Required | Paths unlocked while the physique is owned |
| `skills` | `[]` | Skills unlocked while the physique is owned |
| `base_stats` | `{}` | Flat additions to base stats |
| `stat_modifiers` | `{}` | Value-container modifiers by stat |
| `base_path_bonuses` | `{}` | Flat path bonuses by category and path |
| `path_bonus_modifiers` | `{}` | Path-bonus modifiers by category and path |
| `requirements` | `[]` | Requirements that must pass before the physique can be acquired |
| `item_tooltip` | None | Physique essence presentation |

Paths:

```json
"paths": [
  "ascension:foundation/body",
  "ascension:elemental/earth"
]
```

Skills:

```json
"skills": [
  "example:passive/stone_skin"
]
```

All bonuses and skills are added when the physique is gained and removed when it is lost.

# Base stats
`base_stats` is a stat-to-number map.

```json
"base_stats": {
  "ascension:vitality": 2.0,
  "ascension:strength": 1.0,
  "ascension:spirit": 0.5
}
```

Current Ascension core stats include:

```text
ascension:spirit
ascension:vitality
ascension:strength
ascension:agility
```

# Stat modifiers
`stat_modifiers` maps a stat ID to one or more value-container modifiers.

```json
"stat_modifiers": {
  "ascension:vitality": [
    {
      "value": 0.15,
      "operation": "MULTIPLY_FINAL",
      "id": "example:iron_bloom_vitality"
    }
  ]
}
```

Operations:

```text
ADD_BASE
MULTIPLY_BASE
ADD_FINAL
MULTIPLY_FINAL
```

Use a unique `id` for each modifier. Ascension uses that ID when removing the physique's modifier later.

# Path bonuses
Physiques use generic path-bonus fields.

## Flat path bonuses
```json
"base_path_bonuses": {
  "ascension:affinity": {
    "ascension:foundation/body": 1.0,
    "ascension:elemental/earth": 2.0
  }
}
```

The first key is the bonus category. The second key is the path.

`ascension:affinity` is the common category used by shipped physiques.

## Path-bonus modifiers
```json
"path_bonus_modifiers": {
  "ascension:affinity": {
    "ascension:elemental/earth": [
      {
        "value": 0.2,
        "operation": "ADD_BASE",
        "id": "example:iron_bloom_earth_affinity"
      }
    ]
  }
}
```

The shape is:

```text
category -> path -> modifier list
```

# Requirements
Physiques use the shared Ascension requirement list.

```json
"requirements": [
  {
    "type": "ascension:path_realm",
    "path": "ascension:foundation/body",
    "minimum_major_realm": 2
  }
]
```

See `DATAPACK_HELP.md` for the compact requirement reference.

Physiques may also grant toggleable state/form passives through `skills`. The form behaviour itself belongs in the passive skill JSON, so the physique can stay focused on origin bonuses.

# Physique essences
The standard tooltip setup is:

```json
"item_tooltip": {
  "theme": "ascension:physique_essence",
  "template": "ascension:default_physique_essence",
  "rank": "ascension:ordinary"
}
```

Shipped physiques currently use rank IDs such as:

```text
ascension:ordinary
ascension:profound
ascension:heaven
ascension:saint
ascension:god
ascension:heavens_path
```

`theme_overrides` may customize a specific essence while keeping the standard template.

# Complete example
```json
{
  "type": "ascension:simple",
  "name": "Iron Bloom Physique",
  "description": "Dense bones and earth-aligned meridians reinforce the body.",
  "paths": [
    "ascension:foundation/body",
    "ascension:elemental/earth"
  ],
  "skills": [
    "example:passive/stone_skin"
  ],
  "base_stats": {
    "ascension:vitality": 2.0,
    "ascension:strength": 1.0
  },
  "stat_modifiers": {
    "ascension:vitality": [
      {
        "value": 0.15,
        "operation": "MULTIPLY_FINAL",
        "id": "example:iron_bloom_vitality"
      }
    ]
  },
  "base_path_bonuses": {
    "ascension:affinity": {
      "ascension:foundation/body": 1.0,
      "ascension:elemental/earth": 2.0
    }
  },
  "path_bonus_modifiers": {
    "ascension:affinity": {
      "ascension:elemental/earth": [
        {
          "value": 0.2,
          "operation": "ADD_BASE",
          "id": "example:iron_bloom_earth_affinity"
        }
      ]
    }
  },
  "item_tooltip": {
    "theme": "ascension:physique_essence",
    "template": "ascension:default_physique_essence",
    "rank": "ascension:profound"
  }
}
```

# Compact reference
```text
Type: ascension:simple
Path: data/<namespace>/ascension/physiques/

Fields:
paths
skills
base_stats
stat_modifiers
base_path_bonuses
path_bonus_modifiers
requirements
item_tooltip

Modifier operations:
ADD_BASE
MULTIPLY_BASE
ADD_FINAL
MULTIPLY_FINAL

Default tooltip:
ascension:physique_essence
ascension:default_physique_essence
```
