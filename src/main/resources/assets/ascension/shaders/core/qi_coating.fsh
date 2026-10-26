#version 330

#moj_import <minecraft:globals.glsl>
#moj_import <ascension:void_space.glsl>
#moj_import <ascension:water_pixels.glsl>
#moj_import <ascension:wind_gust.glsl>

// UV layout (written by QiCoatingLayer):
//   x = style * 2 + position around the arm perimeter (0..1, wraps seamlessly)
//   y = 0 at the hand tip, 1 at the top of the coating
// Styles are the shell looks' in QiCoatingLooks (with a style per limb for patterns that differ per limb):
// 5 the space hand coating (starfield), 9 the Time hand coating's echo (58 its quiet look), 10-11 whole-body layers (echo,
// shelved; starfield). Styles 6 (lensing), 7 (horizon) and 8 (pull) were removed.
// 14 and 16 are the pixel lightning hand coating and limb layer (15, Lightning Step, uses 49-55).
// 27 is the Serpent Coil coating (arms only) and 28 the Serpent Coil layer (arms and legs).
// 32 is the rings of the Tornado Fist.
// 41-43 are the Serpent Coil layer on the left arm, right leg and left leg, and 44 the coating on the left arm, so each limb runs
// on its own timing. 45 is the lightning hand coating on the left arm and 46-48 the lightning limbs on the left arm, right leg
// and left leg. 49-50 are Lightning Step's right and left legs, 51 its spear flash trail and 52-55 its footprints. 56 is a Sky
// Stride qi plate, and 57 the Pure Qi hand coating.
// Sword qi is drawn by sword_qi.fsh, and the ice, metal and the Tornado Fist's skin wind are painted into the skin itself by
// qi_coating_skin.fsh.
in vec2 auraUv;
in vec4 vertexColor;

out vec4 fragColor;

const float TAU = 6.28318530718;
const float PERIOD = 4.0;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

// Value noise that tiles every `period` cells along x so patterns wrap around the arm without a seam.
float periodicNoise(vec2 p, float period) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 w = f * f * (3.0 - 2.0 * f);
    float x0 = mod(i.x, period);
    float x1 = mod(i.x + 1.0, period);
    float a = hash(vec2(x0, i.y));
    float b = hash(vec2(x1, i.y));
    float c = hash(vec2(x0, i.y + 1.0));
    float d = hash(vec2(x1, i.y + 1.0));
    return mix(mix(a, b, w.x), mix(c, d, w.x), w.y);
}

// p.x must be u * period for the result to wrap.
float fbm(vec2 p, float period) {
    float value = 0.0;
    float amplitude = 0.55;
    for (int octave = 0; octave < 4; octave++) {
        value += periodicNoise(p, period) * amplitude;
        p *= 2.0;
        period *= 2.0;
        amplitude *= 0.5;
    }
    return value;
}

// ---- Space/void looks (styles 5 and 9-11) ----

// Window into space. Stars and nebula are placed in screen space, so they stay put while the hand moves across them.
vec4 voidStarfield(float u, float v, float t) {
    vec3 color = voidSpace(t);

    // Opaque at the fist, thinning out up the forearm into the fading arm.
    float n = fbm(vec2(u * PERIOD, v * 2.0 + t * 0.1), PERIOD);
    float top = 0.92 + (n - 0.5) * 0.15;
    float alpha = 1.0 - smoothstep(top - 0.45, top, v);
    return vec4(color, alpha * 0.97);
}

// Thin violet film with bands that tear sideways, like the hand isn't quite where it should be. The Time hand coating draws it
// with `glitching` 1 (style 9) for a moment after the player is hurt, and 0 (style 58, its quiet look) the rest of the time.
vec4 voidEcho(float u, float v, float t, float glitching) {
    float tick = floor(t * 11.0);
    float glitch = glitching * step(0.75, hash(vec2(floor(v * 10.0), tick)));
    float n = fbm(vec2(u * PERIOD + glitch * 1.3, v * 2.0 - t * 0.5), PERIOD);
    vec3 color = mix(vec3(0.15, 0.05, 0.30), vec3(0.65, 0.45, 1.00), n);
    color = mix(color, vec3(0.90, 0.80, 1.00), glitch * 0.6);
    float top = 0.8 + (n - 0.5) * 0.2;
    float alpha = 1.0 - smoothstep(top - 0.05, top, v);
    return vec4(color, alpha * (0.22 + glitch * 0.35 + n * 0.15));
}

