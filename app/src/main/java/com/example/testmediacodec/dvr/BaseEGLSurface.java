package com.example.testmediacodec.dvr;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.example.testmediacodec.util.EGLHelper;

public class BaseEGLSurface {
    protected EGLDisplay mEGLDisplay;
    protected EGLConfig mEGLConfig;
    protected EGLContext mEGLContext;
    protected EGLSurface mEGLSurface;
    protected Context mContext;
    protected Renderer mRenderer;
    protected EglStatus mEglStatus = EglStatus.INVALID;
    protected int mWidth;
    protected int mHeight;

    public BaseEGLSurface(Context context) {
        mContext = context;
        WindowManager mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        mWidth = displayMetrics.widthPixels;
        mHeight = displayMetrics.heightPixels;
    }

    public BaseEGLSurface(Context context, int width, int height) {
        mContext = context;
        mWidth = width;
        mHeight = height;
    }

    // 设置渲染器
    public void setRenderer(Renderer renderer) {
        mRenderer = renderer;
    }

    // EGLDisplay宽高发生变化
    public void onSurfaceChanged(int width, int height) {
        mWidth = width;
        mHeight = height;
        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
        createSurface();
        mEglStatus = EglStatus.CHANGED;
    }

    // 请求渲染
    public void requestRender() {
        if (!mEglStatus.isValid()) {
            return;
        }
        if (mEglStatus == EglStatus.INITIALIZED) {
            mRenderer.onSurfaceCreated();
            mEglStatus = EglStatus.CREATED;
        }
        if (mEglStatus == EglStatus.CREATED) {
            mRenderer.onSurfaceChanged(mWidth, mHeight);
            mEglStatus = EglStatus.CHANGED;
        }
        if (mEglStatus == EglStatus.CHANGED || mEglStatus == EglStatus.DRAW) {
            mRenderer.onDrawFrame();
            mEglStatus = EglStatus.DRAW;
        }
    }

    // 创建EGL环境
    //这里本来是void，改成返回eglcontext
    public EGLContext createEGLEnv() {
        createDisplay();
        createConfig();
        createContext();
        createSurface();
        makeCurrent();

        return mEGLContext;

       // SurfaceTexture surfaceTexture = new SurfaceTexture(1);

        //opengl初始化，
        // 纹理初始化，
        // surfacetexture初始化4

        //openGl 初始化

        //对应surface

        //不会啊！！！！！！！
//        surfaceTexture.attachToGLContext();
    }

    // 销毁EGL环境
    public void destroyEGLEnv() {
        // 与显示设备解绑
        EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        // 销毁 EGLSurface
        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
        // 销毁EGLContext
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
        // 销毁EGLDisplay(显示设备)
        EGL14.eglTerminate(mEGLDisplay);
        mEGLContext = null;
        mEGLSurface = null;
        mEGLDisplay = null;
    }

    // 1.创建EGLDisplay
    private void createDisplay() {
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        int[] versions = new int[2];
        EGL14.eglInitialize(mEGLDisplay, versions,0, versions, 1);
    }

    // 2.创建EGLConfig
    private void createConfig() {
        EGLConfig[] configs = new EGLConfig[1];
        int[] configNum = new int[1];
        EGL14.eglChooseConfig(mEGLDisplay, mEGLConfigAttrs, 0, configs, 0,1,  configNum, 0);
        if (configNum[0] > 0) {
            mEGLConfig = configs[0];
        }
    }

    // 3.创建EGLContext
    private void createContext() {
        if (mEGLConfig != null) {
            mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mEGLConfig, EGL14.EGL_NO_CONTEXT, mEGLContextAttrs, 0);
        }
    }

    // 4.创建EGLSurface
    private void createSurface() {
        if (mEGLContext != null && mEGLContext != EGL14.EGL_NO_CONTEXT) {
            int[] eglSurfaceAttrs = {EGL14.EGL_WIDTH, mWidth, EGL14.EGL_HEIGHT, mHeight, EGL14.EGL_NONE};
            mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, eglSurfaceAttrs, 0);
        }
    }

    // 5.绑定EGLSurface和EGLContext到显示设备（EGLDisplay）
    private void makeCurrent() {
        if (mEGLSurface != null && mEGLSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
            mEglStatus = EglStatus.INITIALIZED;
        }
    }

    // EGLConfig参数
    private int[] mEGLConfigAttrs = {
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_DEPTH_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_NONE
    };

    // EGLContext参数
    private int[] mEGLContextAttrs = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};

    // EGL状态
    enum EglStatus {
        INVALID, INITIALIZED, CREATED, CHANGED, DRAW;
        public boolean isValid() {
            return this != INVALID;
        }
    }

    // 渲染器接口
    interface Renderer {
        void onSurfaceCreated();
        void onSurfaceChanged(int width, int height);
        void onDrawFrame();
    }
}
