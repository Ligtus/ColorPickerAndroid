package com.example.familylamp;

import android.widget.Button;

import java.util.ArrayList;

public class Recientes {
    private ArrayList<Button> buttons = new ArrayList<>();

    public Recientes(ArrayList<Button> buttons) {
        this.buttons = buttons;
    }

    public ArrayList<Button> getButtons() {
        return buttons;
    }

    public void setButtons(ArrayList<Button> buttons) {
        this.buttons = buttons;
    }
}
