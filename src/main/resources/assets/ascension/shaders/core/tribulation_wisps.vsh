#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <ascension:tribulation_clouds.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;

//x = spiral arm coordinate in turns, y = log of the distance from the centre / radius
out vec2 cloudUv;
out vec4 vertexColor;
//the cloud and eye palettes, packed into the texture matrix by the renderer. unpacked here once per vertex instead of per pixel
flat out vec3 cloudShadow;
flat out vec3 cloudBase;
flat out vec3 cloudHighlight;
flat out vec3 eyeShadow;
flat out vec3 eyeBase;
flat out vec3 eyeHighlight;

void main() {
    //the renderer already shapes the funnel and stacks the layers, the wisps are flat
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    cloudUv = UV0;
    vertexColor = Color * ColorModulator;
    cloudShadow = cloudUnpack(TextureMat[0][0]);
    cloudBase = cloudUnpack(TextureMat[0][1]);
    cloudHighlight = cloudUnpack(TextureMat[0][2]);
    eyeShadow = cloudUnpack(TextureMat[1][0]);
    eyeBase = cloudUnpack(TextureMat[1][1]);
    eyeHighlight = cloudUnpack(TextureMat[1][2]);
}
