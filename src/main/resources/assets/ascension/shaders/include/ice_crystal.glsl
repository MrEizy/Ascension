#version 330

// Ice shared by the ice qi coatings and sword qi, after the vanilla ice block: a clear sheet of light blue with soft paler
// patches and faint pale streaks running diagonally through it. No plates or seams, so it reads as one piece of ice rather
// than bricks or scales. See-through, so the skin or blade shows under it.

// The sheet, its paler patches, the soft side of a streak, a streak's core, and a streak catching the light. The streaks
// sit only a step or two above the sheet, in colour and opacity, so they read as lines inside the ice rather than paint on it.
const vec4 ICE_BASE = vec4(0.56, 0.76, 0.98, 0.52);
const vec4 ICE_PATCH = vec4(0.62, 0.81, 1.00, 0.55);
const vec4 ICE_STREAK_EDGE = vec4(0.66, 0.84, 1.00, 0.55);
const vec4 ICE_STREAK = vec4(0.72, 0.88, 1.00, 0.58);
const vec4 ICE_GLINT = vec4(0.84, 0.94, 1.00, 0.64);
// Frost: the white leading edge of a freeze, and the loose rime specks just ahead of it.
const vec4 ICE_RIM = vec4(0.96, 0.99, 1.00, 0.92);
const vec4 ICE_RIME_SPECK = vec4(0.90, 0.97, 1.00, 0.45);

// Patches: texels per noise cell, and the noise level above which the sheet turns paler.
const float ICE_PATCH_SIZE = 8.0;
const float ICE_PATCH_LEVEL = 0.58;
// Streaks run on diagonal lanes ICE_STREAK_SPACING texels apart; each lane is cut into ICE_STREAK_SEGMENT-row stretches, of
// which ICE_STREAK_CHANCE hold a streak between ICE_STREAK_MIN and ICE_STREAK_MAX rows long.
const float ICE_STREAK_SPACING = 8.0;
const float ICE_STREAK_SEGMENT = 12.0;
const float ICE_STREAK_CHANCE = 0.45;
const float ICE_STREAK_MIN = 3.0;
const float ICE_STREAK_MAX = 9.0;
// Streaks catch the light now and then, stepped at this frame rate like an animated texture; ICE_GLINT_CHANCE of them per frame.
const float ICE_GLINT_FPS = 3.0;
const float ICE_GLINT_CHANCE = 0.08;

float iceHash(vec2 p) {
    return fract(sin(dot(p, vec2(269.5, 183.3))) * 43758.5453);
}

// Value noise that wraps every `period` cells along x.
float iceNoise(vec2 p, float period) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 w = f * f * (3.0 - 2.0 * f);
    float x0 = mod(i.x, period);
    float x1 = mod(i.x + 1.0, period);
    return mix(mix(iceHash(vec2(x0, i.y)), iceHash(vec2(x1, i.y)), w.x),
               mix(iceHash(vec2(x0, i.y + 1.0)), iceHash(vec2(x1, i.y + 1.0)), w.x), w.y);
}

// Colour and alpha of the ice sheet at `texel`, on a grid that wraps every `period` texels along x (a multiple of
// ICE_PATCH_SIZE and ICE_STREAK_SPACING).
vec4 iceSheet(vec2 texel, float period, float t) {
    // Streak lanes run along x - y; measuring each streak along y alone keeps it whole across the wrap.
    float across = texel.x - texel.y;
    float lane = mod(floor(across / ICE_STREAK_SPACING), period / ICE_STREAK_SPACING);
    float offset = mod(across, ICE_STREAK_SPACING);
    float shift = iceHash(vec2(lane, 1.3)) * ICE_STREAK_SEGMENT;
    float segment = floor((texel.y + shift) / ICE_STREAK_SEGMENT);
    float row = mod(texel.y + shift, ICE_STREAK_SEGMENT);
    vec2 streakId = vec2(lane, segment);
    float streakLength = mix(ICE_STREAK_MIN, ICE_STREAK_MAX, iceHash(streakId + 7.7));
    if (iceHash(streakId) < ICE_STREAK_CHANCE && row < streakLength && offset < 2.0) {
        if (offset < 1.0) {
            return iceHash(streakId + floor(t * ICE_GLINT_FPS) * 0.53) < ICE_GLINT_CHANCE ? ICE_GLINT : ICE_STREAK;
        }
        return ICE_STREAK_EDGE;
    }
    float patchNoise = iceNoise(texel / ICE_PATCH_SIZE, period / ICE_PATCH_SIZE);
    return patchNoise > ICE_PATCH_LEVEL ? ICE_PATCH : ICE_BASE;
}

