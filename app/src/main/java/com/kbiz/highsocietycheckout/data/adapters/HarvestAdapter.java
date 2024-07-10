package com.kbiz.highsocietycheckout.data.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.entities.Harvest;

import java.util.List;

public class HarvestAdapter extends RecyclerView.Adapter<HarvestAdapter.HarvestViewHolder> {
    private List<Harvest> harvests;

    public HarvestAdapter(List<Harvest> harvests) {
        this.harvests = harvests;
    }

    @NonNull
    @Override
    public HarvestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_harvest, parent, false);
        return new HarvestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HarvestViewHolder holder, int position) {
        Harvest harvest = harvests.get(position);
        holder.textViewDate.setText(String.valueOf(harvest.time)); // You may want to format the time appropriately
        holder.textViewAmount.setText(String.valueOf(harvest.amount));
    }

    @Override
    public int getItemCount() {
        return harvests.size();
    }

    public void setHarvests(List<Harvest> harvests) {
        this.harvests = harvests;
        notifyDataSetChanged();
    }

    static class HarvestViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewDate;
        private final TextView textViewAmount;

        public HarvestViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
        }
    }
}
