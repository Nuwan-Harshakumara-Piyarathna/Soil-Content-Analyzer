package com.example.soilcontentanalyzer;

import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;


import android.view.View;
import android.widget.Toast;

import com.example.soilcontentanalyzer.utility.NetworkChangeListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText fName,lName,userName,passwordOne,passwordTwo;
    private TextInputLayout fstName,lstName,user,passOne,passTwo;
    String fNameText,lNameText,userText,passText,cPassText;
    LoadingDialog loadDialog;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,filter);

        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);

        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //text field
        fName = findViewById(R.id.firstName);
        lName = findViewById(R.id.lastName);
        userName =findViewById(R.id.name);
        passwordOne = findViewById(R.id.passOne);
        passwordTwo = findViewById(R.id.passTwo);

        //layout field
        fstName = findViewById(R.id.fname);
        lstName = findViewById(R.id.lname);
        user = findViewById(R.id.loginUsername);
        passOne =findViewById(R.id.loginPassword);
        passTwo = findViewById(R.id.confirmPassword);
        loadDialog = new LoadingDialog(SignUpActivity.this);


        fName.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                fNameText = fName.getText().toString().trim();
                fstName.setError(null);
                fstName.setErrorEnabled(false);
                if(fNameText.isEmpty()) {
                    fstName.setErrorEnabled(true);
                    fstName.setError("First Name can't be Empty");
                }else if(fNameText.length()<4 || fNameText.length()>30) {
                    fstName.setErrorEnabled(true);
                    fstName.setError("First Name should have 4-30 characters");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });


        lName.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                lNameText = lName.getText().toString().trim();
                lstName.setError(null);
                lstName.setErrorEnabled(false);
                if(lNameText.isEmpty()) {
                    lstName.setErrorEnabled(true);
                    lstName.setError("Last Name can't be Empty");
                }else if(lNameText.length()<4 || lNameText.length()>30) {
                    lstName.setErrorEnabled(true);
                    lstName.setError("Last Name should have 4-30 characters");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        userName.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                userText =  userName.getText().toString().trim();
                user.setError(null);
                user.setErrorEnabled(false);
                if(userText.isEmpty()) {
                    user.setErrorEnabled(true);
                    user.setError("User Name can't be Empty");
                }else if(userText.length()<4 || userText.length()>30) {
                    user.setErrorEnabled(true);
                    user.setError("User Name should have 4-30 characters");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        passwordOne.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                passText = passwordOne.getText().toString().trim();
                passOne.setError(null);
                passOne.setErrorEnabled(false);
                if(passText.isEmpty()) {
                    passOne.setErrorEnabled(true);
                    passOne.setError("Password can't be empty");
                }else if(passText.length()<5 || passText.length()>50){
                    passOne.setErrorEnabled(true);
                    passOne.setError("Password is too short");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        passwordTwo.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                cPassText = passwordTwo.getText().toString().trim();
                passTwo.setError(null);
                passTwo.setErrorEnabled(false);
                if(cPassText.isEmpty()){
                    passTwo.setErrorEnabled(true);
                    passTwo.setError("Confirm Password can't be empty");
                }else if(! passText.equals(cPassText)){
                    passTwo.setErrorEnabled(true);
                    passTwo.setError("Password is not matched");
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });




    }

    private boolean validateFields(){
        fNameText = fName.getText().toString().trim();
        lNameText = lName.getText().toString().trim();
        userText =  userName.getText().toString().trim();
        passText = passwordOne.getText().toString().trim();
        cPassText = passwordTwo.getText().toString().trim();

        if(fNameText.isEmpty()){
            fstName.setError("First Name can't be Empty");
            return false;

        }else if(fNameText.length()<4 || fNameText.length()>30){
            fstName.setError("First Name should have 4-30 characters");
            return false;
        }

        if(lNameText.isEmpty()){
            lstName.setError("Last Name can't be Empty");
            return false;
        }else if(lNameText.length()<4 || lNameText.length()>30){
            lstName.setError("Last Name should have 4-30 characters");
            return false;
        }

        if(userText.isEmpty()){
            user.setError("User Name can't be Empty");
            return false;

        }else if(userText.length()<4 || userText.length()>30){
            user.setError("User Name should have 4-30 characters");
            return false;
        }

        if(passText.isEmpty()){
            passOne.setError("Password can't be empty");
            return false;
        }else if(passText.length()<5 || passText.length()>50){
            passOne.setError("Password is too short");
            return false;
        }

        if(cPassText.isEmpty()){
            passTwo.setError("Confirm Password can't be empty");
            return false;
        }else if(! passText.equals(cPassText)){
            passTwo.setError("Password is not matched");
            return false;
        }

        return true;
    }
    public void submitReg(View view) {

        if (!validateFields()) {
            return;
        }

        final String userName = userText;
        final String password = passText;
        final String fName = fNameText;
        final String lName = lNameText;

        loadDialog = new LoadingDialog(SignUpActivity.this);
        loadDialog.startLoadingDialog();

        WebRequest webRequest = new WebRequest(this,loadDialog);
        webRequest.execute(userName, password, fName, lName);
    }

    private class WebRequest extends AsyncTask<String,String,String> {

        LoadingDialog ld;
        Context con;

        public WebRequest(Context con, LoadingDialog ld){
            this.con=con;
            this.ld=ld;
        }


        @Override
        protected String doInBackground(String... strings) {

            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            String baseURL =pref.getString("baseURL",null);
            String url = baseURL + "/all/registration";

            OkHttpClient client = new OkHttpClient();
            MediaType Json = MediaType.parse("application/json;charset=utf-8");
            JSONObject data = new JSONObject();
            String val = "";

            try {
                data.put("userName", strings[0]);
                data.put("password", strings[1]);
                data.put("firstName", strings[2]);
                data.put("lastName", strings[3]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("data", data.toString());

            RequestBody body = RequestBody.create(data.toString(), Json);

            Request request = new Request.Builder().url(
                    url
            ).post(body).build();

            Response response = null;
            String responseBody = null;

            try {
                response = client.newCall(request).execute();
                responseBody = response.body().string();

            } catch (IOException e) {

                e.printStackTrace();
            }

            Log.i("res", responseBody);
            if (response.code() == 200) {

                if (responseBody.equals("userName is already exist")) {
                    return "UserName exists";
                } else {
                    return "OK";
                }

            }


            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ld.dismissDialog();
            if (s == null) {
                Toast toast = Toast.makeText(con, "Something Went Wrong Try Again Later!", Toast.LENGTH_SHORT);
                toast.show();
            } else if (s.equals("UserName exists")) {
                Toast toast = Toast.makeText(con, "User Name is already taken try with different User Name!", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                try {
                    Toast toast=Toast.makeText(con, "Registered Successfully", Toast.LENGTH_SHORT);
                    toast.show();
                    Intent intent = new Intent(con, LoginActivity.class);
                    con.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}





