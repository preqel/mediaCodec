package com.example.testmediacodec.dvr;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by QJMOTOR on 2024/11/1.
 */
public class TestTriRender implements GLSurfaceView.Renderer {

    private SurfaceTexture surfaceTexture;

    private int textureId;

    private GLSurfaceView glSurfaceView ;



    private final FloatBuffer vertexBuffer;
    //顶点颜色缓存
    private final FloatBuffer colorBuffer;
    //渲染程序
    private int mProgram;

    private Context context;
    private static final int POSITION_COMPONENT_COUNT = 3;

    //三个顶点的位置参数
    private float triangleCoords[] = {
            0.5f, 0.5f, 0.0f, // top
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f // bottom right
    };

    //三个顶点的颜色参数
    private float color[] = {
            1.0f, 0.0f, 0.0f, 1.0f,// top
            0.0f, 1.0f, 0.0f, 1.0f,// bottom left
            0.0f, 0.0f, 1.0f, 1.0f// bottom right
    };

    public TestTriRender(SurfaceTexture surfaceTexture, GLSurfaceView glSurfaceView){
        Log.d("TAG23", "TestRender init");
        this.surfaceTexture = surfaceTexture;
        this.glSurfaceView = glSurfaceView;


        vertexBuffer = ByteBuffer.allocateDirect(triangleCoords.length * Float.BYTES) // 直接分配native内存
                .order(ByteOrder.nativeOrder()) // 和本地平台保持一致的字节序
                .asFloatBuffer(); // 将底层字节映射到FloatBuffer实例，方便使用
        vertexBuffer.put(triangleCoords); // 将顶点数据拷贝到native内存中
        // 将数组数据put进buffer之后，指针并不是在首位，所以一定要position到0，至关重要！否则会有很多奇妙的错误！将缓冲区的指针移动到头部，保证数据是从最开始处读取
        vertexBuffer.position(0);

        //顶点颜色相关
        colorBuffer = ByteBuffer.allocateDirect(color.length * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d("TAG23", "TestRender onSurfaceCreated");
        // 生成纹理
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];
        Log.d("TAG23", "TestRender onSurfaceCreated" +textureId);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        // 设置纹理参数
//        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

//        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

      //  GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        // 关联SurfaceTexture和纹理
//        surfaceTexture.attachToGLContext(999);
//        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
//            @Override
//            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//                // 请求下一帧数据
//                GLES20.glFlush();
//            }
//        });
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 更新SurfaceTexture的图像内容
        Log.d("TAG23", "onDrawFrame");


        //把颜色缓冲区设置为我们预设的颜色，绘图设计到多种缓冲区类型:颜色、深度和模板。这里只是向颜色缓冲区中绘制图形
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        // glVertexAttribPointer是把顶点位置属性赋值给着色器程序
        // 0是上面着色器中写的vPosition的变量位置(location = 0)。意思就是绑定vertex坐标数据，然后将在vertextBuffer中的顶点数据传给vPosition变量。
        // 你肯定会想，如果我在着色器中不写呢？int vposition = glGetAttribLocation(program, "vPosition");就可以获得他的属性位置了
        // 第二个size是3，是因为上面我们triangleCoords声明的属性就是3位，xyz
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 3 * Float.BYTES, vertexBuffer);
        //启用顶点变量，这个0也是vPosition在着色器变量中的位置，和上面一样，在着色器文件中的location=0声明的
        // 也就是说由于vPosition在着色器中的位置被指定为0，因此可以简单的通过glVertexAttribPointer()函数调用中的第一个参数和在glEnableVertexAttribArray()函数调用中使用0来引用此变量
        GLES30.glEnableVertexAttribArray(0);

        //准备颜色数据
        /**
         * glVertexAttribPointer()方法的参数上面的也说过了，这里再按照这个场景说一下分别为:
         * index：顶点属性的索引.（这里我们的顶点位置和颜色向量在着色器中分别为0和1）layout (location = 0) in vec4 vPosition; layout (location = 1) in vec4 aColor;
         * size: 指定每个通用顶点属性的元素个数。必须是1、2、3、4。此外，glvertexattribpointer接受符号常量gl_bgra。初始值为4（也就是涉及颜色的时候必为4）。
         * type：属性的元素类型。（上面都是Float所以使用GLES30.GL_FLOAT）；
         * normalized：转换的时候是否要经过规范化，true：是；false：直接转化；
         * stride：跨距，默认是0。（由于我们将顶点位置和颜色数据分别存放没写在一个数组中，所以使用默认值0）
         * ptr： 本地数据缓存（这里我们的是顶点的位置和颜色数据）。
         */
        // 1是aColor在属性的位置，4是因为我们声明的颜色是4位，r、g、b、a。
        GLES30.glVertexAttribPointer(1, 4, GLES30.GL_FLOAT, false, 4 * Float.BYTES, colorBuffer);
        //启用顶点颜色句柄
        GLES30.glEnableVertexAttribArray(1);


        //绘制三个点
//        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, POSITION_COMPONENT_COUNT);

        //绘制三条线
//        GLES30.glLineWidth(3);//设置线宽
//        GLES30.glDrawArrays(GLES30.GL_LINE_LOOP, 0, POSITION_COMPONENT_COUNT);

        //绘制三角形
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, POSITION_COMPONENT_COUNT);

        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(0);
        GLES30.glDisableVertexAttribArray(1);

        surfaceTexture.updateTexImage();

        // 绘制图像，这里需要使用shader等进行绘制
        // ...
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d("TAG23", "onSurfaceChanged-" + width+ height);

        // 视图尺寸变化时的处理
        glSurfaceView.requestRender();
        //    GLES20.glViewport(0, 0, width, height);
    }
}
