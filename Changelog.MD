**ZIC Team**

# Ascension Changelog

## Ascension Version 0.2.0 Snapshot 26.1.2

### Added
* Added 47 new Paths - Foundation Paths (3) Elemental Paths (29) Weapon Paths (15)
* Added 56 new Physiques
* Added 39 new bloodlines
* Added 43 new techniques
* Added 12 new Herb
* Added 5 new Artifacts
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