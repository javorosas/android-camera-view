package com.nearsoft.AndroidToolkit;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: javO
 * Date: 10/28/12
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private Camera camera;
    private boolean previewActive;
    private Vibrator vibrator;
    private String fileName;
    private Camera.Parameters parameters;

    public boolean isPreviewActive() {
        return previewActive;
    }

    private void setPreviewActive(boolean previewActive) {
        this.previewActive = previewActive;
    }

    private String getFileName() {
        return fileName;
    }

    private void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Camera.Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Camera.Parameters parameters) {
        this.parameters = parameters;
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPreviewActive(false);
        super.getHolder().addCallback(this);
        vibrator = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
    }

    public void startCamera(){
        camera = Camera.open();
    }

    public void stopCamera() {
        if (camera != null) {
            if (isPreviewActive()) {
                camera.stopPreview();
                setPreviewActive(false);
            }
            camera.release();
            camera = null;
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return (result);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            camera.setPreviewDisplay(this.getHolder());
        } catch (Throwable t) {
            Log.e(CameraView.class.getName(), "Exception in setPreviewDisplay()", t);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera == null) {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = getBestPreviewSize(width, height, parameters);

        if (size != null) {
            parameters.setPreviewSize(size.width, size.height);
            camera.setParameters(parameters);
            camera.startPreview();
            setPreviewActive(true);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // not used
    }

    public void takePicture(String fileName) {
        setFileName(fileName);
        camera.autoFocus(new Camera.AutoFocusCallback() {
            Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
                public void onShutter() {
                    vibrator.vibrate(300);
                }
            };

            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    camera.takePicture(shutterCallback, null, pictureCallback);
                }
            }
        });
    }

    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            savePhotoAsync(data, fileName);
            camera.startPreview();
            setPreviewActive(true);
        }
    };

    public void savePhotoAsync(byte[] photo, String fileName){
        new SavePhotoTask(getFileName()).execute(photo);
    }

    class SavePhotoTask extends AsyncTask<byte[], String, String> {
        String fileName;

        public SavePhotoTask(String fileName) {
            this.fileName = fileName;
        }


        @Override
        protected String doInBackground(byte[]... jpeg) {

            File photo = new File(this.fileName);
            if (photo.exists()) {
                photo.delete();
            }

            photo.getParentFile().mkdirs();

            try {
                FileOutputStream fos = new FileOutputStream(photo.getPath());

                fos.write(jpeg[0]);
                fos.close();
            }
            catch (java.io.IOException e) {
                Log.e(this.getClass().getName(), "Exception in photoCallback", e);
            }

            return(null);
        }
    }

    public void setCameraDisplayOrientation(Activity activity) {
        if (camera != null) {
            //camera.stopPreview();
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(0, info);
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }

            int result;
            result = (info.orientation - degrees + 360) % 360;

            camera.setDisplayOrientation(result);
            //camera.startPreview();
        }
    }
}