// Whole-body echo layer: the same tearing violet film as voidEcho, but even over the whole part with no top edge.
vec4 voidEchoLayer(float u, float v, float t) {
    float tick = floor(t * 11.0);
    float glitch = step(0.8, hash(vec2(floor(v * 14.0), tick)));
    float n = fbm(vec2(u * PERIOD + glitch * 1.3, v * 2.0 - t * 0.5), PERIOD);
    vec3 color = mix(vec3(0.15, 0.05, 0.30), vec3(0.65, 0.45, 1.00), n);
    color = mix(color, vec3(0.90, 0.80, 1.00), glitch * 0.6);
    return vec4(color, 0.12 + glitch * 0.3 + n * 0.1);
}

// Whole-body starfield: the void shows through at partial strength so the skin underneath stays readable.
vec4 voidStarfieldLayer(float u, float v, float t) {
    return vec4(voidSpace(t), 0.6);
}

// ---- Lightning look prototypes: pixel bolts (styles 14 and 16, plus 45-48 for the other limbs) ----
// Crackling Fist (14, 45 on the left arm) runs up to the elbow; Crackling Limbs (16, 46-48 on the other limbs) runs most of
// the way up the arms and legs. Lightning Step crackles harder around the feet only (49, 50) and leaves crackling
// footprints (52-55). The coatings are bolts racing up each limb (lightningStreaks).

const vec4 BOLT_CORE = vec4(0.95, 0.97, 1.00, 0.95);
const vec4 BOLT_GLOW = vec4(0.50, 0.72, 1.00, 0.70);
const vec4 BOLT_AFTER = vec4(0.42, 0.58, 1.00, 0.45);
const float BOLT_FPS = 10.0;
// Rows between a bolt's zigzag corners, and how many texels each corner may sit off the bolt's line.
const float BOLT_KNOT_ROWS = 3.0;
const float BOLT_JITTER = 4.0;
// Spear flash trail texel grid (along the trail x across it), how far its bolt's corners wander across it, and where along
// the trail (0 at the feet, 1 at the tail) it starts to break up.
const vec2 TRAIL_TEXELS = vec2(64.0, 6.0);
const float TRAIL_JITTER = 2.5;
const float TRAIL_FADE_START = 0.35;
// Texel grids: the hand coating's shell is 8.1px tall, the limbs' 12.8px.
const vec2 CRACKLE_HAND_TEXELS = vec2(48.0, 21.0);
const vec2 CRACKLE_LIMB_TEXELS = vec2(48.0, 33.0);
// How far up the shell the bolts climb.
const float CRACKLE_HAND_TOP = 1.0;
const float CRACKLE_LIMB_TOP = 0.85;

float boltHash(vec3 p) {
    return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453);
}

// Column (in texels around the limb) of a bolt at `row`: a slanted line from (startX, startRow), its corners every
// BOLT_KNOT_ROWS rows pushed off it at random. The first corner sits on the line, so a fork starts on its parent.
float boltColumn(float row, vec3 seed, float startX, float startRow, float slope) {
    float k = max(0.0, row - startRow) / BOLT_KNOT_ROWS;
    float i = floor(k);
    float a = i < 0.5 ? 0.0 : (boltHash(seed + vec3(i, 0.0, 0.0)) - 0.5) * BOLT_JITTER;
    float b = (boltHash(seed + vec3(i + 1.0, 0.0, 0.0)) - 0.5) * BOLT_JITTER;
    return startX + (row - startRow) * slope + mix(a, b, k - i);
}

// How a texel sits on one bolt running `len` rows up from startRow: 2 on its core, 1 on the glow beside it, 0 off it. Each row
// covers every column the bolt crosses within it, so steep zigzags stay unbroken lines. Wraps around `columns`.
float boltTexel(vec2 cell, float columns, vec3 seed, float startX, float startRow, float slope, float len) {
    if (cell.y < startRow || cell.y > startRow + len) {
        return 0.0;
    }
    float x0 = boltColumn(max(startRow, cell.y - 0.5), seed, startX, startRow, slope);
    float x1 = boltColumn(min(startRow + len, cell.y + 0.5), seed, startX, startRow, slope);
    float mid = (x0 + x1) * 0.5;
    float halfSpan = abs(x1 - x0) * 0.5;
    float d = abs(mod(cell.x + 0.5 - mid + columns * 0.5, columns) - columns * 0.5) - halfSpan;
    return d < 0.5 ? 2.0 : d < 1.5 ? 1.0 : 0.0;
}

