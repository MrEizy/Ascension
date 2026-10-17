//shared by the tribulation_clouds vertex and fragment shaders.
//the mesh uv is (spiral coordinate in turns around the eye, log of rn),
//where rn is the distance from the eye divided by the cloud radius.
//the noise is sampled in "noise uv", see cloudNoiseUv

const float TAU = 6.28318530718;
//noise cells around the circle, must stay a whole number so the noise tiles
const float CLOUD_PERIOD = 16.0;
const float CLOUD_ARMS = 3.0;
//radius of the glowing eye, as a fraction of the cloud radius
const float CLOUD_EYE = 0.04;
//where the eyewall is tallest
const float CLOUD_EYEWALL = 0.09;
//noise cells per unit of log radius
const float CLOUD_RADIAL_SCALE = 2.5;

//a colour packed by the renderer as r * 65536 + g * 256 + b. not called "packed", that is a reserved word in glsl
vec3 cloudUnpack(float rgb) {
    float r = floor(rgb / 65536.0);
    float g = floor((rgb - r * 65536.0) / 256.0);
    float b = rgb - r * 65536.0 - g * 256.0;
    return vec3(r, g, b) / 255.0;
}

//shadow at level 0, base at 0.5, highlight at 1
vec3 cloudRamp(vec3 shadow, vec3 base, vec3 highlight, float level) {
    return level < 0.5 ? mix(shadow, base, level * 2.0) : mix(base, highlight, level * 2.0 - 1.0);
}

float cloudHash(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

//value noise that repeats every `period` cells along x
float cloudNoise(vec2 p, float period) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    float x0 = mod(i.x, period);
    float x1 = mod(i.x + 1.0, period);
    float a = cloudHash(vec2(x0, i.y));
    float b = cloudHash(vec2(x1, i.y));
    float c = cloudHash(vec2(x0, i.y + 1.0));
    float d = cloudHash(vec2(x1, i.y + 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

//4 octaves, each doubling the period so the result still tiles
float cloudFbm(vec2 p, float period) {
    float sum = 0.0;
    float amplitude = 0.5;
    for (int octave = 0; octave < 4; octave++) {
        sum += amplitude * cloudNoise(p, period);
        p = p * 2.0 + vec2(0.0, 13.7);
        period *= 2.0;
        amplitude *= 0.5;
    }
    return sum / 0.9375;
}

//cellular noise, distance to the nearest of one random point per cell. cells repeat every `period` along x.
//1 - this gives round puffs, like the tops of cumulus clouds
float cloudCells(vec2 p, float period) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    //squared distances while searching, so there is only one square root at the end
    float nearest = 64.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 neighbour = vec2(float(x), float(y));
            vec2 key = vec2(mod(i.x + neighbour.x, period), i.y + neighbour.y);
            vec2 offset = neighbour + vec2(cloudHash(key), cloudHash(key + 19.19)) - f;
            nearest = min(nearest, dot(offset, offset));
        }
    }
    return sqrt(nearest);
}

//where the outer edge starts breaking up
const float CLOUD_RIM_START = 0.76;

//chaotic edge noise: chunky lumps plus fine fraying. takes noise uv
float cloudFray(vec2 uv) {
    return cloudFbm(vec2(uv.x * CLOUD_PERIOD, uv.y * 0.6), CLOUD_PERIOD) * 0.55
            + cloudFbm(vec2(uv.x * CLOUD_PERIOD * 4.0, uv.y * 2.5) + vec2(0.0, 71.0), CLOUD_PERIOD * 4.0) * 0.45;
}

//true where the outer edge has frayed away. the same on both sheets, so the disk stays closed
bool cloudFrayed(float fray, float rn) {
    return rn > CLOUD_RIM_START && fray < smoothstep(CLOUD_RIM_START, 1.0, rn) * 1.1;
}

//the billows drift inward as flow grows, flow comes from the texture matrix translation
vec2 cloudNoiseUv(vec2 meshUv, float flow) {
    return vec2(meshUv.x, meshUv.y * CLOUD_RADIAL_SCALE + flow);
}

//0 inside the eye and rising steeply, so the eyewall gets a wall on the inside, and 0 again at the rim
float cloudEdges(float rn) {
    float eyeRamp = smoothstep(CLOUD_EYE, CLOUD_EYE + 0.02, rn);
    float rim = 1.0 - smoothstep(0.8, 1.0, rn);
    return eyeRamp * rim;
}

/**
 * the large scale shape of the storm before any puffs, 0 to 1. takes noise uv.
 * a tall eyewall around the eye, spiral rainbands that thin out towards the rim, and thinLayer between them.
 * eyewallWeight is 1 where the eyewall is the tallest part and falls to 0 out in the bands
 */
