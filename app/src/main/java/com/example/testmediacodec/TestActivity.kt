package com.example.testmediacodec

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

//https://blog.csdn.net/userhu2012/article/details/134413862
class TestActivity : ComponentActivity() {

    var flag = 1
    var total = 3

    lateinit var imageView:ImageView

    var mImages = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_empty)
        Log.d("TAG23","onCreate12")

        initDate()
        imageView = findViewById(R.id.imageView)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        imageView.setImageResource(R.drawable.bg_main)
        imageView.setOnClickListener {
            flag ++
            render()
        }
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        render()
    }

    private fun initDate() {
        mImages.clear()
//        mImages.add(R.drawable.t_1)
//        mImages.add(R.drawable.t_2)
//        mImages.add(R.drawable.t_3)
//        mImages.add(R.drawable.t_4)
//        mImages.add(R.drawable.t_5)
//        mImages.add(R.drawable.t_6)
//        mImages.add(R.drawable.t_7)
//        mImages.add(R.drawable.t_8)
       mImages.add(R.drawable.w_1)
       mImages.add(R.drawable.w_2)
       mImages.add(R.drawable.k_1)
    }

    fun render(){
        var uK = flag % total
        for(  i in 0..total -1){
            Glide.with(this@TestActivity).load(mImages.get(uK)).
            transform(RotateTransformation(this@TestActivity, 90f)).
            into(imageView)
        }
    }


}

  class RotateTransformation  : BitmapTransformation {

    //旋转默认0
    private var rotateRotationAngle = 0f;

      constructor(context: Context, rotateRotationAngle:Float):super(){
          this.rotateRotationAngle = rotateRotationAngle

      }

      override fun updateDiskCacheKey(messageDigest: MessageDigest) {
      }


      override fun transform(
          pool: BitmapPool,
          toTransform: Bitmap,
          outWidth: Int,
          outHeight: Int
      ): Bitmap {

          val matrix = Matrix()
          matrix.postRotate(rotateRotationAngle)
          return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);


      }



}
