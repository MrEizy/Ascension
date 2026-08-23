#version 150

layout(std140) uniform DivineSenseWave {
    mat4 InverseView;
    mat4 InverseProjection;
    vec4 CameraAndDepthMode;
    vec4 CenterAndRadius;
    vec4 ColorAndProgress;
    vec4 WaveParameters;
};

uniform sampler2D WorldDepth;

in vec2 screenUv;

out vec4 fragColor;

const float TAU = 6.28318530718;

vec3 reconstructWorldPosition(float depth) {
    float ndcDepth = mix(depth * 2.0 - 1.0, depth, CameraAndDepthMode.w);
    vec4 viewPosition = InverseProjection * vec4(screenUv * 2.0 - 1.0, ndcDepth, 1.0);
    vec3 cameraOffset = viewPosition.xyz / max(abs(viewPosition.w), 0.00001);
    vec3 worldOffset = (InverseView * vec4(cameraOffset, 0.0)).xyz;
    return CameraAndDepthMode.xyz + worldOffset;
}

float spiritualFlow(vec3 worldPosition, float time) {
    float drift = worldPosition.y * 2.15;
    drift += sin(worldPosition.x * 0.72 + time * 1.8) * 1.4;
    drift += cos(worldPosition.z * 0.61 - time * 1.45) * 1.2;
    return 0.5 + 0.5 * sin(drift * 2.6 - time * 3.4);
}

void main() {
    float depth = texture(WorldDepth, screenUv).r;
    if (depth >= 1.0) {
        fragColor = vec4(0.0);
        return;
    }

    vec3 worldPosition = reconstructWorldPosition(depth);
    vec3 center = CenterAndRadius.xyz;
    float radius = CenterAndRadius.w;
    float progress = ColorAndProgress.w;
    float time = WaveParameters.x;
    float trailWidth = WaveParameters.y;
    float frontWidth = WaveParameters.z;
    vec3 baseColor = ColorAndProgress.rgb;

    float distanceFromCenter = distance(worldPosition, center);
    float distanceBehindFront = radius - distanceFromCenter;

    float originFlare = (1.0 - smoothstep(0.0, 0.12, progress))
            * (1.0 - smoothstep(0.75, 3.25, distanceFromCenter));

    float pulse = 0.0;
    vec3 pulseColor = vec3(0.0);

    if (distanceBehindFront >= 0.0 && distanceBehindFront <= trailWidth) {
        float trailPosition = distanceBehindFront / trailWidth;
        float trail = pow(1.0 - trailPosition, 1.45);
        float front = 1.0 - smoothstep(0.0, frontWidth, distanceBehindFront);
        float flow = smoothstep(0.58, 0.96, spiritualFlow(worldPosition, time));
        float echoWave = 0.5 + 0.5 * cos(trailPosition * TAU * 2.0 - time * 2.7);
        float echo = pow(echoWave, 7.0) * smoothstep(frontWidth, trailWidth, distanceBehindFront);

        vec3 deepColor = baseColor * 0.22;
        vec3 brightColor = mix(baseColor, vec3(1.0), 0.58);

        pulseColor = mix(deepColor, baseColor * 1.15, trail);
        pulseColor += baseColor * flow * trail * 0.42;
        pulseColor += brightColor * echo * trail * 0.30;
        pulseColor += brightColor * front * 1.85;
        pulse = trail * 0.72 + front;
    }

    vec3 flareColor = mix(baseColor, vec3(1.0), 0.72) * originFlare * 1.75;
    vec3 finalColor = pulseColor + flareColor;
    float alpha = clamp(pulse + originFlare, 0.0, 1.0);

    fragColor = vec4(finalColor, alpha);
}
