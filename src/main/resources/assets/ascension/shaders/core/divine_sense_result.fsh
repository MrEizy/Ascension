#version 150

layout(std140) uniform DivineSenseResultUniform {
    mat4 ProjMat;
    mat4 ModelViewMat;
    float Time;
    vec3 Tint;
};

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

float scanlines() {
    return sqrt(sin(gl_FragCoord.y + Time * 10.0) * 0.5 + 0.5);
}

void main() {
    float timeScale = (sin(Time * 2.5) + 1.0) * 0.5;
    timeScale = timeScale * 0.15 + 0.85;

    vec2 edgeDist = abs(texCoord0.xy - 0.5) * 2.0;
    edgeDist = edgeDist * 0.25 + 0.75;
    float edgeMul = pow(max(edgeDist.x, edgeDist.y), 8.0) * 0.8 + 0.2;

    vec4 c = vertexColor * scanlines();
    c.rgb *= Tint;
    c *= timeScale;
    c *= edgeMul;
    fragColor = c;
}