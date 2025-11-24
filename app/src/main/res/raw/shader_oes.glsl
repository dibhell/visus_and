#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;
in vec2 vUV;
out vec4 fragColor;
uniform samplerExternalOES uTexture;
uniform float uTime;
uniform vec2 uResolution;
uniform vec3 uBands;

vec3 rgbShift(vec2 uv, float amt) {
    vec2 rUV = uv + vec2(amt, 0.0);
    vec2 gUV = uv;
    vec2 bUV = uv - vec2(amt, 0.0);
    return vec3(texture(uTexture, rUV).r, texture(uTexture, gUV).g, texture(uTexture, bUV).b);
}

void main() {
    vec3 color = texture(uTexture, vUV).rgb;
    float pulse = 0.02 * uBands.x;
    vec3 shifted = rgbShift(vUV, pulse);
    float glitch = step(0.98, fract(sin(dot(vUV * uTime, vec2(12.9898,78.233))) * 43758.5453));
    vec3 outColor = mix(color, shifted, glitch * 0.6);
    fragColor = vec4(outColor, 1.0);
}