// Racing lightning: like the water serpents, each limb has `count` bolts that climb from the hand or foot to `top`, but in a fraction of a second.
// Each is a jagged line of texels with a thick white head; its route is set for the whole climb, while its zigzag is re-rolled
// STREAK_FPS times a second so it flickers as it goes. Its tail breaks up, a dim afterflash is left a few rows behind it, and
// now and then a short fork snaps off its body. No film or sparks: only the lines.
const float STREAK_FPS = 20.0;
// Seconds per cycle and the share of it a bolt is climbing (the rest is a pause before the next one).
const float STREAK_PERIOD = 0.75;
const float STREAK_ACTIVE = 0.4;
// Bolt length (share of the height it climbs), how many rows the white head spans, the share of the length at the tail that breaks up, and
// the rows of afterflash behind it.
const float STREAK_LENGTH = 0.5;
const float STREAK_HEAD_ROWS = 2.0;
const float STREAK_TAIL = 0.3;
const float STREAK_AFTER_ROWS = 3.0;
// Chance per frame that a bolt throws a fork.
const float STREAK_FORK_CHANCE = 0.5;
// Bolts per limb, and Lightning Step's (its legs also climb faster, STREAK_STEP_SPEED times).
const int STREAK_COUNT = 2;
const int STREAK_STEP_COUNT = 3;
const float STREAK_STEP_SPEED = 1.5;
// How far up the legs the bolts climb: Crackling Limbs, and Lightning Step (around the feet).
const float STREAK_LEG_TOP = 0.6;
const float STREAK_STEP_TOP = 0.45;

vec4 lightningStreaks(float u, float v, float t, float limb, vec2 texels, float top, int count, float speed) {
    vec2 cell = floor(vec2(u, v) * texels);
    float tq = floor(t * STREAK_FPS) / STREAK_FPS;
    float frame = floor(t * STREAK_FPS);
    float topRows = floor(top * texels.y);
    float lengthRows = max(4.0, floor(STREAK_LENGTH * topRows));
    float best = 0.0;
    float after = 0.0;
    for (int b = 0; b < count; b++) {
        vec3 base = vec3(float(b) * 13.1, 0.0, limb * 7.9);
        float period = STREAK_PERIOD / speed * (0.8 + 0.4 * boltHash(base + 0.7));
        float cycleTime = tq / period + boltHash(base + 1.9);
        float climb = fract(cycleTime) / STREAK_ACTIVE;
        if (climb > 1.0) {
            continue;
        }
        // The route for this climb, and the zigzag for this frame.
        vec3 route = base + vec3(0.0, floor(cycleTime) * 3.7, 0.0);
        vec3 zigzag = route + vec3(0.0, 0.0, frame * 1.37);
        float startX = boltHash(route + 1.1) * texels.x;
        float slope = (boltHash(route + 2.3) - 0.5) * 1.4;
        float headRow = floor(mix(-1.0, topRows + lengthRows, climb));
        float tailRow = headRow - lengthRows;
        float hit = boltTexel(cell, texels.x, zigzag, startX, 0.0, slope, topRows);
        if (hit > 0.0 && cell.y <= headRow && cell.y >= tailRow) {
            float fromTail = (cell.y - tailRow) / lengthRows;
            if (headRow - cell.y < STREAK_HEAD_ROWS) {
                hit = 2.0;
            } else if (fromTail < STREAK_TAIL) {
                // The tail: glow only, breaking up toward its end.
                hit = boltHash(vec3(cell, frame * 0.61)) < fromTail / STREAK_TAIL ? 1.0 : 0.0;
            }
            best = max(best, hit);
        } else if (hit >= 2.0 && cell.y < tailRow && cell.y >= tailRow - STREAK_AFTER_ROWS) {
            after = 2.0;
        }
        // A fork snapping off the body.
        if (boltHash(zigzag + 8.8) < STREAK_FORK_CHANCE) {
            float forkRow = floor(mix(tailRow + lengthRows * STREAK_TAIL, headRow - STREAK_HEAD_ROWS, boltHash(zigzag + 5.3)));
            if (forkRow >= 0.0 && forkRow < topRows) {
                float forkX = boltColumn(forkRow, zigzag, startX, 0.0, slope);
                float forkSlope = slope + (boltHash(zigzag + 6.1) < 0.5 ? -1.0 : 1.0);
                float fork = boltTexel(cell, texels.x, zigzag + vec3(0.0, 0.0, 41.0), forkX, forkRow, forkSlope,
                        min(3.0 + floor(3.0 * boltHash(zigzag + 7.7)), topRows - forkRow));
                best = max(best, fork);
            }
        }
    }
    if (best >= 2.0) {
        return BOLT_CORE;
    }
    if (best >= 1.0) {
        return BOLT_GLOW;
    }
    return after >= 2.0 ? BOLT_AFTER : vec4(0.0);
}


