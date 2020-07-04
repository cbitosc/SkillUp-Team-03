package com.example.cosc_project;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity2 extends AppCompatActivity {
    Button subjectbtn;
    User sub;
    static String postUrl1 = "http://cbit-qp-api.herokuapp.com/get-yearwise";//url for sending subject details goes here//
    public TextView errortext;
    private RequestQueue mQueue;
    public String text_sub;
    public Spinner spinner_sub;
    ProgressDialog loading;
    static String accessTkn;
    public static String Text_sub;
    public static ArrayList<String> dates = new ArrayList<String>();
    public static ArrayList<String> request_no = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        errortext = findViewById(R.id.errortext);
        //spinner//
        List<User> subject_list = new ArrayList<>();
        //add subjects array ka data into this spinner bro//
        try {
            for (int i = 0; i < Menu2.subjects.size(); i++) {
                sub = new User(Menu2.subjects.get(i));
                subject_list.add(sub);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        spinner_sub = findViewById(R.id.subjectsp);
        ArrayAdapter<User> adapter_sub = new ArrayAdapter<User>(this, android.R.layout.simple_spinner_item, subject_list);
        adapter_sub.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_sub.setAdapter(adapter_sub);
        spinner_sub.setPrompt("select subject");
        try {
            spinner_sub.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int p, long l) {
                    text_sub = adapterView.getItemAtPosition(p).toString();
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        subjectbtn = findViewById(R.id.subjectbtn);
        subjectbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Text_sub = text_sub;
                jsonparse();
            }
        });
    }

    public void jsonparse(){
        loading = ProgressDialog.show(this,"Please wait...","Fetching details...",false,false);
        String url=postUrl1+"?branch_name="+Menu2.Text1+"&sem_no="+Menu2.Text2+"&exam_type="+Menu2.Text3+"&subtype="+Menu2.Text4+"&subject_name="+Text_sub;

        JsonArrayRequest request= new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        String s,p;
                        loading.dismiss();
                        try {
                            for (int i = 0; i < response.length() && i<4; i++) {
                                JSONObject JO = response.getJSONObject(i);

                                s =JO.getString("date");
                                p =JO.getString("request_no");
                                dates.add(s);
                                request_no.add(p);


                            }
                            openActivity3();

                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.toString(), Toast.LENGTH_SHORT).show();
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

        mQueue = Volley.newRequestQueue(MainActivity2.this);
        mQueue.add(request);
    }


    public void openActivity3() {
        Intent intent = new Intent(this, MainActivity3.class);
        startActivity(intent);

    }
}