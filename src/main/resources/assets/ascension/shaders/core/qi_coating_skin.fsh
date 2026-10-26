#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <ascension:ice_crystal.glsl>
#moj_import <ascension:wind_gust.glsl>
#moj_import <ascension:qi_impact.glsl>

// Skin-fitted qi coatings: QiCoatingLayer redraws the coated limbs from the player's own skin, both layers, a hair off each
// surface, and this paints the coating into the skin's pixels. The base layer is coated all over; the outer layer (sleeves and
// pants) only where the skin has pixels, at its own raised depth, so nothing floats over bare skin. The result is the skin with
// the coating mixed in, lit like vanilla's entity shader, so it covers what vanilla drew under it exactly.
//
// Vertex colour (coatingData, see QiCoatingLayer.SkinCoatingConsumer):
//   r = coating in the top bit (0 a hand coating, 1 a whole-body layer on the arms and legs), and in the low 7 bits a
//       random seed for this limb's last hit
//   g = how far the freeze has got, 0 to 1
//   b = how far up the limb this is, 0 at the hand or foot to 1 at the shoulder or hip
//   a = how long ago this limb was last hit, as a share of IMPACT_SECONDS; 1 if not recently
uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexPerFaceColorBack;
in vec4 vertexPerFaceColorFront;
in vec4 lightMapColor;
in vec4 coatingData;
in vec2 texCoord0;

out vec4 fragColor;

// Ice texels per skin pixel along each axis, so the ice grid lines up with the skin's own pixels.
const float SKIN_SUBDIVIDE = 4.0;
// Limbs are 12 skin pixels long.
const float LIMB_ROWS = 12.0 * SKIN_SUBDIVIDE;
// How far up the limb (share of its length) each coating freezes: the Rime Fist to the wrist, the Glacial Mantle most of
// the way up. Then how many ice texels its leading edge wanders either side.
const float RIME_TOP = 0.38;
const float RIME_JAG = 2.0;
const float MANTLE_TOP = 0.8;
const float MANTLE_JAG = 3.0;
// While the frost climbs, its leading edge crawls at this frame rate; rime specks scatter ICE_SPECK_ROWS rows ahead of it.
const float ICE_FREEZE_FPS = 8.0;
const float ICE_SPECK_ROWS = 3.0;
const float ICE_SPECK_CHANCE = 0.25;
// Idle breaks: the ice is split into groups of IDLE_GROUP_LANES crack lanes (about one limb's worth), each on its own clock of
// IDLE_PERIOD seconds; on IDLE_CHANCE of its cycles a piece breaks off somewhere in it, at a random height.
const float IDLE_GROUP_LANES = 4.0;
const float IDLE_PERIOD = 10.0;
const float IDLE_CHANCE = 0.5;
// The ice gives off a little light of its own qi: it never goes darker than ICE_SELF_LIGHT of full brightness, however dark it
// is around it, and it glows faintly on top of that (iceGlow).
const float ICE_SELF_LIGHT = 0.45;

