package com.example.colorpicker.recientes;

import com.example.colorpicker.fragments.MainFragment;

public class Recientes {
    private String[] hexCodes;
    private MainFragment.ButtonListener buttonListener;

    // Constructor
    public Recientes(String[] hexCodes, MainFragment.ButtonListener buttonListener) {
        this.hexCodes = hexCodes;
        this.buttonListener = buttonListener;
    }

    // Getters and setters
    public String[] getHexCodes() {
        return hexCodes;
    }

    public void setHexCodes(String[] hexCodes) {
        this.hexCodes = hexCodes;
    }

    public MainFragment.ButtonListener getButtonListener() {
        return buttonListener;
    }

    public void setButtonListener(MainFragment.ButtonListener buttonListener) {
        this.buttonListener = buttonListener;
    }
}
