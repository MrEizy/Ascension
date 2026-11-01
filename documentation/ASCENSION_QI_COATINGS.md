# Ascension Qi Coatings
This page documents Qi Coatings: the looks a cultivator's qi takes on their hands, body or weapon (frost creeping over the fists, molten gold, qi coating on the blade.), how they are built from datapack files, and how to add new ones.

The short version: **a coating is data, a look is code.** A coating JSON says where the coating goes and which built-in looks draw it; the looks themselves are shader code you pick by name.

The chain from a hotbar skill to pixels on screen is:

**castable skill -> passive skill -> (element) -> qi coating -> looks**

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
- [Gather](#gather)
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
  "body_coating": "ascension:fire_qi_coating",
  "external_coating": "ascension:fire_external_qi_coating",
  "order": 1
}
```

`body_coating` and `external_coating` are coating ids (files in `skill_system/qi_coatings/`). An element without one isn't offered on that skill. `path` is optional: an element without it is open to everyone. 
The built-in elements are Pure Qi (`pure`, no path, first in the wheel, so it is what a new player starts with), Fire, Earth, Water, Ice, Metal, Wind, Lightning, Space and Time. Time's hand coating is a still violet film that glitches and throws flickering copies of the arm for a moment when the player is hurt (`"reacts_to": ["hurt"]`), and its weapon coating a still violet tint that does the same when they swing (`"reacts_to": ["attack"]`). Pure Qi draws with the `pure_qi` shell look (style 57, an even pale cyan film round the hand, softening away up the forearm) and the `pure_qi` sword look (sword style 9, the blade tinted pale cyan).

The pick is stored per player (`CoreAttachments.QI_COATING_SELECTION`, kept through death and relogging). Picking a different element while the coating is on switches it: the old coating comes off and shrinks away (`grow_millis`), and only then does the new one go on (`QiCoatingSelectionService`). If the player loses the path for the element they're showing, it falls back to the first element they still have, or turns off if there's none left.

The skills' passives use two pieces built for this:
- `ascension:qi_coating_visual` (action, `"channel": "body" | "external"`) puts the picked element's coating on in `on_enable`. It comes off when the passive goes off, and back on when the player logs in or respawns.
- `ascension:has_qi_coating_element` (condition, same `channel`) is an `enable_conditions` entry, so the coating can't be turned on with no element to show.

Whole-body layers and Lightning Step are standalone coating skills, held by the **Qi Coatings Leftovers** test technique (`techniques/1_ordinary/qi_coatings_leftovers.json`),
and use the `ascension:qi_coating` state group.

# On-hit and defence effects
A coating's look is only visual. What an element **does** in a fight goes in its element file, as up to three optional hooks. Each one runs normal skill actions, so a hit can apply a status effect, deal extra damage of a custom damage type, play a sound, spawn particles, and so on.

| Hook | Fires when | The actions' target |
|---|---|---|
| `body_on_hit` | Qi Coating is on and the player lands a **bare-handed** melee hit | The entity hit |
| `body_on_hurt` | Qi Coating is on and the player **takes damage from an attacker** (melee, projectile or skill) | The attacker |
| `external_on_hit` | External Qi Coating is on and the player lands a melee hit with a tool it shows on (swords, blades, axes, spears, including a spear charge, and pickaxes, shovels and hoes) | The entity hit |

A hook only fires for the element the coating is showing right now. Hits with anything else in hand (a stick), projectiles the player fires and damage from their skills never fire the on-hit hooks. Damage a hook deals never sets off another hook, so two coated players can't loop each other.

```json
{
  "name": { "translate": "ascension.qi_coating.element.fire.name" },
  "path": "ascension:elemental/fire",
  "body_coating": "ascension:fire_qi_coating",
  "external_coating": "ascension:fire_external_qi_coating",
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

# Mining and tool effects
An element can also change what a tool External Qi Coating shows on does to blocks (`QiCoatingMining`): `external_on_mine` when a block is mined with it, and `external_on_use` when it's used (right-clicked) on one. Bare hands are never changed.

**`external_on_mine`**

| Field | Default | Meaning |
|---|---|---|
| `cost` | None | Paid for each block whose drops it changes, in the skill cost format. If the player can't pay, the drops are left alone |
| `smelt` | `false` | Drops are swapped for what a furnace makes of them (raw iron to an iron ingot, sand to glass, logs to charcoal). No furnace experience: only what the block drops itself (coal, lapis, redstone ores and so on). Works after Fortune |
| `convert` | `{}` | Item to item: the mined block's own drop (the block as an item) becomes the item on the right, count kept. Other drops are left alone, so stone's cobblestone isn't converted as cobblestone. A value can also be `{ "item": ..., "cost": ... }` to charge that cost for this block in place of the element's (Time's anvils cost 250) |
| `to_inventory` | `false` | Drops go straight into the player's inventory; what doesn't fit falls as usual |
| `speed` | None | One entry or a list of `{ "blocks": <block, list or #tag>, "multiplier": 1.5, "tools": <item, list or #tag> }`: faster mining on those blocks, with any coated tool or only `tools`. A big multiplier breaks soft blocks instantly (50 on leaves). Costs nothing beyond the coating's upkeep |
| `fortune` | `0` | Drops are rolled as if the tool had this many more levels of Fortune, on top of its own (Fortune III and 2 here roll as Fortune V). Free |
| `silk_touch` | None | A block, list or #tag: these drop as if the tool had Silk Touch, and ice among them doesn't melt into water. Free |
| `airborne` | `false` | Mining off the ground is as fast as on it (no fivefold slowdown) |
| `underwater` | `false` | Mining with the head under water is as fast as out of it, as with Aqua Affinity |

**`external_on_use`** (paid once a use)

| Field | Default | Meaning |
|---|---|---|
| `cost` | None | Paid each time it does something; nothing happens if the player can't pay |
| `till` | None | A coated hoe tilling a block also tills round it (an area, below) |
| `flatten` | None | A coated shovel making a path also makes it round the block (an area, below) |
| `bonemeal` | `false` | A coated hoe used on something bone meal grows grows it as bone meal does. Blocks the hoe would till (grass, dirt) are tilled instead |
| `transform` | `{}` | Block to block: any coated tool right-clicked on the block on the left turns it into the one on the right. A fluid counts only at its source, and is picked through the click (water in front of a block). A value can also be `{ "block": ..., "cost": ... }` to charge that cost in place of the element's |

An area (`till`, `flatten`) is a radius, or `{ "radius": ..., "charge_ticks": 0, "cost": ... }`. It reaches a round patch `radius` out (1 is 3x3, part radii still add blocks), on the same level, where the tool could change a block (air above, where the player may build); capped at 8. `radius` is a number or a scaled value that can read `ascension:charge` (0 to 1, how far a charged use got) and the player's affinity. With `charge_ticks` 0 the blocks change on the click; above 0, holding the tool on the block charges instead (a rising note each time the patch grows) and they change on release, at full charge after that many ticks (a tap is charge 0). `cost` is paid in place of the use's; either can read how many blocks it changes as `{ "type": "ascension:context", "key": "ascension:qi_coating/blocks" }`.

**`external_on_kill`** (paid once a kill): `cost`, and `to_inventory` (the drops of something killed in melee with a coated weapon go straight into the player's inventory).

The costs of `external_on_mine` and `external_on_kill` can read how many items the drops hold, as `{ "type": "ascension:context", "key": "ascension:qi_coating/items" }`, to charge per item.

In use: Fire smelts, Water turns concrete powder into concrete, Space sends drops to the inventory, Earth mines `#ascension:earth_qi_coating_mineable` (stone, dirt, sand, gravel, clay, terracotta) twice as fast and tills with a hoe and makes paths with a shovel (3x3 on a tap, held for 2 seconds out to radius 4, times 1 + 10 x earth affinity, so 0.01 affinity is 10% further), Life turns cobblestone and stone bricks (and their slabs, stairs and walls) mossy, breaks leaves instantly and logs twice as fast with an axe, and bone-meals with a hoe, Ice mines ice, packed ice and blue ice twice as fast and keeps them whole and freezes with a right-click (water source to ice, ice to packed ice, packed ice to blue ice), Metal mines as if with 2 more levels of Fortune, Wind mines at full speed in the air and Water under water, and Time rewinds worn blocks to fresh ones (weathered copper of every kind, waxed or not, to fresh copper; cracked bricks and tiles to whole; chipped and damaged anvils to new; dead coral blocks to living). Speed bonuses are free beyond the coating's upkeep. Fire's smelting costs 20 qi a block, Water's concrete 5, Life's mossy blocks 15 and Life's bone meal 10 a use; Space's pickup (mined blocks and kills) 1 qi an item; Earth's tilling and paths 2 a use plus 0.35 a block (about 5 for 3x3), and Time's rewinding 5 a block (250 for an anvil); Ice's freezing 5 (water), 10 (to packed ice) and 20 (to blue ice) a use; Metal's Fortune, Ice's pickup and the air and water mining are free.

```json
{
  "external_on_mine": {
    "cost": { "resource": "ascension:qi", "amount": 0.1 },
    "convert": { "minecraft:white_concrete_powder": "minecraft:white_concrete" },
    "speed": [ { "blocks": "#minecraft:leaves", "multiplier": 50.0, "tools": "#minecraft:axes" } ]
  },
  "external_on_use": { "cost": { "resource": "ascension:qi", "amount": 0.2 }, "till": 1, "bonemeal": true }
}
```

# How a coating reaches the player
1. A **castable skill** on the hotbar runs `ascension:toggle_passive` on a passive.
2. The **passive skill** owns the upkeep (qi per second), its state group (so only one coating per group is on at a time) and, in `on_enable`, either an `ascension:qi_coating_visual` (the Qi Coating skills: the picked element's `body_coating` or `external_coating`) or an `ascension:qi_coating` naming a coating directly (standalone coating skills, `{ "type": "ascension:qi_coating", "coating": "example:glacier_fist" }`; its channel follows from the coating's `coverage`).
3. The server keeps which coating each player has on each channel (`QiCoatingManager`) and sends it, with the tick it went on, to that player and everyone tracking them (`QiCoatingSyncPacket`): when it changes, when someone comes into range, and to the player again after a respawn or dimension change. Coatings that grow are timed from that tick, so everyone sees one grown the same amount, however late they arrive.
4. The **coating** (`skill_system/qi_coatings/...`) is synced to every client with the datapacks. The client draws whatever coating it was told each player has (`QiCoatingClientState`), and the server reads the same for coded effects such as the Lightning Step spear flash (`QiCoatingService`).

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

has the id `ascension:ice_qi_coating`, which is what an element's `body_coating` (or an `ascension:qi_coating` action) names.

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
    "look": "ascension:lightning_crackle_limbs",
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
| `echoes` | `false` | Flickering violet copies of the arm (body, or weapon) are drawn beside it |
| `reacts_to` | `[]` | What the coating reacts to: `hurt` (the player is hurt; both arms) and `attack` (the player swings; that arm). Looks with a hit animation play it for 2s after each, and an echoing coating echoes for 0.7s after them instead of all the time.<br/> Between reactions the copies are hidden and the shells or sword draw their `quiet_look` instead of `look`, if they have one |
| `effects` | `[]` | Named coded effects, see [Effects](#effects) |
| `gather` | None | How a `hands` coating forms as it grows: from specks drawn in from round the arm, see [Gather](#gather) |
| `foliage_millis` | `0` | For a look coloured by the biome's foliage (Life's vines): it comes on in the shade of the biome it was turned on in, then takes about this long to take on a new biome's shade (95% of the way). `0` follows the biome at once. |
| `foliage_locked` | `false` | For a foliage look: keeps the shade it came on in, whatever biome the player walks into, until it is turned on again. Life uses it |

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

**Body**:

| Field | Default | Meaning |
|---|---|---|
| `mode` | Required | `film`: one box round every body part. `limbs`: one box round each arm and leg with the look's per-limb pattern (use it with looks marked per-limb below) |
| `inflate` | `0.4` | How far the boxes sit out from the body |
| `legs_only` | `false` | `limbs` only: only the legs get boxes |
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

**Painting the image.** Each frame is laid out exactly like a **64x64 player skin**, so it can be painted in any skin editor (Blockbench, a skin editor website) on a blank skin. Each box takes its pixels from the skin area of the body part it covers: the right arm's box from the right arm's area, the head's from the head's, and so on. Paint the inner layer areas (the outer layer's sleeve, jacket, pants and hat areas aren't used). Paint arms in the **wide (4 pixel) layout**: slim arms read the same wide areas, with their 3 pixel wide faces (front, back, hand end and shoulder end) squeezed from the 4 pixel ones, so one image fits both.

Stack the frames top to bottom in one image: 4 frames of 64x64 make a 64x256 image, with frame 0 at the top. Higher resolutions work too, as long as every frame is square (128x128 frames make a 128x512 image for 4 frames). Transparent pixels show the player through; part-transparent pixels blend.

**How the boxes are shaped.** A box is the player model's part blown up by `inflate`. A layer's `reach` cuts the arm's box off that far from the hand (`12` is the shoulder, so `12 + inflate` keeps the whole box), and the texture is cut with it: a forearm-high coating shows the hand end of the arm's area. The shoulder end's face is left off once the box is cut. `overhang` stretches the hand end out further. A body coating's `film` boxes cover every part; `limbs` boxes cover the arms and legs (or just the legs).

**Growing.** With `grow_millis`, the boxes grow out from the hands and feet (the torso from the waist, the head from the neck) when the coating comes on, and shrink back when it goes off. Layer `color` tints the texture, and its alpha fades the whole box. `echoes` works as with shader looks; `square_cap` does nothing here.

To use a texture coating, point a Qi Coating element at it, or give it its own skill ([Adding a new coating](#adding-a-new-coating-datapack-only)). The Fire element's hand coating is one: its files are named `fire_qi_coating`, and its image (`assets/ascension/textures/qi_coating/`) is 6 frames of 128x128, so each skin pixel is 2x2 texels.

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

# Gather
Instead of growing out from the hand, the coating gathers from its surroundings: small glowing specks of the element appear a little way round each arm and are drawn into it, and the shells form from them. When the coating is turned off it plays backwards, the specks flying back out.
```json
{
  "coverage": "hands",
  "grow_millis": 1800,
  "gather": {
    "style": "patches",
    "colors": ["#624530", "#785438", "#8D6342", "#9C6D49", "#3D291A"]
  }
}
```

| Field | Default | Meaning |
|---|---|---|
| `style` | Required | `patches`: the shells and skin look show only round where specks have landed, each patch spreading pixel by pixel until they join (the skin look is drawn fully formed on the pixels revealed so far). `fade`: the shells and skin look fade in evenly while the specks arrive. `specks`: just the specks, the coating forming its own way (a skin look's spread from the hand, a sword look's spread up the blade). Wind rings fade in with `fade` or `patches` |
| `colors` | Required | The specks' colours (`#RRGGBB` or `#AARRGGBB`), one picked at random per speck |
| `landings` | `9` | How many places round each arm the specks land, the first on the hand's end (1 to 32) |
| `specks_per_landing` | `3` | How many specks fly in to each place (1 to 8) |
| `speck_size` | `0.18` | A speck's half size in pixels; each varies a little from it |
| `glow` | `0.1` | How strongly each speck glows in its own colour, `0` for none |

Needs `coverage` `hands` or `weapon` and `grow_millis` above 0; the whole gather takes `grow_millis`. On the hands it works with shell looks and texture shells alike, and with a `skin` look on its own (the specks then land up to 7 pixels up the forearm). Each speck is drawn as a cube in a darker shade of its colour with its colour on the middle of each face, so it reads clearly against the arm.

On a weapon the specks fly in from around the held item and land on its coated pixels. `fade` fades the sword look in for looks that blend over the item (`pure_qi`, `water`, `starfield`, `cracked`, `arcs`); `patches` shows the look only round where specks have landed, the rest of the item staying plain until they join. Sword looks with their own spread from the hilt (`frost`, `molten`, `heat`) keep it and only get the specks.

# Effects
Behaviour that is coded in Java rather than described by data (`QiCoatingEffects`). Unknown ids are ignored.

| Effect | What it does |
|---|---|
| `ascension:lightning_step` | The player's own model is drawn see-through (the arm too, in first person), crackling footprints are left where they walk or land, and holding right click with a spear spends 3 qi to flash forward, leaving a lightning trail |
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
| `ascension:life_vines` | Minecraft's vine texture wound round the hand, coloured by the biome's foliage and darkened by the world's light like real vines and forearm, a pulse of green-gold life light climbing it every few seconds; grows in from the fingertips itself | Yes (arms) |

**Skin looks** (`qi_coating_skin.fsh`):

| Look | What it looks like |
|---|---|
| `ascension:ice` | Ice freezing over from the hand or foot; hits crack it and break a piece off, which refreezes |
| `ascension:metal` | Molten gold pouring on and cooling, tinted into the skin; hits flash (hands) or ring (body) |
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
| `ascension:arcs` | The lightning hand coating's racing pixel bolts, climbing the blade from the hilt to the tip with forks and strobe flashes | |
| `ascension:frost` | Ice climbing from the hilt; hits break pieces off | Yes |
| `ascension:gale` | Wind blades spiralling up the blade | |
| `ascension:molten` | Molten gold pouring up from the hilt, cooling to gold | Yes |
| `ascension:heat` | Heat spreading from the hilt, red to white-hot at the tip | Yes |
| `ascension:life_vines` | Minecraft's vines growing up the blade from the hilt, coloured by the biome's foliage and the world's light, a pulse of green-gold life light climbing them every few seconds | Yes |

"Grows" looks take how far the coating has spread; give their coatings a `grow_millis` (the built-in ones use 1200). Hit animations (the `ice` and `metal` skin looks, and the `frost`, `molten` and `heat` sword looks) only play for what the coating lists in `reacts_to`.

**Shelved looks.** The whole-body Echo layer (`void_echo_layer`, style 10) was removed from `qi_coating.fsh` and `QiCoatingLooks` (restore it from git history); its coating, old runtime visual wrapper and skill JSON are parked in `src/main/resources-disabled/qi_coatings/`, which the game doesn't load.

# Adding a new coating (datapack only)
Everything below is data; no code or restart needed beyond `/reload` (lang needs a resource reload).

To try coatings in game, learn the **Qi Coatings Dev** technique (`ascension:1_ordinary/qi_coatings_dev`: Qi Coating, External Qi Coating and every qi layer) and take the **Qi Coatings Dev Bloodline** (`ascension:1_ordinary/qi_coatings_dev_bloodline`, in the bloodline creative tab), which holds every element's path so the coating wheel offers them all.

**As a Qi Coating element** (the usual way). Write the coating (step 1 below), then either point an existing element's `body_coating` / `external_coating` at it, or add a new element file in `skill_system/qi_coating_elements/` naming the path that unlocks it. Give the element's `name` a lang key. Nothing else is needed: the Qi Coating skills and the coating wheel pick it up.

**As its own skill** (whole-body layers and one-offs). Example: a frost hand coating called "Glacier Fist".

1. **The coating** `data/example/ascension/skill_system/qi_coatings/glacier_fist.json`:
   ```json
   {
     "coverage": "hands",
     "grow_millis": 1200,
     "reacts_to": ["hurt", "attack"],
     "skin": { "look": "ascension:ice" }
   }
   ```
2. **The passive** `data/example/ascension/skills/passive/glacier_fist.json` (copy any existing `passive/qi_coatings_leftovers/` file):
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
       { "type": "ascension:qi_coating", "coating": "example:glacier_fist" }
     ]
   }
   ```
   Keep `state_group` as `ascension:qi_coating` so it replaces whatever coating was on before.
3. **The castable skill** `data/example/ascension/skills/castable/glacier_fist.json`, which toggles the passive:
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
4. **Grant both** from a technique's `on_added`:
   ```json
   { "type": "ascension:grant_skills", "skills": [ "example:castable/glacier_fist", "example:passive/glacier_fist" ] }
   ```
5. **Hide the passive** from the passive menu by adding it to `data/ascension/tags/ascension/skills/hidden.json` (the hotbar skill is the way to toggle it).
6. **Name it**: lang keys for the name and description. Inside this mod, add them to `AscLangProvider` and run `runData`.

To change an existing coating, edit its file under `skill_system/qi_coatings/` and `/reload`.

# Adding a new look (code)
A look is shader code, so it needs Java and a client restart. If the look can be drawn as a few hand-painted frames, a [texture shell](#texture-shells) needs no code at all. Otherwise, pick the kind:

**Shell look**
1. Write a `vec4 myLook(float u, float v, float t)` in `assets/ascension/shaders/core/qi_coating.fsh` (u runs round the box, v from the hand end, t in seconds).
2. Pick an unused style number (in use: 5, 9, 11, 14-16, 32, 45-61) and add it to the dispatch in `main`. Per-limb looks need one style per limb.
3. Register it in `QiCoatingLooks`: `registerShell(id("my_look"), new ShellLook(new int[]{style}, perLimb, Render.NORMAL))`. Two more flags can follow the render. `grows`: the look grows itself in from the hand (as Life's vines do), getting how far the coating has grown in the vertex alpha instead of an opacity (leave it out of the alpha multiply in `main`). `foliage`: the vertex colour is the biome's foliage colour under the world's light where the player stands, as vines and leaves are coloured (`LightmapColor` rebuilds vanilla's lightmap colour).

**Skin look**
1. Add an `#ifdef MY_LOOK` block to `qi_coating_skin.fsh` (the metal block is the template: a function painting the skin, and a block in `main`).
2. Add a pipeline in `ModRenderPipelines`: `skinPipeline("qi_coating_skin_my_look", "MY_LOOK")`, and register it.
3. Register it in `QiCoatingLooks`: `registerSkin(id("my_look"), () -> ModRenderPipelines.QI_COATING_SKIN_MY_LOOK)`.

The vertex colour each skin look receives: red's top bit is 0 for a hand coating and 1 for a body one (its low 7 bits are the last hit's seed), green is how far it has grown, blue how far up the limb, alpha how long ago the last hit was.

**Sword look**
1. Add a `#elif STYLE == N` block to `sword_qi.fsh` and raise `ModRenderPipelines.SWORD_QI`'s size to cover N.
2. Register it in `QiCoatingLooks`: `registerSword(id("my_look"), new SwordLook(N, SwordData.PLAIN))`. Use `SPREAD` if it grows from the hilt and reacts to hits, `GALE` if it needs to know where across the blade each pixel is.

**Effect**
Add an id to `QiCoatingEffects`. Its client side is a `QiCoatingEffect` (hooks for ticking each player, a see-through body, and drawing with the body or each arm; Lightning Step's `LightningStepVisuals` and the Tornado Fist's `WindRingsEffect` are examples), registered with `QiCoatingLayer.registerEffect(id, effect)`. Server behaviour checks `coating.hasEffect(...)` where it should act (`QiCoatingService`, with the coating from `QiCoatingManager.get`).

Other mods can call the `QiCoatingLooks.register*` methods during client setup to add looks without touching these files.

# Values that must match
Some numbers are shared between shaders and Java; change them together.

| Value | Where |
|---|---|
| Hit reaction length | `QiCoatingLayer.IMPACT_MILLIS` and `IMPACT_SECONDS` in `include/qi_impact.glsl` |
| Gold tint and molten colours | `qi_coating_skin.fsh` (METAL) and `sword_qi.fsh` (STYLE 7) |
| Wind ring style | `QiCoatingLayer.WIND_RING_STYLE` and style 32 in `qi_coating.fsh` |
