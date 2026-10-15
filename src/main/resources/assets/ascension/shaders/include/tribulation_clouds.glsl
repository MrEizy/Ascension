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

//the billows drift inward as flow grows, flow comes from the texture matrix translation
vec2 cloudNoiseUv(vec2 meshUv, float flow) {
    return vec2(meshUv.x, meshUv.y * CLOUD_RADIAL_SCALE + flow);
}

/**
 * how thick the storm is here, 0 to 1. takes noise uv.
 * a tall eyewall around the eye, spiral rainbands that thin out towards the rim, a thin layer between them,
 * all broken up into cumulus puffs. cellOffset only moves the puffs, so both sheets agree on where the bands are
 */
float cloudThickness(vec2 uv, float rn, vec2 cellOffset) {
    //0 inside the eye and rising steeply, so the eyewall gets a wall on the inside
    float eyeRamp = smoothstep(CLOUD_EYE, CLOUD_EYE + 0.02, rn);
    float rim = 1.0 - smoothstep(0.8, 1.0, rn);

    float eyewallDistance = (rn - CLOUD_EYEWALL) / 0.05;
    float eyewall = exp(-eyewallDistance * eyewallDistance);
    //wobbled so the bands are uneven, and thinner further out
    float wobble = cloudFbm(vec2(uv.x * CLOUD_PERIOD, uv.y * 0.5), CLOUD_PERIOD) - 0.5;
    float bands = smoothstep(0.35, 0.75, 0.5 + 0.5 * cos(TAU * (CLOUD_ARMS * uv.x + 0.4 * wobble)));
    bands *= mix(0.75, 0.35, rn);
    //the thin layer between the bands keeps the disk closed
    float mass = 0.15 + 0.85 * max(eyewall, bands);

    float cells = 1.0 - cloudCells(vec2(uv.x * CLOUD_PERIOD * 2.0, uv.y * 2.0) + cellOffset, CLOUD_PERIOD * 2.0);
    //the eyewall is a solid wall with a few broad towers on top. its mass is the highest, so the band sized puffs
    //would cut deep narrow spikes into it. it gets puffs twice the size and a third as deep instead
    float eyewallWeight = eyewall / max(max(eyewall, bands), 0.001);
    float bigCells = 1.0 - cloudCells(vec2(uv.x * CLOUD_PERIOD, uv.y) + cellOffset, CLOUD_PERIOD);
    float puffs = mix(cells, bigCells, eyewallWeight);
    float puffDepth = mix(0.4, 0.15, eyewallWeight);
    return clamp(mass * (1.0 - puffDepth + puffDepth * puffs), 0.0, 1.0) * eyeRamp * rim;
}
