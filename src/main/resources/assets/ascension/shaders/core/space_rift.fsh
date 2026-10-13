#version 150

layout(std140) uniform SpaceRift {
    mat4 InverseView;
    mat4 InverseProjection;
    mat4 ViewProjection;
    vec4 CameraAndDepthMode;
    vec4 CenterAndRadius;
    vec4 RightAndTime;
    vec4 UpAndSeed;
    vec4 Phases;
    vec4 Collapse;
};

uniform sampler2D WorldDepth;
uniform sampler2D EndSky;
uniform sampler2D EndPortal;
uniform sampler2D SceneColor;

in vec2 screenUv;

out vec4 fragColor;

const float TAU = 6.28318530718;
const float VOID_DEPTH = 1.6;
const int END_PORTAL_LAYERS = 15;
// How far (in rift radii) space is dragged toward the centre at the peak of the collapse.
const float COLLAPSE_PULL = 0.55;

vec3 reconstructWorldPosition(float depth) {
    float ndcDepth = mix(depth * 2.0 - 1.0, depth, CameraAndDepthMode.w);
    vec4 viewPosition = InverseProjection * vec4(screenUv * 2.0 - 1.0, ndcDepth, 1.0);
    vec3 cameraOffset = viewPosition.xyz / max(abs(viewPosition.w), 0.00001);
    return CameraAndDepthMode.xyz + (InverseView * vec4(cameraOffset, 0.0)).xyz;
}

vec3 worldRayDirection() {
    vec4 viewPosition = InverseProjection * vec4(screenUv * 2.0 - 1.0, 1.0, 1.0);
    vec3 viewDirection = normalize(viewPosition.xyz / max(abs(viewPosition.w), 0.00001));
    return normalize((InverseView * vec4(viewDirection, 0.0)).xyz);
}

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec2 hash22(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.xx + p3.yz) * p3.zy);
}

