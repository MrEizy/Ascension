#version 150

uniform sampler2D DepthSampler;

layout(std140) uniform DivineSenseEffectUniform {
    mat4 InvViewMat;
    mat4 InvProjMat;
    vec3 Pos;
    vec3 Center;
    float Radius;
    vec3 Color;
};

in vec2 texCoord0;
out vec4 fragColor;

const float WIDTH = 10.0;
const float SHARPNESS = 10.0;

float scanlines() {
    return sin(gl_FragCoord.y) * 0.5 + 0.5;
}

vec3 worldPos(float depth) {
    float z = depth * 2.0 - 1.0;
    vec4 clipSpacePosition = vec4(texCoord0 * 2.0 - 1.0, z, 1.0);
    vec4 viewSpacePosition = InvProjMat * clipSpacePosition;
    viewSpacePosition /= viewSpacePosition.w;
    vec4 worldSpacePosition = InvViewMat * viewSpacePosition;
    return Pos + worldSpacePosition.xyz;
}

void main() {
    vec4 outColor = vec4(0.0);

    vec4 innerColor    = vec4(Color * 0.35, 1.0);
    vec4 midColor      = vec4(Color * 0.7, 1.0);
    vec4 outerColor    = vec4(min(Color * 1.15 + 0.15, vec3(1.0)), 1.0);
    vec4 scanlineColor = vec4(min(Color * 1.6, vec3(1.0)), 1.0);

    float depth = texture(DepthSampler, texCoord0).r;
    vec3 world = worldPos(depth);
    float dist = distance(world, Center);

    if (dist < Radius && dist > Radius - WIDTH && depth < 1.0) {
        float diff = 1.0 - (Radius - dist) / WIDTH;
        vec4 edge = mix(midColor, outerColor, pow(diff, SHARPNESS));
        outColor = mix(innerColor, edge, diff) + scanlines() * scanlineColor;
        outColor *= diff;
    }

    fragColor = outColor;
}