#version 330

#moj_import <minecraft:globals.glsl>
#moj_import <ascension:void_space.glsl>
#moj_import <ascension:stone_plates.glsl>
#moj_import <ascension:water_pixels.glsl>
#moj_import <ascension:ice_crystal.glsl>
#moj_import <ascension:qi_impact.glsl>

// Sword Qi: redraws a held item's own quads. Sampler0 is the item's atlas and auraUv its atlas UV, so the item's
// alpha masks every effect to the blade's pixels. STYLE is set per pipeline: 0 starfield, 1 echo, 2 stone, 3 water, 4 lightning,
// 5 frost, 6 gale, 7 molten, 8 heat, 9 pure qi, 10 time (the echo's quiet look).
// The vertex red channel is the painted mask (SwordQiRenderer.submitMasked): 1 on pixels the coating covers, 0 on the rest,
// which show the plain item.
uniform sampler2D Sampler0;

in vec2 auraUv;
in vec4 vertexColor;

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
    // Earthbound Blade: the earth qi coatings' stone plates over the item, in a grid of STONE_SUBDIVIDE x STONE_SUBDIVIDE texels
    // per item pixel so the plates are finer than the sprite. The item still shows a little through the plates, more
    // through the seams, so its outline still reads.
    const float STONE_SUBDIVIDE = 3.0;
    const float STONE_TEXELS_PER_PLATE = 7.0;
    vec2 texel = floor(auraUv * vec2(textureSize(Sampler0, 0)) * STONE_SUBDIVIDE);
    vec2 id;
    vec2 toCentre;
    float edge;
    stonePlateCell((texel + 0.5) / STONE_TEXELS_PER_PLATE, 4096.0, id, toCentre, edge);
    // Atlas y grows down the sprite, so the bevel is lit from the top.
    vec4 plate = stonePlate(id, toCentre, edge, texel, vec2(-0.4, -1.0), seconds);
    fragColor = vec4(mix(item.rgb, plate.rgb, plate.a * vertexColor.r), vertexColor.a);
#elif STYLE == 3
    // Flowing Blade: the water qi coatings' pixel water running from the hilt to the tip in sloped bands, in a grid of
    // WATER_SUBDIVIDE x WATER_SUBDIVIDE texels per item pixel. Handheld sprites run from the handle at the bottom left to the
    // tip at the top right, and atlas y grows down the sprite, so x - y grows toward the tip.
    const float WATER_SUBDIVIDE = 2.0;
    // Band length along the blade in texels, bands per second, and how far each band's front wobbles (texels).
    const float WATER_BAND = 9.0;
    const float WATER_SPEED = 0.9;
    const float WATER_WOBBLE = 1.5;
    // How strongly the water covers the item; below 1 so the blade shows through.
    const float WATER_COVER = 0.8;
    vec2 texel = floor(auraUv * vec2(textureSize(Sampler0, 0)) * WATER_SUBDIVIDE);
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
    // Charged Blade: the lightning coatings' pixel bolts running hilt to tip, in a grid of BOLT_SUBDIVIDE x BOLT_SUBDIVIDE
    // texels per item pixel. Handheld sprites run from the handle at the bottom left to the tip at the top right, so x - y
    // grows toward the tip and x + y runs across the blade. Bolts follow lanes BOLT_LANE texels apart across it (the sprite's
    // place in the atlas isn't known here, so lanes repeat and any blade holds at least one), zigzagging and breaking up at
    // random, re-rolled BOLT_FPS times a second with a dim afterimage. On frames with a bolt the blade's outline flashes.
    const float BOLT_SUBDIVIDE = 2.0;
    const float BOLT_LANE = 9.0;
    const float BOLT_FPS = 10.0;
    // Texels along the blade between zigzag corners, how far a corner may sit off the lane (texels), and gap chance per run.
    const float BOLT_KNOT = 3.0;
    const float BOLT_JITTER = 4.0;
    const float BOLT_GAP = 0.3;
    vec2 texel = floor(auraUv * vec2(textureSize(Sampler0, 0)) * BOLT_SUBDIVIDE);
    float along = texel.x - texel.y;
    float across = texel.x + texel.y;
    float lane = floor(across / BOLT_LANE);
    float frame = floor(seconds * BOLT_FPS);
    // 2 core, 1 glow, 0 none for this frame's bolt in this lane, then the last frame's.
    float bolt = 0.0;
    float after = 0.0;
    for (int back = 0; back < 2; back++) {
        float f = frame - float(back);
        float present = step(0.35, waterHash(vec2(lane, f * 1.37)));
        float run = floor(along / 12.0);
        present *= step(BOLT_GAP, waterHash(vec2(lane + run * 3.1, f * 0.73)));
        float k = along / BOLT_KNOT;
        float a = (waterHash(vec2(lane * 5.3 + floor(k), f)) - 0.5) * BOLT_JITTER;
        float b = (waterHash(vec2(lane * 5.3 + floor(k) + 1.0, f)) - 0.5) * BOLT_JITTER;
        float centre = lane * BOLT_LANE + BOLT_LANE * 0.5 + mix(a, b, fract(k));
        float d = abs(across - centre);
        float hit = present * (d <= 1.0 ? 2.0 : d <= 2.5 ? 1.0 : 0.0);
        if (back == 0) {
            bolt = hit;
        } else {
            after = hit;
        }
    }
    // Outline: an item pixel with a see-through neighbour.
    vec2 step1 = 1.0 / vec2(textureSize(Sampler0, 0));
    float edge = 1.0 - min(
            min(textureLod(Sampler0, auraUv + vec2(step1.x, 0.0), 0.0).a, textureLod(Sampler0, auraUv - vec2(step1.x, 0.0), 0.0).a),
            min(textureLod(Sampler0, auraUv + vec2(0.0, step1.y), 0.0).a, textureLod(Sampler0, auraUv - vec2(0.0, step1.y), 0.0).a));
    float flash = step(0.5, waterHash(vec2(frame * 0.91, 3.3)));
    vec4 charge = bolt >= 2.0 ? vec4(0.95, 0.97, 1.00, 0.95)
                : bolt >= 1.0 ? vec4(0.50, 0.72, 1.00, 0.70)
                : after >= 2.0 ? vec4(0.42, 0.58, 1.00, 0.45)
                : edge > 0.9 && flash > 0.0 ? vec4(0.60, 0.80, 1.00, 0.65)
                : vec4(0.30, 0.42, 0.95, 0.25);
    fragColor = vec4(mix(item.rgb, charge.rgb, charge.a * vertexColor.r), vertexColor.a);
