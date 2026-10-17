#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <ascension:tribulation_clouds.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;

//-1 to 1 across the quad, x along the tear
out vec2 riftUv;
out vec4 vertexColor;
flat out vec3 eyeShadow;
flat out vec3 eyeBase;
flat out vec3 eyeHighlight;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    riftUv = UV0;
    vertexColor = Color;
    eyeShadow = cloudUnpack(TextureMat[1][0]);
    eyeBase = cloudUnpack(TextureMat[1][1]);
    eyeHighlight = cloudUnpack(TextureMat[1][2]);
}
