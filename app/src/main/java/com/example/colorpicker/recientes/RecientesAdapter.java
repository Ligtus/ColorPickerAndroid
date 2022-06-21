package com.example.colorpicker.recientes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.colorpicker.fragments.MainFragment;
import com.example.colorpicker.R;

import java.util.ArrayList;

public class RecientesAdapter extends RecyclerView.Adapter<RecientesAdapter.ViewHolder> {
    final Vibrator vibrator;
    final Context context;
    final ArrayList<Recientes> colores;
    final int buttons_per_row;
    final SharedPreferences sharedPreferences;
    final boolean vibration;
    final int vibrationTime;

    // Constructor
    public RecientesAdapter(Context context, ArrayList<Recientes> colores, int buttons_per_row) {
        this.context = context;
        this.colores = colores;
        this.buttons_per_row = buttons_per_row;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        sharedPreferences = context.getSharedPreferences("com.example.familylamp", Context.MODE_PRIVATE);
        vibration = sharedPreferences.getBoolean("vibration", true);
        vibrationTime = sharedPreferences.getInt("vibrationTime", 15);
    }

    // Create new viewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recientes, parent, false);
        return new ViewHolder(v, parent.getContext());
    }

    // Bind the viewHolder to the view
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Get the reciente
        Recientes reciente = colores.get(position);
        // Get hex codes for that reciente
        String[] hexCodes = reciente.getHexCodes();
        // Get buttonListener for the reciente
        holder.buttonListener = reciente.getButtonListener();
        // Set background color for each button in the reciente
        for (int i = 0; i < buttons_per_row; i++) {
            if (!hexCodes[i].equals("#000000")) {
                holder.buttons[i].setBackgroundColor(Color.parseColor(hexCodes[i]));
                int iterator = i;

                // Also set the onClickListener to execute a popUpMenu
                holder.buttons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.buttonListener.execute(iterator+(buttons_per_row*position), holder.buttons[iterator]);
                    }
                });
            } else {
                // If the color is black, set the button to default background color
                holder.buttons[i].setBackgroundColor(context.getResources().getColor(R.color.appBackground));
            }
        }
    }

    // Return number of recientes
    @Override
    public int getItemCount() {
        return colores.size();
    }

    // Viewholder class
    public class ViewHolder extends RecyclerView.ViewHolder {
        // buttons Array and ButtonListener instance
        final Button[] buttons = new Button[buttons_per_row];
        MainFragment.ButtonListener buttonListener;
        public ViewHolder(View itemView, Context context) {
            super(itemView);
            // find all buttons inside this viewHolder and get them inside the buttons array
            for (int i = 0; i < buttons_per_row; i++) {
                buttons[i] = itemView.findViewById(context.getResources().getIdentifier("button_" + (i + 1), "id", context.getPackageName()));
            }
        }
    }
}
