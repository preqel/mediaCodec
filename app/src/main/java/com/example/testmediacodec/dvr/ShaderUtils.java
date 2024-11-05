package com.example.testmediacodec.dvr;

import android.content.res.Resources;
import android.opengl.GLES30;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShaderUtils {
    //创建程序id
    public static int createProgram(Resources resources, int vertexShaderResId, int fragmentShaderResId) {
        final int vertexShaderId = compileShader(resources, GLES30.GL_VERTEX_SHADER, vertexShaderResId);
        final int fragmentShaderId = compileShader(resources, GLES30.GL_FRAGMENT_SHADER, fragmentShaderResId);
        return linkProgram(vertexShaderId, fragmentShaderId);
    }

    //通过外部资源编译着色器
    private static int compileShader(Resources resources, int type, int shaderId){
        String shaderCode = readShaderFromResource(resources, shaderId);
        return compileShader(type, shaderCode);
    }

    //通过代码片段编译着色器
    private static int compileShader(int type, String shaderCode){
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);
        return shader;
    }

    //链接到着色器
    private static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        final int programId = GLES30.glCreateProgram();
        //将顶点着色器加入到程序
        GLES30.glAttachShader(programId, vertexShaderId);
        //将片元着色器加入到程序
        GLES30.glAttachShader(programId, fragmentShaderId);
        //链接着色器程序
        GLES30.glLinkProgram(programId);
        return programId;
    }

    //从shader文件读出字符串
    private static String readShaderFromResource(Resources resources, int shaderId) {
        InputStream is = resources.openRawResource(shaderId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
