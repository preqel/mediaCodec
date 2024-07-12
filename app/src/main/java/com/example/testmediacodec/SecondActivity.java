package com.example.testmediacodec;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import androidx.activity.ComponentActivity;

import com.example.testmediacodec.view.CameraFragment;


/**
 * Created By Chengjunsen on 2018/8/22
 */
public class SecondActivity extends ComponentActivity {
    private CameraFragment mCameraFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.main2);

        mCameraFragment = new CameraFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.main_container, mCameraFragment);
        transaction.commit();
    }
}
