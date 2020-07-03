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

public class fetchData extends AsyncTask<Void, Void, Void> {
    String data="";
    public static String subjects[];
    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL("");//url for fetching subjects goes here//
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while (line != null){
                line=bufferedReader.readLine();
                data = data + line;
            }
            JSONArray JA = new JSONArray(data);
            subjects = new String[JA.length()];
            for (int i=0; i<JA.length(); i++){
                JSONObject JO =(JSONObject) JA.get(i);
                subjects[i] = (String) JO.get("subject_name");
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
