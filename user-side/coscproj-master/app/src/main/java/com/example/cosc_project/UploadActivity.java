package com.example.cosc_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {


    public static String request;
    ImageView imageView;
    Button upload;
    Button edit;
    Button delete;
    static String accessTkn;
    TextView uploadStatus;


    private RequestQueue queue;
    ArrayList<String> requests;
    ArrayList<String> branch;
    ArrayList<String> sem;
    ArrayList<String> exam_name;
    ArrayList<String> type;
    ArrayList<String> subtype;

    String username=SharedPrefmanager.KEY_USERNAME;
    int Uploadflag;
    ProgressDialog loading;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        getSupportActionBar().setTitle("Upload Question Paper");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        upload=(Button)findViewById(R.id.upload);
        edit=(Button)findViewById(R.id.edit);
        delete=(Button)findViewById(R.id.delete);
        uploadStatus=(TextView)findViewById(R.id.uploadstatus);

        getUploadStatus();

        imageView=(ImageView)findViewById(R.id.imageView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            finish();
            startActivity(intent);
            return;
        }

        findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 100);
            }
        });

        findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 100);
            }
        });

        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteImage();
            }
        });

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            uploadBitmap(bitmap);
        }
    }


    private void uploadBitmap(final Bitmap bitmap) {

        loading = ProgressDialog.show(this,"Please wait...","Uploading...",false,false);
        String url;
        if(Uploadflag==1) {
            url = "http://cbit-qp-api.herokuapp.com/qpreq";
        }else{
            url = "http://cbit-qp-api.herokuapp.com/qp-update";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        JSONObject details=new JSONObject();
        try{
            details.put("request_no",request);
            details.put("image",imageString);
            details.put("uname",username);
        }catch(JSONException e){
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,details,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loading.dismiss();
                        if(Uploadflag==1) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Upload Successfull!!", Toast.LENGTH_LONG);
                            toast.show();
                        }else{
                            Toast toast = Toast.makeText(getApplicationContext(), "Edit Successfull!!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        imageView.setImageBitmap(bitmap);
                        getUploadStatus();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();

                        //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-type", "Application/json ");
                params.put("Authorization", "Bearer "+accessTkn);
                return params;
            }

        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(this).add(request);
    }


    private void deleteImage(){
        String url="http://cbit-qp-api.herokuapp.com/qp-delete";
        JSONObject data=new JSONObject();
        try {
            data.put("request_no", request);
            data.put("uname", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast toast = Toast.makeText(getApplicationContext(),"Delete Successfull!", Toast.LENGTH_LONG);
                        toast.show();
                        getUploadStatus();
                        imageView.setImageBitmap(null);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Authorization", "Bearer "+accessTkn);
                return headers;
            }

        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(this).add(request);


    }

    public void getUploadStatus(){
        //loading = ProgressDialog.show(this,"Please wait...","getting details...",false,false);
        String url="http://cbit-qp-api.herokuapp.com/get-uploads";
        String finalurl=url+"?request_no="+request+"&uname="+username;

        StringRequest request = new StringRequest(Request.Method.GET, finalurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            //loading.dismiss();
                            JSONArray response = new JSONArray(s);

                            if (response.length()>0){//already uploaded
                                Uploadflag=0;
                            }
                            else{
                                Uploadflag=1;
                            }

                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        if (Uploadflag == 0) {//already uploaded
                            upload.setEnabled(false);
                            upload.setBackgroundColor(getResources().getColor(R.color.GRAY));

                            delete.setEnabled(true);
                            edit.setEnabled(true);
                            edit.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                            delete.setBackgroundColor(getResources().getColor(R.color.colorAccent));

                            uploadStatus.setText("Already Uploaded!");


                        } else {
                            delete.setEnabled(false);
                            edit.setEnabled(false);

                            upload.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                            edit.setBackgroundColor(getResources().getColor(R.color.GRAY));
                            delete.setBackgroundColor(getResources().getColor(R.color.GRAY));

                            upload.setEnabled(true);
                            uploadStatus.setText("Didn't upload yet!");
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {


                public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer "+accessTkn);
                return params;
            }

        };


        Volley.newRequestQueue(this).add(request);

    }
}