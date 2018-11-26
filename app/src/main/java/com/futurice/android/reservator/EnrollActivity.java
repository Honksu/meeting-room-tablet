package com.futurice.android.reservator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurice.android.reservator.model.CreateUserTask;
import com.futurice.android.reservator.model.EnrollUserPhotoTask;
import com.futurice.android.reservator.model.FaceDetector;
import com.futurice.android.reservator.model.UploadListener;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;


public class EnrollActivity extends Activity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "EnrollActivity";
    private String photoPath;
    private File photo;

    private FaceDetector faceDetector = new FaceDetector(this, (Activity)this);

    @BindView(R.id.photoEnroll)
    ImageView photoImageView;
    @BindView(R.id.takePhotoButton)
    Button takePhotoButton;
    @BindView(R.id.enrollButton)
    Button enrollButton;
    @BindView(R.id.emailEditText)
    EditText emailEditText;

    View.OnClickListener takePhotoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dispatchTakePictureIntent();
        }
    };

    View.OnClickListener enrollOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // TODO: check that user has entered e-mail -address
            createUser();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);
        ButterKnife.bind(this);
        takePhotoButton.setOnClickListener(takePhotoOnClickListener);
        enrollButton.setOnClickListener(enrollOnClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            photo = faceDetector.cropLargestFace(photoPath);
            if (photo == null)
                return;
            setPhoto(photo.getAbsolutePath());
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
                Uri photoURI = null;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    photoURI = Uri.fromFile(photoFile);
                } else {
                    photoURI = FileProvider.getUriForFile(this, authorities, photoFile);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String imageFileName = "enrollPhoto";
        File storageDir = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        } else {
            storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        photoPath = image.getAbsolutePath();
        return image;
    }

    private void setPhoto(String croppedPhoto) {
        int targetW = photoImageView.getWidth();
        int targetH = photoImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(croppedPhoto, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(croppedPhoto, bmOptions);
        photoImageView.setImageBitmap(bitmap);
    }

    private void createUser() {
        String username = emailEditText.getText().toString();
        new CreateUserTask(this, new UploadListener() {
            @Override
            public void onUploadCompleted(String result) {
                String id = null;
                ObjectMapper mapper = new ObjectMapper();
                try {
                    JsonNode rootNode = mapper.readTree(result);
                    JsonNode idNode = rootNode.path("id");
                    id = idNode.asText();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                enrollUser(id);
            }
        }).execute(username);
    }

    private void enrollUser(String id) {
        new EnrollUserPhotoTask(this, id, new UploadListener() {
            @Override
            public void onUploadCompleted(String result) {
                // TODO: show toast that enrollment was a success
                // maybe move to personalactivity after a slight pause
            }
        }).execute(photo);
    }
}
