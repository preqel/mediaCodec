package com.example.testmediacodec;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

public class CameraHelper {

    public static boolean hasCamera(Context context){
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Camera camera = null;
            try {
                camera = Camera.open();
            }catch(RuntimeException e){
                return false;
            }finally {
                if(camera!= null){
                    camera.release();
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