#ifdef WIND
// Tornado Fist (the WIND pipeline): light green wind painted into the forearm's skin, under the box of wind rings
// QiCoatingLayer draws around it. Thin slanted streaks coil round the arm, each with a bright head and a tail fading behind it,
// in WIND_SKIN_SUBDIVIDE texels per skin pixel, stepping WIND_SKIN_FPS times a second the same way as the rings (qi_coating.fsh
// windRing). Between gusts only a few faint streaks drift round; every so often a gust climbs from the hand up the arm and the
// streaks thicken and brighten as it passes. The four sides of a limb sit side by side in the skin texture, 16 pixels round, so
// the streaks repeat every 16 pixels and wrap round the arm without a seam. Only coatingData.b (how far up the limb) is used.
// (The first version: flat streaks 2-3 texels long in lanes a skin pixel tall, half of each lane's 4-texel cells lit, colour
// 0.90, 1.00, 0.91, 0.45, over a film of 0.72, 0.94, 0.78, 0.08 all over the arm, every lane running all the time.)
const float WIND_SKIN_SUBDIVIDE = 2.0;
const float WIND_SKIN_AROUND = 16.0 * WIND_SKIN_SUBDIVIDE;
const float WIND_SKIN_ROWS = 12.0 * WIND_SKIN_SUBDIVIDE;
const float WIND_SKIN_FPS = 16.0;
// -1.0 turns the skin's streaks the same way as the rings (the first try, 1.0, turned them against each other).
const float WIND_SKIN_DIRECTION = -1.0;
// How far up the limb the wind reaches (about as far as the rings), and how many texels its top edge wanders.
const float WIND_SKIN_TOP = WIND_GUST_TOP;
const float WIND_SKIN_JAG = 3.0;
// The streaks run in bands WIND_SKIN_BAND texels tall; each band is cut round the arm into cells WIND_SKIN_CELL texels long
// (WIND_SKIN_AROUND must be a whole number of them), and each cell may carry one streak. A streak is WIND_SKIN_WIDTH texels
// wide and leans WIND_SKIN_SLANT texels round the arm for every texel up, so it reads as a coil.
const float WIND_SKIN_BAND = 6.0;
const float WIND_SKIN_CELL = 8.0;
const float WIND_SKIN_WIDTH = 2.0;
const float WIND_SKIN_SLANT = 1.0;
// Share of cells carrying a streak between gusts, and at a gust's front. The wind keeps running steadily between gusts, which
// only swell it (the first try, 0.15 and 0.45 below, left the arm nearly bare between short flashes).
const float WIND_SKIN_CALM_CHANCE = 0.45;
const float WIND_SKIN_GUST_CHANCE = 0.8;
// How faint the streaks are between gusts, as a share of a gust's brightness.
const float WIND_SKIN_CALM_ALPHA = 0.75;
const vec4 WIND_SKIN_HEAD = vec4(0.82, 1.00, 0.85, 0.75);
const vec4 WIND_SKIN_TAIL = vec4(0.62, 0.92, 0.70, 0.12);
// Like the ice, the wind never goes darker than this share of full brightness.
const float WIND_SELF_LIGHT = 0.6;
vec4 windSkin(vec2 texel, float row, float seconds) {
    float column = floor(texel.x / WIND_SKIN_SUBDIVIDE);
    if (row >= WIND_SKIN_TOP * WIND_SKIN_ROWS - floor(iceHash(vec2(column, 6.1)) * WIND_SKIN_JAG)) {
        return vec4(0.0);
    }
    float gust = windGust(row / WIND_SKIN_ROWS, seconds);
    float band = floor(texel.y / WIND_SKIN_BAND);
    float rise = texel.y - band * WIND_SKIN_BAND;
    float frame = floor(seconds * WIND_SKIN_FPS);
    // Bands run at one or two texels a frame.
    float speed = 1.0 + floor(iceHash(vec2(band, 2.3)) * 2.0);
    float x = mod(texel.x - rise * WIND_SKIN_SLANT + WIND_SKIN_DIRECTION * frame * speed, WIND_SKIN_AROUND);
    float cell = floor(x / WIND_SKIN_CELL);
    if (iceHash(vec2(cell, band)) >= mix(WIND_SKIN_CALM_CHANCE, WIND_SKIN_GUST_CHANCE, gust)
            || x - cell * WIND_SKIN_CELL >= WIND_SKIN_WIDTH) {
        return vec4(0.0);
    }
    // Each streak spans 4 to 6 of its band's texels, starting at a random height in it.
    float streakLength = WIND_SKIN_BAND - floor(iceHash(vec2(cell, band + 7.0)) * 3.0);
    float start = floor(iceHash(vec2(cell, band + 13.0)) * (WIND_SKIN_BAND - streakLength + 1.0));
    if (rise < start || rise >= start + streakLength) {
        return vec4(0.0);
    }
    // The slant puts the streak's end further down the skin texture ahead of it as it moves (the other end with
    // WIND_SKIN_DIRECTION 1.0): that end is the head, and the tail fades away behind it in three steps.
    float along = (rise - start) / (streakLength - 1.0);
    if (WIND_SKIN_DIRECTION > 0.0) {
        along = 1.0 - along;
    }
    vec4 streak = mix(WIND_SKIN_TAIL, WIND_SKIN_HEAD, ceil(along * 3.0) / 3.0);
    streak.a *= mix(WIND_SKIN_CALM_ALPHA, 1.0, gust);
    return streak;
}
#endif

