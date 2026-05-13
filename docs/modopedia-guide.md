# Modopedia Integration Guide

Modopedia version: `1.1.11`  
NeoForge 1.21.1  
Maven: `https://maven.favouriteless.net/releases`  
Dependency coord: `net.favouriteless.modopedia:modopedia-neoforge-${minecraft_version}:${modopedia_version}`

---

## Gradle Setup

**gradle.properties:**
```properties
modopedia_version=1.1.11
```

**build.gradle dependencies block:**
```groovy
# Dev-only (won't ship in the jar):
localRuntime "net.favouriteless.modopedia:modopedia-neoforge-${minecraft_version}:${modopedia_version}"

# Required dependency (ships in jar, players need it installed):
implementation "net.favouriteless.modopedia:modopedia-neoforge-${minecraft_version}:${modopedia_version}"
```

The `localRuntime` config is already set up in the project — it extends `runtimeClasspath` so it appears in `runClient` but is excluded from the built artifact. Swap to `implementation` when ready to ship.

---

## File Structure

Everything is data-driven — no Java required for basic books.

```
data/<modid>/modopedia/books/
    <bookid>.json                              ← book definition

assets/<modid>/modopedia/
    books/<bookid>/<language>/
        categories/
            <category>.json                    ← category definition
        entries/
            <entry>.json                       ← entry with pages
    templates/
        <template>.json                        ← reusable page layouts
    book_textures/
        <texture>.json                         ← custom book texture metadata
```

---

## Book Definition (`data/<modid>/modopedia/books/<bookid>.json`)

```json
{
  "title": "My Book",
  "subtitle": "A subtitle",
  "landing_text": "Welcome text shown on the landing page.",
  "model": "<modid>:item/modopedia_books/my_book",
  "type": { "id": "modopedia:classic", "locked_view_type": "translucent" },
  "texture": "modopedia:purple_gold",
  "text_colour": "3B2A1A",
  "header_colour": "B8A349",
  "font": "minecraft:default",
  "open_sound": "minecraft:item.book.page_turn",
  "flip_sound": "minecraft:item.book.page_turn"
}
```

| Field | Required | Notes |
|---|---|---|
| `title` | Yes | Supports translation keys |
| `subtitle` | No | |
| `landing_text` | No | Supports FString formatting |
| `model` | No | Must live in `models/item/modopedia_books/` |
| `creative_tab` | No | Which creative tab the book appears in |
| `type` | No | Defaults to `modopedia:classic` |
| `texture` | No | Defaults to `modopedia:default` |
| `text_colour` | No | Hex color for body text |
| `header_colour` | No | Hex color for headers |
| `font` | No | ResourceLocation |
| `open_sound` | No | Sound on opening |
| `flip_sound` | No | Sound on page turn |

---

## Built-in Book Textures

25 pre-made textures in the format `modopedia:<colour>_<metal>`:

- **Colors:** brown, blue, green, red, purple  
- **Metals:** iron, silver, gold, brass, copper  
- Example: `modopedia:purple_gold`, `modopedia:red_iron`

### Custom Book Textures

Place at `assets/<modid>/modopedia/book_textures/<name>.json`. The JSON defines the texture file path, book background dimensions, page rectangles (u, v, width, height), button positions (left, right, back, refresh), title backer position, and optional widget positions (separators, frames, grids, arrows).

---

## Category Definition (`assets/.../categories/<category>.json`)

```json
{
  "title": "My Category",
  "icon": { "id": "minecraft:diamond" },
  "landing_text": "Short description shown on the category screen.",
  "entries": ["entry_one", "entry_two"],
  "children": ["subcategory_one"],
  "sort_num": 1
}
```

---

## Entry Definition (`assets/.../entries/<entry>.json`)

```json
{
  "title": "My Entry",
  "icon": { "id": "minecraft:emerald" },
  "advancement": "minecraft:story/mine_diamond",
  "pages": [
    {
      "components": [
        {
          "type": "modopedia:header",
          "text": "Introduction"
        },
        {
          "type": "modopedia:text",
          "x": 0,
          "y": 14,
          "text": "This is body text with $(b)bold$() and $(c:#FF5500)color$()."
        }
      ]
    }
  ]
}
```