float cloudMass(vec2 uv, float rn, float thinLayer, float wobble, out float eyewallWeight) {
    float eyewallDistance = (rn - CLOUD_EYEWALL) / 0.05;
    float eyewall = exp(-eyewallDistance * eyewallDistance);
    //wobbled so the bands are uneven, and thinner further out
    float bands = smoothstep(0.35, 0.75, 0.5 + 0.5 * cos(TAU * (CLOUD_ARMS * uv.x + 0.4 * wobble)));
    bands *= mix(0.75, 0.35, rn);
    eyewallWeight = eyewall / max(max(eyewall, bands), 0.001);
    //the thin layer between the bands keeps the disk closed
    return thinLayer + (1.0 - thinLayer) * max(eyewall, bands);
}

//how unevenly the bands are bent. it changes slowly, so nearby samples can share one. takes noise uv
float cloudWobble(vec2 uv) {
    return cloudFbm(vec2(uv.x * CLOUD_PERIOD, uv.y * 0.5), CLOUD_PERIOD) - 0.5;
}

/**
 * how thick the storm is here, 0 to 1. takes noise uv.
 * the mass broken up into cumulus puffs. cellOffset only moves the puffs, so both sheets agree on where the bands are
 */
//band sized puffs
float cloudSmallPuffs(vec2 uv, vec2 cellOffset) {
    return 1.0 - cloudCells(vec2(uv.x * CLOUD_PERIOD * 2.0, uv.y * 2.0) + cellOffset, CLOUD_PERIOD * 2.0);
}

//the broad towers on top of the eyewall, twice the size of the band puffs
float cloudBigPuffs(vec2 uv, vec2 cellOffset) {
    return 1.0 - cloudCells(vec2(uv.x * CLOUD_PERIOD, uv.y) + cellOffset, CLOUD_PERIOD);
}

float cloudThickness(vec2 uv, float rn, vec2 cellOffset, float wobble) {
    //nothing inside the eye or past the rim, so there is no point working out the puffs
    float edges = cloudEdges(rn);
    if (edges <= 0.0) {
        return 0.0;
    }
    float eyewallWeight;
    float mass = cloudMass(uv, rn, 0.15, wobble, eyewallWeight);

    //the eyewall is a solid wall with a few broad towers on top. its mass is the highest, so the band sized puffs
    //would cut deep narrow spikes into it. it gets puffs twice the size and a third as deep instead.
    //almost everywhere one of the two puff sizes has no weight, so only the one that shows is worked out
    float puffs;
    if (eyewallWeight < 0.01) {
        puffs = cloudSmallPuffs(uv, cellOffset);
    } else if (eyewallWeight > 0.99) {
        puffs = cloudBigPuffs(uv, cellOffset);
    } else {
        puffs = mix(cloudSmallPuffs(uv, cellOffset), cloudBigPuffs(uv, cellOffset), eyewallWeight);
    }
    float puffDepth = mix(0.4, 0.15, eyewallWeight);
    return clamp(mass * (1.0 - puffDepth + puffDepth * puffs), 0.0, 1.0) * edges;
}

float cloudThickness(vec2 uv, float rn, vec2 cellOffset) {
    return cloudThickness(uv, rn, cellOffset, cloudWobble(uv));
}

//the order TribulationCloudTransition.Style is declared in
const int CLOUD_STYLE_CORES = 0;
const int CLOUD_STYLE_RIM = 1;
const int CLOUD_STYLE_EYE = 2;
const int CLOUD_STYLE_SCATTER = 3;
const int CLOUD_STYLE_VORTEX = 4;

//the vortex eye is full size this far into gathering, and has gone out this far into breaking up
const float VORTEX_EYE_GROWN = 0.15;
//the dot of light the vortex eye grows from, as a fraction of its full size
const float VORTEX_EYE_DOT = 0.12;

//how big the vortex eye is, as a fraction of its full size. 0 once it has gone out
float cloudVortexEyeScale(float formation, bool dissipating) {
    if (dissipating) {
        //shrinks away at the very start of breaking up, before any cloud goes
        return smoothstep(1.0 - VORTEX_EYE_GROWN, 1.0, formation);
    }
    //a dot of light that quickly swells, fast at first and settling into its full size
    float grown = clamp(formation / VORTEX_EYE_GROWN, 0.0, 1.0);
    grown = 1.0 - (1.0 - grown) * (1.0 - grown);
    return mix(VORTEX_EYE_DOT, 1.0, grown);
}

//the patches the vortex gathers and breaks up in, 0 to 1. takes noise uv. finer than the wobble, so the rim gets many small clumps
float cloudVortexPatch(vec2 uv) {
    return cloudFbm(vec2(uv.x * CLOUD_PERIOD * 2.0, uv.y * 1.2) + vec2(0.0, 41.0), CLOUD_PERIOD * 2.0);
}