// Breaking ice: carved cracks run up from a starting row, a piece of ice between two of them works loose and breaks off, and
// frost creeps back in to fill the hole. Works in texel columns (x) and rows counted up from where the break starts (r), and
// `age` is seconds since it started. Cracks follow lanes BREAK_LANE columns apart; CRACK_LANE_CHANCE of the lanes crack, each
// a line wandering up to CRACK_WANDER columns off its course at corners CRACK_KNOT rows apart, reaching CRACK_REACH rows,
// sometimes with a short fork. Between two cracked lanes, SHARD_CHANCE of the time a piece SHARD_MIN to SHARD_MAX rows tall
// breaks off.
const float BREAK_LANE = 8.0;
const float CRACK_LANE_CHANCE = 0.6;
const float CRACK_KNOT = 4.0;
const float CRACK_WANDER = 1.5;
const float CRACK_REACH = 16.0;
const float CRACK_FORK_CHANCE = 0.5;
const float CRACK_FORK_ROWS = 5.0;
const float SHARD_CHANCE = 0.4;
const float SHARD_MIN = 5.0;
const float SHARD_MAX = 9.0;
// Timeline (seconds): the cracks spread until CRACK_SPREAD, the piece shakes loose until SHARD_BREAK, frost refills the hole
// by SHARD_REFILLED, and the cracks heal texel by texel from CRACK_HEAL until BREAK_END.
const float CRACK_SPREAD = 0.15;
const float SHARD_BREAK = 0.4;
const float SHARD_REFILLED = 1.4;
const float CRACK_HEAL = 1.0;
const float BREAK_END = 1.6;
// A loosening piece's outline flickers at this frame rate.
const float SHARD_SHAKE_FPS = 12.0;
// Carved cracks: a groove darker than the ice, with a lit edge on one side, so they read as fractures in it rather than light.
const vec4 CRACK_GROOVE = vec4(0.30, 0.48, 0.74, 0.72);
const vec4 CRACK_LIT_EDGE = vec4(0.86, 0.96, 1.00, 0.70);
// iceBreak's answer for a texel of the hole where the piece broke off.
const vec4 ICE_HOLE = vec4(0.0, 0.0, 0.0, -1.0);

float crackSeedHash(float lane, float seed, float salt) {
    return iceHash(vec2(lane * 1.31 + salt, seed * 0.77 + salt * 2.1));
}

bool laneCracks(float lane, float seed) {
    return crackSeedHash(lane, seed, 4.4) < CRACK_LANE_CHANCE;
}

// Column of the crack in `lane` at row r: a slanted line from a random start in the lane, its corners every CRACK_KNOT rows
// pushed off it at random. The first corner sits on the line, so the crack starts where it should.
float crackColumn(float lane, float r, float seed) {
    float base = lane * BREAK_LANE + 2.0 + crackSeedHash(lane, seed, 0.3) * (BREAK_LANE - 4.0);
    float drift = (crackSeedHash(lane, seed, 1.9) - 0.5) * 0.5;
    float k = r / CRACK_KNOT;
    float knot = floor(k);
    float a = knot < 1.0 ? 0.0 : (iceHash(vec2(lane * 5.3 + knot, seed)) - 0.5) * 2.0 * CRACK_WANDER;
    float b = (iceHash(vec2(lane * 5.3 + knot + 1.0, seed)) - 0.5) * 2.0 * CRACK_WANDER;
    return base + drift * r + mix(a, b, fract(k));
}

