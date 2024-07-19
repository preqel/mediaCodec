package com.yjx.baselib.tools.camera2

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * @author: QiuYunLiang
 * @date: 2022/11/10
 * @description: camera2辅助类
 */

class Camera2Helper {

    /**
     *  预览的一个建筑器
     */
    private var mPreviewBuilder: CaptureRequest.Builder? = null

    /**
     * A reference to the opened [android.hardware.camera2.CameraDevice].
     */
    private var mCameraDevice: CameraDevice? = null

    /**
     * MediaRecorder
     */
    private var mMediaRecorder: MediaRecorder? = null

    /**
     * 传感器获取到的设备方向
     */
    private var mSensorOrientation: Int? = null

    /**
     * 相机预览
     */
    private var mPreviewSize: Size? = null

    /**
     * 录制的大小
     */
    private var mVideoSize: Size? = null

    /**
     * 同步信号量节流
     */
    private val mCameraOpenCloseLock = Semaphore(1)

    /**
     * An [AutoFitTextureView] for camera preview.
     */
    private var mTextureView: TextureView? = null

    /**
     * 不录制时是否需要预览界面
     */
    private var mIsNeedPreView: Boolean = false

    /**
     * A reference to the current [android.hardware.camera2.CameraCaptureSession] for
     * preview.
     */
    private var mPreviewSession: CameraCaptureSession? = null

    /**
     * 创建子线程handler的一个工具类
     */
    private var mBackgroundThread: HandlerThread? = null

    /**
     *子线程的handle
     */
    private var mBackgroundHandler: Handler? = null

    /**
     * 视频路径
     */
    private var mNextVideoAbsolutePath: String? = null

    /**
     * 录制模式
     */
    private var mRecordModel: Int = -1

    /**
     * 是否正在录制
     */
    @Volatile
    private var mIsRecording: Boolean = false

    private var choices: Array<Size> = arrayOf()

    /**
     * 录制监听
     */
    private var mRecordListener: OnRecordListener? = null

    private var mOpenCallBack: ((Boolean) -> Unit)? = null
    private var mContext: Context? = null
    private val DEFAULT_ORIENTATIONS = SparseIntArray()
    private val INVERSE_ORIENTATIONS = SparseIntArray()

    companion object {
        private val TAG: String = Camera2Helper::class.java.simpleName
        private const val SENSOR_ORIENTATION_DEFAULT_DEGREES = 90
        private const val SENSOR_ORIENTATION_INVERSE_DEGREES = 270

        //开启录制的模式
        const val NORMAL_MODEL: Int = 0
        const val MAX_DURATION: Int = 1
        const val MAX_FILE_SIZE: Int = 2
        const val CONTINUOUS_BY_FILE_SIZE: Int = 3
        const val CONTINUOUS_BY_DURATION: Int = 4

        //是否启用音频
        const val VOICE_USE: Int = 0
        const val VOICE_NOT_USE: Int = 1

        const val NOT_READY = "not_ready"
        const val RECORDING = "recording"
    }

