package com.mzmm.wave_me.recognize;

import static com.mzmm.wave_me.R.drawable.flash_off;
import static com.mzmm.wave_me.R.drawable.flash_on;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.mzmm.wave_me.R;
import com.mzmm.wave_me.databinding.ActivityCameraBinding;

import java.io.IOException;

public class CameraCode extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    int counter = 0;
    ActivityCameraBinding binding;
    boolean is_Flash = false;
    private com.mzmm.wave_me.recognize.objectDetectorClass objectDetectorClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        int MY_PERMISSIONS_REQUEST_CAMERA = 0;
        if (ContextCompat.checkSelfPermission(CameraCode.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(CameraCode.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.frame_Surface);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        try {
            objectDetectorClass = new objectDetectorClass(getAssets(), "hand_model.tflite", "custom_label.txt", 300, "sign_lang_model.tflite", 96);
            Log.d("MainActivity", "Model is successfully loaded");
        } catch (IOException e) {
            Log.d("MyLog", "Getting some error");
            Dialog dialog = new Dialog(this);
            dialog.setTitle("Error");
            e.printStackTrace();
        }
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                if (status == BaseLoaderCallback.SUCCESS) {
                    cameraBridgeViewBase.enableView();
                } else {
                    super.onManagerConnected(status);
                }
            }
        };
        binding.flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_Flash) {
                    binding.flash.setImageResource(flash_off);
                } else {
                    binding.flash.setImageResource(flash_on);
                }
                is_Flash = !is_Flash;
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "There is problem", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        if (counter % 2 == 0) {
            Core.flip(frame, frame, 1);
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2GRAY);
        }
        counter++;

        Toast.makeText(this, "Getting Camera Frame", Toast.LENGTH_SHORT).show();

        return frame;
    }

    public boolean checkPermissionForCamera() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}