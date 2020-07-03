package com.example.cosc_project;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import static android.text.TextUtils.isEmpty;

public class registration extends AppCompatActivity {

    EditText usernameEditText,passwordEditText,rollnoEditText;
    Spinner spinner1,spinner2;
    Button registerButton;
    TextView text_login;
    private RequestQueue queue;
    JsonObjectRequest objectRequest;
    JSONObject data;
    ProgressDialog loading;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        getSupportActionBar().setTitle("Registration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        text_login=(TextView)findViewById(R.id.text_login);
        text_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(registration.this, login.class);
                startActivity(loginIntent);
            }
        });

        usernameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);
        rollnoEditText = (EditText) findViewById(R.id.rollno);
        registerButton = (Button) findViewById(R.id.button_register);


        spinner1 = (Spinner) findViewById(R.id.branchsp);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.Branch, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        spinner1.setPrompt("Select Branch");

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spinner1.setSelection(i);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        spinner2 = (Spinner) findViewById(R.id.semestersp);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.Semester, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
        spinner2.setPrompt("Select Semester");
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int p, long l) {
                spinner2.setSelection(p);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });



        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Object bran = spinner1.getSelectedItem();
                Object sem = spinner2.getSelectedItem();
                String branch=bran.toString();
                String sem_no=sem.toString();
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String rollno= rollnoEditText.getText().toString().trim();

                if(isEmpty(username)||isEmpty(password)||isEmpty(rollno)||isEmpty(branch)||isEmpty(sem_no)) {
                    Toast toast = Toast.makeText(getApplicationContext(),username+password+rollno+branch+sem_no, Toast.LENGTH_LONG);
                    toast.show();
                }
                else {
                    userRegister(username,password,rollno,branch,sem_no);
                }
            }
        });

    }

    public void userRegister(String username,String password,String rollno,String branch,String sem_no) {
        String URL = "https://cbit-qp-api.herokuapp.com/user-register";
        loading = ProgressDialog.show(this, "Please wait...", "Sending details...", false, false);
        data = new JSONObject();
        try {
            data.put("uname", username);
            data.put("password", password);
            data.put("rno",rollno);
            data.put("branch_name",branch);
            data.put("sem_no", sem_no);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        queue = Volley.newRequestQueue(this);
        objectRequest = new JsonObjectRequest(Request.Method.POST,
                URL,
                data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loading.dismiss();
                        try {
                            Toast toast = Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG);
                            toast.show();
                            opennext();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Toast toast = Toast.makeText(getApplicationContext(), "Invalid credentials... Please try agian!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
        objectRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(objectRequest);
    }
    private void opennext() {
        Intent intent = new Intent(this, login.class);
        startActivity(intent);
    }


}
