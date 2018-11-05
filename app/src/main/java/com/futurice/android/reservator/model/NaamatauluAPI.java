package com.futurice.android.reservator.model;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.opencv.android.CameraRenderer.LOGTAG;

public class NaamatauluAPI extends AsyncTask<File, Void, String> {

    public UploadListener delegate = null;
    private String recognizedName = "";

    static final String baseUrl = "http://api.wackymemes.com/api/v1/";
    static final String subUrl = "users/recognize/";
    OkHttpClient client = new OkHttpClient();

    public NaamatauluAPI(UploadListener response){
        delegate = response;
    }

    public String getRecognizedName () {
        return recognizedName;
    }

    @Override
    protected String doInBackground(File... file) {
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("faces", file[0].getName(),
                        RequestBody.create(MediaType.parse("image/png"), file[0]))
                .build();
        Request request = new Request.Builder().url(baseUrl + subUrl).post(formBody).addHeader("Accept", "application/json; q=0.5").build();
        Response response = null;
        try {
            response = this.client.newCall(request).execute();
        } catch (IOException e) {
            Log.e(LOGTAG, "Face upload failed: " + e);
        }
        String result = null;

        if (response.isSuccessful()) {
            try {
                result = response.body().string();
            } catch (IOException e) {
                Log.e(LOGTAG, "Invalid result: " + e);
            }
        } else {
            result = response.message();
            Log.e(LOGTAG, "Ja vihkoon...");
        }

        return result;
    }

    @Override
     protected void onPostExecute(String result) {
        delegate.onUploadCompleted(result);
    }
}