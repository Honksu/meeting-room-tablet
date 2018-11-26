package com.futurice.android.reservator.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.futurice.android.reservator.common.PreferenceManager;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateUserTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "CreateUserTask";
    public UploadListener delegate = null;
    private Context context;
    static final String subUrl = "users/";
    OkHttpClient client = new OkHttpClient();
    PreferenceManager preferences;

    public CreateUserTask(Context context, UploadListener response) {
        this.context = context;
        delegate = response;
    }

    @Override
    protected String doInBackground(String... strings) {
        preferences = PreferenceManager.getInstance(context);
        String url = preferences.getBaseUrl() + subUrl;
        RequestBody formBody = new FormBody.Builder()
                .add("username", strings[0]).build();
        Request request = new Request.Builder()
                .url(url).post(formBody)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Token " + preferences.getToken()).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            Log.e(TAG, "User creation failed");
        }
        String result = null;
        if (response.isSuccessful()) {
            Log.d(TAG, "User created");
            try {
                result = response.body().string();
            } catch (IOException e) {
                Log.e(TAG, "Invalid result");
            }
            return result;
        } else {
            result = response.message();
            Log.e(TAG, "User creation failed: " + result);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.onUploadCompleted(result);
    }
}
