package com.example.testmediacodec

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.example.testmediacodec.render.DemoRenderer
import com.example.testmediacodec.ui.theme.TestMediaCodecTheme
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

//https://blog.csdn.net/userhu2012/article/details/134413862
class NoPreviewActivity : ComponentActivity(), SurfaceHolder.Callback {

    lateinit var surfaceTexture: SurfaceTexture

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.main3)
        val mOK =  findViewById<TextView>(R.id.btnOK)
        val mstop = findViewById<Button>(R.id.btnstop)


       val  surfaceView = findViewById(R.id.surfaceView) as GLSurfaceView


//        surfaceView.setRenderer(DemoRenderer())
//        surfaceView.setRenderer()
        surfaceView.holder.addCallback(this)

        surfaceView.holder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        /**
         * 这里很关键，如果是要展示，就用texttureview 的 surfaceview
         */
        surfaceTexture = SurfaceTexture(0)

        surfaceView.visibility = View.VISIBLE

        mOK.setOnClickListener {
            Toast.makeText(this, "dd",Toast.LENGTH_SHORT).show()
            start_camera(surfaceView.holder)
        }
        mstop.setOnClickListener {
            stop_camera()
        }
    }

    private fun stop_camera() {
        mCamera!!.stopPreview()
        mCamera!!.release()
    }

    fun start_camera(holder:SurfaceHolder){

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
//            mCamera?.setPreviewTexture(surfaceTexture)
           mCamera?.setPreviewDisplay(holder)
            mCamera?.setPreviewCallback(object: Camera.PreviewCallback {
                override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
                    Log.d("TAG23","接受到数据了")
                }
            })
            surfaceTexture.setOnFrameAvailableListener {
                    surfaceTexture->Log.e("TAG23","OnFrame")
                 // surfaceTexture.updateTexImage()
            }
            mCamera?.startPreview()
            //camera.takePicture(shutter, raw, jpeg)
        } catch (e: Exception) {
            Log.e("TAG23", "init_camera: $e")
            return
        }

    }
    var mCamera:Camera ?= null

    val   mPreviewCallback :Camera.PreviewCallback = object: Camera.PreviewCallback {
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