// ---- Sky Stride footholds (56, drawn by SkyStrideClient as small flat squares under the feet, in the world) ----
// A translucent pale-cyan pixel square: a soft notched rim (and an inner ring, on grids big enough for one) over a faint fill,
// with a glint sweeping around the rim and the whole square pulsing softly. The vertex alpha fades it at the end of its life, and texels drop out as it does.
// Texels across a foothold, a little over one per skin pixel.
const float PLATE_TEXELS = 6.0;
const vec4 PLATE_RIM = vec4(0.78, 0.96, 1.00, 0.55);
const vec4 PLATE_RING = vec4(0.55, 0.86, 1.00, 0.55);
const vec4 PLATE_FILL = vec4(0.45, 0.82, 1.00, 0.25);
const vec4 PLATE_GLINT = vec4(1.00, 1.00, 1.00, 0.80);
// Glint trips around the rim per second, and the pulse's rate and strength.
const float PLATE_GLINT_SPEED = 1.5;
const float PLATE_PULSE_SPEED = 3.0;
const float PLATE_PULSE = 0.15;

vec4 qiPlate(float u, float v, float t) {
    vec2 cell = floor(vec2(u, v) * PLATE_TEXELS);
    if (boltHash(vec3(cell, 12.9)) > vertexColor.a * 1.2) {
        return vec4(0.0);
    }
    vec2 p = cell + 0.5 - PLATE_TEXELS * 0.5;
    float ring = max(abs(p.x), abs(p.y));
    float edge = PLATE_TEXELS * 0.5 - 0.5;
    // Notched corners.
    if (abs(p.x) > edge - 0.5 && abs(p.y) > edge - 0.5) {
        return vec4(0.0);
    }
    float tq = floor(t * 12.0) / 12.0;
    float pulse = 1.0 + PLATE_PULSE * sin(tq * PLATE_PULSE_SPEED * TAU);
    if (ring > edge - 0.5) {
        float angle = atan(p.y, p.x) / TAU + 0.5;
        float glint = abs(fract(angle - tq * PLATE_GLINT_SPEED + 0.5) - 0.5);
        return glint < 0.06 ? PLATE_GLINT : vec4(PLATE_RIM.rgb, PLATE_RIM.a * pulse);
    }
    if (abs(ring - (edge - 3.0)) < 0.5) {
        return vec4(PLATE_RING.rgb, PLATE_RING.a * pulse);
    }
    return vec4(PLATE_FILL.rgb, PLATE_FILL.a * pulse);
}

// ---- Pure Qi (style 57) ----
// The element-less coating anyone can use: plain qi, shaped into an even pale cyan film round the hand that softens away up the
// forearm.
const vec4 PURE_QI_FILM = vec4(0.45, 0.82, 1.00, 0.18);

vec4 pureQi(float u, float v, float t) {
    return vec4(PURE_QI_FILM.rgb, PURE_QI_FILM.a * (1.0 - smoothstep(0.45, 0.95, v)));
}

// Lightning Step's footprints (52-55, four variants): a flat square on the ground under a step, with MARK_BOLTS short jagged
// bolts snapping out from a bright centre, re-rolled with the coatings' bolts. QiCoatingLayer fades the vertex alpha from 1 to 0
// over the mark's life, and texels drop out as it does, so it breaks up as it fades.
// Texels across a mark (a foot-sized square, so 2 per skin pixel); the bolt layout below is measured on a 16-texel grid and
// scaled to this one.
const float MARK_TEXELS = 8.0;
const int MARK_BOLTS = 4;
// Texels along a bolt between its zigzag corners, and how far each corner may sit off the bolt's line.
const float MARK_KNOT = 2.0;
const float MARK_JITTER = 1.5;

