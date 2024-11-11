package com.example.testmediacodec.dvr

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.MediaPlayer
import android.opengl.EGLContext
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.example.testmediacodec.R
import com.example.testmediacodec.normalsec.CameraView
import com.example.testmediacodec.render.OGRender
import com.example.testmediacodec.render.TriRender
//https://blog.51cto.com/u_16213352/7046194

class DVRActivity : ComponentActivity(), Model.Callback{

    private var mImageView: ImageView? = null

    private var mEGlSurface: MyEGLSurface? = null

    val surfacetesxtid = 1

    private var mCamera :Camera?= null

    private var glSurfaceView:GLSurfaceView?= null

    private var surfaceTexture:SurfaceTexture?= null

    private lateinit  var recordSurfaceRenderHandler :RecordSurfaceRenderHandler

    private var shareContext:EGLContext?= null   //共享的上下文

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dvr)
        mImageView = findViewById(R.id.iv_show)
        glSurfaceView = findViewById(R.id.GLsurfaceView)
        recordSurfaceRenderHandler = RecordSurfaceRenderHandler.createHandler()
        findViewById<Button>(R.id.btnOK).setOnClickListener{
            start_camera()
        }
        findViewById<Button>(R.id.btnstop).setOnClickListener {
            if(shareContext!= null){
                recordSurfaceRenderHandler.setEglContext(shareContext , surfacetesxtid, surfaceTexture,true )
            }
        }

        findViewById<Button>(R.id.stopRecord).setOnClickListener {
           RecordSurfaceRenderHandler.idfff = 1
        }

        surfaceTexture = SurfaceTexture(surfacetesxtid)
//      val render = TestRender(surfaceTexture, glSurfaceView)
//        glSurfaceView?.setRenderer(render)
      //  val render = OGRender(this@DVRActivity,surfaceTexture, glSurfaceView)

val
         surface = Surface(surfaceTexture)

     val mediaplayer = MediaPlayer.create(this, R.raw.video)
        mediaplayer.setSurface(surface
        )
        mediaplayer.isLooping= true
        mediaplayer.start()
        surfaceTexture?.setOnFrameAvailableListener { it->
            Log.d("TAG23","onFragmeAvable 1")


            if(mTransformMatrix== null){
                mTransformMatrix = FloatArray(16)
            }
            it.getTransformMatrix(mTransformMatrix)
            // mEGlSurface?.requestRender()
            Log.d("TAG23","OnFrameAvailable" + mTransformMatrix.joinToString("-"))
            val timestamp = it.timestamp
            if(timestamp == 0L){
                Log.d("TAG24", "timestamp null")
            } else {
                //向另外要给线程发送数据，感谢https://www.jianshu.com/p/702e7b065eb3
                recordSurfaceRenderHandler.sendMessage(
                    recordSurfaceRenderHandler.obtainMessage(
                        RecordSurfaceRenderHandler.MSG_RENDER_DRAW2,
                        (timestamp shr 32) .toInt(), timestamp.toInt(), mTransformMatrix
                    )
                )
            }
            surfaceTexture?.updateTexImage()

        }



        initEGLSurface()
        /**
         * 不要显示图像了
         */
      //  mEGlSurface!!.requestRender()
}

    private fun initEGLSurface() {
        mEGlSurface = MyEGLSurface(this)
      val render: MyRender = MyRender(resources)
     // val render = TriRender(this@DVRActivity)
        render.setCallback(this)
        shareContext =   mEGlSurface!!.init(render)

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
                  //  mEGlSurface?.requestRender()
                  //  surfaceTexture?.updateTexImage()
                }
            })
            surfaceTexture?.setOnFrameAvailableListener {
                    surfaceTexture->
                if(mTransformMatrix== null){
                    mTransformMatrix = FloatArray(16)
                }
                surfaceTexture.getTransformMatrix(mTransformMatrix)
               // mEGlSurface?.requestRender()
                Log.d("TAG23","OnFrameAvailable" + mTransformMatrix.joinToString("-"))
                val timestamp = surfaceTexture.timestamp
                if(timestamp == 0L){
                    Log.d("TAG24", "timestamp null")
                } else {
                    //向另外要给线程发送数据，感谢https://www.jianshu.com/p/702e7b065eb3
                    recordSurfaceRenderHandler.sendMessage(
                        recordSurfaceRenderHandler.obtainMessage(
                            RecordSurfaceRenderHandler.MSG_RENDER_DRAW2,
                            (timestamp shr 32) .toInt(), timestamp.toInt(), mTransformMatrix
                        )
                    )
                }
                //貌似不是这样写的
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