package com.example.foregroundlocationservice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    ArrayList<DataModel> dataHolder;

    public RecyclerAdapter(ArrayList<DataModel> dataHolder) {
        this.dataHolder = dataHolder;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_set_coordinates_main_activity,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.latitude.setText(dataHolder.get(position).getLati());
        holder.longitude.setText(dataHolder.get(position).getLongi());
    }

    @Override
    public int getItemCount() {
        return dataHolder.size();
    }

    public void notifyData(ArrayList<DataModel> dataHolder) {
        this.dataHolder = dataHolder;
        notifyDataSetChanged();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView latitude,longitude;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            latitude=itemView.findViewById(R.id.latitude);
            longitude=itemView.findViewById(R.id.longitude);
        }
    }
}