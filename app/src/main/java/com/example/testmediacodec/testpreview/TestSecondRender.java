package com.example.testmediacodec.testpreview;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by QJMOTOR on 2024/11/1.
 */
public class TestSecondRender implements GLSurfaceView.Renderer , SurfaceTexture.OnFrameAvailableListener{

    private SurfaceTexture surfaceTexture;

    private int textureId;

    private GLSurfaceView glSurfaceView ;
    private Camera camera;

    public TestSecondRender(SurfaceTexture surfaceTexture, GLSurfaceView glSurfaceView, android.hardware.Camera camera){
        Log.d("TAG23", "TestRender init");
        this.surfaceTexture = surfaceTexture;
        this.glSurfaceView = glSurfaceView;
        this.camera = camera;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d("TAG23", "TestRender onSurfaceCreated");
        // 生成纹理
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];
        Log.d("TAG23", "TestRender onSurfaceCreated" +textureId);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        // 设置纹理参数
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);


        surfaceTexture.setOnFrameAvailableListener( this);

      //  GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        // 关联SurfaceTexture和纹理
//        surfaceTexture.attachToGLContext(999);
//        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
//            @Override
//            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//                // 请求下一帧数据
//                GLES20.glFlush();
//            }
//        });
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 更新SurfaceTexture的图像内容
        Log.d("TAG23", "onDrawFrame");
        surfaceTexture.updateTexImage();


        // 绘制图像，这里需要使用shader等进行绘制
        // ...
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d("TAG23", "onSurfaceChanged-" + width+ height);

        // 视图尺寸变化时的处理
        glSurfaceView.requestRender();

        try {
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        camera.
        //    GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.d("TAG23", "onFramgeAvable");
        glSurfaceView.requestRender();
    }
}
