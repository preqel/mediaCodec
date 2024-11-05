package com.example.testmediacodec.dvr;

import android.content.res.Resources;
import android.opengl.GLES30;

public class MyRender implements BaseEGLSurface.Renderer {
    private Model mModel;

    public MyRender(Resources resources) {
        mModel = new Model(resources);
    }

    @Override
    public void onSurfaceCreated() {
        //设置背景颜色
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //启动深度测试
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        //创建程序id
        mModel.onModelCreate();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mModel.onModelChange(width, height);
    }

    @Override
    public void onDrawFrame() {
        GLES30.glClearColor(0.5f, 0.7f, 0.3f, 1.0f);
        // 将颜色缓存区设置为预设的颜色
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        // 启用顶点的数组句柄
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glEnableVertexAttribArray(1);
        // 绘制模型
        mModel.onModelDraw();
        // 禁止顶点数组句柄
        GLES30.glDisableVertexAttribArray(0);
        GLES30.glDisableVertexAttribArray(1);
    }

    public void setCallback(Model.Callback callback) {
        mModel.setCallback(callback);
    }
}
