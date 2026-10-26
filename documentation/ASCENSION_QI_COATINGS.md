# Ascension Qi Coatings
This page documents Qi Coatings: the looks a cultivator's qi takes on their hands, body or weapon (frost creeping over the fists, molten gold, qi coating on the blade.), how they are built from datapack files, and how to add new ones.

The short version: **a coating is data, a look is code.** A coating JSON says where the coating goes and which built-in looks draw it; the looks themselves are shader code you pick by name.

The chain from a hotbar skill to pixels on screen is:

**castable skill -> passive skill -> (element) -> runtime visual -> qi coating -> looks**

## Quick links
- [The Qi Coating skills and elements](#the-qi-coating-skills-and-elements)
- [On-hit and defence effects](#on-hit-and-defence-effects)
- [How a coating reaches the player](#how-a-coating-reaches-the-player)
- [Files and IDs](#files-and-ids)
- [Minimal coatings](#minimal-coatings)
- [Root fields](#root-fields)
- [Shells](#shells)
- [Texture shells](#texture-shells)
- [Skin](#skin)
- [Sword](#sword)
- [Hand particles](#hand-particles)
- [Effects](#effects)
- [Built-in looks](#built-in-looks)
- [Adding a new coating (datapack only)](#adding-a-new-coating-datapack-only)
- [Adding a new look (code)](#adding-a-new-look-code)
- [Values that must match](#values-that-must-match)

# The Qi Coating skills and elements
Players get two skills when any of their paths reaches its 3rd major realm (every path's `actions` grant them with the `ascension:realm_change/third_major_realm` condition):

| Skill | Coats | State group / visual key |
|---|---|---|
| **Qi Coating** (`castable/qi_coating/qi_coating`) | the hands | `ascension:qi_coating` |
| **External Qi Coating** (`castable/qi_coating/external_qi_coating`) | the held weapon | `ascension:external_qi_coating` |

The two have separate state groups and visual keys, so one of each can be on at a time (`QiCoatingChannel`: `body` and `external`).

**Elements.** Which coating each skill shows is the player's pick in the **coating wheel**: with one of the two skills selected on the hotbar, hold Shift (the sneak key) and the skill wheel key.
The wheel lists the elements whose path the player has. Elements are data, in `skill_system/qi_coating_elements/`:

```json
{
  "name": { "translate": "ascension.qi_coating.element.fire.name" },
  "path": "ascension:elemental/fire",
  "body_visual": "ascension:qi_coating/fire_qi_coating",
  "external_visual": "ascension:qi_coating/fire_external_qi_coating",
  "order": 1
}
```

`body_visual` and `external_visual` are runtime visual ids, as for `ascension:persistent_visual`. An element without one isn't offered on that skill. `path` is optional: an element without it is open to everyone. 
The built-in elements are Pure Qi (`pure`, no path, first in the wheel, so it is what a new player starts with), Fire, Earth, Water, Ice, Metal, Wind, Lightning, Space and Time. Time's hand coating is a still violet film that glitches and throws flickering copies of the arm for a moment when the player is hurt (`"reacts_to": ["hurt"]`), and its weapon coating a still violet tint that does the same when they swing (`"reacts_to": ["attack"]`). Pure Qi draws with the `pure_qi` shell look (style 57, an even pale cyan film round the hand, softening away up the forearm) and the `pure_qi` sword look (sword style 9, the blade tinted pale cyan).

The pick is stored per player (`CoreAttachments.QI_COATING_SELECTION`, kept through death and relogging). Picking a different element while the coating is on switches it: the old coating comes off and shrinks away (`grow_millis`), and only then does the new one go on (`QiCoatingSelectionService`). If the player loses the path for the element they're showing, it falls back to the first element they still have, or turns off if there's none left.

The skills' passives use two pieces built for this:
- `ascension:qi_coating_visual` (action, `"channel": "body" | "external"`) shows the picked element in `on_enable`. Taking it off on disable and putting it back on relog work like `persistent_visual`.
- `ascension:has_qi_coating_element` (condition, same `channel`) is an `enable_conditions` entry, so the coating can't be turned on with no element to show.

Whole-body layers and Lightning Step are standalone coating skills, held by the **Qi Coatings Leftovers** test technique (`techniques/1_ordinary/qi_coatings_leftovers.json`),
and use the `ascension:qi_coating` state group.

# On-hit and defence effects
A coating's look is only visual. What an element **does** in a fight goes in its element file, as up to three optional hooks. Each one runs normal skill actions, so a hit can apply a status effect, deal extra damage of a custom damage type, play a sound, spawn particles, and so on.

| Hook | Fires when | The actions' target |
|---|---|---|
| `body_on_hit` | Qi Coating is on and the player lands a **bare-handed** melee hit | The entity hit |
| `body_on_hurt` | Qi Coating is on and the player **takes damage from an attacker** (melee, projectile or skill) | The attacker |
| `external_on_hit` | External Qi Coating is on and the player lands a melee hit with a weapon it shows on (swords, blades, axes, spears, including a spear charge) | The entity hit |

A hook only fires for the element the coating is showing right now. Hits with anything else in hand (a pickaxe, a stick), projectiles the player fires and damage from their skills never fire the on-hit hooks. Damage a hook deals never sets off another hook, so two coated players can't loop each other.

```json
{
  "name": { "translate": "ascension.qi_coating.element.fire.name" },
  "path": "ascension:elemental/fire",
  "body_visual": "ascension:qi_coating/fire_qi_coating",
  "external_visual": "ascension:qi_coating/fire_external_qi_coating",
  "order": 1,
  "body_on_hit": {
    "actions": [
      { "type": "ascension:effect", "definition": "ascension:burning", "duration": 60, "subject": "target" }
    ]
  },
  "body_on_hurt": {
    "cooldown": 40,
    "actions": [
      { "type": "ascension:effect", "definition": "ascension:burning", "duration": 40, "subject": "target" }
    ]
  },
  "external_on_hit": {
    "cooldown": 20,
    "cost": { "resource": "ascension:qi", "amount": 2.0 },
    "actions": [
      { "type": "ascension:damage", "base": 3.0, "damage_type": "example:fire_qi", "classifications": ["ascension:skill"], "subject": "target" }
    ]
  }
}
```

Every hook takes the same fields:

| Field | Default | Meaning |
|---|---|---|
| `actions` | Required | Skill actions, run with the entity hit (or the attacker) as the target. See [Actions and definitions](ASCENSION_SKILLS.md#actions-and-definitions) and [Damage](ASCENSION_SKILLS.md#damage) |
| `cooldown` | `0` | Ticks before the hook can fire again (20 = 1 second). `0` fires on **every hit**. Each hook has its own cooldown per player |
| `cost` | None | Paid each time it fires, in the skill cost format (`{ "resource": "ascension:qi", "amount": 2.0 }` or a list of them). If the player can't pay, the hook doesn't fire and nothing is spent |
| `conditions` | `[]` | Skill conditions that must all pass, see [Runtime conditions](ASCENSION_SKILLS.md#runtime-conditions) |

The actions run as the coating's passive skill (Qi Coating or External Qi Coating), so mastery and scaled values resolve against it. The damage of the hit that set the hook off is available as the variable `ascension:trigger/damage`, for example to make a hook deal a share of it back (see [Scaled values](ASCENSION_SKILLS.md#scaled-values)).

**Custom damage types.** `damage_type` takes any damage type id. A new one is a vanilla damage type file, `data/<namespace>/damage_type/<name>.json`; give the action `classifications` so defences that filter by classification treat it as intended.

**Standalone coating skills** (the whole-body layers and one-offs) don't use elements. Give their passive skill `triggers` instead, with `"event": "damage_dealt"` or `"damage_taken"` (see [Passive skills](ASCENSION_SKILLS.md#passive-skills)); triggers only fire while the passive, and so the coating, is on.

# How a coating reaches the player
1. A **castable skill** on the hotbar runs `ascension:toggle_passive` on a passive.
2. The **passive skill** owns the upkeep (qi per second), its state group (so only one coating per group is on at a time) and, in `on_enable`, either an `ascension:qi_coating_visual` (the Qi Coating skills, which look up the picked element's runtime visual) or an `ascension:persistent_visual` naming a runtime visual directly (standalone coating skills).
3. The **runtime visual** (`skill_system/visuals/...`) has one `ascension:custom` element whose `visual` is the **coating's id**.
4. The **coating** (`skill_system/qi_coatings/...`) is synced to every client. The client renders whatever coating a player's running runtime visuals name (`QiCoatingClientState`), and the server checks the same chain for coded effects such as the Lightning Step spear flash (`QiCoatingService`).

Coatings that grow in (`grow_millis` above 0) also shrink away in reverse when they are turned off.

# Files and IDs
Coating files live in:

```text
data/<namespace>/ascension/skill_system/qi_coatings/...
```

For example:

```text
data/ascension/ascension/skill_system/qi_coatings/ice_qi_coating.json
```

has the id `ascension:ice_qi_coating`, which is what the runtime visual names:

```json
{
  "elements": [
    {
      "type": "ascension:custom",
      "visual": "ascension:ice_qi_coating"
    }
  ]
}
```

Coatings are a synced datapack registry, so `/reload` updates them for everyone (clients clear their cached lookups when the datapacks are resent).

# Minimal coatings
A hand coating drawn on boxes round the forearms:

```json
{
  "coverage": "hands",
  "shells": {
    "look": "ascension:space_starfield",
    "layers": [
      { "inflate": 0.5, "reach": 7.6, "overhang": 0.5, "square_cap": true }
    ]
  }
}
```

A coating painted into the player's own skin, growing in from the fingertips:

```json
{
  "coverage": "hands",
  "grow_millis": 900,
  "skin": { "look": "ascension:ice" }
}
```

A whole-body coating, one box round each limb:

```json
{
  "coverage": "body",
  "shells": {
    "look": "ascension:water_serpent",
    "layers": [
      { "inflate": 0.4, "reach": 12.4, "overhang": 0.4, "square_cap": true }
    ],
    "body": { "mode": "limbs" }
  }
}
```

Sword qi:

```json
{
  "coverage": "weapon",
  "grow_millis": 1200,
  "sword": { "look": "ascension:heat" }
}
```

# Root fields
| Field | Default | Meaning |
|---|---|---|
| `coverage` | Required | `hands`, `body`, `whole_skin` or `weapon` (see below) |
| `shells` | None | Boxes round the limbs, shaded by a shell look |
| `skin` | None | The coating painted into the player's skin by a skin look |
| `sword` | None | The held weapon redrawn by a sword look. Required for `weapon`, not allowed otherwise |
| `grow_millis` | `0` | Time to grow in when turned on and to shrink away when turned off |
| `fades_arm` | `false` | The arm itself fades from opaque at the shoulder to mostly see-through at the hand |
| `echoes` | `false` | Flickering violet copies of the arm (body, or weapon) are drawn beside it |
| `reacts_to` | `[]` | What the coating reacts to: `hurt` (the player is hurt; both arms) and `attack` (the player swings; that arm). Looks with a hit animation play it for 2s after each, and an echoing coating echoes for 0.7s after them instead of all the time.<br/> Between reactions the copies are hidden and the shells or sword draw their `quiet_look` instead of `look`, if they have one |
| `overlay` | None | Another coating's id whose shells are drawn on top of this one wherever this one is drawn |
| `hand_particles` | `[]` | Particles spawned at the hands every tick |
| `effects` | `[]` | Named coded effects, see [Effects](#effects) |

A coating that isn't `weapon` needs `shells`, `skin` or both.

| `coverage` | Third person | First person |
|---|---|---|
| `hands` | Both arms (shell `layers`, skin on the arms) | The arm on screen |
| `body` | Shells as `shells.body` says; skin on the arms and legs | The arm on screen (shell `layers`, skin) |
| `whole_skin` | Like `body`, but the skin look also covers the head and torso, pouring on part by part (legs, then torso, then head; arms meanwhile) | Like `body` |
| `weapon` | The held weapon, if it is bladed or a polearm | The held weapon |

# Shells
```json
{
  "shells": {
    "look": "ascension:space_starfield",
    "layers": [
      { "inflate": 0.95, "reach": 7.6, "overhang": 0.8, "color": "#A0FFFFFF" },
      { "inflate": 0.35, "reach": 6.0, "overhang": 0.3, "color": "#D8FFFFFF" }
    ],
    "body": { "mode": "film" }
  }
}
```

| Field | Default | Meaning |
|---|---|---|
| `look` | One of the two | A shell look id, see [Built-in looks](#built-in-looks) |
| `quiet_look` | None | Shell look drawn instead of `look` between reactions (`reacts_to`) |
| `texture` | One of the two | An animated PNG painted on the boxes instead of a look, see [Texture shells](#texture-shells) |
| `layers` | `[]` | Boxes round each arm, outermost first so the inner ones draw over it. A `hands` coating draws these; a body coating draws them on the arm in first person |
| `body` | None | How a body coating covers the body in third person. Without it a body coating's shells only show in first person |

**Layer** (pixels; an arm is 12 long and its hand tip is at 0):

| Field | Default | Meaning |
|---|---|---|
| `inflate` | Required | How far the box sits out from the arm on each side |
| `reach` | Required | How far up the arm it goes from the hand tip. `12` is the shoulder; `12 + inflate` also covers the top edge |
| `overhang` | `0` | How far past the hand tip it goes |
| `color` | `#FFFFFFFF` | ARGB tint, `#AARRGGBB` or `#RRGGBB`. The alpha sets how see-through the whole box is |
| `square_cap` | `false` | Maps the fist cap so a square pixel grid on the sides stays square on it (use it for blocky looks) |
| `outer` | `false` | Texture shells only: paint this box from the skin's outer layer (sleeve) area instead of the inner one |

**Body**:

| Field | Default | Meaning |
|---|---|---|
| `mode` | Required | `film`: one box round every body part. `limbs`: one box round each arm and leg with the look's per-limb pattern (use it with looks marked per-limb below) |
| `inflate` | `0.4` | How far the boxes sit out from the body |
| `limb_reach` | Whole limb | `film` only: how far up each limb the boxes go from the hand or foot, with open tops |
| `torso_and_head` | `true` | `film` only: whether the head and torso get a box too |
| `legs_only` | `false` | `limbs` only: only the legs get boxes |
| `outer` | `false` | Texture shells only: paint the boxes from the skin's outer layer areas |
| `color` | `#FFFFFFFF` | Texture shells only: ARGB tint multiplied into the texture; its alpha sets how see-through the boxes are |

# Texture shells
Instead of a shader `look`, shells can be painted with a hand-drawn, frame-by-frame PNG. This needs no code: the coating JSON and the image are all there is.

```json
{
  "coverage": "hands",
  "grow_millis": 900,
  "shells": {
    "texture": {
      "path": "ascension:textures/qi_coating/fire_qi_coating.png",
      "frames": 6,
      "frame_millis": 250
    },
    "layers": [
      { "inflate": 0.6, "reach": 12.6 }
    ]
  }
}
```

| Field | Default | Meaning |
|---|---|---|
| `path` | Required | The image, as a full texture path (`<namespace>:textures/...png`), under `assets/<namespace>/textures/` |
| `frames` | `1` | How many frames are stacked in the image |
| `frame_millis` | `250` | How long each frame shows. `250` is 4 changes a second |
| `emissive` | `true` | Glows at full brightness like the shader looks. `false` lights it like the skin (darker at night, shaded per face) |

**Painting the image.** Each frame is laid out exactly like a **64x64 player skin**, so it can be painted in any skin editor (Blockbench, a skin editor website) on a blank skin. Each box takes its pixels from the skin area of the body part it covers: the right arm's box from the right arm's area, the head's from the head's, and so on. Paint the inner layer areas; a layer with `"outer": true` (or a body with `"outer": true`) uses the outer layer (sleeve, jacket, pants, hat) areas instead, so one image can hold two different layers. Paint arms in the **wide (4 pixel) layout**: slim arms read the same wide areas, with their 3 pixel wide faces (front, back, hand end and shoulder end) squeezed from the 4 pixel ones, so one image fits both.

Stack the frames top to bottom in one image: 4 frames of 64x64 make a 64x256 image, with frame 0 at the top. Higher resolutions work too, as long as every frame is square (128x128 frames make a 128x512 image for 4 frames). Transparent pixels show the player through; part-transparent pixels blend.

**How the boxes are shaped.** A box is the player model's part blown up by `inflate`. A layer's `reach` cuts the arm's box off that far from the hand (`12` is the shoulder, so `12 + inflate` keeps the whole box), and the texture is cut with it: a forearm-high coating shows the hand end of the arm's area. The shoulder end's face is left off once the box is cut. `overhang` stretches the hand end out further. A body coating's `film` boxes cover every part (each limb only up to `limb_reach`, if set); `limbs` boxes cover the arms and legs.

**Growing.** With `grow_millis`, the boxes grow out from the hands and feet (the torso from the waist, the head from the neck) when the coating comes on, and shrink back when it goes off. Layer `color` tints the texture, and its alpha fades the whole box. `echoes` and `overlay` work as with shader looks; `square_cap` does nothing here.

To use a texture coating, add its runtime visual and point a Qi Coating element at it, or give it its own skill ([Adding a new coating](#adding-a-new-coating-datapack-only)). The Fire element's hand coating is one: its files are named `fire_qi_coating`, and its image (`assets/ascension/textures/qi_coating/`) is 6 frames of 128x128, so each skin pixel is 2x2 texels.

# Skin
```json
{ "skin": { "look": "ascension:metal" } }
```

The limbs are redrawn from the player's own skin, a hair off its surface, and the look paints into those pixels (`qi_coating_skin.fsh`). Skin looks that grow (ice, metal) spread up from the hands and feet when `grow_millis` is set.

# Sword
```json
{ "sword": { "look": "ascension:frost" } }
```

| Field | Default | Meaning |
|---|---|---|
| `look` | Required | A sword look id, see [Built-in looks](#built-in-looks) |
| `quiet_look` | None | Sword look drawn between reactions (`reacts_to`), see below |

The held weapon is redrawn by the look (`sword_qi.fsh`) in place of vanilla's, only on the pixels its hand-painted mask marks (`assets/ascension/textures/sword_qi_mask/`, listed in `assets/ascension/sword_qi_masks.json`). With `"echoes": true` the weapon is drawn as normal and see-through copies of it flicker beside it instead; with a `quiet_look` as well, the quiet look is drawn in place of the plain weapon, alone between reactions (`reacts_to`) and under the copies while echoing.

# Hand particles
```json
{
  "hand_particles": [
    { "particle": { "type": "minecraft:dripping_water" }, "chance": 0.06, "spread": 0.08, "offset_y": -0.05 },
    { "particle": { "type": "minecraft:portal" }, "count": 2, "converge": 0.45 }
  ]
}
```

| Field | Default | Meaning |
|---|---|---|
| `particle` | Required | Any particle, in vanilla's particle format |
| `chance` | `1.0` | Chance per tick, per hand, of spawning |
| `count` | `1` | How many spawn each time |
| `spread` | `0.0` | How far from the hand (blocks, either way across) each may start |
| `offset_y` | `0.0` | How far above the hand they start |
| `velocity` | `[0, 0, 0]` | Starting velocity |
| `converge` | `0.0` | Above 0: each starts at the hand with a velocity this long in a random direction instead. For particles like `minecraft:portal`, which travel from their start plus velocity back to their start, this makes them stream into the hand |

# Effects
Behaviour that is coded in Java rather than described by data (`QiCoatingEffects`). Unknown ids are ignored.

| Effect | What it does |
|---|---|
| `ascension:lightning_step` | The player's own model is drawn see-through (the arm too, in first person), crackling footprints are left where they walk or land, and holding right click with a spear spends 3 qi to flash forward, leaving a lightning trail |
| `ascension:serpent_splashes` | Spray bursts at the top of each limb as its water serpent thins away there. Timed with the `water_serpent` shell looks; on the arms for a `hands` coating, the arms and legs for a body one |
| `ascension:wind_rings` | A box of wind rings round each forearm, their streaks in time with the `ascension:wind` skin look |

# Built-in looks
Looks are registered in `QiCoatingLooks` (client). A coating that names an unknown look logs one warning and that part isn't drawn.

**Shell looks** (`qi_coating.fsh`; per-limb looks give each arm and leg its own pattern and write depth so sleeves don't show over them):

| Look | What it looks like | Per-limb |
|---|---|---|
| `ascension:pure_qi` | An even pale cyan film round the hand, softening away up the forearm | |
| `ascension:space_starfield` | A window into starry space | |
| `ascension:time_echo` | A thin violet film whose bands tear sideways (use it with `echoes`) | |
| `ascension:time_echo_quiet` | The same violet film held still, for `quiet_look` | |
| `ascension:space_starfield_layer` | Starfield at partial strength (for `film` bodies) | |
| `ascension:lightning_crackle` | Pixel bolts racing up the arms | Yes (arms) |
| `ascension:lightning_crackle_limbs` | Pixel bolts racing up every limb, crackling round the feet | Yes |
| `ascension:lightning_step` | Strong bolts round the feet only (doesn't write depth, so a see-through body shows behind it) | Yes |
| `ascension:water_serpent_arms` | A pool of water round the hand with a water serpent winding up the arm | Yes (arms) |
| `ascension:water_serpent` | The same on every limb | Yes |

**Skin looks** (`qi_coating_skin.fsh`):

| Look | What it looks like |
|---|---|
| `ascension:ice` | Ice freezing over from the hand or foot; hits crack it and break a piece off, which refreezes |
| `ascension:metal` | Molten gold pouring on and cooling, tinted into the skin; hits spark (hands) or ring (body) |
| `ascension:wind` | Light green wind streaks coiling round the arm in gusts (pair it with `ascension:wind_rings`) |

**Sword looks** (`sword_qi.fsh`):

| Look | What it looks like | Grows |
|---|---|---|
| `ascension:pure_qi` | A film of plain qi tinting the blade pale cyan | |
| `ascension:starfield` | The blade becomes a window into space | |
| `ascension:time_echo` | A violet ghost of the blade whose bands tear brighter (use it with `echoes`) | |
| `ascension:time_echo_quiet` | A still violet film over the blade, for `quiet_look` | |
| `ascension:cracked` | Stone plates | |
| `ascension:water` | Water flowing toward the tip | |
| `ascension:arcs` | Pixel bolts | |
| `ascension:frost` | Ice climbing from the hilt; hits break pieces off | Yes |
| `ascension:gale` | Wind blades spiralling up the blade | |
| `ascension:molten` | Molten gold pouring up from the hilt, cooling to gold | Yes |
| `ascension:heat` | Heat spreading from the hilt, red to white-hot at the tip | Yes |

"Grows" looks take how far the coating has spread; give their coatings a `grow_millis` (the built-in ones use 1200). Hit animations (the `ice` and `metal` skin looks, and the `frost`, `molten` and `heat` sword looks) only play for what the coating lists in `reacts_to`.

**Shelved looks.** The whole-body Echo layer (`void_echo_layer`, style 10) is commented out in `QiCoatingLooks`; its coating, runtime visual and skill JSON are parked in `src/main/resources-disabled/qi_coatings/`, which the game doesn't load.

# Adding a new coating (datapack only)
Everything below is data; no code or restart needed beyond `/reload` (lang needs a resource reload).

To try coatings in game, learn the **Qi Coatings Dev** technique (`ascension:1_ordinary/qi_coatings_dev`: Qi Coating, External Qi Coating and every qi layer) and take the **Qi Coatings Dev Bloodline** (`ascension:1_ordinary/qi_coatings_dev_bloodline`, in the bloodline creative tab), which holds every element's path so the coating wheel offers them all.

**As a Qi Coating element** (the usual way). Write the coating and its runtime visual (steps 1 and 2 below), then either point an existing element's `body_visual` / `external_visual` at the runtime visual, or add a new element file in `skill_system/qi_coating_elements/` naming the path that unlocks it. Give the element's `name` a lang key. Nothing else is needed: the Qi Coating skills and the coating wheel pick it up.

**As its own skill** (whole-body layers and one-offs). Example: a frost hand coating called "Glacier Fist".

1. **The coating** `data/example/ascension/skill_system/qi_coatings/glacier_fist.json`:
   ```json
   {
     "coverage": "hands",
     "grow_millis": 1200,
     "reacts_to": ["hurt", "attack"],
     "skin": { "look": "ascension:ice" },
     "hand_particles": [
       { "particle": { "type": "minecraft:snowflake" }, "chance": 0.1, "spread": 0.1 }
     ]
   }
   ```
2. **The runtime visual** `data/example/ascension/skill_system/visuals/qi_coating/glacier_fist.json`:
   ```json
   { "elements": [ { "type": "ascension:custom", "visual": "example:glacier_fist" } ] }
   ```
3. **The passive** `data/example/ascension/skills/passive/glacier_fist.json` (copy any existing `passive/qi_coatings_leftovers/` file):
   ```json
   {
     "type": "ascension:passive",
     "name": { "translate": "example.skill.glacier_fist.name" },
     "description": { "translate": "example.skill.glacier_fist.desc" },
     "toggleable": true,
     "enabled_by_default": false,
     "state_group": "ascension:qi_coating",
     "upkeep": { "resource": "ascension:qi", "amount": 1.0, "interval": 20 },
     "on_enable": [
       { "type": "ascension:persistent_visual", "key": "ascension:qi_coating", "visual": "example:qi_coating/glacier_fist" }
     ]
   }
   ```
   Keep `state_group` and `key` as `ascension:qi_coating` so it replaces whatever coating was on before.
4. **The castable skill** `data/example/ascension/skills/castable/glacier_fist.json`, which toggles the passive:
   ```json
   {
     "type": "ascension:active",
     "name": { "translate": "example.skill.glacier_fist.name" },
     "description": { "translate": "example.skill.glacier_fist.desc" },
     "cast": { "mode": "instant" },
     "cooldown": { "base": 0.5 },
     "require_targets": false,
     "actions": [ { "type": "ascension:toggle_passive", "skill": "example:passive/glacier_fist" } ]
   }
   ```
5. **Grant both** from a technique's `on_added`:
   ```json
   { "type": "ascension:grant_skills", "skills": [ "example:castable/glacier_fist", "example:passive/glacier_fist" ] }
   ```
6. **Hide the passive** from the passive menu by adding it to `data/ascension/tags/ascension/skills/hidden.json` (the hotbar skill is the way to toggle it).
7. **Name it**: lang keys for the name and description. Inside this mod, add them to `AscLangProvider` and run `runData`.

To change an existing coating, edit its file under `skill_system/qi_coatings/` and `/reload`.

# Adding a new look (code)
A look is shader code, so it needs Java and a client restart. If the look can be drawn as a few hand-painted frames, a [texture shell](#texture-shells) needs no code at all. Otherwise, pick the kind:

**Shell look**
1. Write a `vec4 myLook(float u, float v, float t)` in `assets/ascension/shaders/core/qi_coating.fsh` (u runs round the box, v from the hand end, t in seconds).
2. Pick an unused style number (in use: 5, 9-11, 14-16, 27, 28, 32, 41-58) and add it to the dispatch in `main`. Per-limb looks need one style per limb.
3. Register it in `QiCoatingLooks`: `registerShell(id("my_look"), new ShellLook(new int[]{style}, perLimb, stone, Render.NORMAL))`.

**Skin look**
1. Add an `#ifdef MY_LOOK` block to `qi_coating_skin.fsh` (the metal block is the template: a function painting the skin, and a block in `main`).
2. Add a pipeline in `ModRenderPipelines`: `skinPipeline("qi_coating_skin_my_look", "MY_LOOK")`, and register it.
3. Register it in `QiCoatingLooks`: `registerSkin(id("my_look"), () -> ModRenderPipelines.QI_COATING_SKIN_MY_LOOK)`.

The vertex colour each skin look receives: red's top bit is 0 for a hand coating and 1 for a body one (its low 7 bits are the last hit's seed), green is how far it has grown, blue how far up the limb, alpha how long ago the last hit was.

**Sword look**
1. Add a `#elif STYLE == N` block to `sword_qi.fsh` and raise `ModRenderPipelines.SWORD_QI`'s size to cover N.
2. Register it in `QiCoatingLooks`: `registerSword(id("my_look"), new SwordLook(N, SwordData.PLAIN))`. Use `SPREAD` if it grows from the hilt and reacts to hits, `GALE` if it needs to know where across the blade each pixel is.

**Effect**
Add an id to `QiCoatingEffects` and check `coating.hasEffect(...)` where it should act: `QiCoatingLayer` for client visuals, `QiCoatingService` for server behaviour.

Other mods can call the `QiCoatingLooks.register*` methods during client setup to add looks without touching these files.

# Values that must match
Some numbers are shared between shaders and Java; change them together.

| Value | Where |
|---|---|
| Hit reaction length | `QiCoatingLayer.IMPACT_MILLIS` and `IMPACT_SECONDS` in `include/qi_impact.glsl` |
| Gold tint and molten colours | `qi_coating_skin.fsh` (METAL) and `sword_qi.fsh` (STYLE 7) |
| Serpent timing | `SERPENT_*` in `qi_coating.fsh` and `QiCoatingLayer` (for `ascension:serpent_splashes`) |
| Wind ring style | `QiCoatingLayer.WIND_RING_STYLE` and style 32 in `qi_coating.fsh` |
