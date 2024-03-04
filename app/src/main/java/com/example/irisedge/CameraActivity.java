package com.example.irisedge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class CameraActivity extends org.opencv.android.CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
//
    public Mat mRgba,mGrey;
    public CascadeClassifier cascadeClassifier;
    public CameraBridgeViewBase cameraBridgeViewBase;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    public int absoluteObjectSize = 0;

    public CameraActivity() {
//        Toast.makeText(this, "Constructor called", Toast.LENGTH_SHORT).show();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);


        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }








        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.cameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "Open CV not working", Toast.LENGTH_SHORT).show();
        } else {
            cameraBridgeViewBase.enableView();
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

    private void copyCascadeFile() {
        try {
            // Load the cascade file from the assets folder
            InputStream is = getResources().getAssets().open("haarcascade_frontalface_alt.xml");
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "haarcascade_fullbody.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            cascadeClassifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
            Toast.makeText(this, "cascade loaded successfully: "+cascadeFile.getName(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "FAILED TO LOAD CASCADE:"+e, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }



    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initDebug();
        } else {
            Toast.makeText(this, "OpenCvLoader", Toast.LENGTH_SHORT).show();
            if (cameraBridgeViewBase != null) {
                cameraBridgeViewBase.enableView();
            }
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
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
//        Mat detectionMat = objectDetection();
//        drawBoundingBoxes(mRgba, detectionMat);

        return mRgba;
    }

    private Mat objectDetection(){
        String outputName = "output0";
        double scaleFactor = 1/255.0;
        Net net = Dnn.readNetFromONNX("best.onnx");
        Mat blob, detectionMat = null;
        blob = Dnn.blobFromImage(mRgba, scaleFactor, new Size(224, 224), new Scalar(0,0,0), false, false);
        net.setInput(blob);
        detectionMat = net.forward(outputName);
        return detectionMat;
    }

    private void drawBoundingBoxes(Mat mat, Mat detectionMat){
        for (int i = 0; i < detectionMat.rows(); i++) {
            double confidence = detectionMat.get(i, 4)[0];
            if (confidence > 0.5) {
                // The bounding box coordinates are the first four values in the row
                int centerX = (int) detectionMat.get(i, 0)[0];
                int centerY = (int) detectionMat.get(i, 1)[0];
                int width = (int) detectionMat.get(i, 2)[0];
                int height = (int) detectionMat.get(i, 3)[0];

                // Draw the bounding box
                Point center = new Point(centerX, centerY);
                int left = centerX - width / 2;
                int top = centerY - height / 2;
                Rect rect = new Rect(left, top, width, height);
                Imgproc.rectangle(mat, rect.tl(), rect.br(), new Scalar(255, 0, 0), 2);
            }
        }
    }

}