package com.example.testmediacodec.dvr;


import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.example.testmediacodec.VideoEncoder;
import com.example.testmediacodec.render.RecordRenderDrawer;
import com.example.testmediacodec.test.CameraToMpegTest;
import com.example.testmediacodec.util.StorageUtil;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;

/***
 ** 编码录制视频的线程（重要）
 *
 */
public class RecordSurfaceRenderHandler extends Handler {

    private static final boolean DEBUG = false;	// TODO set false on release

    private static final String TAG = "RecordSurfaceRenderH";

    public static final int MSG_RENDER_SET_GLCONTEXT = 1;

    public static final int MSG_RENDER_DRAW = 2;

    public static final int MSG_RENDER_QUIT = 4;

    public  static final int MSG_RENDER_DRAW2 = 3;

    public static final int MSG_CHECK_VALID = 5;

    public static final int STOP_RECORD = 6;


    private int mTexId = -1;

    private final RenderThread mThread;

    public  static int idfff = 0;

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
        Log.d("TAG24", "settetid"+ mTexId);
        sendMessage(obtainMessage(MSG_RENDER_SET_GLCONTEXT, isRecordable ? 1 : 0, 0, new ContextParams(shared_context, surface)));
    }

    public void onInitDraw(){

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
                Log.d("TAG24", "MSG_RENDER_DRAW2 mtextid" + mTexId);
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

            case STOP_RECORD:
                mThread. handleStop();
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
        private GLDrawer2D mDrawer ;
        private CameraToMpegTest.STextureRender sTextureRender;


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
            Log.d("cjs2", " handle sEt Egl Context");
            release();
            synchronized (mSync) {
                if(surface instanceof SurfaceTexture){
                    mSurface = new Surface((SurfaceTexture) surface);
                } else {
                    Log.d("cjs2", "surface type wrong");
                }
//                mSurface = surface instanceof Surface ? (Surface)surface
//                        : (surface instanceof SurfaceTexture ? new Surface((SurfaceTexture)surface) : null);
            }

            mEgl = new EGLBase(shard_context, false, isRecordable);
        //    mEgl.createEGLEnv();
            mEgl.createDisplay();
            mEgl.createConfig();
            mEgl.createContext(shard_context);
            if(mVideoEncoder!= null){
                mEgl.mEGLSurface =  mEgl.createWindowSurface(   mVideoEncoder.getmInputSurface());
            //    mEgl.mEGLSurface =  mEgl.createWindowSurface(   videoEncoder2.getInputSurface());
                mEgl.makeCurrent();
                mTargetSurface = mEgl.mEGLSurface;
                mDrawer = new GLDrawer2D();

                mDrawer.initTex();
            }
          //  mTargetSurface = mEgl.createFromSurface(surface);
            Log.d("cjs2", "mDraw init");

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


        SurfaceEncoder mVideoEncoder;

          VideoEncoder videoEncoder2;



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

        /**
         * @wei 未启动record时, 线程会跑几帧后在swap阻塞.猜测是由于 InputSurface无效.
         * v2 加判断,Recording时才真正画
         * @param tex_id
         * @param transform
         * @param timestampNanos
         */
        private void handleFrameAvailable(int tex_id,float[] transform, long timestampNanos) {
            Log.v(TAG,"handleFrameAvailable #0"+ timestampNanos);
             mVideoEncoder = SurfaceEncoder.getInstance();


            if(mVideoEncoder==null || !mVideoEncoder.isRecording())
                return;
            String  outputFile = StorageUtil.getVedioPath(true) + "dvrt.mp4";
            Log.d("TAG23", "output"+ outputFile);
            if(videoEncoder2 == null){
                File file = new File(outputFile);
                try {
                    videoEncoder2 = new VideoEncoder(400, 400, file);
                }catch (IOException io){
                    Log.d("TAG23", "exec"+ outputFile + io.getMessage());
                    io.printStackTrace();
                }
            }
            Log.d(TAG, "handleDrain: #3"+ idfff);



            mVideoEncoder.drainAllEncoderMuxer(false);
        //   mVideoEncoder.drainAllEncoderMuxer(false);
         //   mVideoEncoder.drainAllEncoderMuxer(false);

//            if(idfff  ==1 ){
//                Log.d(TAG, "drainEncoder: true");
//                videoEncoder2.drainEncoder(true);
//            } else {
//                Log.d(TAG, "drainEncoder: false");
//                videoEncoder2.drainEncoder(false);
//            }
//            if(i>1000){
//                videoEncoder2.drainEncoder(true);
//            } else {
            //    videoEncoder2.drainEncoder(false);
//            }
            if(mDrawer == null)
            {
                Log.d("cjs2", "mDraw is null ");

                Log.v(TAG,"handleFrameAvailable mDrawer null");
                return;
            }

           // sTextureRender.drawFrame(mTargetSurface);
          mDrawer.draw(tex_id, transform);
       //     generateSurfaceFrame(0,transform);

            //testDraw();
           // mTargetSurface.setPresentationTime(timestampNanos);
            if(mEgl == null){
                Log.v(TAG,"mEgl is null before handleFrameAvailable#1 ");
            }
            try{
               mEgl.setPresentationTime(timestampNanos);
            } catch (Exception e){
                Log.e("cjs2", "error happend");
                Log.e("cjs2", e.getMessage());
            }
        //    mTargetSurface.swap();
            mEgl.swapBuffer();
            Log.v(TAG,"handleFrameAvailable #1");
        }




        private FloatBuffer mTriangleVertices;



        private void testDraw(int mTextureID) {
//            RecordRenderDrawer a= new RecordRenderDrawer();
            // (optional) clear to green so we can see if we're failing to set pixels
//            GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
//            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
//
//            GLES20.glUseProgram(mProgram);
//            checkGlError("glUseProgram");
//
//            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
//
//            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
//            GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
//                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
//            checkGlError("glVertexAttribPointer maPosition");
//            GLES20.glEnableVertexAttribArray(maPositionHandle);
//            checkGlError("glEnableVertexAttribArray maPositionHandle");
//
//            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
//            GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
//                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
//            checkGlError("glVertexAttribPointer maTextureHandle");
//            GLES20.glEnableVertexAttribArray(maTextureHandle);
//            checkGlError("glEnableVertexAttribArray maTextureHandle");
//
//            Matrix.setIdentityM(mMVPMatrix, 0);
//            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
//            GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
//
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//            checkGlError("glDrawArrays");
//
//            // IMPORTANT: on some devices, if you are sharing the external texture between two
//            // contexts, one context may not see updates to the texture unless you un-bind and
//            // re-bind it.  If you're not using shared EGL contexts, you don't need to bind
//            // texture 0 here.
//            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        }

        public void handleStop(){
            Log.v(TAG,"handleStop ");
            videoEncoder2.drainEncoder(true);
            if (mEgl != null) {

                mEgl.releaseTartetSurface();
                mEgl.destroyEGLEnv();
//                mEglHelper.destroySurface(mEglSurface);
//                mEglHelper.destroyGL();
//                mEglSurface = EGL14.EGL_NO_SURFACE;
                videoEncoder2.release();
                mEgl = null;
                mVideoEncoder = null;
            }
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
                Log.d("cjs2", "mDraw set  null ");

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


