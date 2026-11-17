# Ascension Inner World Profiles

This page documents the datapack format used to create Ascension Inner World profiles, how a player's hidden world seed is built from them, and how to add new ones.

A profile is one datapack JSON per cultivation path. It holds several **variants** (themes). When a player first casts Inner World, only the profiles of the paths they actually cultivate are candidates, and their permanent seed picks 3-5 variants weighted by how far along each path is. Each variant decides what that region looks like: its biome, palette, terrain shape, features, ambient qi, ores and treasure, herbs, mobs and structures.

The intended workflow is:

**path -> profile JSON -> variants -> game**

Most normal content should require no Java. A variant can be as small as a palette.

## Quick links
- [Files and IDs](#files-and-ids)
- [Minimal profile](#minimal-profile)
- [Root fields](#root-fields)
- [How a seed is built](#how-a-seed-is-built)
- [Variants](#variants)
- [Palette](#palette)
- [Decor](#decor)
- [Terrain](#terrain)
- [Features](#features)
- [Qi](#qi)
- [Resources](#resources)
- [Mobs](#mobs)
- [Structures](#structures)
- [Regions and borders](#regions-and-borders)
- [Water, caves and structures](#water-caves-and-structures)
- [Complete example](#complete-example)
- [Compact reference](#compact-reference)

# Files and IDs
Profile files live in:

```text
data/<namespace>/ascension/inner_world_profile/...
```

For example:

```text
data/example/ascension/inner_world_profile/ember_dao.json
```

becomes the profile id:

```text
example:ember_dao
```

A profile's path must name a real path. When a player first casts Inner World, only profiles whose path the player actually cultivates are candidates, so one profile per path is the normal case. A profile with no variants, or an empty profile set, falls back to a built-in default world.

## Minimal profile
```json
{
  "path": "ascension:elemental/fire",
  "variants": [
    {
      "biome": "minecraft:nether_wastes",
      "palette": {
        "top": "minecraft:netherrack"
      },
      "terrain": {
        "amplitude": 26.0
      }
    }
  ]
}
```
Everything except path, variants, the variant's biome and palette.top has a sensible default.

## Root fields

| Field | Default | Meaning |
| --- | --- | --- |
| path | Required | The path this profile belongs to. Only players with this path can roll its variants |
| weight_multiplier | 1.0 | Scales this profile's whole share of the candidate pool relative to other profiles of the same path |
| variants | Required | The themes. Ship at least 3 so a player rolling this path gets variety |

## How a seed is built
At first cast the server snapshots the player's cultivation and rolls a permanent hidden seed, created once and never shown or changed.

Each held path gets a weight of (major realm + 1) + progress through the current major realm. Paths under 10% of the total are dropped, except the strongest path, which is always kept.

Every profile whose path matches a kept path is a candidate. Each candidate variant's weight is scaled by path weight / kept total * profile weight_multiplier, then by the variant's own weight.

The heaviest candidate becomes theme 0, which owns the core around the spawn dais. The seed then picks 3-5 themes total from the remaining candidates, weighted by the values above.

Randomness never adds a theme outside the player's paths. If the player has paths but no shipped profile matches, the built-in fallback world is used.

## Variants
A variant is one theme: a biome, a palette, a terrain shape, features, qi, resources, mobs and structures.

| Field | Default | Meaning |
| --- | --- | --- |
| weight | 1.0 | This variant's share of its profile's slice. Weights above 1.0 make it more likely to be rolled |
| biome | Required | The atmosphere biome applied to the chunk (sky, fog, grass/foliage tint, particles, ambience). Must be a real biome id |
| palette | Required | The surface, stone and deep blocks, plus decoration. See Palette |
| terrain | Default | Offset, amplitude, frequency and terrace. See Terrain |
| features | [] | Themed features: pools, springs, rivers, caves, ravines, spires, pillars, mesas, floating islands, trees. See Features |
| qi | Default | Ambient qi capacity, regen and affinity. See Qi |
| resources | Empty | Ores, treasure veins and wild herbs. See Resources |
| mobs | Empty | Realm range and spawn list. See Mobs |
| structures | [] | Vanilla and modded structures picked per grid cell. See Structures |

## Palette
```json
"palette": {
  "top": "minecraft:grass_block",
  "sub": "minecraft:dirt",
  "stone": "minecraft:stone",
  "deep": "minecraft:deepslate",
  "bands": [],
  "decor": []
}
```

| Field | Default | Meaning |
| --- | --- | --- |
| top | Required | The surface block of a normal column |
| sub | minecraft:dirt | The 2 blocks under the surface |
| stone | minecraft:stone | The stone layer |
| deep | minecraft:deepslate | The bottom 5 blocks of a column |
| bands | [] | If set, replaces the stone layer with a repeating vertical stripe, for badlands-style cliffs. Cycles by Y |
| decor | [] | Decorations placed on top of the surface. See Decor |
Palettes are the style layer: they may dither across a region border, so a grass block can meet podzol at a border without a hard line. Structural features (mesas, spires, pillars, floating islands) are decided at their own centre and always use one variant's palette, never a mix.

### Decor
A decoration is placed one block above the surface, chosen by a roll. Chances stack in list order, so the first entry whose accumulated chance is above the roll wins.

```json
"decor": [
  { "block": "minecraft:short_grass", "chance": 0.08 },
  { "block": "minecraft:dandelion", "chance": 0.02 }
]
```

| Field | Default | Meaning |
| --- | --- | --- |
| block | Required | The block to place |
| chance | Required | Absolute chance per column. 0.08 means about 8% of columns |
Decorations are skipped on the dais, in pools and shore bands, in carved columns, and on raised structures unless the structure builds its own bands.

## Terrain
```json
"terrain": {
  "offset": 0.0,
  "amplitude": 28.0,
  "frequency": 1.0,
  "terrace": 0
}
```

| Field | Default | Meaning |
| --- | --- | --- |
| offset | 0.0 | Raises or lowers the whole region, in blocks |
| amplitude | 28.0 | Hill height, in blocks |
| frequency | 1.0 | Feature size multiplier. Higher means smaller, busier hills |
| terrace | 0 | If above 0, heights snap to steps of this size, for plateaus |
Every column is a pure function of (seed, x, z). A region's height is blended with its nearest neighbour at the border, so biomes meet without a cliff. smooth mode drops the small bumpy octaves near water, so a shoreline meets the water level cleanly.

## Features
Features are per-variant list entries, each with a type.

### Pools and springs
```json
{
  "type": "pools",
  "block": "minecraft:water",
  "density": 0.35,
  "shore": "minecraft:sand"
}
```

| Field | Default | Meaning |
| --- | --- | --- |
| type | Required | pools or springs |
| block | minecraft:water, or liquified spiritual qi for springs | The fluid block |
| density | 0.5 | Share of the area that floods. Higher means more, larger pools |
| shore | Type default | The shore block. Water uses sand, lava uses basalt, ice uses snow block, springs use calcite |
Springs are pools of liquified spiritual qi, flagged so ambient qi concentrates around them and the spring effects (qi restore, cultivation bonus) apply inside them. Springs only appear inside slow "spirit vein" patches, are rare, and never near a river.

### Rivers
```json
{
  "type": "rivers",
  "block": "minecraft:water",
  "density": 0.21,
  "shore": "minecraft:gravel"
}
```
Rivers share one river network for the whole plot, independent of a region's terrain frequency, so two river regions side by side share the same river. A slow drainage mask makes rivers appear in only part of the land and taper out where it fades, so they end cleanly, never in a wall. When the next region has rivers too, the river simply continues across the border.

### Caves and ravines
```json
{
  "type": "caves",
  "density": 0.4
}
```
```json
{
  "type": "ravines",
  "density": 0.5,
  "min_height": 14,
  "max_height": 30
}
```
Caves are two warped 3D noise fields carved through the stone. Ravines are narrow surface gorges. Both are thinned out towards region borders and never carve through raised structures or water.

| Field | Default | Meaning |
| --- | --- | --- |
| density | 0.5 | How thick the caves or ravines are |
| min_height | Feature default | Ravines only: shallowest depth |
| max_height | Feature default | Ravines only: deepest depth |

### Spires, pillars and mesas
```json
{
  "type": "spires",
  "density": 0.1,
  "min_size": 2,
  "max_size": 4,
  "min_height": 12,
  "max_height": 28,
  "block": "minecraft:quartz_block"
}
```

| Field | Default | Meaning |
| --- | --- | --- |
| density | Required | Chance a grid cell rolls this feature |
| min_size, max_size | Feature default | Horizontal radius, in blocks |
| min_height, max_height | Feature default | Vertical height, in blocks |
| block | Variant palette's stone | The structural block. Mesas use the palette's bands instead if they have them |
Spires taper with height. Pillars are vertical columns. Mesas are flat-topped plateaus. All are judged at their own centre, so a border never cuts one in half. Water never goes where one stands, so a mesa is never riddled with lakes.

### Floating islands
```json
{
  "type": "floating_islands",
  "density": 0.12,
  "min_size": 6,
  "max_size": 12,
  "min_height": 45,
  "max_height": 100,
  "block": "minecraft:stone"
}
```
Floating islands are built above the surface from min_height to max_height, with a top and sub layer, and can hover over any variant. They are only considered outside a radius of 40 blocks from the spawn dais.

### Trees
```json
{
  "type": "trees",
  "tree": "minecraft:oak",
  "density": 0.06
}
```

| Field | Default | Meaning |
| --- | --- | --- |
| tree | Required | A vanilla configured feature id, e.g. minecraft:oak, minecraft:fancy_oak, minecraft:mega_jungle_tree, minecraft:cherry |
| density | Required | Chance a 6-block tree cell rolls this entry. Entries stack: the roll is compared against their accumulated density |
Trees are planted by vanilla's own configured features, so their shapes, roots, vines and fungi are exactly vanilla's. A chunk is only planted once it and all 8 chunks around it are built, so canopies never spill over unbuilt void. Trees are skipped inside a structure footprint, and every tree standing in a planned structure's footprint is cleared before the structure is placed.

## Qi
```json
"qi": {
  "capacity": 420,
  "regen": 4,
  "affinity": 1.4
}
```

| Field | Default | Meaning |
| --- | --- | --- |
| capacity | 300 | Ambient qi capacity of a chunk in this region |
| regen | 3 | Ambient qi regen per tick |
| affinity | 1.0 | The path affinity this region's air carries |
Ambient qi is applied per chunk on load. Density is the variant's values x the world's tier scaling (1 + 0.5 * tier) x 1.5 if this is the strongest path's region x 1 + 2 * spring share. So a region with springs is markedly richer, and a region of the player's strongest path is richer still. Tier changes only reach already-loaded chunks on their next load.

Path affinity is off by default (InnerWorldQi.ADD_PATH_AFFINITY) because of a crash in chunk path bonus syncing. When it is on, the region's own path is the affinity added.

## Resources
```json
"resources": {
  "ores": [],
  "treasure": [],
  "herbs": []
}
```

### Ores
```json
{
  "block": "minecraft:iron_ore",
  "density": 0.06,
  "min_depth": 6,
  "max_depth": 30,
  "scale": 5.0
}
```

| Field | Default | Meaning |
| --- | --- | --- |
| block | Required | The ore block |
| density | Required | How much of the eligible stone becomes ore |
| min_depth, max_depth | Feature default | Depth below the surface, in blocks |
| scale | 1.5 minimum | The size of the noise field. Larger means bigger, smoother ore blobs |
Ores replace stone only, below the surface layers and above the bottom 5 blocks. Ores are never placed on the dais.

### Treasure veins
```json
{
  "block": "minecraft:gold_block",
  "density": 0.12,
  "min_depth": 12,
  "max_depth": 28,
  "radius": 2
}
```

| Field | Default | Meaning |
| --- | --- | --- |
| block | Required | The treasure block |
| weight | 1.0 | This entry's share when a vein cell rolls a treasure type |
| density | Required | Chance a vein cell becomes a vein |
| min_depth, max_depth | Feature default | Depth below the surface of the vein centre |
| radius | 1 | The vein's radius. The vein is a sphere of this radius centred on the depth |
Vein positions are a pure function of the seed, so they are never stored. A faint particle column rises from the surface above any intact vein within 28 blocks, and Divine Sense can reveal veins inside its radius. A vein is intact while its centre block is not air.

### Herbs
```json
{
  "herb": "ascension:ginseng",
  "density": 0.006,
  "quality_bonus": 0
}
```

| Field | Default | Meaning |
| --- | --- | --- |
| herb | Required | The herb id |
| density | Required | Chance per column that this herb site is chosen |
| quality_bonus | 0 | Extra quality tiers a herb from this entry starts with |
Herb sites are a pure function of the seed, so a harvested herb regrows at the same spot. Age gets +1 tier per 3 world tiers, quality gets +1 tier per 2 world tiers, plus up to +1.5 tiers from the herb patch's 24-block luck roll, plus the entry's quality_bonus, capped by the herb's own quality cap. Herbs only spawn where the herb's own support and spawn rules accept, and their qi requirements are checked against the inner world's own ambient qi instead of the biome's.

## Mobs
```json
"mobs": {
  "min_realm": 0,
  "max_realm": 2,
  "life_multiplier_per_realm": 0.5,
  "spawns": [
    {
      "entity": "minecraft:zombie",
      "weight": 5,
      "min_group": 1,
      "max_group": 3
    }
  ]
}
```

| Field | Default | Meaning |
| --- | --- | --- |
| min_realm | 0 | The lowest mob cultivation realm this region spawns |
| max_realm | 2 | The highest without the Life Dao |
| life_multiplier_per_realm | 0.5 | With the Life Dao, the top realm is raised by this x the Life Dao realm |
| spawns | [] | The spawn list |

### Spawn entries

| Field | Default | Meaning |
| --- | --- | --- |
| entity | Required | The entity type id |
| weight | 1.0 | This entity's share of the region's spawns |
| min_group, max_group | 1 | How many spawn together |
A mob's "realm" maps onto the mob cultivation tier ladder: realm 0 = NORMAL, 1 = ELITE, 2 and above = ANCIENT. A mob in this region rolls a realm in [min_realm, max_realm], becomes that tier, and rolls the tier's potential traits exactly the way a normal mob spawn does. The Life Dao (ascension:elemental/life at major realm 1+) raises the top of that range and unlocks structures marked requires_life.

A region with no spawns entries spawns no mobs. That is usually a mistake, not a design.

## Structures
```json
"structures": [
  {
    "structure": "minecraft:village_plains",
    "weight": 3.0,
    "spacing": 320,
    "requires_life": true
  }
]
```

| Field | Default | Meaning |
| --- | --- | --- |
| structure | Required | The structure id |
| weight | 1.0 | This entry's share when a grid cell rolls a structure |
| spacing | 320 | Roughly how often it appears, in blocks |
| requires_life | true | If true, the entry only generates once the owner's Life Dao is at major realm 1+ |
Sites sit on a deterministic 160-block grid per plot. A cell's entry is rolled once, then up to six positions inside the cell are tried (a cheap dryness check first, vanilla planning only for spots that pass), and the first one that really fits is used.

### Placement rules

A structure is only placed when every chunk it covers is already built terrain. Sites near the frontier wait until later rings arrive.

Sites over water, banks, mesas or the dais are skipped.

Jigsaw structures (villages and the like) are judged piece by piece: every rigid piece (a house) must stand on dry, unraised, reasonably level ground, and at most one rigid piece in ten may fail. Roads and other terrain-matching pieces may cross anything, as in vanilla.

Non-jigsaw structures must be at most 15% wet, unraised, off the dais, and not too uneven.

Trees in and just around the footprint are cleared first, and the footprint is remembered so no tree is planted there later.

Rigid pieces get a levelled pad, a thin version of vanilla's terrain beard; roads follow the ground as in vanilla.

Inhabitants get a mob cultivation realm from the region's range, like spawned mobs (currently off; see GIVE_INHABITANTS_REALMS).

Spacing is a hint, not a promise: spacing sets the chance a cell rolls this entry at all (chance = (CELL / spacing)^2), and the cell must still pass the position and suitability checks. A village needs roughly 12-16 chunks built around its site, so structures only start appearing once the plot has built radius 6+.

## Regions and borders
Regions are vanilla-style: two warped, multi-octave climate noises place every column on a 2D climate map. Each of the seed's 3-5 themes owns a point on that map, and a column belongs to the nearest theme point (distance scaled by the theme's path weight, so strongly cultivated paths own more land). Because the noise is smooth you get continent-sized, irregular biomes with natural borders. The core around the dais always belongs to theme 0.

STRUCTURE vs STYLE: palette and decoration may dither across a border, but every structural feature (mesas, spires, pillars, floating islands) is decided once at its own centre, so it is always built whole, never riddled with holes where a column happened to roll a neighbouring theme. Lakes, rivers, caves and ravines fade smoothly out towards a border instead of stopping dead.

biomeAt is crisp (vanilla-style biome borders, no dithering) and decides the atmosphere biome. variantAt uses the dithered dominant theme and decides style and mob spawning. Both are pure functions of (seed, x, z).

## Water, caves and structures
WATER is sculpted, not punched in: a warped noise field defines each lake or river; inside it the bed dips smoothly into a bowl, outside it the terrain eases down (or up) to meet the shoreline over a wide bank, and a band of shore blocks (sand, basalt, calcite...) ties it together. Every pool in a plot shares one water level.

A liquid block placed next to AIR schedules a flow tick even with block-update flags off. During generation the neighbouring column is often not built yet, so a pool column is only filled once all four neighbours are provably there and each is either another pool column or ground at or above the water level. Anything still waiting is completed later, when the neighbour is built.

Every column is self-contained (nothing writes into a neighbouring column), so the budgeted per-tick column loop can stop and resume anywhere. A ring only advances when it is whole, so players never see (or stand on) half-built land.

## Complete example
```json
{
  "path": "ascension:elemental/fire",
  "weight_multiplier": 1.0,
  "variants": [
    {
      "weight": 1.2,
      "biome": "minecraft:nether_wastes",
      "palette": {
        "top": "minecraft:netherrack",
        "sub": "minecraft:netherrack",
        "stone": "minecraft:blackstone",
        "deep": "minecraft:deepslate",
        "decor": [
          { "block": "minecraft:fire", "chance": 0.004 }
        ]
      },
      "terrain": {
        "amplitude": 26.0,
        "frequency": 1.1
      },
      "features": [
        { "type": "pools", "block": "minecraft:lava", "density": 0.2 },
        {
          "type": "spires",
          "block": "minecraft:basalt",
          "density": 0.12,
          "min_size": 2,
          "max_size": 5,
          "min_height": 10,
          "max_height": 26
        },
        { "type": "rivers", "block": "minecraft:lava", "density": 0.21 }
      ],
      "qi": { "capacity": 500, "regen": 5, "affinity": 1.4 },
      "resources": {
        "ores": [
          {
            "block": "minecraft:nether_gold_ore",
            "density": 0.06,
            "min_depth": 6,
            "max_depth": 30
          }
        ],
        "treasure": [
          {
            "block": "minecraft:ancient_debris",
            "density": 0.2,
            "min_depth": 14,
            "max_depth": 30,
            "radius": 2
          }
        ],
        "herbs": []
      },
      "mobs": {
        "min_realm": 0,
        "max_realm": 3,
        "life_multiplier_per_realm": 0.5,
        "spawns": [
          {
            "entity": "minecraft:zombified_piglin",
            "weight": 6,
            "min_group": 2,
            "max_group": 4
          },
          {
            "entity": "minecraft:piglin",
            "weight": 4,
            "min_group": 1,
            "max_group": 3
          },
          {
            "entity": "minecraft:hoglin",
            "weight": 3,
            "min_group": 1,
            "max_group": 2
          },
          {
            "entity": "minecraft:magma_cube",
            "weight": 3,
            "min_group": 1,
            "max_group": 2
          }
        ]
      },
      "structures": [
        {
          "structure": "minecraft:bastion_remnant",
          "weight": 1.0,
          "spacing": 360
        },
        {
          "structure": "minecraft:ruined_portal_nether",
          "weight": 1.0,
          "spacing": 256,
          "requires_life": false
        }
      ]
    }
  ]
}
```

## Compact reference

**Profile directory**

```text
data/<namespace>/ascension/inner_world_profile/
```

**Root fields**

```text
path
weight_multiplier
variants
```

**Variant fields**

```text
weight
biome
palette: top, sub, stone, deep, bands, decor
terrain: offset, amplitude, frequency, terrace
features: pools, springs, rivers, caves, ravines, spires, pillars, mesas,
          floating_islands, trees
qi: capacity, regen, affinity
resources: ores, treasure, herbs
mobs: min_realm, max_realm, life_multiplier_per_realm, spawns
structures: structure, weight, spacing, requires_life
```

**Rules**

- A player's seed picks 3-5 variants from the profiles of the paths they hold, weighted by path progress and variant weight.
- The heaviest candidate is theme 0 and owns the core around the dais.
- Structural features are decided at their own centre; water and caves fade towards region borders.
- Trees are planted by vanilla configured features.
- Treasure veins and herb sites are pure functions of the seed, so they can be recomputed and regrow.
- Structures need built land around them and wait for it, never cut into void.
