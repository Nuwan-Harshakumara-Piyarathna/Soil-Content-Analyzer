package com.example.soilcontentanalyzer;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.soilcontentanalyzer.Model.Measurement;
import com.example.soilcontentanalyzer.Model.Path;
import com.example.soilcontentanalyzer.utility.NetworkChangeListener;
import com.fangxu.allangleexpandablebutton.AllAngleExpandableButton;
import com.fangxu.allangleexpandablebutton.ButtonData;
import com.fangxu.allangleexpandablebutton.ButtonEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private static Context context;

    public static ArrayList<Measurement> measurements = new ArrayList<Measurement>();
    public static ArrayList<Path> paths = new ArrayList<>();
    public static Stack<LatLng> previousPointsStack = new Stack<>();
    public static ArrayList<LatLng> previousPointsList = new ArrayList<>();

    //set this CHANGED variable to true only if data (measurements and/or paths) is updated
    public static boolean CHANGED = false;
    public static int SIZE = 0;

    public static BluetoothAdapter bluetoothAdapter = null;

    public static String deviceName = null;
    public static String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;
    public static boolean isConnected = false;

    public final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    static BottomNavigationView bottomNav;

    private static final int REQUEST_ENABLE_BT = 0;

    String TAG = "MainActivity";
    LoadingDialog loadDialog;


    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);

        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to Exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //finish();
                        // Terminate Bluetooth Connection and close app
                        if (createConnectThread != null) {
                            createConnectThread.cancel();
                        }
                        finishAffinity();
                        System.exit(0);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.context = getApplicationContext();

        BluetoothManager bluetoothManager = getApplicationContext().getSystemService(BluetoothManager.class);
        MainActivity.bluetoothAdapter = bluetoothManager.getAdapter();
        if (MainActivity.bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(getApplicationContext(),"Device doesn't support Bluetooth",Toast.LENGTH_SHORT);
        }

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        final Button buttonConnect = findViewById(R.id.btn_bluetooth);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        loadDialog = new LoadingDialog(this);
        loadDialog.startLoadingDialog();
        Log.e("MainActivity", "getting Field Map");
        new Thread(new Runnable() {
            @Override
            public void run() {
                getFieldMap();
            }
        }).start();
        Log.e("MainActivity", "got the Field Map");

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new PathFragment()).commit();
        }
        installButton90to90();
//        checkForUpdates();

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.bluetoothAdapter.isEnabled()){
                    Toast.makeText(getApplicationContext(), "Turning On Bluetooth...", Toast.LENGTH_SHORT).show();
                    //intent to on bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                } else {
                    Intent intentOpenBluetoothSettings = new Intent();
                    intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                    startActivity(intentOpenBluetoothSettings);
                }
            }
        });

        MainActivity.deviceName = getIntent().getStringExtra("deviceName");
        if (MainActivity.deviceName != null){
            // Get the device address to make BT Connection
            MainActivity.deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progree and connection status
            buttonConnect.setText("Connecting to " + MainActivity.deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            Log.e("PathFragment:Bluetooth",""+"Case 7");
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            MainActivity.createConnectThread = new MainActivity.CreateConnectThread(bluetoothAdapter,MainActivity.deviceAddress);
            MainActivity.createConnectThread.start();
        }

        /*
        Second most important piece of Code. GUI Handler
         */
        MainActivity.handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case MainActivity.CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                buttonConnect.setText("Connected to " + MainActivity.deviceName);
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                break;
                            case -1:
                                buttonConnect.setText("Device fails to connect");
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                break;
                        }
                        break;

                    case MainActivity.MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        Log.e("READ_MESSAGE:",arduinoMsg);
                        if (arduinoMsg.isEmpty()) break;
                        String myMsg = "Message : " + arduinoMsg;
//                        Toast.makeText(getActivity(), myMsg,Toast.LENGTH_LONG).show();
                        String arr[] = arduinoMsg.split(",");
                        Log.e("Array", Arrays.toString(arr));
                        if (arr.length != 4) break;
                        double value_N = Double.parseDouble(arr[0]);
                        double value_P = Double.parseDouble(arr[1]);
                        double value_K = Double.parseDouble(arr[2]);
                        double value_PH = Double.parseDouble(arr[3]);
                        //TODO:Arduino Read
