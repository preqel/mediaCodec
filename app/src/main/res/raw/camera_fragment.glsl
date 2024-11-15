// 由于是使用 Android 设备的摄像头进行采样，因此不能使用
// 常规的 sampler2D 采样器，而是使用 OpenGL 扩展
// GL_OES_EGL_image_external，该扩展支持从外部纹理中进行纹理采样
#extension GL_OES_EGL_image_external : require

// 声明本着色器中的 float 是中等精度
precision mediump float;

// 采样点坐标，即从顶点着色器传递过来的插值后的纹理坐标
varying vec2 aCoord;

// 统一变量 vTexture，它是一个外部（扩展）纹理采样器，用于从外部纹理中采样颜色
uniform samplerExternalOES vTexture;

void main() {
    // 通过使用外部纹理采样器 vTexture 和插值后的纹理坐标 aCoord，
    // 从外部纹理中采样对应位置的颜色，并将结果赋值给内置变量 gl_FragColor，
    // 表示该片段的最终颜色
    gl_FragColor = texture2D(vTexture, aCoord);
}
