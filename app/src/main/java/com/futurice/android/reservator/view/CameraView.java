package com.futurice.android.reservator.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.futurice.android.reservator.LandingActivity;
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
    private FaceDetector faceDetector;

    public enum State {
        NO_USER,
        RECOGNIZED,
        USING,
        LOGOUT
    }

    State state = State.NO_USER;
    boolean manualStop = false;
    AtomicBoolean takingphoto = new AtomicBoolean(false);

    private Runnable clearUser = new Runnable() {
        @Override
        public void run() {
            CurrentUser.getInstance().clearUser();
            state = State.NO_USER;
            //Toast.makeText(context, "User logged out", Toast.LENGTH_LONG).show();
        }
    };

    private Camera.FaceDetectionListener fdListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (!takingphoto.get()) {
                if (faces.length > 0) {
                    Log.d(TAG, faces.length + " face(s)");
                    boolean closeUser = false;
                    switch (state) {
                        case NO_USER:
                            takingphoto.set(true);
                            camera.takePicture(null, null, null, pictureCallback);
                            if (manualStop) {
                                camera.stopFaceDetection();
                            }
                            //Toast.makeText(context, "Recognizing...", Toast.LENGTH_SHORT).show();
                            break;
                        case RECOGNIZED:
                            for (Camera.Face face : faces) {
                                if (face.rect.height() > 400) {
                                    CurrentUser.getInstance().setLoggedIn();
                                    state = State.USING;
                                    //Toast.makeText(context, "Hello, " + CurrentUser.getInstance().getUsername(), Toast.LENGTH_LONG).show();
                                    final Intent i = new Intent(context, LandingActivity.class);
                                    context.startActivity(i);
                                    break;
                                }
                            }
                            break;
                        case USING:
                            for (Camera.Face face : faces) {
                                if (face.rect.height() > 400) {
                                    closeUser = true;
                                }
                            }
                            if (!closeUser) {
                                state = State.LOGOUT;
                                //Toast.makeText(context, "Logging out...", Toast.LENGTH_SHORT).show();
                                getHandler().postDelayed(clearUser, 5000);
                            }
                            break;
                        case LOGOUT:
                            for (Camera.Face face : faces) {
                                if (face.rect.height() > 400) {
                                    closeUser = true;
                                }
                            }
                            if (closeUser) {
                                getHandler().removeCallbacks(clearUser);
                                //Toast.makeText(context, "Back again", Toast.LENGTH_SHORT).show();
                                state = State.USING;
                            }
                    }
                }
                if (faces.length == 0) {
                    Log.d(TAG, "No faces");
                    switch (state) {
                        // TODO: pohdipa missä vaiheessa tyhjätä käyttäjä, tuleeko tää liian aikaisin?
                        case RECOGNIZED:
                            getHandler().post(clearUser);
                            break;
                        case USING:
                            state = State.LOGOUT;
                            //Toast.makeText(context, "Logging out...", Toast.LENGTH_SHORT).show();
                            getHandler().postDelayed(clearUser, 5000);
                            break;
                    }
                }
            }
        }
    };

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, final Camera camera) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    File photo = pictureTaken(data);
                    File cropped = faceDetector.cropLargestFace(photo.getAbsolutePath());
                    if (cropped == null) {
                        Log.e(TAG, "Cannot get image");
                        startAgain();
                        return;
                    }
                    sendCropped(cropped);
                }
            });
        }
    };


    public CameraView(Context context) { this(context, null); }

    public CameraView(Context context, AttributeSet attrs) { this(context, attrs, 0); }

    // TODO: luodaan uusi cameraview activityn vaihtuessa -> täytyy tarkistaa currentuserilta onko joku aktiivisena
    public CameraView(Context context, AttributeSet attrs, int defStyleattr) {
        super(context, attrs, defStyleattr);
        holder = getHolder();
        holder.addCallback(this);
        this.context = context;
        faceDetector = new FaceDetector(context, (Activity)context);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            manualStop = true;
        }
        if (CurrentUser.getInstance().getUsername() != null) {
            state = State.USING;
        }
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
            startAgain();
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
        //if (!manualStop) {
        camera.startPreview();
        //}
        return photo;
    }

    private void sendCropped(File cropped) {
        new NaamatauluAPI(context, new UploadListener() {
            @Override
            public void onUploadCompleted(String result) {
                if (result == null) {
                    //Toast.makeText(context, "Not identified", Toast.LENGTH_LONG).show();
                    startAgain();
                    camera.startFaceDetection();
                    return;
                }
                if (CurrentUser.getInstance().processJson(result)) {
                    //Toast.makeText(context, "Recognized", Toast.LENGTH_SHORT).show();
                    state = State.RECOGNIZED;
                } else {
                    //Toast.makeText(context, "Didn't recognize you", Toast.LENGTH_SHORT).show();
                    state = State.NO_USER;
                }
                camera.startFaceDetection();
                takingphoto.set(false);
            }
        }).execute(cropped);
    }

    private void startAgain() {
        state = State.NO_USER;
        takingphoto.set(false);
        if (!manualStop) {
            camera.startPreview();
        }
        camera.startFaceDetection();
    }
}
