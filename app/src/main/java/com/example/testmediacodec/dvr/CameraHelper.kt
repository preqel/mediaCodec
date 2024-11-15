package com.example.testmediacodec.dvr

import android.app.Activity
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import java.io.IOException

/**
 * @author: w.k
 * @date: 2024/11/15
 * @description:
 */
class CameraHelper(
    private val mActivity: Activity,
    private var mCameraId: Int,
    private var mWidth: Int,
    private var mHeight: Int
) : Camera.PreviewCallback{

    private lateinit var mCamera: Camera
    private lateinit var mSurfaceTexture: SurfaceTexture
    private lateinit var mBuffer: ByteArray
//    private var mPreviewCallback: CameraPreviewCallback? = null

    /**
     * 开始摄像头预览
     */
    fun startPreview(surfaceTexture: SurfaceTexture) {
        // 1.保存传入的 SurfaceTexture
        mSurfaceTexture = surfaceTexture

        try {
            // 2.打开摄像头
            mCamera = Camera.open(mCameraId )

            // 3.设置摄像头
            // 3.1 设置摄像头参数
            val param = mCamera.parameters
            // 预览格式为 NV21
            param.previewFormat = ImageFormat.NV21
            // 预览尺寸
          //  param.setPreviewSize(mWidth, mHeight)
            // 更新摄像头参数
            mCamera.parameters = param

            // 3.2 将摄像头采集到的图像旋转为正方向
//            setPreviewOrientation()

            // 3.3 设置接收预览数据的缓冲区与回调
            // 图像数据缓存，NV21 属于 YUV420，占用大小为 RGB 的一半
            mBuffer = ByteArray(mWidth * mHeight * 3 / 2)
            // 将 mBuffer 添加到预览回调的缓冲队列以接收回调数据
            mCamera.addCallbackBuffer(mBuffer)
            // 设置预览回调
            mCamera.setPreviewCallback(this)

            // 3.4 设置展示预览画面的纹理，这样 SurfaceTexture 中
            // 也有一份图像数据，可以传给 OpenGL 渲染到屏幕上
            mCamera.setPreviewTexture(mSurfaceTexture)

            // 4.开启预览
            mCamera.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

        override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
            Log.d("TAG36", "onPreviewFrame execute");
    }
}
