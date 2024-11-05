package com.example.testmediacodec.dvr;

import android.content.Context;

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

    public void init(Renderer renderer) {
        setRenderer(renderer);
        createEGLEnv();
    }

}