//                        MainActivity.measurements.add(new Measurement(MainActivity.SIZE, value_N, value_P, value_K, value_PH, MapsFragment.latitude, MapsFragment.longitude));
//                        DecimalFormat df = new DecimalFormat("#.0");
//                        if(MapsFragment.gMap != null && MapsFragment.mapHelper != null) {
//                            Marker marker = MapsFragment.mapHelper.addMarker(MapsFragment.latitude, MapsFragment.longitude, "Location " + MapsFragment.locationNo, String.format("N = %smg/kg\nP = %smg/kg\nK = %smg/kg", df.format(value_N), df.format(value_P), df.format(value_K)), false);
//                            MapsFragment.markers.add(marker);
//                        }
//                        MapsFragment.locationNo++;
//                        MainActivity.SIZE += 1;
//                        MainActivity.CHANGED = true;
                        break;
                }
            }
        };

        // Select Bluetooth Device
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MainActivity.bluetoothAdapter.isEnabled()){
                    Toast.makeText(getApplicationContext(), "Turning On Bluetooth...", Toast.LENGTH_SHORT).show();
                    //intent to on bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                } else {
                    // Move to adapter list
                    Intent intent = new Intent(getApplicationContext(), SelectDeviceActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    private void getFieldMap() {
        Log.d("Okhttp3:", "getFieldMap function called");
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String baseURL = pref.getString("baseURL", null);
        String url = baseURL + "/map/find";
        Log.e("BASE_URL", ""+baseURL);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // connect timeout
                .writeTimeout(30, TimeUnit.SECONDS) // write timeout
                .readTimeout(30, TimeUnit.SECONDS) // read timeout
                .build();

        String jwt = pref.getString("jwt", null);
        Log.d("Okhttp3:", "jwt = " + jwt);
        Request request = new Request.Builder().header("Authorization", "Bearer " + jwt).url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            Log.d("Okhttp3:", "request done, got the response");
            Log.d("Okhttp3:", response.body().toString());
            String jsonData = response.body().string();
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray pathsArray = jsonObject.getJSONArray("paths");
            JSONArray measurementsArray = jsonObject.getJSONArray("measurements");
            ArrayList<Path> loadedPaths = new ArrayList<>();
            double latitudeP1, longitudeP1, latitudeP2, longitudeP2;
            for (int i = 0; i < pathsArray.length(); i++) {
                JSONObject object1 = pathsArray.getJSONObject(i);
                latitudeP1 = object1.getDouble("latitudeP1");
                longitudeP1 = object1.getDouble("longitudeP1");
                latitudeP2 = object1.getDouble("latitudeP2");
                longitudeP2 = object1.getDouble("longitudeP2");
                Path path = new Path(latitudeP1, longitudeP1, latitudeP2, longitudeP2);
                //store the data into the array
                loadedPaths.add(path);
            }
            paths = loadedPaths;
            if (paths.size() > 0) {
                Log.e(TAG, "getFieldMap:- Paths size :" + MainActivity.paths.size());
                Log.e(TAG, "getFieldMap:- Paths :" + MainActivity.paths);
            }
            ArrayList<Measurement> loadedMeasurements = new ArrayList<>();
            int location;
            double N, P, K, PH, latitude, longitude;
            for (int i = 0; i < measurementsArray.length(); i++) {
                JSONObject object2 = measurementsArray.getJSONObject(i);
                location = object2.getInt("location");
                N = object2.getDouble("n");
                P = object2.getDouble("p");
                K = object2.getDouble("k");
                PH = object2.getDouble("ph");
                latitude = object2.getDouble("latitude");
                longitude = object2.getDouble("longitude");

                Measurement measurement = new Measurement(location, N, P, K, PH, latitude, longitude);
                //store the data into the array
                //TODO: consider this
//                loadedMeasurements.add(measurement);
            }
            measurements = loadedMeasurements;
            if (measurements.size() > 0) {
                Log.e(TAG, "getFieldMap:- Measurements size :" + MainActivity.measurements.size());
                Log.e(TAG, "getFieldMap:- Measurements :" + MainActivity.measurements);
            }
            final String toast_message;
            if (response.code() == 200) {
                toast_message = "Successfully Loaded Map data";
            } else {
                toast_message = "Unable to load Map data";
            }
            if (getApplicationContext() != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        Log.d("Okhttp3:", toast_message);
                        Toast.makeText(getApplicationContext(), toast_message, Toast.LENGTH_LONG).show();
                    }
                });
            }
            loadDialog.dismissDialog();
        } catch (IOException | JSONException e) {
            loadDialog.dismissDialog();
            this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String jwt = pref.getString("jwt", null);
        if (jwt == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
    }

    public final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.nav_path:
                            selectedFragment = new PathFragment();
                            break;
                        case R.id.nav_maps:
                            selectedFragment = new MapsFragment();
                            break;
                        case R.id.nav_report:
                            selectedFragment = new MeasurementFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
            };


    public void installButton90to90() {
        final AllAngleExpandableButton button = findViewById(R.id.button_expandable_90_90);
        final List<ButtonData> buttonDatas = new ArrayList<>();
        int[] drawable = {R.drawable.menu, R.drawable.book, R.drawable.privacy, R.drawable.ic_baseline_logout_24};
        int[] color = {R.color.textColor, R.color.blue, R.color.green, R.color.red};
        for (int i = 0; i < 4; i++) {
            ButtonData buttonData;
            if (i == 0) {
                buttonData = ButtonData.buildIconButton(getApplicationContext(), drawable[i], 15);
            } else {
                buttonData = ButtonData.buildIconButton(getApplicationContext(), drawable[i], 0);
            }
            buttonData.setBackgroundColorId(getApplicationContext(), color[i]);
            buttonData.setIconPaddingDp(15);
            buttonDatas.add(buttonData);
        }
        button.setButtonDatas(buttonDatas);
        setListener(button);
    }

    private void setListener(AllAngleExpandableButton button) {
        button.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                switch (index) {
                    case 1: {
                        Intent intent = new Intent(getApplicationContext(), InstructionsActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case 2: {
                        Intent intent = new Intent(getApplicationContext(), PrivacyPolicyActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case 3: {
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("jwt", null);
                        editor.commit();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        break;
                    }
                    default: {
                    }
                }
            }

            @Override
            public void onExpand() {
                // showToast("onExpand");
            }

            @Override
            public void onCollapse() {
                // showToast("onCollapse");
            }
        });
    }

//    private void checkForUpdates() {
//        Log.e("VERSION CODE", String.valueOf(BuildConfig.VERSION_CODE));
//        Log.e("VERSION NAME", String.valueOf(BuildConfig.VERSION_NAME));
//
//        // Instantiate the RequestQueue.
//        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
//
//        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
//        String baseURL = pref.getString("baseURL", null);
//        Log.e("BASE_URL", baseURL);
//        String url = baseURL + "/all/version/find";
//        Log.e("VERSION_URL", url);
//
//        JsonObjectRequest request = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null,
//                new com.android.volley.Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            String newerVersion = response.getString("version");
//                            Log.e("BACKEND VERSION", newerVersion);
//                            String currentVersion = String.valueOf(BuildConfig.VERSION_NAME);
//                            if (newerVersion.compareTo(currentVersion) > 0) {
//                                android.view.ContextThemeWrapper ctw = new android.view.ContextThemeWrapper(MainActivity.this, R.style.Theme_AlertDialog);
//                                final android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(ctw);
//                                alertDialogBuilder.setTitle("Update Quiz Me");
//                                alertDialogBuilder.setCancelable(false);
//                                alertDialogBuilder.setIcon(R.drawable.playstore1);
//                                alertDialogBuilder.setMessage("Quiz Me recommends that you update to the latest version for a seamless & enhanced performance of the app.");
//                                alertDialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        try {
//                                            Log.e("UPDATE TRYCATCH", "try");
//                                            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + getPackageName())));
//                                        } catch (ActivityNotFoundException e) {
//                                            Log.e("UPDATE TRYCATCH", "catch");
//                                            startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
//                                        }
//                                    }
//                                });
//                                alertDialogBuilder.show();
//
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new com.android.volley.Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//            }
//        });
//
//        // Add the request to the RequestQueue.
//        queue.add(request);
//
//    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        private String TAG = "CONNECT DEVICE";

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            Log.e("MainActivity:Bluetooth","CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address)");
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
//            if (ActivityCompat.checkSelfPermission(MainActivity.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.e("MainActivity:Bluetooth","run() 1");
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            if (ActivityCompat.checkSelfPermission(MainActivity.context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                Log.e("MainActivity:Bluetooth","case 1");
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
            bluetoothAdapter.cancelDiscovery();
            Log.e("MainActivity:Bluetooth","case 2");

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                Log.e("MainActivity:Bluetooth","case 3");
                mmSocket.connect();
                Log.e("MainActivity:Bluetooth","case 4");
                Log.e("Status", "Device connected");
                isConnected = true;
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                Log.e("MainActivity:Bluetooth","case 5");
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            Log.e("MainActivity:Bluetooth","case 6");

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
            if(connectedThread == null){
                connectedThread = new ConnectedThread(mmSocket);
                connectedThread.run();
                Log.e("MainActivity:Bluetooth","case 8");
            }
            Log.e("MainActivity:Bluetooth","case 9");
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            Log.e("MainActivity:Bluetooth","cancel()");
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.e("MainActivity:Bluetooth","ConnectedThread(BluetoothSocket socket)");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.e("MainActivity:Bluetooth","run() 2");
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            Log.e("MainActivity:Bluetooth","write(String input)"+input);
            Log.e("MainActivity:Bluetooth",""+MainActivity.deviceName);
            Log.e("MainActivity:Bluetooth",""+MainActivity.deviceAddress);
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
                Log.e("MainActivity:Bluetooth","Message sent");
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


}