vec4 lightningMark(float u, float v, float t, float variant) {
    vec2 cell = floor(vec2(u, v) * MARK_TEXELS);
    if (boltHash(vec3(cell, 4.4 + variant)) > vertexColor.a * 1.2) {
        return vec4(0.0);
    }
    vec2 p = (cell + 0.5 - MARK_TEXELS * 0.5) * (16.0 / MARK_TEXELS);
    // Widths below are in 16-grid units, so one of this grid's texels is 16 / MARK_TEXELS of them across.
    if (length(p) < 2.0) {
        return BOLT_CORE;
    }
    float frame = floor(t * BOLT_FPS);
    float best = 0.0;
    float after = 0.0;
    for (int back = 0; back < 2; back++) {
        float hit = 0.0;
        for (int b = 0; b < MARK_BOLTS; b++) {
            vec3 seed = vec3(float(b) * 7.3 + variant * 31.1, (frame - float(back)) * 1.37, 2.2);
            float angle = (float(b) + boltHash(seed)) / float(MARK_BOLTS) * TAU;
            vec2 dir = vec2(cos(angle), sin(angle));
            float along = dot(p, dir);
            float len = 4.0 + 3.5 * boltHash(seed + 1.9);
            if (along < 0.0 || along > len) {
                continue;
            }
            float k = along / MARK_KNOT;
            float i = floor(k);
            float a = i < 0.5 ? 0.0 : (boltHash(seed + vec3(i, 3.0, 0.0)) - 0.5) * 2.0 * MARK_JITTER;
            float c = (boltHash(seed + vec3(i + 1.0, 3.0, 0.0)) - 0.5) * 2.0 * MARK_JITTER;
            float d = abs(dot(p, vec2(-dir.y, dir.x)) - mix(a, c, k - i));
            hit = max(hit, d < 1.0 ? 2.0 : d < 2.6 ? 1.0 : 0.0);
        }
        if (back == 0) {
            best = hit;
        } else {
            after = hit;
        }
    }
    return best >= 2.0 ? BOLT_CORE : best >= 1.0 ? BOLT_GLOW : after >= 2.0 ? BOLT_AFTER : vec4(0.0);
}

// Lightning Step's spear flash trail (51), a ribbon along the path from each foot: u runs from the player's end (0) to the
// trail's tail (1), v across it. One jagged bolt runs its length, re-rolled with the coatings' bolts, breaking up toward the tail.
vec4 lightningTrail(float u, float v, float t) {
    vec2 cell = floor(vec2(u, v) * TRAIL_TEXELS);
    float frame = floor(t * BOLT_FPS);
    float tail = (cell.x + 0.5) / TRAIL_TEXELS.x;
    if (boltHash(vec3(cell, frame * 0.53)) < smoothstep(TRAIL_FADE_START, 1.0, tail)) {
        return vec4(0.0);
    }
    float best = 0.0;
    float after = 0.0;
    for (int back = 0; back < 2; back++) {
        vec3 seed = vec3(19.3, (frame - float(back)) * 1.37, 0.0);
        float k0 = max(0.0, cell.x - 0.5) / BOLT_KNOT_ROWS;
        float k1 = (cell.x + 0.5) / BOLT_KNOT_ROWS;
        float x0 = TRAIL_TEXELS.y * 0.5 + mix((boltHash(seed + vec3(floor(k0), 0.0, 0.0)) - 0.5),
                (boltHash(seed + vec3(floor(k0) + 1.0, 0.0, 0.0)) - 0.5), fract(k0)) * TRAIL_JITTER * 2.0;
        float x1 = TRAIL_TEXELS.y * 0.5 + mix((boltHash(seed + vec3(floor(k1), 0.0, 0.0)) - 0.5),
                (boltHash(seed + vec3(floor(k1) + 1.0, 0.0, 0.0)) - 0.5), fract(k1)) * TRAIL_JITTER * 2.0;
        float d = abs(cell.y + 0.5 - (x0 + x1) * 0.5) - abs(x1 - x0) * 0.5;
        float hit = d < 0.5 ? 2.0 : d < 1.5 ? 1.0 : 0.0;
        if (back == 0) {
            best = hit;
        } else {
            after = hit;
        }
    }
    return best >= 2.0 ? BOLT_CORE : best >= 1.0 ? BOLT_GLOW : after >= 2.0 ? BOLT_AFTER : vec4(0.0);
}

// ---- Water look prototypes: pixel water streams (water_pixels.glsl) ----
// Serpent Coil runs up the arms as a coating (27, 44 on the left arm) and up the arms and legs as a layer (28, 41-43 on the
// other limbs). Each limb has its own shader style so it can run on its own timing and know which side is its outside.

