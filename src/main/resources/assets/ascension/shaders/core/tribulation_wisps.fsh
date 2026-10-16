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

//must match LAYERS in WispyTribulationCloudRenderer
const float WISP_LAYERS = 3.0;
//extra twist on top of the mesh spiral, so the streaks wind round the eye more tightly than the arms do
const float STREAK_SHEAR = 0.45;
//the wisps start fraying out here and are gone by the rim
const float WISP_RIM = 0.72;
//radius of the glowing eye, as a fraction of the cloud radius
const float WISP_EYE = 0.035;
//how far the eye's light reaches into the clouds
const float EYE_LIGHT = 0.45;
//how fast the glow in the eye churns, per unit of flow
const float EYE_CHURN = 60.0;

//1 along thin lines where the noise crosses its middle, for the bright filaments
float cloudRidge(vec2 p, float period) {
    return 1.0 - abs(cloudFbm(p, period) * 2.0 - 1.0);
}

//how much of this part of a gathering or breaking up storm is there, soft so the wisps fade in instead of popping.
//the same shapes as cloudGathered, with the streak density standing in for the mass
float wispGathered(int style, float formation, float density, float wobble, float rn, float patchiness, float closedAt) {
    if (formation >= closedAt) {
        return 1.0;
    }
    float gathered = smoothstep(0.0, closedAt, formation);
    float value;
    float threshold;
    if (style == CLOUD_STYLE_CORES) {
        //the thickest streaks first, then the haze between them
        value = density + patchiness * (wobble + 0.5);
        threshold = mix(1.1, 0.0, gathered);
    } else if (style == CLOUD_STYLE_RIM) {
        value = rn + patchiness * wobble;
        threshold = 1.1 - 1.2 * gathered;
    } else if (style == CLOUD_STYLE_EYE) {
        value = patchiness * wobble - rn;
        threshold = 0.1 - 1.2 * gathered;
    } else {
        value = wobble + 0.5;
        threshold = 1.0 - gathered;
    }
    return smoothstep(threshold - 0.08, threshold + 0.08, value);
}

void main() {
    float flow = TextureMat[3][1];
    //how far the storm has gathered, 0 for clear sky and 1 for the full storm, and how the current transition does it
    float formation = TextureMat[2][0];
    int style = int(TextureMat[2][1] + 0.5);
    float patchiness = TextureMat[2][2];
    float closedAt = TextureMat[2][3];
    float eyeAt = TextureMat[0][3];
    float layer = TextureMat[1][3];

    float rn = exp(cloudUv.y);
    //each layer gets its own streaks. the shift is along the streaks, so the noise still tiles around the circle
    vec2 seed = vec2(0.0, layer * 23.7);
    //lines of constant s are tight spirals. the noise changes slowly along them and quickly across them, so it stretches into streaks.
    //the flow slides the pattern along the streaks, so the wisps drift into the eye
    float s = cloudUv.x + STREAK_SHEAR * cloudUv.y;
    float along = cloudUv.y + flow;

    //big slow curls bend the streaks, so they swirl instead of running in perfect spirals
    float warp = cloudFbm(vec2(s * 5.0, along * 0.7) + seed, 5.0) - 0.5;
    s += warp * 0.08;

    //broad bands of thicker and thinner cloud
    float broad = cloudFbm(vec2(s * 3.0, along * 0.4) + seed + vec2(0.0, 7.0), 3.0);
    //long thin streaks, and bright filaments running through them
    float streaks = cloudFbm(vec2(s * 40.0, along * 1.6) + seed, 40.0);
    float density = smoothstep(0.3, 0.8, streaks * mix(0.6, 1.25, broad));
    float filaments = pow(cloudRidge(vec2(s * 90.0, along * 2.5) + seed + vec2(0.0, 3.0), 90.0), 6.0);

    //the wisps fray out raggedly at the rim and thin out where they spiral into the eye
    float rimFray = cloudFbm(vec2(s * 8.0, along * 0.8) + seed + vec2(0.0, 11.0), 8.0);
    float rim = 1.0 - smoothstep(WISP_RIM, 1.0, rn + (rimFray - 0.5) * 0.25);
    float inner = smoothstep(WISP_EYE * 0.6, WISP_EYE * 2.5, rn);
    //the lowest layer carries the colour, the thinner ones above add depth
    float layerOpacity = mix(0.9, 0.45, layer / (WISP_LAYERS - 1.0));

    float alpha = (density * 0.85 + filaments * 0.35) * rim * inner * layerOpacity;
    alpha *= wispGathered(style, formation, density, broad - 0.5, rn, patchiness, closedAt);

    //lightning glow arrives in the vertex alpha. the lightning comes from the eye, so it glows in its highlight
    float glow = clamp(vertexColor.a, 0.0, 1.0);
    //light from the eye catches the clouds around it, once the eye has lit
    float eyeOn = smoothstep(eyeAt, 1.0, formation);
    float eyeLight = (1.0 - smoothstep(0.0, EYE_LIGHT, rn)) * eyeOn;

    float tone = clamp(0.15 + density * 0.55 + filaments * 0.4 + eyeLight * 0.3, 0.0, 1.0);
    //the vertex colour is the tint
    vec3 color = cloudRamp(cloudShadow, cloudBase, cloudHighlight, tone) * vertexColor.rgb;
    color += eyeHighlight * (eyeLight * eyeLight * 0.6 + glow);

    //only the lowest layer draws the eye, a churning core of light the streaks pour into
    if (layer < 0.5 && rn < WISP_EYE * 2.5) {
        float churn = cloudFbm(vec2(s * 12.0, cloudUv.y * 3.0 - flow * EYE_CHURN), 12.0);
        float core = 1.0 - smoothstep(WISP_EYE * 0.3, WISP_EYE * 2.5, rn + (churn - 0.5) * WISP_EYE);
        float energy = clamp(core * (0.6 + 0.4 * churn) + glow, 0.0, 1.0);
        float eyeAlpha = core * eyeOn;
        //the tint only colours the clouds, the eye has its own palette
        color = mix(color, cloudRamp(eyeShadow, eyeBase, eyeHighlight, energy), eyeAlpha);
        alpha = max(alpha, eyeAlpha);
    }

    if (alpha < 0.004) {
        discard;
    }
    fragColor = vec4(min(color, vec3(1.0)), clamp(alpha, 0.0, 1.0));
}
