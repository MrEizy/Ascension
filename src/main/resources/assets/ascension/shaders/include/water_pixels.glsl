#version 330

// Pixel water shared by the water qi coatings and sword qi: a few flat blues and a foam white, like a vanilla water texture
// rather than a smooth gradient. Each colour comes with its own alpha, so the water stays see-through, clearer than the
// earth coatings' stone plates.

// Deep water at the edges of a stream, the body, the lit stripe along its top, and foam on heads and crests.
const vec4 WATER_DEEP = vec4(0.10, 0.30, 0.68, 0.62);
const vec4 WATER_MID = vec4(0.18, 0.48, 0.88, 0.66);
const vec4 WATER_LIGHT = vec4(0.42, 0.74, 1.00, 0.70);
const vec4 WATER_FOAM = vec4(0.88, 0.96, 1.00, 0.88);
// Water moves in steps at this frame rate, like an animated texture.
const float WATER_FPS = 8.0;

float waterHash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

// Signed distance around the perimeter (u, 0..1, wrapping) from u to centre.
float waterAround(float u, float centre) {
    return fract(u - centre + 0.5) - 0.5;
}

// A texel of a stream `width` wide (in the same units as `across`), shaded deep at its edges with a lit stripe along one side.
vec4 waterStreamTexel(float across, float width) {
    float side = across / width;
    if (abs(side) > 0.62) {
        return WATER_DEEP;
    }
    return side > 0.05 && side < 0.45 ? WATER_LIGHT : WATER_MID;
}
