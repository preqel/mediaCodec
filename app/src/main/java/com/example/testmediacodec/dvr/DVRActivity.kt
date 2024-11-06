package com.example.testmediacodec.dvr

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.example.testmediacodec.R

class DVRActivity : ComponentActivity(), Model.Callback{

    private var mImageView: ImageView? = null
    private var mEGlSurface: MyEGLSurface? = null

    val surfacetesxtid = 1
    private var mCamera :Camera?= null
    private var glSurfaceView:GLSurfaceView?= null
    private var surfaceTexture:SurfaceTexture?= null

    var recordSurfaceRenderHandler = RecordSurfaceRenderHandler.createHandler()

    override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dvr)
        mImageView = findViewById(R.id.iv_show)
        glSurfaceView = findViewById(R.id.GLsurfaceView)

        findViewById<Button>(R.id.btnOK).setOnClickListener{
            //RecordSurfaceRenderHandler()'
            //todo 这里
            //recordSurfaceRenderHandler.setEglContext();
            start_camera()
        }

        findViewById<Button>(R.id.btnstop).setOnClickListener {
            val message = recordSurfaceRenderHandler.obtainMessage()
           recordSurfaceRenderHandler.sendMessage(message);
            val handler = RecordSurfaceRenderHandler.createHandler()
            val eglbase = EGLBase(this@DVRActivity)
            eglbase.createEGLEnv()
            if(eglbase.mEGLContext != null){
                handler.setEglContext(eglbase.mEGLContext , surfacetesxtid, surfaceTexture,true )
            }
        }


        surfaceTexture = SurfaceTexture(surfacetesxtid)

//        val message = recordSurfaceRenderHandler.obtainMessage()
//        recordSurfaceRenderHandler.sendMessage(message);
        initEGLSurface()
        mEGlSurface!!.requestRender()
}

    private fun initEGLSurface() {
        mEGlSurface = MyEGLSurface(this)
        val render: MyRender = MyRender(resources)
        render.setCallback(this)
        mEGlSurface!!.init(render)
    }

    override fun onCall(bitmap: Bitmap?) {
        runOnUiThread {
            mImageView!!.setImageBitmap(bitmap)
        }
    }

    var mTransformMatrix = FloatArray(16)

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

            //如果不要图形
            mCamera?.setPreviewTexture(surfaceTexture)
       //     mCamera.setPreviewDisplay(glSurfaceView?.holder)
//            mCamera?.setPreviewDisplay()
            //如果要显示camera
//            surfaceView.visibility = View.VISIBLE
//            mCamera?.setPreviewDisplay(surfaceView.holder)
            mCamera?.setPreviewCallback(object: Camera.PreviewCallback {
                override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
                    Log.d("TAG23","接受到数据了" )
                    if(data!= null && data.size> 0 ){
                        Log.d("TAG23","数据shi" + data.size)
                    }
                }
            })
            surfaceTexture?.setOnFrameAvailableListener {
                    surfaceTexture->

                if(mTransformMatrix== null){
                    mTransformMatrix = FloatArray(16)
                }
                surfaceTexture.getTransformMatrix(mTransformMatrix)
                val timestamp = surfaceTexture.timestamp
                if(timestamp == 0L){
                    return@setOnFrameAvailableListener
                }
                //向另外要给线程发送数据，感谢https://www.jianshu.com/p/702e7b065eb3
                recordSurfaceRenderHandler.sendMessage(
                    recordSurfaceRenderHandler.obtainMessage(
                        RecordSurfaceRenderHandler.MSG_RENDER_DRAW2,
                        (timestamp shr 32) as Int, timestamp as Int, mTransformMatrix
                    )
                )

                Log.d("TAG23","OnFrameAvailable")
                //   surfaceTexture.updateTexImage()
            }
            mCamera?.startPreview()
            //camera.takePicture(shutter, raw, jpeg)
        } catch (e: Exception) {
            Log.e("TAG23", "init_camera: $e")
            return
        }
    }

}