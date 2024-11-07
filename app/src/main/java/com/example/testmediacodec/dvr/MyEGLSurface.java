package com.example.testmediacodec.dvr;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.view.Surface;
import android.view.SurfaceView;

/**
 * Created by QJMOTOR on 2024/11/5.
 */
public class MyEGLSurface extends BaseEGLSurface{


    private SurfaceTexture surfaceTexture;

    public MyEGLSurface(Context context) {
        super(context);
    }

    public MyEGLSurface(Context context, int width, int height) {
        super(context, width, height);
    }

    public EGLContext init(Renderer renderer) {
        setRenderer(renderer);
        return  createEGLEnv();
    }

    int textureId = 0;
    public void  initOther(){
        // 设置渲染环境的参数
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        // 生成纹理
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
         textureId = textures[0];
        // 绑定纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        // 设置纹理参数
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // 实例化SurfaceTexture，并传入我们的纹理ID
        surfaceTexture = new SurfaceTexture(textureId);

        // 设置SurfaceTexture的监听器来获取新的纹理坐标
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                // 当新的帧可用时，我们会在这里更新我们的渲染循环


            }
        });

        // 创建一个Surface来使用SurfaceTexture
        Surface surface = new Surface(surfaceTexture);


        return surface;

    }



}
