package com.example.familylamp;;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainFragment extends Fragment {

    // Connection variables
    private final int PORT = 11555;

    // Color circle view and dialog variables
    ImageView iv;
    ImageView colorDialogButton;
    Bitmap bitmap;
    int px = 0;

    // Recientes variables
    ArrayList<Recientes> recientesList = new ArrayList<>();
    RecyclerView recientesRecyclerView;
    RecientesAdapter adapter;
    final int BUTTONS_PER_ROW = 6;

    // Muestra and color variables
    Button muestra;
    int oldR = 0, oldG = 0, oldB = 0;
    int rBrillo = 0, gBrillo = 0, bBrillo = 0;
    int r=0, g=0, b=0;
    String hex = "#000000";

    // Brillo variables and views
    TextView brilloValue;
    SeekBar brillo;

    // Saved color variables and buttons
    static ArrayList<String> recientes = new ArrayList<String>();

    // Settings variables
    SharedPreferences prefs;
    boolean vibration, showCodes, autoSend, overwrite;
    int vibrationTime, nRecientes;

    // Vibrator variable
    Vibrator vibrator;

    public MainFragment() {
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
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
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load application preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Load vibration preferences
        vibration = prefs.getBoolean("vibration", true);
        vibrationTime = prefs.getInt("vibrationTime", 15);
        if (vibration) {
            vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        }

        // Load color codes preferences
        showCodes = prefs.getBoolean("showCodes", false);

        // Load automatic send preferences
        autoSend = prefs.getBoolean("auto_send", false);

        // Load overwrite preferences
        overwrite = prefs.getBoolean("overwrite", true);

        // Load number of recientes, if it doesn't exist, set it to 2 (default)
        nRecientes  = Integer.parseInt(prefs.getString("nRecientes", "2"));

        // Load index of saved colors if it exists, else set it to 0
        int index = prefs.getInt("index", 0);

        // If there are saved colors, load them
        if (index != 0) {
            recientes.clear();
            for (int i=0; i < index; i++) {
                recientes.add(prefs.getString("color" + i, "#000000"));
            }
        }

        if ((nRecientes * BUTTONS_PER_ROW) != recientes.size()) {
            cargarRecientes();
        }

        // Bind the recycler view to the adapter
        adapter = new RecientesAdapter(getContext(), recientesList, BUTTONS_PER_ROW);
        recientesRecyclerView = view.findViewById(R.id.recientesRecyclerView);
        recientesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recientesRecyclerView.setAdapter(adapter);

        // Show saved colors
        cargarRecientes();

        // Set app main background color, this is used to reset the color in case settings has been opened
        getActivity().findViewById(R.id.nav_host_fragment).setBackgroundColor(getResources().getColor(R.color.appBackground));

        // Get "cambiar" button, drawable and animation
        ImageButton cambiarBtn = getView().findViewById(R.id.cambiarBtn);
        if (!autoSend) {
            cambiarBtn.setImageResource(R.drawable.send_24dp);
        }
        Drawable imgCambiar = cambiarBtn.getDrawable();
        AnimatedVectorDrawable animCambiar = (AnimatedVectorDrawable) imgCambiar;

        // Set "cambiar" button onClickListener to change color and start animation
        cambiarBtn.setOnClickListener(view1 -> {
            updateRecientes(getView(), adapter, 0);
            if (recientes.size() < (BUTTONS_PER_ROW * nRecientes) || overwrite || !autoSend) {
                animCambiar.start();
            }
        });

        iv = getView().findViewById(R.id.color);
        colorDialogButton = getView().findViewById(R.id.colorDialogButton);
        muestra = getView().findViewById(R.id.muestra);
        iv.setDrawingCacheEnabled(true);
        iv.buildDrawingCache(true);
        brillo = getView().findViewById(R.id.brillo);
        brilloValue = getView().findViewById(R.id.brilloValue);

        colorDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modRGB();
            }
        });

        ImageButton btnSettings = getView().findViewById(R.id.buttonSettings);
        View explosion = getView().findViewById(R.id.btnExplosion);

        Drawable imgSettings = btnSettings.getDrawable();
        AnimatedVectorDrawable animSettings = (AnimatedVectorDrawable) imgSettings;
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.btn_explosion_anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                settings();
                explosion.setVisibility(View.INVISIBLE);
                btnSettings.setVisibility(View.INVISIBLE);
                getActivity().findViewById(R.id.nav_host_fragment).setBackgroundColor(getResources().getColor(R.color.settingsBackground));

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                explosion.setVisibility(View.VISIBLE);
                explosion.startAnimation(animation);
                animSettings.start();
                //settings();
            }
        });

        iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    int x = (int) motionEvent.getX();
                    int y = (int) motionEvent.getY();
                    if ((y < view.getHeight()) && (x < view.getWidth()) && (x > 0) && (y > 0)) {
                        bitmap = iv.getDrawingCache();
                        //log result of bitmap.getcolor
                        int pixel = bitmap.getPixel(x, y);
                        r = Color.red(pixel);
                        g = Color.green(pixel);
                        b = Color.blue(pixel);
                        //testrgb.setText("cuadrante: " + getQuadrant(x, y) + " startCoords: " + getQuadrantStartPoint(getQuadrant(x, y))[0] + ", " + getQuadrantStartPoint(getQuadrant(x, y))[1]);

                        //playing = true;
                        chooseColor(true);

                    }


                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    int x = (int) motionEvent.getX();
                    int y = (int) motionEvent.getY();
                    if ((y < view.getHeight()) && (x < view.getWidth()) && (x > 0) && (y > 0)) {
                        bitmap = iv.getDrawingCache();
                        //log result of bitmap.getcolor
                        int pixel = bitmap.getPixel(x, y);
                        r = Color.red(pixel);
                        g = Color.green(pixel);
                        b = Color.blue(pixel);
                        //testrgb.setText("cuadrante: " + getQuadrant(x, y) + " startCoords: " + getQuadrantStartPoint(getQuadrant(x, y))[0] + ", " + getQuadrantStartPoint(getQuadrant(x, y))[1]);

                        chooseColor(false);

                    }
                    return true;
                }
                return false;
            }
        });

        brillo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                brilloValue.setText(String.valueOf(progress));
                if (fromUser) {
                    if (hex != "#000000") {
                        chooseColor(true);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        if (recientes.size() >= 1) {
            chooseFromHex(recientes.get(0));
        }


    }

    private int getQuadrant(int x, int y) {
        if (x < (iv.getWidth() / 2)) {
            if (y < (iv.getHeight() / 2)) {
                return 1;
            } else {
                return 3;
            }
        } else {
            if (y < (iv.getHeight() / 2)) {
                return 2;
            } else {
                return 4;
            }
        }
    }

    private int[] getQuadrantStartPoint(int quadrant) {
        int[] startPoint = new int[2];
        switch (quadrant) {
            case 1:
                startPoint[0] = 0;
                startPoint[1] = 0;
                break;
            case 2:
                startPoint[0] = iv.getWidth() / 2;
                startPoint[1] = 0;
                break;
            case 3:
                startPoint[0] = 0;
                startPoint[1] = iv.getHeight() / 2;
                break;
            case 4:
                startPoint[0] = iv.getWidth() / 2;
                startPoint[1] = iv.getHeight() / 2;
                break;
        }
        return startPoint;
    }

    public void chooseColor(boolean animate) {
        calcBrilloColors();
        int color = Color.rgb(rBrillo, gBrillo, bBrillo);

        hex = String.format("#%06X", (0xFFFFFF & color));

        if (!hex.equals("#000000")) {
            if (animate) {
                animMuestraColor();
            } else {
                setMuestraColor();
                oldR = rBrillo;
                oldG = gBrillo;
                oldB = bBrillo;
            }
        } else {
            clearMuestraColor();
        }
    }

    public void chooseFromHex(String hexReciente) {
        hex = hexReciente;
        int px = Color.parseColor(hex);
        rBrillo = (int) (Color.red(px));
        gBrillo = (int) (Color.green(px));
        bBrillo = (int) (Color.blue(px));

        calcBrillo(rBrillo, gBrillo, bBrillo);

        animMuestraColor();
    }

    public void updateRecientes(View view, RecyclerView.Adapter adapter, int index) {
        if (!hex.equals("#000000")) {
            if (recientes.size() == 0) {
                recientes.add(hex);
                cargarRecientes();
                adapter.notifyDataSetChanged();
                return;
            }
            if (!recientes.contains(hex)) {
                if (recientes.size() < (BUTTONS_PER_ROW * nRecientes) || overwrite) {
                    recientes.add(index, hex);
                    if (recientes.size() > (BUTTONS_PER_ROW * nRecientes)) {
                        recientes.remove(recientes.size() - 1);
                    }
                } else {
                    if (vibration) {
                        vibrator.vibrate(vibrationTime);
                    }
                    Toast.makeText(getContext(), R.string.error_no_space, Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (vibration) {
                    vibrator.vibrate(vibrationTime);
                }
                Toast.makeText(getContext(), R.string.error_already_saved, Toast.LENGTH_SHORT).show();
                return;
            }
            cargarRecientes();
            adapter.notifyDataSetChanged();
        } else {
            if (vibration) {
                vibrator.vibrate(vibrationTime);
            }
            Toast.makeText(getContext(), R.string.error_no_color, Toast.LENGTH_SHORT).show();
        }
        float brilloTmp = Float.parseFloat(brilloValue.getText().toString()) / 100;
        BroadCastThread bct = new BroadCastThread((int)(r*brilloTmp) + "," + (int)(g*brilloTmp) + "," + (int)(b*brilloTmp), PORT);
        bct.start();
    }

    public void overwrite(int index) {
        if (recientes.size() > index) {
            recientes.set(index, hex);
            cargarRecientes();
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), R.string.color_overwritten, Toast.LENGTH_SHORT).show();
        }
    }

    public int getHigher(int r, int g, int b) {
        int higher = 0;
        if (r > g) {
            if (r > b) {
                higher = r;
            } else {
                higher = b;
            }
        } else {
            if (g > b) {
                higher = g;
            } else {
                higher = b;
            }
        }
        return higher;
    }

    public void calcBrillo(int r, int g, int b) {
        int higher = getHigher(r, g, b);
        double brilloValue = ((double)higher / (double)(255)) * 100;
        ValueAnimator valAnim = ValueAnimator.ofInt(brillo.getProgress(), (int)brilloValue);
        valAnim.setDuration(250);
        valAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                brillo.setProgress(value);
            }
        });
        valAnim.start();
    }

    public void calcBrilloColors() {
        float brilloColor = Float.parseFloat(brilloValue.getText().toString()) / 100;
        rBrillo = (int) (r * brilloColor);
        gBrillo = (int) (g * brilloColor);
        bBrillo = (int) (b * brilloColor);
    }

    public void chooseColorDialog(View colorDialog, TextView valuesDialog) {
        int color = Color.rgb(rBrillo, gBrillo, bBrillo);

        hex = String.format("#%06X", (0xFFFFFF & color));

        if (!hex.equals("#000000")) {
            calcBrillo(rBrillo, gBrillo, bBrillo);
            calcBrilloColors();
            setMuestraColor();
            colorDialog.setBackgroundColor(color);
        } else {
            clearMuestraColor();
            colorDialog.setBackgroundColor(getResources().getColor(R.color.nulo));
        }
        if (showCodes) {
            valuesDialog.setText("RGB\n" + rBrillo + ", " + gBrillo + ", " + bBrillo + "\nHEX\n" + hex);
        }
    }

    public void modRGB() {
        final Dialog dialog = new Dialog(this.getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.rgb_dialog);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_background));

        TextView valuesDialog = dialog.findViewById(R.id.valuesDialog);
        View colorDialog = dialog.findViewById(R.id.colorDialog);
        SeekBar sbR = dialog.findViewById(R.id.seekBar_0);
        sbR.setProgress(rBrillo);
        sbR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                r = progress;
                chooseColorDialog(colorDialog, valuesDialog);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar sbG = dialog.findViewById(R.id.seekBar_1);
        sbG.setProgress(gBrillo);
        sbG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                g = progress;
                chooseColorDialog(colorDialog, valuesDialog);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar sbB = dialog.findViewById(R.id.seekBar_2);
        sbB.setProgress(bBrillo);
        sbB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                b = progress;
                chooseColorDialog(colorDialog, valuesDialog);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        chooseColorDialog(colorDialog, valuesDialog);

        Button confirm = dialog.findViewById(R.id.confirmDialog);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateRecientes(getView(), adapter, 0);
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    public void cargarRecientes() {
        /*
         * If the number of recientes in preferences is not equal to the number of saved colors,
         * clear the list and add the saved colors.
         */
        recientesList.clear();
        for (int i = 0, j = 0; i < nRecientes; i++) {
            // Each reciente is a row of buttons
            String[] hexCodes = new String[BUTTONS_PER_ROW];
            for (int k = 0; k < BUTTONS_PER_ROW; k++, j++) {
                if (j < recientes.size()) {
                    hexCodes[k] = recientes.get(j);
                } else {
                    hexCodes[k] = "#000000";
                }
            }

            // Create a new reciente
            Recientes reciente = new Recientes(hexCodes, new ButtonListener());
            recientesList.add(reciente);
        }

    }

    public void guardarRecientes() {
        SharedPreferences.Editor sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        int index = 0;
        for (String reciente: recientes) {
            sharedPreferencesEditor.putString("color" + index, reciente);
            index++;
        }
        sharedPreferencesEditor.putInt("index", index);
        sharedPreferencesEditor.commit();
    }

    private void clearMuestraColor() {
        muestra.setText("");
        muestra.setBackgroundColor(getResources().getColor(R.color.nulo));
    }

    private void setMuestraColor() {
        if (showCodes) {
            muestra.setText("RGB\n" + rBrillo + ", " + gBrillo + ", " + bBrillo + "\nHEX\n" + hex);
        }
        muestra.setBackgroundColor(Color.rgb(rBrillo, gBrillo, bBrillo));
        chooseTextColor();
    }

    private void animMuestraColor() {
        int endRGB = Color.argb(255, rBrillo, gBrillo, bBrillo);
        ValueAnimator valAnimRGB = ValueAnimator.ofArgb(Color.argb(255, oldR, oldG, oldB), endRGB);
        oldR = rBrillo;
        oldG = gBrillo;
        oldB = bBrillo;
        valAnimRGB.setDuration(75);
        valAnimRGB.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Object color = animation.getAnimatedValue();
                rBrillo = Color.red((int)color);
                gBrillo = Color.green((int)color);
                bBrillo = Color.blue((int)color);
                setMuestraColor();
            }
        });
        valAnimRGB.start();
    }

    private boolean isBetween(double value, double min, double max) {
        return value >= min && value <= max; // true if value is between min and max
    }

    private int calcValue(int value, double brillo) {
        int valueTmp;
        if (isBetween(brillo, 0.45, 1)) {
            valueTmp = 255 - (int) (value / brillo);
        } else {
            valueTmp = (int) (value / brillo);
        }

        if (valueTmp < 0) {
            return 0;
        } else if (valueTmp > 255) {
            return 255;
        } else {
            return valueTmp;
        }
    }

    private void chooseTextColor(){
        double tmpR, tmpG, tmpB;
        double brillo = Double.parseDouble(brilloValue.getText().toString()) / 100;
        tmpR = calcValue(r, brillo);
        tmpG = calcValue(g, brillo);
        tmpB = calcValue(b, brillo);
        muestra.setTextColor(Color.rgb((int)tmpR, (int)tmpG, (int)tmpB));
    }

    private void deleteColor(int index) {
        recientes.remove(index);
        cargarRecientes();
        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), R.string.color_deleted, Toast.LENGTH_SHORT).show();
    }



    /*
     * An inner class to handle the clicks on the recientes.
     * This class is passed as a parameter to the Recientes constructor,
     * then the Adapter binds its execute method to the onClickListener.
     */
    public class ButtonListener {
        ButtonListener() {};

        public void execute(int iterator, Button btn) {
            ConfirmManager cm = new ConfirmManager(getContext());
            PopupMenu popup = new PopupMenu(getActivity(), btn, Gravity.CENTER, 0, R.style.PopUpMenuCustom);
            popup.setForceShowIcon(true);
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.popup_send:
                            return true;
                        case R.id.popup_set:
                            chooseFromHex(recientes.get(iterator));
                            return true;
                        case R.id.popup_overwrite:
                            if (!recientes.contains(hex)) {
                                cm.confirmDialog(
                                        getResources().getString(R.string.overwrite_color_title),
                                        getResources().getString(R.string.overwrite_color_message),
                                        getResources().getString(R.string.confirm_overwrite),
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                overwrite(iterator);
                                            }
                                        }
                                );
                            } else {
                                Toast.makeText(getContext(), R.string.error_already_saved, Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        case R.id.popup_insert:
                            updateRecientes(btn, adapter, iterator);
                            return true;
                        case R.id.popup_delete:
                            cm.confirmDialog(
                                    getResources().getString(R.string.delete_color_title),
                                    getResources().getString(R.string.delete_color_message),
                                    getResources().getString(R.string.confirm_delete),
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            deleteColor(iterator);
                                        }
                                    }
                            );
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                }
            });

            if (vibration) {
                vibrator.vibrate(vibrationTime);
            }

            popup.show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        guardarRecientes();
    }

    @Override
    public void onResume() {
        super.onResume();
        clearMuestraColor();
        cargarRecientes();
    }

    public void settings() {
        NavHostFragment.findNavController(this).navigate(R.id.action_nav_home_to_nav_settings);
    }
}