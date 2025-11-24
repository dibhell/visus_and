#version 300 es
precision mediump float;
in vec2 vUV;
out vec4 fragColor;
uniform sampler2D uTexture;
uniform float uTime;
uniform vec2 uResolution;
uniform vec3 uBands;
uniform int uEffectId;

vec3 rgbShift(vec2 uv, float amt) {
    vec2 rUV = uv + vec2(amt, 0.0);
    vec2 gUV = uv;
    vec2 bUV = uv - vec2(amt, 0.0);
    return vec3(texture(uTexture, rUV).r, texture(uTexture, gUV).g, texture(uTexture, bUV).b);
}

vec3 applyEffect(vec2 uv, vec3 src) {
    if (uEffectId == 2) {
        return 1.0 - src;
    }
    if (uEffectId == 1) {
        float amt = 0.02 + 0.04 * uBands.x;
        return rgbShift(uv, amt);
    }
    if (uEffectId == 3) {
        float line = step(0.9, fract(uv.y * 80.0 + uTime * 5.0));
        return mix(src, vec3(line), 0.3);
    }
    return src;
}

void main() {
    vec3 base = texture(uTexture, vUV).rgb;
    vec3 outColor = applyEffect(vUV, base);
    fragColor = vec4(outColor, 1.0);
}