// Serpent Coil: the whole limb (12.8px tall).
const vec2 SERPENT_TEXELS = vec2(48.0, 33.0);
const float SERPENT_TURNS = 1.25;
const float SERPENT_LENGTH = 0.4;
const float SERPENT_HEAD_WIDTH = 10.0;
const float SERPENT_TAIL_WIDTH = 4.0;
// Seconds per climb, and the share of it the serpent is out (the rest is a pause before the next one forms).
const float SERPENT_PERIOD = 4.0;
const float SERPENT_ACTIVE = 0.85;
// Where the serpent has thinned away to nothing, just short of the shoulder or hip, and over how much of the limb below that
// it tapers, so it ends smoothly instead of being cut off.
const float SERPENT_TOP = 0.88;
const float SERPENT_TAPER = 0.24;
// Where each limb's serpent is in its climb at time 0: right arm, left arm, right leg, left leg. QiCoatingLayer's splash
// particles use the same values.
const float SERPENT_LIMB_OFFSETS[4] = float[4](0.0, 0.43, 0.71, 0.19);
// Where around each limb (share of the perimeter) its serpent starts, in the same limb order, so no two limbs look alike.
const float SERPENT_LIMB_PHASES[4] = float[4](0.0, 0.37, 0.61, 0.18);
// Middle of each limb's outer face, in the same limb order: the right limbs' outer side is the last face, the left limbs' the
// second. The pool's foam line runs up it.
const float POOL_OUTER_FACE[4] = float[4](0.875, 0.375, 0.875, 0.375);
// Foam line half-width in texels, and how far it wanders sideways as it runs (share of the perimeter).
const float POOL_FOAM_WIDTH = 1.2;
const float POOL_FOAM_WANDER = 0.025;
// The pool of flowing water around each hand and foot the serpents rise out of: its height (share of the limb), how far its top
// edge rolls, and how fast its bands swirl around the limb.
const float POOL_HEIGHT = 0.22;
const float POOL_WAVE = 0.03;
const float POOL_SWIRL = 0.7;

// A stream wound along a helix around the limb: `turns` times from v = 0 to 1, starting `phase` of the way around, with its
// head at `head` (v) and its body trailing `len` behind it toward the hand or foot. Works in texels: `cell` is the texel and
// `texels` the grid size. Above `fadeTop - fadeLength` the stream narrows, reaching nothing at `fadeTop`. Returns vec4(0) off
// the stream. `t` is already stepped to the water frame rate.
vec4 waterStream(vec2 cell, vec2 texels, float phase, float turns, float head, float len, float headWidth, float tailWidth,
                 float fadeTop, float fadeLength, float t) {
    vec2 q = (cell + 0.5) / texels;
    // 0 at the head, 1 at the tail end; slightly negative is the rounded front of the head.
    float behind = (head - q.y) / len;
    const float NOSE = 0.05;
    if (behind < -NOSE || behind > 1.3) {
        return vec4(0.0);
    }
    // Distance across the stream in texels, measured square to the helix rather than around the limb.
    float slope = turns * texels.x / texels.y;
    float across = waterAround(q.x, phase + q.y * turns) * texels.x / sqrt(1.0 + slope * slope);
    float width = mix(headWidth, tailWidth, clamp(behind, 0.0, 1.0)) * (behind < 0.0 ? 1.0 + behind / NOSE : 1.0)
            * clamp((fadeTop - q.y) / fadeLength, 0.0, 1.0);
    if (abs(across) > width * 0.5) {
        return vec4(0.0);
    }
    float side = across / (width * 0.5);
    if (behind > 1.0) {
        // Wake: a few droplets left behind the tail.
        return waterHash(cell + floor(t * WATER_FPS) * 0.37) < 0.2 && abs(side) < 0.5 ? vec4(WATER_LIGHT.rgb, 0.45) : vec4(0.0);
    }
    if (behind > 0.65 && waterHash(cell + floor(t * WATER_FPS) * 0.37) < (behind - 0.65) / 0.35) {
        // The tail breaks up into the film.
        return vec4(0.0);
    }
    if (behind < 0.15) {
        // Foam head with a lit rim.
        return abs(side) < 0.55 ? WATER_FOAM : WATER_LIGHT;
    }
    return waterStreamTexel(across, width * 0.5);
}

