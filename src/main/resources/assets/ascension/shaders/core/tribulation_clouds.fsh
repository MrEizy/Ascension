#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <ascension:tribulation_clouds.glsl>

in vec2 cloudUv;
in vec4 vertexColor;

out vec4 fragColor;

//tone steps, few enough that the bands stay sharp
const float LEVELS = 5.0;
const float RIM_START = 0.76;
//the black eye frays out to here
const float EYE_EDGE = 0.05;
const vec3 FLASH_GLOW = vec3(0.6, 0.6, 0.75);

void main() {
    //snap to a log polar pixel grid, the whole pixel takes the colour at its centre.
    //the grid turns with the spiral, while the billows flow inward through it.
    //pixels are square, so they shrink towards the eye like the billows do
    float pixels = TextureMat[3][0];
    float pixelLogSize = TAU / pixels;
    float pixelLog = (floor(cloudUv.y / pixelLogSize) + 0.5) * pixelLogSize;
    float pixelX = (floor(cloudUv.x * pixels) + 0.5) / pixels;
    vec2 uv = cloudNoiseUv(vec2(pixelX, pixelLog), TextureMat[3][1]);
    float rn = exp(pixelLog);

    //chaotic edges: chunky lumps plus fine fraying, same pattern on both sheets so the disk stays closed
    float fray = cloudFbm(vec2(uv.x * CLOUD_PERIOD, uv.y * 0.6), CLOUD_PERIOD) * 0.55
            + cloudFbm(vec2(uv.x * CLOUD_PERIOD * 4.0, uv.y * 2.5) + vec2(0.0, 71.0), CLOUD_PERIOD * 4.0) * 0.45;
    if (rn > RIM_START && fray < smoothstep(RIM_START, 1.0, rn) * 1.1) {
        discard;
    }
    //the eye is solid black, with a frayed edge
    if (rn < CLOUD_EYE || (rn < EYE_EDGE && fray < 1.0 - smoothstep(CLOUD_EYE, EYE_EDGE, rn))) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

#ifdef TOP_LAYER
    vec2 offset = vec2(5.0, 31.0);
#else
    vec2 offset = vec2(0.0);
#endif
    //slope of the surface, x around the eye and y outward
    float height = cloudHeight(uv, rn, offset);
    float slopeAround = cloudHeight(uv + vec2(0.02, 0.0), rn, offset) - height;
    float slopeOut = cloudHeight(uv + vec2(0.0, 0.3), rn, offset) - height;

    //thin flow lines stretched along the spiral
    float streak = cloudFbm(vec2(uv.x * CLOUD_PERIOD * 3.0, uv.y * 0.3), CLOUD_PERIOD * 3.0) - 0.5;

#ifdef TOP_LAYER
    //lit from above, with a rim light on billows facing outward
    float tone = 0.45 + height * 0.4 - slopeOut * 2.5 - slopeAround * 1.0 + streak * 0.15;
#else
    //darker underside, edges facing the eye catch the lightning
    float tone = 0.28 + height * 0.35 + slopeOut * 2.5 + slopeAround * 0.8 + streak * 0.15;
#endif
    //darker towards the eye
    tone *= mix(0.6, 1.0, smoothstep(CLOUD_EYE, 0.4, rn));

    float level = min(floor(clamp(tone, 0.0, 1.0) * LEVELS), LEVELS - 1.0) / (LEVELS - 1.0);
    float grey = mix(0.25, 0.9, level);

    //lightning glow arrives in the vertex alpha, stepped like the tones
    float glow = floor(clamp(vertexColor.a, 0.0, 1.0) * 3.0 + 0.5) / 3.0;
    vec3 glowColor = FLASH_GLOW * mix(vec3(1.0), vertexColor.rgb, 0.6);

    fragColor = vec4(min(vertexColor.rgb * grey + glowColor * glow, vec3(1.0)), 1.0);
}
