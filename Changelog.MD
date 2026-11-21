**ZIC Team**

# Ascension Changelog

---

## Ascension Version 0.2.2 Snapshot 26.1.2

### Added
* Added a new Technique, Bloodline and Physique Type (Connected type)
* Added a new Technique and Physique using the Connected Types
* Mastery related changes to many skills
* Added cultivation path assignment and management commands
* Added Cloth Fist Wraps, the first fist weapon. They're crafted from wool and string and can be dyed.
* Added 2 new fist weapon enchantments:
  * Impact adds bonus melee damage (up to level 5).
  * Penetrating Strike ignores part of the target's armor, 15% per level (up to level 3).
* Chakrams can now carry External Qi Coating. Thrown chakrams show the sword qi look in flight and apply the element's on-hit effect to whatever they hit.
* Added an animation for sitting on cushions and cultivating on cushions
* Techniques now give stats, and have enforced max realms etc
* Added new Skill; Sword Control and Twin Sword Control
* Added weapon cultivation passives to the weapon paths (Techniques will be used to grant stronger ones and special effect ones hopefully)
* Added Scythe Path and related stuff
* Added a second furnace tier with a pill prediction feature

### Removed
* Removed all divine senses
* Temporarily disabled all Spherical Devestation Skills
* Removed obsolete testing commands

### Changes
* Auction max and min time changed: 1 min to 1440 min
* Auction Items show proper tooltip on hover now
* Turned all Divine senses into 3 different types. Soul, Essence and Body only.
* Skills can actually gain Mastery now
* Passive Skills have Levels, Active Skills have Ranks for master
* Buffed the effect of focus steering 

### Fixes
* Projection Skills actually project, deal some damage and break blocks
* Claiming Button in Auction Inbox actually functions now
* Fixed so Phasing Recall can spawn on the top of pagoda
* Fixed premature stat-sync dirty flag clearing by separating entity stat updates from network patch tracking in OriginSource
* Plum Blossom Sword Technique now cultivates sword and essence

### Technical Changes
* Unified RGB and ARGB colour handling across most skill visuals, particles, and Qi Coatings
* Refactored and consolidated commands

---

## Ascension Version 0.2.1 Snapshot 26.1.2

### Added
* Added Dual Wielding Skill (Fist, Blade Dao gets it at second major realm)
* Added 8 New Weapons (Chakrams)
* Added 4 New Enchantments (Chakram Enchantments)
* Added Missing Peach Blocks
* Added More Recipes
* Added custom divine sense to all techniques
* Added Concealment skill type
* Added a data-driven Player Visual System supporting custom models, textures, and GeckoLib animations
* Added semi-animated demo model Fox Ears and Tails for the Moonveil Fox Bloodline, with 1–3 tails based on bloodline purity
* New structure; Large Pagoda
* Sect Elders and Sect Leaders spawning has been moved to the large Pagoda
* Added more ways to get techniques

### Removed
* Disabled Spherical Devestation
* Sect Elders and Sect Masters no longer spawn in villages

### Changes
* Updated the Alchemy Reaction phase to require player Qi contribution, with consumption rates determined by the selected Alchemy Technique
* Updated the Alchemy Stabilization phase with an interactive minigame, where player performance influences the final pill quality
* Moved Spawn Eggs into Spawn Egg Tab
* Tweaked the progression between realms
* Decreased chance for Young Master to spawn in villages
* Older herbs gain a +45% increase in properties per growth-stage :)

### Fixes
* Fixed Peach Planks not having a Recipe.
* Fixed Body Cultivation skills by making them toggleable
* Auction House and Bidder not opening GUIs
* NPC Guidance now actual gives randomly selected techniques from their respective pools
* Jade Bottle Tooltip now shows pill and pill expiry instead of placeholder values

### Technical Changes
* Moved nested ToggleablePassiveSkill class out of PassiveSkill for reusability

---

## Ascension Version 0.2.0 Snapshot 26.1.2

### Added
* Added 47 new Paths - Foundation Paths (3) Elemental Paths (29) Weapon Paths (15)
* Added 56 new Physiques
* Added 39 new bloodlines
* Added 43 new techniques
* Added 12 new Herb
* Added 5 new Artifacts
* [**W.I.P**] [**Extremely Bugged**] Added New World Type -> Jianghu with overhauled terrain generation and increased world height (416)
* [**W.I.P**] [Needs Decoration] Added 8 new biomes; Heavenreach Peak, Graystone Foothills, Azure Cloud Range, Ancient Grove, Jadebloom Forest, Misty Woods, Verdant Moor, Golden Steppe
* Added 3 new Tool sets + Weapons Black Iron, Frost Silver, Spiritual Stone
* Added Inner World with JSon compatibility for special biomes etc...
* Ported Most blocks from 1.21.1
* Added Datapack support for Paths, Bloodlines, Physiques, Techniques, Skills, Tribulations and Atmospheric Qi Density.  
* [**W.I.P**] [**Extremely Bugged**] Added New World Type -> Jianghu with overhauled terrain generation and increased world height (max y = 416)
* [**W.I.P**] [Needs Decoration] Added 8 new biomes; Heavenreach Peaks, Greystone Foothills, Azure Cloud Range, Ancient Grove, Jadebloom Forest, Misty Woods, Verdant Moor, Golden Steppe
* Added 3 new Tool sets + Weapons: Black Iron, Frost Silver, Spiritual Stone
* Ported Most blocks from 1.21.1 (Marble and variants moved into [Builders Zenith](https://www.curseforge.com/minecraft/mc-mods/builders-zenith))
* Added Datapack support for Paths, Bloodlines, Physiques, Techniques, Skills, Tribulations and Atmospheric Qi Density.
* Starter Bloodline and Physique Selection on first world/server join
* [**W.I.P**] A Cultivation System for Mobs who actually grow over time and wield skills and valuable loot
* Added a Stamina system for movement and physical actions
* Added Mastery to active skills
* Added tons of new skills and skill types
* [**W.I.P**] Added in Alchemy (Herbs + Pill Creation)
* [**W.I.P**] Added in Formations
* Added five cultivator NPCs: Male Cultivator, Female Cultivator, Young Master, Sect Elder, and Sect Master, with individual names, skins, and cultivation realms.
* Ported the Treasure Rat

### Removed
* Vanilla Health overlay

### Changes
* Expanded Main Paths to 12 major realms.
* Elemental paths (Daos) have 9 major realms.
* Weapon paths (Daos) have 6 major realms.
* Functional GUI Port
* Updated cultivation rates so secondary Path affinities provide a smaller bonus instead of another full base cultivation rate
* Sprinting, swimming, climbing, crawling, jumping, attacking and elytra movement can now consume Stamina
* Stamina regeneration is slower while hungry and stops at zero hunger
* Can possess multiple techniques per path
* Moved Marble into Builders-Zenith

### Fixes
* Fixed mob cultivation source initialisation and realm changes across world reloads.


### Technical Changes
* Merged all active skills into one active_skill type
* Unified all current passive skills into one passive_skill type
* Added data-driven NPC personality, gift preference, and village spawning configurations.
* Added entity-type affiliation tags for Orthodox, Unorthodox, and Demonic mobs.

---