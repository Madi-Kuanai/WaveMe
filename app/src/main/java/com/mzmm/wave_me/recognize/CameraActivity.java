package com.mzmm.wave_me.recognize;

import static com.mzmm.wave_me.R.id.add_btn;
import static com.mzmm.wave_me.R.id.space_btn;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.mzmm.wave_me.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "MyLog";

    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;
    private JavaCameraView javaCameraView;
    private int mCameraId = 0;
    private com.mzmm.wave_me.recognize.objectDetectorClass objectDetectorClass;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface
                        .SUCCESS: {
                    Log.i(TAG, "OpenCv Is loaded");
                    mOpenCvCameraView.enableView();
                }
                default: {
                    super.onManagerConnected(status);

                }
                break;
            }
        }
    };

    public CameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA = 0;
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        setContentView(R.layout.activity_camera);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setFocusable(true);
        mOpenCvCameraView.setCvCameraViewListener(this);
        try {
            objectDetectorClass = new objectDetectorClass(getAssets(), "hand_model.tflite", "custom_label.txt", 300, "sign_lang_model.tflite", 96);
            Log.d("MainActivity", "Model is successfully loaded");
        } catch (IOException e) {
            Log.d("MainActivity", "Getting some error");
            Dialog dialog = new Dialog(this);
            dialog.setTitle("Error");

            e.printStackTrace();
        }
        mOpenCvCameraView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                if (mCameraId == 1) {
                    mOpenCvCameraView.disableView();
                    mCameraId = 0;
                    mOpenCvCameraView.setCameraIndex(mCameraId);
                    mOpenCvCameraView.enableView();
                }
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                if (mCameraId == 0) {
                    mOpenCvCameraView.disableView();
                    mCameraId = 1;
                    mOpenCvCameraView.setCameraIndex(mCameraId);
                    mOpenCvCameraView.enableView();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            //if load success
            Log.d(TAG, "Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            //if not loaded
            Log.d(TAG, "Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }

    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        // Before watching this video please watch previous video of loading tensorflow lite model

        // now call that function
        Mat out = new Mat();
        Imgproc.resize(mRgba.t(), mRgba.t(), mRgba.size());
        out = objectDetectorClass.recognizeImage(mRgba);

        if (mCameraId == 1) {
            Core.flip(mRgba, mRgba, -1);
            Core.flip(mGray, mGray, -1);
        }

        return out;
    }

    public void set_letter(View view) {
        EditText text_box = findViewById(R.id.editTextTextMultiLine1);

        switch (view.getId()) {
            case add_btn: if (objectDetectorClass.getText() != null) {text_box.setText(new StringBuilder().append(text_box.getText()).append(objectDetectorClass.getText()).toString());} else Log.d("My", "Null Letter");
                break;
            case space_btn: text_box.setText(new StringBuilder().append(text_box.getText()).append(" ")); break;
            default:
                Log.d("My", "Error");
        }
    }



}
