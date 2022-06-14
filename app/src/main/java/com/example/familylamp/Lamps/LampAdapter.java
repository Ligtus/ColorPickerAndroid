package com.example.familylamp.Lamps;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.familylamp.Fragments.LampFragment;
import com.example.familylamp.R;

import java.util.ArrayList;

public class LampAdapter extends RecyclerView.Adapter<LampAdapter.LampViewHolder> {
    // List of lamps
    ArrayList<Lamp> lamps;
    // LampFragment reference
    LampFragment lampFragment;

    // Constructor
    public LampAdapter(ArrayList<Lamp> lamps, LampFragment lampFragment) {
        this.lamps = lamps;
        this.lampFragment = lampFragment;
    }

    // Create a new lamp view holder
    @Override
    public LampViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lamp, parent, false);
        return new LampViewHolder(v);
    }

    // Bind the lamp view holder to the lamp
    @Override
    public void onBindViewHolder(LampViewHolder holder, int position) {
        // Get the lamp
        Lamp lamp = lamps.get(position);
        // Set the lamp name
        holder.lampName.setText(lamp.getName());
        // Set the lamp IP
        holder.lampAddress.setText(lamp.getIp().getHostAddress());
    }

    // Get the number of lamps
    @Override
    public int getItemCount() {
        return lamps.size();
    }

    // Lamp view holder
    public class LampViewHolder extends RecyclerView.ViewHolder {
        TextView lampName, lampAddress;
        public LampViewHolder(View itemView) {
            super(itemView);
            // Set onclick listener for the lamp
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // When the lamp is clicked, get the lamp IP and store it in a shared preference
                    PreferenceManager.getDefaultSharedPreferences(v.getContext()).edit().putString("ip", lampAddress.getText().toString()).apply();
                    // Notify the user
                    Toast.makeText(itemView.getContext(), "Ip seleccionada: '" + lampAddress.getText().toString() + "'", Toast.LENGTH_SHORT).show();
                    // Navigate to the main fragment
                    NavHostFragment.findNavController(lampFragment).navigate(R.id.action_lampFragment_to_mainFragment);
                }
            });
            // Get the lamp name and IP views
            lampName = itemView.findViewById(R.id.lampName);
            lampAddress = itemView.findViewById(R.id.lampIp);
        }
    }
}