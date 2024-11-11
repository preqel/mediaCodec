package com.example.testmediacodec.normalsec;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;

/**
 * Author: preqel
 * Created on: 2024/11/10.
 * Description:
 */
class CameraManeger {

    private Camera mCamera;

    public void OpenCamera(SurfaceTexture surfaceTexture) {
        try {
            mCamera = Camera.open();
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
