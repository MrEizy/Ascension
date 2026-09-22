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
    vec3 worldOffset = (InverseView * vec4(cameraOffset, 0.0)).xyz;
    return CameraAndDepthMode.xyz + worldOffset;
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
    if (nearHit > 0.0) {
        return nearHit;
    }
    return farHit > 0.0 ? farHit : -1.0;
}

float rayGlowSquared(vec3 rayOrigin, vec3 rayDirection, vec3 center, float innerRadius, float outerRadius, float sceneDistance) {
    vec3 toCenter = center - rayOrigin;
    float alongRay = max(dot(toCenter, rayDirection), 0.0);
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

vec4 travelEffect(vec3 camera, vec3 rayDirection, float sceneDistance) {
    vec3 center = CenterAndRadius.xyz;
    float radius = max(CenterAndRadius.w, 0.08);
    float time = WaveParameters.x;
    float travelProgress = clamp(ColorAndProgress.w, 0.0, 1.0);
    vec3 direction = normalize(MotionParameters.xyz);
    float trailLength = max(MotionParameters.w, radius * 2.0);
    vec3 baseColor = ColorAndProgress.rgb;
    vec3 hotColor = mix(baseColor, vec3(1.0, 0.92, 0.78), 0.70);
    vec3 coreColor = baseColor * vec3(0.055, 0.035, 0.025);

    float halo = rayGlowSquared(
            camera,
            rayDirection,
            center,
            radius * 1.04,
            radius * 1.62 + 0.12,
            sceneDistance
    );

    vec3 trailCenterA = center - direction * trailLength * 0.30;
    vec3 trailCenterB = center - direction * trailLength * 0.72;
    float trailA = rayGlowSquared(
            camera,
            rayDirection,
            trailCenterA,
            radius * 0.40,
            radius * 0.92,
            sceneDistance
    );
    float trailB = rayGlowSquared(
            camera,
            rayDirection,
            trailCenterB,
            radius * 0.20,
            radius * 0.58,
            sceneDistance
    );

    float pulse = 0.84 + 0.16 * sin(time * 8.0);
    float trailFade = 1.0 - smoothstep(0.78, 1.0, travelProgress);
    vec3 color = baseColor * halo * halo * (0.16 + pulse * 0.07);
    color += baseColor * trailA * trailA * 0.11 * trailFade;
    color += hotColor * trailB * trailB * 0.035 * trailFade;
    float alpha = max(halo * 0.10, max(trailA * 0.055, trailB * 0.025));

    float hitDistance = firstSphereHit(camera, rayDirection, center, radius);
    if (hitDistance > 0.0 && hitDistance <= sceneDistance + 0.08) {
        vec3 hitPosition = camera + rayDirection * hitDistance;
        vec3 normal = normalize(hitPosition - center);
        float facing = clamp(dot(normal, -rayDirection), 0.0, 1.0);
        float rim = pow(1.0 - facing, 2.05);
        float currents = organicCurrents(normal, time);
        float heartbeat = 0.88 + 0.12 * sin(time * 9.5);
        float currentStrength = currents * heartbeat;
        float heat = clamp(rim * 1.20 + currentStrength * 0.92, 0.0, 1.0);
        vec3 sphereColor = mix(coreColor, baseColor * 0.27, currentStrength * 0.40);
        sphereColor = mix(sphereColor, hotColor, heat);
        float sphereAlpha = clamp(0.82 + rim * 0.15 + currentStrength * 0.04, 0.0, 0.97);
        color = mix(color, sphereColor, sphereAlpha);
        alpha = max(alpha, sphereAlpha);
    }

    return vec4(color, clamp(alpha, 0.0, 0.97));
}

vec4 impactEffect(vec3 camera, vec3 rayDirection, float depth, float sceneDistance) {
    vec3 center = CenterAndRadius.xyz;
    float impactRadius = max(CenterAndRadius.w, 0.1);
    float progress = clamp(ColorAndProgress.w, 0.0, 1.0);
    float time = WaveParameters.x;
    float trailWidth = max(WaveParameters.y, 0.5);
    float frontWidth = max(WaveParameters.z, 0.1);
    vec3 baseColor = ColorAndProgress.rgb;
    vec3 hotColor = mix(baseColor, vec3(1.0, 0.93, 0.80), 0.76);
    vec3 coreColor = baseColor * vec3(0.05, 0.032, 0.022);

    float coreRadius = min(3.1, max(0.82, sqrt(impactRadius) * 0.52));
    float chargeFade = 1.0 - smoothstep(0.18, 0.31, progress);
    float burstProgress = clamp((progress - 0.16) / 0.24, 0.0, 1.0);
    float burstEase = 1.0 - pow(1.0 - burstProgress, 3.0);
    float aftershock = smoothstep(0.42, 0.92, progress);
    float shellRadius = mix(coreRadius, impactRadius, burstEase) * mix(1.0, 1.10, aftershock);
    float shellFade = 1.0 - smoothstep(0.70, 1.0, progress);

    vec3 color = vec3(0.0);
    float alpha = 0.0;

    float chargePulse = 0.94 + 0.06 * sin(time * 13.0);
    float chargeRadius = coreRadius * chargePulse;
    float coreHit = firstSphereHit(camera, rayDirection, center, chargeRadius);
    if (chargeFade > 0.001 && coreHit > 0.0 && coreHit <= sceneDistance + 0.08) {
        vec3 hitPosition = camera + rayDirection * coreHit;
        vec3 normal = normalize(hitPosition - center);
        float facing = clamp(dot(normal, -rayDirection), 0.0, 1.0);
        float rim = pow(1.0 - facing, 2.0);
        float currents = organicCurrents(normal, time * 1.15);
        float heat = clamp(rim * 1.25 + currents * 0.95, 0.0, 1.0);
        vec3 sphereColor = mix(coreColor, hotColor, heat);
        float sphereAlpha = (0.82 + rim * 0.14 + currents * 0.04) * chargeFade;
        color = mix(color, sphereColor, sphereAlpha);
        alpha = max(alpha, sphereAlpha);
    }

    if (burstProgress > 0.0) {
        float shellHit = firstSphereHit(camera, rayDirection, center, shellRadius);
        if (shellHit > 0.0 && shellHit <= sceneDistance + 0.10) {
            vec3 hitPosition = camera + rayDirection * shellHit;
            vec3 normal = normalize(hitPosition - center);
            float facing = clamp(dot(normal, -rayDirection), 0.0, 1.0);
            float rim = pow(1.0 - facing, 5.0);
            float currents = organicCurrents(normal, time * 1.28 + shellRadius * 0.035);
            float edgeCurrent = currents * pow(1.0 - facing, 1.7);
            float shellAlpha = clamp((rim * 0.74 + edgeCurrent * 0.16) * shellFade, 0.0, 0.68);
            vec3 shellColor = mix(baseColor * 0.58, hotColor, clamp(rim * 1.25 + edgeCurrent * 0.55, 0.0, 1.0));
            color = mix(color, shellColor, shellAlpha);
            alpha = max(alpha, shellAlpha);
        }

        float haloOuter = rayGlowSquared(
                camera,
                rayDirection,
                center,
                shellRadius * 0.98,
                shellRadius * 1.035 + 0.18,
                sceneDistance
        );
        float haloInner = rayGlowSquared(
                camera,
                rayDirection,
                center,
                shellRadius * 0.90,
                shellRadius * 0.985,
                sceneDistance
        );
        float haloBand = max(haloOuter - haloInner, 0.0);
        color += hotColor * haloBand * 0.18 * shellFade;
        alpha = max(alpha, haloBand * 0.11 * shellFade);
    }

    if (depth < 1.0) {
        vec3 worldPosition = reconstructWorldPosition(depth);
        vec3 delta = worldPosition - center;
        float radial = length(delta);
        float angle = atan(delta.z, delta.x);

        float irregularA = 0.72 + 0.28 * sin(angle * 5.0 + radial * 0.31 - time * 3.8);
        float irregularB = 0.78 + 0.22 * sin(angle * 9.0 - radial * 0.21 + time * 2.6);
        float arcMask = clamp(irregularA * irregularB, 0.20, 1.0);

        float chargeGroundRadius = coreRadius * (1.85 + 0.16 * sin(time * 6.5));
        float chargeBand = 1.0 - smoothstep(0.0, 0.30, abs(radial - chargeGroundRadius));
        chargeBand *= chargeFade * arcMask;

        float behindFront = shellRadius - radial;
        float mainRing = 1.0 - smoothstep(0.0, frontWidth, abs(behindFront));
        float echoOne = 1.0 - smoothstep(0.0, frontWidth * 1.30, abs(behindFront - impactRadius * 0.085));
        float echoTwo = 1.0 - smoothstep(0.0, frontWidth * 1.65, abs(behindFront - impactRadius * 0.175));
        float inside = step(0.0, behindFront);
        echoOne *= inside;
        echoTwo *= inside;

        float waveA = abs(fract(radial * 0.145 - time * 1.28) - 0.5) * 2.0;
        float waveB = abs(fract(radial * 0.095 + time * 0.82) - 0.5) * 2.0;
        float pulseLines = pow(1.0 - waveA, 13.0) + pow(1.0 - waveB, 16.0) * 0.48;
        float wake = 1.0 - smoothstep(0.0, trailWidth, max(behindFront, 0.0));
        wake *= inside * burstProgress;

        float ringEnergy = (mainRing * 1.08 + echoOne * 0.50 + echoTwo * 0.23) * arcMask * shellFade * burstProgress;
        float crawlingEnergy = pulseLines * wake * arcMask * 0.27 * shellFade;
        float flash = (1.0 - smoothstep(0.0, coreRadius * 3.2, radial));
        flash *= exp(-pow((progress - 0.28) * 23.0, 2.0));

        vec3 terrainColor = baseColor * (chargeBand * 0.64 + crawlingEnergy);
        terrainColor += hotColor * (ringEnergy + flash * 0.78);
        float terrainAlpha = clamp(chargeBand * 0.38 + ringEnergy * 0.63 + crawlingEnergy * 0.24 + flash * 0.42, 0.0, 0.80);

        color = mix(color, terrainColor, terrainAlpha);
        alpha = max(alpha, terrainAlpha);
    }

    return vec4(color, clamp(alpha, 0.0, 0.97));
}

void main() {
    float depth = texture(WorldDepth, screenUv).r;
    vec3 camera = CameraAndDepthMode.xyz;
    vec3 rayDirection = worldRayDirection();

    if (WaveParameters.w <= 0.5) {
        vec3 direction = normalize(MotionParameters.xyz);
        float radius = max(CenterAndRadius.w, 0.08);
        float trailLength = max(MotionParameters.w, radius * 2.0);
        vec3 boundCenter = CenterAndRadius.xyz - direction * trailLength * 0.34;
        float boundRadius = radius * 1.85 + trailLength * 0.58;
        vec3 toBound = boundCenter - camera;
        float alongRay = max(dot(toBound, rayDirection), 0.0);
        float perpendicularSquared = max(dot(toBound, toBound) - alongRay * alongRay, 0.0);
        if (perpendicularSquared > boundRadius * boundRadius) {
            fragColor = vec4(0.0);
            return;
        }

        float sceneDistance = depth < 1.0
                ? distance(reconstructWorldPosition(depth), camera)
                : 1000000.0;
        fragColor = travelEffect(camera, rayDirection, sceneDistance);
        return;
    }

    float sceneDistance = depth < 1.0
            ? distance(reconstructWorldPosition(depth), camera)
            : 1000000.0;
    fragColor = impactEffect(camera, rayDirection, depth, sceneDistance);
}
