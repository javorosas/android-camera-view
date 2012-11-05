package com.nearsoft.CameraViewSample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import com.nearsoft.AndroidToolkit.CameraView;

public class MainActivity extends Activity {

    CameraView cameraView;
    Button btnTakePic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        cameraView = (CameraView) this.findViewById(R.id.cameraView);
        btnTakePic = (Button) this.findViewById(R.id.btnTakePic);

        btnTakePic.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.takePicture(Environment.getExternalStorageDirectory() + "/CameraViewSample/picture.jpg");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.startCamera();
        cameraView.setCameraDisplayOrientation(this);
    }

    @Override
    protected void onPause() {
        cameraView.stopCamera();
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
