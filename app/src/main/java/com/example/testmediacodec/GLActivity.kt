package com.example.testmediacodec

import android.os.Bundle
import androidx.activity.ComponentActivity

class GLActivity : ComponentActivity() {


        lateinit   var mCameraFragment: GLFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main2)

        mCameraFragment = GLFragment()

        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.main_container, mCameraFragment)
        transaction.commit()
    }
}