- `advancement` — entry is locked until the player earns this advancement (optional)
- Each object in `pages` is one book page
- Each page has a `components` array

---

## Page Components

All components share `x` and `y` position fields. The coordinate origin is the top-left of the page area.

### `modopedia:text`
Body text with word wrap and alignment.
```json
{
  "type": "modopedia:text",
  "x": 0, "y": 0,
  "text": "Your text here.",
  "width": 114,
  "line_height": 9,
  "justify": "left"
}
```
- `justify`: `left` (default), `center`, `right`

---

### `modopedia:header`
Large bold section title.
```json
{
  "type": "modopedia:header",
  "x": 0, "y": 0,
  "text": "Section Title",
  "centered": true,
  "colour": "#B8A349",
  "bold": true,
  "underline": false
}
```

---

### `modopedia:separator`
Horizontal divider line. X position is ignored — always centers to the book's line width.
```json
{
  "type": "modopedia:separator",
  "x": 0, "y": 20
}
```

---

### `modopedia:image`
Images from your resource pack. Supports multiple images with arrow navigation.
```json
{
  "type": "modopedia:image",
  "x": 0, "y": 0,
  "images": ["mymod:textures/gui/my_image.png"],
  "width": 100,
  "height": 100
}
```

---

### `modopedia:item_gallery`
Flexible item display system with multiple layout modes.

**Simple (single item):**
```json
{
  "type": "modopedia:item_gallery",
  "x": 0, "y": 0,
  "display": { "type": "modopedia:simple", "item": { "id": "minecraft:diamond" } }
}
```

**Cycling (rotates through items every second):**
```json
{
  "display": { "type": "modopedia:cycling", "items": [{"id": "minecraft:diamond"}, {"id": "minecraft:emerald"}] }
}
```

**Tag (cycles through all items in a tag):**
```json
{
  "display": { "type": "modopedia:tag", "tag": "minecraft:logs" }
}
```

**Grid (rows of items):**
```json
{
  "display": {
    "type": "modopedia:grid",
    "columns": 4,
    "padding": 16,
    "centered": true,
    "displays": [ ...list of display objects... ]
  }
}
```

**Rings (concentric circles of items):**
```json
{
  "display": {
    "type": "modopedia:rings",
    "radius": 30,
    "count": 6,
    "offset": 8,
    "displays": [ ...list of display objects... ]
  }
}
```

---

### `modopedia:showcase`
Rotating 3D item model render.
```json
{
  "type": "modopedia:showcase",
  "x": 0, "y": 0,
  "items": [{"id": "minecraft:diamond_sword"}],
  "width": 100,
  "height": 100,
  "scale": 1.0
}
```
Multiple items cycle automatically.

---

### `modopedia:entity`
Rotating live entity render.
```json
{
  "type": "modopedia:entity",
  "x": 0, "y": 0,
  "entity": "minecraft:zombie",
  "tag": "{}",
  "width": 100,
  "height": 100,
  "offset_y": 0
}
```

---

### `modopedia:crafting_grid`
Visual crafting grid pulled from book texture. Position only, no other fields.
```json
{
  "type": "modopedia:crafting_grid",
  "x": 0, "y": 0
}
```

### `modopedia:crafting_arrow`
Arrow graphic for crafting display.
```json
{ "type": "modopedia:crafting_arrow", "x": 60, "y": 10 }
```

### `modopedia:crafting_flame`
Flame graphic for smelting display.
```json
{ "type": "modopedia:crafting_flame", "x": 60, "y": 10 }
```

---

### `modopedia:large_frame` / `modopedia:medium_frame` / `modopedia:small_frame`
Decorative frames from the book texture. Position only.
```json
{ "type": "modopedia:large_frame", "x": 0, "y": 0 }
```

---