    init {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90)
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0)
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270)
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180)

        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270)
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180)
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90)
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0)
    }

    fun openCamera(
        context: Context,
        width: Int,
        height: Int,
        textureView: TextureView,
        isNeedPreView: Boolean,
        openCallBack: ((Boolean) -> Unit)?
    ) {
        mOpenCallBack = openCallBack
        mIsNeedPreView = isNeedPreView
        mTextureView = textureView
        mContext = context
        val manager: CameraManager =
            mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw java.lang.RuntimeException("Time out waiting to lock camera opening.")
            }
            val cameraId: String = manager.cameraIdList[0]
            val characteristics: CameraCharacteristics = manager.getCameraCharacteristics(cameraId)
            val map = characteristics
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
            if (map == null) {
                throw java.lang.RuntimeException("Cannot get available preview/video sizes")
            }
            mVideoSize = chooseVideoSize(
                map.getOutputSizes(
                    MediaRecorder::class.java
                )
            )
            choices = map.getOutputSizes(
                SurfaceTexture::class.java
            )
            mPreviewSize = chooseOptimalSize()
            //调整预览
            //configureTransform(width, height)
            mMediaRecorder = MediaRecorder()
            if (ActivityCompat.checkSelfPermission(
                    mContext!!,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                mOpenCallBack?.let { it(false) }
                return
            }
            manager.openCamera(cameraId, mStateCallback, null)
        } catch (e: CameraAccessException) {
            mOpenCallBack?.let { it(false) }
            e.printStackTrace()
        } catch (e: NullPointerException) {
            mOpenCallBack?.let { it(false) }
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw  RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    /**
     * [CameraDevice.StateCallback] is called when [CameraDevice] changes its status.
     */
    private val mStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(@NonNull cameraDevice: CameraDevice) {
            Log.e(TAG, "onOpened ${cameraDevice.id} ")
            mCameraDevice = cameraDevice
            if (mIsNeedPreView) {
                startBackgroundThread()
                startPreview()
            }
            mCameraOpenCloseLock.release()
            mOpenCallBack?.let { it(true) }
        }

        override fun onDisconnected(@NonNull cameraDevice: CameraDevice) {
            Log.e(TAG, "onDisconnected ${cameraDevice.id} ")
            closeCamera(true)
            mOpenCallBack?.let { it(false) }
        }

        override fun onError(@NonNull cameraDevice: CameraDevice, error: Int) {
            Log.e(TAG, "onError ${cameraDevice.id}  ${error}")
            closeCamera(true)
            mOpenCallBack?.let { it(false) }
        }
    }

    /**
     * Start the camera preview.
     */
    fun startPreview(width: Int = 0, height: Int = 0) {
        if (width != 0 && height != 0) {
            mPreviewSize = chooseOptimalSize()
        }
        if (null == mCameraDevice || !mTextureView!!.isAvailable || null == mPreviewSize) {
            return
        }
        try {
            closePreviewSession()
            val texture = mTextureView!!.surfaceTexture!!
            texture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
            mPreviewBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            val previewSurface = Surface(texture)
            mPreviewBuilder!!.addTarget(previewSurface)
            mCameraDevice!!.createCaptureSession(
                listOf(previewSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(@NonNull session: CameraCaptureSession) {
                        mPreviewSession = session
                        updatePreview()
                    }

                    override fun onConfigureFailed(@NonNull session: CameraCaptureSession) {
                        Log.e("qyl", "onConfigureFailed")
                    }
                }, mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * 开始录制
     */
    fun startRecordingVideo(
        recordModel: Int = NORMAL_MODEL,
        value: Long,
        voiceModel: Int = VOICE_USE,
        listener: OnRecordListener?
    ) {
        mRecordListener = listener
        if (mIsRecording) {
            listener?.onRecordError(RECORDING)
            return
        }
        if (null == mCameraDevice || !mTextureView!!.isAvailable || null == mPreviewSize) {
            Log.e(
                TAG,
                "mCameraDevice:${mCameraDevice}  " +
                        "---mTextureView${mTextureView}++ isAvailable${mTextureView!!.isAvailable} "
                        +
                        " ---mPreviewSize${mPreviewSize}"
            )
            listener?.onRecordError(NOT_READY)
            return
        }
        try {
            startBackgroundThread()
            mRecordModel = recordModel
            closePreviewSession()
            setUpMediaRecorder(value, voiceModel)
            val texture = mTextureView!!.surfaceTexture!!
            texture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
            mPreviewBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            val surfaces: MutableList<Surface> = ArrayList()

            // Set up Surface for the camera preview
            val previewSurface = Surface(texture)
            surfaces.add(previewSurface)
            mPreviewBuilder!!.addTarget(previewSurface)

            // Set up Surface for the MediaRecorder
            val recorderSurface = mMediaRecorder!!.surface
            surfaces.add(recorderSurface)
            mPreviewBuilder!!.addTarget(recorderSurface)

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice!!.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
                        mPreviewSession = cameraCaptureSession
                        updatePreview()
                        try {
                            mIsRecording = true
                            mMediaRecorder!!.start()
                            mRecordListener?.onRecordStart()
                        } catch (e: Exception) {
                            mIsRecording = false
                            if (mRecordListener != null) {
                                Log.e(TAG, "createCaptureSession--$e")
                                mRecordListener?.onRecordError(e.message)
                            }
                        }
                    }

                    override fun onConfigureFailed(
                        @NonNull cameraCaptureSession:
                        CameraCaptureSession
                    ) {
                        Log.e(TAG, "onConfigureFailed error！")
                        mRecordListener?.onRecordError("onConfigureFailed error")
                    }
                },
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            Log.e(TAG, "CameraAccessException $e")
            mRecordListener?.onRecordError(e.message)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "CameraAccessException $e")
            mRecordListener?.onRecordError(e.message)
        }
    }

    /**
     * 停止录制
     */
    fun stopRecordingVideo() {
        var savePath = ""
        try {
            if (mIsRecording) {
                mMediaRecorder!!.setOnInfoListener(null)
                mMediaRecorder!!.stop()
                mMediaRecorder!!.reset()
                savePath = mNextVideoAbsolutePath!!
                mNextVideoAbsolutePath = null
                if (mIsNeedPreView) {
                    startPreview()
                }
                mIsRecording = false
                mRecordListener?.onRecordFinish(savePath)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            mRecordListener?.onRecordError(e.message)
        } finally {
            mRecordListener = null
        }
    }

    @Throws(IOException::class)
    private fun setUpMediaRecorder(value: Long, voiceModel: Int) {
        if (voiceModel == VOICE_USE) {
            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        }
        mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        if (mNextVideoAbsolutePath.isNullOrBlank()) {
            mNextVideoAbsolutePath = getVideoFilePath()
        }
        when (mRecordModel) {
            MAX_DURATION, CONTINUOUS_BY_DURATION -> {
                mMediaRecorder!!.setMaxDuration(value.toInt())
                mMediaRecorder!!.setOnInfoListener(object : MediaRecorder.OnInfoListener {
                    override fun onInfo(mr: MediaRecorder?, what: Int, extra: Int) {
                        Log.e(TAG, "  what: ${what}  extra:${extra}")
                        when (what) {
                            MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED -> {
                                if (mRecordModel == MAX_DURATION) {
                                    //倒计时停止录制
                                    stopRecordingVideo()
                                }
                            }
                        }
                    }

                })
            }
            MAX_FILE_SIZE, CONTINUOUS_BY_FILE_SIZE -> {
                //todo 设置mediaRecord的监听  实现同大小文件联系录制
                mMediaRecorder!!.setMaxFileSize(value)
            }
            else -> {
            }
        }

        mMediaRecorder!!.setOutputFile(mNextVideoAbsolutePath)
        mMediaRecorder!!.setVideoEncodingBitRate(10000000)
        mMediaRecorder!!.setVideoFrameRate(30)
        mMediaRecorder!!.setVideoSize(mVideoSize!!.width, mVideoSize!!.height)
        mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        if (voiceModel == VOICE_USE) {
            mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }

        //如果activity 判断它的横竖屏
        if (mContext is Activity) {
            val rotation = (mContext as Activity).windowManager.defaultDisplay.rotation
            when (mSensorOrientation) {
                SENSOR_ORIENTATION_DEFAULT_DEGREES -> mMediaRecorder!!.setOrientationHint(
                    DEFAULT_ORIENTATIONS.get(
                        rotation
                    )
                )
                SENSOR_ORIENTATION_INVERSE_DEGREES -> mMediaRecorder!!.setOrientationHint(
                    INVERSE_ORIENTATIONS.get(
                        rotation
                    )
                )
            }
        }
        mMediaRecorder!!.prepare()
    }

    /**
     * 获取录制路径
     */
    private fun getVideoFilePath(): String {
        val dir = mContext!!.getExternalFilesDir(null)
        return ((if (dir == null) "" else dir.absolutePath + "/")
                + System.currentTimeMillis() + ".mp4")
    }

    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * 关闭相机释放资源
     * @param isError Boolean  如果是异常处理就不需要关闭预览
     */
    fun closeCamera(isError: Boolean) {
        try {
            stopRecordingVideo()
            mCameraOpenCloseLock.acquire()
            if (!isError) {
                closePreviewSession()
            } else {
                mPreviewSession = null
            }
            if (null != mCameraDevice) {
                mCameraDevice!!.close()
                mCameraDevice = null
            }
            if (null != mMediaRecorder) {
                mMediaRecorder!!.release()
                mMediaRecorder = null
            }
            mIsRecording = false
        } catch (e: InterruptedException) {
            throw java.lang.RuntimeException("Interrupted while trying to lock camera closing.")
        } finally {
            stopBackgroundThread()
            mCameraOpenCloseLock.release()
        }
    }

    fun isCameraOpen(): Boolean {
        return mCameraDevice != null
    }

    fun closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession!!.close()
            mPreviewSession = null
        }
    }

    /**
     * Update the camera preview. [.startPreview] needs to be called in advance.
     */
    private fun updatePreview() {
        if (null == mCameraDevice) {
            return
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder!!)
            val thread = HandlerThread("CameraPreview")
            thread.start()
            mPreviewSession!!.setRepeatingRequest(
                mPreviewBuilder!!.build(),
                null,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun setUpCaptureRequestBuilder(builder: CaptureRequest.Builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
    }

    /**
     * Configures the necessary [android.graphics.Matrix] transformation to `mTextureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        if (null == mTextureView || null == mPreviewSize || mContext !is Activity) {
            return
        }
        val rotation = (mContext as Activity).windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(
            0f, 0f, mPreviewSize!!.height.toFloat(),
            mPreviewSize!!.width.toFloat()
        )
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                viewHeight.toFloat() / mPreviewSize!!.height,
                viewWidth.toFloat() / mPreviewSize!!.width
            )
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        }
        mTextureView!!.setTransform(matrix)
    }

    private fun chooseVideoSize(choices: Array<Size>): Size? {
        for (size in choices) {
            if (size.width == size.height * 4 / 3 && size.width <= 1080) {
                return size
            }
        }
        Log.e(
            "qyl",
            "Couldn't find any suitable video size"
        )
        return choices[choices.size - 1]
    }

    /**
     * Given `choices` of `Size`s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal `Size`, or an arbitrary one if none were big enough
     */
    private fun chooseOptimalSize(): Size? {
        if (choices.isEmpty()) return null
        val textureViewRatio = if (mSensorOrientation == 0) {
            //后置
            mTextureView!!.width.toFloat() / mTextureView!!.height
        } else {
            //前置
            mTextureView!!.height.toFloat() / mTextureView!!.width
        }
        val targetSizeList: MutableList<Size> = ArrayList()
        var mAbsValue = 1000f
        for (option in choices) {
            val optionRatio = option.width.toFloat() / option.height
            val newValue = abs(textureViewRatio - optionRatio)
            if (newValue - mAbsValue < 0f) {
                mAbsValue = newValue
                targetSizeList.clear()
                targetSizeList.add(option)
            } else if (newValue - mAbsValue == 0f) {
                targetSizeList.add(option)
            }
        }
        return if (targetSizeList.size > 0) {
            Collections.max(targetSizeList, CompareSizesByArea())
        } else {
            choices[0]
        }
    }

    /**
     * Compares two `Size`s based on their areas.
     */
    internal class CompareSizesByArea : Comparator<Size?> {
        override fun compare(o1: Size?, o2: Size?): Int {
            return java.lang.Long.signum(
                o1!!.width.toLong() * o1.height -
                        o2!!.width.toLong() * o2.height
            )
        }
    }
}