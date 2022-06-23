package com.example.soilcontentanalyzer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.soilcontentanalyzer.utility.NetworkChangeListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {

    int status;
    CountDownTimer timer;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(networkChangeListener);
        super.onDestroy();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void registerConnectivityNetworkMonitorForAPI21AndUp() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.registerNetworkCallback(
                    builder.build(),
                    new ConnectivityManager.NetworkCallback() {
                        /**
                         * @param network
                         */
                        @Override
                        public void onAvailable(Network network) {

                            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                            String token = pref.getString("jwt", null);
                            if (token != null) {
                                WebRequest webRequest = new WebRequest(getApplicationContext());
                                webRequest.execute();
                            }else {
                                status = 0;
                                timer.start();
//                                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
//                                startActivity(intent);
                            }
                        }

                        /**
                         * @param network
                         */
                        @Override
                        public void onLost(Network network) {


                        }
                    }

            );
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        timer = new CountDownTimer(6000, 15) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                Intent intent;
                if (status == 0) {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);

                } else {
                    intent = new Intent(SplashActivity.this, MainActivity.class);

                }
                startActivity(intent);
                finish();

            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);

        registerConnectivityNetworkMonitorForAPI21AndUp();


        status = 0;

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("baseURL", "https://soilanalyzer.herokuapp.com");
        editor.commit();



        String token = pref.getString("jwt", null);
        if (token != null && isNetworkConnected()) {
            WebRequest webRequest = new WebRequest(getApplicationContext());
            webRequest.execute();
        }

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent loginIntent = new Intent(SplashActivity.this,LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        },3500);*/
    }

    private class WebRequest extends AsyncTask<String, String, String> {

        Context con;


        public WebRequest(Context con) {
            this.con = con;

        }


        @Override
        protected String doInBackground(String... strings) {

            OkHttpClient client = new OkHttpClient();
            MediaType Json = MediaType.parse("application/json;charset=utf-8");
            JSONObject data = new JSONObject();
            String val = "";
            SharedPreferences pref = con.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            String baseURL = pref.getString("baseURL", null);
            String token = pref.getString("jwt", null);
            Log.i("token", token);

            try {
                data.put("jwt", token);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("data", data.toString());

            RequestBody body = RequestBody.create(data.toString(), Json);


            String url = baseURL + "/all/is_token_expired";
            Log.d("URL : ", url);
            Request request = new Request.Builder().url(
                    url
            ).addHeader("Authorization", "Bearer " + token).post(body).build();

            Response response = null;
            String responseBody = null;
            JSONObject json = null;

            try {
                response = client.newCall(request).execute();
                responseBody = response.body().string();
                val = responseBody;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if ((response != null) && response.code() == 200) {
                try {
                    //json = new JSONObject(responseBody);
                    //val = json.getString("jwt");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return null;
            }


            return val;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                Log.i("message", "Something Went Wrong Try Again Later!");

            } else if (s.equals("false")) {
                Log.i("loginStatus", s);

                status = 1;
            } else if (s.equals("true")) {
                Log.i("loginStatus", s);
            }

            timer.start();

        }


    }
}