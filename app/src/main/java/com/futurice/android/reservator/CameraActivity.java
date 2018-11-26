package com.futurice.android.reservator;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurice.android.reservator.model.FaceDetector;
import com.futurice.android.reservator.model.NaamatauluAPI;
import com.futurice.android.reservator.model.UploadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "CameraActivity";
    Camera camera;
    SurfaceHolder holder;

    private final AtomicBoolean recognizing = new AtomicBoolean(false);
    private FaceDetector faceDetector = new FaceDetector(this, (Activity)this);
    private Handler handler;

    private Camera.FaceDetectionListener fdListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                Log.d(TAG, faces.length + " face(s)");
                if (!recognizing.getAndSet(true)) {
                    camera.takePicture(null, null, null, pictureCallback);
                }
            }
            if (faces.length == 0) {
                // TODO: to be used when user leaves the device and will be logged out
            }
        }
    };

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    File photo = pictureTaken(data);
                    File cropped = faceDetector.cropLargestFace(photo.getAbsolutePath());
                    if (cropped == null) {
                        Log.e(TAG, "Cannot get image");
                        return;
                    }
                    sendCropped(cropped);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        SurfaceView surface = (SurfaceView)findViewById(R.id.cameraPreview);
        holder = surface.getHolder();
        holder.addCallback(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;
        holder.setFixedSize(height, width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            openCamera();
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            camera.startFaceDetection();
        } catch (IOException e) {
            Log.e(TAG, "IOException " + e);
        }
    }

    private void openCamera() {
        if (camera == null) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            int defaultCameraId = 0;
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    defaultCameraId = i;
                }
            }
            camera = Camera.open(defaultCameraId);
            camera.setFaceDetectionListener(fdListener);
        }
    }

    private Handler getHandler() {
        if (handler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            handler = new Handler(thread.getLooper());
        }
        return handler;
    }

    private File pictureTaken(byte[] data) {
        File photo = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo.jpg");
        OutputStream os = null;
        try {
            os = new FileOutputStream(photo);
            os.write(data);
            os.close();
        } catch (IOException e) {
            Log.e(TAG, "Cannot write to " + e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return photo;
    }

    private void sendCropped(File cropped) {
        new NaamatauluAPI(this, new UploadListener() {
            @Override
            public void onUploadCompleted(String result) {
                if (result == null) {
                    Log.d(TAG, "Not identified");
                    return;
                }
                String username = null;
                ObjectMapper mapper = new ObjectMapper();
                try {
                    JsonNode rootNode = mapper.readTree(result);
                    JsonNode nameNode = rootNode.path("username");
                    username = nameNode.asText();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot read JSON");
                }
                Toast.makeText(CameraActivity.this, "Hello, " + username, Toast.LENGTH_LONG).show();
            }
        }).execute(cropped);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.stopFaceDetection();
        camera.stopPreview();
        camera.release();
    }
}
