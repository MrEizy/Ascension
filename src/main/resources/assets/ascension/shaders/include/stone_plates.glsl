#version 330

// Stone plates shared by the earth qi coatings and sword qi: uneven 5-8 sided plates built from small flat texels, each in
// one of four browns, with a 1-texel dark seam around it and a 1-texel bevel. Plate space is measured in plates, and the
// seam and bevel widths assume about 7 texels per plate.

// Mostly solid, but still a little see-through like the other qi coatings; the seams between plates let more show through.
const float STONE_PLATE_ALPHA = 0.86;
const float STONE_SEAM_ALPHA = 0.65;
// A few plates brighten for a moment, stepped at this frame rate like an animated texture.
const float STONE_PLATE_FPS = 4.0;

float stonePlateHash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

// Octagonal norm: cell borders come out as straight cuts at 0, 45 and 90 degrees, so plates read as uneven
// 5-8 sided stones once they are snapped to texels.
float octLength(vec2 d) {
    d = abs(d);
    return max(max(d.x, d.y), (d.x + d.y) * 0.7071);
}

// Weighted Voronoi over the plates, wrapping every `period` columns. Gives the nearest plate's id, the offset from p to its
// centre, and how far p is from the plate's border (F2 - F1).
void stonePlateCell(vec2 p, float period, out vec2 id, out vec2 toCentre, out float edge) {
    vec2 base = floor(p);
    float f1 = 1e9;
    float f2 = 1e9;
    id = vec2(0.0);
    toCentre = vec2(0.0);
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 cell = base + vec2(x, y);
            vec2 cellId = vec2(mod(cell.x, period), cell.y);
            vec2 centre = cell + 0.15 + 0.7 * vec2(stonePlateHash(cellId), stonePlateHash(cellId + 17.3));
            // Uneven sizes: heavier plates claim more ground than their neighbours.
            float d = octLength(p - centre) / (0.8 + 0.4 * stonePlateHash(cellId + 5.1));
            if (d < f1) {
                f2 = f1;
                f1 = d;
                id = cellId;
                toCentre = centre - p;
            } else if (d < f2) {
                f2 = d;
            }
        }
    }
    edge = f2 - f1;
}

// Colour and alpha of a texel on the plate found by stonePlateCell. `light` is the direction in plate space the bevel's
// lit side faces.
vec4 stonePlate(vec2 id, vec2 toCentre, float edge, vec2 texel, vec2 light, float t) {
    if (edge < 0.2) {
        return vec4(0.24, 0.16, 0.10, STONE_SEAM_ALPHA);
    }
    float tone = stonePlateHash(id + 2.9);
    vec3 color = tone < 0.25 ? vec3(0.38, 0.26, 0.17)
               : tone < 0.60 ? vec3(0.47, 0.33, 0.22)
               : tone < 0.88 ? vec3(0.55, 0.40, 0.27)
               : vec3(0.64, 0.48, 0.33);
    if (edge < 0.45) {
        float facing = dot(normalize(-toCentre + 1e-4), normalize(light));
        color *= facing > 0.3 ? 1.18 : facing < -0.3 ? 0.82 : 1.0;
    }
    // Speckle: a few texels per plate a shade lighter or darker, like a vanilla stone texture.
    float speckle = stonePlateHash(texel + 1.7);
    color *= speckle < 0.12 ? 0.88 : speckle > 0.9 ? 1.1 : 1.0;
    float flash = step(0.9, stonePlateHash(id + floor(t * STONE_PLATE_FPS)));
    return vec4(color * (1.0 + flash * 0.12), STONE_PLATE_ALPHA + flash * 0.08);
}
