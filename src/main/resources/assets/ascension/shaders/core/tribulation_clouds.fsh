#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <ascension:tribulation_clouds.glsl>

in vec2 cloudUv;
in vec4 vertexColor;
flat in vec3 cloudShadow;
flat in vec3 cloudBase;
flat in vec3 cloudHighlight;
flat in vec3 eyeShadow;
flat in vec3 eyeBase;
flat in vec3 eyeHighlight;

out vec4 fragColor;

//tone steps, few enough that the bands stay sharp
const float LEVELS = 5.0;
//the glowing eye frays out to here
const float EYE_EDGE = 0.065;
//how fast the glow in the eye churns, per unit of flow
const float EYE_CHURN = 30.0;

//which of the tone steps this is, from 0 to 1
float posterize(float tone) {
    return min(floor(clamp(tone, 0.0, 1.0) * LEVELS), LEVELS - 1.0) / (LEVELS - 1.0);
}

void main() {
    float flow = TextureMat[3][1];
    //how far the storm has gathered, 0 for clear sky and 1 for the full storm, and how the current transition does it
    float formation = TextureMat[2][0];
    int style = int(TextureMat[2][1] + 0.5);
    float patchiness = TextureMat[2][2];
    float closedAt = TextureMat[2][3];
    float eyeAt = TextureMat[0][3];
    //snap to a log polar pixel grid, the whole pixel takes the colour at its centre.
    //the grid turns with the spiral, while the billows flow inward through it.
    //pixels are square, so they shrink towards the eye like the billows do
    float pixels = TextureMat[3][0];
    float pixelLogSize = TAU / pixels;
    float pixelLog = (floor(cloudUv.y / pixelLogSize) + 0.5) * pixelLogSize;
    float pixelX = (floor(cloudUv.x * pixels) + 0.5) / pixels;
    vec2 uv = cloudNoiseUv(vec2(pixelX, pixelLog), flow);
    float rn = exp(pixelLog);

    //lightning glow arrives in the vertex alpha, stepped like the tones. the lightning comes from the eye, so it glows in its highlight
    float glow = floor(clamp(vertexColor.a, 0.0, 1.0) * 3.0 + 0.5) / 3.0;
    vec3 glowColor = eyeHighlight;

    //the fray noise only matters at the rim and around the eye, everywhere else it is skipped.
    //1 never cuts anything away, at the rim or at the eye
    float fray = rn > CLOUD_RIM_START || rn < EYE_EDGE ? cloudFray(uv) : 1.0;
    if (cloudFrayed(fray, rn)) {
        discard;
    }

    //the eye glows like a charged core with a frayed edge, and flares when lightning strikes
    if (rn < CLOUD_EYE || (rn < EYE_EDGE && fray < 1.0 - smoothstep(CLOUD_EYE, EYE_EDGE, rn))) {
#ifdef TOP_LAYER
        //only the underside draws the eye, both sheets meet there
        discard;
#endif
        //the eye is the last thing to form, and the first to go dark when the storm breaks up
        if (formation < eyeAt) {
            discard;
        }
        float churn = cloudFbm(vec2(uv.x * CLOUD_PERIOD * 2.0, uv.y * 3.0 - flow * EYE_CHURN), CLOUD_PERIOD * 2.0);
        float energy = 0.3 + 0.5 * smoothstep(0.35, 0.8, churn) + glow * 1.5;
        energy *= smoothstep(eyeAt, 1.0, formation);
        energy = floor(energy * 4.0) / 4.0;
        //the tint only colours the clouds, the eye has its own palette
        vec3 eye = cloudRamp(eyeShadow, eyeBase, eyeHighlight, clamp(energy, 0.0, 1.0)) + glowColor * glow;
        fragColor = vec4(min(eye, vec3(1.0)), 1.0);
        return;
    }

#ifdef TOP_LAYER
    vec2 cellOffset = vec2(5.0, 31.0);
#else
    vec2 cellOffset = vec2(0.0);
#endif
    float wobble = cloudWobble(uv);
    //a gathering storm only shows the parts its style has formed so far, breaking up runs the same style backwards
    if (formation < closedAt) {
        float eyewallWeight;
        float mass = cloudMass(uv, rn, 0.15, wobble, eyewallWeight);
        if (!cloudGathered(style, formation, mass, wobble, rn, patchiness, closedAt)) {
            discard;
        }
    }

    //slope of the surface, x around the eye and y outward. the wobble barely changes over such a short step, so all three share one
    float thickness = cloudThickness(uv, rn, cellOffset, wobble);
    float slopeAround = cloudThickness(uv + vec2(0.02, 0.0), rn, cellOffset, wobble) - thickness;
    float slopeOut = cloudThickness(uv + vec2(0.0, 0.3), rn, cellOffset, wobble) - thickness;

#ifdef TOP_LAYER
    //brighter the higher it reaches, with shadows on slopes facing away from the light
    float tone = 0.5 + thickness * 0.45 - slopeOut * 2.0 - slopeAround * 1.0;
#else
    //thicker cloud lets less light through, so the eyewall and bands are dark and the thin cloud between them lighter
    float tone = 0.62 - thickness * 0.45 + slopeOut * 2.0 + slopeAround * 0.8;
    tone *= mix(0.8, 1.0, smoothstep(CLOUD_EYE, 0.3, rn));
#endif

    //the vertex colour is the tint
    vec3 cloud = cloudRamp(cloudShadow, cloudBase, cloudHighlight, posterize(tone)) * vertexColor.rgb;
    fragColor = vec4(min(cloud + glowColor * glow, vec3(1.0)), 1.0);
}
