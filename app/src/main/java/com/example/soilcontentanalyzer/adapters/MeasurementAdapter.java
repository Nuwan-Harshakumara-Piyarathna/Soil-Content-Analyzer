package com.example.soilcontentanalyzer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soilcontentanalyzer.Model.MeasurementModel;
import com.example.soilcontentanalyzer.R;

import java.util.List;

public class MeasurementAdapter extends RecyclerView.Adapter<MeasurementAdapter.ViewHolder> {

    Context context;
    List<MeasurementModel> measurementModelList;

    public MeasurementAdapter(Context context, List<MeasurementModel> measurementModelList) {
        this.context = context;
        this.measurementModelList = measurementModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(measurementModelList != null && measurementModelList.size() > 0) {
            MeasurementModel model = measurementModelList.get(position);
            holder.measurement_id.setText(model.getLocation());
            holder.measurement_N.setText(model.getN());
            holder.measurement_P.setText(model.getP());
            holder.measurement_K.setText(model.getK());
        } else {
            return;
        }
    }

    @Override
    public int getItemCount() {
        return measurementModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView measurement_id, measurement_N, measurement_P, measurement_K;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            measurement_id = itemView.findViewById(R.id.measurement_id);
            measurement_N = itemView.findViewById(R.id.measurement_N);
            measurement_P = itemView.findViewById(R.id.measurement_P);
            measurement_K = itemView.findViewById(R.id.measurement_K);
        }
    }
}