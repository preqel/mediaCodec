package com.example.testmediacodec.normalsec;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.example.testmediacodec.R;


/**
 * Author: preqel
 * Created on: 2024/11/10.
 * Description:
 * https://github.com/huazi5D/CameraOpenglEs
 */
public class NormalSecActvity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_normal_sec);
        GLSurfaceView surfaceView = findViewById(R.id.cameraview);

        Button btn = (Button) findViewById(R.id.btnh);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(surfaceView.isShown() == true){
                        surfaceView.onPause();
                        surfaceView.setVisibility(View.INVISIBLE);
                    }else {
                        surfaceView.onResume();
                        surfaceView.setVisibility(View.VISIBLE);
                    }
            }
        });

        super.onCreate(savedInstanceState);
    }
}
