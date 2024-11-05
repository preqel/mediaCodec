package com.example.testmediacodec.dvr;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.opengl.GLES30;


import com.example.testmediacodec.R;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Model {
    private static final int TEXTURE_DIMENSION = 2; // 纹理坐标维度
    private static final int VERTEX_DIMENSION = 3; // 顶点坐标维度
    private Callback mCallback;
    private Resources mResources;
    private float mVertex[] = {-1.0f, 1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, -1.0f, 0.0f};
    private float[] mFboTexture = {0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f};
    protected FloatBuffer mVertexBuffer;
    protected FloatBuffer mFboTextureBuffer;
    // 帧缓冲对象 - 颜色、深度、模板附着点，纹理对象可以连接到帧缓冲区对象的颜色附着点
    private int[] mFrameBufferId = new int[1];
    private int[] mTextureId = new int[2];
    private int mProgramId;
    private Point mBitmapSize = new Point();

    public Model(Resources resources) {
        mResources = resources;
        mVertexBuffer = ArraysUtils.getFloatBuffer(mVertex);
        mFboTextureBuffer = ArraysUtils.getFloatBuffer(mFboTexture);
    }

    // 模型创建
    public void onModelCreate() {
        mProgramId = ShaderUtils.createProgram(mResources, R.raw.vertex_shader, R.raw.fragment_shader);
        TextureUtils.loadTexture(mResources, R.raw.xxx, mBitmapSize, mTextureId, mFrameBufferId);
    }

    // 模型参数变化
    public void onModelChange(int width, int height) {
        GLES30.glViewport(0, 0, mBitmapSize.x, mBitmapSize.y);
    }

    // 模型绘制
    public void onModelDraw() {
        GLES30.glUseProgram(mProgramId);
        // 准备顶点坐标和纹理坐标
        GLES30.glVertexAttribPointer(0, VERTEX_DIMENSION, GLES30.GL_FLOAT, false, 0, mVertexBuffer);
        GLES30.glVertexAttribPointer(1, TEXTURE_DIMENSION, GLES30.GL_FLOAT, false, 0, mFboTextureBuffer);
        // 激活纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE);
        // 绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureId[0]);
        // 绑定缓存
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBufferId[0]);
        // 绘制贴图
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        showBitmap();
    }

    private void showBitmap() {
        // 分配字节缓区大小， 一个像素4个字节
        ByteBuffer byteBuffer = ByteBuffer.allocate(mBitmapSize.x * mBitmapSize.y * Integer.BYTES);
        GLES30.glReadPixels(0, 0, mBitmapSize.x, mBitmapSize.y, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, byteBuffer);
        Bitmap bitmap = Bitmap.createBitmap(mBitmapSize.x, mBitmapSize.y, Bitmap.Config.ARGB_8888);
        // 从缓存区读二进制缓冲数据
        bitmap.copyPixelsFromBuffer(byteBuffer);
        // 回调
        mCallback.onCall(bitmap);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback{
        void onCall(Bitmap bitmap);
    }
}
