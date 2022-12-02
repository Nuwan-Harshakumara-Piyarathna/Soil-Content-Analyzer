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



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_path_fragment, container, false);
        Button btn_start_walk = view.findViewById(R.id.btn_start_walk);
        // UI Initialization


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

        return view;
    }



}