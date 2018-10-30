package com.futurice.android.reservator.model;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class NaamatauluAPI {

    static final String baseUrl = "https://naamataulu-backend.herokuapp.com/api/v1/";

    public NaamatauluAPI(){

    }

    public void post(String subUrl, String data) {
        String urlString = baseUrl + subUrl; // URL to call
        OutputStream out = null;

        try {
            URL url = new URL(urlString); //in the real code, there is an ip and a port
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("username", "testi");
            jsonParam.put("face_features", "testi");
            jsonParam.put("face_recognition_implementer", "testi");

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));

            os.flush();
            os.close();

            conn.disconnect();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}