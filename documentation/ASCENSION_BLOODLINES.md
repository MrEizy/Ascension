# Ascension Bloodlines
This page documents the datapack format used to create Ascension bloodlines.

Bloodlines are purity-driven progression definitions. They may unlock paths and apply stat or path-bonus gains as purity rises from `1` to `100`.

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
Type: ascension:simple
Path: data/<namespace>/ascension/bloodlines/
Purity: 1..100

Condition:
ascension:on_purity_in_range

Useful references:
ascension:on_gained
ascension:all_purity

Actions:
ascension:give_base_stats
ascension:give_path_bonuses

Default tooltip:
ascension:bloodline_essence
ascension:default_bloodline_essence
```
