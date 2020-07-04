package com.example.cosc_project;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Menu2 extends Fragment {

    @Nullable
    static String postUrl = "http://cbit-qp-api.herokuapp.com/get-subjects";//url for sending branch details go here//
    public String text1, text2, text3, text4;
    public static String Text1, Text2, Text3, Text4;
    public Spinner spinner1, spinner2, spinner3, spinner4;
    public static String text;
    static String accessTkn;
    private RequestQueue mQueue;
    ProgressDialog loading;
    public static ArrayList<String> subjects = new ArrayList<String>();

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments

        View view = inflater.inflate(R.layout.fragment_menu_2, container, false);

        spinner1 = view.findViewById(R.id.branchsp);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getContext(), R.array.branch, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        spinner1.setPrompt("select branch");
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int p, long l) {
                spinner1.setSelection(p, false);
                text1 = adapterView.getItemAtPosition(p).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinner2 = (Spinner) view.findViewById(R.id.semestersp);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getContext(), R.array.semester, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
        spinner2.setPrompt("select semester");
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int p, long l) {
                spinner2.setSelection(p, false);
                text2 = adapterView.getItemAtPosition(p).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        spinner3 = (Spinner) view.findViewById(R.id.examtypesp);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(getContext(), R.array.examtype, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter3);
        spinner3.setPrompt("select exam-type");
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int p, long l) {
                spinner3.setSelection(p, false);
                text3 = adapterView.getItemAtPosition(p).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        spinner4 = (Spinner) view.findViewById(R.id.subtypesp);
        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(getContext(), R.array.subtype, android.R.layout.simple_spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner4.setAdapter(adapter4);
        spinner4.setPrompt("select sub-type");
        spinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int p, long l) {
                spinner4.setSelection(p, false);
                text4 = adapterView.getItemAtPosition(p).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Button button1 = view.findViewById(R.id.viewbtn);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Text1 = text1;
                Text2 = text2;
                Text3 = text3;
                Text4 = text4;
                jsonparse();
            }
        });

        return view;

    }
    public void jsonparse(){
        loading = ProgressDialog.show(getContext(),"Please wait...","Fetching details...",false,false);
        String url=postUrl+"?branch_name="+Text1+"&sem_no="+Text2+"&exam_type="+Text3+"&subtype="+Text4;

        JsonArrayRequest request= new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        loading.dismiss();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject JO = response.getJSONObject(i);
                                String s = JO.getString("subject_name");
                                subjects.add(s);
                            }
                            openActivity2();
                            if (subjects.size()==0) {
                                Toast toast = Toast.makeText(getContext().getApplicationContext(), "No subjects available for the selected details!", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast toast = Toast.makeText(getContext().getApplicationContext(), error.toString(), Toast.LENGTH_LONG);
                toast.show();
            }
        }
        ){
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + accessTkn);
                return params;
            }
        };

        mQueue = Volley.newRequestQueue(getContext());
        mQueue.add(request);
    }

    public void openActivity2() {
        Intent intent = new Intent(getContext().getApplicationContext(), MainActivity2.class);
        startActivity(intent);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Search Question Papers");
    }

}
