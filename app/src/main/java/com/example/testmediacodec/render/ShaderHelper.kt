package com.example.testmediacodec.render

import android.opengl.GLES20
import android.util.Log
import android.util.Log.DEBUG

/**
 * @author: w.k
 * @date: 2024/11/15
 * @description:
 */
class ShaderHelper {

    companion object{

        const val TAG ="ShaderHelper"


        /**
         * 将顶点着色器和片元着色器链接到 OpenGL 程序中
         *
         * @param vertexShaderId   顶点着色器id
         * @param fragmentShaderId 片元着色器id
         * @return 链接成功则返回 OpenGL 程序 ID，否则返回 0
         */
        fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
            // 1.创建着色器程序
            val programId = GLES20.glCreateProgram()
            if (programId == 0) {
                Log.e(TAG, "创建 OpenGL 程序失败")
                return 0
            }

            // 2.将着色器对象附加到着色器程序上
            GLES20.glAttachShader(programId, vertexShaderId)
            GLES20.glAttachShader(programId, fragmentShaderId)

            // 3.链接着色器，将所有添加到 Program 中的着色器链接到一起
            GLES20.glLinkProgram(programId)

            // 4.获取并判断链接状态
            val status = IntArray(1)
            GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, status, 0)
            if (status[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Link program error:${GLES20.glGetProgramInfoLog(programId)}")
                // 删除程序
                GLES20.glDeleteProgram(programId)
                return 0
            }

            // 5.释放已经编译过，但不再需要的着色器对象（以及所占用的资源）
            GLES20.glDeleteShader(vertexShaderId)
            GLES20.glDeleteShader(fragmentShaderId)

            return programId
        }

        /**
         * 验证程序（开发过程中可用于调试）
         */
        fun validateProgram(programId: Int): Boolean {
            GLES20.glValidateProgram(programId)
            val validateStatus = IntArray(1)
            GLES20.glGetProgramiv(programId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0)
            if (validateStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Program validation error:${GLES20.glGetProgramInfoLog(programId)}")
                return false
            }
            return true
        }


        /**
         * 加载并编译顶点着色器
         * @param shaderCode 顶点着色器代码
         * @return 编译成功返回顶点着色器 ID，否则返回 0
         */
        fun compileVertexShader(shaderCode: String): Int {
            return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode)
        }

        /**
         * 加载并编译片元着色器
         * @param shaderCode 片元着色器代码
         * @return 编译成功返回顶点着色器 ID，否则返回 0
         */
        fun compileFragmentShader(shaderCode: String): Int {
            return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode)
        }

        /**
         * 加载并编译着色器代码
         * @param type 着色器类型。GL_VERTEX_SHADER 是顶点着色器，GL_FRAGMENT_SHADER 是片元着色器
         * @param code 着色器代码
         * @return 成功返回着色器 Id，失败返回 0
         */
        private fun compileShader(type: Int, code: String): Int {
            // 1.创建着色器
            val shaderId = GLES20.glCreateShader(type)
            if (shaderId == 0) {
                    Log.e(TAG, "创建着色器失败")
                return 0
            }

            // 2.编译着色器代码
            // 2.1 将源代码绑定到着色器上，加载到 OpenGL 中以编译和执行
            GLES20.glShaderSource(shaderId, code)
            // 2.2 编译着色器中的源代码为可在 GPU 上执行的二进制形式
            GLES20.glCompileShader(shaderId)
            // 2.3 获取编译状态
            val status = IntArray(1)
            GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, status, 0)
            // 2.4 判断编译状态
            if (status[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Load vertex shader failed:${GLES20.glGetShaderInfoLog(shaderId)}")
                    Log.d(TAG, "着色器代码: \n${code}")
                // 删除着色器对象
                GLES20.glDeleteShader(shaderId)
                return 0
            }
            return shaderId
        }
    }
}