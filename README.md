# Husbandry

A Paper plugin that introduces genetic traits and breeding mechanics to Minecraft animals, and a crop trait system that adds depth to farming. Animals gain unique traits that affect their stats, behavior, and abilities, and pass those traits to their offspring through an inheritance system. Crops can roll traits on harvest that modify yield, and quality crops serve as the bridge between the two systems through Quality Feed crafting.

**Requires:** Paper 26.1.1 | Java 25

---

## Table of Contents

- [Installation](#installation)
- [Getting Started](#getting-started)
- [Animal Traits](#animal-traits)
  - [How Animals Get Traits](#how-animals-get-traits)
  - [Gender and Breeding](#gender-and-breeding)
  - [Trait Inheritance](#trait-inheritance)
  - [Quality Feed and Rare Breeding](#quality-feed-and-rare-breeding)
  - [Trait List: General](#trait-list-general)
  - [Trait List: Species-Specific](#trait-list-species-specific)
- [Crop Traits](#crop-traits)
  - [How Crops Get Traits](#how-crops-get-traits)
  - [Crop Trait Effects](#crop-trait-effects)
  - [Trait Inheritance for Crops](#trait-inheritance-for-crops)
  - [Quality Trait vs Quality Crop](#quality-trait-vs-quality-crop)
  - [Pumpkins and Melons](#pumpkins-and-melons)
  - [Immature Crops](#immature-crops)
  - [Crafting with Traits](#crafting-with-traits)
- [Custom Items](#custom-items)
  - [Trait Analyzer](#trait-analyzer)
  - [Trait Editor](#trait-editor)
  - [Crop Analyzer](#crop-analyzer)
  - [Crop Editor](#crop-editor)
  - [Quality Feed](#quality-feed)
- [Commands](#commands)
- [Configuration](#configuration)
- [Supported Animals](#supported-animals)
- [Supported Crops](#supported-crops)

---

## Installation

1. Build the plugin with `./gradlew build` (requires Java 25).
2. Place the resulting JAR from `build/libs/` into your Paper server's `plugins/` folder.
3. Start or restart the server.
4. The plugin generates a default `config.yml` on first run.

---

## Getting Started

Once installed, the trait system is fully automatic. Animals receive random traits and a gender the first time they are loaded, and crops have a chance to roll traits when harvested. No setup or configuration is required beyond placing the JAR in your plugins folder.

To inspect traits, craft a **Trait Analyzer** or **Crop Analyzer** and right-click the target. Server operators can use `/husbandry` commands to obtain tools and test items directly.

---

## Animal Traits

### How Animals Get Traits

When an animal entity is loaded for the first time (spawned naturally or via commands), the plugin initializes it with:

- A **gender** (Male or Female), assigned randomly with a 50/50 chance.
- **1 to 2 random traits**, rolled from the pool of traits applicable to that animal's species. Trait rarity is weighted by the configured chances (default: 75% Basic, 20% Rare, 5% Legendary).

**Spawner-block animals are excluded.** Animals spawned from spawner blocks are permanently flagged and will never receive traits or a gender. This flag persists across chunk unloads and server restarts.

Trait data is stored directly on the entity via Persistent Data Containers (PDC) and persists across server restarts, chunk unloads, and world saves with no external database required.

### Gender and Breeding

Every animal is assigned either **Male** or **Female** on initialization. Breeding requires one parent of each gender. Two animals of the same gender cannot breed, and the breeding event will be silently cancelled.

### Trait Inheritance

When two compatible animals breed successfully, their offspring's traits are determined by the inheritance rules of each parent trait:

| Inheritance Type | Rule | Example Traits |
|:---:|---|---|
| **Dominant** | Inherited if **at least one** parent has it | Sluggish, Brittle, Barren, Hydrophobic |
| **Recessive** | Inherited only if **both** parents have it | Hasty, Beefy, Heavy, Fertile |
| **Special** | **Never** inherited through normal breeding | Fireproof, Bountiful, Invincible, Pollinator |

Special traits represent powerful abilities that cannot be passed down through conventional breeding. See [Quality Feed and Rare Breeding](#quality-feed-and-rare-breeding) for the one exception, which requires both parents to share the trait.

### Quality Feed and Rare Breeding

**Quality Feed** items are crafted from quality crops (see [Crop Traits](#crop-traits)) and provide the only way to inherit **Rare-rarity Special** traits through breeding.

To use Quality Feed:

1. Right-click each parent with the appropriate Quality Feed item before they breed.
2. Both parents must be fed for the effect to activate.
3. When both parents are quality-fed, any **Rare-rarity Special** trait that **both** parents share is inherited to the offspring.
4. If only one parent has a Rare Special trait, it is **not** passed down, even with Quality Feed.
5. **Legendary-rarity Special** traits are never inherited, even with Quality Feed.

Each feed type works only on specific animal species:

| Feed Item | Animals |
|---|---|
| Quality Wheat Feed | Cow, Sheep, Mooshroom, Goat |
| Quality Carrot Feed | Pig |
| Quality Golden Carrot Feed | Rabbit, Horse, Donkey |
| Quality Seed Feed | Chicken |

### Trait List: General

These traits can appear on any animal species.

| Trait | Rarity | Inheritance | Effect |
|---|:---:|:---:|---|
| **Sluggish** | Basic | Dominant | Reduces movement speed by 50% |
| **Hasty** | Basic | Recessive | Increases movement speed by 50% |
| **Beefy** | Basic | Recessive | Increases maximum health by 50% |
| **Brittle** | Basic | Dominant | Reduces maximum health by 50% |
| **Heavy** | Basic | Recessive | Increases knockback resistance by 50% |
| **Fertile** | Basic | Recessive | When both parents have this trait, a twin is spawned alongside the baby |
| **Barren** | Basic | Dominant | Prevents this animal from breeding entirely (cancels the breed event) |
| **Hydrophobic** | Basic | Dominant | Takes half a heart of damage per second while in water or rain |
| **Fireproof** | Rare | Special | Grants permanent fire resistance |
| **Waterbreathing** | Rare | Special | Prevents suffocation damage |
| **Swift Swimmer** | Rare | Special | Grants permanent Dolphin's Grace effect while swimming |
| **Hydrophilic** | Rare | Special | Heals half a heart per second while in water or rain |
| **Bountiful** | Rare | Special | Doubles all item drops on death |
| **Invincible** | Legendary | Special | Immune to all damage |

### Trait List: Species-Specific

These traits only appear on (and are only rolled for) the listed species.

#### Wolf

| Trait | Rarity | Inheritance | Effect |
|---|:---:|:---:|---|
| **Cowardly** | Basic | Dominant | Reduces attack damage by 50% |
| **Fierce** | Basic | Recessive | Increases attack damage by 50% |

#### Bee

| Trait | Rarity | Inheritance | Effect |
|---|:---:|:---:|---|
| **Honey Hindrance** | Basic | Dominant | Does not add honey to the hive when entering with nectar |
| **Honey Helper** | Rare | Special | Adds an extra honey level to the hive when entering with nectar |
| **Pollinator** | Legendary | Special | Instantly grows crops to full maturity when pollinating |

#### Horse, Donkey, and Mule

| Trait | Rarity | Inheritance | Effect |
|---|:---:|:---:|---|
| **Short Jumper** | Basic | Dominant | Reduces jump strength by 50% |
| **High Jumper** | Basic | Recessive | Increases jump strength by 50% |

#### Sheep

| Trait | Rarity | Inheritance | Effect |
|---|:---:|:---:|---|
| **Wooly** | Rare | Special | Doubles wool drops when sheared |

#### Chicken

| Trait | Rarity | Inheritance | Effect |
|---|:---:|:---:|---|
| **Empty** | Basic | Dominant | Prevents egg laying |
| **Eggy** | Rare | Special | Drops an extra egg each time the chicken lays one |

---

## Crop Traits

### How Crops Get Traits

When a **non-traited, fully grown** crop is harvested, each trait is rolled independently with its own chance:

| Trait | Rarity | Default Chance |
|---|:---:|:---:|
| **Barren** | Basic | 15% |
| **Bountiful** | Rare | 10% |
| **Quality** | Legendary | 5% |

Multiple traits can be rolled simultaneously on the same harvest. If no traits are rolled, the drops are completely vanilla.

**Initial roll targets:** Traits from initial rolls are only applied to **plantable items** (seeds, carrots, potatoes) and **fruit items** (pumpkins, melon slices). Non-plantable harvest products like wheat and beetroot **never** receive traits from an initial roll.

### Crop Trait Effects

When a traited crop is harvested at full maturity:

| Trait | Effect |
|---|---|
| **Barren** | The crop produces **nothing** when harvested. All drops are removed. |
| **Bountiful** | All drop quantities are **doubled**. This stacks with Fortune enchantments since Fortune is applied before doubling. |
| **Quality** | All drops receive the **Quality Crop** marker (gold name, enchantment glint, "Quality Crop" lore). Seeds and plantables inherit all other traits but **not** the Quality trait itself. See [Quality Trait vs Quality Crop](#quality-trait-vs-quality-crop). |

When multiple traits are present, all effects apply. Barren takes absolute precedence: if a crop has both Barren and Bountiful, nothing drops.

### Trait Inheritance for Crops

Crop trait inheritance follows a simple flow:

1. **Harvest** a non-traited crop. Each trait is rolled independently.
2. **Plantable drops** (seeds, carrots, potatoes) and **fruit drops** (pumpkins, melon slices) receive the rolled traits.
3. **Plant** a traited seed. All traits, including Quality, are stored on the crop block.
4. **Harvest** the grown crop. Trait effects apply to the drops.
5. **Seeds from the harvest** inherit all traits **except Quality**. The Quality trait is consumed to produce quality items and does not pass to the next generation.
6. All drops receive the **Quality Crop** marker if the crop had the Quality trait.

This means Quality is a one-generation effect: plant a Quality seed, harvest quality items, but the next seeds will not carry Quality forward. Other traits like Bountiful persist indefinitely through replanting.

### Quality Trait vs Quality Crop

This is the most important distinction in the crop system:

| Concept | What It Is | PDC Tag | Visual | Purpose |
|---|---|---|---|---|
| **Quality Trait** | A planting trait stored on seeds | `crop_traits` (includes `QUALITY`) | "Quality" lore line in gold | When planted and harvested, the crop's drops become quality items |
| **Quality Crop Marker** | A crafting marker on harvested items | `quality_crop` (separate tag) | "Quality [Name]" display name in gold, "Quality Crop" lore, enchantment glint | Marks the item as a quality crafting ingredient for Quality Feed recipes |

Key rules:
- The Quality **trait** causes a crop to produce quality items. It is consumed on harvest and does not pass to the next generation of seeds.
- The Quality Crop **marker** is what matters for crafting Quality Feed. It appears on items harvested from Quality-traited crops.
- **Plantable items (seeds, carrots, potatoes) will never have both** the Quality trait and the Quality Crop marker at the same time. If both would be applied, the Quality Crop marker takes precedence and the Quality trait is stripped. Non-plantable items (wheat, beetroot, pumpkin, melon slice) can only have the Quality Crop marker, never the Quality trait.
- Planting a seed that only has the Quality Crop marker (but no Quality trait) will **not** produce quality items. Only the trait matters for planting.

### Pumpkins and Melons

Pumpkins and melons work differently from standard crops because the harvestable fruit grows from a stem:

- **Planting:** When a traited pumpkin or melon seed is planted, its traits (including Quality) are stored on the stem block.
- **Fruit growth:** When a fruit block grows from a stem, the stem's traits are copied to the fruit block automatically. If the stem has the Quality trait, it is consumed from the stem and applied to the fruit (one-shot behavior).
- **Fruit harvest:** The fruit block carries its own traits. Barren fruit drops nothing, Bountiful fruit drops double, and Quality fruit produces quality items.
- **Stem harvest:** Breaking a stem with traits returns traited seeds. Barren stems drop nothing.

### Immature Crops

Breaking a crop before it is fully grown has specific behavior for traited crops:

| Scenario | Result |
|---|---|
| Immature + no traits | Vanilla behavior (normal seed drop) |
| Immature + Barren | **Nothing drops** (Barren takes precedence over everything) |
| Immature + any other traits | Drops **1 traited seed** with the crop's traits preserved |
| Immature + Barren + other traits | **Nothing drops** (Barren always wins) |

Trait data is always cleaned up from the chunk when an immature crop is broken, regardless of whether it had traits.

### Crafting with Traits

When crafting **pumpkin seeds** or **melon seeds** from a pumpkin or melon slice:

- All traits on the source item, including the Quality trait, are passed through to the resulting seeds.
- The Quality Crop marker, if present on the source, is also passed through to the seeds.
- If the source has both the Quality trait and the Quality Crop marker, the Quality Crop marker takes precedence on the resulting seeds and the Quality trait is stripped (plantables never have both).
- A Quality pumpkin (with the Quality trait but no Quality Crop marker) crafted into seeds will produce Quality pumpkin seeds that, when planted, will grow a stem capable of producing quality pumpkins.

---

## Custom Items

### Trait Analyzer

A tool for inspecting animal traits. Right-click any animal to open a GUI displaying its gender and all current traits with their rarity and description.

**Crafting Recipe (Shapeless):**
Comparator + Beef + Porkchop + Chicken + Feather + Egg + Leather + Mutton + Wool

### Trait Editor

An operator-only tool for modifying animal traits. Right-click any animal to open an editor GUI where traits can be toggled on or off.

**Obtainable via:** `/husbandry editor` (requires `husbandry.admin` permission)

### Crop Analyzer

A tool for inspecting crop traits on planted crops and stems. Right-click any supported crop block to open a GUI displaying the block's current traits.

**Crafting Recipe (Shapeless):**
Comparator + Wheat Seeds + Wheat + Potato + Carrot + Melon Seeds + Melon Slice + Pumpkin + Pumpkin Seeds

### Crop Editor

An operator-only tool for modifying crop traits on planted blocks. Right-click any supported crop block to open an editor GUI where traits can be toggled.

**Obtainable via:** `/husbandry crop_editor` (requires `husbandry.admin` permission)

### Quality Feed

Quality Feed items are crafted from quality crops and used to enable rare trait inheritance during animal breeding. Feed both parents before they breed to activate the effect.

All Quality Feed recipes are **shapeless**. Every ingredient slot must contain a **full stack of 64 items**, and every crop ingredient must be a **Quality Crop** (identifiable by its gold name, "Quality Crop" lore, and enchantment glint). Each recipe produces **4 feed items**. The entire crafting grid (all 9 slots) is consumed when crafting.

| Feed Item | Recipe (each slot = 64 items) |
|---|---|
| Quality Wheat Feed | 3 slots Quality Wheat (192) + 3 slots Quality Pumpkin (192) + 3 slots Quality Melon Slice (192) |
| Quality Carrot Feed | 3 slots Quality Carrot (192) + 3 slots Quality Potato (192) + 3 slots Quality Beetroot (192) |
| Quality Golden Carrot Feed | 4 slots Quality Carrot (256) + 4 slots Quality Melon Slice (256) + 1 slot Gold Ingot (64) |
| Quality Seed Feed (variant A) | 3 slots Quality Wheat Seeds (192) + 3 slots Quality Beetroot Seeds (192) + 3 slots Quality Pumpkin Seeds (192) |
| Quality Seed Feed (variant B) | 3 slots Quality Wheat Seeds (192) + 3 slots Quality Beetroot Seeds (192) + 3 slots Quality Melon Seeds (192) |

---

## Commands

All commands require the `husbandry.admin` permission (default: op).

| Command | Description |
|---|---|
| `/husbandry analyzer` | Gives a Trait Analyzer |
| `/husbandry editor` | Gives a Trait Editor |
| `/husbandry crop_analyzer` | Gives a Crop Analyzer |
| `/husbandry crop_editor` | Gives a Crop Editor |
| `/husbandry quality_wheat_feed` | Gives Quality Wheat Feed (x4) |
| `/husbandry quality_carrot_feed` | Gives Quality Carrot Feed (x4) |
| `/husbandry quality_golden_carrot_feed` | Gives Quality Golden Carrot Feed (x4) |
| `/husbandry quality_seed_feed` | Gives Quality Seed Feed (x4) |
| `/husbandry quality_crop <type>` | Gives a Quality Crop item for testing |

**Quality Crop types:** `wheat`, `beetroot`, `carrot`, `potato`, `wheat_seeds`, `beetroot_seeds`, `pumpkin_seeds`, `melon_seeds`, `pumpkin`, `melon_slice`

---

## Configuration

The plugin generates a `config.yml` with the following defaults:

```yaml
# Trait rarity chances for animals (when first encountered).
# Values are percentages and should sum to 100.
animal-traits:
  basic-chance: 75
  rare-chance: 20
  legendary-chance: 5

# Independent per-trait chances for crops (when harvesting non-traited crops).
# Each trait is rolled independently, so a crop can have multiple traits.
# If no traits are rolled, the drop is vanilla (no traits).
crop-traits:
  barren-chance: 15
  bountiful-chance: 10
  quality-chance: 5
```

**Animal trait chances** are weighted probabilities used when an animal is first initialized. They determine the rarity tier of each trait rolled, and should sum to 100.

**Crop trait chances** are independent probabilities. Each trait is rolled separately on every non-traited crop harvest, meaning a single harvest can produce seeds with multiple traits (e.g., both Bountiful and Quality at 0.5% combined chance).

---

## Supported Animals

The trait system applies to all passive animal mobs. Species-specific traits are restricted to their listed types, while general traits can appear on any animal.

| Animal | Species-Specific Traits |
|---|---|
| Cow | -- |
| Sheep | Wooly |
| Pig | -- |
| Chicken | Empty, Eggy |
| Rabbit | -- |
| Horse | Short Jumper, High Jumper |
| Donkey | Short Jumper, High Jumper |
| Mule | Short Jumper, High Jumper |
| Goat | -- |
| Mooshroom | -- |
| Wolf | Cowardly, Fierce |
| Bee | Honey Hindrance, Honey Helper, Pollinator |

---

## Supported Crops

| Crop | Plantable Item | Harvest Products |
|---|---|---|
| Wheat | Wheat Seeds | Wheat, Wheat Seeds |
| Beetroots | Beetroot Seeds | Beetroot, Beetroot Seeds |
| Carrots | Carrot | Carrot |
| Potatoes | Potato | Potato |
| Pumpkin | Pumpkin Seeds | Pumpkin (fruit block) |
| Melon | Melon Seeds | Melon Slice (from fruit block) |