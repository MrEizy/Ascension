#version 330

#moj_import <minecraft:globals.glsl>
#moj_import <ascension:void_space.glsl>
#moj_import <ascension:stone_plates.glsl>
#moj_import <ascension:water_pixels.glsl>
#moj_import <ascension:ice_crystal.glsl>
#moj_import <ascension:qi_impact.glsl>
#moj_import <ascension:life_vines.glsl>
#moj_import <ascension:molten_gold.glsl>

// Sword Qi: redraws a held item's own quads. Sampler0 is the item's atlas and auraUv its atlas UV, so the item's
// alpha masks every effect to the blade's pixels. STYLE is set per pipeline: 0 starfield, 1 echo, 2 stone, 3 water, 4 lightning,
// 5 frost, 6 gale, 7 molten, 8 heat, 9 pure qi, 10 time (the echo's quiet look), 11 life.
// The vertex red channel is the painted mask (SwordQiRenderer.submitMasked): 1 on pixels the coating covers, 0 on the rest,
// which show the plain item.
// Handheld sprites run from the handle at the bottom left to the tip at the top right, and atlas y grows down, so x - y runs
// toward the tip and x + y across the blade.
uniform sampler2D Sampler0;

in vec2 auraUv;
in vec4 vertexColor;
// Where this fragment sits on the item's model, in model pixels (sword_qi.vsh); every look lays its pattern out in it, so
// patterns keep their size on 3D weapon models whose faces stretch a few texture pixels a long way.
in vec2 patternPx;

out vec4 fragColor;

#if STYLE == 5
// Frostbite Edge breaking, the blade's version of the ice coatings' iceBreak (same timeline): two carved cracks run across the
// blade, the piece between them shakes loose and breaks off, the bare blade shows, and frost creeps back in from both cracks.
// Measured in item pixels along the blade; a 16px sprite's diagonal runs FROST_ITEM_PIXELS of them.
const float FROST_ITEM_PIXELS = 30.0;
// The piece is FROST_PIECE_MIN to FROST_PIECE_MAX pixels long, centred FROST_BREAK_MIN to FROST_BREAK_MAX of the way to the tip.
const float FROST_PIECE_MIN = 2.0;
const float FROST_PIECE_MAX = 4.0;
const float FROST_BREAK_MIN = 0.35;
const float FROST_BREAK_MAX = 0.8;

// Where crack `k` (0 near the hilt, 1 near the tip) crosses the blade at `across` (pixels across the blade, in pairs): its
// distance along the blade, wandering a pixel either way so the cracks are jagged.
float frostCrackAlong(float centre, float halfLength, float across, float seed, float k) {
    float wander = floor((iceHash(vec2(floor(across / 2.0), seed + k * 3.1)) - 0.5) * 2.0 + 0.5);
    return centre + (k < 0.5 ? -halfLength : halfLength) + wander;
}

// What breaking does at `alongPx` (pixels along the blade, with the texel's place inside its pixel) and `across`, for a break
// `age` seconds old: vec4(0) for nothing, ICE_HOLE for the bare blade, or a colour to draw.
vec4 frostBreak(float alongPx, float across, vec2 texel, float age, float seed) {
    if (age < 0.0 || age >= BREAK_END) {
        return vec4(0.0);
    }
    float centre = mix(FROST_BREAK_MIN, FROST_BREAK_MAX, iceHash(vec2(seed, 1.7))) * FROST_ITEM_PIXELS;
    float halfLength = mix(FROST_PIECE_MIN, FROST_PIECE_MAX, iceHash(vec2(seed, 2.9))) * 0.5;
    float fromA = alongPx - frostCrackAlong(centre, halfLength, across, seed, 0.0);
    float fromB = frostCrackAlong(centre, halfLength, across, seed, 1.0) - alongPx;
    if (fromA > 0.5 && fromB > 0.5 && age >= CRACK_SPREAD) {
        float inside = min(fromA, fromB);
        if (age < SHARD_BREAK) {
            return inside < 1.0 && mod(floor(age * SHARD_SHAKE_FPS), 2.0) == 0.0 ? ICE_RIM : vec4(0.0);
        }
        float refill = (age - SHARD_BREAK) / (SHARD_REFILLED - SHARD_BREAK) * (halfLength + 0.5);
        if (inside < refill) {
            return vec4(0.0);
        }
        return inside < refill + 0.4 ? ICE_RIM : ICE_HOLE;
    }
    // The cracks spread across the blade, then heal texel by texel.
    if (iceHash(vec2(across, seed + 5.5)) >= age / CRACK_SPREAD) {
        return vec4(0.0);
    }
    float healing = (age - CRACK_HEAL) / (BREAK_END - CRACK_HEAL);
    if (healing > 0.0 && iceHash(texel + seed * 0.13) < healing) {
        return vec4(0.0);
    }
    float d = min(abs(fromA), abs(fromB));
    if (d < 0.25) {
        return CRACK_GROOVE;
    }
    // Lit on the side toward the tip.
    return (fromA >= 0.25 && fromA < 0.5) || (-fromB >= 0.25 && -fromB < 0.5) ? CRACK_LIT_EDGE : vec4(0.0);
}
#endif

