#version 300 es
precision mediump float;
uniform sampler2D uTextureUnit;
in vec2 vTexCoord;
out vec4 fragColor;
void main() {
     vec4 color = texture(uTextureUnit, vTexCoord);
     float rgb = color.g;
     vec4 c = vec4(rgb, rgb, rgb, color.a);
     fragColor = c;
}