#ifdef METAL
// Metal coatings (the METAL pipeline): the Molten Fist on the hands and the Golden Body over the whole skin pour on as molten
// metal qi and cool to gold. The skin is tinted gold rather than replaced (goldTint): each pixel keeps its own brightness and a
// little of its own colour, so hair, face and clothes still read through the metal. Like the ice, the gold spreads up from the
// hand or foot (the Golden Body's torso from the waist and head from the neck, each once the part below has filled:
// QiCoatingLayer staggers how far along each part is) when it comes on, and melts back down when it goes off (iceFreezeRow, so
// it takes the same coatingData). While it spreads, the MOLTEN_ROWS rows behind its front glow white, yellow, orange then red,
// and that glow shrinks back into the front over the last MOLTEN_COOL of the spread as the metal cools; drops of molten gold run
// down from the front in DRIP_CHANCE of the two-texel columns, one every DRIP_PERIOD seconds at DRIP_SPEED rows a second.
// Molten texels give off their own light. Every METAL_SHEEN_PERIOD seconds a chevron of light sweeps up the Molten Fist. A swing
// or hit throws sparks off the Molten Fist; on the Golden Body it sends a bright ring out up and down the arm from where it
// struck. Drawn in METAL_SUBDIVIDE x METAL_SUBDIVIDE texels per skin pixel.
const float METAL_SUBDIVIDE = 2.0;
const float METAL_ROWS = 12.0 * METAL_SUBDIVIDE;
// How far up the limb each coating reaches (as RIME_TOP and MANTLE_TOP), how many texels its top edge wanders, and the frame
// rate its leading edge crawls at while it spreads. The Golden Body reaches all the way: its wandering edge only ever rises
// past the top, so it clears every row.
const float MOLTEN_FIST_TOP = 0.38;
const float MOLTEN_FIST_JAG = 1.0;
const float GOLDEN_BODY_TOP = 1.0;
const float GOLDEN_BODY_JAG = 2.0;
const float METAL_SPREAD_FPS = 8.0;
// The gold tint: a pixel's brightness picks a gold along a ramp from GOLD_DEEP (dark pixels) to GOLD_BRIGHT (light ones), then
// GOLD_TINT_AMOUNT of the way from the skin's own colour to that. The ramp keeps the gold saturated at every brightness, so
// fully lit faces (tops of the head and shoulders) don't clip to a pale, near-white gold. GOLD_DEEP, GOLD_BRIGHT, GOLD_TINT_AMOUNT,
// GOLD_SHINE and the molten colours must match sword_qi.fsh's Molten Edge.
const vec3 GOLD_DEEP = vec3(0.30, 0.18, 0.04);
const vec3 GOLD_BRIGHT = vec3(0.98, 0.80, 0.42);
const float GOLD_TINT_AMOUNT = 0.8;
const vec3 GOLD_SHINE = vec3(1.00, 0.97, 0.80);
// How far the sheen and the Molten Fist's hit flash brighten the gold toward GOLD_SHINE.
const float METAL_SHEEN_SHINE = 0.6;
const float METAL_FLASH_SHINE = 0.3;
const float MOLTEN_ROWS = 5.0;
const float MOLTEN_COOL = 0.2;
const vec3 MOLTEN_WHITE = vec3(1.00, 0.97, 0.84);
const vec3 MOLTEN_YELLOW = vec3(1.00, 0.80, 0.32);
const vec3 MOLTEN_ORANGE = vec3(1.00, 0.52, 0.12);
const vec3 MOLTEN_RED = vec3(0.82, 0.27, 0.06);
const float DRIP_CHANCE = 0.3;
const float DRIP_PERIOD = 0.8;
const float DRIP_SPEED = 14.0;
// The sheen: a chevron METAL_SHEEN_WIDTH rows thick, its point METAL_SHEEN_DIP rows below its ends, sweeping up the limb in
// METAL_SHEEN_RUN seconds. Its arms repeat every 8 texels round the limb, which the 32 round divides into, so it has no seam.
const float METAL_SHEEN_PERIOD = 3.0;
const float METAL_SHEEN_RUN = 0.6;
const float METAL_SHEEN_WIDTH = 2.0;
const float METAL_SHEEN_DIP = 2.0;
// Molten Fist sparks: for METAL_SPARK_SECONDS after a hit, METAL_SPARK_CHANCE of the texels flash, re-rolled
// METAL_SPARK_FPS times a second, and the whole fist flashes brighter for the first METAL_FLASH_SECONDS.
const float METAL_SPARK_SECONDS = 0.35;
const float METAL_SPARK_CHANCE = 0.08;
const float METAL_SPARK_FPS = 16.0;
const float METAL_FLASH_SECONDS = 0.12;
// Golden Body ring: runs out from the struck row at METAL_RING_SPEED rows a second, for METAL_RING_SECONDS.
const float METAL_RING_SPEED = 40.0;
const float METAL_RING_SECONDS = 0.45;
// The gold keeps a little light of its own qi, like the ice.
const float METAL_SELF_LIGHT = 0.35;
// How much flatter than vanilla the gold's face shading is (see where it's lit, in main).
const float METAL_SHADE_MID = 0.75;
const float METAL_SHADE_FLATTEN = 0.5;