### `modopedia:multiblock`
Interactive rotating 3D multiblock structure.
```json
{
  "type": "modopedia:multiblock",
  "x": 0, "y": 0,
  "multiblock_id": "mymod:my_structure",
  "width": 100,
  "height": 100,
  "offset_x": 0,
  "offset_y": 0,
  "scale": 1.0,
  "view_angle": 30,
  "no_offsets": false,
  "previewable": true
}
```
- `previewable`: adds a button to preview the structure in-world
- Either `multiblock_id` or inline `multiblock` object; ID takes priority if both present

---

### `modopedia:tooltip`
Invisible hover area that shows tooltip text.
```json
{
  "type": "modopedia:tooltip",
  "x": 10, "y": 0,
  "width": 30,
  "height": 9,
  "tooltip": ["Line one", "Line two"]
}
```

---

## Templates

Templates are reusable component layouts. Define once, reference anywhere.

**Template file** (`assets/<modid>/modopedia/templates/<name>.json`):
```json
{
  "components": [
    { "type": "modopedia:text", "x": 0, "y": 0, "text": "#passthrough" },
    { "type": "modopedia:tooltip", "x": 10, "y": 0, "width": 30, "height": 9, "tooltip": ["Test tooltip"] }
  ]
}
```

**Using a template in an entry:**
```json
{
  "template": "mymod:my_template",
  "my_field": "value passed into template"
}
```

Use `#passthrough` in a template field to pass a value from the entry JSON directly into the component. Built-in templates include `modopedia:headered_text`.

---

## Text Formatting (FString)

Used in `text`, `landing_text`, and any FString field.

| Tag | Effect |
|---|---|
| `$(b)` / `$(/b)` | Bold |
| `$(i)` / `$(/i)` | Italic |
| `$(u)` / `$(/u)` | Underline |
| `$(s)` / `$(/s)` | Strikethrough |
| `$(o)` / `$(/o)` | Obfuscated |
| `$(c:#FF5500)` / `$(/c)` | Color (hex, name, or ID 0-15) |
| `$(f:namespace:font)` / `$(/f)` | Custom font |
| `$(l:https://...)` / `$(/l)` | Clickable link |
| `$(clip:text)` / `$(/clip)` | Copy to clipboard on click |
| `$(cmd:/command)` / `$(/cmd)` | Run command on click |
| `$(t:tooltip text)` / `$(/t)` | Hover tooltip |
| `$(cl:mycategory)` | Navigate to category |
| `$(el:myentry)` | Navigate to entry |
| `$()` | Reset all formatting |

---

## Java API (Advanced — only needed for custom behavior)

| Feature | What it does |
|---|---|
| `PageComponentRegistry#register` | Register a custom rendered component (Client Entrypoint) |
| `BookTypeRegistry#register` | Register a custom book type with its own codec (Common Entrypoint) |
| `BookScreenFactoryRegistry#register` | Register the screen factory for a custom book type (Client Entrypoint) |
| `TextFormatterRegistry#register` | Register a custom FString formatting tag (Client Entrypoint) |
| `TemplateRegistry#registerProcessor` | Register a template processor for dynamic data injection (Client Entrypoint) |

Custom `PageComponent` classes extend `net.favouriteless.modopedia.api.book.page_components.PageComponent` and override `init` (grab lookup values), `render`, and optionally `mouseClicked`.

Custom `TemplateProcessor` implements the functional interface and receives a `MutableLookup` to modify before components are initialized — useful for injecting recipe data, live game state, etc.

---

## Advancement-Based Locking

Both categories and entries support an `"advancement"` field. Modopedia checks `ClientAdvancements.get(id)` + `AdvancementProgress.isDone()` client-side and locks the content until that advancement is complete.

**Category lock:**
```json
{
  "title": "My Category",
  "icon": { "id": "minecraft:book" },
  "entries": ["some_entry"],
  "sort_num": 2,
  "advancement": "ascension:some_advancement"
}
```

