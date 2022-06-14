package com.example.familylamp.Recientes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.example.familylamp.Fragments.MainFragment;
import com.example.familylamp.R;

import java.util.ArrayList;

public class RecientesAdapter extends RecyclerView.Adapter<RecientesAdapter.ViewHolder> {
    Vibrator vibrator;
    Context context;
    ArrayList<Recientes> colores;
    int buttons_per_row;
    SharedPreferences sharedPreferences;
    boolean vibration;
    int vibrationTime;

    public RecientesAdapter(Context context, ArrayList<Recientes> colores, int buttons_per_row) {
        this.context = context;
        this.colores = colores;
        this.buttons_per_row = buttons_per_row;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        sharedPreferences = context.getSharedPreferences("com.example.familylamp", Context.MODE_PRIVATE);
        vibration = sharedPreferences.getBoolean("vibration", true);
        vibrationTime = sharedPreferences.getInt("vibrationTime", 15);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recientes, parent, false);
        return new ViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Recientes reciente = colores.get(position);
        String[] hexCodes = reciente.getHexCodes();
        holder.buttonListener = reciente.getButtonListener();
        for (int i = 0; i < buttons_per_row; i++) {
            if (hexCodes[i] != "#000000") {
                holder.buttons[i].setBackgroundColor(Color.parseColor(hexCodes[i]));
                int iterator = i;
                holder.buttons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.buttonListener.execute(iterator+(buttons_per_row*position), holder.buttons[iterator]);
                    }
                });
            } else {
                holder.buttons[i].setBackgroundColor(context.getResources().getColor(R.color.appBackground));
            }
        }
    }

    @Override
    public int getItemCount() {
        return colores.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button[] buttons = new Button[buttons_per_row];
        MainFragment.ButtonListener buttonListener;
        public ViewHolder(View itemView, Context context) {
            super(itemView);
            for (int i = 0; i < buttons_per_row; i++) {
                buttons[i] = itemView.findViewById(context.getResources().getIdentifier("button_" + (i + 1), "id", context.getPackageName()));
            }
        }
    }
}
