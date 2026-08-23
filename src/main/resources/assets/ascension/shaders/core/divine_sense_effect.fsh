#version 330

layout(std140) uniform DivineSenseEffect {
    mat4 invViewMat;
    mat4 invProjMat;
    vec4 cameraPos;
    vec4 centerAndRadius;
    vec4 colorAndDepthMode;
};

uniform sampler2D DepthSampler;

in vec2 texCoord0;

out vec4 fragColor;

const float width = 5.0;
const float edgeSharpness = 8.0;

float scanlines() {
    return sin(gl_FragCoord.y * 1.35) * 0.5 + 0.5;
}

vec3 worldpos(float depth) {
    float z = colorAndDepthMode.w > 0.5 ? depth : depth * 2.0 - 1.0;
    vec4 clipSpacePosition = vec4(texCoord0 * 2.0 - 1.0, z, 1.0);
    vec4 viewSpacePosition = invProjMat * clipSpacePosition;
    viewSpacePosition /= viewSpacePosition.w;
    vec4 worldSpacePosition = invViewMat * viewSpacePosition;

    return cameraPos.xyz + worldSpacePosition.xyz;
}

void main() {
    float depth = texture(DepthSampler, texCoord0).r;
    if (depth >= 1.0) {
        fragColor = vec4(0.0);
        return;
    }

    vec3 center = centerAndRadius.xyz;
    float radius = centerAndRadius.w;
    vec3 pos = worldpos(depth);
    float dist = distance(pos, center);

    if (dist >= radius || dist <= radius - width) {
        fragColor = vec4(0.0);
        return;
    }

    float diff = 1.0 - (radius - dist) / width;
    float edge = pow(diff, edgeSharpness);
    float line = scanlines();
    vec3 baseColor = colorAndDepthMode.rgb;
    vec3 innerColor = baseColor * 0.35;
    vec3 outerColor = mix(baseColor * 1.35, vec3(1.0), 0.35);
    vec3 color = mix(innerColor, outerColor, edge);
    color += baseColor * line * 0.55;
    color *= diff;

    fragColor = vec4(color, diff);
}
