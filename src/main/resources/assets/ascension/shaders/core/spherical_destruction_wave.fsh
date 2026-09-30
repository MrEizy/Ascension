#version 150

layout(std140) uniform SphericalDestructionWave {
    mat4 InverseView;
    mat4 InverseProjection;
    vec4 CameraAndDepthMode;
    vec4 CenterAndRadius;
    vec4 ColorAndProgress;
    vec4 WaveParameters;
    vec4 MotionParameters;
};

uniform sampler2D WorldDepth;

in vec2 screenUv;

out vec4 fragColor;

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

float firstSphereHit(vec3 rayOrigin, vec3 rayDirection, vec3 center, float radius) {
    vec3 offset = rayOrigin - center;
    float b = dot(offset, rayDirection);
    float c = dot(offset, offset) - radius * radius;
    float discriminant = b * b - c;
    if (discriminant < 0.0) {
        return -1.0;
    }
    float root = sqrt(discriminant);
    float nearHit = -b - root;
    float farHit = -b + root;
    return nearHit > 0.0 ? nearHit : (farHit > 0.0 ? farHit : -1.0);
}

float rayGlowSquared(vec3 camera, vec3 ray, vec3 center, float innerRadius,
                     float outerRadius, float sceneDistance) {
    vec3 toCenter = center - camera;
    float alongRay = max(dot(toCenter, ray), 0.0);
    if (alongRay > sceneDistance + outerRadius) {
        return 0.0;
    }
    float perpendicularSquared = max(dot(toCenter, toCenter) - alongRay * alongRay, 0.0);
    return 1.0 - smoothstep(innerRadius * innerRadius, outerRadius * outerRadius, perpendicularSquared);
}

float organicCurrents(vec3 normal, float time) {
    float warpA = sin(dot(normal, vec3(3.7, -2.4, 4.9)) * 2.8 + time * 2.1) * 0.55;
    float warpB = sin(dot(normal, vec3(-4.2, 3.3, 2.6)) * 2.2 - time * 1.7) * 0.42;
    float bandA = abs(sin(dot(normal, vec3(9.2, 4.7, -6.4)) + warpA + time * 4.6));
    float bandB = abs(sin(dot(normal, vec3(-5.8, 10.4, 3.9)) + warpB - time * 3.7));
    float bandC = abs(sin(dot(normal, vec3(4.1, -7.2, 11.3)) + warpA - warpB + time * 2.9));
    float lineA = 1.0 - smoothstep(0.0, 0.12, bandA);
    float lineB = 1.0 - smoothstep(0.0, 0.09, bandB);
    float lineC = 1.0 - smoothstep(0.0, 0.07, bandC);
    return clamp(lineA * 0.62 + lineB * 0.38 + lineC * 0.24, 0.0, 1.0);
}

vec4 over(vec4 behind, vec4 front) {
    float opacity = front.a + behind.a * (1.0 - front.a);
    if (opacity <= 0.00001) {
        return vec4(0.0);
    }
    vec3 composite = (front.rgb * front.a + behind.rgb * behind.a * (1.0 - front.a)) / opacity;
    return vec4(composite, opacity);
}

vec4 continuousSphere(vec3 camera, vec3 ray, float sceneDistance) {
    vec3 center = CenterAndRadius.xyz;
    float radius = max(CenterAndRadius.w, 0.05);
    float time = WaveParameters.x;
    bool impact = WaveParameters.w > 0.5;
    float progress = clamp(ColorAndProgress.w, 0.0, 1.0);
    vec3 baseColor = ColorAndProgress.rgb;
    vec3 hotColor = mix(baseColor, vec3(1.0, 0.93, 0.79), 0.72);
    vec3 coreColor = baseColor * vec3(0.048, 0.030, 0.020);

    float halo = rayGlowSquared(camera, ray, center, radius * 1.035,
                                radius * (impact ? 1.065 : 1.62) + 0.14, sceneDistance);
    float haloFade = impact ? (1.0 - smoothstep(0.67, 1.0, progress)) : 1.0;
    vec4 result = vec4(baseColor * (0.13 + 0.035 * sin(time * 8.0)),
                       halo * (impact ? 0.055 : 0.12) * haloFade);
    result.a *= halo;

    if (!impact) {
        vec3 direction = normalize(MotionParameters.xyz);
        float trailLength = max(MotionParameters.w, radius * 2.0);
        float fade = 1.0 - smoothstep(0.82, 1.0, progress);
        float trailA = rayGlowSquared(camera, ray, center - direction * trailLength * 0.30,
                                      radius * 0.40, radius * 0.92, sceneDistance);
        float trailB = rayGlowSquared(camera, ray, center - direction * trailLength * 0.72,
                                      radius * 0.20, radius * 0.58, sceneDistance);
        result = over(result, vec4(baseColor * 0.40, trailA * trailA * 0.085 * fade));
        result = over(result, vec4(hotColor * 0.37, trailB * trailB * 0.045 * fade));
    }

    float hit = firstSphereHit(camera, ray, center, radius);
    if (hit <= 0.0 || hit > sceneDistance + 0.08) {
        return result;
    }

    vec3 normal = normalize(camera + ray * hit - center);
    float facing = clamp(dot(normal, -ray), 0.0, 1.0);
    float rim = pow(1.0 - facing, impact ? 2.8 : 2.05);
    float currents = organicCurrents(normal, time);
    float heartbeat = 0.87 + 0.13 * sin(time * (impact ? 13.0 : 9.5));
    float charged = impact ? 1.0 + 0.25 * (1.0 - smoothstep(0.20, 0.43, progress)) : 1.0;
    float heat = clamp(rim * 1.26 + currents * heartbeat * charged * 0.95, 0.0, 1.0);
    vec3 surfaceColor = mix(coreColor, hotColor, heat);
    float opacity = clamp(0.89 + rim * 0.065 + currents * 0.035, 0.0, 0.97);

    if (impact) {
        float coreFade = 1.0 - smoothstep(0.37, 0.78, progress);
        float edgeFade = 1.0 - smoothstep(0.72, 1.0, progress);
        opacity = (opacity * coreFade + rim * 0.52 * edgeFade) * (1.0 - smoothstep(0.93, 1.0, progress));
        surfaceColor = mix(surfaceColor, hotColor, rim * (1.0 - coreFade) * 0.55);
    }
    return over(result, vec4(surfaceColor, clamp(opacity, 0.0, 0.97)));
}

