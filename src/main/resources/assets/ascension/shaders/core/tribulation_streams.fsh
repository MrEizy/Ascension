#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <ascension:tribulation_clouds.glsl>

in vec2 streamUv;
in vec4 vertexColor;
flat in vec3 cloudShadow;
flat in vec3 cloudBase;
flat in vec3 cloudHighlight;
flat in vec3 eyeHighlight;

out vec4 fragColor;

//the noise never wraps around a stream, so any period that is never reached does
const float STREAM_PERIOD = 256.0;
//how far the eye's light reaches along the streams
const float EYE_LIGHT = 0.45;

void main() {
    float flow = TextureMat[3][1];
    float formation = TextureMat[2][0];
    float closedAt = TextureMat[2][3];

    float across = streamUv.x;
    float logRn = streamUv.y;
    float rn = exp(logRn);
    float head = cloudVortexStreamHead(formation, closedAt);
    //the cloud slides along the stream towards the eye, stretched out the way it is pulled
    float along = logRn + flow;
    float body = cloudFbm(vec2(across * 1.5, along * 3.0) + vec2(0.0, 5.0), STREAM_PERIOD);
    float streaks = cloudFbm(vec2(across * 4.0, along * 9.0), STREAM_PERIOD);
    float density = smoothstep(0.3, 0.8, body * 0.55 + streaks * 0.55);

    //thins to a ragged thread right behind its head, fuller further back
    float behind = logRn - head;
    float fullness = smoothstep(0.0, 0.8, behind);
    float edge = 1.0 - smoothstep(mix(0.1, 0.5, fullness), 1.0, abs(across) + (body - 0.5) * 0.6);
    //a feathered head, and the far end fading into the unseen source cloud
    float headFade = smoothstep(-0.05, 0.25, behind + (body - 0.5) * 0.3);
    float sourceFade = 1.0 - smoothstep(VORTEX_STREAM_SOURCE - 0.35, VORTEX_STREAM_SOURCE, logRn);
    //swallowed by the sea as it closes
    float closing = 1.0 - smoothstep(closedAt * 0.8, closedAt, formation);

    float alpha = edge * (0.3 + 0.7 * density) * headFade * sourceFade * closing * 0.9;
    if (alpha < 0.004) {
        discard;
    }

    //lit up by the eye as it is pulled in close
    float eyeLight = 1.0 - smoothstep(0.0, EYE_LIGHT, rn);
    float tone = clamp(0.2 + density * 0.6 + eyeLight * 0.2, 0.0, 1.0);
    //the vertex colour is the tint
    vec3 color = cloudRamp(cloudShadow, cloudBase, cloudHighlight, tone) * vertexColor.rgb + eyeHighlight * eyeLight * eyeLight * 0.5;
    fragColor = vec4(min(color, vec3(1.0)), clamp(alpha, 0.0, 1.0));
}
