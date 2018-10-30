package com.futurice.android.reservator.model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.OpenCVLoader;
import org.opencv.objdetect.CascadeClassifier;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.opencv.android.CameraRenderer.LOGTAG;

public class FaceDetector {

    private Context context;
    private Activity activity;
    private NaamatauluAPI naamaTaulu = new NaamatauluAPI();

    public FaceDetector(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        if (!OpenCVLoader.initDebug()) {
            Log.e(LOGTAG, "OpenCV library not found.");
        }
    }

    // TODO remove
    // This is a debug helper function to transfer files from Download to internal storage
    public void transferImgFromDownloadToInternal (String fileName, String destFileName) {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this.activity, permissions, 0);

        String file = Environment.getExternalStorageDirectory().getAbsolutePath() +"/Download/"+fileName;
        File imgFile = new File(file);

        int size = (int) imgFile.length();
        System.out.println("size."+size);
        byte[] bytes = new byte[size];

        try {
            DataInputStream buf = new DataInputStream(new FileInputStream(imgFile));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Mat mat = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);

        Imgcodecs.imwrite(context.getFilesDir().getPath() + "/" + destFileName, mat); // TODO this is really really bad
    }

    public void cropFaces (String fileName) {
        //transferImgFromDownloadToInternal(fileName, "face.png");
        String path = context.getFilesDir().getPath() + "/face.png";
        Mat img = Imgcodecs.imread(path);

        if (img.empty()) {
            Log.e(LOGTAG, "Reading image from " + path + " failed");
            return;
        }

        MatOfRect faces = new MatOfRect();

        try {
            Mat grayscaleImage = new Mat(img.width(), img.height(), CvType.CV_8UC4);
            Imgproc.cvtColor(img, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
            String classifierPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/haarcascade_frontalface_alt.xml";
            CascadeClassifier cascadeClassifier = new CascadeClassifier(classifierPath);
            if (cascadeClassifier != null) {
                cascadeClassifier.detectMultiScale(img, faces, 1.1, 5, 2,
                        new Size(50, 50), new Size());
            } else {
                Log.e(LOGTAG, "Reading classifier file from " + classifierPath+ " failed");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Rect[] facesArray = faces.toArray();
        double largestSize = 0;
        Rect largestFace = null;
        for (int i = 0; i < facesArray.length; i++) {
            double faceSize = facesArray[i].size().height; // They are rectangles so it doesn't matter which dimension we use
            if (faceSize > largestSize) {
                largestSize = faceSize;
                largestFace = facesArray[i];
            }
            //rectangle(img, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3); // for debugging
        }

        Mat cropped = new Mat(img, largestFace);

        Imgcodecs.imwrite(context.getFilesDir().getPath() + "/" + "croppedFace.png", cropped);

        //naamaTaulu.post("/users", data);
    }

}
