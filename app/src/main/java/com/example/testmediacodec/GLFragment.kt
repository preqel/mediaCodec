package com.example.testmediacodec

import android.annotation.SuppressLint
import android.app.Fragment
import android.content.Context
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import com.example.testmediacodec.util.CameraUtil
import com.example.testmediacodec.util.LogUtil
import com.example.testmediacodec.util.PermisstionUtil
import com.example.testmediacodec.view.CameraSensor
import com.example.testmediacodec.view.CameraSensor.CameraSensorListener
import com.example.testmediacodec.widget.CameraFocusView
import com.example.testmediacodec.widget.CameraGLSurfaceView
import com.example.testmediacodec.widget.CameraGLSurfaceView.CameraGLSurfaceViewCallback
import com.example.testmediacodec.widget.CameraProgressButton
import com.example.testmediacodec.widget.CameraSwitchView

class GLFragment : Fragment() , CameraProgressButton.Listener, CameraGLSurfaceViewCallback,
    CameraSensorListener{


    private val REQUEST_CODE: Int = 1
    private val MSG_START_PREVIEW: Int = 1
    private val MSG_SWITCH_CAMERA: Int = 2
    private val MSG_RELEASE_PREVIEW: Int = 3
    private val MSG_MANUAL_FOCUS: Int = 4
    private val MSG_ROCK: Int = 5

    private var mCameraView: CameraGLSurfaceView? = null
    private var mCameraSensor: CameraSensor? = null
    private var mProgressBtn: CameraProgressButton? = null
    private var mFocusView: CameraFocusView? = null
    private var mSwitchView: CameraSwitchView? = null
    private var isFocusing = false
    private var mPreviewSize: Size? = null
    private var mCameraHanlder: Handler? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val contentView = inflater.inflate(R.layout.fragment_camera, container, false)
        initCameraHandler()
        initView(contentView)
        return contentView
    }

    private fun initView(contentView: View) {
        isFocusing = false
        mPreviewSize = null
        mCameraView = contentView.findViewById(R.id.camera_view)
        mProgressBtn = contentView.findViewById(R.id.progress_btn)
        mFocusView = contentView.findViewById(R.id.focus_view)
        mSwitchView = contentView.findViewById(R.id.switch_view)

        mCameraSensor = CameraSensor(context)
        mCameraSensor!!.setCameraSensorListener(this)
        mProgressBtn?.setProgressListener(this)
        mCameraView?.setCallback(this@GLFragment)
        mCameraView?.setOnTouchListener(OnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                focus(event.x.toInt(), event.y.toInt(), false)
                return@OnTouchListener true
            }
            false
        })
        mSwitchView?.setOnClickListener(View.OnClickListener {
            mCameraHanlder!!.sendEmptyMessage(
                MSG_SWITCH_CAMERA
            )
        })
    }

    private fun initCameraHandler() {
        mCameraHanlder = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_START_PREVIEW -> startPreview()
                    MSG_RELEASE_PREVIEW -> releasePreview()
                    MSG_SWITCH_CAMERA -> switchCamera()
                    MSG_MANUAL_FOCUS -> manualFocus(msg.arg1, msg.arg2)
                    MSG_ROCK -> autoFocus()
                    else -> {}
                }
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onDetach() {
       mCameraHanlder!!.sendEmptyMessage(MSG_RELEASE_PREVIEW)
        super.onDetach()
    }

    override fun onSurfaceViewCreate(texture: SurfaceTexture?) {
    }

    override fun onSurfaceViewChange(width: Int, height: Int) {
        Log.e(LogUtil.TAG, "surfaceChanged: ( $width x $height )")
        mPreviewSize = Size(width, height)
        mCameraHanlder!!.sendEmptyMessage(MSG_START_PREVIEW)
    }

    fun startPreview() {
        val  isRquestPermission = requestPermission()

        Log.d(LogUtil.TAG, "isRequestPermission====》"+ isRquestPermission)
        if (mPreviewSize != null && isRquestPermission) {
            if (CameraUtil.getCamera() == null) {
                CameraUtil.openCamera()
                Log.e(LogUtil.TAG, "openCamera")
                CameraUtil.setDisplay(mCameraView!!.surfaceTexture)
            }
            CameraUtil.startPreview(activity, mPreviewSize!!.width, mPreviewSize!!.height)
            mCameraSensor!!.start()
            mSwitchView!!.setOrientation(mCameraSensor!!.x, mCameraSensor!!.y, mCameraSensor!!.z)
        }
    }

    fun releasePreview() {
        CameraUtil.releaseCamera()
        mCameraSensor!!.stop()
        mFocusView!!.cancelFocus()
        Log.e(LogUtil.TAG, "releasePreview releaseCamera")
    }

    fun switchCamera() {
        mFocusView!!.cancelFocus()
        if (CameraUtil.getCamera() != null && mPreviewSize != null) {
            mCameraView!!.releaseSurfaceTexture()
            CameraUtil.releaseCamera()
            CameraUtil.switchCameraId()
            CameraUtil.openCamera()
            mCameraView!!.resumeSurfaceTexture()
            CameraUtil.setDisplay(mCameraView!!.surfaceTexture)
            CameraUtil.startPreview(activity, mPreviewSize!!.width, mPreviewSize!!.height)
        }
    }

    fun autoFocus() {
        if (CameraUtil.isBackCamera() && CameraUtil.getCamera() != null) {
            focus(mCameraView!!.width / 2, mCameraView!!.height / 2, true)
        }
        mSwitchView!!.setOrientation(mCameraSensor!!.x, mCameraSensor!!.y, mCameraSensor!!.z)
    }

    fun manualFocus(x: Int, y: Int) {
        focus(x, y, false)
    }

    private fun focus(x: Int, y: Int, isAutoFocus: Boolean) {
        if (CameraUtil.getCamera() == null || !CameraUtil.isBackCamera()) {
            return
        }
        if (isFocusing && isAutoFocus) {
            return
        }
        isFocusing = true
        val focusPoint = Point(x, y)
        val screenSize = Size(mCameraView!!.width, mCameraView!!.height)
        if (!isAutoFocus) {
            mFocusView!!.beginFocus(x, y)
        }
        CameraUtil.newCameraFocus(
            focusPoint, screenSize
        ) { success, camera ->
            isFocusing = false
            if (!isAutoFocus) {
                mFocusView!!.endFocus(success)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        releasePreview()
    }

    override fun onResume() {
        super.onResume()
        startPreview()
    }

    override fun onShortPress() {
        if (requestPermission()) {
            takePicture()
        }
    }

    @SuppressLint("NewApi")
    private fun takePicture() {
    }

    override fun onStartLongPress() {
        if (requestPermission()) {
            mCameraView!!.startRecord()
        }
    }

    override fun onEndLongPress() {
        Log.d("TAG23", "onEndLOngpress")
        mCameraView!!.stopRecord()
    }

    override fun onEndMaxProgress() {
    }

    override fun onRock() {
        mCameraHanlder!!.sendEmptyMessage(MSG_ROCK)
    }

    private fun requestPermission(): Boolean {
        return (PermisstionUtil.checkPermissionsAndRequest(
            context,
            PermisstionUtil.CAMERA,
            REQUEST_CODE,
            "请求相机权限被拒绝"
        )
                && PermisstionUtil.checkPermissionsAndRequest(
            context,
            PermisstionUtil.STORAGE,
            REQUEST_CODE,
            "请求访问SD卡权限被拒绝"
        ))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCode) {
            mCameraHanlder!!.sendEmptyMessage(MSG_START_PREVIEW)
        }
    }
}