vec4 groundShockwave(vec3 worldPosition) {
    float progress = clamp(ColorAndProgress.w, 0.0, 1.0);
    float time = WaveParameters.x;
    float radius = max(CenterAndRadius.w, 0.05);
    float originalRadius = max(MotionParameters.w, 0.05);
    vec3 baseColor = ColorAndProgress.rgb;
    vec3 hotColor = mix(baseColor, vec3(1.0, 0.93, 0.80), 0.76);
    vec3 delta = worldPosition - CenterAndRadius.xyz;
    float radial = length(delta);
    float angle = atan(delta.z, delta.x);
    float irregularA = 0.72 + 0.28 * sin(angle * 5.0 + radial * 0.31 - time * 3.8);
    float irregularB = 0.78 + 0.22 * sin(angle * 9.0 - radial * 0.21 + time * 2.6);
    float arcMask = clamp(irregularA * irregularB, 0.20, 1.0);

    float chargeFade = 1.0 - smoothstep(0.15, 0.24, progress);
    float chargeBand = 1.0 - smoothstep(0.0, 0.28, abs(radial - originalRadius * 1.85));
    chargeBand *= chargeFade * arcMask;

    float expansion = smoothstep(0.12, 0.47, progress);
    float shellFade = 1.0 - smoothstep(0.67, 1.0, progress);
    float behindFront = radius - radial;
    float frontWidth = max(WaveParameters.z, 0.10);
    float trailWidth = max(WaveParameters.y, 0.5);
    float inside = step(0.0, behindFront);
    float mainRing = 1.0 - smoothstep(0.0, frontWidth, abs(behindFront));
    float echoOne = 1.0 - smoothstep(0.0, frontWidth * 1.3, abs(behindFront - radius * 0.08));
    float echoTwo = 1.0 - smoothstep(0.0, frontWidth * 1.65, abs(behindFront - radius * 0.17));
    echoOne *= inside;
    echoTwo *= inside;

    float waveA = abs(fract(radial * 0.145 - time * 1.28) - 0.5) * 2.0;
    float waveB = abs(fract(radial * 0.095 + time * 0.82) - 0.5) * 2.0;
    float pulseLines = pow(1.0 - waveA, 13.0) + pow(1.0 - waveB, 16.0) * 0.48;
    float wake = inside * (1.0 - smoothstep(0.0, trailWidth, max(behindFront, 0.0)));
    float ringEnergy = (mainRing * 1.02 + echoOne * 0.48 + echoTwo * 0.22) * arcMask * shellFade * expansion;
    float crawling = pulseLines * wake * arcMask * 0.27 * shellFade * expansion;
    float flash = (1.0 - smoothstep(0.0, originalRadius * 3.2, radial));
    flash *= exp(-pow((progress - 0.29) * 23.0, 2.0));

    vec3 terrainColor = baseColor * (chargeBand * 0.64 + crawling);
    terrainColor += hotColor * (ringEnergy + flash * 0.73);
    float alpha = clamp(chargeBand * 0.38 + ringEnergy * 0.63 + crawling * 0.24 + flash * 0.42, 0.0, 0.80);
    return vec4(terrainColor, alpha);
}

void main() {
    vec3 camera = CameraAndDepthMode.xyz;
    vec3 ray = worldRayDirection();
    float radius = max(CenterAndRadius.w, 0.05);
    bool impact = WaveParameters.w > 0.5;
    vec3 boundCenter = CenterAndRadius.xyz;
    float boundRadius;
    if (impact) {
        boundRadius = max(radius * 1.45 + 4.0, MotionParameters.w * 2.1 + 2.0);
    } else {
        vec3 direction = normalize(MotionParameters.xyz);
        float trailLength = max(MotionParameters.w, radius * 2.0);
        boundCenter -= direction * trailLength * 0.34;
        boundRadius = radius * 1.85 + trailLength * 0.58;
    }
    vec3 toBound = boundCenter - camera;
    float alongRay = max(dot(toBound, ray), 0.0);
    float offAxis = max(dot(toBound, toBound) - alongRay * alongRay, 0.0);
    if (offAxis > boundRadius * boundRadius) {
        fragColor = vec4(0.0);
        return;
    }

    float depth = texture(WorldDepth, screenUv).r;
    vec3 worldPosition = depth < 1.0 ? reconstructWorldPosition(depth) : camera + ray * 1000000.0;
    float sceneDistance = depth < 1.0 ? distance(worldPosition, camera) : 1000000.0;
    vec4 sphere = continuousSphere(camera, ray, sceneDistance);
    if (!impact || depth >= 1.0) {
        fragColor = sphere;
        return;
    }

    vec4 ground = groundShockwave(worldPosition);
    float sphereHit = firstSphereHit(camera, ray, CenterAndRadius.xyz, radius);
    fragColor = sphereHit > 0.0 && sphereHit < sceneDistance - 0.08
            ? over(ground, sphere)
            : over(sphere, ground);
}