// What breaking does to the texel at column `texel.x`, row r, for a break `age` seconds old: vec4(0) for nothing, ICE_HOLE for
// the hole, or a colour to draw. Only lanes laneLo to laneHi take part.
vec4 iceBreak(vec2 texel, float r, float age, float seed, float laneLo, float laneHi) {
    if (r < 0.0 || r >= CRACK_REACH || age < 0.0 || age >= BREAK_END) {
        return vec4(0.0);
    }
    float x = texel.x + 0.5;
    float lane0 = floor(texel.x / BREAK_LANE);

    // The piece between this lane's crack and the next one's, or the previous lane's and this one's.
    for (int i = -1; i <= 0; i++) {
        float left = lane0 + float(i);
        if (left < laneLo || left + 1.0 > laneHi || !laneCracks(left, seed) || !laneCracks(left + 1.0, seed)
                || crackSeedHash(left, seed, 3.3) >= SHARD_CHANCE) {
            continue;
        }
        float r0 = 2.0 + floor(crackSeedHash(left, seed, 5.7) * 3.0);
        float r1 = min(r0 + mix(SHARD_MIN, SHARD_MAX, crackSeedHash(left, seed, 6.6)), CRACK_REACH - 1.0);
        float leftX = crackColumn(left, r, seed);
        float rightX = crackColumn(left + 1.0, r, seed);
        if (x <= leftX + 0.7 || x >= rightX - 0.7 || r < r0 || r >= r1 || age < CRACK_SPREAD) {
            continue;
        }
        // Texels in from the piece's nearest edge.
        float inside = min(min(x - leftX, rightX - x), min(r - r0 + 1.0, r1 - r));
        if (age < SHARD_BREAK) {
            // Shaking loose: its outline flickers white.
            return inside < 1.5 && mod(floor(age * SHARD_SHAKE_FPS), 2.0) == 0.0 ? ICE_RIM : vec4(0.0);
        }
        // Broken off: frost creeps in from the edges until the hole is sealed, behind the same white edge and rime as the freeze.
        float refill = (age - SHARD_BREAK) / (SHARD_REFILLED - SHARD_BREAK) * (BREAK_LANE * 0.5 + 1.0);
        if (inside < refill) {
            return vec4(0.0);
        }
        if (inside < refill + 1.0) {
            return ICE_RIM;
        }
        return inside < refill + 3.0 && iceHash(texel + floor(age * 8.0) * 0.31) < 0.25 ? ICE_RIME_SPECK : ICE_HOLE;
    }

    // Carved cracks, drawn as far as they have spread, then healing texel by texel.
    if (r >= min(1.0, age / CRACK_SPREAD) * CRACK_REACH) {
        return vec4(0.0);
    }
    float healing = (age - CRACK_HEAL) / (BREAK_END - CRACK_HEAL);
    if (healing > 0.0 && iceHash(texel + seed * 0.13) < healing) {
        return vec4(0.0);
    }
    bool litEdge = false;
    for (int i = -1; i <= 1; i++) {
        float lane = lane0 + float(i);
        if (lane < laneLo || lane > laneHi || !laneCracks(lane, seed)) {
            continue;
        }
        float d = x - crackColumn(lane, r, seed);
        // A short fork snapping off partway up, running off at a slant.
        float forkStart = 5.0 + floor(crackSeedHash(lane, seed, 7.3) * 5.0);
        if (crackSeedHash(lane, seed, 6.2) < CRACK_FORK_CHANCE && r >= forkStart && r < forkStart + CRACK_FORK_ROWS) {
            float side = crackSeedHash(lane, seed, 8.1) < 0.5 ? -1.0 : 1.0;
            float forkD = x - (crackColumn(lane, forkStart, seed) + side * (r - forkStart));
            d = abs(forkD) < abs(d) ? forkD : d;
        }
        if (abs(d) < 0.7) {
            return CRACK_GROOVE;
        }
        litEdge = litEdge || (d >= 0.7 && d < 1.7);
    }
    return litEdge ? CRACK_LIT_EDGE : vec4(0.0);
}


// The ice's own faint qi glow, added on top of its colour: ICE_GLOW_COLOR, ICE_GLOW_STRENGTH strong. It is steady (a moving glow
// was too much), a touch stronger in some soft patches ICE_GLOW_PATCH texels across than others, posterised into ICE_GLOW_STEPS
// levels like the other pixel coatings.
const vec3 ICE_GLOW_COLOR = vec3(0.45, 0.75, 1.00);
const float ICE_GLOW_STRENGTH = 0.12;
const float ICE_GLOW_PATCH = 8.0;
const float ICE_GLOW_STEPS = 3.0;

vec3 iceGlow(vec2 texel) {
    float patchLevel = iceNoise(texel / ICE_GLOW_PATCH + 31.7, 4096.0 / ICE_GLOW_PATCH);
    return ICE_GLOW_COLOR * ICE_GLOW_STRENGTH * floor((0.6 + 0.4 * patchLevel) * ICE_GLOW_STEPS + 0.5) / ICE_GLOW_STEPS;
}

// Row (in texels) the freeze has reached at `column`: `progress` (0 to 1) of the way to `top` rows, with a jagged leading edge
// that jumps a little every frame while it climbs and settles once it is done, so it reads as frost creeping rather than a
// wipe. `jag` is how many rows the edge wanders either side.
float iceFreezeRow(float column, float top, float progress, float jag, float frame) {
    float settled = step(0.999, progress);
    float wobble = mix(iceHash(vec2(column, frame)), 0.5, settled);
    float edge = (iceHash(vec2(floor(column / 2.0), 4.4)) - 0.5) * 2.0 * jag + (wobble - 0.5) * 2.0;
    // Eased so the frost rushes up the first stretch and slows as it reaches the top.
    float eased = 1.0 - (1.0 - progress) * (1.0 - progress);
    return eased * (top + jag) + edge * min(1.0, eased * 4.0);
}
