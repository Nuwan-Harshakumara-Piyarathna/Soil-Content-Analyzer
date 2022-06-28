package com.example.soilcontentanalyzer;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ReportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportFragment newInstance(String param1, String param2) {
        ReportFragment fragment = new ReportFragment();
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
        View reportView = inflater.inflate(R.layout.fragment_report, container, false);
        // Inflate the layout for this fragment
        init(reportView);
        return reportView;
    }

    public void init(View view) {
        // https://stackoverflow.com/questions/18207470/adding-table-rows-dynamically-in-android
        TableLayout stk = view.findViewById(R.id.table_main);
        TableRow tbrow0 = new TableRow(getContext());
        tbrow0.setBackgroundColor(Color.parseColor("#96c896"));
//        TableLayout.LayoutParams params1 = new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
//        TableRow.LayoutParams params2 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4f);
//        TableRow.LayoutParams params3 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f);
//        tbrow0.setLayoutParams(params1);

        TextView tv0 = new TextView(getContext());
        tv0.setText(" Location ");
        tv0.setTextColor(Color.WHITE);
        tv0.setTextSize(20);
//        tv0.setLayoutParams(params2);
        tbrow0.addView(tv0);

        TextView tv1 = new TextView(getContext());
        tv1.setText("    N    ");
        tv1.setTextColor(Color.WHITE);
        tv1.setTextSize(20);
//        tv1.setLayoutParams(params3);
        tbrow0.addView(tv1);

        TextView tv2 = new TextView(getContext());
        tv2.setText("    P    ");
        tv2.setTextColor(Color.WHITE);
        tv2.setTextSize(20);
//        tv2.setLayoutParams(params3);
        tbrow0.addView(tv2);

        TextView tv3 = new TextView(getContext());
        tv3.setText("    K    ");
        tv3.setTextColor(Color.WHITE);
        tv3.setTextSize(20);
//        tv3.setLayoutParams(params3);
        tbrow0.addView(tv3);

        stk.addView(tbrow0);
        for (int i = 0; i < MainActivity.SIZE; i++) {
            TableRow tbrow = new TableRow(getContext());
            tbrow.setBackgroundColor(Color.WHITE);
            TextView t1v = new TextView(getContext());
            t1v.setText("" + MainActivity.measurements.get(i).getLocationNo());
            t1v.setTextColor(Color.BLACK);
            t1v.setGravity(Gravity.CENTER);
            t1v.setTextSize(20);
//            t1v.setLayoutParams(params2);
            tbrow.addView(t1v);
            for (int j = 0; j < 3; j++) {
                TextView tv = new TextView(getContext());
                DecimalFormat df = new DecimalFormat("#.0");
                tv.setText(df.format(MainActivity.measurements.get(i).getNPK()[j]));
                tv.setTextColor(Color.BLACK);
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(20);
//                tv.setLayoutParams(params3);
                tbrow.addView(tv);
            }
            stk.addView(tbrow);
        }

    }
}