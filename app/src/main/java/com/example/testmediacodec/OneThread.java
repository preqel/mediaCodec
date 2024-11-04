package com.example.testmediacodec;

import android.content.Context;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;
import android.util.Log;

import com.example.testmediacodec.util.EGLHelper;
import com.example.testmediacodec.util.LogUtil;
import com.example.testmediacodec.util.StorageUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by wk on 2024/11/4.
 */
public class OneThread extends Thread{


    private EGLContext context;
    private int width;
    private int height;

    public OneThread(EGLContext context, int width ,int height){
        this.context = context;
        this.width = width;
        this.height = height;
    }

    @Override
    public void run() {
        try {
            EGLHelper mEglHelper = new EGLHelper();
            mEglHelper.createGL(context);
            String  mVideoPath = StorageUtil.getVedioPath(true) + "glvideo.mp4";
            Log.d("TAG23", "mVideoPath _____________>"+ mVideoPath);
            VideoEncoder mVideoEncoder = new VideoEncoder(width, height, new File(mVideoPath));
            EGLSurface mEglSurface = mEglHelper.createWindowSurface(mVideoEncoder.getInputSurface());
            boolean error = mEglHelper.makeCurrent(mEglSurface);
            if (!error) {
                Log.e(LogUtil.TAG, "prepareVideoEncoder: make current error");
            }
            //onCreated();
        } catch (IOException e) {
            e.printStackTrace();
        }


        super.run();
    }
}
