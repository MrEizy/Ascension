#version 330

#moj_import <minecraft:light.glsl>
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>

// Vanilla's entity vertex shader for skin-fitted qi coatings (qi_coating_skin.fsh), lit the same way as the skin under it.
// The vertex colour isn't a tint here: it carries the coating's data (QiCoatingLayer.SkinCoatingConsumer), so it is passed
// through as is and the lighting is worked out on white.

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexPerFaceColorBack;
out vec4 vertexPerFaceColorFront;
out vec4 lightMapColor;
out vec4 coatingData;
out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);

    vec2 light = minecraft_compute_light(Light0_Direction, Light1_Direction, Normal);
    vertexPerFaceColorBack = minecraft_mix_light_separate(-light, vec4(1.0));
    vertexPerFaceColorFront = minecraft_mix_light_separate(light, vec4(1.0));
    lightMapColor = sample_lightmap(Sampler2, UV2);
    coatingData = Color;
    texCoord0 = UV0;
}