#if STYLE == 4
// Charged Blade: the lightning hand coating's racing bolts (qi_coating.fsh lightningStreaks) climbing the blade from the hilt to
// the tip. On the texel grid x - y runs along the blade (rows, 0 at the bottom left) and x + y across it (columns, 0
// down a 16px sprite's diagonal, where blades sit). Bolts start near the diagonal and barely slant, so they run up the blade rather than off it.
// The timing, head, tail, afterflash, forks and strobe frames are the hand's; keep them in step with qi_coating.fsh.
const float BLADE_BOLT_SUBDIVIDE = 3.0;
const float BLADE_BOLT_ROWS = 32.0 * BLADE_BOLT_SUBDIVIDE;
// Rows between a bolt's zigzag corners, how far either side of the diagonal (columns) it starts, how far each corner may sit
// off its line (times chaos), and its most slant (columns per row). A pixel across the blade is about four columns.
const float BLADE_BOLT_KNOT_ROWS = 4.0;
const float BLADE_BOLT_START = 1.5;
const float BLADE_BOLT_JITTER = 2.0;
const float BLADE_BOLT_SLANT = 0.03;
// Columns either side of a bolt's line that are its white core, and its blue glow.
const float BLADE_BOLT_CORE = 1.0;
const float BLADE_BOLT_GLOW = 2.5;
const vec4 BLADE_BOLT_CORE_COLOR = vec4(0.95, 0.97, 1.00, 0.95);
const vec4 BLADE_BOLT_GLOW_COLOR = vec4(0.50, 0.72, 1.00, 0.70);
const vec4 BLADE_BOLT_AFTER_COLOR = vec4(0.42, 0.58, 1.00, 0.45);
// The hand's streak settings (STREAK_* and CRACKLE_*). Rows run diagonally here, about 0.7 texels each, so the head and
// afterflash span a row more.
const float BLADE_STREAK_FPS = 20.0;
const float BLADE_STREAK_PERIOD = 0.75;
const float BLADE_STREAK_ACTIVE = 0.4;
const float BLADE_STREAK_LENGTH = 0.5;
const float BLADE_STREAK_HEAD_ROWS = 3.0;
const float BLADE_STREAK_TAIL = 0.3;
const float BLADE_STREAK_AFTER_ROWS = 4.0;
const float BLADE_STREAK_FORK_CHANCE = 0.5;
const float BLADE_STREAK_STROBE_CHANCE = 0.12;
const int BLADE_BOLT_COUNT = 3;
const float BLADE_BOLT_SPEED = 1.4;
const float BLADE_BOLT_CHAOS = 1.6;

float bladeBoltHash(vec3 p) {
    return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453);
}

// Column of a bolt at `row`: a slanted line from (startCol, startRow), its corners every BLADE_BOLT_KNOT_ROWS rows pushed up
// to jitter / 2 columns off it. The first corner sits on the line, so a fork starts on its parent.
float bladeBoltColumn(float row, vec3 seed, float startCol, float startRow, float slope, float jitter) {
    float k = max(0.0, row - startRow) / BLADE_BOLT_KNOT_ROWS;
    float i = floor(k);
    float a = i < 0.5 ? 0.0 : (bladeBoltHash(seed + vec3(i, 0.0, 0.0)) - 0.5) * jitter;
    float b = (bladeBoltHash(seed + vec3(i + 1.0, 0.0, 0.0)) - 0.5) * jitter;
    return startCol + (row - startRow) * slope + mix(a, b, k - i);
}

// How a cell (column, row) sits on one bolt running `len` rows up from startRow: 2 on its core, 1 on its glow, 0 off it.
float bladeBoltTexel(vec2 cell, vec3 seed, float startCol, float startRow, float slope, float len, float jitter) {
    if (cell.y < startRow || cell.y > startRow + len) {
        return 0.0;
    }
    float d = abs(cell.x - bladeBoltColumn(cell.y, seed, startCol, startRow, slope, jitter));
    return d < BLADE_BOLT_CORE ? 2.0 : d < BLADE_BOLT_GLOW ? 1.0 : 0.0;
}

