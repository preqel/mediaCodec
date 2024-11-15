package com.example.testmediacodec.dvr

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.example.testmediacodec.render.J2Render

/**
 * @author: w.k
 * @date: 2024/11/15
 * @description:
 */
class FilterSurfaceView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

     var render :J2Render?= null

    init {
        // 1.设置 EGL 版本
        setEGLContextClientVersion(2)

        render  = J2Render(this)
        // 2.设置渲染器
        setRenderer( render)
        // 3.设置渲染模式为按需渲染
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun getJ2Render():J2Render?{
        if(render!= null){
            return render;
        } else return null;
    }


}