#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in float fresnel;
in vec3 localPos;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

void main() {
    vec4 tex = texture(Sampler0, texCoord0);
    if (tex.a < 0.05) {
        discard;
    }

    float time = GameTime * 24000.0;
    vec3 silver = vec3(0.78, 0.89, 1.0);

    vec4 color = tex * vec4(vertexColor.rgb, 1.0) * ColorModulator;

    float rim = pow(fresnel, 1.8);
    color.rgb = mix(color.rgb, silver, rim * 0.75);
    color.rgb += silver * rim * 0.55;

    float band = sin(localPos.y * 5.5 - time * 0.09) * 0.5 + 0.5;
    float band2 = sin(localPos.y * 11.0 + time * 0.05 + localPos.x * 3.0) * 0.5 + 0.5;
    color.rgb += silver * (band * 0.10 + band2 * 0.06);

    float sparkle = hash(floor(texCoord0 * 128.0) + vec2(floor(time * 0.12), 0.0));
    sparkle = step(0.985, sparkle);
    color.rgb += silver * sparkle * 0.6;

    float alpha = tex.a * vertexColor.a;
    alpha *= 0.55 + rim * 0.45;
    alpha += band * 0.08 * vertexColor.a;
    color.a = clamp(alpha, 0.0, 1.0);

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}