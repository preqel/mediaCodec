package com.example.testmediacodec.dvr;

import static com.example.testmediacodec.util.LogUtil.TAG;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * w.k
 * EGL环境构建器（重要）
 */
public class EGLBase {


    protected EGLDisplay mEGLDisplay;
    protected EGLConfig mEGLConfig;
    protected EGLContext mEGLContext;
    public EGLSurface mEGLSurface;
    protected Context mContext;
    protected BaseEGLSurface.Renderer mRenderer;
    protected BaseEGLSurface.EglStatus mEglStatus = BaseEGLSurface.EglStatus.INVALID;
    protected int mWidth;
    protected int mHeight;

    public EGLBase(Context context) {
        mContext = context;
        WindowManager mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        mWidth = displayMetrics.widthPixels;
        mHeight = displayMetrics.heightPixels;
    }

    public EGLBase(EGLContext context, int width, int height) {
        this.mEGLContext = context;
        this.mWidth = width;
        this.mHeight = height;
    }

    public EGLBase(EGLContext context,boolean flag , boolean isRecordable ){
        this.mEGLContext =context;
    }

    // 设置渲染器
    public void setRenderer(BaseEGLSurface.Renderer renderer) {
        mRenderer = renderer;
    }

    // EGLDisplay宽高发生变化
    public void onSurfaceChanged(int width, int height) {
        mWidth = width;
        mHeight = height;
        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
        createSurface();
        mEglStatus = BaseEGLSurface.EglStatus.CHANGED;
    }

    // 请求渲染
    public void requestRender() {
        if (!mEglStatus.isValid()) {
            return;
        }
        if (mEglStatus == BaseEGLSurface.EglStatus.INITIALIZED) {
            mRenderer.onSurfaceCreated();
            mEglStatus = BaseEGLSurface.EglStatus.CREATED;
        }
        if (mEglStatus == BaseEGLSurface.EglStatus.CREATED) {
            mRenderer.onSurfaceChanged(mWidth, mHeight);
            mEglStatus = BaseEGLSurface.EglStatus.CHANGED;
        }
        if (mEglStatus == BaseEGLSurface.EglStatus.CHANGED || mEglStatus == BaseEGLSurface.EglStatus.DRAW) {
            mRenderer.onDrawFrame();
            mEglStatus = BaseEGLSurface.EglStatus.DRAW;
        }
    }

    // 创建EGL环境
    public void createEGLEnv() {
        createDisplay();
        createConfig();
        createContext();
        createSurface();
        makeCurrent();
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

    public EGLSurface createFromSurface(Object surface) {
        EGLSurface b = null;
        if(surface instanceof SurfaceTexture){

        } else {
             SurfaceTexture a = (SurfaceTexture)surface;
            //todo 这里初始化完成 @w.k

        }
        return b;
    }

    // 5.绑定EGLSurface和EGLContext到显示设备（EGLDisplay）
    public void makeCurrent() {
        if (mEGLSurface != null && mEGLSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
            mEglStatus = BaseEGLSurface.EglStatus.INITIALIZED;
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

    //释放
    public void release() {


    }

    //释放
    public void releaseTartetSurface(){


    }

    // EGL状态
    enum EglStatus {
        INVALID, INITIALIZED, CREATED, CHANGED, DRAW;
        public boolean isValid() {
            return this != INVALID;
        }
    }

    public boolean setPresentationTime( long timeStamp) {
        if (!EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, timeStamp)) {
            Log.d(TAG, "setPresentationTime" + EGL14.eglGetError());
            return false;
        }
        return true;
    }

    public boolean swapBuffer() {
        if (!EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface)) {
            Log.d(TAG, "swapBuffers" + EGL14.eglGetError());
            return false;
        }
        return true;
    }

    // 渲染器接口
    interface Renderer {
        void onSurfaceCreated();
        void onSurfaceChanged(int width, int height);
        void onDrawFrame();
    }


}
