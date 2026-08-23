#version 150

uniform sampler2D DepthSampler;

layout(std140) uniform DivineSenseEffectUniform {
    mat4 InvViewMat;
    mat4 InvProjMat;
    vec3 Pos;
    vec3 Center;
    float Radius;
    float ZeroToOneDepth;
    vec3 Color;
};

in vec2 texCoord0;
out vec4 fragColor;

const float WIDTH = 2.75;
const float FRONT_GLOW_WIDTH = 0.55;

float scanlines() {
    float stripe = step(0.52, fract(gl_FragCoord.y / 4.0));
    return mix(0.3, 1.0, stripe);
}

vec3 worldPos(float depth) {
    float z = mix(depth * 2.0 - 1.0, depth, ZeroToOneDepth);
    vec4 clipSpacePosition = vec4(texCoord0 * 2.0 - 1.0, z, 1.0);
    vec4 viewSpacePosition = InvProjMat * clipSpacePosition;
    viewSpacePosition /= viewSpacePosition.w;
    vec4 worldSpacePosition = InvViewMat * viewSpacePosition;
    return Pos + worldSpacePosition.xyz;
}

void main() {
    float depth = texture(DepthSampler, texCoord0).r;
    if (depth >= 1.0) {
        fragColor = vec4(0.0);
        return;
    }

    vec3 world = worldPos(depth);
    float dist = distance(world, Center);
    float behindFront = Radius - dist;

    if (behindFront < 0.0 || behindFront > WIDTH) {
        fragColor = vec4(0.0);
        return;
    }

    float band = 1.0 - smoothstep(0.0, WIDTH, behindFront);
    float frontGlow = 1.0 - smoothstep(0.0, FRONT_GLOW_WIDTH, behindFront);
    float stripe = scanlines();

    vec3 innerColor = Color * 0.35;
    vec3 outerColor = min(Color * 1.45 + 0.2, vec3(1.0));
    vec3 pulseColor = mix(innerColor, outerColor, band);
    float intensity = band * (0.8 + stripe * 0.9) + frontGlow * 1.6;

    fragColor = vec4(pulseColor * intensity, band);
}
