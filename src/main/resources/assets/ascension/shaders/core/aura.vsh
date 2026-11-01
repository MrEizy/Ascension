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

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec2 auraUv;
out vec4 vertexColor;
// The vertex relative to the camera, for fragment shaders that shade by the angle a face is seen at (qi_coating.fsh).
out vec3 viewPosition;

void main() {
    viewPosition = (ModelViewMat * vec4(Position, 1.0)).xyz;
    gl_Position = ProjMat * vec4(viewPosition, 1.0);
    auraUv = UV0;
    vertexColor = Color * ColorModulator;
}
