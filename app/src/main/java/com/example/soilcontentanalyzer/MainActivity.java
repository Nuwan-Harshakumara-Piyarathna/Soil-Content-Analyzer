package com.example.soilcontentanalyzer;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.soilcontentanalyzer.helper.Measurement;
import com.example.soilcontentanalyzer.utility.NetworkChangeListener;
import com.fangxu.allangleexpandablebutton.AllAngleExpandableButton;
import com.fangxu.allangleexpandablebutton.ButtonData;
import com.fangxu.allangleexpandablebutton.ButtonEventListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    public static ArrayList<Measurement> measurements = new ArrayList<>();
    public static int SIZE = 0;

    static BottomNavigationView bottomNav;



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

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);


        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new PathFragment()).commit();
        }
        installButton90to90();

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String jwt = pref.getString("jwt",null);
        if(jwt==null){
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
                            selectedFragment = new ReportFragment();
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

}