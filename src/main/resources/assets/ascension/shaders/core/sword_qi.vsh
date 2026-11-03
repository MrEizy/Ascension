#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

layout(std140) uniform Projection {
    mat4 ProjMat;
};

// Sword Qi (sword_qi.fsh). As aura.vsh, plus UV2: where the vertex sits on the item's model, in model pixels times
// PATTERN_PRECISION (SwordQiRenderer.setPatternPixel), which the looks lay their patterns out in.
const float PATTERN_PRECISION = 64.0;

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

out vec2 auraUv;
out vec4 vertexColor;
out vec2 patternPx;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    auraUv = UV0;
    vertexColor = Color * ColorModulator;
    patternPx = vec2(UV2) / PATTERN_PRECISION;
}
