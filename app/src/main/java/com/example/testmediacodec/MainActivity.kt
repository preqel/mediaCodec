package com.example.testmediacodec

import android.media.MediaRecorder
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.testmediacodec.ui.theme.TestMediaCodecTheme

class MainActivity : ComponentActivity() {

    val lifecyclerCamerac : LifecycleCameraController;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.main)

        val mstart = findViewById<TextView>(R.id.textView);

        mstart.setOnClickListener {

            val r =   MediaRecorder()

            r.start()

            r.stop()


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