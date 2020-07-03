package com.example.cosc_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class MainActivity3 extends AppCompatActivity {
    User date;
    Button date_button;
    private RequestQueue mQueue;
    public static String Request_no;
    public static String image_base64;
    static String accessTkn;
    public Spinner spinner_date;
    String postUrl2 = "http://cbit-qp-api.herokuapp.com/qpreq";//url for sending request_no goes here//
    public static String text_date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        date_button = findViewById(R.id.date_btn);
        TextView errortext = (TextView) findViewById(R.id.textView6);
        date_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int j = 0; j < MainActivity2.dates.size(); j++) {
                    if (MainActivity2.dates.get(j) == (text_date)) {
                        Request_no = MainActivity2.request_no.get(j);
                    }
                }
                jsonparse();
            }
        });
        //spinner//
        spinner_date = findViewById(R.id.spinner_date);
        List<User> date_list = new ArrayList<>();
        for (int i = 0; i < MainActivity2.dates.size(); i++) {
            date = new User(MainActivity2.dates.get(i));
            date_list.add(date);
        }
        ArrayAdapter<User> adapter_date = new ArrayAdapter<User>(this, android.R.layout.simple_spinner_item, date_list);
        adapter_date.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_date.setAdapter(adapter_date);
        spinner_date.setPrompt("select date");
        try {
            spinner_date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int p, long l) {
                    text_date = adapterView.getItemAtPosition(p).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void jsonparse(){
        String url=postUrl2+"?request_no="+Request_no;
        JsonArrayRequest request= new JsonArrayRequest(Request.Method.GET, url, null,
                new com.android.volley.Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject JO = response.getJSONObject(0);
                            image_base64 = JO.getString("image");
                            openActivity4();

                            } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }
        ){
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + accessTkn);
                return params;
            }
        };
        mQueue = Volley.newRequestQueue(MainActivity3.this);
        mQueue.add(request);
    }

    public void openActivity4() {
        Intent intent = new Intent(this, MainActivity4.class);
        startActivity(intent);

    }
}