// A skin pixel tinted gold, keeping its own brightness (on the gold ramp) and a little of its own colour.
vec3 goldTint(vec3 skin) {
    float lum = dot(skin, vec3(0.299, 0.587, 0.114));
    return mix(skin, mix(GOLD_DEEP, GOLD_BRIGHT, clamp(lum * 1.1, 0.0, 1.0)), GOLD_TINT_AMOUNT);
}

// The gold at this texel, or vec4(0) where the coating hasn't reached. `glow` is set to 1 on texels that give off their own
// light (molten ones).
vec4 metalSkin(vec3 skin, vec2 texel, float row, bool body, float progress, float impactAge, float impactSeed, float seconds,
               out float glow) {
    glow = 0.0;
    float top = body ? GOLDEN_BODY_TOP : MOLTEN_FIST_TOP;
    float jag = body ? GOLDEN_BODY_JAG : MOLTEN_FIST_JAG;
    float front = iceFreezeRow(texel.x, top * METAL_ROWS, progress, jag, floor(seconds * METAL_SPREAD_FPS));
    if (row >= front) {
        return vec4(0.0);
    }
    float heatRows = MOLTEN_ROWS * clamp((1.0 - progress) / MOLTEN_COOL, 0.0, 1.0);
    float behind = floor(front - 1.0 - row);
    if (behind < heatRows) {
        glow = 1.0;
        float heat = behind / max(heatRows, 1.0);
        return vec4(heat < 0.25 ? MOLTEN_WHITE : heat < 0.5 ? MOLTEN_YELLOW : heat < 0.75 ? MOLTEN_ORANGE : MOLTEN_RED, 1.0);
    }
    if (progress < 0.999 && mod(texel.x, 2.0) == 0.0) {
        float column = floor(texel.x / 2.0);
        if (iceHash(vec2(column, 8.3)) < DRIP_CHANCE) {
            float phase = fract(seconds / DRIP_PERIOD + iceHash(vec2(column, 9.1)));
            float drip = front - 1.0 - heatRows - floor(phase * DRIP_PERIOD * DRIP_SPEED);
            if (row == drip || row == drip + 1.0) {
                glow = 1.0;
                return vec4(row == drip ? MOLTEN_ORANGE : MOLTEN_RED, 1.0);
            }
        }
    }
    if (row >= front - 1.0) {
        return vec4(GOLD_SHINE, 1.0);
    }
    vec3 gold = goldTint(skin);
    if (!body) {
        // The sheen sweeps from below the hand to past the coating's top.
        float sheenRun = (seconds - floor(seconds / METAL_SHEEN_PERIOD) * METAL_SHEEN_PERIOD) / METAL_SHEEN_RUN;
        float sheenRow = floor(sheenRun * (top * METAL_ROWS + METAL_SHEEN_DIP + METAL_SHEEN_WIDTH * 2.0) - METAL_SHEEN_WIDTH);
        float chevron = row + floor(abs(mod(texel.x, 8.0) - 4.0) / 4.0 * METAL_SHEEN_DIP);
        if (chevron >= sheenRow && chevron < sheenRow + METAL_SHEEN_WIDTH) {
            gold = mix(gold, GOLD_SHINE, METAL_SHEEN_SHINE);
        }
    }
    if (impactAge >= 0.0) {
        if (!body) {
            if (impactAge < METAL_FLASH_SECONDS) {
                gold = mix(gold, GOLD_SHINE, METAL_FLASH_SHINE);
            }
            if (impactAge < METAL_SPARK_SECONDS
                    && iceHash(texel + vec2(impactSeed * 1.31, floor(impactAge * METAL_SPARK_FPS) * 0.73)) < METAL_SPARK_CHANCE) {
                return vec4(1.0, 1.0, 0.92, 1.0);
            }
        } else if (impactAge < METAL_RING_SECONDS) {
            float struck = floor(iceHash(vec2(impactSeed, 3.7)) * max(1.0, top * METAL_ROWS - jag - 1.0));
            if (abs(abs(row - struck) - floor(impactAge * METAL_RING_SPEED)) < 1.0) {
                gold = GOLD_SHINE;
            }
        }
    }
    return vec4(gold, 1.0);
}
#endif