#elif STYLE == 5
    // Frostbite Edge: the ice qi coatings' ice sheet climbs the blade from the hilt to the tip when it comes on, behind a
    // ragged white edge, in a grid of FROST_SUBDIVIDE x FROST_SUBDIVIDE texels per item pixel, and glows faintly like the
    // hand coatings' ice. A swing or hit breaks a piece off it (frostBreak), and now and then one breaks off on its own.
    // Vertex colour (SwordQiRenderer.frostColor): r has the painted mask in its top bit (coated or plain) and the last hit's
    // seed in its low 7 bits; g is how far the frost has climbed; b how far along the item this pixel sits; a how long ago the
    // last hit was, as a share of IMPACT_SECONDS (1 if not recently). The result is drawn opaque.
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
    vec2 size = vec2(textureSize(Sampler0, 0));
    vec2 texel = floor(auraUv * size * FROST_SUBDIVIDE);
    vec2 pixel = floor(auraUv * size);
    float maskBits = floor(vertexColor.r * 255.0 + 0.5);
    float coated = step(128.0, maskBits);
    float impactSeed = mod(maskBits, 128.0);
    float impactAge = vertexColor.a < 1.0 ? vertexColor.a * IMPACT_SECONDS : -1.0;
    float progress = vertexColor.g;
    float along = vertexColor.b;
    float frame = floor(seconds * FROST_FPS);
    float eased = 1.0 - (1.0 - progress) * (1.0 - progress);
    float front = eased * (1.0 + FROST_JAG * 2.0) - FROST_JAG
            + (iceHash(pixel * 0.73 + (progress < 0.999 ? frame : 0.0)) - 0.5) * 2.0 * FROST_JAG;
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
        // Handheld sprites run from the handle at the bottom left to the tip at the top right and atlas y grows down, so
        // within a pixel x - y runs toward the tip and x + y across the blade.
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
    // back. Handheld sprites run from the handle at the bottom left to the tip at the top right and atlas y grows down, so x - y
    // runs toward the tip and x + y across the blade. Vertex colour (SwordQiRenderer.galeColor): r the painted mask, g how far
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
    vec2 texel = floor(auraUv * vec2(textureSize(Sampler0, 0)) * GALE_SUBDIVIDE);
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
    // or hit throws sparks off it, with a brief flash. The gold tint and molten colours must match qi_coating_skin.fsh's.
    // Vertex colour (SwordQiRenderer.frostColor): r has the painted mask in its top bit and the last hit's seed in its low 7
    // bits; g is how far the pour has got; b how far along the item this pixel sits; a how long ago the last hit was, as a
    // share of IMPACT_SECONDS (1 if not recently). The result is drawn opaque.
    const float MOLTEN_JAG = 0.06;
    const float MOLTEN_FPS = 8.0;
    const float MOLTEN_SPAN = 0.18;
    const float MOLTEN_COOL = 0.2;
    const vec3 MOLTEN_WHITE = vec3(1.00, 0.97, 0.84);
    const vec3 MOLTEN_YELLOW = vec3(1.00, 0.80, 0.32);
    const vec3 MOLTEN_ORANGE = vec3(1.00, 0.52, 0.12);
    const vec3 MOLTEN_RED = vec3(0.82, 0.27, 0.06);
    const vec3 GOLD_DEEP = vec3(0.30, 0.18, 0.04);
    const vec3 GOLD_BRIGHT = vec3(0.98, 0.80, 0.42);
    const float GOLD_TINT_AMOUNT = 0.8;
    const vec3 GOLD_SHINE = vec3(1.00, 0.97, 0.80);
    // Sparks as on the Molten Fist.
    const float SPARK_SECONDS = 0.35;
    const float SPARK_CHANCE = 0.1;
    const float SPARK_FPS = 16.0;
    const float FLASH_SECONDS = 0.12;
    const float FLASH_SHINE = 0.3;
    vec2 pixel = floor(auraUv * vec2(textureSize(Sampler0, 0)));
    float maskBits = floor(vertexColor.r * 255.0 + 0.5);
    float coated = step(128.0, maskBits);
    float impactSeed = mod(maskBits, 128.0);
    float impactAge = vertexColor.a < 1.0 ? vertexColor.a * IMPACT_SECONDS : -1.0;
    float progress = vertexColor.g;
    float along = vertexColor.b;
    float frame = floor(seconds * MOLTEN_FPS);
    float eased = 1.0 - (1.0 - progress) * (1.0 - progress);
    float front = eased * (1.0 + MOLTEN_JAG * 2.0) - MOLTEN_JAG
            + (iceHash(pixel * 0.73 + (progress < 0.999 ? frame : 0.0)) - 0.5) * 2.0 * MOLTEN_JAG;
    vec3 color = item.rgb;
    if (along <= front) {
        float span = MOLTEN_SPAN * clamp((1.0 - progress) / MOLTEN_COOL, 0.0, 1.0);
        float behind = front - along;
        if (behind < span) {
            float heat = behind / span;
            color = heat < 0.25 ? MOLTEN_WHITE : heat < 0.5 ? MOLTEN_YELLOW : heat < 0.75 ? MOLTEN_ORANGE : MOLTEN_RED;
        } else {
            float lum = dot(item.rgb, vec3(0.299, 0.587, 0.114));
            color = mix(item.rgb, mix(GOLD_DEEP, GOLD_BRIGHT, clamp(lum * 1.1, 0.0, 1.0)), GOLD_TINT_AMOUNT);
            if (impactAge >= 0.0 && impactAge < FLASH_SECONDS) {
                color = mix(color, GOLD_SHINE, FLASH_SHINE);
            }
            if (impactAge >= 0.0 && impactAge < SPARK_SECONDS
                    && iceHash(pixel + vec2(impactSeed * 1.31, floor(impactAge * SPARK_FPS) * 0.73)) < SPARK_CHANCE) {
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
    // Vertex colour as the Molten Edge's (SwordQiRenderer.frostColor).
    // The result is drawn opaque.
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
    const float SPARK_SECONDS = 0.35;
    const float SPARK_CHANCE = 0.1;
    const float SPARK_FPS = 16.0;
    vec2 pixel = floor(auraUv * vec2(textureSize(Sampler0, 0)));
    float maskBits = floor(vertexColor.r * 255.0 + 0.5);
    float coated = step(128.0, maskBits);
    float impactSeed = mod(maskBits, 128.0);
    float impactAge = vertexColor.a < 1.0 ? vertexColor.a * IMPACT_SECONDS : -1.0;
    float progress = vertexColor.g;
    float along = vertexColor.b;
    float frame = floor(seconds * HEAT_FPS);
    float eased = 1.0 - (1.0 - progress) * (1.0 - progress);
    float front = eased * (1.0 + HEAT_JAG * 2.0) - HEAT_JAG
            + (iceHash(pixel * 0.73 + (progress < 0.999 ? frame : 0.0)) - 0.5) * 2.0 * HEAT_JAG;
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
        if (impactAge >= 0.0 && impactAge < SPARK_SECONDS
                && iceHash(pixel + vec2(impactSeed * 1.31, floor(impactAge * SPARK_FPS) * 0.73)) < SPARK_CHANCE) {
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
#endif
}
