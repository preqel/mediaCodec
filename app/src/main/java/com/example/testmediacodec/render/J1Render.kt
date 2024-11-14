package com.example.testmediacodec.render

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author: w.k
 * @date: 2024/11/14
 * @description:
 */
class J1Render(private var mGLSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer,
    SurfaceTexture.OnFrameAvailableListener {

   // private lateinit var mCameraHelper: CameraHelper
    private lateinit var mTextureIds: IntArray
    private lateinit var mSurfaceTexture: SurfaceTexture
   // private lateinit var mScreenFilter: ScreenFilter
    private val mMatrix: FloatArray = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mTextureIds = IntArray(1)
        // 生成纹理 ID，参数依次为纹理 ID 数组长度、纹理 ID 数组、数组偏移量
        GLES20.glGenTextures(mTextureIds.size, mTextureIds, 0)

        // 2.2 创建 SurfaceTexture
        mSurfaceTexture = SurfaceTexture(mTextureIds[0])

        // 2.3 为 SurfaceTexture 设置数据监听，当有视频帧可用时会回调 onFrameAvailable()
        mSurfaceTexture.setOnFrameAvailableListener(this)

        // 3.创建 ScreenFilter 以进行图像绘制
    //    mScreenFilter = ScreenFilter(mGLSurfaceView.context)


    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    }

    override fun onDrawFrame(gl: GL10?) {
        // 1.清空屏幕为黑色
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        // 设置清理哪一个缓冲区
        // GL_COLOR_BUFFER_BIT 颜色缓冲区
        // GL_DEPTH_BUFFER_BIT 深度缓冲区
        // GL_STENCIL_BUFFER_BIT 模型缓冲区
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 2.更新纹理
        // 2.1 更新离屏渲染的 SurfaceTexture 的数据，即获取新的帧
        mSurfaceTexture.updateTexImage()
        // 2.2 获取到新的帧的变换矩阵
        mSurfaceTexture.getTransformMatrix(mMatrix)

        // 3.交给滤镜进行具体的绘制工作
      //  mScreenFilter.onDrawFrame(mTextureIds[0], mMatrix)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        mGLSurfaceView.requestRender()
    }
}