// The hand's lightningStreaks on the blade: BLADE_BOLT_COUNT bolts racing from the hilt to the tip, each a jagged line with a
// white head, a breaking-up tail, a dim afterflash behind it and now and then a fork, plus strobe frames where all of it
// flashes white. Transparent where no bolt is.
vec4 bladeBolts(vec2 cell, float t) {
    float chaos = BLADE_BOLT_CHAOS;
    float fps = BLADE_STREAK_FPS * chaos;
    float frame = floor(t * fps);
    float tq = frame / fps;
    float jitter = BLADE_BOLT_JITTER * chaos;
    float spread = 0.4 * chaos;
    float lengthRows = floor(BLADE_STREAK_LENGTH * BLADE_BOLT_ROWS);
    float best = 0.0;
    float after = 0.0;
    for (int b = 0; b < BLADE_BOLT_COUNT; b++) {
        vec3 base = vec3(float(b) * 13.1, 0.0, 5.3);
        float period = BLADE_STREAK_PERIOD / BLADE_BOLT_SPEED * (1.0 - spread * 0.5 + spread * bladeBoltHash(base + 0.7));
        float cycleTime = tq / period + bladeBoltHash(base + 1.9);
        float climb = fract(cycleTime) / BLADE_STREAK_ACTIVE;
        if (climb > 1.0) {
            continue;
        }
        // The route for this climb, and the zigzag for this frame.
        vec3 route = base + vec3(0.0, floor(cycleTime) * 3.7, 0.0);
        vec3 zigzag = route + vec3(0.0, 0.0, frame * 1.37);
        float startCol = (bladeBoltHash(route + 1.1) - 0.5) * 2.0 * BLADE_BOLT_START;
        float slope = (bladeBoltHash(route + 2.3) - 0.5) * 2.0 * BLADE_BOLT_SLANT;
        float headRow = floor(mix(-1.0, BLADE_BOLT_ROWS + lengthRows, climb));
        float tailRow = headRow - lengthRows;
        float hit = bladeBoltTexel(cell, zigzag, startCol, 0.0, slope, BLADE_BOLT_ROWS, jitter);
        if (hit > 0.0 && cell.y <= headRow && cell.y >= tailRow) {
            float fromTail = (cell.y - tailRow) / lengthRows;
            if (headRow - cell.y < BLADE_STREAK_HEAD_ROWS) {
                hit = 2.0;
            } else if (fromTail < BLADE_STREAK_TAIL) {
                // The tail: glow only, breaking up toward its end.
                hit = bladeBoltHash(vec3(cell, frame * 0.61)) < fromTail / BLADE_STREAK_TAIL ? 1.0 : 0.0;
            }
            best = max(best, hit);
        } else if (hit >= 2.0 && cell.y < tailRow && cell.y >= tailRow - BLADE_STREAK_AFTER_ROWS) {
            after = 2.0;
        }
        // A fork snapping off the body, slanting off toward one edge of the blade.
        if (bladeBoltHash(zigzag + 8.8) < BLADE_STREAK_FORK_CHANCE * chaos) {
            float forkRow = floor(mix(tailRow + lengthRows * BLADE_STREAK_TAIL, headRow - BLADE_STREAK_HEAD_ROWS,
                    bladeBoltHash(zigzag + 5.3)));
            if (forkRow >= 0.0 && forkRow < BLADE_BOLT_ROWS) {
                float forkCol = bladeBoltColumn(forkRow, zigzag, startCol, 0.0, slope, jitter);
                float forkSlope = slope + (bladeBoltHash(zigzag + 6.1) < 0.5 ? -0.6 : 0.6);
                float fork = bladeBoltTexel(cell, zigzag + vec3(0.0, 0.0, 41.0), forkCol, forkRow, forkSlope,
                        min(4.0 + floor(4.0 * chaos * bladeBoltHash(zigzag + 7.7)), BLADE_BOLT_ROWS - forkRow), jitter);
                best = max(best, fork);
            }
        }
    }
    bool strobe = bladeBoltHash(vec3(3.1, frame * 0.37, 9.4)) < BLADE_STREAK_STROBE_CHANCE * (chaos - 1.0);
    if (best >= 2.0 || strobe && (best >= 1.0 || after >= 2.0)) {
        return BLADE_BOLT_CORE_COLOR;
    }
    if (best >= 1.0) {
        return BLADE_BOLT_GLOW_COLOR;
    }
    return after >= 2.0 ? BLADE_BOLT_AFTER_COLOR : vec4(0.0);
}
#endif

// ---- Looks that spread up the blade (SPREAD data: STYLE 5, 7, 8 and 11) ----
// Their vertex colour (SwordQiRenderer.frostColor): the painted mask in red's top bit and the last hit's seed in its low 7 bits,
// how far the look has spread in green, how far along the item this pixel sits in blue, and how long ago the last hit was in
// alpha (as a share of IMPACT_SECONDS; 1 if not recently).
float spreadCoated() {
    return step(128.0, floor(vertexColor.r * 255.0 + 0.5));
}

float spreadHitSeed() {
    return mod(floor(vertexColor.r * 255.0 + 0.5), 128.0);
}

// Seconds since the last hit, or -1 if not recently.
float spreadHitAge() {
    return vertexColor.a < 1.0 ? vertexColor.a * IMPACT_SECONDS : -1.0;
}

