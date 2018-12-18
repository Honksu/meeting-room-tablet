package com.futurice.android.reservator.view;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.futurice.android.reservator.common.CurrentUser;
import com.futurice.android.reservator.model.FaceDetector;
import com.futurice.android.reservator.model.NaamatauluAPI;
import com.futurice.android.reservator.model.UploadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = CameraView.class.getSimpleName();
    private SurfaceHolder holder;
    private Camera camera;
    private Context context;

    private final AtomicBoolean recognizing = new AtomicBoolean(false);
    private FaceDetector faceDetector;

    private Runnable clearUser = new Runnable() {
        @Override
        public void run() {
            CurrentUser.getInstance().clearUser();
        }
    };

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
                Log.d(TAG, "No faces");
                getHandler().postDelayed(clearUser, 3000);
                recognizing.set(false);
            }
        }
    };

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, final Camera camera) {
            Log.d(TAG, "onPictureTaken");
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    File photo = pictureTaken(data);
                    File cropped = faceDetector.cropLargestFace(photo.getAbsolutePath());
                    if (cropped == null) {
                        Log.e(TAG, "Cannot get image");
                        return;
                    }
                    camera.startPreview();
                    camera.startFaceDetection();
                    sendCropped(cropped);
                }
            });
        }
    };


    public CameraView(Context context) { this(context, null); }

    public CameraView(Context context, AttributeSet attrs) { this(context, attrs, 0); }

    public CameraView(Context context, AttributeSet attrs, int defStyleattr) {
        super(context, attrs, defStyleattr);
        holder = getHolder();
        holder.addCallback(this);
        this.context = context;
        faceDetector = new FaceDetector(context, (Activity)context);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        openCamera();
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            camera.startFaceDetection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void openCamera() {
        if (camera == null) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            int cameraId = 0;
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraId = i;
                }
            }
            camera = Camera.open(cameraId);
            camera.setFaceDetectionListener(fdListener);
        }
    }

    private File pictureTaken(byte[] data) {
        File photo = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "photo.jpg");
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
        new NaamatauluAPI(context, new UploadListener() {
            @Override
            public void onUploadCompleted(String result) {
                if (result == null) {
                    Log.d(TAG, "Not identified");
                    Toast.makeText(context, "Not identified", Toast.LENGTH_LONG).show();
                    return;
                }
                CurrentUser.getInstance().processJson(result);
                String username = CurrentUser.getInstance().getUsername();

                Toast.makeText(context, "Hello, " + username, Toast.LENGTH_LONG).show();
            }
        }).execute(cropped);
    }
}
