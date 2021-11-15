package com.mzmm.wave_me.recognize;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;
import java.util.List;

public class Tutorial3View extends JavaCameraView implements Camera.PictureCallback {
    private String mPictureFileName;

    public Tutorial3View(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedFlashModes();
    }

    public boolean isEffectSupported() {
        return (mCamera.getParameters().getFlashMode() != null);
    }

    public String getEffect() {
        return mCamera.getParameters().getFlashMode();
    }

    public void setEffect(String effect) {
        if (mCamera == null)
            try {
                mCamera = android.hardware.Camera.open();
                /*mCamera.set(Highgui.CV_CAP_PROP_ANDROID_FLASH_MODE,
                        Highgui.CV_CAP_ANDROID_FLASH_MODE_TORCH);*/

                Camera.Parameters p = mCamera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(p);
                mCamera.startPreview();

            } catch (RuntimeException ex) {
            }

       /* mCamera.getParameters();
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(params);
        mCamera.startPreview();*/
    }

    public List<Camera.Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(int w, int h) {
        disconnectCamera();
        mMaxHeight = h;
        mMaxWidth = w;
        connectCamera(getWidth(), getHeight());
    }

    public Camera.Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    public void takePicture(final String fileName) {
        this.mPictureFileName = fileName;
        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
        // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
        mCamera.setPreviewCallback(null);

        // PictureCallback is implemented by the current class
        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);
            fos.write(data);
            fos.close();
        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }

    }

    public void cameraRelease() {
        if (mCamera != null) {
            mCamera.release();
        }
    }
}
