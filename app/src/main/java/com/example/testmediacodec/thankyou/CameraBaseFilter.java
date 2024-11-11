package com.example.testmediacodec.thankyou;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import com.example.testmediacodec.R;

/**
 * Created by QJMOTOR on 2024/11/11.
 */
//public class CameraBaseFilter extends BaseFilter {
//
//    private final static String TAG = "CameraBaseFilter";
//    public CameraBaseFilter(Context context) {
//        super(GLesUtils.readTextFileFromResource(context, R.raw.base_fliter_normal_vertex),
//                GLesUtils.readTextFileFromResource(context, R.raw.base_filter_nomal_oes_fragement));
//    }
//
//    private int textureTransformLocation;//mvp矩阵在glsl中的 Uniform 句柄值
//    protected void onInit() {
//        super.onInit();
//        textureTransformLocation = GLES30.glGetUniformLocation(getProgramId(), "textureTransform");
//        updateVertexArray();
//    }
//
//    private void updateVertexArray(){
//        mVertexBuffer = ByteBuffer.allocateDirect(TextureRotateUtil.VERTEX.length * 4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer();
//        mVertexBuffer.put(TextureRotateUtil.VERTEX).position(0);
//
//        mTextureBuffer = ByteBuffer.allocateDirect(TextureRotateUtil.TEXTURE_ROTATE_90.length * 4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer();
//        mTextureBuffer.put(TextureRotateUtil.getRotateTexture(Rotation.ROTATION_90, false, true))
//                .position(0);
//    }
//
//    @Override
//    public int onDrawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
//        if (!hasInitialized()) {
//            return GLesUtils.NOT_INIT;
//        }
//// Log.d(TAG,"getProgramId() :" +getProgramId());
//        GLES30.glUseProgram(getProgramId());
//        runPendingOnDrawTask();
//        if(count == 0){
//            Log.d(TAG,"onDrawFrame getProgramId() :" +getProgramId());
//            Log.d(TAG,"onDrawFrame textureTransformLocation() :" +
//                    Arrays.toString(textureTransformMatrix));
//            Log.d(TAG,"onDrawFrame mInputWidth :" +
//                    mInputWidth + ",mInputHeight:" + mInputHeight);
//
//        }
//        count++;
////启用顶点坐标
//        vertexBuffer.position(0);
//        GLES30.glVertexAttribPointer(mAttributePosition,
//                2, GLES30.GL_FLOAT, false, 0, vertexBuffer);
//        GLES30.glEnableVertexAttribArray(mAttributePosition);
//
////启用纹理坐标
//        textureBuffer.position(0);
//        GLES30.glVertexAttribPointer(mAttributeTextureCoordinate,
//                2, GLES30.GL_FLOAT, false, 0, textureBuffer);
//        GLES30.glEnableVertexAttribArray(mAttributeTextureCoordinate);
//
////设置mvp矩阵
//        GLES30.glUniformMatrix4fv(textureTransformLocation,
//                1, false, textureTransformMatrix, 0);
//
////启用纹理，此处纹理即为相机启动之后设置给相机预览创建的texture
//        if (textureId != GLesUtils.NO_TEXTURE) {
//            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
//            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
//            GLES30.glUniform1i(mUniformTexture, 0);
//        }
//
////启动绘制，请绘制完成之后清除绘制参数，顶点着色器，片元着色器 和纹理
//        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
//        GLES30.glDisableVertexAttribArray(mAttributePosition);
//        GLES30.glDisableVertexAttribArray(mAttributeTextureCoordinate);
//        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
//
//        return GLesUtils.ON_DRAWN;
//    }
//
//    //绘制到fbo上，且fbo控制在不在本方法之内
//    public int onDrawToFramebuffer(final int textureId){
//
//        GLES30.glUseProgram(getProgramId());
//
//        mVertexBuffer.position(0);
//        GLES30.glVertexAttribPointer(mAttributePosition, 2, GLES30.GL_FLOAT, false, 0, mVertexBuffer);
//        GLES30.glEnableVertexAttribArray(mAttributePosition);
//        mTextureBuffer.position(0);
//        GLES30.glVertexAttribPointer(mAttributeTextureCoordinate, 2, GLES30.GL_FLOAT, false, 0, mTextureBuffer);
//        GLES30.glEnableVertexAttribArray(mAttributeTextureCoordinate);
//        GLES30.glUniformMatrix4fv(textureTransformLocation, 1, false, textureTransformMatrix, 0);
//
//        if (textureId != GLesUtils.NO_TEXTURE) {
//            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
//            GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
//            GLES30.glUniform1i(mUniformTexture, 0);
//        }
//
//        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
//
//        GLES30.glDisableVertexAttribArray(mAttributePosition);
//        GLES30.glDisableVertexAttribArray(mAttributeTextureCoordinate);
//        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
//        return frameBufferTexture[0];
//    }
//
//    public void initFrameBuffer(int width, int height){
////初始化的参数，先根据默认参数进行清除数据
//        if (frameBuffer != null && (frameWidth != width || frameHeight != height))
//            destroyFrameBuffer();
//
////初始化FBO
//        if (frameBuffer == null) {
////传入参数是预览的宽和高
//            frameWidth = width;
//            frameHeight = height;
//
//            frameBuffer = new int[1];
//            frameBufferTexture = new int[1];
////生成FBO
//            GLES30.glGenFramebuffers(1, frameBuffer, 0);
//
////生成FBO附着的纹理
//            GLES30.glGenTextures(1, frameBufferTexture, 0);
//            Log.i(TAG,"initFrameBuffer:" +frameBufferTexture[0] );
//            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, frameBufferTexture[0]);
//            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
//            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
//            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
//            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
//
////分配FBO的缓存大小
//            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height,
//                    0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
////绑定FBO
//            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0]);
////将FBO对应附着的纹理和FBO绑定起来
//            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
//                    GLES30.GL_TEXTURE_2D, frameBufferTexture[0], 0);
//
//            if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)!= GLES30.GL_FRAMEBUFFER_COMPLETE) {
//                Log.e(TAG,"glCheckFramebufferStatus not GL_FRAMEBUFFER_COMPLETE");
//                return ;
//            }else {
//                Log.i(TAG,"glCheckFramebufferStatus GL_FRAMEBUFFER_COMPLETE");
//            }
//            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
//            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
//
//        }
//
//}