// Serpent Coil (27-28, 41-44): one water serpent per limb winds from the hand or foot toward the shoulder or hip, its foam
// head leading, and thins away to nothing just short of the top; then a new one rises out of the pool of water round the hand
// or foot. `limb` indexes the SERPENT_LIMB_ and POOL_ arrays: 0 right arm, 1 left arm, 2 right leg, 3 left leg.
vec4 waterSerpentLimb(float u, float v, float t, int limb) {
    vec2 cell = floor(vec2(u, v) * SERPENT_TEXELS);
    vec2 q = (cell + 0.5) / SERPENT_TEXELS;
    float tq = floor(t * WATER_FPS) / WATER_FPS;

    // Pool: water flowing round the hand or foot in sloped bands, up to a rolling lighter crest, with the odd glint. A line
    // of white foam runs up the outer side and curls along the crest there, broken into dashes that flow upward.
    vec4 pool = vec4(0.0);
    float poolTop = POOL_HEIGHT + POOL_WAVE * sin(q.x * TAU * 2.0 - tq * 4.0 + float(limb) * 1.7);
    if (q.y < poolTop) {
        pool = fract(q.x * 3.0 + q.y * 2.0 - tq * POOL_SWIRL) < 0.22 ? vec4(WATER_LIGHT.rgb, 0.5) : vec4(WATER_MID.rgb, 0.42);
        float foamCentre = POOL_OUTER_FACE[limb] + POOL_FOAM_WANDER * sin(q.y * TAU * 4.0 - tq * 5.0);
        float fromFoam = abs(waterAround(q.x, foamCentre)) * SERPENT_TEXELS.x;
        bool crest = q.y > poolTop - 1.5 / SERPENT_TEXELS.y;
        if (crest) {
            pool = fromFoam < 6.0 ? vec4(WATER_FOAM.rgb, 0.8) : vec4(WATER_LIGHT.rgb, 0.6);
        } else if (fromFoam < POOL_FOAM_WIDTH && fract(q.y * 6.0 - tq * 1.5) < 0.75) {
            pool = vec4(WATER_FOAM.rgb, 0.8);
        } else if (waterHash(cell + floor(t * 4.0) * 0.61) > 0.98) {
            pool = vec4(WATER_FOAM.rgb, 0.6);
        }
    }

    if (q.y > SERPENT_TOP) {
        return pool;
    }
    float cycle = fract(t / SERPENT_PERIOD + SERPENT_LIMB_OFFSETS[limb]);
    float progress = cycle / SERPENT_ACTIVE;
    if (progress > 1.0) {
        return pool;
    }
    float head = mix(-0.05, SERPENT_TOP + SERPENT_LENGTH, progress);
    float phase = SERPENT_LIMB_PHASES[limb];

    vec4 serpent = waterStream(cell, SERPENT_TEXELS, phase, SERPENT_TURNS, head, SERPENT_LENGTH,
            SERPENT_HEAD_WIDTH, SERPENT_TAIL_WIDTH, SERPENT_TOP, SERPENT_TAPER, tq);
    // A dark eye on each side of the head.
    float behind = (head - q.y) / SERPENT_LENGTH;
    if (serpent.a > 0.0 && behind > 0.04 && behind < 0.08) {
        float slope = SERPENT_TURNS * SERPENT_TEXELS.x / SERPENT_TEXELS.y;
        float across = waterAround(q.x, phase + q.y * SERPENT_TURNS) * SERPENT_TEXELS.x / sqrt(1.0 + slope * slope);
        if (abs(abs(across) - SERPENT_HEAD_WIDTH * 0.3) < 0.5) {
            return vec4(0.04, 0.12, 0.36, 0.92);
        }
    }
    return serpent.a > 0.0 ? serpent : pool;
}

