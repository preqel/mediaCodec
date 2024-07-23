package com.example.testmediacodec

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import androidx.activity.ComponentActivity
import java.io.IOException

//https://blog.csdn.net/userhu2012/article/details/134413862
class HasPreviewActivity : ComponentActivity(), SurfaceHolder.Callback{


   lateinit var surfaceView:SurfaceView

//    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        enableEdgeToEdge()
        setContentView(R.layout.main4)
        surfaceView = findViewById(R.id.surfaceView) as SurfaceView
        surfaceView.holder.addCallback(this)
       surfaceView.holder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        val mOk = findViewById<Button>(R.id.btnOK)
        mOk.setOnClickListener {
      //      startBackGround()
            start_camera()
        }
    val mstop = findViewById<Button>(R.id.btnstop)
    mstop.setOnClickListener {
              stop_camera()
    }
    }

    private fun stop_camera() {
        mCamera!!.stopPreview()
        mCamera!!.release()
    }
    var mCamera:Camera?= null

    fun start_camera(){

        try {
            mCamera   = Camera.open()
        } catch (e: RuntimeException) {
            Log.e("TAG23", "init_camera: $e")
            return
        }
        val param = mCamera?.getParameters()
        //modify parameter
        param?.previewFrameRate = 20
        param?.setPreviewSize(176, 144)
        mCamera?.setParameters(param)
        try {
            mCamera?.setPreviewDisplay(surfaceView.holder)
            mCamera?.setPreviewCallback(object: Camera.PreviewCallback {
                override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
                    Log.d("TAG23","接受到数据了")
                }

            })
            mCamera?.startPreview()
            //camera.takePicture(shutter, raw, jpeg)
        } catch (e: Exception) {
            Log.e("TAG23", "init_camera: $e")
            return
        }
    }

    private fun startBackGround() {


        var mCameraID: Int = Camera.CameraInfo.CAMERA_FACING_BACK
        //new SurfaceTexture
       // val  surfaceTexture:SurfaceTexture = SurfaceTexture(0);
        //打开摄像头
        var mCamera:Camera?= null
        try {
              mCamera = Camera.open()
        }catch (e:RuntimeException){
            e.printStackTrace()
        }
        //设置camera参数
        val  params: Camera.Parameters = mCamera!!.getParameters();
   //     params.setZoom(0)
//        params.setPreviewFormat(ImageFormat.NV21)
        params.previewFrameRate = 20

        val supportedPreviewSizes: List<Camera.Size> = mCamera!!.parameters.supportedPreviewSizes
      //  params.setPreviewSize(surfaceTexture, 1024);
     //   params.setPictureSize(600, 1024);
        val size = supportedPreviewSizes[0]
        params.setPreviewSize(size.width ,size.height)
        mCamera.parameters = params
        //设置回调用于获取摄像头数据
        mCamera.setPreviewCallback(mPreviewCallback )
        try {
            //这一步是最关键的，使用surfaceTexture 来承载相机的预览，而不需要设置一个可见的view
            //mCamera.setPreviewTexture(surfaceTexture);
            mCamera.setPreviewDisplay(surfaceView.holder);
            mCamera.startPreview()
        } catch ( e: IOException) {
            Log.e("TAG23",e.message.toString())
            e.printStackTrace()
        }
    }


    val mPreviewCallback :Camera.PreviewCallback = object: Camera.PreviewCallback {

        override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
            Log.d("TAG23", "产生了数据" + data?.size)
        }

    };

    override fun surfaceCreated(holder: SurfaceHolder) {


    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }
}