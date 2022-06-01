package com.example.familylamp;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainFragment extends Fragment {

    // Connection variables
    private final int PORT = 11555;

    // Color circle view variables
    ImageView iv;
    Bitmap bitmap;
    int px = 0;

    // Muestra and color variables and button
    Button muestra;
    int r=0, g=0, b=0;
    int rBrillo=0, gBrillo=0, bBrillo=0;
    String hex = "#000000";

    // Brillo variables and views
    TextView brilloValue;
    SeekBar brillo;
    boolean choose = true;

    // Saved color variables and buttons
    ArrayList<Button> botones = new ArrayList<Button>();
    ArrayList<String> recientes = new ArrayList<String>();

    // Settings variables
    SharedPreferences prefs;
    boolean vibration;
    int vibrationTime;


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

        // Load number of recientes, if it doesn't exist, set it to 12
        int nRecientes  = Integer.parseInt(prefs.getString("nRecientes", "12"));

        // Get number of recientes rows, there are 6 colors per row
        int nFilas = (int)Math.ceil(nRecientes/6);

        // Load index of saved colors if it exists, else set it to 0
        int index = prefs.getInt("index", 0);

        // If there are saved colors, load them
        if (index != 0) {
            recientes.clear();
            for (int i=0; i < index; i++) {
                recientes.add(prefs.getString("color" + i, "#000000"));
            }
        }

        // Show saved colors
        cargarRecientes();

        // Set app main background color, this is used to reset the color in case settings has been opened
        getActivity().findViewById(R.id.nav_host_fragment).setBackgroundColor(getResources().getColor(R.color.appBackground));

        // Get "cambiar" button, drawable and animation
        ImageButton cambiarBtn = getView().findViewById(R.id.cambiarBtn);
        Drawable imgCambiar = cambiarBtn.getDrawable();
        AnimatedVectorDrawable animCambiar = (AnimatedVectorDrawable) imgCambiar;

        // Set "cambiar" button onClickListener to change color and start animation
        cambiarBtn.setOnClickListener(view1 -> {
            updateRecientes(getView());
            animCambiar.start();
        });

        iv = getView().findViewById(R.id.color);
        muestra = getView().findViewById(R.id.muestra);
        iv.setDrawingCacheEnabled(true);
        iv.buildDrawingCache(true);
        brillo = getView().findViewById(R.id.brillo);
        brilloValue = getView().findViewById(R.id.brilloValue);

        muestra.setOnClickListener(new View.OnClickListener() {
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

        TextView testrgb = getView().findViewById(R.id.testrgb);

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
                        int tmpR = Color.red(pixel);
                        int tmpG = Color.green(pixel);
                        int tmpB = Color.blue(pixel);
                        testrgb.setText("cuadrante: " + getQuadrant(x, y) + " startCoords: " + getQuadrantStartPoint(getQuadrant(x, y))[0] + ", " + getQuadrantStartPoint(getQuadrant(x, y))[1]);

                        chooseColor(tmpR, tmpG, tmpB, true);

                        r=tmpR;
                        g=tmpG;
                        b=tmpB;
                    }


                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    int x = (int) motionEvent.getX();
                    int y = (int) motionEvent.getY();
                    if ((y < view.getHeight()) && (x < view.getWidth()) && (x > 0) && (y > 0)) {
                        bitmap = iv.getDrawingCache();
                        //log result of bitmap.getcolor
                        int pixel = bitmap.getPixel(x, y);
                        int tmpR = Color.red(pixel);
                        int tmpG = Color.green(pixel);
                        int tmpB = Color.blue(pixel);
                        testrgb.setText("cuadrante: " + getQuadrant(x, y) + " startCoords: " + getQuadrantStartPoint(getQuadrant(x, y))[0] + ", " + getQuadrantStartPoint(getQuadrant(x, y))[1]);

                        chooseColor(tmpR, tmpG, tmpB, true);

                        r = tmpR;
                        g = tmpG;
                        b = tmpB;

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
                        chooseColor(r,g,b,false);
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

    public void chooseColor(int tmpR, int tmpG, int tmpB, boolean save) {
        float brilloColor = Float.parseFloat(brilloValue.getText().toString()) / 100;
        int oldR = r, oldG = g, oldB = b;
        rBrillo = (int) (r * brilloColor);
        gBrillo = (int) (g * brilloColor);
        bBrillo = (int) (b * brilloColor);
        int color = Color.rgb(rBrillo, gBrillo, bBrillo);

        hex = String.format("#%06X", (0xFFFFFF & color));

        if (!hex.equals("#000000")) {
            animMuestraColor(oldR, oldG, oldB);
        } else {
            clearMuestraColor();
        }

    }

    public void chooseFromHex(String hexReciente) {
        hex = hexReciente;
        int px = Color.parseColor(hex);
        r = (int) (Color.red(px));
        g = (int) (Color.green(px));
        b = (int) (Color.blue(px));

        setMuestraColor();
    }

    public void updateRecientes(View view) {
        if (!hex.equals("#000000")) {
            if (recientes.size() == 0) {
                recientes.add(hex);
            }
            if (!recientes.contains(hex)) {
                recientes.add(0, hex);
                if (recientes.size() > 12) {
                    recientes.remove(12);
                }

            }
            cargarRecientes();
            float brilloTmp = Float.parseFloat(brilloValue.getText().toString())/100;
            BroadCastThread bct = new BroadCastThread((int)(r*brilloTmp) + "," + (int)(g*brilloTmp) + "," + (int)(b*brilloTmp), PORT);
            bct.start();
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

    public void chooseColorDialog(View colorDialog, TextView valuesDialog) {
        int color = Color.rgb(r, g, b);

        hex = String.format("#%06X", (0xFFFFFF & color));

        if (!hex.equals("#000000")) {
            calcBrillo(r, g, b);
            setMuestraColor();
            colorDialog.setBackgroundColor(color);
        } else {
            clearMuestraColor();
            colorDialog.setBackgroundColor(getResources().getColor(R.color.nulo));
        }

        valuesDialog.setText("RGB\n" + r + ", " + g + ", " + b + "\nHEX\n" + hex);

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
        sbR.setProgress(r);
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
        sbG.setProgress(g);
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
        sbB.setProgress(b);
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
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    public void confirmDialog(int iterator) {
        new AlertDialog.Builder(this.getActivity(), R.style.AlertDialogCustom)
                .setTitle(R.string.delete_color_title)
                .setMessage(R.string.delete_color_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        recientes.remove(iterator);
                        // show recientes on log
                        Log.d("Recientes", "array: " + recientes.toString());
                        cargarRecientes();
                        Toast.makeText(getContext(), R.string.color_deleted, Toast.LENGTH_SHORT).show();
                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }}).show();
    }

    public void cargarRecientes() {

        // Get all buttons from layout only once
        if (botones.size() < 12) {
            for (int i = 0; i < 2; i++) {
                LinearLayout ll = getView().findViewById(getResources().getIdentifier("linearLayout" + (i + 1), "id", getActivity().getPackageName()));
                for (int j = 0; j < ll.getChildCount(); j++) {
                    if (ll.getChildAt(j) instanceof Button) {
                        botones.add((Button) ll.getChildAt(j));
                    }
                }
            }
        }

        /*
            For each button, set it's background color to the color of the reciente
            and add onclick and onlongclick listeners
         */
        for (int i = 0; i < botones.size(); i++) {
            Button btn = botones.get(i);
            // log recientes size
            Log.d("Recientes", "size: " + recientes.size());
            if (i < recientes.size()) {
                btn.setBackgroundColor(Color.parseColor(recientes.get(i)));
                int iterator = i;
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (recientes.size() > iterator) {
                            chooseFromHex(recientes.get(iterator));
                            calcBrillo(r, g, b);
                        }
                    }
                });
                btn.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (recientes.size() > iterator) {
                            confirmDialog(iterator);
                            if (vibration) {
                                Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                                vibrator.vibrate(vibrationTime);
                            }
                        }
                        return true;
                    }
                });
            } else {
                btn.setBackgroundColor(getResources().getColor(R.color.nulo));
            }
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
        muestra.setText("RGB\n" + r + ", " + g + ", " + b + "\nHEX\n" + hex);
        muestra.setBackgroundColor(Color.rgb(rBrillo, gBrillo, bBrillo));
        chooseTextColor();
    }

    private void animMuestraColor(int tmpR, int tmpG, int tmpB) {
        ValueAnimator valAnimR = ValueAnimator.ofInt(tmpR, rBrillo);
        valAnimR.setDuration(200);
        valAnimR.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                rBrillo = value;
                setMuestraColor();
            }
        });
        valAnimR.start();

        ValueAnimator valAnimG = ValueAnimator.ofInt(tmpG, gBrillo);
        valAnimG.setDuration(200);
        valAnimG.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                gBrillo = value;
                setMuestraColor();
            }
        });
        valAnimG.start();

        ValueAnimator valAnimB = ValueAnimator.ofInt(tmpB, bBrillo);
        valAnimB.setDuration(200);
        valAnimB.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                bBrillo = value;
                setMuestraColor();
            }
        });
        valAnimB.start();
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

    @Override
    public void onStop() {
        super.onStop();
        guardarRecientes();
    }

    @Override
    public void onResume() {
        super.onResume();
        botones.clear();
        cargarRecientes();
    }

    public void settings() {
        NavHostFragment.findNavController(this).navigate(R.id.action_nav_home_to_nav_settings);
    }
}