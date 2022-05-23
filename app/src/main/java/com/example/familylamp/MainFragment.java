package com.example.familylamp;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

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
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainFragment extends Fragment {

    private final int PORT = 11555;
    ImageView iv;
    //View colorView;
    Button values;
    TextView brilloValue;
    Bitmap bitmap;
    SeekBar brillo;
    boolean choose = true;
    int r=0, g=0, b=0;
    int px = 0;
    String hex = "#000000";
    ArrayList<Button> botones = new ArrayList<Button>();
    ArrayList<String> recientes = new ArrayList<String>();

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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = this.getActivity().getPreferences(MODE_PRIVATE);

        int index = sharedPreferences.getInt("index", 0);

        if (index != 0) {
            recientes.clear();
            for (int i=0; i < index; i++) {
                recientes.add(sharedPreferences.getString("color" + i, "#000000"));
            }
        }

        ImageButton cambiarBtn = getView().findViewById(R.id.cambiarBtn);
        Drawable imgCambiar = cambiarBtn.getDrawable();
        AnimatedVectorDrawable animCambiar = (AnimatedVectorDrawable) imgCambiar;

        cambiarBtn.setOnClickListener(view1 -> {
            updateRecientes(getView());
            animCambiar.start();
        });

        cargarRecientes();

        iv = getView().findViewById(R.id.color);
        values = getView().findViewById(R.id.muestra);
        iv.setDrawingCacheEnabled(true);
        iv.buildDrawingCache(true);
        brillo = getView().findViewById(R.id.brillo);
        brilloValue = getView().findViewById(R.id.brilloValue);

        values.setOnClickListener(new View.OnClickListener() {
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
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    bitmap = iv.getDrawingCache();
                    if ((((int) motionEvent.getY() < iv.getHeight()) && ((int) motionEvent.getY() > 10)) &&
                            (((int) motionEvent.getX() < iv.getWidth()) && ((int) motionEvent.getX() > 10))) {
                        int pxTmp = bitmap.getPixel((int) motionEvent.getX(), (int) motionEvent.getY());
                        int[] rgb = new int[]{0,0,0};
                        if (getRGB(pxTmp) != rgb) {
                            px = pxTmp;
                        }
                        int[] rgbTmp = getRGB(px);
                        testrgb.setText("x: " + (int) motionEvent.getX() + " y: " + (int) motionEvent.getY() + " rgb: " );

                    }
                    if (px != 0) {
                        chooseColor();
                    }
                }
                return true;
            }
        });

        brillo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                brilloValue.setText(String.valueOf(progress));
                if (choose) {
                    if (px != 0) {
                        chooseColor();
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

    public int[] getRGB(int color) {
        int[] rgb = new int[3];
        rgb[0] = (int) (Color.red(color));
        rgb[1] = (int) (Color.green(color));
        rgb[2] = (int) (Color.blue(color));
        return rgb;
    }

    public void chooseColor() {
        float brilloColor = Float.parseFloat(brilloValue.getText().toString()) / 100;
        int[] rgb = getRGB(px);
        r = (int) (rgb[0] * brilloColor);
        g = (int) (rgb[1] * brilloColor);
        b = (int) (rgb[2] * brilloColor);
        int color = Color.rgb(r, g, b);

        hex = String.format("#%06X", (0xFFFFFF & color));

        if (!hex.equals("#000000")) {
            setMuestraColor();
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
            for (int i = 0; i < recientes.size(); i++) {
                botones.get(i).setBackgroundColor(Color.parseColor(recientes.get(i)));
            }
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
        brillo.setProgress((int)brilloValue);
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
                choose = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                choose = true;
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
                choose = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                choose = true;
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
                choose = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                choose = true;
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

    public void cargarRecientes() {
        for (int i = 1; i <= 12; i++) {
            int id = getResources().getIdentifier("button_" + i, "id", this.getActivity().getPackageName());
            botones.add((Button) getView().findViewById(id));
            // log botones length
            Log.d("botones", String.valueOf(botones.size()));
            int iterator = i - 1;
            getView().findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recientes.size() > iterator) {
                        chooseFromHex(recientes.get(iterator));
                    }
                }
            });
        }
        for (int i = 0; i < recientes.size(); i++) {
            botones.get(i).setBackgroundColor(Color.parseColor(recientes.get(i)));
        }
    }

    public void guardarRecientes() {
        SharedPreferences.Editor sharedPreferencesEditor = this.getActivity().getPreferences(MODE_PRIVATE).edit();
        int index = 0;
        for (String reciente: recientes) {
            sharedPreferencesEditor.putString("color" + index, reciente);
            index++;
        }
        sharedPreferencesEditor.putInt("index", index);
        sharedPreferencesEditor.commit();
    }

    private void clearMuestraColor() {
        values.setText("");
        values.setBackgroundColor(getResources().getColor(R.color.nulo));
    }

    private void setMuestraColor() {
        values.setText("RGB\n" + r + ", " + g + ", " + b + "\nHEX\n" + hex);
        values.setBackgroundColor(Color.rgb(r, g, b));
        chooseTextColor();
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
        values.setTextColor(Color.rgb((int)tmpR, (int)tmpG, (int)tmpB));
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