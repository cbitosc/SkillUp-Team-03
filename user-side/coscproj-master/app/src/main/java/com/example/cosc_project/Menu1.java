package com.example.cosc_project;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Menu1 extends Fragment {

    @Nullable

    ArrayList<DataModel> dataModels;
    ProgressDialog loading;
    String b,s,e,t,sub,r,d;
    String branch_name=SharedPrefmanager.KEY_BRANCH;
    String sem_no=SharedPrefmanager.KEY_SEM;
    ListView myListView;
    private RequestQueue queue;
    static String accessTkn;
    JSONObject data;
    int flag=0;
    List<String> requests=new ArrayList<String>();
    List<String> branch=new ArrayList<String>();
    List<String> sem=new ArrayList<String>();
    List<String> exam_name=new ArrayList<String>();
    List<String> type=new ArrayList<String>();
    List<String> subtype=new ArrayList<String>();
    List<String> details=new ArrayList<String>();

    private static CustomAdapter adapter;
    private static final String URL="http://cbit-qp-api.herokuapp.com/get-active-exams?sem_no=";

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu_1, container, false);


        dataModels=new ArrayList<>();


        String url=URL+sem_no+"&"+"branch_name="+branch_name;
        myListView = (ListView) view.findViewById(R.id.myListView);
        //loading = ProgressDialog.show(getContext(),"Please wait...","Fetching...",false,false);
        StringRequest stringRequest= new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String st) {
                        //loading.dismiss();
                        int flag=0;
                        String currentTime,currentDate;
                        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
                        SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm:ss");

                        currentDate=dateFormat.format(new Date());
                        currentTime=timeFormat.format(new Date());

                        try {
                            final JSONArray response = new JSONArray(st);
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject active_exam = response.getJSONObject(i);
                                String date = active_exam.getString("date");
                                String end_at = active_exam.getString("end_at");

                                if (date.compareTo(currentDate)<0) {
                                        b = active_exam.getString("branch_name");
                                        s = active_exam.getString("sem_no");
                                        e = active_exam.getString("subject_name");
                                        t = active_exam.getString("exam_type");
                                        sub = active_exam.getString("subtype");
                                        requests.add(active_exam.getString("request_no"));
                                        d = t + " , " + sub + " , " + b + " , " + "SEM : " + s;
                                        dataModels.add(new DataModel(e, d));
                                }
                                else if(date.compareTo(currentDate)==0){
                                    if(end_at.compareTo(currentTime)<=0){
                                        b = active_exam.getString("branch_name");
                                        s = active_exam.getString("sem_no");
                                        e = active_exam.getString("subject_name");
                                        t = active_exam.getString("exam_type");
                                        sub = active_exam.getString("subtype");
                                        requests.add(active_exam.getString("request_no"));
                                        d = t + " , " + sub + " , " + b + " , " + "SEM : " + s;
                                        dataModels.add(new DataModel(e, d));
                                    }
                                    else{
                                        Date reference = timeFormat.parse(currentTime);
                                        Date compare = timeFormat.parse(end_at);
                                        int seconds = Math.toIntExact((compare.getTime() - reference.getTime()) / 1000L);
                                        scheduleNotification(getNotification(active_exam.getString("subject_name")),seconds*1000);

                                    }
                                }

                            }

                            adapter= new CustomAdapter(dataModels,getContext().getApplicationContext());
                            myListView.setAdapter(adapter);

                        if(response.length()==0) {
                            Toast toast = Toast.makeText(getContext().getApplicationContext(), "No Active Exams", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(getContext().getApplicationContext(), error.toString(), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }) {

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer "+accessTkn);
                return params;
            }

        };
        queue = Volley.newRequestQueue(getContext());
        queue.add(stringRequest);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                DataModel dataModel= dataModels.get(position);

                Intent Upload = new Intent(getContext().getApplicationContext(), com.example.cosc_project.UploadActivity.class);
                Upload.putExtra("com.example.cosc_project.ITEM_INDEX", position);
                UploadActivity.request=requests.get(position);
                startActivity(Upload);
            }
        });
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Dashboard");
    }

    private void scheduleNotification(Notification notification, int delay) {

        Intent notificationIntent = new Intent(getContext(), NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String content) {
        String id="channel 1";
        NotificationManager mNotificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id,
                    "Channel Name",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext().getApplicationContext(), id)
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle("Upload Question Paper") // title for notification
                .setContentText(content)// message for notification
                .setAutoCancel(true); // clear notification after click
        //Intent intent = new Intent(getContext().getApplicationContext(),MainActivity.class);
        //PendingIntent pi = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //mBuilder.setContentIntent(pi);
        //mNotificationManager.notify(0, mBuilder.build());
        return mBuilder.build();
    }


}
