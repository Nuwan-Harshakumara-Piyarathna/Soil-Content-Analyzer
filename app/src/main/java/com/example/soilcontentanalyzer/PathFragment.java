package com.example.soilcontentanalyzer;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.soilcontentanalyzer.Model.Measurement;
import com.google.android.gms.maps.model.Marker;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;

public class PathFragment extends Fragment {

    private static final int REQUEST_ENABLE_BT = 0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothManager bluetoothManager = getContext().getSystemService(BluetoothManager.class);
        MainActivity.bluetoothAdapter = bluetoothManager.getAdapter();
        if (MainActivity.bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(getContext(),"Device doesn't support Bluetooth",Toast.LENGTH_SHORT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_path_fragment, container, false);
        Button btn_start_walk = view.findViewById(R.id.btn_start_walk);
        // UI Initialization
        final Button buttonConnect = view.findViewById(R.id.btn_bluetooth);
        final ProgressBar progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        final Button buttonRequestMeasurements = view.findViewById(R.id.buttonToggle);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.bluetoothAdapter.isEnabled()){
                    Toast.makeText(getContext(), "Turning On Bluetooth...", Toast.LENGTH_SHORT).show();
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

        btn_start_walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.bluetoothAdapter.isEnabled()) {
                    Toast.makeText(getContext(),"Connect to the Device first",Toast.LENGTH_LONG);
                    return;
                }
                MainActivity.bottomNav.setSelectedItemId(R.id.nav_maps);
            }
        });

        // If a bluetooth device has been selected from SelectDeviceActivity
        MainActivity.deviceName = getActivity().getIntent().getStringExtra("deviceName");
        if (MainActivity.deviceName != null){
            // Get the device address to make BT Connection
            MainActivity.deviceAddress = getActivity().getIntent().getStringExtra("deviceAddress");
            // Show progree and connection status
            buttonConnect.setText("Connecting to " + MainActivity.deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
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
                                buttonRequestMeasurements.setEnabled(true);
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
                        if (arduinoMsg.isEmpty()) break;
                        Log.e("READ_MESSAGE",arduinoMsg);
                        String myMsg = "Message : " + arduinoMsg;
//                        Toast.makeText(getActivity(), myMsg,Toast.LENGTH_LONG).show();
                        String arr[] = arduinoMsg.split(",");
                        Log.e("Array", Arrays.toString(arr));
                        if (arr.length != 3) break;
                        double value_N = Double.parseDouble(arr[0]);
                        double value_P = Double.parseDouble(arr[1]);
                        double value_K = Double.parseDouble(arr[2]);
                        MainActivity.measurements.add(new Measurement(MainActivity.SIZE, value_N, value_P, value_K, MapsFragment.latitude, MapsFragment.longitude));
                        DecimalFormat df = new DecimalFormat("#.0");
                        if(MapsFragment.gMap != null && MapsFragment.mapHelper != null) {
                            Marker marker = MapsFragment.mapHelper.addMarker(MapsFragment.latitude, MapsFragment.longitude, "Location " + MapsFragment.locationNo, String.format("N = %smg/kg\nP = %smg/kg\nK = %smg/kg", df.format(value_N), df.format(value_P), df.format(value_K)), false);
                            MapsFragment.markers.add(marker);
                        }
                        MapsFragment.locationNo++;
                        MainActivity.SIZE += 1;
                        MainActivity.CHANGED = true;
                        break;
                }
            }
        };

        // Select Bluetooth Device
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MainActivity.bluetoothAdapter.isEnabled()){
                    Toast.makeText(getContext(), "Turning On Bluetooth...", Toast.LENGTH_SHORT).show();
                    //intent to on bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                } else {
                    // Move to adapter list
                    Intent intent = new Intent(getContext(), SelectDeviceActivity.class);
                    startActivity(intent);
                }
            }
        });


        // Button to Request measurements from Arduino Device
        buttonRequestMeasurements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cmdText = "<request>";
                // Send command to Arduino board
                MainActivity.connectedThread.write(cmdText);
            }
        });

        return view;
    }



}