package com.kbiz.highsocietycheckout.data.view;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kbiz.highsocietycheckout.R;

public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView textViewUserName;
    private final OnItemClickListener onItemClickListener;

    public UserViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
        super(itemView);
        textViewUserName = itemView.findViewById(R.id.textViewName);
        this.onItemClickListener = onItemClickListener;
        itemView.setOnClickListener(this);
    }

    public void bind(String userName) {
        textViewUserName.setText(userName);
    }

    @Override
    public void onClick(View v) {
        onItemClickListener.onItemClick(getAdapterPosition());
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}