**Entry lock:**
```json
{
  "title": "My Entry",
  "icon": { "id": "ascension:some_item" },
  "advancement": "ascension:some_advancement",
  "pages": [...]
}
```

### Toasts are NOT required

Toasts (the pop-up notification) are cosmetic and have nothing to do with modopedia's check. You can:

- **Make a silent hidden advancement** by omitting the `display` block entirely — it won't appear in the advancements screen or show any toast, but modopedia will still see it
- **Award advancements programmatically** from Java via `player.getAdvancements().award(holder, criterionName)` — modopedia has a mixin on `ClientboundUpdateAdvancementsPacket` so the book screen refreshes automatically when an advancement is awarded

**Minimal silent advancement** (`data/ascension/advancements/my_unlock.json`):
```json
{
  "criteria": {
    "unlocked": {
      "trigger": "minecraft:impossible"
    }
  }
}
```
Use `"trigger": "minecraft:impossible"` so it never fires naturally — only your code awards it. Then call `player.getAdvancements().award(holder, "unlocked")` server-side when the condition is met (physique chosen, realm unlocked, etc.).

---

## Known Pitfalls

### Hex colors must NOT have a `#` prefix
`text_colour` and `header_colour` are parsed with `Integer.parseInt(str, 16)`. A leading `#` throws `NumberFormatException` and causes the book to **silently fail to load** — no error in the log, the book just doesn't open.

```json
"text_colour": "3B2A1A"    ✓ correct
"text_colour": "#3B2A1A"   ✗ breaks the book silently
```

### The `creative_tab` field causes a duplicate item in the creative tab
If you have a custom Java item that opens the book AND you set `"creative_tab"` in the book JSON, Modopedia also adds its own `modopedia:book` item to that tab. Remove `"creative_tab"` from the JSON and register the tab slot in Java only.

### Modopedia must be on the compile classpath
`localRuntime` alone puts Modopedia on the runtime classpath but NOT the compile classpath, so any Java class that imports from `net.favouriteless.modopedia.client` will fail to compile. Always add both:
```groovy
compileOnly "net.favouriteless.modopedia:modopedia-neoforge-${minecraft_version}:${modopedia_version}"
localRuntime "net.favouriteless.modopedia:modopedia-neoforge-${minecraft_version}:${modopedia_version}"
```

### Text does NOT automatically flow to the next page
There is no automatic overflow. If a text component's content is too tall for the page, it renders beyond the page boundary straight to the bottom of the screen. You must either keep text within the limit or manually split it across multiple pages in the `pages` array.

---

## Page Layout Reference

The default text component width is **114px** and line height is **9px**. Minecraft's default font averages ~5–6px per character, giving roughly **19–22 characters per line**.

### Item page layout (item_gallery + header + separator + text)
```
y=2   item_gallery (16×16 icon)
y=22  header
y=34  separator
y=42  text starts here
```
Text space from y=42 to the page bottom is approximately **9 lines**.  
**Safe text limit: ~130 characters, no `\n` line breaks.**

If content is longer, split into two pages:
- Page 1: item_gallery + header + separator + one short sentence
- Page 2: header + separator + text at y=20 (text-only layout, ~12 lines available)

### Text-only page layout (header + separator + text)
```
y=0   header
y=12  separator
y=20  text starts here
```
Text space from y=20 is approximately **12 lines**.  
**Safe text limit: ~250 characters. Use single `\n` between paragraphs, not `\n\n`.**

### Line break rules
- `\n` — explicit line break (no blank gap between paragraphs). Use this.
- `\n\n` — blank line between paragraphs. Costs an extra line per use. Avoid on item pages; use sparingly on text-only pages.

---

## What Requires No Java vs. What Needs Java

| Task | Java needed? |
|---|---|
| Book with text, images, items | No |
| Crafting recipe display | No |
| Entity/item showcases | No |
| Multiblock display | No |
| Custom book texture/colors | No |
| Advancement-locked entries | No |
| Custom component renderer | Yes |
| Custom book screen layout | Yes |
| Custom FString tags | Yes |
| Dynamic data in templates | Yes |
