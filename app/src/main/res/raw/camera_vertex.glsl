// 顶点坐标，用于确定要绘制的图像的外部轮廓
attribute vec4 vPosition;

// 纹理坐标，接收采样器采样图片的坐标
attribute vec4 vCoord;

// 4 * 4 的变换矩阵，需要将原本的 vCoord（01,11,00,10）与
// 变换矩阵相乘，才能得到 SurfaceTexture 正确的采样坐标
uniform mat4 vMatrix;

// 传给片元着色器的向量
varying vec2 aCoord;

void main() {
    // 顶点坐标赋值给内置变量 gl_Position 作为顶点的最终位置
    gl_Position = vPosition;
    // 将变换后的纹理的 xy 坐标传递给片元着色器，但是部分机型
    // 用上面的方式做有问题，所以要采用下面的兼容模式
//    aCoord = vCoord.xy;
    aCoord = (vMatrix * vCoord).xy;
}
