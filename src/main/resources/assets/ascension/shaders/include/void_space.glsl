#version 330

// Screen-space starry void shared by the void qi coatings and sword qi. Stars and nebula are placed by gl_FragCoord,
// so they stay put while whatever shows them moves across the screen.

float voidSpaceHash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float voidSpaceNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 w = f * f * (3.0 - 2.0 * f);
    float a = voidSpaceHash(i);
    float b = voidSpaceHash(i + vec2(1.0, 0.0));
    float c = voidSpaceHash(i + vec2(0.0, 1.0));
    float d = voidSpaceHash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, w.x), mix(c, d, w.x), w.y);
}

float voidSpaceFbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.55;
    for (int octave = 0; octave < 4; octave++) {
        value += voidSpaceNoise(p) * amplitude;
        p *= 2.0;
        amplitude *= 0.5;
    }
    return value;
}

vec3 voidSpace(float t) {
    vec2 screen = gl_FragCoord.xy;
    float cloud = voidSpaceFbm(screen / 220.0 + vec2(t * 0.02, 0.0));
    float cloudMask = smoothstep(0.35, 0.85, voidSpaceFbm(screen / 140.0 - vec2(0.0, t * 0.015)));
    vec3 color = vec3(0.004, 0.004, 0.02) + mix(vec3(0.12, 0.02, 0.22), vec3(0.02, 0.10, 0.26), cloud) * cloudMask * 0.8;

    for (int layer = 0; layer < 3; layer++) {
        float fl = float(layer);
        float cellSize = 9.0 + fl * 7.0;
        vec2 p = screen / cellSize + vec2(t * 0.05 * (fl + 1.0), fl * 31.0);
        vec2 cell = floor(p);
        float h = voidSpaceHash(cell + fl * 17.0);
        if (h > 0.86) {
            vec2 jitter = (vec2(voidSpaceHash(cell + 3.1), voidSpaceHash(cell + 7.7)) - 0.5) * 0.6;
            float d = length(fract(p) - 0.5 - jitter) * cellSize;
            float radius = 0.7 + fl * 0.35;
            float twinkle = 0.65 + 0.35 * sin(t * (2.0 + h * 4.0) + h * 6.28318530718);
            vec3 starColor = mix(vec3(0.75, 0.80, 1.00), vec3(1.00, 0.85, 0.95), voidSpaceHash(cell + 11.0));
            color += starColor * exp(-d * d / (radius * radius)) * twinkle * (1.4 - fl * 0.3);
        }
    }
    return color;
}
