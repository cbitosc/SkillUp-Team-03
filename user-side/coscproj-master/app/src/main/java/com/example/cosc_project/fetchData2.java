package com.example.cosc_project;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class fetchData2 extends AsyncTask<Void, Void, Void> {
    String data="";
    public static String dates[];
    public static String request_no[];
    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL("");//url for fetching dates, request_no goes here//
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while (line != null){
                line=bufferedReader.readLine();
                data = data + line;
            }
            JSONArray JA = new JSONArray(data);
            dates = new String[JA.length()];
            for (int i=0; i<JA.length() && i<4; i++){
                JSONObject JO =(JSONObject) JA.get(i);
                dates[i] = (String) JO.get("date");
                request_no[i] = (String) JO.get("request_no");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
