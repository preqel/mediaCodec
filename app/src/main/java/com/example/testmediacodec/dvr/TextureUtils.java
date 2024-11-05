package com.example.testmediacodec.dvr;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES30;
import android.opengl.GLUtils;

public class TextureUtils {
    // 加载纹理贴图
    public static void loadTexture(Resources resources, int resourceId, Point bitmapSize, int[] textureId, int[] frameBufferId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId, options);
        bitmapSize.set(bitmap.getWidth(), bitmap.getHeight());
        // 生成纹理id
        GLES30.glGenTextures(2, textureId, 0);
        for (int i = 0; i < 2; i++) {
            // 绑定纹理到OpenGL
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId[i]);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            if (i == 0) {
                // 第一个纹理对象给渲染管线(加载bitmap到纹理中)
                GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap, 0);
            } else {
                // 第二个纹理对象给帧缓冲区
                GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(),
                        0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
            }
            // 取消绑定纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_NONE);
        }
        // 创建帧缓存id
        GLES30.glGenFramebuffers(1, frameBufferId, 0);
        // 绑定帧缓存
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferId[0]);
        // 将第二个纹理附着在帧缓存的颜色附着点上
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, textureId[1], 0);
        // 取消绑定帧缓存
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_NONE);
    }
}
