package com.example.testmediacodec.dvr;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

/***
 ** 编码录制视频的线程（重要）
 */
public class RecordSurfaceRenderHandler extends Handler {

    private static final boolean DEBUG = false;	// TODO set false on release

    private static final String TAG = "RecordSurfaceRenderH";

    public static final int MSG_RENDER_SET_GLCONTEXT = 1;

    public static final int MSG_RENDER_DRAW = 2;

    public static final int MSG_RENDER_QUIT = 4;

    public  static final int MSG_RENDER_DRAW2 = 3;

    public static final int MSG_CHECK_VALID = 5;

    private int mTexId = -1;

    private final RenderThread mThread;

    /**安全获得RenderHandler*/
    public static RecordSurfaceRenderHandler createHandler() {
        if (DEBUG) Log.v(TAG, "createHandler:");
        return createHandler("RenderSurfaceThread");
    }

    private static final RecordSurfaceRenderHandler createHandler(final String name) {
        if (DEBUG) Log.v(TAG, "createHandler:name=" + name);
        final RenderThread thread = new RenderThread(name);
        thread.start();
        return thread.getHandler();
    }

    public final void setEglContext(final EGLContext shared_context, final int tex_id, final Object surface, final boolean isRecordable) {
        if (DEBUG) Log.i(TAG, "RenderHandler:setEglContext:");
        if (!(surface instanceof Surface) && !(surface instanceof SurfaceTexture) && !(surface instanceof SurfaceHolder))
            throw new RuntimeException("unsupported window type:" + surface);
        mTexId = tex_id;
        sendMessage(obtainMessage(MSG_RENDER_SET_GLCONTEXT, isRecordable ? 1 : 0, 0, new ContextParams(shared_context, surface)));
    }

    public final void release() {
        if (DEBUG) Log.i(TAG, "release:");
        removeMessages(MSG_RENDER_SET_GLCONTEXT);
        removeMessages(MSG_RENDER_DRAW);
        sendEmptyMessage(MSG_RENDER_QUIT);
    }

    @Override
    public final void handleMessage(final Message msg) {
        switch (msg.what) {
            case MSG_RENDER_SET_GLCONTEXT:
                Log.d("TAG24", "MSG_RENDER_SET_GLCONTEXT");
                final ContextParams params = (ContextParams)msg.obj;
                mThread.handleSetEglContext(params.shared_context, params.surface, msg.arg1 != 0);
                break;
            case MSG_RENDER_DRAW:
                Log.d("TAG24", "MSG_RENDER_DRAW");
                //mThread.handleDraw(msg.arg1, (float[])msg.obj);		//@wei 0604
                mThread.handleDraw(mTexId,(float[])msg.obj);
                break;
            case MSG_RENDER_DRAW2:
                Log.d("TAG24", "MSG_RENDER_DRAW2");
                long timestamp = (((long) msg.arg1) << 32) |
                        (((long) msg.arg2) & 0xffffffffL);
                mThread.handleFrameAvailable(mTexId, (float[]) msg.obj, timestamp);
                //mThread.handleDrain();
                break;
            case MSG_CHECK_VALID:
                Log.d("TAG24", "MSG_CHECK_VALID");
                synchronized (mThread.mSync) {
                    mThread.mSync.notify();
                }
                break;
            case MSG_RENDER_QUIT:
                Looper.myLooper().quit();
                break;
            default:
                super.handleMessage(msg);
        }
    }

    //********************************************************************************
    private RecordSurfaceRenderHandler(final RenderThread thread) {
        if (DEBUG) Log.i(TAG, "RenderHandler:");
        mThread = thread;
    }

    private static final class ContextParams {
        final EGLContext shared_context;
        final Object surface;
        public ContextParams(final EGLContext shared_context, final Object surface) {
            this.shared_context = shared_context;
            this.surface = surface;
        }
    }

    /**
     * Thread to execute render methods
     * You can also use HandlerThread insted of this and create Handler from its Looper.
     */
    private static final class RenderThread extends Thread {
        private static final String TAG_THREAD = "RenderThread";
        private final Object mSync = new Object();
        private RecordSurfaceRenderHandler mHandler;
        private EGLBase mEgl;
         private EGLSurface mTargetSurface;
     //   private EGLBase.EglSurface mTargetSurface;
        private Surface mSurface;
        private GLDrawer2D mDrawer;

