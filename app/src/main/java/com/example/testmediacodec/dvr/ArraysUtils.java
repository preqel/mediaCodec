package com.example.testmediacodec.dvr;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ArraysUtils {
    public static FloatBuffer getFloatBuffer(float[] floatArr) {
        FloatBuffer fb = ByteBuffer.allocateDirect(floatArr.length * Float.BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
        fb.put(floatArr);
        fb.position(0);
        return fb;
    }
}
