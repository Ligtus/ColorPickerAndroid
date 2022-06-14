package com.example.familylamp.Fragments;

import android.app.Dialog;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;

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

import com.example.familylamp.Lamps.Lamp;
import com.example.familylamp.Lamps.LampAdapter;
import com.example.familylamp.Lamps.LampAnimator;
import com.example.familylamp.NetworkComms.MultiCastReceiveThread;
import com.example.familylamp.R;

import java.net.InetAddress;
import java.util.ArrayList;

public class LampFragment extends Fragment {

    public ArrayList<Lamp> lamps;
    public LampAdapter lampAdapter;
    ImageView skipButton, addButton, loadingImage;
    RecyclerView lampRecyclerView;
    MultiCastReceiveThread mcrt = null;
    //MultiCastSendThread mcst = null;
    Handler handler;
    String deviceIP;

    public LampFragment() {
    }

    public static LampFragment newInstance() {
        LampFragment fragment = new LampFragment();
        return fragment;
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingImage = getView().findViewById(R.id.loadingImage);
        AnimatedVectorDrawable loadingAnimation = (AnimatedVectorDrawable) loadingImage.getDrawable();
        loadingAnimation.start();

        handler = new Handler();

        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(getContext().WIFI_SERVICE);
        deviceIP = android.text.format.Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        startThreads();

        lamps = new ArrayList<>();

        /*try {
            lamps.add(new Lamp("Ejemplo", InetAddress.getByName("192.168.0.16")));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }*/

        lampRecyclerView = view.findViewById(R.id.lampRecyclerView);
        lampAdapter = new LampAdapter(lamps, this);
        lampRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lampRecyclerView.setAdapter(lampAdapter);
        lampRecyclerView.setItemAnimator(new LampAnimator());

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

    public void mainFragment() {
        NavHostFragment.findNavController(this).navigate(R.id.action_lampFragment_to_mainFragment);
    }

    public void manualAdd() {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.add_lamp_dialog);
        dialog.getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dialog_background, null));

        EditText lampName = dialog.findViewById(R.id.lampName);
        EditText lampIp = dialog.findViewById(R.id.lampIp);

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

        dialog.findViewById(R.id.addLampCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), R.string.add_lamp_canceled, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void addLamp(Lamp lamp) {
        try {
            if (lamp.getIp().toString().equals(InetAddress.getByName(deviceIP).toString())) return;
            if (!lamps.contains(lamp)) {
                lamps.add(lamp);
                lampAdapter.notifyItemInserted(lamps.size() - 1);
            }
            if (lamps.size() > 0 && loadingImage.getVisibility() != View.INVISIBLE) {
                loadingImage.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startThreads() {
        if (mcrt == null || mcrt.isInterrupted() || !mcrt.isAlive()) {
            mcrt = new MultiCastReceiveThread(handler, this);
            mcrt.start();
        }

        /*if (mcst == null || mcst.isInterrupted() || !mcst.isAlive()) {
            mcst = new MultiCastSendThread("Lamp," +  Build.MODEL + "," + deviceIP);
            mcst.start();
        }*/
    }

    public void stopThreads() {
        if (mcrt != null && mcrt.isAlive()) {
            if (mcrt.socket != null && mcrt.socket.isConnected()) {
                mcrt.socket.close();
            }
            mcrt.interrupt();
        }
        /*if (mcst != null) {
            mcst.socket.close();
            mcst.interrupt();
        }*/
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
        /*if (mcrt != null && mcst != null) {
            if (!mcrt.isAlive() || !mcst.isAlive()) {
                searchButton.setVisibility(View.VISIBLE);
            }
        }*/
        startThreads();
    }

}