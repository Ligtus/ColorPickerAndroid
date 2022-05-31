package com.example.familylamp;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainFragment extends Fragment {

    // Connection variables
    private final int PORT = 11555;

    // Color circle view variables
    ImageView iv;
    Bitmap bitmap;
    int px = 0;

    // Muestra and color variables and button
    Button values;
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

        // Load saved colors
        SharedPreferences sharedPreferences = this.getActivity().getPreferences(MODE_PRIVATE);

        // Load number of recientes, if it doesn't exist, set it to 12
        int nRecientes  = sharedPreferences.getInt("nRecientes", 12);

        // Get number of recientes rows, there are 6 colors per row
        int nFilas = (int)Math.ceil(nRecientes/6);

        // Load index of saved colors if it exists, else set it to 0
        int index = sharedPreferences.getInt("index", 0);

        // If there are saved colors, load them
        if (index != 0) {
            recientes.clear();
            for (int i=0; i < index; i++) {
                recientes.add(sharedPreferences.getString("color" + i, "#000000"));
            }
        }

        // Show saved colors
        cargarRecientes(nRecientes, nFilas);

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
                getActivity().findViewById(R.id.nav_host_fragment).setBackgroundColor(getResources().getColor(R.color.explosionBackground));

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
                    if (y < view.getHeight() && x < view.getWidth()) {
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
                    if (y < view.getHeight() && x < view.getWidth()) {
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

    public void cargarRecientes(int nRecientes, int nFilas) {

        LinearLayout scrollLinear = getActivity().findViewById(R.id.scrollLinear);
        for (int i = 0; i < nFilas; i++) {
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < nRecientes/nFilas; j++) {
                final int index = i * (nRecientes/nFilas) + j;
                if (index < recientes.size()) {
                    Button btn = new Button(getActivity());
                    btn.setBackgroundColor(Color.parseColor(recientes.get(index)));
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            chooseFromHex(recientes.get(index));
                            calcBrillo(r, g, b);
                        }
                    });
                    btn.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            recientes.remove(index);
                            cargarRecientes(nRecientes, nFilas);
                            return true;
                        }
                    });
                    linearLayout.addView(btn);
                }
            }
            scrollLinear.addView(linearLayout);
        }

        // For each button, set it's background color to the color of the reciente
        // TODO - make this use amount of recientes setting
        for (int i = 1; i <= nRecientes; i++) {
            int id = getResources().getIdentifier("button_" + i, "id", this.getActivity().getPackageName());
            botones.add((Button) getView().findViewById(id));
            int iterator = i - 1;
            getView().findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recientes.size() > iterator) {
                        chooseFromHex(recientes.get(iterator));
                        calcBrillo(r, g, b);
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
        values.setBackgroundColor(Color.rgb(rBrillo, gBrillo, bBrillo));
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
        SharedPreferences sharedPreferences = this.getActivity().getPreferences(MODE_PRIVATE);
        int nRecientes = sharedPreferences.getInt("nRecientes", 12);
        int nFilas = nRecientes/6;
        cargarRecientes(nRecientes, nFilas);
    }

    public void settings() {
        NavHostFragment.findNavController(this).navigate(R.id.action_nav_home_to_nav_settings);
    }
}