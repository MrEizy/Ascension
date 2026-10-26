# Ascension Golden Cores
This page documents the datapack format for golden cores and how they behave at runtime.

A golden core is formed on a path at a specific realm. The core then tracks that path, and grows with it: each realm the path reaches above the realm the core was acquired at can run its own actions. Unlike physiques or techniques, an origin can hold **several golden cores, including several copies of the same core**. Each held core is a separate instance with its own unique id (UUID).

## Quick links
- [Files and IDs](#files-and-ids)
- [Minimal golden core](#minimal-golden-core)
- [Root fields](#root-fields)
- [Realm actions](#realm-actions)
- [Runtime behaviour](#runtime-behaviour)
- [Forming a golden core](#forming-a-golden-core)
- [Complete example](#complete-example)
- [Debug command](#debug-command)

# Files and IDs
Golden core files live in:

```text
data/<namespace>/ascension/golden_cores/...
```

A file at `data/example/ascension/golden_cores/flame_core.json` has the ID `example:flame_core`.

For normal datapack golden cores, use:

```json
"type": "ascension:simple"
```

# Minimal golden core
```json
{
  "type": "ascension:simple",
  "name": "Flame Core",
  "description": "A core tempered in true fire."
}
```

# Root fields
| Field | Default | Meaning |
|---|---|---|
| `name` | Required | Display name Component |
| `description` | Required | Description Component |
| `skills` | `[]` | Skills granted while the core is held |
| `stats` | `{}` | Stat modifiers applied while the core is held (see `DATAPACK_HELP.md` value modifiers) |
| `path_bonus` | `{}` | Path bonus modifiers applied while the core is held |
| `on_added` | `[]` | Progress action list, run upward when the core is added and downward when removed |
| `realm_actions` | `[]` | Condition/action pairs run for each realm above the acquisition realm |
| `item_tooltip` | none | Item tooltip definition |
| `requirements` | none | Requirements, for use by whatever grants the core |

`stats` and `path_bonus` modifier ids are automatically salted with the instance id, so two copies of the same core both apply their modifiers instead of overwriting each other. Give progress actions the default `"conflict_action": "salt"` for the same reason: the salt passed to `on_added` and `realm_actions` always includes the instance id.

# Realm actions
`realm_actions` uses the same condition/action pair format as path and bloodline handlers:

```json
"realm_actions": [
  {
    "condition": "ascension:realm_change/all_major_realms",
    "actions": [
      { "type": "ascension:give_stats", "stats": { "ascension:vitality": { "flat": [ { "id": "example:flame_core_major", "value": 1 } ], "multiplier": [] } } }
    ]
  }
]
```

All realm conditions (`ascension:every_realm`, `ascension:every_major_realm`, `ascension:major_realms_in`, ...) work, and are tested against the exact realm being entered or left.

Realm actions run for every realm in **(acquisition realm, current realm]**. The acquisition realm itself never runs realm actions, use `on_added` for anything the core gives straight away.

# Runtime behaviour
- **Adding late:** a core added while its path is already above the acquisition realm immediately runs the realm actions for every realm in between.
- **Breakthroughs and regression:** each realm the path enters runs its actions upward, and each realm it loses runs them downward. If the path drops to or below the acquisition realm, or the path is removed entirely, every realm action is rolled back but the **core stays**. Removing the core is left to whatever owns it.
- **Removal:** removing a core first rolls back its realm actions, then reverses `on_added`, skills, stats and path bonuses.
- **Ownership:** like skills and paths, cores are added with an owner id and only removed once no owners remain. Adding with an existing instance id only adds the owner. If the path or acquisition realm differ from the held core, the core is removed and re-applied with the new details (realm actions are rolled back and replayed). If the core id differs (e.g. the core has evolved), the held core is left untouched.
- **Saving:** cores are saved with the origin and wait for their owners to re-add them on load, like skills. Realm progress is not saved, it is rebuilt from the tracked path.

# Forming a golden core
Golden cores are formed through a path breakthrough. Three pieces work together:

1. **The tribulation** — an `ascension:golden_core` tribulation. It plays out like a lightning tribulation (`strikes`, `damage`, `tick_delay`), and its data also records which golden core it forms.
   ```json
   { "type": "ascension:golden_core", "strikes": 4, "damage": 2.0, "tick_delay": 10 }
   ```
2. **The path realm** — the minor realm uses that tribulation in manual mode, so it waits at full progress instead of starting on its own:
   ```json
   { "name": "...", "progress": 100000, "tribulation": "ascension:golden_core_tribulation", "tribulation_mode": "manual" }
   ```
3. **The skill** — an `ascension:golden_core_forming` skill (see `ASCENSION_SKILLS.md`) naming the core, path and realm. Casting it triggers the realm's tribulation and sets the core on the tribulation's data before it starts.

When the tribulation succeeds the path breaks through and keeps the completed tribulation for that realm, as it does for every breakthrough tribulation. The completed tribulation then adds the golden core:
- the core tracks the skill's `path`, its acquisition realm is the skill's `realm`, and it gets a new instance id stored on the tribulation's data
- the **tribulation is the owner** (`ascension:tribulation/<tribulation data id>`)
- if the path drops below that realm, or is removed, the tribulation removes its ownership. Reaching the realm again adds the core back with the same instance id
- a failed tribulation forms nothing, and can be triggered again

# Complete example
```json
{
  "type": "ascension:simple",
  "name": { "translate": "ascension.golden_core.test_core.name" },
  "description": { "translate": "ascension.golden_core.test_core.desc" },
  "stats": {
    "ascension:vitality": {
      "flat": [ { "id": "ascension:test_core_vitality_base", "value": 1 } ],
      "multiplier": []
    }
  },
  "realm_actions": [
    {
      "condition": "ascension:realm_change/all_realms",
      "actions": [
        {
          "type": "ascension:give_stats",
          "stats": {
            "ascension:vitality": {
              "flat": [ { "id": "ascension:test_core_vitality_realm", "value": 0.5 } ],
              "multiplier": []
            }
          }
        }
      ]
    }
  ]
}
```

# Debug command
```text
/ascension golden_core add <players> <core> <path> <major> <minor>
/ascension golden_core remove <players> <uuid>
/ascension golden_core list <players>
```

Cores added through the command are owned by `ascension:command`, so `remove` only removes that ownership. `list` shows each instance's id, tracked path, acquisition realm, the highest realm whose actions are applied and its owners.
