#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <ascension:tribulation_clouds.glsl>

//how the thickness is split around the flat cloud base, most of it rises above
const float TOP_SHARE = 0.65;
const float BOTTOM_SHARE = 0.35;

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
#ifdef TOP_LAYER
    //the top gets its own puffs, so it does not mirror the underside
    vec2 cellOffset = vec2(5.0, 31.0);
    float share = TOP_SHARE;
#else
    vec2 cellOffset = vec2(0.0);
    float share = -BOTTOM_SHARE;
#endif
    float flow = TextureMat[3][1];
    //the full thickness of the storm in blocks, scaled with its radius
    float maxThickness = TextureMat[3][2];
    float lift = cloudThickness(cloudNoiseUv(UV0, flow), exp(UV0.y), cellOffset) * share * maxThickness;
    //the storm swells as it gathers, TextureMat[2][0] is how far it has
    lift *= smoothstep(0.0, 1.0, TextureMat[2][0]);
    vec3 position = Position + vec3(0.0, lift, 0.0);

    gl_Position = ProjMat * ModelViewMat * vec4(position, 1.0);
    cloudUv = UV0;
    vertexColor = Color * ColorModulator;
    cloudShadow = cloudUnpack(TextureMat[0][0]);
    cloudBase = cloudUnpack(TextureMat[0][1]);
    cloudHighlight = cloudUnpack(TextureMat[0][2]);
    eyeShadow = cloudUnpack(TextureMat[1][0]);
    eyeBase = cloudUnpack(TextureMat[1][1]);
    eyeHighlight = cloudUnpack(TextureMat[1][2]);
}
