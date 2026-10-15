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
    float nearest = 8.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 neighbour = vec2(float(x), float(y));
            vec2 key = vec2(mod(i.x + neighbour.x, period), i.y + neighbour.y);
            vec2 point = neighbour + vec2(cloudHash(key), cloudHash(key + 19.19));
            nearest = min(nearest, length(point - f));
        }
    }
    return nearest;
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
float cloudThickness(vec2 uv, float rn, vec2 cellOffset, float wobble) {
    float eyewallWeight;
    float mass = cloudMass(uv, rn, 0.15, wobble, eyewallWeight);

    float cells = 1.0 - cloudCells(vec2(uv.x * CLOUD_PERIOD * 2.0, uv.y * 2.0) + cellOffset, CLOUD_PERIOD * 2.0);
    //the eyewall is a solid wall with a few broad towers on top. its mass is the highest, so the band sized puffs
    //would cut deep narrow spikes into it. it gets puffs twice the size and a third as deep instead
    float bigCells = 1.0 - cloudCells(vec2(uv.x * CLOUD_PERIOD, uv.y) + cellOffset, CLOUD_PERIOD);
    float puffs = mix(cells, bigCells, eyewallWeight);
    float puffDepth = mix(0.4, 0.15, eyewallWeight);
    return clamp(mass * (1.0 - puffDepth + puffDepth * puffs), 0.0, 1.0) * cloudEdges(rn);
}

float cloudThickness(vec2 uv, float rn, vec2 cellOffset) {
    return cloudThickness(uv, rn, cellOffset, cloudWobble(uv));
}

//the order TribulationCloudTransition.Style is declared in
const int CLOUD_STYLE_CORES = 0;
const int CLOUD_STYLE_RIM = 1;
const int CLOUD_STYLE_EYE = 2;

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
    //scatter: random patches everywhere that merge
    return wobble + 0.5 >= 1.0 - gathered;
}
