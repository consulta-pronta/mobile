package com.unnebulous.consultapronta.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unnebulous.consultapronta.R;

import java.util.List;

public class SymptomAdapter
        extends RecyclerView.Adapter<SymptomAdapter.SymptomViewHolder> {
    private final List<Symptom> symptoms;
    public SymptomAdapter(List<Symptom> symptoms) {this.symptoms = symptoms;
    }

    @NonNull
    @Override
    public SymptomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_symptom, parent, false);
        return new SymptomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SymptomViewHolder holder, int position) {

        Symptom symptom = symptoms.get(position);

        holder.intensityText.setText(holder.itemView.getContext().getString(R.string.intensity, symptom.getIntensity()));

        holder.descriptionText.setText(symptom.getDescription());

        holder.dateText.setText(symptom.getDate());

        holder.locationText.setText(holder.itemView.getContext().getString(R.string.location, symptom.getLocation()));
    }

    @Override
    public int getItemCount() {return symptoms.size();}


    public static class SymptomViewHolder extends RecyclerView.ViewHolder {
        TextView intensityText;
        TextView descriptionText;
        TextView dateText;
        TextView locationText;

        public SymptomViewHolder(@NonNull View itemView) {
            super(itemView);

            intensityText = itemView.findViewById(R.id.intensity_text);

            descriptionText = itemView.findViewById(R.id.symptom_description);

            dateText = itemView.findViewById(R.id.date_text);

            locationText = itemView.findViewById(R.id.location_text);
        }
    }
}