float valueNoise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash12(i), hash12(i + vec2(1.0, 0.0)), u.x),
               mix(hash12(i + vec2(0.0, 1.0)), hash12(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm2(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        value += valueNoise2(p) * amplitude;
        p = p * 2.03 + vec2(17.1, 9.3);
        amplitude *= 0.5;
    }
    return value;
}

// Chebyshev (L-infinity) Voronoi: cells come out as blocky, mostly-rectangular shards.
// Returns (border distance, cell id hash, cell centre radius in shard space).
const float SHARD_JITTER = 0.6;
vec2 shardPoint(vec2 cell, float seed) {
    return cell + 0.5 + (hash22(cell + seed) - 0.5) * SHARD_JITTER;
}

vec3 shardCells(vec2 x, float seed) {
    vec2 n = floor(x);
    float f1 = 8.0;
    float f2 = 8.0;
    vec2 nearest = vec2(0.0);
    for (int j = -2; j <= 2; j++) {
        for (int i = -2; i <= 2; i++) {
            vec2 point = shardPoint(n + vec2(float(i), float(j)), seed);
            vec2 delta = abs(point - x);
            float d = max(delta.x, delta.y);
            if (d < f1) {
                f2 = f1;
                f1 = d;
                nearest = point;
            } else if (d < f2) {
                f2 = d;
            }
        }
    }
    return vec3((f2 - f1) * 0.5, hash12(nearest * 1.37 + seed), length(nearest));
}

// Plane-local coords are mildly warped so shards are smaller near the impact point
// (kept gentle so the shards stay square).
const float SHARD_SCALE = 3.6;
const float SHARD_WARP = 0.18;
vec2 toShardSpace(vec2 local, float seed) {
    float r = length(local);
    float angle = seed * 0.37;
    mat2 rotation = mat2(cos(angle), sin(angle), -sin(angle), cos(angle));
    return rotation * local * pow(max(r, 0.0001), -SHARD_WARP) * SHARD_SCALE;
}

float shardSpaceRadius(float radius) {
    return pow(max(radius, 0.0001), 1.0 - SHARD_WARP) * SHARD_SCALE;
}

float radialCracks(vec2 local, float reach, float seed) {
    float r = length(local);
    float angle = atan(local.y, local.x);
    float lines = 0.0;
    for (int i = 0; i < 9; i++) {
        float fi = float(i);
        float baseAngle = (fi + hash12(vec2(fi, seed)) * 0.7) * TAU / 9.0;
        float crackLength = mix(0.85, 1.55, hash12(vec2(seed, fi * 3.1))) * reach;
        float wiggle = (fbm2(vec2(r * 7.0, fi * 4.3 + seed)) - 0.5) * 0.32;
        float delta = abs(mod(angle - baseAngle - wiggle + TAU * 0.5, TAU) - TAU * 0.5) * r;
        float width = mix(0.016, 0.003, clamp(r / max(crackLength, 0.001), 0.0, 1.0));
        float line = 1.0 - smoothstep(0.0, width, delta);
        line *= 1.0 - smoothstep(crackLength * 0.8, crackLength, r);
        lines = max(lines, line);
    }
    return lines;
}

// Vanilla End portal (core/rendertype_end_portal.fsh), sampled in screen space exactly like the block.
const vec3[] END_PORTAL_COLORS = vec3[](
    vec3(0.022087, 0.098399, 0.110818),
    vec3(0.011892, 0.095924, 0.089485),
    vec3(0.027636, 0.101689, 0.100326),
    vec3(0.046564, 0.109883, 0.114838),
    vec3(0.064901, 0.117696, 0.097189),
    vec3(0.063761, 0.086895, 0.123646),
    vec3(0.084817, 0.111994, 0.166380),
    vec3(0.097489, 0.154120, 0.091064),
    vec3(0.106152, 0.131144, 0.195191),
    vec3(0.097721, 0.110188, 0.187229),
    vec3(0.133516, 0.138278, 0.148582),
    vec3(0.070006, 0.243332, 0.235792),
    vec3(0.196766, 0.142899, 0.214696),
    vec3(0.047281, 0.315338, 0.321970),
    vec3(0.204675, 0.390010, 0.302066),
    vec3(0.080955, 0.314821, 0.661491)
);

const mat4 END_PORTAL_SCALE_TRANSLATE = mat4(
    0.5, 0.0, 0.0, 0.25,
    0.0, 0.5, 0.0, 0.25,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
);

mat4 endPortalLayer(float layer, float gameTime) {
    mat4 translate = mat4(
        1.0, 0.0, 0.0, 17.0 / layer,
        0.0, 1.0, 0.0, (2.0 + layer / 1.5) * (gameTime * 1.5),
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
    );
    float angle = radians((layer * layer * 4321.0 + layer * 9.0) * 2.0);
    mat2 rotate = mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
    mat2 scale = mat2((4.5 - layer / 4.0) * 2.0);
    return mat4(scale * rotate) * translate * END_PORTAL_SCALE_TRANSLATE;
}

vec3 endPortalVoid(float gameTime) {
    vec4 projected = vec4(screenUv, 0.0, 1.0);
    vec3 color = textureProj(EndSky, projected).rgb * END_PORTAL_COLORS[0];
    for (int i = 0; i < END_PORTAL_LAYERS; i++) {
        color += textureProj(EndPortal, projected * endPortalLayer(float(i + 1), gameTime)).rgb * END_PORTAL_COLORS[i];
    }
    return color;
}

float boltDistance(vec2 p, vec2 a, vec2 b, float epoch, float jag) {
    vec2 ab = b - a;
    float abLength = length(ab);
    vec2 axis = ab / max(abLength, 0.0001);
    vec2 side = vec2(-axis.y, axis.x);
    vec2 offset = p - a;
    float along = dot(offset, axis) / max(abLength, 0.0001);
    if (along < 0.0 || along > 1.0) {
        return 1.0;
    }
    float envelope = sin(along * 3.14159);
    float displacement = (fbm2(vec2(along * 9.0, epoch * 13.7)) - 0.5) * jag * envelope
                       + (valueNoise2(vec2(along * 31.0, epoch * 7.1)) - 0.5) * jag * 0.25 * envelope;
    return abs(dot(offset, side) - displacement * abLength);
}

float voidLightning(vec2 deep, float time, float seed) {
    float total = 0.0;
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        float period = 2.2 + hash12(vec2(fi, seed + 1.0)) * 2.6;
        float shifted = time + hash12(vec2(seed, fi)) * period;
        float epoch = floor(shifted / period) + fi * 17.0;
        float local = fract(shifted / period) * period;
        float strike = exp(-local * 9.0) + exp(-max(local - 0.11, 0.0) * 14.0) * step(0.11, local) * 0.7;
        if (strike < 0.01) {
            continue;
        }
        float startAngle = hash12(vec2(epoch, seed)) * TAU;
        float sweep = 1.8 + hash12(vec2(seed, epoch)) * 2.4;
        vec2 a = vec2(cos(startAngle), sin(startAngle)) * 0.95;
        vec2 b = vec2(cos(startAngle + sweep), sin(startAngle + sweep)) * mix(0.25, 0.9, hash12(vec2(epoch, fi)));
        float d = boltDistance(deep, a, b, epoch, 0.45);
        vec2 mid = mix(a, b, 0.45);
        vec2 branchEnd = mid + vec2(cos(startAngle + 1.9), sin(startAngle + 1.9)) * 0.4;
        d = min(d, boltDistance(deep, mid, branchEnd, epoch + 3.0, 0.6) * 1.6);
        float core = 1.0 - smoothstep(0.0, 0.006, d);
        float glow = exp(-d * 38.0) * 0.35;
        total += (core + glow) * strike;
    }
    return total;
}

