package com.example.soilcontentanalyzer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.soilcontentanalyzer.utility.NetworkChangeListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText userName,password;
    private TextInputLayout user,pass;
    String userText,passText;
    LoadingDialog loadDialog;
    Button button;
    TextView languageButton;
    int isSinhala = 0;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_login);


        //text field
        userName = findViewById(R.id.name);
        password = findViewById(R.id.pass);

        //layout
        user = findViewById(R.id.loginUsername);
        pass = findViewById(R.id.loginPassword);
        button = findViewById(R.id.loginBtn);
        languageButton = findViewById(R.id.changeLan);

        languageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeLanguageDialog();
            }
        });

        userName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                userText = userName.getText().toString().trim();
                user.setError(null);
                user.setErrorEnabled(false);
                if (userText.isEmpty()) {
                    user.setErrorEnabled(true);
                    user.setError("User Name can't be Empty");
                }

            }
        });

        password.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                passText = password.getText().toString().trim();
                pass.setError(null);
                pass.setErrorEnabled(false);
                if (passText.isEmpty()) {
                    pass.setErrorEnabled(true);
                    pass.setError("Password can't be Empty");
                }

            }
        });

    }

    private void showChangeLanguageDialog() {

        final String[] languages = {"English","සිංහල"};
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Change Language");
        builder.setSingleChoiceItems(languages, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    isSinhala = 0;
                    setLocate("en");
                    Log.e("lan","en");
                    recreate();
                }
                if(i == 1){
                    isSinhala = 1;
                    setLocate("si");
                    Log.e("lan","si");
                    recreate();
                }

                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setLocate(String language) {

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("My_Lang",language);
        editor.apply();

    }

    public void loadLocale(){
        SharedPreferences sharedPreferences = getSharedPreferences("Settings",Activity.MODE_PRIVATE);
        String language = sharedPreferences.getString("My_Lang","");
        setLocate(language);
    }

    public void goReg(View v){

        Intent intent = new Intent(this,SignUpActivity.class);
        this.startActivity(intent);

    }

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
    public void onBackPressed() {
        //finish();
        finishAffinity();
        System.exit(0);
    }

    private boolean validateFields() {

        userText = userName.getText().toString().trim();
        passText = password.getText().toString().trim();

        if (userText.isEmpty()) {
            user.setError(getString(R.string.username_error));
            return false;
        }

        if (passText.isEmpty()) {
            if(isSinhala == 0) {
                pass.setError("Password cannot be empty");
            }else {
                pass.setError("මුරපදය හිස් විය නොහැක");
            }
            return false;
        }

        return true;
    }

    public void submitLogin(View view){

        if(!validateFields()){
            return;
        }

        final String userName = userText;
        final String password = passText;

        loadDialog = new LoadingDialog(LoginActivity.this);
        loadDialog.startLoadingDialog();

        WebRequest webRequest = new WebRequest(this,loadDialog);
        webRequest.execute(userName,password);



    }

    private class WebRequest extends AsyncTask<String,String,String> {

        Context con;
        LoadingDialog ld;

        public WebRequest(Context con, LoadingDialog ld){
            this.con=con;
            this.ld=ld;
        }


        @Override
        protected String doInBackground(String... strings) {

            OkHttpClient client = new OkHttpClient();
            MediaType Json = MediaType.parse("application/json;charset=utf-8");
            JSONObject data = new JSONObject();
            String val = "";

            try {
                data.put("userName", strings[0]);
                data.put("password", strings[1]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("data", data.toString());

            RequestBody body = RequestBody.create(data.toString(), Json);



            SharedPreferences pref = con.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            String baseURL =pref.getString("baseURL",null);
            String url = baseURL+"/all/login";
            Log.d("URL : ",url);
            Request request = new Request.Builder().url(
                    url
            ).post(body).build();

            Response response = null;
            String responseBody = null;
            JSONObject json = null;

            try {
                response = client.newCall(request).execute();
                responseBody = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(responseBody == null){
                return "Connection Error. Try Again!";
            }
            if(responseBody.equals("user not found")){
                return "User not found";
            }
            else if(responseBody.equals("Incorrect userName or Password.")){
                return "Incorrect userName or Password.";
            }
            if(response.code()==200) {
                try {
                    json = new JSONObject(responseBody);
                    val = json.getString("jwt");
                } catch ( Exception e) {
                    e.printStackTrace();
                }
            }else{
                return null;
            }


            return val;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ld.dismissDialog();
            if(s==null){
                Toast toast;
                if(isSinhala == 0) {
                    toast=Toast.makeText(con, "Something Went Wrong. Try Again Later!", Toast.LENGTH_SHORT);
                }else {
                    toast=Toast.makeText(con, "යම් වැරැද්දක් සිදු වී ඇත. පසුව නැවත උත්සාහ කරන්න!", Toast.LENGTH_SHORT);
                }

                toast.show();
            }
            else if(s.equals("Incorrect userName or Password.")){
                Toast toast;
                if(isSinhala == 0) {
                    toast=Toast.makeText(con, "Incorrect username or Password.", Toast.LENGTH_SHORT);
                }else {
                    toast=Toast.makeText(con, "පරිශීලක නාමය හෝ මුරපදය වැරදි.", Toast.LENGTH_SHORT);
                }
                toast.show();
            }
            else if(s.equals("user not found")){
                Toast toast;
                if(isSinhala == 0) {
                    toast=Toast.makeText(con, "New User?Sign UP", Toast.LENGTH_SHORT);
                }else {
                    toast=Toast.makeText(con, "නව පරිශීලකයෙක්ද? ලියාපදිංචි වන්න.", Toast.LENGTH_SHORT);
                }

                toast.show();
            }
            else {

                try {

                    SharedPreferences pref = con.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("jwt", s);
                    editor.commit();
                    Intent intent = new Intent(con, MainActivity.class);
                    con.startActivity(intent);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }
}





