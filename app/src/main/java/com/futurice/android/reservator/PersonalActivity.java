package com.futurice.android.reservator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.futurice.android.reservator.model.FaceDetector;
import com.futurice.android.reservator.model.NaamatauluAPI;
import com.futurice.android.reservator.model.UploadListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.opencv.android.CameraRenderer.LOGTAG;

public class PersonalActivity extends Activity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String photoPath;
    private boolean photoTaken = false;

    private FaceDetector faceDetector = new FaceDetector(this, (Activity)this);

    @BindView(R.id.photo)
    ImageView photoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            photoPath = savedInstanceState.getString("photo_path");
            photoTaken = savedInstanceState.getBoolean("photo_taken");
        }
        setContentView(R.layout.activity_personal);
        ButterKnife.bind(this);
        if (!photoTaken) {
            dispatchTakePictureIntent();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (photoPath != null) {
            outState.putString("photo_path", photoPath);
        }
        outState.putBoolean("photo_taken", photoTaken);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //faceDetector.transferImgFromDownloadToInternal(photoPath, "face.png");
            File img = faceDetector.cropLargestFace(photoPath);
            new NaamatauluAPI(new UploadListener() {
                @Override
                public void onUploadCompleted(String result) {
                    Log.d(LOGTAG, "Hello, " + result);
                    setPhoto();
                }
            }).execute(img);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String authorities = getApplicationContext().getPackageName() + ".fileprovider";
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, authorities,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String imageFileName = "FREC_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        photoPath = image.getAbsolutePath();
        return image;
    }

    private void setPhoto() {
        int targetW = photoImageView.getMeasuredWidth();
        int targetH = photoImageView.getMeasuredHeight();

        if (targetH == 0 || targetW == 0) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            targetH = metrics.heightPixels;
            targetW = metrics.widthPixels;
        }

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        photoImageView.setImageBitmap(bitmap);
    }
}
