package com.example.familylamp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    /* OLD activity code
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);

        int index = sharedPreferences.getInt("index", 0);

        if (index != 0) {
            for (int i=0; i < index; i++) {
                recientes.add(sharedPreferences.getString("color" + i, "#000000"));
            }
        }

        for (int i = 1; i <= 12; i++) {
            int id = getResources().getIdentifier("button_" + i, "id", getPackageName());
            botones.add((Button) findViewById(id));
            int iterator = i - 1;
            findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recientes.size() > iterator) {
                        chooseFromHex(recientes.get(iterator));
                    }
                }
            });
        }

        cargarRecientes();

        iv = findViewById(R.id.color);
        values = findViewById(R.id.muestra);
        values = findViewById(R.id.muestra);
        iv.setDrawingCacheEnabled(true);
        iv.buildDrawingCache(true);
        brillo = findViewById(R.id.brillo);
        brilloValue = findViewById(R.id.brilloValue);

        //TODO borrar test
        TextView test = findViewById(R.id.testrgb);

        values.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modRGB();
            }
        });


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
                    }
                    if (px != 0) {
                        test.setText("px: " + px);
                        ();
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
        TextView test = (TextView) findViewById(R.id.testrgb);
        test.setText(Integer.toString((int)brilloValue));
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
        final Dialog dialog = new Dialog(MainActivity.this);
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
        for (int i = 0; i < recientes.size(); i++) {
            botones.get(i).setBackgroundColor(Color.parseColor(recientes.get(i)));
        }
    }

    public void guardarRecientes() {
        SharedPreferences.Editor sharedPreferencesEditor = getPreferences(MODE_PRIVATE).edit();
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
    protected void onStop() {
        super.onStop();
        guardarRecientes();
    }

    public void settings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
    }
}