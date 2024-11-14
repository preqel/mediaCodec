package com.example.testmediacodec.render;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by QJMOTOR on 2024/11/14.
 */
public class J2Render implements SurfaceTexture.OnFrameAvailableListener, GLSurfaceView.Renderer {

    private SurfaceTexture surfaceTexture;

    private int textureId;

    private GLSurfaceView glSurfaceView ;

    private  float[] mMatrix = new float[16];

    public J2Render(GLSurfaceView glSurfaceView){
this.glSurfaceView= glSurfaceView;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        glSurfaceView.requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];

        // 生成纹理 ID，参数依次为纹理 ID 数组长度、纹理 ID 数组、数组偏移量

        // 2.2 创建 SurfaceTexture
        surfaceTexture =new  SurfaceTexture(textureId);

        // 2.3 为 SurfaceTexture 设置数据监听，当有视频帧可用时会回调 onFrameAvailable()
        surfaceTexture.setOnFrameAvailableListener(this);

        // 3.创建 ScreenFilter 以进行图像绘制
      //  mScreenFilter = ScreenFilter(mGLSurfaceView.context)

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 开启摄像头预览
      //  mCameraHelper.startPreview(mSurfaceTexture)

        // 设置 OpenGL 的绘制视窗
       // mScreenFilter.onReady(width, height)
    }

    // RGB color values for generated frames
    private static final int TEST_R0 = 0;
    private static final int TEST_G0 = 136;
    private static final int TEST_B0 = 0;
    private static final int TEST_R1 = 236;
    private static final int TEST_G1 = 50;
    private static final int TEST_B1 = 186;
    int mWidth = 400;
    int mHeight = 400;
    private void generateSurfaceFrame(int frameIndex,float[] tex_matrix ) {
        frameIndex %= 8;

        int startX, startY;
        if (frameIndex < 4) {
            // (0,0) is bottom-left in GL
            startX = frameIndex * (mWidth / 4);
            startY = mHeight / 2;
        } else {
            startX = (7 - frameIndex) * (mWidth / 4);
            startY = 0;
        }

        GLES20.glClearColor(TEST_R0 / 255.0f, TEST_G0 / 255.0f, TEST_B0 / 255.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(startX, startY, mWidth / 4, mHeight / 2);
        GLES20.glClearColor(TEST_R1 / 255.0f, TEST_G1 / 255.0f, TEST_B1 / 255.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);


//
//            if (tex_matrix != null)
//                GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, tex_matrix, 0);
//            GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
//            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex_id);
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_NUM);
//            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 1.清空屏幕为黑色
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        // 设置清理哪一个缓冲区
        // GL_COLOR_BUFFER_BIT 颜色缓冲区
        // GL_DEPTH_BUFFER_BIT 深度缓冲区
        // GL_STENCIL_BUFFER_BIT 模型缓冲区
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 2.更新纹理
        // 2.1 更新离屏渲染的 SurfaceTexture 的数据，即获取新的帧
        surfaceTexture.updateTexImage();
        // 2.2 获取到新的帧的变换矩阵
        surfaceTexture.getTransformMatrix(mMatrix);

        StringBuilder sb = new StringBuilder();


      //  generateSurfaceFrame(0, mMatrix);

        for(int i = 0;i<mMatrix.length;i++){
            sb.append(mMatrix[i]);
        }

        Log.d("TAG25", "matrix"+ sb);

        // 3.交给滤镜进行具体的绘制工作
      //  mScreenFilter.onDrawFrame(mTextureIds[0], mMatrix);


    }
}
