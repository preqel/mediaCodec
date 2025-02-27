package com.example.testmediacodec

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.example.testmediacodec.ui.theme.TestMediaCodecTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

//https://blog.csdn.net/userhu2012/article/details/134413862
class MainActivity : ComponentActivity() {

        companion object {

            val CAMERAX_PERMISSIONS = arrayOf(
                android.Manifest.permission.CAMERA,//请求相机
                android.Manifest.permission.RECORD_AUDIO)//请求录制音频
        }

    lateinit var lifecycleCameraController : LifecycleCameraController

    private var recording: Recording? = null

    private lateinit var outputDirectory:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.main)

        lifecycleCameraController = LifecycleCameraController(applicationContext).apply {
            setEnabledUseCases(CameraController.VIDEO_CAPTURE)
        }

        lifecycleCameraController.bindToLifecycle(this)

        lifecycleCameraController.imageCaptureFlashMode = ImageCapture.FLASH_MODE_AUTO



        handlerPermission()

        initFile()

        val mStart = findViewById<TextView>(R.id.textView);
        val mStop = findViewById<TextView>(R.id.textView2);
        val mSwitch = findViewById<TextView>(R.id.textViewSwitch)
        val mPic = findViewById<TextView>(R.id.textViewPic)
        val mPreview = findViewById<PreviewView>(R.id.previewView)

        mPreview.controller = lifecycleCameraController;


        val name = SimpleDateFormat("yyyy-MM-dd hh:mm:ss",
            Locale.CHINA).format(System.currentTimeMillis())
        //定义关于一条视频记录的相关配置
        val contentValue = ContentValues().apply{
            put(MediaStore.MediaColumns.DISPLAY_NAME,name)
            put(MediaStore.MediaColumns.MIME_TYPE,"video/mp4")
            if(Build.VERSION.SDK_INT>Build.VERSION_CODES.P){
                put(MediaStore.Video.Media.RELATIVE_PATH,"Movies/CameraX-Video")
            }
        }


        val mediaOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            ).setContentValues(contentValue).build();

        if(CameraHelper.hasCamera(this)){
            Log.d("TAG", "有摄像头")
        } else {
            Log.d("TAG", "没有摄像头")
        }

        mSwitch.setOnClickListener {
            if (lifecycleCameraController.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                lifecycleCameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                lifecycleCameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            }
        }

        mStart.setOnClickListener {

            if(recording != null){
                recording?.stop();
                recording = null;
                return@setOnClickListener
            }

            if(hasRequiredPermissions() == false){
                return@setOnClickListener
            }

            val outputFile = File(filesDir,"video.mp4")
           //开始录制

            Toast.makeText(this, "开始录制",Toast.LENGTH_SHORT).show();

            lifecycleCameraController.let {
                  //it.startRecording();
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@let
                }

             //   recording = it.startRecording(FileOutputOptions.Builder(outputFile).build(),
                recording = it.startRecording(getMediaStoreOutputOptions(),
                    AudioConfig.create(true),
                    ContextCompat.getMainExecutor(applicationContext), object :Consumer<VideoRecordEvent>{
                    override fun accept(event: VideoRecordEvent) {
                        Log.d("TAG23","on record preqel")

                        when(event){
                            is VideoRecordEvent.Finalize->{
                                if(event.hasError()){
                                    recording?.close();
                                    recording = null;
                                    Toast.makeText(this@MainActivity, "视频录制失败"+event.error,Toast.LENGTH_LONG).show();
                                }else {
                                    Toast.makeText(this@MainActivity, "视频录制成功",Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                })
            }
        }

        mStop.setOnClickListener {
            if(mStop!= null){
                recording?.stop()
                Toast.makeText(this, "录制结束",Toast.LENGTH_SHORT).show()
            }
        }

        mPic.setOnClickListener {
            val photoFile = createPhontFile()
        }
    }

    private fun initFile() {
        outputDirectory  = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath  + "/CameraControllerTest/"+".png"
                var file = File(outputDirectory)
        if(!file.exists()){
            file.mkdirs()
        }
    }

    private fun createPhontFile(): File {

        return File(outputDirectory, System.currentTimeMillis().toString()+".jpg");
    }

    private fun getMediaStoreOutputOptions():MediaStoreOutputOptions {
        val name = SimpleDateFormat("yyyy-MM-dd hh:mm:ss",
            Locale.CHINA).format(System.currentTimeMillis())
        //定义关于一条视频记录的相关配置
        val contentValue = ContentValues().apply{
            put(MediaStore.MediaColumns.DISPLAY_NAME,name)
            put(MediaStore.MediaColumns.MIME_TYPE,"video/mp4")
            if(Build.VERSION.SDK_INT>Build.VERSION_CODES.P){
                put(MediaStore.Video.Media.RELATIVE_PATH,"Movies/CameraX-Video")
            }
        }
        //配置输出到媒体库的输出参数
        val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
            contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValue).build()
        return mediaStoreOutputOptions;
    }


    private fun hasRequiredPermissions():Boolean = CAMERAX_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(
            applicationContext,
            it) == PackageManager.PERMISSION_GRANTED
    }


    private fun handlerPermission() {
         if(!hasRequiredPermissions()){
             ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS, 0);

         }
    }

}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestMediaCodecTheme {
        Greeting("Android")
    }
}