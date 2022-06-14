package com.example.familylamp.fragments;

import android.app.Dialog;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.familylamp.lamps.Lamp;
import com.example.familylamp.lamps.LampAdapter;
import com.example.familylamp.lamps.LampAnimator;
import com.example.familylamp.networkComms.MultiCastReceiveThread;
import com.example.familylamp.R;

import java.net.InetAddress;
import java.util.ArrayList;

public class LampFragment extends Fragment {

    // Found lamps ArrayList variable, adapter and recycler view
    public ArrayList<Lamp> lamps;
    public LampAdapter lampAdapter;
    RecyclerView lampRecyclerView;

    // ImageView for buttons and loading animation
    ImageView skipButton, addButton, loadingImage;

    // Networking variables
    MultiCastReceiveThread mcrt = null;

    // Handler for updating the UI
    Handler handler;


    public LampFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lamp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get loading animation and start it
        loadingImage = getView().findViewById(R.id.loadingImage);
        AnimatedVectorDrawable loadingAnimation = (AnimatedVectorDrawable) loadingImage.getDrawable();
        loadingAnimation.start();

        // Get the handler for updating the UI
        handler = new Handler();

        // Start network thread to listen for lamps
        startThreads();

        // Initialize the found lamps arraylist, recycler view and adapter
        lamps = new ArrayList<>();
        lampRecyclerView = view.findViewById(R.id.lampRecyclerView);
        lampAdapter = new LampAdapter(lamps, this);
        lampRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lampRecyclerView.setAdapter(lampAdapter);

        // Also set the recycler view animator
        lampRecyclerView.setItemAnimator(new LampAnimator());

        // Get add button and set on click listener as well as set the animation
        addButton = view.findViewById(R.id.addButton);
        Drawable addDrawable = addButton.getDrawable();
        AnimatedVectorDrawable addAnimatedVectorDrawable = (AnimatedVectorDrawable) addDrawable;
        addButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 addAnimatedVectorDrawable.start();
                 manualAdd();
             }
         });

        // Get skip button and set on click listener as well as set the animation
        skipButton = getActivity().findViewById(R.id.skipButton);
        Drawable skipDrawable = skipButton.getDrawable();
        AnimatedVectorDrawable skipAnimatedDrawable = (AnimatedVectorDrawable) skipDrawable;
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipAnimatedDrawable.start();
                mainFragment();
            }
        });
    }

    // Method to navigate to the main fragment
    public void mainFragment() {
        NavHostFragment.findNavController(this).navigate(R.id.action_lampFragment_to_mainFragment);
    }

    // Method to add a lamp manually
    public void manualAdd() {
        // Create a dialog to add a lamp
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.add_lamp_dialog);
        dialog.getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dialog_background, null));

        // Get the views from the dialog
        EditText lampName = dialog.findViewById(R.id.lampName);
        EditText lampIp = dialog.findViewById(R.id.lampIp);

        // Set the on click listener for the add button
        dialog.findViewById(R.id.addLampConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String name = lampName.getText().toString();
                    InetAddress ip = InetAddress.getByName(lampIp.getText().toString());
                    Lamp lamp = new Lamp(name, ip);
                    addLamp(lamp);
                    Toast.makeText(getContext(), R.string.add_lamp_added, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getContext(), R.string.add_lamp_error, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } finally {
                    dialog.dismiss();
                }
            }
        });

        // Set the on click listener for the cancel button
        dialog.findViewById(R.id.addLampCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), R.string.add_lamp_canceled, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }

    // Method to add a lamp to the arraylist and adapter
    public void addLamp(Lamp lamp) {
        try {
            // If the lamp is not already in the arraylist, add it
            if (!lamps.contains(lamp)) {
                lamps.add(lamp);
                lampAdapter.notifyItemInserted(lamps.size() - 1);
            }

            // Also if the lamps arraylist not empty, remove the loading animation
            if (lamps.size() > 0 && loadingImage.getVisibility() != View.INVISIBLE) {
                loadingImage.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to start the network threads to listen for lamps
    public void startThreads() {
        if (mcrt == null || mcrt.isInterrupted() || !mcrt.isAlive()) {
            mcrt = new MultiCastReceiveThread(handler, this);
            mcrt.start();
        }
    }

    // Method to stop the network threads
    public void stopThreads() {
        if (mcrt != null && mcrt.isAlive()) {
            if (mcrt.socket != null && mcrt.socket.isConnected()) {
                mcrt.socket.close();
            }
            mcrt.interrupt();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopThreads();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopThreads();
    }

    @Override
    public void onResume() {
        super.onResume();

        startThreads();
    }

}