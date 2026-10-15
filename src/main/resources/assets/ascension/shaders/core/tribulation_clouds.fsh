#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <ascension:tribulation_clouds.glsl>

in vec2 cloudUv;
in vec4 vertexColor;

out vec4 fragColor;

//tone steps, few enough that the bands stay sharp
const float LEVELS = 5.0;
const float RIM_START = 0.76;
//the glowing eye frays out to here
const float EYE_EDGE = 0.065;
const vec3 FLASH_GLOW = vec3(0.6, 0.6, 0.75);
//the dim charged glow the eye always has, before lightning
const vec3 EYE_COLOR = vec3(0.22, 0.2, 0.38);
//how fast the glow in the eye churns, per unit of flow
const float EYE_CHURN = 30.0;

void main() {
    //snap to a log polar pixel grid, the whole pixel takes the colour at its centre.
    //the grid turns with the spiral, while the billows flow inward through it.
    //pixels are square, so they shrink towards the eye like the billows do
    float pixels = TextureMat[3][0];
    float pixelLogSize = TAU / pixels;
    float pixelLog = (floor(cloudUv.y / pixelLogSize) + 0.5) * pixelLogSize;
    float pixelX = (floor(cloudUv.x * pixels) + 0.5) / pixels;
    float flow = TextureMat[3][1];
    vec2 uv = cloudNoiseUv(vec2(pixelX, pixelLog), flow);
    float rn = exp(pixelLog);

    //chaotic edges: chunky lumps plus fine fraying, same pattern on both sheets so the disk stays closed
    float fray = cloudFbm(vec2(uv.x * CLOUD_PERIOD, uv.y * 0.6), CLOUD_PERIOD) * 0.55
            + cloudFbm(vec2(uv.x * CLOUD_PERIOD * 4.0, uv.y * 2.5) + vec2(0.0, 71.0), CLOUD_PERIOD * 4.0) * 0.45;
    if (rn > RIM_START && fray < smoothstep(RIM_START, 1.0, rn) * 1.1) {
        discard;
    }

    //lightning glow arrives in the vertex alpha, stepped like the tones
    float glow = floor(clamp(vertexColor.a, 0.0, 1.0) * 3.0 + 0.5) / 3.0;
    vec3 glowColor = FLASH_GLOW * mix(vec3(1.0), vertexColor.rgb, 0.6);

    //the eye glows like a charged core with a frayed edge, and flares when lightning strikes
    if (rn < CLOUD_EYE || (rn < EYE_EDGE && fray < 1.0 - smoothstep(CLOUD_EYE, EYE_EDGE, rn))) {
#ifdef TOP_LAYER
        //only the underside draws the eye, both sheets meet there
        discard;
#endif
        float churn = cloudFbm(vec2(uv.x * CLOUD_PERIOD * 2.0, uv.y * 3.0 - flow * EYE_CHURN), CLOUD_PERIOD * 2.0);
        float energy = 0.3 + 0.5 * smoothstep(0.35, 0.8, churn) + glow * 1.5;
        energy = floor(energy * 4.0) / 4.0;
        vec3 eye = EYE_COLOR * mix(vec3(1.0), vertexColor.rgb, 0.5) * energy + glowColor * glow;
        fragColor = vec4(min(eye, vec3(1.0)), 1.0);
        return;
    }

#ifdef TOP_LAYER
    vec2 cellOffset = vec2(5.0, 31.0);
#else
    vec2 cellOffset = vec2(0.0);
#endif
    //slope of the surface, x around the eye and y outward
    float thickness = cloudThickness(uv, rn, cellOffset);
    float slopeAround = cloudThickness(uv + vec2(0.02, 0.0), rn, cellOffset) - thickness;
    float slopeOut = cloudThickness(uv + vec2(0.0, 0.3), rn, cellOffset) - thickness;

#ifdef TOP_LAYER
    //brighter the higher it reaches, with shadows on slopes facing away from the light
    float tone = 0.5 + thickness * 0.45 - slopeOut * 2.0 - slopeAround * 1.0;
#else
    //thicker cloud lets less light through, so the eyewall and bands are dark and the thin cloud between them lighter
    float tone = 0.62 - thickness * 0.45 + slopeOut * 2.0 + slopeAround * 0.8;
    tone *= mix(0.8, 1.0, smoothstep(CLOUD_EYE, 0.3, rn));
#endif

    float level = min(floor(clamp(tone, 0.0, 1.0) * LEVELS), LEVELS - 1.0) / (LEVELS - 1.0);
    float grey = mix(0.2, 0.9, level);

    fragColor = vec4(min(vertexColor.rgb * grey + glowColor * glow, vec3(1.0)), 1.0);
}
