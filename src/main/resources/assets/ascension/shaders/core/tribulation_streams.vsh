#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <ascension:tribulation_clouds.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;

//x = across the stream from -1 to 1, y = log of the distance from the centre / radius
out vec2 streamUv;
out vec4 vertexColor;
flat out vec3 cloudShadow;
flat out vec3 cloudBase;
flat out vec3 cloudHighlight;
flat out vec3 eyeHighlight;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    streamUv = UV0;
    vertexColor = Color * ColorModulator;
    cloudShadow = cloudUnpack(TextureMat[0][0]);
    cloudBase = cloudUnpack(TextureMat[0][1]);
    cloudHighlight = cloudUnpack(TextureMat[0][2]);
    eyeHighlight = cloudUnpack(TextureMat[1][2]);
}