//the unseen source clouds past the rim that the vortex pulls its streams from, evenly spaced around it.
//must match STREAMS in TribulationCloudRenderer
const float VORTEX_STREAMS = 3.0;
//log of how far out the sources are, as a fraction of the radius. must match STREAM_SOURCE in TribulationCloudRenderer
const float VORTEX_STREAM_SOURCE = 0.47;
//half the width of a stream ribbon in turns, the same at every radius. must match STREAM_WIDTH / TAU in TribulationCloudRenderer
const float VORTEX_STREAM_WIDTH = 0.055;
//the streams start pulling once the eye has mostly grown, and their heads reach the eye this far towards closedAt
const float VORTEX_REACH_START = 0.1;
const float VORTEX_REACH_END = 0.6;

//the log rn the heads of the streams have been pulled in to
float cloudVortexStreamHead(float formation, float closedAt) {
    float reach = clamp((formation - VORTEX_REACH_START) / (closedAt * VORTEX_REACH_END - VORTEX_REACH_START), 0.0, 1.0);
    return mix(VORTEX_STREAM_SOURCE, log(CLOUD_EYE), reach);
}

/**
 * how far inside the cloud the streams have brought in this point is, negative outside it.
 * cloud settles along each stream behind its head, thin just behind the head and fanning out sideways further back,
 * and keeps spreading until the streams meet into one sea at closedAt.
 * the streams follow the mesh's spiral arms, so in mesh uv each one is a line of constant x, at a whole fraction of a turn.
 * meshUv is (spiral coordinate in turns, log rn)
 */
float cloudVortexDeposit(vec2 meshUv, float formation, float closedAt) {
    //how far behind the heads this is, in log radius. negative ahead of them, where nothing has arrived yet
    float behind = meshUv.y - cloudVortexStreamHead(formation, closedAt);
    float fromStream = abs(fract(meshUv.x * VORTEX_STREAMS + 0.5) - 0.5) / VORTEX_STREAMS;
    //how far the deposited cloud has spread out from its stream, slow at first and quicker as more is pulled in
    float spreading = smoothstep(VORTEX_REACH_START, closedAt, formation);
    float halfWidth = VORTEX_STREAM_WIDTH * 0.6 * smoothstep(0.0, 0.4, behind)
            + (0.5 / VORTEX_STREAMS + 0.05) * spreading * spreading * smoothstep(0.0, 0.5, behind);
    float inside = min((halfWidth - fromStream) * VORTEX_STREAMS * 2.0, behind * 3.0);
    //the last gaps close as the sea comes together
    float merge = smoothstep(closedAt * 0.8, closedAt, formation);
    return max(inside, merge * 1.2 - 0.8);
}

/**
 * how far inside the vortex's cloud this point is, negative where there is no cloud yet or any more.
 * patch is a 0 to 1 noise sampled in noise uv, it frays the edges. meshUv is (spiral coordinate in turns, log rn).
 * gathering: once the eye has grown, cloud streams in from past the rim and spreads out, see cloudVortexDeposit.
 * breaking up: nothing goes until the eye is out, then holes open everywhere and widen, a little sooner towards the rim
 */
float cloudVortexCoverage(float formation, bool dissipating, vec2 meshUv, float patch, float patchiness, float closedAt) {
    if (dissipating) {
        float rn = exp(meshUv.y);
        float gone = 1.0 - smoothstep(0.0, 1.0 - VORTEX_EYE_GROWN, formation);
        return patch - gone * (1.15 + (rn - 0.5) * 0.25);
    }
    return cloudVortexDeposit(meshUv, formation, closedAt) + (patch - 0.5) * (0.2 + patchiness * 0.6);
}

/**
 * whether this part of a gathering or breaking up storm has formed yet.
 * formation is 0 for clear sky and 1 for the full storm, the cloud is complete from closedAt on.
 * mass and wobble are storm scale, so both sheets agree on what has formed
 */
bool cloudGathered(int style, float formation, float mass, float wobble, float rn, float patchiness, float closedAt) {
    float gathered = smoothstep(0.0, closedAt, formation);
    if (style == CLOUD_STYLE_CORES) {
        //the eyewall and band cores first, then the thin cloud between them
        return mass + patchiness * (wobble + 0.5) >= mix(1.1, 0.0, gathered);
    }
    if (style == CLOUD_STYLE_RIM) {
        //from the outer edge inward, the eye last
        return rn + patchiness * wobble >= 1.1 - 1.2 * gathered;
    }
    if (style == CLOUD_STYLE_EYE) {
        //outward from the eye, the rim last
        return rn - patchiness * wobble <= -0.1 + 1.2 * gathered;
    }
    //scatter: random patches everywhere that merge. the vortex is handled by cloudVortexCoverage before it gets here
    return wobble + 0.5 >= 1.0 - gathered;
}
