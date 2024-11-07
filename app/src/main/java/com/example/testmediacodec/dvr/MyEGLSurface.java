package com.example.testmediacodec.dvr;

import android.content.Context;
import android.opengl.EGLContext;

/**
 * Created by QJMOTOR on 2024/11/5.
 */
public class MyEGLSurface extends BaseEGLSurface{


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



}
