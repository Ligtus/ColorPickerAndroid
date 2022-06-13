package com.example.familylamp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class LampAdapter extends RecyclerView.Adapter<LampAdapter.LampViewHolder> {
    ArrayList<Lamp> lamps;
    SharedPreferences sharedPreferences;
    LampFragment lampFragment;

    public LampAdapter(ArrayList<Lamp> lamps, LampFragment lampFragment) {
        this.lamps = lamps;
        this.lampFragment = lampFragment;
    }

    @Override
    public LampViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lamp, parent, false);
        return new LampViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LampViewHolder holder, int position) {
        Lamp lamp = lamps.get(position);
        holder.lampName.setText(lamp.getName());
        holder.lampAddress.setText(lamp.getIp().getHostAddress());
    }

    @Override
    public int getItemCount() {
        return lamps.size();
    }

    public class LampViewHolder extends RecyclerView.ViewHolder {
        TextView lampName, lampAddress;
        public LampViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PreferenceManager.getDefaultSharedPreferences(v.getContext()).edit().putString("ip", lampAddress.getText().toString()).apply();
                    Toast.makeText(itemView.getContext(), "Ip seleccionada: '" + lampAddress.getText().toString() + "'", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(lampFragment).navigate(R.id.action_lampFragment_to_mainFragment);
                }
            });

            lampName = itemView.findViewById(R.id.lampName);
            lampAddress = itemView.findViewById(R.id.lampIp);
        }
    }
}