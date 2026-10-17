#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <ascension:tribulation_clouds.glsl>

in vec2 riftUv;
in vec4 vertexColor;
flat in vec3 eyeShadow;
flat in vec3 eyeBase;
flat in vec3 eyeHighlight;

out vec4 fragColor;

//the noise never wraps around the rift, so any period that is never reached does
const float RIFT_PERIOD = 256.0;
//widest half width of the main tear once fully open, in quad units, and how much wider a strike tears it
const float RIFT_WIDTH = 0.16;
const float FLASH_WIDTH = 0.06;
//how far the glow reaches out from the edge of the tear
const float GLOW_REACH = 0.12;
//stars per quad unit inside the void, and how fast they drift
const float STAR_CELLS = 18.0;
const float STAR_DRIFT = 0.15;

vec2 rotate(vec2 p, float angle) {
    float c = cos(angle);
    float s = sin(angle);
    return vec2(c * p.x - s * p.y, s * p.x + c * p.y);
}

/**
 * how far outside a tear running along x from -1 to 1 this point is, negative inside it.
 * the tear tapers to a point at both ends, its line wanders, and its edges are ragged and flicker slowly
 */
float tear(vec2 p, float width, float time, float seed) {
    float taper = pow(max(1.0 - p.x * p.x, 0.0), 0.7);
    float centre = (cloudFbm(vec2(p.x * 2.0 + seed, seed * 3.1), RIFT_PERIOD) - 0.5) * 0.35;
    float ragged = (cloudFbm(vec2(p.x * 9.0 + seed, time * 0.6 + seed), RIFT_PERIOD) - 0.5) * 0.6
            + (cloudNoise(vec2(p.x * 40.0 + seed, time * 1.3), RIFT_PERIOD) - 0.5) * 0.3;
    return abs(p.y - centre) - width * taper * (1.0 + ragged);
}

void main() {
    float time = TextureMat[3][1];
    float formation = TextureMat[2][0];
    float eyeAt = TextureMat[0][3];
    //the strike flash arrives in the vertex alpha
    float flash = clamp(vertexColor.a, 0.0, 1.0);
    //opens as the eye lights and is the first thing to seal as the storm breaks up
    float open = smoothstep(eyeAt, 1.0, formation);
    if (open <= 0.0) {
        discard;
    }

    vec2 p = riftUv;
    float width = (RIFT_WIDTH + FLASH_WIDTH * flash) * open;
    //the main tear, and two thin cracks forking off it. distances are scaled back to quad units
    float d = tear(p, width, time, 0.0);
    d = min(d, tear(rotate(p - vec2(0.35, 0.02), 0.7) * 2.4, width * 0.3, time, 7.0) / 2.4);
    d = min(d, tear(rotate(p - vec2(-0.4, -0.03), -2.4) * 2.8, width * 0.25, time, 13.0) / 2.8);
    //nothing may reach the edge of the quad
    float border = 1.0 - smoothstep(0.85, 1.0, max(abs(p.x), abs(p.y)));

    vec3 color;
    float alpha;
    if (d < 0.0) {
        //a void behind the sky with stars drifting through it
        vec2 starUv = p * STAR_CELLS + vec2(time * STAR_DRIFT, time * STAR_DRIFT * 0.4);
        vec2 cell = floor(starUv);
        float star = cloudHash(cell);
        vec2 starPos = vec2(cloudHash(cell + 3.7), cloudHash(cell + 9.1));
        float twinkle = 0.6 + 0.4 * sin(time * 4.0 + star * 40.0);
        float starLight = star > 0.88 ? (1.0 - smoothstep(0.0, 0.18, length(fract(starUv) - starPos))) * twinkle : 0.0;
        color = eyeShadow * 0.15 + mix(eyeHighlight, vec3(1.0), 0.5) * starLight;
        //the torn edge burns just inside the void too
        color = mix(color, eyeBase, smoothstep(-0.03, 0.0, d) * 0.8);
        alpha = 0.96 * border;
    } else {
        float glow = exp(-d / GLOW_REACH) * open * (0.85 + flash * 0.6);
        if (glow < 0.01) {
            discard;
        }
        color = cloudRamp(eyeShadow, eyeBase, eyeHighlight, clamp(glow * 1.2 + flash * 0.4, 0.0, 1.0));
        alpha = clamp(glow, 0.0, 1.0) * border;
    }
    if (alpha < 0.004) {
        discard;
    }
    fragColor = vec4(min(color, vec3(1.0)), alpha);
}
