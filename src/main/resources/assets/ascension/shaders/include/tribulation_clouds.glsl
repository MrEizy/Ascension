//shared by the tribulation_clouds vertex and fragment shaders.
//the mesh uv is (spiral coordinate in turns around the eye, log of rn),
//where rn is the distance from the eye divided by the cloud radius.
//the noise is sampled in "noise uv", see cloudNoiseUv

const float TAU = 6.28318530718;
//noise cells around the circle, must stay a whole number so the noise tiles
const float CLOUD_PERIOD = 16.0;
const float CLOUD_ARMS = 3.0;
//radius of the black eye, as a fraction of the cloud radius
const float CLOUD_EYE = 0.025;
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

//0 at the eye and the outer rim, so the top and bottom sheets meet and close the disk
float cloudEnvelope(float rn) {
    return smoothstep(CLOUD_EYE, 0.15, rn) * (1.0 - smoothstep(0.8, 1.0, rn));
}

//the billows drift inward as flow grows, flow comes from the texture matrix translation
vec2 cloudNoiseUv(vec2 meshUv, float flow) {
    return vec2(meshUv.x, meshUv.y * CLOUD_RADIAL_SCALE + flow);
}

//how far the cloud surface bulges out from the base, 0 to 1. takes noise uv
float cloudHeight(vec2 uv, float rn, vec2 offset) {
    float billows = cloudFbm(vec2(uv.x * CLOUD_PERIOD, uv.y) + offset, CLOUD_PERIOD);
    //the spiral only shapes the surface, it never opens gaps
    float streak = 0.5 + 0.5 * cos(TAU * CLOUD_ARMS * uv.x);
    return smoothstep(0.2, 0.85, mix(billows, streak, 0.25)) * cloudEnvelope(rn);
}
