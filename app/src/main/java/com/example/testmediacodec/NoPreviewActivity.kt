package com.example.testmediacodec

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
import com.example.testmediacodec.ui.theme.TestMediaCodecTheme
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

//https://blog.csdn.net/userhu2012/article/details/134413862
class NoPreviewActivity : ComponentActivity() {


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.main3)
        val mBack =  findViewById<TextView>(R.id.tvBackRecord)
        mBack.setOnClickListener {
            startBackGround()
        }
    }

    var mCamera:Camera ?= null
    private fun startBackGround() {


//        var mCameraID: Int = Camera.CameraInfo.CAMERA_FACING_FRONT
        //new SurfaceTexture
        val  surfaceTexture:SurfaceTexture = SurfaceTexture(0);
        //打开摄像头
           mCamera = Camera.open();
        //设置camera参数
        val  params: Camera.Parameters = mCamera!!.getParameters();
      //  params.setZoom(0);
      //  params.setPreviewFormat(ImageFormat.NV21);
      //  params.setPreviewSize(surfaceTexture, 1024);
     //   params.setPictureSize(600, 1024);
        mCamera?.setParameters(params)
        //设置回调用于获取摄像头数据
        mCamera?.setPreviewCallback(mPreviewCallback );
        try {
            //这一步是最关键的，使用surfaceTexture来承载相机的预览，而不需要设置一个可见的view
            mCamera?.setPreviewTexture(surfaceTexture);
            mCamera?.startPreview();
        } catch ( e: IOException) {
            e.printStackTrace();
        }
    }


    val   mPreviewCallback :Camera.PreviewCallback = object: Camera.PreviewCallback {
        override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
            Log.d("TAG23", "产生了数据" + data?.size)
        }

    };
}