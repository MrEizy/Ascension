// The Life coating's vines, shared by the hand coating (qi_coating.fsh lifeVines) and the blade (sword_qi.fsh STYLE 11):
// Minecraft's own vine texture, coloured as vines are by the biome's foliage under the world's light (which each passes in its
// vertex colour), with a pulse of soft green-gold life light climbing them that isn't lit, so it shines in the dark.

// block/vine.png, top row first: 0 is clear, 1-4 its four greys from darkest to lightest.
const int VINE_PATTERN[256] = int[256](
    0, 0, 0, 2, 1, 4, 3, 1, 1, 3, 0, 2, 3, 4, 4, 0,
    0, 3, 3, 2, 1, 3, 0, 0, 0, 2, 0, 2, 1, 3, 4, 0,
    4, 4, 3, 1, 2, 0, 0, 0, 0, 0, 4, 3, 1, 1, 0, 0,
    4, 3, 2, 1, 0, 0, 0, 0, 0, 4, 4, 3, 2, 1, 0, 0,
    0, 0, 2, 1, 0, 0, 0, 0, 0, 1, 1, 2, 3, 2, 0, 0,
    0, 3, 2, 2, 1, 0, 0, 2, 2, 1, 0, 0, 3, 2, 1, 1,
    1, 3, 3, 2, 3, 3, 1, 2, 3, 0, 0, 3, 2, 1, 0, 0,
    2, 0, 0, 2, 3, 4, 4, 0, 3, 0, 3, 2, 4, 4, 0, 0,
    0, 0, 0, 0, 1, 3, 4, 0, 0, 0, 0, 2, 3, 4, 0, 0,
    0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 3, 2, 1, 3, 3, 0,
    0, 0, 0, 0, 3, 1, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0,
    0, 0, 1, 3, 2, 1, 3, 0, 0, 4, 4, 1, 0, 0, 0, 0,
    1, 3, 3, 2, 1, 3, 3, 0, 0, 4, 3, 2, 1, 0, 2, 1,
    0, 4, 3, 1, 1, 0, 3, 0, 0, 3, 0, 2, 1, 1, 1, 3,
    0, 3, 2, 1, 1, 0, 0, 0, 0, 0, 1, 2, 2, 0, 0, 2,
    0, 0, 2, 1, 2, 3, 2, 0, 2, 1, 0, 1, 3, 3, 0, 0
);
// The texture's four greys (84, 107, 131 and 170), which the foliage colour tints. VINE_BRIGHTNESS brightens them (1 is
// vanilla; about 0.2 a shade); the life light does the shining.
const float VINE_BRIGHTNESS = 1.0;
const float VINE_SHADES[4] = float[4](84.0 / 255.0, 107.0 / 255.0, 131.0 / 255.0, 170.0 / 255.0);
// The life light: its colour, how often a pulse sets off, how long it takes to climb, and how far it brightens the vines at its
// peak. (How wide its band is is each look's own, in its own units.)
const vec3 LIFE_GLOW = vec3(0.88, 1.00, 0.58);
const float LIFE_PULSE_PERIOD = 3.0;
const float LIFE_PULSE_SECONDS = 1.1;
const float LIFE_PULSE_STRENGTH = 0.7;

// The vine texture's shade (0 clear, 1-4) at a texel, wrapping every 16; the texture's top row is at y 15.
int vineShade(int x, int y) {
    // mod, not %, which GLSL leaves undefined for negative numbers.
    int column = int(mod(float(x), 16.0));
    int row = int(mod(float(y), 16.0));
    return VINE_PATTERN[(15 - row) * 16 + column];
}

// A vine texel's colour: its grey times `foliage` (the biome's foliage colour under the world's light), lit `glow` (0 to 1) of
// the way toward the life light.
vec3 vineColour(int shade, vec3 foliage, float glow) {
    vec3 vine = min(vec3(1.0), foliage * VINE_SHADES[shade - 1] * VINE_BRIGHTNESS);
    return mix(vine, LIFE_GLOW, glow * LIFE_PULSE_STRENGTH);
}

// How far along its climb (0 to 1) the life light is `t` seconds in, offset by `delay` seconds; above 1 between pulses.
float lifePulseClimb(float t, float delay) {
    return fract((t + delay) / LIFE_PULSE_PERIOD) * LIFE_PULSE_PERIOD / LIFE_PULSE_SECONDS;
}