        public RenderThread(final String name) {
            super(name);
        }

        public final RecordSurfaceRenderHandler getHandler() {
            synchronized (mSync) {
                // create rendering thread
                try {
                    mSync.wait();
                } catch (final InterruptedException e) {
                }
            }
            return mHandler;
        }

        /**
         * Set shared context and Surface
         * @param shard_context
         * @param surface
         */
        public final void handleSetEglContext(final EGLContext shard_context, final Object surface, final boolean isRecordable) {
            if (DEBUG) Log.i(TAG_THREAD, "setEglContext:");
            release();
            synchronized (mSync) {
                mSurface = surface instanceof Surface ? (Surface)surface
                        : (surface instanceof SurfaceTexture ? new Surface((SurfaceTexture)surface) : null);
            }
            mEgl = new EGLBase(shard_context, false, isRecordable);
            mTargetSurface = mEgl.createFromSurface(surface);
            mDrawer = new GLDrawer2D();
        }

        /**
         * drawing  @wei 这里也很重要。Texture 创建保存在 GPU内存里.不同shader可以共用一个texture 。
         * @param tex_id
         * @param tex_matrix
         */
        public void handleDraw(final int tex_id, final float[] tex_matrix) {
            if (DEBUG) Log.i(TAG_THREAD, "draw");
            if (tex_id >= 0) {
                //mTargetSurface.makeCurrent(); 直接换成egl的makecurrent
                mEgl.makeCurrent();
                mDrawer.draw(tex_id, tex_matrix);
             //   mTargetSurface.swap(); 直接换成egl
                mEgl.swapBuffer();

            }
        }

        /**
         * @wei 未启动record时, 线程会跑几帧后在swap阻塞.猜测是由于 InputSurface无效.
         * v2 加判断,Recording时才真正画
         * @param tex_id
         * @param transform
         * @param timestampNanos
         */
        private void handleFrameAvailable(int tex_id,float[] transform, long timestampNanos) {
            Log.v(TAG,"handleFrameAvailable #0");
            SurfaceEncoder mVideoEncoder = SurfaceEncoder.getInstance();
            if(mVideoEncoder==null || !mVideoEncoder.isRecording())
                return;
            Log.d(TAG, "handleDrain: #3");
            mVideoEncoder.drainAllEncoderMuxer(false);
            if(mDrawer== null)
            {
                return;
            }
            mDrawer.draw(tex_id, transform);
           // mTargetSurface.setPresentationTime(timestampNanos);
            mEgl.setPresentationTime(timestampNanos);
        //    mTargetSurface.swap();
            mEgl.swapBuffer();
            Log.v(TAG,"handleFrameAvailable #1");
        }

        @Override
        public final void run() {
            if (DEBUG) Log.v(TAG_THREAD, "started");
            Looper.prepare();
            synchronized (mSync) {
                mHandler = new RecordSurfaceRenderHandler(this);
                mSync.notify();
            }
            Looper.loop();
            if (DEBUG) Log.v(TAG_THREAD, "finishing");
            release();
            synchronized (mSync) {
                mHandler = null;
            }
            if (DEBUG) Log.v(TAG_THREAD, "finished");
        }

        private final void release() {
            if (DEBUG) Log.v(TAG_THREAD, "release:");
            if (mDrawer != null) {
                mDrawer.release();
                mDrawer = null;
            }
            synchronized (mSync) {
                mSurface = null;
            }
            if (mTargetSurface != null) {
                clear();
                //mTargetSurface.release(); 直接换成egl的成员函数
                mEgl.releaseTartetSurface();
                mTargetSurface = null;
            }
            if (mEgl != null) {
//                mEgl.release();
                mEgl.release();
                mEgl = null;
            }
        }

        /**
         * Fill black on specific Surface
         */
        private final void clear() {
            if (DEBUG) Log.v(TAG_THREAD, "clear:");
            //mTargetSurface.makeCurrent();
            mEgl.makeCurrent();
            GLES20.glClearColor(0, 0, 0, 1);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            // mTargetSurface.swap();
            mEgl.swapBuffer();
        }
    }
}


