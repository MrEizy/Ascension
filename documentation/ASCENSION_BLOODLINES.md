# Ascension Bloodlines
This page documents the datapack format used to create Ascension bloodlines.

Bloodlines are purity-driven progression definitions. They may unlock paths and apply stat, path-bonus, or skill changes as purity rises from `1` to `100`.

The intended workflow is:

**bloodline identity -> purity milestones -> progression actions -> bloodline essence -> game**

## Quick links
- [Files and IDs](#files-and-ids)
- [Minimal bloodline](#minimal-bloodline)
- [Root fields](#root-fields)
- [Purity](#purity)
- [Purity handlers](#purity-handlers)
- [Progress actions](#progress-actions)
- [Bloodline essences](#bloodline-essences)
- [Connected bloodlines](#connected-bloodlines)
- [Evolution bloodlines](#evolution-bloodlines)
- [Complete example](#complete-example)
- [Compact reference](#compact-reference)

# Files and IDs
Bloodline files live in:

```text
data/<namespace>/ascension/bloodlines/...
```

For example:

```text
data/example/ascension/bloodlines/dragon/ember_dragon.json
```

becomes:

```text
example:dragon/ember_dragon
```

Current bloodlines use:

```json
"type": "ascension:simple"
```

Two more types add requirements and evolution on top of the normal fields: `ascension:connected_bloodline` and `ascension:evolution_bloodline`, see [Connected bloodlines](#connected-bloodlines) and [Evolution bloodlines](#evolution-bloodlines).

# Minimal bloodline
```json
{
  "type": "ascension:simple",
  "name": "Ember Dragon Bloodline",
  "description": "A thin trace of draconic fire runs through the blood.",
  "purity_handler": []
}
```

# Root fields
| Field | Default | Meaning |
|---|---|---|
| `name` | Required | Display name Component |
| `description` | Required | Description Component |
| `paths` | `[]` | Paths unlocked while the bloodline is owned |
| `purity_handler` | Required | Purity conditions and progression actions |
| `requirements` | `[]` | Requirements that must pass before the bloodline can be acquired |
| `item_tooltip` | None | Bloodline essence presentation |

Path example:

```json
"paths": [
  "ascension:foundation/body",
  "ascension:elemental/fire"
]
```

# Purity
Simple bloodlines use integer purity from `1` to `100`.

Purity changes are processed one point at a time. This is important for `purity_handler`: a condition covering every purity point may run many times, not once.

For example:

```json
{
  "condition": "ascension:all_purity",
  "actions": [
    {
      "type": "ascension:give_base_stats",
      "stats": [
        { "id": "ascension:vitality", "value": 0.1 }
      ]
    }
  ]
}
```

adds `0.1` Vitality per purity point. At 100 purity, that listener contributes `+10` Vitality in total.

When purity falls, matching gains are removed one step at a time.

# Purity handlers
A `purity_handler` is a list of condition/action listeners.

```json
"purity_handler": [
  {
    "condition": "ascension:on_gained",
    "actions": [
      {
        "type": "ascension:give_base_stats",
        "stats": [
          { "id": "ascension:spirit", "value": 2.0 }
        ]
      }
    ]
  },
  {
    "condition": {
      "type": "ascension:on_purity_in_range",
      "start": 50,
      "end": 50
    },
    "actions": [
      {
        "type": "ascension:give_base_stats",
        "stats": [
          { "id": "ascension:spirit", "value": 3.0 }
        ]
      }
    ]
  }
]
```

Useful shipped references:

| Reference | Meaning |
|---|---|
| `ascension:on_gained` | Purity `1` only |
| `ascension:all_purity` | Every purity point from `1` through `100` |

Inline condition:

```json
{
  "type": "ascension:on_purity_in_range",
  "start": 25,
  "end": 50
}
```

Both ends are inclusive.

Use a single-value range for a milestone:

```json
{
  "type": "ascension:on_purity_in_range",
  "start": 75,
  "end": 75
}
```

# Progress actions
Bloodlines currently use the shared progression actions.

## Base stats
```json
{
  "type": "ascension:give_base_stats",
  "stats": [
    { "id": "ascension:vitality", "value": 0.15 },
    { "id": "ascension:strength", "value": 0.1 }
  ]
}
```

## Path bonuses
```json
{
  "type": "ascension:give_path_bonuses",
  "bonuses": {
    "ascension:affinity": {
      "ascension:foundation/body": 0.02,
      "ascension:elemental/fire": 0.03
    }
  }
}
```

## Skills
Purity milestones may grant or remove skills:

```json
{
  "condition": {
    "type": "ascension:on_purity_in_range",
    "start": 50,
    "end": 50
  },
  "actions": [
    {
      "type": "ascension:grant_skills",
      "skills": ["example:passive/dragon_instinct"]
    }
  ]
}
```

When purity falls back through that milestone, the grant is reversed automatically. `ascension:remove_skills` behaves inversely: it removes skills while progressing upward and restores them when that progression is reversed.

Actions may also be stored as reusable files under:

```text
data/<namespace>/ascension/progress_actions/...
```

and referenced by ID:

```json
"actions": [
  "example:dragon_base_growth"
]
```

Conditions can be stored similarly under `progress_action_conditions`.

# Bloodline essences
The standard tooltip setup is:

```json
"item_tooltip": {
  "theme": "ascension:bloodline_essence",
  "template": "ascension:default_bloodline_essence",
  "rank": "ascension:ordinary"
}
```

Shipped bloodlines currently use rank IDs such as:

```text
ascension:ordinary
ascension:profound
ascension:heaven
ascension:saint
ascension:god
ascension:heavens_path
```

`theme_overrides` may customize a specific essence while keeping the default template.

# Connected bloodlines
`ascension:connected_bloodline` is a normal bloodline that can only be gained while the player already has something else. It has every field of `ascension:simple`, plus the connection fields below. At least one connection is required.

| Field | Meaning |
|---|---|
| `physique_connection` | The physique(s) the player must have |
| `technique_connection` | The technique(s) the player must have |
| `path_connection` | The path(s) the player must have |

A connection is one ID, or a list of IDs. With a list the player needs at least one of the listed IDs. When several connections are set, every one of them has to be met. IDs are full registry IDs including any folder, for example `example:body/ember_body_art`.

A connected definition cannot require its own kind, so it has no connection field for it. It also fails to load if it has none of its connection fields.

The connections are checked when the essence or manual is used. If one is missing the player is told what it is, for example `Requires the technique: Ember Body Art`, and nothing changes.

```json
{
  "type": "ascension:connected_bloodline",
  "name": "Awakened Ember Blood",
  "description": "Only stirs in a body that already holds the right physique.",
  "physique_connection": "example:body/iron_bloom",
  "purity_handler": []
}
```

# Evolution bloodlines
`ascension:evolution_bloodline` is a normal bloodline that can evolve into another bloodline. It has every field of `ascension:simple`, plus an `evolution` object. It is usually written at the top of the file.

```json
{
  "type": "ascension:evolution_bloodline",
  "evolution": {
    "into": "example:saint/celestial_war_tiger",
    "catalyst": "example:profound/crane_bloodline",
    "absorb_catalyst": ["minecraft:nether_star", "minecraft:amethyst_block"],
    "purity": 80,
    "result_purity": 10,
    "realm": {
      "path": "ascension:foundation/body",
      "major_realm": 2,
      "minor_realm": 1
    },
    "path": "ascension:foundation/body"
  },
  "name": "Barbarian Bloodline",
  "description": "A thin bloodline that can still awaken into something greater.",
  "purity_handler": []
}
```
| `evolution` field | Default | Meaning |
|---|---|---|
| `into` | Required | The bloodline it evolves into |
| `catalyst` | None | Bloodline(s) whose essence triggers the evolution when used |
| `absorb_catalyst` | None | Item or block ID(s) that trigger the evolution when absorbed |
| `purity` | None | `1` to `100`. The purity the bloodline the player already has must have reached. The purity of the catalyst essence does not matter |
| `result_purity` | `1` | `1` to `100`. The purity the evolved bloodline starts with |
| `realm` | None | The path realm the player must be at least |
| `path` | None | The path(s) the player must have, at least one of them |

At least one of `catalyst` and `absorb_catalyst` is required, and either one can trigger the evolution. `catalyst`, `absorb_catalyst` and `path` take one ID or a list. Every requirement that is set has to be met.

`realm` is written as `path`, `major_realm` and `minor_realm` (default `0`), using the same zero-based numbers as the path file. The player must be at least that realm on that path.

An evolution only happens to a player who currently has this bloodline. The evolved bloodline is added first, with `result_purity`, and then this one is removed.

| Trigger | What happens |
|---|---|
| Use a bloodline essence listed in `catalyst` | If every requirement is met the bloodline evolves into `into` and the essence is used up. Otherwise the player is told what is missing and nothing changes |
| Absorb an ID listed in `absorb_catalyst` | An `ascension:absorption_cultivation` skill with `evolution_type` on that block or item absorbs it, and if every requirement is met the bloodline evolves into `into`. See `ASCENSION_SKILLS.md` |

# Complete example
```json
{
  "type": "ascension:simple",
  "name": "Ember Dragon Bloodline",
  "description": "Draconic fire strengthens as the bloodline becomes purer.",
  "paths": [
    "ascension:foundation/body",
    "ascension:elemental/fire"
  ],
  "purity_handler": [
    {
      "condition": "ascension:all_purity",
      "actions": [
        {
          "type": "ascension:give_base_stats",
          "stats": [
            { "id": "ascension:vitality", "value": 0.12 },
            { "id": "ascension:strength", "value": 0.08 }
          ]
        },
        {
          "type": "ascension:give_path_bonuses",
          "bonuses": {
            "ascension:affinity": {
              "ascension:foundation/body": 0.01,
              "ascension:elemental/fire": 0.015
            }
          }
        }
      ]
    },
    {
      "condition": {
        "type": "ascension:on_purity_in_range",
        "start": 50,
        "end": 50
      },
      "actions": [
        {
          "type": "ascension:give_base_stats",
          "stats": [
            { "id": "ascension:spirit", "value": 3.0 }
          ]
        },
        {
          "type": "ascension:grant_skills",
          "skills": ["example:passive/dragon_instinct"]
        }
      ]
    },
    {
      "condition": {
        "type": "ascension:on_purity_in_range",
        "start": 100,
        "end": 100
      },
      "actions": [
        {
          "type": "ascension:give_base_stats",
          "stats": [
            { "id": "ascension:vitality", "value": 5.0 },
            { "id": "ascension:strength", "value": 5.0 }
          ]
        }
      ]
    }
  ],
  "item_tooltip": {
    "theme": "ascension:bloodline_essence",
    "template": "ascension:default_bloodline_essence",
    "rank": "ascension:profound"
  }
}
```

# Compact reference
```text
Types: ascension:simple, ascension:connected_bloodline, ascension:evolution_bloodline
Path: data/<namespace>/ascension/bloodlines/
Purity: 1..100

Connected bloodline fields (at least one):
physique_connection
technique_connection
path_connection

Evolution bloodline:
evolution: into, catalyst, absorb_catalyst, purity, result_purity, realm, path

Condition:
ascension:on_purity_in_range

Useful references:
ascension:on_gained
ascension:all_purity

Actions:
ascension:give_base_stats
ascension:give_path_bonuses
ascension:grant_skills
ascension:remove_skills

Default tooltip:
ascension:bloodline_essence
ascension:default_bloodline_essence
```