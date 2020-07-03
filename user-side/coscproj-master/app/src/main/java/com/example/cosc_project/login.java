package com.example.cosc_project;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

public class login extends AppCompatActivity {
    Button login;
    EditText username, password;
    private TextView textView;
    private RequestQueue queue;
    JsonObjectRequest objectRequest;
    private String user;
    private String passw;
    String accessTkn;
    ProgressDialog loading;
    TextView usernameProfile;
    TextView rollnoProfile;
    TextView branchProfile;


    JSONObject data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        login = (Button) findViewById(R.id.login);
        username = (EditText) findViewById(R.id.user);
        password = (EditText) findViewById(R.id.pass);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogin();
                queue.add(objectRequest);
            }
        });
        textView = (TextView) findViewById(R.id.register);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register = new Intent(login.this, registration.class);
                startActivity(register);
            }
        });
    }
    public void showLogin() {
        loading = ProgressDialog.show(this, "Please wait...", "Logging in...", false, false);
        user = username.getText().toString();
        passw = password.getText().toString();
        if (isEmpty(user) || isEmpty(passw)) {
            Toast toast = Toast.makeText(getApplicationContext(), user + passw, Toast.LENGTH_LONG);
            toast.show();
        }
        String URL = "https://cbit-qp-api.herokuapp.com/user-login";
        data = new JSONObject();
        try {
            data.put("uname", user);
            data.put("password", passw);
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
                        try {

                            accessTkn = response.getString("access_token");
                            Menu1.accessTkn = accessTkn;
                            MainPage.accessTkn=accessTkn;
                            UploadActivity.accessTkn = accessTkn;
                            MainActivity2.accessTkn=accessTkn;
                            MainActivity3.accessTkn=accessTkn;
                            Menu2.accessTkn=accessTkn;
                            SharedPrefmanager.KEY_USERNAME = user;
                            SharedPrefmanager.KEY_BRANCH=response.getString("branch_name");
                            SharedPrefmanager.KEY_SEM=response.getString("sem_no");
                            loading.dismiss();
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
                        Toast toast = Toast.makeText(getApplicationContext(), "Login Failed...Invalid credentials", Toast.LENGTH_LONG);
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
        Intent intent = new Intent(this, MainPage.class);
        startActivity(intent);
    }



}
