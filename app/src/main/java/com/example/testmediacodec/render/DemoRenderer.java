package com.example.testmediacodec.render;

import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by QJMOTOR on 2024/11/7.
 */
public class DemoRenderer implements GLSurfaceView.Renderer {
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // 设置个红色背景
      //  GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
    }

    public void onDrawFrame(GL10 unused) {
        // Redraw background color 重绘背景
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {

    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // 设置绘图的窗口(可以理解成在画布上划出一块区域来画图)
     //   GLES20.glViewport(100,100,width,height);
    }
}
