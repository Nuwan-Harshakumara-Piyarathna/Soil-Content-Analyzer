package com.example.soilcontentanalyzer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.soilcontentanalyzer.adapters.MeasurementAdapter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeasurementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeasurementFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RecyclerView recyclerView;
    MeasurementAdapter measurementAdapter;
    Button btn_clear_all;
    LoadingDialog loadDialog;

    public MeasurementFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MeasurementFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MeasurementFragment newInstance(String param1, String param2) {
        MeasurementFragment fragment = new MeasurementFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_measurement, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_measured_values);
        measurementAdapter = new MeasurementAdapter(getContext());
        recyclerView.setAdapter(measurementAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        btn_clear_all = view.findViewById(R.id.btn_clear_all);
        if(MainActivity.measurements.size() > 0) {
            btn_clear_all.setVisibility(View.VISIBLE);
        }

        btn_clear_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Clearing all map data", Toast.LENGTH_SHORT);
                    loadDialog = new LoadingDialog(getActivity());
                    loadDialog.startLoadingDialog();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            doDeleteRequest();
                        }
                    }).start();
            }
        });
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void doDeleteRequest() {
        Log.d("Okhttp3:", "doDeleteRequest function called");
        SharedPreferences pref = getContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String baseURL =pref.getString("baseURL",null);
        String url = baseURL + "/map/delete";

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // connect timeout
                .writeTimeout(30, TimeUnit.SECONDS) // write timeout
                .readTimeout(30, TimeUnit.SECONDS) // read timeout
                .build();

        String jwt = pref.getString("jwt", null);
        Log.d("Okhttp3:", "jwt = " + jwt);
        Request request = new Request.Builder().header("Authorization", "Bearer " + jwt).url(url).delete().build();

        try (Response response = client.newCall(request).execute()) {
            Log.d("Okhttp3:", "request done, got the response");
            Log.d("Okhttp3:", String.valueOf(response.body()));
            final String toast_message;
            loadDialog.dismissDialog();
            if (response.code() == 200){
                toast_message = "Successfully cleared Map data";
                MainActivity.measurements.clear();
                MainActivity.paths.clear();
                MainActivity.CHANGED = false;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_clear_all.setVisibility(View.GONE);
                        measurementAdapter.clearAll();
                    }
                });
            }
            else {
                Log.e("Okhttp3:", String.valueOf(response.body()));
                toast_message = "Something Went Wrong ";
            }
            if (getContext() != null) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        Log.d("Okhttp3:", toast_message);
                        Toast.makeText(getContext(), toast_message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (IOException e) {
            loadDialog.dismissDialog();
            Toast.makeText(getContext(), "Something Went Wrong", Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }

}