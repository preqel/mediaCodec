package com.example.testmediacodec

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.MediaPlayer
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.testmediacodec.render.TestRender
import com.example.testmediacodec.util.EGLHelper
import com.example.testmediacodec.widget.CameraGLSurfaceView
import java.io.IOException

//https://blog.csdn.net/userhu2012/article/details/134413862
//https://blog.51cto.com/u_16213355/8289594
class HasPreviewActivity : ComponentActivity(), SurfaceHolder.Callback{

   lateinit var surfaceView:SurfaceView
   lateinit var surfaceViewLose:TextureView

   lateinit var glsurfaceview: GLSurfaceView
   lateinit var complexView : CameraGLSurfaceView

   companion object {
       lateinit var surfaceTexture: SurfaceTexture
   }

//    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//      enableEdgeToEdge()
        setContentView(R.layout.main4)
        surfaceView = findViewById(R.id.surfaceView) as SurfaceView
        val surface:Surface =  surfaceView.holder.surface

        surfaceView.holder.addCallback(this)
        surfaceView.holder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
       // surfaceView.visibility = View.VISIBLE

        surfaceViewLose = findViewById(R.id.surfaceViewLose)
        surfaceViewLose.surfaceTextureListener = object: TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
          //    surfaceTexture   = surfaceViewLose.surfaceTexture!!
          //    surfaceTexture = surface
          //    https://blog.51cto.com/u_16213352/7046194
            try{
                val mediaPlayer = MediaPlayer.create(this@HasPreviewActivity, R.raw.video)
                mediaPlayer.isLooping = true
                mediaPlayer.prepare()
                mediaPlayer.setDisplay(surfaceView.holder)
                mediaPlayer.start()

            } catch (e:Exception){
                Log.e("TAG23", e.message.toString())
                e.printStackTrace()
            }

        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false;
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        }
    }

    /**
     * 这里很关键，如果是要展示，就用texttureview 的surface
     */
     surfaceTexture = SurfaceTexture(1)
     // val glsurfaceview = GLSurfaceView(this@HasPreviewActivity)
     glsurfaceview = findViewById<GLSurfaceView>(R.id.glsurfaceview)
//
     val render = TestRender(surfaceTexture, glsurfaceview)
    glsurfaceview.setRenderer(render)

//    glsurfaceview.setEGLContextClientVersion(3);
//    glsurfaceview.setRenderer(  com.example.testmediacodec.glsurfaceview2.CameraSurfaceRender(glsurfaceview));
//    glsurfaceview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);




   //  val content = findViewById<FrameLayout>(R.id.fl_content)
    // content.addView(glsurfaceview)


//        surfaceView.holder.addCallback(this)
//        surfaceView.holder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        val mOk = findViewById<Button>(R.id.btnOK)
        mOk.setOnClickListener {
      //     startBackGround()
             start_camera()
        }
    val mstop = findViewById<Button>(R.id.btnstop)

    complexView = findViewById<CameraGLSurfaceView>(R.id.cl_content)
    val surface2:Surface = Surface(surfaceTexture)


    //正式开始编程

    //1创建一个offscreenEgL
    val mEglHelper = EGLHelper()

    mEglHelper.createGL()


    complexView.setCallback(object: CameraGLSurfaceView.CameraGLSurfaceViewCallback{
        override fun onSurfaceViewCreate(texture: SurfaceTexture?) {
            Log.d("TAG23", "onSurfaceviewcreate")
        }

        override fun onSurfaceViewChange(width: Int, height: Int) {
            Log.d("TAG23", "onSurfaceViewChange")
        }

    })
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

            //如果不要图形
            mCamera?.setPreviewTexture(surfaceTexture)
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
            surfaceTexture.setOnFrameAvailableListener {
                surfaceTexture->Log.d("TAG23","OnFrameAvailable")
             //   surfaceTexture.updateTexImage()
            }
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
        val  surfaceTexture:SurfaceTexture = SurfaceTexture(0);
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
            mCamera.setPreviewTexture(surfaceTexture);
          //  mCamera.setPreviewDisplay(surfaceView.holder);
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

    }

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun onResume() {
        glsurfaceview.onResume()
        super.onResume()
    }

    override fun onPause() {
        glsurfaceview.onPause()
        super.onPause()
    }
}