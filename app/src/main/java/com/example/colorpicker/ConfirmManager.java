package com.example.colorpicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

// This class is used to create a dialog box to confirm the user's choice.

public class ConfirmManager {
    // Variables of context and layout inflater, to know where to inflate the dialog box.
    final Context context;
    final LayoutInflater inflater;

    // Constructor
    public ConfirmManager(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    /**
     * This method creates a dialog box with the given parameters.
     * @param title The title of the dialog box.
     * @param message The message of the dialog box.
     * @param positiveButton The text of the positive button.
     * @param positiveAction The action to be executed when the positive button is pressed.
     */
    public void confirmDialog(String title, String message, String positiveButton, Runnable positiveAction) {
        // Inflate the dialog box.
        AlertDialog.Builder confirmar = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
        View layout = inflater.inflate(R.layout.confirm_dialog, null);
        confirmar.setView(layout);
        // Show the dialog box and set its background drawable.
        AlertDialog dialog = confirmar.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Get the text views and buttons from the dialog box.
        TextView titleText = layout.findViewById(R.id.dialogTitle);
        TextView messageText = layout.findViewById(R.id.dialogMessage);
        Button positiveButtonText = layout.findViewById(R.id.confirmDialogSuccess);

        // Set the text of the text views and buttons.
        titleText.setText(title);
        messageText.setText(message);
        positiveButtonText.setText(positiveButton);

        // Set the onClickListener of the positive button.
        layout.findViewById(R.id.confirmDialogSuccess).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                positiveAction.run();
                dialog.dismiss();
            }
        });

        // Set the onClickListener of the negative button.
        layout.findViewById(R.id.confirmDialogCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}