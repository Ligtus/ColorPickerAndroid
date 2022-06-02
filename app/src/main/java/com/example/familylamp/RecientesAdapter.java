package com.example.familylamp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecientesAdapter extends RecyclerView.Adapter<RecientesAdapter.ViewHolder> {
    Context context;
    ArrayList<Recientes> recientes;

    public RecientesAdapter(Context context, ArrayList<Recientes> recientes) {
        this.context = context;
        this.recientes = recientes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recientes, parent, false);
        return new ViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.buttons.set(position, recientes.get(position).getButtons().get(position));
    }

    @Override
    public int getItemCount() {
        return recientes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Context context;
        ArrayList<Button> buttons = new ArrayList<>();
        public ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            for (int i = 0; i < 6; i++) {
                buttons.add((Button) itemView.findViewById(context.getResources().getIdentifier("button_" + (i+1), "id", context.getPackageName())));
            }
        }
    }
}}
