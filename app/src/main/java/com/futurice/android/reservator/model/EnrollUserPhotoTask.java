package com.futurice.android.reservator.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.futurice.android.reservator.common.PreferenceManager;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EnrollUserPhotoTask extends AsyncTask<File, Void, String> {
    private static final String TAG = "EnrollUserPhotoTask";
    public UploadListener delegate = null;
    private Context context;
    private String id;

    static final String subUrl = "users/";
    OkHttpClient client = new OkHttpClient();

    public EnrollUserPhotoTask(Context context, String id, UploadListener response) {
        this.context = context;
        this.id = id;
        delegate = response;
    }

    @Override
    protected String doInBackground(File... files) {
        PreferenceManager preferences = PreferenceManager.getInstance(context);
        String url = preferences.getBaseUrl() + subUrl + id + "/enroll/";

        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("faces", files[0].getName(),
                        RequestBody.create(MediaType.parse("image/png"), files[0]))
                .build();
        Request request = new Request.Builder().url(url).post(formBody)
                .addHeader("Accept", "application/json; q=0.5")
                .addHeader("Authorization", "Token " + preferences.getToken()).build();
        Response response = null;
        try {
            response = this.client.newCall(request).execute();
        } catch (IOException e) {
            Log.e(TAG, "Face upload failed: " + e);
        }

        String result = null;
        if (response.isSuccessful()) {
            try {
                result = response.body().string();
            } catch (IOException e) {
                Log.e(TAG, "Invalid result: " + e);
            }
            return result;
        } else {
            result = response.message();
            Log.e(TAG, "Face upload failed: " + result);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.onUploadCompleted(result);
    }
}