// How far along the item (0 at the hilt, 1 at the tip) the spread has reached at this pixel, `progress` (0 to 1) of the way
// there, the front wandering up to `jag` either way per pixel. `frame` re-rolls the wander, so a front given the frame number
// crawls while it spreads (pass 0 once it has finished).
float spreadFront(float progress, vec2 pixel, float jag, float frame) {
    return progress * (1.0 + jag * 2.0) - jag + (iceHash(pixel * 0.73 + frame) - 0.5) * 2.0 * jag;
}

// Hit sparks (the Molten Edge's and the Heated Blade's): SPREAD_SPARK_CHANCE of the pixels flash for SPREAD_SPARK_SECONDS after
// a hit, re-rolled SPREAD_SPARK_FPS times a second.
const float SPREAD_SPARK_SECONDS = 0.35;
const float SPREAD_SPARK_CHANCE = 0.1;
const float SPREAD_SPARK_FPS = 16.0;

bool spreadSpark(vec2 pixel, float hitSeed, float hitAge) {
    return hitAge >= 0.0 && hitAge < SPREAD_SPARK_SECONDS
            && iceHash(pixel + vec2(hitSeed * 1.31, floor(hitAge * SPREAD_SPARK_FPS) * 0.73)) < SPREAD_SPARK_CHANCE;
}

