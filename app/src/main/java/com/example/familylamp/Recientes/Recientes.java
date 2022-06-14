package com.example.familylamp.Recientes;

import com.example.familylamp.Fragments.MainFragment;

public class Recientes {
    private String[] hexCodes;
    private MainFragment.ButtonListener buttonListener;

    public Recientes(String[] hexCodes, MainFragment.ButtonListener buttonListener) {
        this.hexCodes = hexCodes;
        this.buttonListener = buttonListener;
    }

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
