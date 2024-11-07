package com.example.testmediacodec.testpreview;

import static android.opengl.GLES10.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;

import static com.example.testmediacodec.util.LogUtil.TAG;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.testmediacodec.TestPreviewActivity;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *
 *
 * Created by QJMOTOR on 2024/11/7.
 */
public class HRender implements GLSurfaceView.Renderer , SurfaceTexture.OnFrameAvailableListener {
    private int mOESTextureId = -1;

    private SurfaceTexture mSurfaceTexture;
    private GLSurfaceView glSurfaceView;
    private Camera mCamera;
    private Context context;
    Triangle triangle = null;

    public HRender(Context context, Camera camera, SurfaceTexture surfaceTexture, GLSurfaceView glSurfaceView) {
        this.context=context;
        this.mSurfaceTexture = surfaceTexture;
        this.glSurfaceView = glSurfaceView;
        this.mCamera = camera;
    }
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //todo 只运行一次
        // requestPermission();
        //擦除颜色红色
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        mOESTextureId = createOESTextureObject();
        //创建一个渲染图
        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
        //new一个控制GLES渲染的类
        triangle = new Triangle(context);
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //添加帧可用监听，通知GLSurface渲染
        mSurfaceTexture.setOnFrameAvailableListener( this);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //todo 渲染窗口大小发生改变的处理
        Log.e(TAG, "onSurfaceChanged232323232323232 width:" + width + "  height" + height);
        triangle.Change(width, height);
    }


    @Override
    public void onDrawFrame(GL10 gl10) {
        //todo 执行渲染工作
        glClear(GL_COLOR_BUFFER_BIT);
        mSurfaceTexture.updateTexImage();
        triangle.draw();
    }

    public static int createOESTextureObject() {
        int[] tex = new int[1];
        //生成一个纹理
        GLES20.glGenTextures(1, tex, 0);
        //将此纹理绑定到外部纹理上
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        //设置纹理过滤参数
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        //解除纹理绑定
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        glSurfaceView.requestRender();
    }
}
