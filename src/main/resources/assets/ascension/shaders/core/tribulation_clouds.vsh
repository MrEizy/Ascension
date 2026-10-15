#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <ascension:tribulation_clouds.glsl>

//how far the billows bulge out of the base surface, in blocks
const float AMPLITUDE = 6.0;

in vec3 Position;
in vec2 UV0;
in vec4 Color;

//x = spiral arm coordinate in turns, y = log of the distance from the centre / radius
out vec2 cloudUv;
out vec4 vertexColor;

void main() {
#ifdef TOP_LAYER
    //the top bulges up with its own noise, so it does not mirror the underside
    vec2 offset = vec2(5.0, 31.0);
    float direction = 1.0;
#else
    vec2 offset = vec2(0.0);
    float direction = -1.0;
#endif
    float flow = TextureMat[3][1];
    float height = cloudHeight(cloudNoiseUv(UV0, flow), exp(UV0.y), offset);
    vec3 position = Position + vec3(0.0, direction * height * AMPLITUDE, 0.0);

    gl_Position = ProjMat * ModelViewMat * vec4(position, 1.0);
    cloudUv = UV0;
    vertexColor = Color * ColorModulator;
}
