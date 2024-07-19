package com.yjx.inoexdash.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.view.Gravity
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.ViewGroup
import android.view.WindowManager
import com.example.testmediacodec.other.AutoFitTextureView
import com.yjx.baselib.tools.camera2.Camera2Helper
import com.yjx.baselib.tools.camera2.OnRecordListener

/**
 *@author: QiuYunLiang
 *@date: 2022/11/10
 *@description: 后台摄像服务
 */

class CameraRecordService : Service() {
    /**
     * 预览视图
     */
    private var autoFitTextureView: AutoFitTextureView? = null

    /**
     * 系统窗口管理类
     */
    private var mWindowManager: WindowManager? = null

    private var mWidth: Int? = null
    private var mHeight: Int? = null
    private var mTextureView: TextureView? = null
    private var isPreview: Boolean = false
    private var mCamera: Camera2Helper? = null

    private val mSurfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(
            surfaceTexture: SurfaceTexture,
            width: Int, height: Int
        ) {
            Log.e("qyl", "onSurfaceTextureAvailable  ${width}   ${height}")
            openCamera(width, height, autoFitTextureView!!)
        }

        override fun onSurfaceTextureSizeChanged(
            surfaceTexture: SurfaceTexture,
            width: Int, height: Int
        ) {
            Log.e("qyl", "onSurfaceTextureSizeChanged  ${width}  ${height}")
            if (isPreview) {
                mCamera?.startPreview(width, height)
            }

        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            Log.e("qyl", "onSurfaceTextureDestroyed ")
            return true
        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {

        }
    }

//    private val iCameraHelperAidlInterface: ICameraHelperAidlInterface.Stub =
//        object : ICameraHelperAidlInterface.Stub() {
//            override fun startRecord(
//                type: Int,
//                value: Long,
//                voiceModel: Int,
//                listener: IRecordCallBack
//            ) {
//                mCamera?.startRecordingVideo(type, value, voiceModel,
//                    object : OnRecordListener {
//                        override fun onRecordFinish(path: String) {
//                            try {
//                                listener.onRecordFinish(path)
//                            } catch (e: RemoteException) {
//                                e.printStackTrace()
//                            }
//                        }
//
//                        override fun onRecordError(error: String?) {
//                            try {
//                                //实现回调单一选项，避免回调缓存
//                                var isOpened = false
//                                //如果是相机问题，先尝试打开一次，如果还是失败就放弃
//                                if (error == Camera2Helper.NOT_READY) {
//                                    mCamera?.openCamera(
//                                        this@CameraRecordService,
//                                        mWidth!!,
//                                        mHeight!!,
//                                        mTextureView!!,
//                                        false
//                                    ) {
//                                        if (it) {
//                                            isOpened = true
//                                            mCamera?.startRecordingVideo(
//                                                type,
//                                                value,
//                                                voiceModel,
//                                                object : OnRecordListener {
//                                                    override fun onRecordFinish(path: String) {
//                                                        listener.onRecordFinish(path)
//                                                    }
//
//                                                    override fun onRecordError(error: String?) {
//                                                        listener.onRecordError(error)
//                                                    }
//
//                                                    override fun onRecordStart() {
//                                                        listener.onRecordStart()
//                                                    }
//
//                                                })
//                                        } else {
//                                            if (!isOpened) {
//                                                listener.onRecordError(error)
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    listener.onRecordError(error)
//                                }
//                            } catch (e: RemoteException) {
//                                e.printStackTrace()
//                            }
//                        }
//
//                        override fun onRecordStart() {
//                            listener.onRecordStart()
//                        }
//                    })
//            }
//
//            override fun updateSurfaceViewLayout(
//                width: Int,
//                height: Int,
//                x: Int,
//                y: Int,
//                gravity: Int,
//                isPreview: Boolean
//            ) {
//                autoFitTextureView?.let {
//                    if (mCamera?.isCameraOpen() == false) {
//                        mCamera?.openCamera(
//                            this@CameraRecordService,
//                            mWidth!!,
//                            mHeight!!,
//                            mTextureView!!,
//                            false
//                        ) {
//                            if (it) {
//                                realUpdateSurfaceViewLayout(
//                                    width,
//                                    height,
//                                    x,
//                                    y,
//                                    gravity,
//                                    isPreview,
//                                    autoFitTextureView!!
//                                )
//                            }
//                        }
//                    } else {
//                        realUpdateSurfaceViewLayout(
//                            width,
//                            height,
//                            x,
//                            y,
//                            gravity,
//                            isPreview,
//                            autoFitTextureView!!
//                        )
//                    }
//                }
//            }
//
//            override fun stopRecord() {
//                mCamera?.stopRecordingVideo()
//            }
//
//            override fun closeCamera() {
//                mCamera?.closeCamera(false)
//            }
//        }
//
//    private fun realUpdateSurfaceViewLayout(
//        width: Int,
//        height: Int,
//        x: Int,
//        y: Int,
//        gravity: Int,
//        isPreview: Boolean,
//        autoFitTextureView: TextureView
//    ) {
//        this.isPreview = isPreview
//        if (!isPreview) {
//            mCamera?.closePreviewSession()
//        }
//        val layoutFlag: Int =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//            } else {
//                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
//            }
//        val layoutParams = WindowManager.LayoutParams(
//            width, height,
//            layoutFlag,
//            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
//                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
//                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//            PixelFormat.TRANSLUCENT
//        )
//        layoutParams.gravity = gravity
//        layoutParams.x = x
//        layoutParams.y = y
//        mWindowManager?.updateViewLayout(autoFitTextureView, layoutParams)
//    }

    override fun onCreate() {
        super.onCreate()
        mCamera = Camera2Helper()
        setupSurfaceViewForRecording()
    }

//    override fun onBind(intent: Intent?): IBinder {
////        return iCameraHelperAidlInterface
//          return IBinder();
//    }

    override fun onDestroy() {
        super.onDestroy()
        mCamera?.closeCamera(false)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun setupSurfaceViewForRecording() {
        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mWindowManager = this.getSystemService(WINDOW_SERVICE) as WindowManager
        }
        autoFitTextureView = AutoFitTextureView(this)
        val layoutParams = WindowManager.LayoutParams(
            1, 1,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.START or Gravity.TOP
        layoutParams.gravity = Gravity.CENTER
        mWindowManager?.addView(autoFitTextureView, layoutParams)
        prepareView()
    }

    fun updateSurfaceViewLayout(params: ViewGroup.LayoutParams) {
        autoFitTextureView?.let {
            mWindowManager?.updateViewLayout(autoFitTextureView, params)
        }
    }

    /**
     * 准备视图
     */
    private fun prepareView() {
        if (autoFitTextureView!!.isAvailable) {
            openCamera(
                autoFitTextureView!!.width,
                autoFitTextureView!!.height,
                autoFitTextureView!!
            )
        } else {
            autoFitTextureView!!.surfaceTextureListener = mSurfaceTextureListener
        }
    }

    /**
     * 打开摄像头
     *
     * @param width
     * @param height
     */
    private fun openCamera(width: Int, height: Int, textureView: TextureView) {
        mWidth = width
        mHeight = height
        mTextureView = textureView
        mCamera?.openCamera(
            this, width,
            height, textureView, false, null
        )
    }

}