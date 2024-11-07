package com.example.testmediacodec;

import static android.opengl.GLES10.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGLContext;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.example.testmediacodec.dvr.Model;
import com.example.testmediacodec.dvr.MyEGLSurface;
import com.example.testmediacodec.dvr.RecordSurfaceRenderHandler;
import com.example.testmediacodec.testpreview.HRender;
import com.example.testmediacodec.testpreview.TestSecondRender;
import com.example.testmediacodec.testpreview.Triangle;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by QJMOTOR on 2024/11/7.
 */
public class TestPreviewActivity extends Activity implements SurfaceTexture.OnFrameAvailableListener, Model.Callback {
    private String TAG = "CameraActivity";
    private final int PERMISSION_CODE = 1;
    private int mOESTextureId = -1;
    private Camera mCamera = null;

    private GLSurfaceView glSurfaceView;
    private SurfaceTexture mSurfaceTexture;




    //************
    private EGLContext shareContext  ; //共享的上下文

   private RecordSurfaceRenderHandler recordSurfaceRenderHandler ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCamera = Camera.open();
        setContentView(R.layout.activity_test_preview);
        recordSurfaceRenderHandler = RecordSurfaceRenderHandler.createHandler();
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


//                Intent intent = new Intent(CameraActivity.this, EmptyActivity.class);
//                startActivity(intent);
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(shareContext!= null){
                    recordSurfaceRenderHandler.setEglContext(shareContext , 1, mSurfaceTexture,true );
                }

            }
        });

//        requestPermission();
        mSurfaceTexture = new SurfaceTexture(1);

        glSurfaceView = findViewById(R.id.gl);
        //设置渲染GLES版本
        glSurfaceView.setEGLContextClientVersion(2);
        //设置渲染回调
      glSurfaceView.setRenderer(new MyRender(this));
      // glSurfaceView.setRenderer(new MyRender(mSurfaceTexture, glSurfaceView, mCamera));

       // glSurfaceView.setRenderer(new HRender(TestPreviewActivity.this, mCamera, mSurfaceTexture, glSurfaceView));
        /*渲染方式，RENDERMODE_WHEN_DIRTY表示被动渲染，只有在调用requestRender或者onResume等方法时才会进行渲染。RENDERMODE_CONTINUOUSLY表示持续渲染*/
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

       // Log.d(TAG, "onCreate: 这是相机页面");

        initEGLSurface();
    }


    private void initEGLSurface() {
        MyEGLSurface  mEGlSurface = new MyEGLSurface(this);
        com.example.testmediacodec.dvr.MyRender render =new  com.example.testmediacodec.dvr.MyRender(this.getResources());
        render.setCallback(this);
        shareContext =   mEGlSurface.init(render);
    }

    private void requestPermission(){
     //   PermissionHelper.with(this).requestCode(PERMISSION_CODE).requestPermissions(Manifest.permission.CAMERA).request();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //Log.e(TAG,"requestPermission onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      //  PermissionHelper.requestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    float[] mTransformMatrix = new  float[16];
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture){
        glSurfaceView.requestRender();
        if(mTransformMatrix== null){
            mTransformMatrix = new  float[16];
        }
        surfaceTexture.getTransformMatrix(mTransformMatrix);
        Log.d("TAG23","OnFrameAvailable" );
        long timestamp = surfaceTexture.getTimestamp();
        if(timestamp == 0L){
            Log.d("TAG24", "timestamp null");
        } else {
            //向另外要给线程发送数据，感谢https://www.jianshu.com/p/702e7b065eb3
            Message message = recordSurfaceRenderHandler.obtainMessage(RecordSurfaceRenderHandler.MSG_RENDER_DRAW2, (int) (timestamp >> 32), (int)timestamp, mTransformMatrix);
            recordSurfaceRenderHandler.sendMessage(message);
        }
      //  setOnFrameAvailableListener

    }

    @Override
    public void onCall(Bitmap bitmap) {

    }

    public class MyRender implements GLSurfaceView.Renderer{
        private Context context;
        Triangle triangle = null;
        public MyRender(Context context) {
            this.context=context;
        }
        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            //todo 只运行一次
           // requestPermission();
            //擦除颜色红色
            glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
            mOESTextureId = createOESTextureObject();
            //创建一个渲染图
            mSurfaceTexture = new SurfaceTexture(mOESTextureId);
            //new一个控制GLES渲染的类
            triangle = new Triangle(context);
            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //添加帧可用监听，通知GLSurface渲染
            mSurfaceTexture.setOnFrameAvailableListener(TestPreviewActivity.this);
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
            //todo 渲染窗口大小发生改变的处理
            Log.e(TAG, "onSurfaceChanged232323232323232 width:" + width + "  height" + height);
            triangle.Change(width, height);
        }


        @Override
        public void onDrawFrame(GL10 gl10) {
            //todo 执行渲染工作
            glClear(GL_COLOR_BUFFER_BIT);
            mSurfaceTexture.updateTexImage();
            triangle.draw();
        }
    }
    public static int createOESTextureObject() {
        int[] tex = new int[1];
        //生成一个纹理
        GLES20.glGenTextures(1, tex, 0);
        //将此纹理绑定到外部纹理上
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        //设置纹理过滤参数
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        //解除纹理绑定
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}
