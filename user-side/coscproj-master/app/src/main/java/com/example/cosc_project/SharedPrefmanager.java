package com.example.cosc_project;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SharedPrefmanager {
    public static String KEY_USERNAME = null;
    private static final String SHARED_PREF_NAME = "Userdata";
    private static final String KEY_PASSWORD =null;
    public static String KEY_BRANCH=null;
    public static String KEY_ROLLNO=null;
    public static String KEY_SEM=null;

    private static SharedPrefmanager mInstance;
    private static Context mCtx;
    private SharedPrefmanager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefmanager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefmanager(context);
        }
        return mInstance;
    }
    public void userLogin(User user) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_PASSWORD, user.getPassword());
        editor.putString(KEY_BRANCH,user.getBranch());
        editor.putString(KEY_SEM,user.getSem());
        editor.apply();
    }

    //this method will checker whether user is already logged in or not
    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, null) != null;
    }

    //this method will give the logged in user
    public User getUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return new User(
                sharedPreferences.getString(KEY_USERNAME, null),
                sharedPreferences.getString(KEY_PASSWORD, null),
                sharedPreferences.getString(KEY_BRANCH, null),
                sharedPreferences.getString(KEY_SEM, null)
        );
    }

    //this method will logout the user
    public static void logout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        mCtx.startActivity(new Intent(mCtx,MainActivity.class));
    }
}
