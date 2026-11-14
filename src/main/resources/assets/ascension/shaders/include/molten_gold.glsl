// The Metal element's molten gold, shared by the skin coatings (qi_coating_skin.fsh, METAL) and the Molten Edge blade
// (sword_qi.fsh STYLE 7).

// The gold tint: a pixel's brightness picks a gold along a ramp from GOLD_DEEP (dark pixels) to GOLD_BRIGHT (light ones), then
// GOLD_TINT_AMOUNT of the way from the pixel's own colour to that. The ramp keeps the gold saturated at every brightness, so
// fully lit faces (tops of the head and shoulders) don't clip to a pale, near-white gold. GOLD_SHINE is what sheens and hit
// flashes brighten it toward.
const vec3 GOLD_DEEP = vec3(0.30, 0.18, 0.04);
const vec3 GOLD_BRIGHT = vec3(0.98, 0.80, 0.42);
const float GOLD_TINT_AMOUNT = 0.8;
const vec3 GOLD_SHINE = vec3(1.00, 0.97, 0.80);
// Molten gold, hottest first.
const vec3 MOLTEN_WHITE = vec3(1.00, 0.97, 0.84);
const vec3 MOLTEN_YELLOW = vec3(1.00, 0.80, 0.32);
const vec3 MOLTEN_ORANGE = vec3(1.00, 0.52, 0.12);
const vec3 MOLTEN_RED = vec3(0.82, 0.27, 0.06);

// A pixel tinted gold, keeping its own brightness (on the gold ramp) and a little of its own colour.
vec3 goldTint(vec3 colour) {
    float lum = dot(colour, vec3(0.299, 0.587, 0.114));
    return mix(colour, mix(GOLD_DEEP, GOLD_BRIGHT, clamp(lum * 1.1, 0.0, 1.0)), GOLD_TINT_AMOUNT);
}

// Molten gold `heat` of the way back from the front (0 to 1): white, yellow, orange, then red.
vec3 moltenColour(float heat) {
    return heat < 0.25 ? MOLTEN_WHITE : heat < 0.5 ? MOLTEN_YELLOW : heat < 0.75 ? MOLTEN_ORANGE : MOLTEN_RED;
}
