package com.example.testmediacodec

import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.hardware.Camera.ShutterCallback
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

//https://blog.csdn.net/userhu2012/article/details/134413862
class VideoServer : ComponentActivity(), SurfaceHolder.Callback{

    var testView: TextView? = null

    var camera: Camera? = null
    var surfaceView: SurfaceView? = null
    var surfaceHolder: SurfaceHolder? = null
    var rawCallback: PictureCallback? = null
    var shutterCallback: ShutterCallback? = null
    var jpegCallback: PictureCallback? = null
    private val tag = "VideoServer"

    var start: Button? = null
   lateinit var stop:android.widget.Button
    lateinit var capture:android.widget.Button

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.testmediacodec.R.layout.main5)

        start = findViewById<View>(R.id.btn_start) as Button
        start!!.setOnClickListener { start_camera() }
        stop = findViewById<View>(R.id.btn_stop) as Button
        capture = findViewById<View>(R.id.capture) as Button
        stop.setOnClickListener(View.OnClickListener { stop_camera() })
        capture.setOnClickListener {
            captureImage()
        }

        surfaceView = findViewById<View>(R.id.surfaceView1) as SurfaceView
        surfaceHolder = surfaceView!!.holder
        surfaceHolder?.addCallback(this)
        surfaceHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        rawCallback =
            PictureCallback { data, camera -> Log.d("Log", "onPictureTaken - raw") }

        /** Handles data for jpeg picture  */
        shutterCallback = ShutterCallback { Log.i("Log", "onShutter'd") }
        jpegCallback = PictureCallback { data, camera ->
            var outStream: FileOutputStream? = null
            try {
                outStream = FileOutputStream(
                    String.format(
                        "/sdcard/%d.jpg", System.currentTimeMillis()
                    )
                )
                outStream.write(data)
                outStream.close()
                Log.d("Log", "onPictureTaken - wrote bytes: " + data.size)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
            }
            Log.d("Log", "onPictureTaken - jpeg")
        }
    }

    private fun captureImage() {
        // TODO Auto-generated method stub
        camera!!.takePicture(shutterCallback, rawCallback, jpegCallback)
    }

    private fun start_camera() {
        try {
            camera = Camera.open()
        } catch (e: RuntimeException) {
            Log.e(tag, "init_camera: $e")
            return
        }
        val param = camera?.getParameters()
        //modify parameter
        param?.previewFrameRate = 20
        param?.setPreviewSize(176, 144)
        camera?.setParameters(param)
        try {
            camera?.setPreviewDisplay(surfaceHolder)
            camera?.setPreviewCallback(object: Camera.PreviewCallback {
                override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
                     Log.d("TAG23","接受到数据了")
                }

            })
            camera?.startPreview()
            //camera.takePicture(shutter, raw, jpeg)
        } catch (e: Exception) {
            Log.e(tag, "init_camera: $e")
            return
        }
    }

    private fun stop_camera() {
        camera!!.stopPreview()
        camera!!.release()
    }



    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }
}