// ---- Collapse: before the glass breaks, space is dragged in on itself ----

// Rift-local plane coords -> screen UV, so the warp follows the rift's plane in perspective.
vec2 localToScreenUv(vec2 p) {
    vec3 world = CenterAndRadius.xyz + (RightAndTime.xyz * p.x + UpAndSeed.xyz * p.y) * CenterAndRadius.w;
    vec4 clip = ViewProjection * vec4(world - CameraAndDepthMode.xyz, 1.0);
    if (clip.w <= 0.0001) {
        return screenUv;
    }
    return clip.xy / clip.w * 0.5 + 0.5;
}

// The scene behind the rift, pinched and twisted toward the centre.
vec3 collapsedScene(vec2 local, float time) {
    float pinch = Collapse.x;
    float r = length(local);
    vec2 dir = r > 0.0001 ? local / r : vec2(0.0);
    float edgeFade = 1.0 - smoothstep(1.3, 1.7, r);

    float twist = pinch * 1.4 * exp(-r * r * 2.5);
    vec2 twisted = mat2(cos(twist), sin(twist), -sin(twist), cos(twist)) * local;

    // Ripples of space being sucked in toward the centre.
    float ripple = sin(r * 16.0 + time * 14.0) * 0.035 * abs(pinch) * exp(-r * 1.5);
    float pull = (pinch * COLLAPSE_PULL * exp(-r * r * 1.4) + ripple) * edgeFade;

    // Slight colour split, stronger the harder space is pulled.
    vec3 color;
    color.r = texture(SceneColor, localToScreenUv(twisted + dir * pull * 1.06)).r;
    color.g = texture(SceneColor, localToScreenUv(twisted + dir * pull)).g;
    color.b = texture(SceneColor, localToScreenUv(twisted + dir * pull * 0.94)).b;

    // Light drains toward the tear while space is being pulled in.
    color *= 1.0 - 0.55 * clamp(pinch, 0.0, 1.0) * exp(-r * r * 3.0);
    return color;
}

