package com.example.soilcontentanalyzer;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Set;
import java.util.UUID;

public class PathFragment extends Fragment {
    private static final int REQUEST_ENABLE_BT = 1;

    Button btn_bluetooth;
    TextView t1;
    String address = null , name=null;

    BluetoothAdapter bluetoothAdapter = null;
    BluetoothSocket btSocket = null;
    Set<BluetoothDevice> pairedDevices;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothManager bluetoothManager = getContext().getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(getContext(),"Device doesn't support Bluetooth",Toast.LENGTH_SHORT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_path_fragment, container, false);
        Button btn_bluetooth = view.findViewById(R.id.btn_bluetooth);
        Button btn_start_walk = view.findViewById(R.id.btn_start_walk);

        btn_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);
            }
        });

        btn_start_walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(getContext(),"Connect to the Device first",Toast.LENGTH_LONG);
                    return;
                }
                MainActivity.bottomNav.setSelectedItemId(R.id.nav_maps);
            }
        });
        return view;
    }

}