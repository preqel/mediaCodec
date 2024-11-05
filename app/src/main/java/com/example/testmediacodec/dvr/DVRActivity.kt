package com.example.testmediacodec.dvr

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.example.testmediacodec.R

class DVRActivity : ComponentActivity(), Model.Callback{

    private var mImageView: ImageView? = null
    private var mEGlSurface: MyEGLSurface? = null

    override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_dvr)

        mImageView = findViewById<ImageView>(R.id.iv_show)


    findViewById<Button>(R.id.btnOK).setOnClickListener{

    }

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

}