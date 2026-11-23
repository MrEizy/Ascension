# Ascension Techniques
This page documents the normal datapack format used to create Ascension techniques.

A technique is a generic origin source, much like a bloodline or physique. It has very little predefined behaviour: when it is learned it runs a list of progression actions, and when it is removed it reverses them. Skills, stats and path bonuses are all granted through those actions.

Techniques are not tied to a path. A technique and a path can still affect each other, but only through the generic systems every source uses (requirements, skills, stats, path bonuses).

The intended workflow is:

**skills -> technique JSON -> technique manual -> game**

## Quick links
- [Files and IDs](#files-and-ids)
- [Minimal technique](#minimal-technique)
- [Root fields](#root-fields)
- [On added actions](#on-added-actions)
- [Requirements](#requirements)
- [Technique manuals](#technique-manuals)
- [Connected techniques](#connected-techniques)
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

A technique that needs something else first uses `ascension:connected_technique`, see [Connected techniques](#connected-techniques).

# Minimal technique
```json
{
  "type": "ascension:simple_technique",
  "name": "Ember Body Art",
  "description": "Temper the body through disciplined fire circulation."
}
```

# Root fields
| Field | Default | Meaning |
|---|---|---|
| `name` | Required | Display name Component |
| `description` | Required | Description Component |
| `on_added` | `[]` | Progression actions run when the technique is learned, reversed when it is removed |
| `requirements` | `[]` | Requirements that must pass before the technique can be acquired |
| `item_tooltip` | None | Technique manual presentation |

# On added actions
`on_added` is a list of progression actions. Each entry is either the ID of a shared action in `data/<namespace>/ascension/progress_actions/` or an inline action. Unlike bloodline and path handlers, these actions have no condition: they always run.

```json
"on_added": [
  {
    "type": "ascension:grant_skills",
    "skills": [
      "example:ember_body/cultivation",
      "example:ember_body/iron_pulse"
    ]
  },
  {
    "type": "ascension:give_stats",
    "stats": {
      "ascension:vitality": {
        "flat": [
          { "id": "example:ember_body_art_vitality", "value": 2.0 }
        ],
        "multiplier": []
      }
    }
  },
  "example:shared_body_stats"
]
```

The list runs in two directions:

| Event | Direction | Effect |
|---|---|---|
| Technique learned | Up | `grant_skills` grants each skill, `give_stats` adds its modifiers, `give_path_bonuses` adds its bonuses |
| Technique removed | Down | The same list runs again in reverse: skills are removed and modifiers/bonuses are taken away |

Skills granted by a technique are owned by the technique's ID. A skill granted by several sources is only removed once every owner has removed it.

The salt passed to the actions is the technique ID without its namespace (`example:body/ember_body_art` -> `body/ember_body_art`). With the default `salt` conflict action, two techniques that use the same modifier ID still get separate modifiers.

Built-in actions:

```text
ascension:give_stats
ascension:give_path_bonuses
ascension:grant_skills
ascension:remove_skills
```

See `DATAPACK_HELP.md` for the full action formats and conflict actions.

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

# Requirements
Technique acquisition uses the shared requirement system:

```json
"requirements": [
  {
    "type": "ascension:has_physique",
    "physique": "example:iron_bloom"
  }
]
```

See `DATAPACK_HELP.md` for the complete compact requirement list.

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

Use `default_cultivation_technique` when the technique includes a cultivation active/passive skill. Use `default_battle_style` for skill-only combat styles. Both templates list the technique's `on_added` gains.

`theme_overrides` may customize individual manuals without creating a new theme.

# Connected techniques
`ascension:connected_technique` is a normal technique that can only be learned while the player already has something else. It has every field of `ascension:simple_technique`, plus the connection fields below. At least one connection is required.

| Field | Meaning |
|---|---|
| `physique_connection` | The physique(s) the player must have |
| `bloodline_connection` | The bloodline(s) the player must have |
| `path_connection` | The path(s) the player must have |

A connection is one ID, or a list of IDs. With a list the player needs at least one of the listed IDs. When several connections are set, every one of them has to be met. IDs are full registry IDs including any folder, for example `example:body/ember_body_art`.

A connected definition cannot require its own kind, so it has no connection field for it. It also fails to load if it has none of its connection fields.

The connections are checked when the essence or manual is used. If one is missing the player is told what it is, for example `Requires the technique: Ember Body Art`, and nothing changes.

```json
{
  "type": "ascension:connected_technique",
  "name": "Boundless Qi Scripture",
  "description": "A breathing method that only a certain physique can follow.",
  "physique_connection": "example:profound/god_envied_physique",
  "on_added": [
    {
      "type": "ascension:grant_skills",
      "skills": ["example:boundless/cultivation"]
    }
  ]
}
```

Unlike physiques and bloodlines, techniques cannot evolve.

# Complete example
```json
{
  "type": "ascension:simple_technique",
  "name": "Ember Body Art",
  "description": "Temper flesh through disciplined fire circulation.",
  "requirements": [
    {
      "type": "ascension:has_physique",
      "physique": "example:iron_bloom"
    }
  ],
  "on_added": [
    {
      "type": "ascension:grant_skills",
      "skills": [
        "example:ember_body/cultivation",
        "example:ember_body/falling_mountain_cut",
        "example:ember_body/tempered_skin"
      ]
    },
    {
      "type": "ascension:give_stats",
      "stats": {
        "ascension:vitality": {
          "flat": [
            { "id": "example:ember_body_art_vitality", "value": 2.0 }
          ],
          "multiplier": []
        },
        "ascension:strength": {
          "flat": [
            { "id": "example:ember_body_art_strength", "value": 1.0 }
          ],
          "multiplier": []
        }
      }
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
Types: ascension:simple_technique, ascension:connected_technique
Path: data/<namespace>/ascension/techniques/

Root fields:
name
description
on_added
requirements
item_tooltip

Connected technique fields (at least one):
physique_connection
bloodline_connection
path_connection

on_added: list of action IDs or inline actions, no condition
Learned: actions run upward
Removed: actions run downward (reversed)
Salt: technique ID path

Progress actions:
ascension:give_stats
ascension:give_path_bonuses
ascension:grant_skills
ascension:remove_skills
```