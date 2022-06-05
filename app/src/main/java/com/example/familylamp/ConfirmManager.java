package com.example.familylamp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class ConfirmManager {
    Context context;
    LayoutInflater inflater;

    public ConfirmManager(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void confirmDialog(String title, String message, String positiveButton, Runnable positiveAction) {
        AlertDialog.Builder confirmar = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        View layout = inflater.inflate(R.layout.confirm_dialog, null);
        confirmar.setView(layout);
        AlertDialog dialog = confirmar.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView titleText = layout.findViewById(R.id.dialogTitle);
        TextView messageText = layout.findViewById(R.id.dialogMessage);
        Button positiveButtonText = layout.findViewById(R.id.confirmDialogSuccess);

        titleText.setText(title);
        messageText.setText(message);
        positiveButtonText.setText(positiveButton);

        layout.findViewById(R.id.confirmDialogSuccess).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                positiveAction.run();
                dialog.dismiss();
            }
        });

        layout.findViewById(R.id.confirmDialogCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}
