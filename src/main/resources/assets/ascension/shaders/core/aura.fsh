#version 330

in vec2 auraUv;
in vec4 vertexColor;

out vec4 fragColor;

const float TAU = 6.28318530718;

float flameField(float u, float v) {
    float field = sin(u * TAU * 3.0 + v * 10.0) * 0.32;
    field += sin(u * TAU * 7.0 - v * 15.0 + 1.7) * 0.20;
    field += sin(u * TAU * 11.0 + v * 23.0 + 3.2) * 0.11;
    return field;
}

float flowingField(float u, float v) {
    float field = sin(u * TAU * 2.0 + v * 6.0) * 0.22;
    field += sin(u * TAU * 4.0 - v * 8.0 + 1.2) * 0.13;
    field += sin(u * TAU * 6.0 + v * 11.0 + 2.6) * 0.07;
    return field;
}

float mistField(float u, float v) {
    float field = sin(u * TAU * 2.0 + v * 4.0) * 0.18;
    field += sin(u * TAU * 5.0 - v * 5.5 + 2.1) * 0.10;
    return field;
}

float stormField(float u, float v) {
    float field = sin(u * TAU * 5.0 + v * 14.0) * 0.35;
    field += sin(u * TAU * 11.0 - v * 22.0 + 1.4) * 0.23;
    field += sin(u * TAU * 17.0 + v * 31.0 + 3.0) * 0.14;
    return field;
}

void main() {
    float style = floor(max(0.0, auraUv.x) / 8.0);
    float u = fract(auraUv.x);
    float v = clamp(auraUv.y, 0.0, 1.0);
    float field;
    float upperBase;
    float upperRange;
    float alphaFloor;
    float heatScale;

    if (style < 0.5) {
        field = flameField(u, v);
        upperBase = 0.72;
        upperRange = 0.18;
        alphaFloor = 0.42;
        heatScale = 0.42;
    } else if (style < 1.5) {
        field = flowingField(u, v);
        upperBase = 0.80;
        upperRange = 0.12;
        alphaFloor = 0.52;
        heatScale = 0.28;
    } else if (style < 2.5) {
        field = mistField(u, v);
        upperBase = 0.90;
        upperRange = 0.07;
        alphaFloor = 0.28;
        heatScale = 0.18;
    } else {
        field = stormField(u, v);
        upperBase = 0.67;
        upperRange = 0.22;
        alphaFloor = 0.34;
        heatScale = 0.48;
    }

    float lowerFade = smoothstep(0.0, style < 2.5 && style > 1.5 ? 0.14 : 0.07, v);
    float upperEdge = upperBase + field * upperRange;
    float upperFade = 1.0 - smoothstep(upperEdge, 1.0, v);
    float streak = smoothstep(-0.28, 0.32, field + (1.0 - v) * 0.18);
    float alpha = vertexColor.a * lowerFade * upperFade * mix(alphaFloor, 1.0, streak);

    if (alpha <= 0.008) {
        discard;
    }

    float heat = clamp((1.0 - v) * heatScale * 0.66 + streak * heatScale * 0.34, 0.0, heatScale);
    vec3 color = mix(vertexColor.rgb, vec3(1.0), heat);
    fragColor = vec4(color, alpha);
}