void main() {
    vec4 item = textureLod(Sampler0, auraUv, 0.0);
    if (item.a < 0.1) {
        discard;
    }
    float seconds = GameTime * 1200.0;

#if STYLE == 0
    // A blade-shaped window into space, with a whisper of the item's own shading so its form still reads.
    vec3 color = voidSpace(seconds) + item.rgb * 0.08;
    fragColor = vec4(mix(item.rgb, color, vertexColor.r), vertexColor.a);
#elif STYLE == 1
    // Violet ghost of the coated pixels only (the plain item is drawn as normal under it); horizontal bands tear brighter for a
    // frame at a time. The tint is SwordQiRenderer.ECHO_TINT's colour.
    if (vertexColor.r < 0.5) {
        discard;
    }
    const vec3 ECHO_TINT = vec3(0.725, 0.549, 1.0);
    float tick = floor(seconds * 11.0);
    float glitch = step(0.75, fract(sin(dot(vec2(floor(gl_FragCoord.y / 6.0), tick), vec2(127.1, 311.7))) * 43758.5453));
    vec3 color = mix(item.rgb * ECHO_TINT, vec3(0.90, 0.80, 1.00), glitch * 0.6);
    fragColor = vec4(color, vertexColor.a * (0.6 + glitch * 0.3));
#elif STYLE == 2
    // Earthbound Blade: the earth hand coating's cobbles (textures/qi_coating/earth_qi_coating.png) over the item, opaque, at the
    // texture's density of STONE_SUBDIVIDE x STONE_SUBDIVIDE texels per item pixel so the stones match the hands' in size.
    // Each cobble is one of four browns, its top-left rim lit and bottom-right rim shaded a little toward the texture's
    // highlight and shadow, with a few faint speckles, between dark seams. The pixels on the sprite's edge (next to a see-through
    // pixel) are darkened so its shape still reads, whatever the item's own colours; the colours are the texture's.
    const float STONE_SUBDIVIDE = 3.0;
    const float STONE_TEXELS_PER_COBBLE = 5.5;
    const float STONE_SEAM = 0.26;
    const float STONE_RIM = 0.38;
    const float STONE_RIM_AMOUNT = 0.55;
    const float STONE_OUTLINE_SHADE = 0.62;
    const vec3 STONE_SEAM_COLOR = vec3(0.239, 0.161, 0.102);
    const vec3 STONE_LIT = vec3(0.612, 0.427, 0.286);
    const vec3 STONE_SHADE = vec3(0.337, 0.239, 0.157);
    vec2 texel = floor(patternPx * STONE_SUBDIVIDE);
    vec2 id;
    vec2 toCentre;
    float edge;
    stonePlateCell((texel + 0.5) / STONE_TEXELS_PER_COBBLE, 4096.0, id, toCentre, edge);
    float tone = stonePlateHash(id + 2.9);
    vec3 stone = tone < 0.25 ? vec3(0.384, 0.271, 0.180)
               : tone < 0.55 ? vec3(0.471, 0.329, 0.220)
               : tone < 0.80 ? vec3(0.518, 0.365, 0.243)
               : vec3(0.553, 0.388, 0.259);
    if (edge < STONE_RIM) {
        // Atlas y grows down the sprite, so the top-left rim is the lit one.
        float facing = dot(normalize(-toCentre + 1e-4), normalize(vec2(-0.7, -1.0)));
        stone = facing > 0.35 ? mix(stone, STONE_LIT, STONE_RIM_AMOUNT)
              : facing < -0.35 ? mix(stone, STONE_SHADE, STONE_RIM_AMOUNT) : stone;
    }
    float speckle = stonePlateHash(texel + 1.7);
    stone *= speckle < 0.08 ? 0.92 : speckle > 0.94 ? 1.06 : 1.0;
    vec3 color = edge < STONE_SEAM ? STONE_SEAM_COLOR : stone;
    vec2 stoneStep = 1.0 / vec2(textureSize(Sampler0, 0));
    float rim = min(
            min(textureLod(Sampler0, auraUv + vec2(stoneStep.x, 0.0), 0.0).a, textureLod(Sampler0, auraUv - vec2(stoneStep.x, 0.0), 0.0).a),
            min(textureLod(Sampler0, auraUv + vec2(0.0, stoneStep.y), 0.0).a, textureLod(Sampler0, auraUv - vec2(0.0, stoneStep.y), 0.0).a));
    if (rim < 0.1) {
        color *= STONE_OUTLINE_SHADE;
    }
    fragColor = vec4(mix(item.rgb, color, vertexColor.r), vertexColor.a);
#elif STYLE == 3
    // Flowing Blade: the water qi coatings' pixel water running from the hilt to the tip in sloped bands, in a grid of
    // WATER_SUBDIVIDE x WATER_SUBDIVIDE texels per item pixel.
    const float WATER_SUBDIVIDE = 2.0;
    // Band length along the blade in texels, bands per second, and how far each band's front wobbles (texels).
    const float WATER_BAND = 9.0;
    const float WATER_SPEED = 0.9;
    const float WATER_WOBBLE = 1.5;
    // How strongly the water covers the item; below 1 so the blade shows through.
    const float WATER_COVER = 0.8;
    vec2 texel = floor(patternPx * WATER_SUBDIVIDE);
    float frame = floor(seconds * WATER_FPS);
    float tq = frame / WATER_FPS;
    float along = texel.x - texel.y + floor(sin((texel.x + texel.y) * 0.6 + tq * 4.0) * WATER_WOBBLE);
    float band = fract(along / WATER_BAND - tq * WATER_SPEED);
    vec4 water = band < 0.14 ? (waterHash(texel + frame * 0.37) > 0.5 ? WATER_FOAM : WATER_LIGHT)
               : band < 0.5 ? WATER_MID
               : vec4(WATER_DEEP.rgb, 0.4);
    if (waterHash(texel + frame * 0.61) > 0.97) {
        water = WATER_FOAM;
    }
    fragColor = vec4(mix(item.rgb, water.rgb, water.a * WATER_COVER * vertexColor.r), vertexColor.a);
#elif STYLE == 4
    // Charged Blade: the hand coating's racing bolts up the blade (bladeBolts), over the plain item. The red channel fades it in
    // with the gather (PLAIN data).
    vec2 texel = floor(patternPx * BLADE_BOLT_SUBDIVIDE);
    // Texel centres on a 16px sprite's diagonal add up to 16 pixels, so column 0 runs down it.
    vec2 cell = vec2(texel.x + texel.y + 1.0 - 16.0 * BLADE_BOLT_SUBDIVIDE, texel.x - texel.y + 16.0 * BLADE_BOLT_SUBDIVIDE);
    vec4 charge = bladeBolts(cell, seconds);
    fragColor = vec4(mix(item.rgb, charge.rgb, charge.a * vertexColor.r), vertexColor.a);
#elif STYLE == 5
    // Frostbite Edge: the ice qi coatings' ice sheet climbs the blade from the hilt to the tip when it comes on, behind a
    // ragged white edge, in a grid of FROST_SUBDIVIDE x FROST_SUBDIVIDE texels per item pixel, and glows faintly like the
    // hand coatings' ice. A swing or hit breaks a piece off it (frostBreak), and now and then one breaks off on its own.
    // Vertex colour: the spreading looks', with g how far the frost has climbed. The result is drawn opaque.
    const float FROST_SUBDIVIDE = 3.0;
    // How far the frost's leading edge wanders (share of the item's length), and the frame rate it crawls at while climbing.
    const float FROST_JAG = 0.08;
    const float FROST_FPS = 8.0;
    // Share of the length the white leading edge spans, and the loose rime just ahead of it.
    const float FROST_RIM = 0.03;
    const float FROST_RIME_AHEAD = 0.05;
    // How strongly the ice covers the item; below 1 so the blade shows through.
    const float FROST_COVER = 0.85;
    // Idle breaks: every FROST_IDLE_PERIOD seconds, FROST_IDLE_CHANCE of the time, a piece breaks off on its own.
    const float FROST_IDLE_PERIOD = 10.0;
    const float FROST_IDLE_CHANCE = 0.5;
    vec2 texel = floor(patternPx * FROST_SUBDIVIDE);
    vec2 pixel = floor(patternPx);
    float coated = spreadCoated();
    float impactSeed = spreadHitSeed();
    float impactAge = spreadHitAge();
    float progress = vertexColor.g;
    float along = vertexColor.b;
    float frame = floor(seconds * FROST_FPS);
    float eased = 1.0 - (1.0 - progress) * (1.0 - progress);
    float front = spreadFront(eased, pixel, FROST_JAG, progress < 0.999 ? frame : 0.0);
    vec4 ice;
    if (along > front) {
        ice = along < front + FROST_RIME_AHEAD && iceHash(texel + frame * 0.31) < 0.25 ? ICE_RIME_SPECK : vec4(0.0);
    } else if (along > front - FROST_RIM) {
        ice = ICE_RIM;
    } else {
        // The atlas doesn't wrap, so any period that is a multiple of the patch and streak sizes will do.
        ice = iceSheet(texel, 4096.0, seconds);
        float breakAge = impactAge;
        float breakSeed = impactSeed;
        if (breakAge < 0.0 || breakAge >= BREAK_END) {
            float cycle = seconds / FROST_IDLE_PERIOD;
            vec2 idleId = vec2(floor(cycle), 7.7);
            if (iceHash(idleId) < FROST_IDLE_CHANCE) {
                breakAge = fract(cycle) * FROST_IDLE_PERIOD;
                breakSeed = floor(iceHash(idleId + 1.3) * 128.0);
            }
        }
        vec2 sub = texel - pixel * FROST_SUBDIVIDE;
        float alongPx = along * FROST_ITEM_PIXELS + ((sub.x - sub.y) / (2.0 * FROST_SUBDIVIDE));
        vec4 broken = frostBreak(alongPx, pixel.x + pixel.y, texel, breakAge, breakSeed);
        if (broken.a < 0.0) {
            // Broken off: the bare blade shows.
            ice = vec4(0.0);
        } else if (broken.a > 0.0) {
            ice = broken;
        }
        if (ice.a > 0.0) {
            ice.rgb += iceGlow(texel);
        }
    }
    fragColor = vec4(mix(item.rgb, ice.rgb, ice.a * FROST_COVER * coated), 1.0);
#elif STYLE == 6
    // Gale Edge: a faint pale wind wash over the blade, with crescent wind blades spiralling round it from the hilt to the tip,
    // in a grid of GALE_SUBDIVIDE x GALE_SUBDIVIDE texels per item pixel, stepping GALE_FPS times a second. The item is a flat
    // sprite, so the spin is faked: each wind blade is a slanted, bowed stripe sliding toward the tip, so at any one spot it
    // sweeps across the blade from one edge to the other, bright as it comes in and fading as it leaves, as if it went round the
    // back. Vertex colour (SwordQiRenderer.galeColor): r the painted mask, g how far
    // across the item the pixel sits (0.5 down its middle), b how far along it.
    const float GALE_SUBDIVIDE = 2.0;
    const float GALE_FPS = 12.0;
    // A 16px sprite's diagonal is this many pixels each way (as FROST_ITEM_PIXELS).
    const float GALE_ITEM_PIXELS = 30.0;
    // Pixels along the blade from one wind blade to the next, how far a blade leans along the item per pixel across it, how much
    // it bows back at its ends, and how many blades pass a spot each second.
    const float GALE_PITCH = 7.0;
    const float GALE_SLANT = 1.2;
    const float GALE_BOW = 0.35;
    const float GALE_SPEED = 1.6;
    // Share of blades left out so the spiral isn't perfectly even, a blade's bright core and the trail behind it (shares of the
    // pitch, about one and two texels), and how far either side of the middle (pixels) the blade fades over as it goes round.
    const float GALE_GAP = 0.25;
    const float GALE_CORE = 0.08;
    const float GALE_TRAIL = 0.16;
    const float GALE_HALF_WIDTH = 2.0;
    // Light green, like the wind rings in qi_coating.fsh.
    const vec4 GALE_BLADE = vec4(0.82, 1.00, 0.84, 0.9);
    const vec4 GALE_BLADE_TRAIL = vec4(0.62, 0.93, 0.68, 0.55);
    const vec4 GALE_WASH = vec4(0.52, 0.86, 0.60, 0.16);
    vec2 texel = floor(patternPx * GALE_SUBDIVIDE);
    float frame = floor(seconds * GALE_FPS);
    float alongPx = (texel.x - texel.y) / GALE_SUBDIVIDE;
    float acrossPx = (texel.x + texel.y) / GALE_SUBDIVIDE;
    // From the middle of the item, snapped to the texel grid.
    float fromMiddle = floor((vertexColor.g - 0.5) * GALE_ITEM_PIXELS * GALE_SUBDIVIDE + 0.5) / GALE_SUBDIVIDE;
    float phase = (alongPx + acrossPx * GALE_SLANT - fromMiddle * fromMiddle * GALE_BOW) / GALE_PITCH - frame / GALE_FPS * GALE_SPEED;
    float bladeId = floor(phase);
    float f = phase - bladeId;
    vec4 gale = GALE_WASH;
    if (waterHash(vec2(bladeId, 2.3)) >= GALE_GAP && (f < GALE_CORE || f > 1.0 - GALE_TRAIL)) {
        gale = f < GALE_CORE ? GALE_BLADE : GALE_BLADE_TRAIL;
        // Comes in bright on the low-across side and fades in three steps as it crosses to the high side.
        float crossed = clamp((fromMiddle + GALE_HALF_WIDTH) / (2.0 * GALE_HALF_WIDTH), 0.0, 1.0);
        gale.a = max(gale.a * (1.0 - floor(crossed * 3.0) / 3.0 * 0.85), GALE_WASH.a);
    }
    float coated = step(0.5, vertexColor.r);
    fragColor = vec4(mix(item.rgb, gale.rgb, gale.a * coated), vertexColor.a);
#elif STYLE == 7
    // Molten Edge: the metal skin coatings (qi_coating_skin.fsh) on the blade. When it comes on, metal qi pours up from the hilt
    // to the tip as molten gold behind a ragged front, the MOLTEN_SPAN of the item behind the front glowing white, yellow, orange
    // then red, and that glow shrinks back into the front over the last MOLTEN_COOL of the pour as it cools; it melts back down
    // when it goes off. Behind it the blade is tinted gold, keeping each pixel's brightness and a little of its colour. A swing
    // or hit throws sparks off it, with a brief flash. The gold and molten colours come from molten_gold.glsl, shared with qi_coating_skin.fsh.
    // Vertex colour: the spreading looks' (spreadCoated, spreadHitSeed, spreadHitAge), with g how far the pour has got. Its hit
    // sparks are spreadSpark's. The result is drawn opaque.
    const float MOLTEN_JAG = 0.06;
    const float MOLTEN_FPS = 8.0;
    const float MOLTEN_SPAN = 0.18;
    const float MOLTEN_COOL = 0.2;
    const float FLASH_SECONDS = 0.12;
    const float FLASH_SHINE = 0.3;
    vec2 pixel = floor(patternPx);
    float coated = spreadCoated();
    float impactAge = spreadHitAge();
    float progress = vertexColor.g;
    float along = vertexColor.b;
    float frame = floor(seconds * MOLTEN_FPS);
    float eased = 1.0 - (1.0 - progress) * (1.0 - progress);
    float front = spreadFront(eased, pixel, MOLTEN_JAG, progress < 0.999 ? frame : 0.0);
    vec3 color = item.rgb;
    if (along <= front) {
        float span = MOLTEN_SPAN * clamp((1.0 - progress) / MOLTEN_COOL, 0.0, 1.0);
        float behind = front - along;
        if (behind < span) {
            float heat = behind / span;
            color = moltenColour(heat);
        } else {
            color = goldTint(item.rgb);
            if (impactAge >= 0.0 && impactAge < FLASH_SECONDS) {
                color = mix(color, GOLD_SHINE, FLASH_SHINE);
            }
            if (spreadSpark(pixel, spreadHitSeed(), impactAge)) {
                color = vec3(1.0, 1.0, 0.92);
            }
        }
    }
    fragColor = vec4(mix(item.rgb, color, coated), 1.0);
#elif STYLE == 8
    // Heated Blade: fire qi heats the blade like metal in a forge. When it comes on, the heat spreads up from the hilt to the
    // tip behind a white-hot front (like the Molten Edge's pour), and cools back down when it goes off. Behind it the blade
    // glows hotter toward the tip: deep red, orange, yellow, then white-hot past HEAT_YELLOW_TO_WHITE of the item, keeping the
    // blade's own shading, and HEAT_FLICKER of its pixels flicker a step hotter each frame. A swing or hit throws ember sparks.
    // Vertex colour as the Molten Edge's, with g how far the heat has spread. The result is drawn opaque.
    const float HEAT_JAG = 0.06;
    const float HEAT_FPS = 10.0;
    const float HEAT_FRONT = 0.06;
    const float HEAT_RED_TO_ORANGE = 0.45;
    const float HEAT_ORANGE_TO_YELLOW = 0.65;
    const float HEAT_YELLOW_TO_WHITE = 0.85;
    const float HEAT_FLICKER = 0.15;
    const float HEAT_AMOUNT = 0.75;
    const vec3 HEAT_RED = vec3(0.72, 0.12, 0.03);
    const vec3 HEAT_ORANGE = vec3(1.00, 0.45, 0.06);
    const vec3 HEAT_YELLOW = vec3(1.00, 0.80, 0.30);
    const vec3 HEAT_WHITE = vec3(1.00, 0.95, 0.78);
    vec2 pixel = floor(patternPx);
    float coated = spreadCoated();
    float progress = vertexColor.g;
    float along = vertexColor.b;
    float frame = floor(seconds * HEAT_FPS);
    float eased = 1.0 - (1.0 - progress) * (1.0 - progress);
    float front = spreadFront(eased, pixel, HEAT_JAG, progress < 0.999 ? frame : 0.0);
    vec3 color = item.rgb;
    if (along <= front) {
        float heat = along < HEAT_RED_TO_ORANGE ? 0.0 : along < HEAT_ORANGE_TO_YELLOW ? 1.0 : along < HEAT_YELLOW_TO_WHITE ? 2.0 : 3.0;
        if (iceHash(pixel + frame * 0.37) < HEAT_FLICKER) {
            heat += 1.0;
        }
        if (progress < 0.999 && front - along < HEAT_FRONT) {
            heat = 3.0;
        }
        vec3 glow = heat < 1.0 ? HEAT_RED : heat < 2.0 ? HEAT_ORANGE : heat < 3.0 ? HEAT_YELLOW : HEAT_WHITE;
        float lum = dot(item.rgb, vec3(0.299, 0.587, 0.114));
        color = mix(item.rgb, glow * (0.55 + 0.6 * lum), HEAT_AMOUNT);
        if (spreadSpark(pixel, spreadHitSeed(), spreadHitAge())) {
            color = HEAT_WHITE;
        }
    }
    fragColor = vec4(mix(item.rgb, color, coated), 1.0);
#elif STYLE == 9
    // Pure Qi: a film of plain qi over the blade, tinting it the Pure Qi hand coating's pale cyan.
    const vec3 PURE_QI_TINT = vec3(0.45, 0.82, 1.00);
    const float PURE_QI_AMOUNT = 0.3;
    float lum = dot(item.rgb, vec3(0.299, 0.587, 0.114));
    vec3 color = mix(item.rgb, PURE_QI_TINT * (0.6 + 0.5 * lum), PURE_QI_AMOUNT);
    fragColor = vec4(mix(item.rgb, color, vertexColor.r), vertexColor.a);
#elif STYLE == 10
    // Time, between swings: a still film of the echo's violet over the blade. Style 1's tearing copies join it as the player
    // swings.
    const vec3 TIME_TINT = vec3(0.725, 0.549, 1.0);
    const float TIME_AMOUNT = 0.3;
    float lum = dot(item.rgb, vec3(0.299, 0.587, 0.114));
    vec3 color = mix(item.rgb, TIME_TINT * (0.6 + 0.5 * lum), TIME_AMOUNT);
    fragColor = vec4(mix(item.rgb, color, vertexColor.r), vertexColor.a);
#elif STYLE == 11
    // Life's blade: the hand coating's vines (life_vines.glsl) over the coated pixels, laid out on the model's pixels at
    // VINE_BLADE_TEXELS vine texels a pixel. When it comes on they grow from the hilt to the tip behind a ragged front, and draw
    // back down when it goes off; the life light climbs them from the hilt to the tip every LIFE_PULSE_PERIOD seconds. The rest
    // of the item is drawn plain, unlit like every sword look.
    // Vertex colour (SwordQiRenderer.frostColor, with the foliage colour where a hit would go, SwordLook.foliage): r has the
    // painted mask in its top bit; g is how far it has grown; b how far along the item this pixel sits; and the foliage colour
    // under the world's light, 5 bits a channel, is split between r's low 7 bits (low) and a (high). The result is drawn opaque.
    const float VINE_BLADE_TEXELS = 2.0;
    // How far (as a share of the item) the growing front wanders either way, and how far the life light reaches either side
    // of its head.
    const float VINE_BLADE_JAG = 0.06;
    const float VINE_BLADE_PULSE = 0.12;
    float coated = spreadCoated();
    float foliageBits = floor(vertexColor.a * 255.0 + 0.5) * 128.0 + spreadHitSeed();
    vec3 foliage = vec3(floor(foliageBits / 1024.0), mod(floor(foliageBits / 32.0), 32.0), mod(foliageBits, 32.0)) / 31.0;
    float progress = vertexColor.g;
    float along = vertexColor.b;
    vec2 texel = floor(patternPx * VINE_BLADE_TEXELS);
    float front = spreadFront(progress, floor(patternPx), VINE_BLADE_JAG, 0.0);
    int shade = vineShade(int(texel.x), int(texel.y));
    vec3 color = item.rgb;
    if (coated > 0.5 && along <= front && shade > 0) {
        float climb = lifePulseClimb(seconds, 0.0);
        float head = mix(-VINE_BLADE_PULSE, 1.0 + VINE_BLADE_PULSE, climb);
        float glow = climb <= 1.0 ? max(0.0, 1.0 - abs(along - head) / VINE_BLADE_PULSE) : 0.0;
        glow = floor(glow * 3.0 + 0.5) / 3.0;
        color = vineColour(shade, foliage, glow);
    }
    fragColor = vec4(color, 1.0);
#endif
}
