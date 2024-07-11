package com.kbiz.highsocietycheckout.data.adapters;

import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kbiz.highsocietycheckout.R;
import com.kbiz.highsocietycheckout.data.entities.Harvest;

import java.util.List;
import java.util.Locale;

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
        holder.textViewDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(harvest.time));
        holder.textViewHarvesterHash.setText(harvest.userHash.substring(harvest.userHash.length()-7));
        holder.textViewAmount.setText(String.valueOf(harvest.amount));

        // Set alternating background colors
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.dark_green,null));
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.light_green,null));
        }
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
        private final TextView textViewHarvesterHash;

        public HarvestViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewHarvesterHash = itemView.findViewById(R.id.textViewHarvesterHash);
        }
    }
}
