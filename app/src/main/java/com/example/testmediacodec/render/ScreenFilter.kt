package com.example.testmediacodec.render

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import com.example.testmediacodec.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @author: w.k
 * @date: 2024/11/15
 * @description:
 */
class ScreenFilter(context: Context) {

    // OpenGL 的程序 ID
    private var mProgramId = 0
    // 着色器中声明的变量的地址
    private var vPosition = 0
    private var vCoord = 0
    private var vMatrix = 0
    private var vTexture = 0
    // 给着色器中声明的变量传值时所需要的 Buffer
    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mTextureBuffer: FloatBuffer
    // 要进行绘制的 Surface 的宽高
    private var mWidth = 0
    private var mHeight = 0

    init {
        Log.d("TAG36", "init start")
        // 1.读取顶点着色器和片元着色器代码
        val vertexSource = ResourceReader.readTextFromRawFile(context, R.raw.camera_vertex)
        val fragmentSource = ResourceReader.readTextFromRawFile(context, R.raw.camera_fragment)

        try {
            // 2.编译着色器代码并获取着色器 ID
            val vertexShaderId = ShaderHelper.compileVertexShader(vertexSource)
            val fragmentShaderId = ShaderHelper.compileFragmentShader(fragmentSource)



            // 3.创建着色器程序，并链接顶点和片元着色器
            mProgramId = ShaderHelper.linkProgram(vertexShaderId, fragmentShaderId)

            // 4.获取着色器中声明的对象的地址，后续要通过地址为这些变量赋值
            // 4.1 获取顶点着色器中的属性变量地址
            vPosition = GLES20.glGetAttribLocation(mProgramId, "vPosition")
            vCoord = GLES20.glGetAttribLocation(mProgramId, "vCoord")
            vMatrix = GLES20.glGetUniformLocation(mProgramId, "vMatrix")
            // 4.2 获取片元着色器中变量地址
            vTexture = GLES20.glGetUniformLocation(mProgramId, "vTexture")

            // 5.创建给着色器中声明的变量传值时所需要的 Buffer
            // 5.1 创建顶点坐标 Buffer。顶点坐标，4 个顶点，每个顶点有 XY 两个维度，
            // 每个维度是 4 个字节的 Float，因此总共占 4 * 2 * 4 个字节
            mVertexBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            // 清空一下再赋值
            mVertexBuffer.clear()
            // 传入 OpenGL 世界坐标系的四个顶点坐标，注意顺序
            val vertex = floatArrayOf(
                -1.0f, -1.0f, // 左下
                1.0f, -1.0f, // 右下
                -1.0f, 1.0f, // 左上
                1.0f, 1.0f, // 右上
            )
            mVertexBuffer.put(vertex)

            // 5.2 创建纹理坐标 Buffer
            mTextureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            mTextureBuffer.clear()
            // 传入 Android 屏幕坐标系的四个顶点，顺序要与 v 中的对应
            val texture = floatArrayOf(
                0.0f, 1.0f, // 左下
                1.0f, 1.0f, // 右下
                0.0f, 0.0f, // 左上
                1.0f, 0.0f, // 右上
            )
            mTextureBuffer.put(texture)
            Log.d("TAG36", "init end")
        }catch (e:Exception){
            e.printStackTrace()
        }


    }

    fun onReady(width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }

    fun onDrawFrame(textureId: Int, matrix: FloatArray){
        // 1.目标窗口的位置和大小，传入的是原点（坐标系以左下角）坐标
        GLES20.glViewport(0, 0, mWidth, mHeight)

        // 2.使用着色器程序
        GLES20.glUseProgram(mProgramId)

        // 3.渲染，实际上是为着色器中声明的变量传值的过程
        // 3.1 为顶点坐标赋值
        // NIO Buffer 要养成使用前先移动到 0 的习惯
        mVertexBuffer.position(0)
        // 传值，将 mVertexBuffer 中的值传入到 vPosition 起始的地址中。2 表示是 XY 两个维度
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
        // 传完值后要激活
        GLES20.glEnableVertexAttribArray(vPosition)

        // 3.2 为纹理坐标赋值
        mTextureBuffer.position(0)
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer)
        GLES20.glEnableVertexAttribArray(vCoord)

        // 3.3 为变换矩阵赋值
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, matrix, 0)
        // 4.进行绘制
        // 4.1 激活 textureId 所表示的纹理
        GLES20.glActiveTexture(textureId)
        // 4.2 将 GL_TEXTURE_EXTERNAL_OES 所表示的用于处理外部纹理的纹理对象与纹理绑定
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        // 4.3 将纹理单元索引 0 绑定到采样器变量 vTexture 上
        // vTexture 是片元着色器中声明的采样器 uniform samplerExternalOES vTexture
        GLES20.glUniform1i(vTexture, 0)
        // 4.4 通知 OpenGL 绘制。从第 0 个开始，一共 4 个点
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

    }
}