// ---- Wind look prototypes: the Tornado Fist's rings ----
// Each wind ring (QiCoatingLayer.submitWindRings) is a band WIND_AROUND texels round and WIND_ROWS tall. The
// Tornado Fist's rings stand still, so the motion is here: light green streaks slide round each row at their own pace, stepping
// WIND_FPS times a second, each a bright head with a tail fading behind it, and each row a texel further round than the one
// below so they lean like the coiled streaks painted into the skin under them (qi_coating_skin.fsh), which run the same way.
// Between gusts only a few faint streaks run; when a gust climbs the arm (windGust, timed with the skin's) they fill in and
// brighten. There is no film between streaks.
const float WIND_AROUND = 40.0;
const float WIND_ROWS = 3.0;
const float WIND_FPS = 16.0;
// Each row is cut into cells WIND_CELL texels long (WIND_AROUND is a whole number of them), each of which may carry a streak.
const float WIND_CELL = 8.0;
// Share of cells carrying a streak between gusts, and at a gust's height. Like the skin, the rings keep running steadily
// between gusts (the first try: 0.25 and 0.4 below).
const float WIND_CALM_CHANCE = 0.5;
const float WIND_GUST_CHANCE = 0.8;
// How faint the streaks are between gusts, as a share of a gust's brightness.
const float WIND_CALM_ALPHA = 0.75;
const vec4 WIND_HEAD = vec4(0.82, 1.00, 0.85, 0.7);
const vec4 WIND_TAIL = vec4(0.62, 0.92, 0.70, 0.1);
// The rings don't know their own height up the arm, so they all take the gust as it passes this share of the limb, about the
// middle of the box.
const float WIND_RING_HEIGHT = 0.3;
vec4 windRing(float u, float v, float t) {
    float row = min(floor(v * WIND_ROWS), WIND_ROWS - 1.0);
    float frame = floor(t * WIND_FPS);
    float gust = windGust(WIND_RING_HEIGHT, t);
    // The middle row slides twice as fast as the edges.
    float x = mod(floor(u * WIND_AROUND) - row + frame * (row == 1.0 ? 2.0 : 1.0), WIND_AROUND);
    float cell = floor(x / WIND_CELL);
    float local = x - cell * WIND_CELL;
    float streakLength = 3.0 + floor(hash(vec2(cell, row + 4.0)) * 4.0);
    if (hash(vec2(cell, row)) >= mix(WIND_CALM_CHANCE, WIND_GUST_CHANCE, gust) || local >= streakLength) {
        return vec4(0.0);
    }
    // The streaks move toward smaller x, so the head is the cell's near end and the tail fades back from it in three steps.
    float along = 1.0 - local / (streakLength - 1.0);
    vec4 streak = mix(WIND_TAIL, WIND_HEAD, ceil(along * 3.0) / 3.0);
    streak.a *= mix(WIND_CALM_ALPHA, 1.0, gust);
    return streak;
}

void main() {
    // GameTime is the day fraction (0..1 over 24000 ticks), interpolated per frame -> seconds.
    float seconds = GameTime * 1200.0;
    float style = floor(max(0.0, auraUv.x) / 2.0);
    float u = clamp(auraUv.x - style * 2.0, 0.0, 1.0);
    // The stone plates start each limb at its own point around the perimeter (QiCoatingLayer.LIMB_PATTERN_SHIFTS), so their
    // u runs past 1 and wraps back onto the perimeter here.
    float wrappedU = fract(max(0.0, auraUv.x) - style * 2.0);
    float v = clamp(auraUv.y, 0.0, 1.0);

    int id = int(style);
    vec4 coating;
    if (id == 5) {
        coating = voidStarfield(u, v, seconds);
    } else if (id == 9 || id == 58) {
        coating = voidEcho(u, v, seconds, id == 9 ? 1.0 : 0.0);
    } else if (id == 10) {
        coating = voidEchoLayer(u, v, seconds);
    } else if (id == 11) {
        coating = voidStarfieldLayer(u, v, seconds);
    } else if (id == 27 || id == 44) {
        coating = waterSerpentLimb(wrappedU, v, seconds, id == 44 ? 1 : 0);
    } else if (id == 28 || (id >= 41 && id <= 43)) {
        coating = waterSerpentLimb(wrappedU, v, seconds, id == 28 ? 0 : id - 40);
    } else if (id == 14 || id == 45) {
        coating = lightningStreaks(u, v, seconds, id == 45 ? 1.0 : 0.0, CRACKLE_HAND_TEXELS, CRACKLE_HAND_TOP, STREAK_COUNT, 1.0);
    } else if (id == 16 || (id >= 46 && id <= 48)) {
        coating = lightningStreaks(u, v, seconds, id == 16 ? 0.0 : float(id - 45), CRACKLE_LIMB_TEXELS,
                id >= 47 ? STREAK_LEG_TOP : CRACKLE_LIMB_TOP, STREAK_COUNT, 1.0);
    } else if (id == 49 || id == 50) {
        coating = lightningStreaks(u, v, seconds, float(id - 39), CRACKLE_LIMB_TEXELS, STREAK_STEP_TOP, STREAK_STEP_COUNT,
                STREAK_STEP_SPEED);
    } else if (id == 51) {
        coating = lightningTrail(u, v, seconds);
    } else if (id >= 52 && id <= 55) {
        coating = lightningMark(u, v, seconds, float(id - 52));
    } else if (id == 56) {
        coating = qiPlate(u, v, seconds);
    } else if (id == 57) {
        coating = pureQi(u, v, seconds);
    } else if (id == 32) {
        coating = windRing(wrappedU, v, seconds);
    } else {
        discard;
    }

    float alpha = coating.a * vertexColor.a;
    if (alpha <= 0.01) {
        discard;
    }
    fragColor = vec4(coating.rgb * vertexColor.rgb, alpha);
}