void main() {
    vec4 skin = texture(Sampler0, texCoord0);
    if (skin.a < 0.1) {
        // No pixel here on the outer layer: leave it bare like vanilla does.
        discard;
    }
    float seconds = GameTime * 1200.0;
#ifdef WIND
    vec2 windTexel = floor(texCoord0 * vec2(textureSize(Sampler0, 0)) * WIND_SKIN_SUBDIVIDE);
    vec4 wind = windSkin(windTexel, floor(coatingData.b * WIND_SKIN_ROWS), seconds);
    if (wind.a <= 0.0) {
        discard;
    }
    vec3 windLight = (gl_FrontFacing ? vertexPerFaceColorFront : vertexPerFaceColorBack).rgb * lightMapColor.rgb;
    vec4 windColor = vec4(mix(skin.rgb * windLight, wind.rgb * max(windLight, vec3(WIND_SELF_LIGHT)), wind.a), skin.a) * ColorModulator;
    fragColor = apply_fog(windColor, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd,
            FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
    return;
#endif
#ifdef METAL
    float metalBits = floor(coatingData.r * 255.0 + 0.5);
    vec2 metalTexel = floor(texCoord0 * vec2(textureSize(Sampler0, 0)) * METAL_SUBDIVIDE);
    float glow;
    // The cap at a part's top end (shoulder, hip, neck, crown) sits at exactly the full height, one row past the last: kept on
    // the last row, so it fills with the rest instead of catching the front's ragged edge as stripes.
    float metalRow = min(floor(coatingData.b * METAL_ROWS), METAL_ROWS - 1.0);
    vec4 metal = metalSkin(skin.rgb, metalTexel, metalRow, metalBits >= 128.0, coatingData.g,
            coatingData.a < 1.0 ? coatingData.a * IMPACT_SECONDS : -1.0, mod(metalBits, 128.0), seconds, glow);
    if (metal.a <= 0.0) {
        // Not reached yet: vanilla's skin underneath shows as it is.
        discard;
    }
    vec3 faceShade = (gl_FrontFacing ? vertexPerFaceColorFront : vertexPerFaceColorBack).rgb;
    vec3 metalLight = faceShade * lightMapColor.rgb;
    // The gold is lit more evenly than skin, and only by how bright the world light is, not its colour: vanilla's face shading
    // (0.4 turned away to 1.0 toward the lights) pulled METAL_SHADE_FLATTEN of the way to METAL_SHADE_MID, so the faces turned
    // to the lights (tops of the head and shoulders) don't wash out to a pale, whitish gold as the player moves, and the bluish
    // night light doesn't grey it.
    float worldLight = dot(lightMapColor.rgb, vec3(0.299, 0.587, 0.114));
    vec3 goldLight = vec3(max(mix(faceShade.r, METAL_SHADE_MID, METAL_SHADE_FLATTEN) * worldLight, METAL_SELF_LIGHT));
    goldLight = mix(goldLight, vec3(1.0), glow);
    vec4 metalColor = vec4(mix(skin.rgb * metalLight, metal.rgb * goldLight, metal.a), skin.a) * ColorModulator;
    fragColor = apply_fog(metalColor, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd,
            FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
    return;
#endif
    float coatingBits = floor(coatingData.r * 255.0 + 0.5);
    bool mantle = coatingBits >= 128.0;
    float impactSeed = mod(coatingBits, 128.0);
    float impactAge = coatingData.a < 1.0 ? coatingData.a * IMPACT_SECONDS : -1.0;
    float progress = coatingData.g;
    float top = mantle ? MANTLE_TOP : RIME_TOP;
    float jag = mantle ? MANTLE_JAG : RIME_JAG;

    vec2 texel = floor(texCoord0 * vec2(textureSize(Sampler0, 0)) * SKIN_SUBDIVIDE);
    float row = floor(coatingData.b * LIMB_ROWS);
    float frame = floor(seconds * ICE_FREEZE_FPS);
    float front = iceFreezeRow(texel.x, top * LIMB_ROWS, progress, jag, frame);
    vec4 ice;
    if (row >= front) {
        // Loose rime just ahead of the frost: flickering while it climbs, still once it has settled.
        float speckFrame = progress < 0.999 ? frame : 0.0;
        ice = row < front + ICE_SPECK_ROWS && iceHash(texel + speckFrame * 0.31) < ICE_SPECK_CHANCE ? ICE_RIME_SPECK : vec4(0.0);
    } else if (row >= front - 1.0) {
        ice = ICE_RIM;
    } else {
        // The skin texture never wraps around a limb, so any period that is a multiple of the patch and streak sizes will do.
        ice = iceSheet(texel, 4096.0, seconds);
        // A hit cracks the ice up from the hand and breaks a piece off, which frost then refills.
        vec4 broken = iceBreak(texel, row, impactAge, impactSeed, -1e6, 1e6);
        if (broken.a == 0.0) {
            // Now and then a piece breaks off on its own somewhere in the frozen part.
            float group = floor(texel.x / (BREAK_LANE * IDLE_GROUP_LANES));
            float cycle = seconds / IDLE_PERIOD + iceHash(vec2(group, 2.2));
            vec2 idleId = vec2(group, floor(cycle));
            if (iceHash(idleId + 0.5) < IDLE_CHANCE) {
                // Kept below the lowest the ragged top of the freeze can dip, so the whole break sits in the ice.
                float startRow = floor(iceHash(idleId + 1.5) * max(0.0, top * LIMB_ROWS - jag - 2.0 - CRACK_REACH));
                broken = iceBreak(texel, row - startRow, fract(cycle) * IDLE_PERIOD, floor(iceHash(idleId + 2.5) * 128.0),
                        group * IDLE_GROUP_LANES, group * IDLE_GROUP_LANES + IDLE_GROUP_LANES - 1.0);
            }
        }
        if (broken.a < 0.0) {
            // The hole where a piece broke off: vanilla's skin underneath shows through.
            discard;
        }
        if (broken.a > 0.0) {
            ice = broken;
        }
    }
    if (ice.a <= 0.0) {
        // Not frozen yet: vanilla's skin underneath shows as it is.
        discard;
    }

    // The skin is lit as vanilla lights it; the ice mixed into it keeps some light of its own and glows faintly on top.
    vec3 light = (gl_FrontFacing ? vertexPerFaceColorFront : vertexPerFaceColorBack).rgb * lightMapColor.rgb;
    vec3 iceLight = max(light, vec3(ICE_SELF_LIGHT));
    vec3 iceColor = ice.rgb * iceLight + iceGlow(texel);
    vec4 color = vec4(mix(skin.rgb * light, iceColor, ice.a), skin.a) * ColorModulator;
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd,
            FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