void main() {
    vec3 camera = CameraAndDepthMode.xyz;
    vec3 ray = worldRayDirection();
    vec3 center = CenterAndRadius.xyz;
    float radius = max(CenterAndRadius.w, 0.1);
    vec3 right = RightAndTime.xyz;
    vec3 up = UpAndSeed.xyz;
    vec3 normal = normalize(cross(right, up));
    float time = RightAndTime.w;
    float seed = UpAndSeed.w;
    float crackPhase = Phases.x;
    float openPhase = Phases.y;
    float fade = Phases.z;

    float denom = dot(ray, normal);
    if (abs(denom) < 0.0001) {
        fragColor = vec4(0.0);
        return;
    }
    float hitDistance = dot(center - camera, normal) / denom;
    if (hitDistance <= 0.0) {
        fragColor = vec4(0.0);
        return;
    }
    vec3 hitPoint = camera + ray * hitDistance;
    vec2 local = vec2(dot(hitPoint - center, right), dot(hitPoint - center, up)) / radius;
    float r = length(local);
    if (r > 1.75) {
        fragColor = vec4(0.0);
        return;
    }

    float depth = texture(WorldDepth, screenUv).r;
    float sceneDistance = depth < 1.0 ? distance(reconstructWorldPosition(depth), camera) : 1.0e6;
    if (hitDistance > sceneDistance) {
        fragColor = vec4(0.0);
        return;
    }

    vec3 cells = shardCells(toShardSpace(local, seed), seed);
    float border = cells.x;
    float shardId = cells.y;
    float cellRadius = cells.z;

    // A shard falls once the opening front passes its centre; a ragged per-shard bias keeps the hole irregular.
    float openFront = shardSpaceRadius(openPhase * 0.95) * (0.82 + 0.3 * shardId);
    float sinceBroken = openFront - cellRadius;
    // The void tear left by the collapse; the shattering glass widens this same opening.
    float coreEdge = Collapse.y * (1.0 + 0.12 * (valueNoise2(normalize(local + 0.0001) * 3.0 + time * 2.0) - 0.5));
    bool inCore = r < coreEdge;
    bool hole = (openPhase > 0.02 && sinceBroken > 0.0) || inCore;

    float crackReach = crackPhase;
    float crackFront = shardSpaceRadius(crackReach * 1.05);
    float cracked = 1.0 - smoothstep(crackFront * 0.85, crackFront, cellRadius);

    vec4 result = vec4(0.0);

    // Faint warped halo around the whole fracture.
    float haloMask = (1.0 - smoothstep(0.55, 1.6, r)) * max(crackPhase, openPhase);
    result = vec4(vec3(0.12, 0.45, 0.5), haloMask * 0.10);

    if (hole) {
        vec3 color = endPortalVoid(Phases.w);

        vec3 deepPoint = camera + ray * (dot(center + normal * VOID_DEPTH * radius * sign(denom) - camera, normal) / denom);
        vec2 deep = vec2(dot(deepPoint - center, right), dot(deepPoint - center, up)) / radius;
        float lightning = voidLightning(deep * 0.9, time, seed);
        color += vec3(0.6, 0.95, 1.0) * lightning * 0.55;

        float rimGlow = sinceBroken > 0.0 ? exp(-border * 14.0) * smoothstep(-0.2, 0.0, -sinceBroken + 0.35) : 0.0;
        color += vec3(0.2, 0.7, 0.75) * rimGlow * 0.45;

        float freshFlash = sinceBroken > 0.0 ? 1.0 - smoothstep(0.0, 0.35, sinceBroken) : 0.0;
        color = mix(color, vec3(0.85, 0.9, 1.0), freshFlash * 0.45);
        result = vec4(color, 1.0);
    } else if (cracked > 0.0) {
        float facet = 0.5 + 0.5 * sin(shardId * 37.0 + dot(ray, normal) * 9.0 + time * 0.4);
        float edge = 1.0 - smoothstep(0.0, 0.035, border);
        float edgeGlow = exp(-border * 22.0);
        float nearHole = 1.0 - smoothstep(0.0, 0.9, -sinceBroken);
        vec3 glassColor = mix(vec3(0.55, 0.7, 1.0), vec3(0.95, 0.97, 1.0), facet);
        float alpha = cracked * (0.05 + facet * 0.06 + edge * 0.75 + edgeGlow * 0.25 + nearHole * 0.1);
        vec3 color = glassColor * (0.6 + edge * 0.8) + vec3(0.15, 0.55, 0.6) * nearHole * 0.4;
        result.rgb = mix(result.rgb, color, clamp(alpha / max(alpha + result.a, 0.0001), 0.0, 1.0));
        result.a = clamp(result.a + alpha, 0.0, 0.92);
    }

    float spider = radialCracks(local, crackPhase, seed) * (hole ? 0.0 : 1.0);
    result.rgb = mix(result.rgb, vec3(0.92, 0.95, 1.0), spider);
    result.a = max(result.a, spider * 0.85);

    if (Collapse.w > 0.5) {
        vec3 warped = collapsedScene(local, time);
        result = vec4(mix(warped, result.rgb, result.a), 1.0);
        // Event-horizon ring hugging the tear, and a soft burst at the centre right around the snap.
        result.rgb += vec3(0.5, 0.95, 1.0) * exp(-abs(r - coreEdge) * 55.0) * Collapse.z * 0.9;
        float burst = Collapse.z * Collapse.z * Collapse.z * Collapse.z;
        result.rgb += vec3(0.85, 0.95, 1.0) * burst * exp(-r * r * 5.0) * 0.6;
    }

    result.a *= fade;
    fragColor = result;
}
