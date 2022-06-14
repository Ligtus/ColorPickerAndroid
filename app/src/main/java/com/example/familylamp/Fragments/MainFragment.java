package com.example.familylamp.Fragments;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Vibrator;
import android.util.Log;
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

import com.example.familylamp.ConfirmManager;
import com.example.familylamp.NetworkComms.UDPSender;
import com.example.familylamp.R;
import com.example.familylamp.Recientes.Recientes;
import com.example.familylamp.Recientes.RecientesAdapter;
import com.example.familylamp.SQLiteHelper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MainFragment extends Fragment {

    // Connection variables
    private final int PORT = 11556;

    // Color circle view and dialog variables
    ImageView iv;
    ImageButton colorDialogButton, connectButton, settingsButton;
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
    String hex = "#000000", hexUnedit;
    int editingIndex = -1;

    // Brillo variables and views
    TextView brilloValue;
    SeekBar brillo;

    // Saved color variables and buttons
    static ArrayList<String> colors = new ArrayList<String>();

    // Settings variables
    SharedPreferences prefs;
    boolean vibration, showCodes, autoSend, overwrite;
    int vibrationTime, nColors;

    // Vibrator variable
    Vibrator vibrator;

    // SQLite variables
    SQLiteHelper sqLiteHelper;
    SQLiteDatabase colorsDB;

    // Network Variables
    UDPSender udpSender;
    InetAddress lampAddress = null;


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

        // Get all views
        iv = getView().findViewById(R.id.color);
        colorDialogButton = getView().findViewById(R.id.colorDialogButton);
        settingsButton = getView().findViewById(R.id.buttonSettings);
        connectButton = getView().findViewById(R.id.connectButton);
        muestra = getView().findViewById(R.id.muestra);
        iv.setDrawingCacheEnabled(true);
        iv.buildDrawingCache(true);
        brillo = getView().findViewById(R.id.brillo);
        brilloValue = getView().findViewById(R.id.brilloValue);

        // Load application preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Load lamp ip address
        try {
            lampAddress = InetAddress.getByName(prefs.getString("ip", null));
            Log.d("address", "onViewCreated: " + lampAddress.getHostAddress());
        } catch (UnknownHostException e) {
            Toast.makeText(getActivity(), R.string.error_ip_invalid, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        // Load vibration preferences
        vibration = prefs.getBoolean("vibration", true);
        vibrationTime = prefs.getInt("vibrationTime", 15);
        if (vibration) {
            vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        }

        // Load color codes preferences
        showCodes = prefs.getBoolean("showCodes", false);

        // Load automatic send preferences
        autoSend = prefs.getBoolean("auto_send", true);

        // Load overwrite preferences
        overwrite = prefs.getBoolean("overwrite", true);

        // Load number of recientes, if it doesn't exist, set it to 2 (default)
        nColors = Integer.parseInt(prefs.getString("nColors", "2"));

        // Load saved colors from SQLite
        sqLiteHelper = new SQLiteHelper(getActivity());
        colorsDB = sqLiteHelper.getWritableDatabase();

        Cursor cursor = colorsDB.rawQuery("SELECT * FROM " + sqLiteHelper.getTableName(), null);

        try {
            colors.clear(); // Clear the colors array in case it has elements

            while(cursor.moveToNext() && (cursor.getPosition() < BUTTONS_PER_ROW * nColors)) {
                colors.add(cursor.getString(1));
            }
        } finally {
            // Close the cursor
            cursor.close();
        }

        // Bind the recycler view to the adapter
        adapter = new RecientesAdapter(getContext(), recientesList, BUTTONS_PER_ROW);
        recientesRecyclerView = view.findViewById(R.id.recientesRecyclerView);
        recientesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recientesRecyclerView.setAdapter(adapter);

        // Show saved colors
        cargarRecientes();

        // Set app main background color, this is used to reset the color in case other fragments have been opened
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
            if (colors.size() < (BUTTONS_PER_ROW * nColors) || overwrite) {
                animCambiar.start();
            }
            if (!autoSend) {
                sendColor();
            }
        });

        // Set colorDialogButton onClickListener to open color dialog
        Drawable colorDialogImg = colorDialogButton.getDrawable();
        AnimatedVectorDrawable animColorDialog = (AnimatedVectorDrawable) colorDialogImg;
        colorDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animColorDialog.start();
                colorDialog();
            }
        });

        // Set connection button onClickListener to open lamp connection screen and start animation
        View explosionLamps = getView().findViewById(R.id.btnExplosionLamps);
        Drawable connectImg = connectButton.getDrawable();
        AnimatedVectorDrawable animConnect = (AnimatedVectorDrawable) connectImg;
        Animation animationLamps = AnimationUtils.loadAnimation(getContext(), R.anim.btn_explosion_anim);
        animationLamps.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                connect();
                explosionLamps.setVisibility(View.INVISIBLE);
                connectButton.setVisibility(View.INVISIBLE);
                getActivity().findViewById(R.id.nav_host_fragment).setBackgroundColor(getResources().getColor(R.color.settingsBackground));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!animationLamps.hasStarted()) {
                    // TranslationZ defines the depth of the element so that it is not visible over the other elements
                    settingsButton.setTranslationZ(0);
                    explosionLamps.setTranslationZ(1);
                    connectButton.setTranslationZ(1);
                    connectButton.setFadingEdgeLength(0);
                    explosionLamps.setVisibility(View.VISIBLE);
                    explosionLamps.startAnimation(animationLamps);
                    animConnect.start();
                }
            }
        });

        // Set settings button onClickListener to open settings screen and start animation
        View explosionSettings = getView().findViewById(R.id.btnExplosionSettings);
        Drawable imgSettings = settingsButton.getDrawable();
        AnimatedVectorDrawable animSettings = (AnimatedVectorDrawable) imgSettings;
        Animation animationSettings = AnimationUtils.loadAnimation(getContext(), R.anim.btn_explosion_anim);
        animationSettings.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                settings();
                explosionSettings.setVisibility(View.INVISIBLE);
                settingsButton.setVisibility(View.INVISIBLE);
                getActivity().findViewById(R.id.nav_host_fragment).setBackgroundColor(getResources().getColor(R.color.settingsBackground));

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!animationSettings.hasStarted()) {
                    settingsButton.setTranslationZ(1);
                    explosionSettings.setTranslationZ(1);
                    connectButton.setTranslationZ(0);
                    explosionSettings.setVisibility(View.VISIBLE);
                    explosionSettings.startAnimation(animationSettings);
                    animSettings.start();
                }
            }
        });

        // Set color circle image onClickListener
        iv.setOnTouchListener(new View.OnTouchListener() {
            // If the user touches the image, the color is changed with an animation
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    int x = (int) motionEvent.getX();
                    int y = (int) motionEvent.getY();
                    if ((y < view.getHeight()) && (x < view.getWidth()) && (x > 0) && (y > 0)) {
                        bitmap = iv.getDrawingCache();
                        int pixel = bitmap.getPixel(x, y);
                        r = Color.red(pixel);
                        g = Color.green(pixel);
                        b = Color.blue(pixel);
                        chooseColor(true);

                    }
                    return true;

                    // If the user moves the finger, the color is changed accordingly
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    int x = (int) motionEvent.getX();
                    int y = (int) motionEvent.getY();
                    if ((y < view.getHeight()) && (x < view.getWidth()) && (x > 0) && (y > 0)) {
                        bitmap = iv.getDrawingCache();
                        int pixel = bitmap.getPixel(x, y);
                        r = Color.red(pixel);
                        g = Color.green(pixel);
                        b = Color.blue(pixel);
                        chooseColor(false);

                    }
                    return true;

                    // If the user releases the finger and autoSend is enabled, the color is sent to the lamp
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (autoSend) {
                        sendColor();
                    }
                    return true;
                }
                return false;
            }
        });

        // Set brightness seekbar onSeekBarChangeListener
        brillo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // If the user changes the seekbar, the brightness is changed accordingly and the new color is shown
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

            // When the user releases the seekbar, the color is sent to the lamp if autoSend is enabled
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (autoSend) {
                    sendColor();
                }
            }
        });

        // If colors array is not empty, show the first color when the app is opened
        if (colors.size() >= 1) {
            chooseFromHex(colors.get(0));
        }
    }

    // Method to change the color sample
    public void chooseColor(boolean animate) {
        calcBrilloColors();
        // Get the color from the RGB values
        int color = Color.rgb(rBrillo, gBrillo, bBrillo);

        // Get the hex value using the color
        hex = String.format("#%06X", (0xFFFFFF & color));

        // If the color is not black...
        if (!hex.equals("#000000")) {
            if (animate) {
                // If animate is true, the color is changed with an animation
                animMuestraColor();
            } else {
                // Else, the color is changed without animation
                setMuestraColor();
                oldR = rBrillo;
                oldG = gBrillo;
                oldB = bBrillo;
            }
        } else {
            // If the color is black, the color is set to default value
            clearMuestraColor();
        }
    }

    // Method to change the color sample from a hex value
    public void chooseFromHex(String hexReciente) {
        // Set global hex value
        hex = hexReciente;

        // Get the RGB values from the hex value
        int px = Color.parseColor(hex);
        rBrillo = (int) (Color.red(px));
        gBrillo = (int) (Color.green(px));
        bBrillo = (int) (Color.blue(px));

        // Get the brightness value from the RGB values
        calcBrillo(rBrillo, gBrillo, bBrillo);

        // Set the sample color with an animation
        animMuestraColor();
    }

    // Method to send the color value to the lamp
    public void sendColor() {
        if (lampAddress != null && !lampAddress.getHostAddress().equals("::1")) {
            udpSender = new UDPSender(lampAddress, PORT, "Color," + hex);
            udpSender.start();
        } else {
            // If the lamp address is null or loopback, the color is not sent
            Log.d("Lamp", getResources().getString(R.string.error_ip_invalid));
        }
    }

    // Method to send the color value to the lamp, passing the hex value as parameter
    public void sendColor(String hex) {
        if (lampAddress != null && !lampAddress.getHostAddress().equals("::1")) {
            udpSender = new UDPSender(lampAddress, PORT, "Color," + hex);
            udpSender.start();
        } else {
            // If the lamp address is null or loopback, the color is not sent
            Log.d("Lamp", getResources().getString(R.string.error_ip_invalid));
        }
    }

    // Method to update colors array with the new color
    public void updateRecientes(View view, RecyclerView.Adapter adapter, int index) {
        // If the color is not black...
        if (!hex.equals("#000000")) {
            // If the colors array is empty, add the new color
            if (colors.size() == 0) {
                colors.add(hex);
                cargarRecientes();
                adapter.notifyDataSetChanged(); // Notify RecyclerView of data changes
                return;
            }

            // If the color is not already in the array...
            if (!colors.contains(hex)) {
                // If the edit mode is enabled, the color with the index is replaced with the new color
                if (editingIndex != -1) {
                    colors.set(editingIndex, hex);
                    editColor(editingIndex, hex);
                    editingIndex = -1;
                    Toast.makeText(getContext(), R.string.color_edited, Toast.LENGTH_SHORT).show();
                    return;
                }
                // If the colors array is full and overwrite is enabled, remove the first color
                if (colors.size() < (BUTTONS_PER_ROW * nColors) || overwrite) {
                    colors.add(index, hex);
                    if (colors.size() > (BUTTONS_PER_ROW * nColors)) {
                        colors.remove(colors.size() - 1);
                    }
                    cargarRecientes();
                    adapter.notifyDataSetChanged();
                } else {
                    // If the colors array is full and overwrite is disabled, vibrate and notify the user
                    if (vibration) {
                        vibrator.vibrate(vibrationTime);
                    }
                    Toast.makeText(getContext(), R.string.error_no_space, Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // If the color is already in the array, vibrate and notify the user
                if (vibration) {
                    vibrator.vibrate(vibrationTime);
                }
                Toast.makeText(getContext(), R.string.error_already_saved, Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // If the color is black, vibrate and notify the user
            if (vibration) {
                vibrator.vibrate(vibrationTime);
            }
            Toast.makeText(getContext(), R.string.error_no_color, Toast.LENGTH_SHORT).show();
        }
    }

    // Method to overwrite a color based on the index passed as parameter
    public void overwrite(int index) {
        // If the colors array size is greater than the index...
        if (colors.size() > index) {
            colors.set(index, hex); // Set the color at the index to the new color
            cargarRecientes();
            adapter.notifyDataSetChanged(); // Notify RecyclerView of data changes
            Toast.makeText(getContext(), R.string.color_overwritten, Toast.LENGTH_SHORT).show();
        }
    }

    // Method to get the higher of three values passed as parameters
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

    // Method to calculate brightness value from RGB values passed as parameters
    public void calcBrillo(int r, int g, int b) {
        // Get the higher of the three values
        int higher = getHigher(r, g, b);
        // Calculate the brightness value and round it to the nearest integer
        double brilloValue = ((double)higher / (double)(255)) * 100;
        // Set the brightness value with an animation
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

    // Method to calculate final RGB values from the brightness value and the raw RGB values
    public void calcBrilloColors() {
        float brilloColor = Float.parseFloat(brilloValue.getText().toString()) / 100;
        rBrillo = (int) (r * brilloColor);
        gBrillo = (int) (g * brilloColor);
        bBrillo = (int) (b * brilloColor);
    }

    // Method to calculate raw RGB values from the brightness value and the final RGB values
    public void calcColorsFromBrillo() {
        float brilloColor = Float.parseFloat(brilloValue.getText().toString()) / 100;
        r = (int) (rBrillo / brilloColor);
        g = (int) (gBrillo / brilloColor);
        b = (int) (bBrillo / brilloColor);
    }


    /**
     * Method to calculate color in the colorDialog, just as ChooseColor
     * @see #chooseColor(boolean) ()
     */
    public void chooseColorDialog(View colorDialog, TextView valuesDialog) {
        int color = Color.rgb(rBrillo, gBrillo, bBrillo);

        hex = String.format("#%06X", (0xFFFFFF & color));

        if (!hex.equals("#000000")) {
            calcBrillo(rBrillo, gBrillo, bBrillo);
            setMuestraColor();
            colorDialog.setBackgroundColor(color);
        } else {
            clearMuestraColor();
            colorDialog.setBackgroundColor(getResources().getColor(R.color.nulo));
        }
        // If showCodes is enabled, show the RGB and hex values
        if (showCodes) {
            valuesDialog.setText("RGB\n" + rBrillo + ", " + gBrillo + ", " + bBrillo + "\nHEX\n" + hex);
        }
    }

    // Method to invoke the color picker dialog
    public void colorDialog() {
        final Dialog dialog = new Dialog(this.getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.rgb_dialog);
        // Set drawable background, to get the rounded corners
        dialog.getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dialog_background, null));

        // Get all the views from the dialog
        TextView valuesDialog = dialog.findViewById(R.id.valuesDialog);
        View colorDialog = dialog.findViewById(R.id.colorDialog);
        Button confirm = dialog.findViewById(R.id.cancelDialog);

        // Get R seekBar and set its progress to the current R value
        SeekBar sbR = dialog.findViewById(R.id.seekBar_0);
        sbR.setProgress(rBrillo);
        sbR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                rBrillo = progress;
                chooseColorDialog(colorDialog, valuesDialog);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (autoSend) {
                    sendColor();
                }
            }
        });

        // Get G seekBar and set its progress to the current G value
        SeekBar sbG = dialog.findViewById(R.id.seekBar_1);
        sbG.setProgress(gBrillo);
        sbG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                gBrillo = progress;
                chooseColorDialog(colorDialog, valuesDialog);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (autoSend) {
                    sendColor();
                }
            }
        });

        // Get B seekBar and set its progress to the current B value
        SeekBar sbB = dialog.findViewById(R.id.seekBar_2);
        sbB.setProgress(bBrillo);
        sbB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                bBrillo = progress;
                chooseColorDialog(colorDialog, valuesDialog);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (autoSend) {
                    sendColor();
                }
            }
        });

        // Calculate raw RGB values from the brightness value and the final RGB values
        calcColorsFromBrillo();

        // Show the color in the colorDialog
        chooseColorDialog(colorDialog, valuesDialog);

        // Set confirm button listener
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateRecientes(getView(), adapter, 0);
                // If autoSend is enabled, send the color
                if (autoSend) {
                    sendColor();
                }
                // Close the dialog
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }

    // Method to load the recent colors into the recyclerView
    public void cargarRecientes() {
        /*
         * If the number of recientes in preferences is not equal to the number of saved colors,
         * clear the list and add the saved colors.
         */
        recientesList.clear();
        for (int i = 0, j = 0; i < nColors; i++) {
            // Each reciente is a row of buttons
            String[] hexCodes = new String[BUTTONS_PER_ROW];
            for (int k = 0; k < BUTTONS_PER_ROW; k++, j++) {
                if (j < colors.size()) {
                    hexCodes[k] = colors.get(j);
                } else {
                    hexCodes[k] = "#000000";
                }
            }

            // Create a new reciente and add it to the list
            Recientes reciente = new Recientes(hexCodes, new ButtonListener());
            recientesList.add(reciente);
        }

    }

    // Method to save the recent colors into the SQLite database
    public void guardarRecientes() {
        // Delete all the colors from the database
        colorsDB.execSQL("DELETE FROM " + sqLiteHelper.getTableName());

        // Insert the colors into the database
        for(String hex : colors) {
            ContentValues values = new ContentValues();
            values.put("hexcode", hex);
            colorsDB.insert(sqLiteHelper.getTableName(), null, values);
        }
    }

    // Method to clear the color from the sample
    private void clearMuestraColor() {
        muestra.setText("");
        muestra.setBackgroundColor(getResources().getColor(R.color.nulo));
    }

    // Method to edit a color in the recyclerView
    private void editColor(int index, String hex) {
        Recientes reciente = recientesList.get(index/BUTTONS_PER_ROW);
        String[] hexCodes = reciente.getHexCodes();
        hexCodes[index%BUTTONS_PER_ROW] = hex;
        reciente.setHexCodes(hexCodes);
        recientesList.set(index/BUTTONS_PER_ROW, reciente);
        adapter.notifyItemChanged(index/BUTTONS_PER_ROW); // Notify the adapter that the item has changed
    }

    // Method to set the color of the sample
    private void setMuestraColor() {
        // If showCodes is enabled, set the text to the hex code
        if (showCodes) {
            muestra.setText("RGB\n" + rBrillo + ", " + gBrillo + ", " + bBrillo + "\nHEX\n" + hex);
        }

        // Also set the background color the color that is in editing mode
        if (editingIndex != -1) {
            editColor(editingIndex, hex);
        }

        muestra.setBackgroundColor(Color.rgb(rBrillo, gBrillo, bBrillo));
        // Also set the text color in case showCodes is enabled
        chooseTextColor();
    }

    // Method to animate the transition between the colors
    private void animMuestraColor() {
        // Calculate the color to animate to
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

    // Method that returns if a value is in between the min and max values
    private boolean isBetween(double value, double min, double max) {
        return value >= min && value <= max; // true if value is between min and max
    }

    // Method to calculate the RGB values for the text so it always stays visible
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

    // Method that sets the text color of the sample
    private void chooseTextColor(){
        double tmpR, tmpG, tmpB;
        double brillo = Double.parseDouble(brilloValue.getText().toString()) / 100;
        // Get temporary RGB values from the method described above
        tmpR = calcValue(r, brillo);
        tmpG = calcValue(g, brillo);
        tmpB = calcValue(b, brillo);
        muestra.setTextColor(Color.rgb((int)tmpR, (int)tmpG, (int)tmpB));
    }

    // Method to delete a color from the recyclerView
    private void deleteColor(int index) {
        colors.remove(index);
        cargarRecientes();
        adapter.notifyDataSetChanged(); // Notify the adapter that the entire list has changed, since the number of items has changed
        Toast.makeText(getContext(), R.string.color_deleted, Toast.LENGTH_SHORT).show();
    }

    /*
     * An inner class to handle the clicks on the recientes.
     * This class is passed as a parameter to the Recientes constructor,
     * then the Adapter binds its execute method to the onClickListener.
     */
    public class ButtonListener {
        // Default constructor
        ButtonListener() {};

        // Method to handle the click on a color
        public void execute(int iterator, Button btn) {
            // Call ConfirmManager to create a confirmation dialog
            ConfirmManager cm = new ConfirmManager(getContext());

            // Create a new PopupMenu
            PopupMenu popup = new PopupMenu(getActivity(), btn, Gravity.CENTER, 0, R.style.PopUpMenuCustom);
            // Set ForceShowIcon to true to show the icons of the options
            popup.setForceShowIcon(true);
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

            // If editing mode is enabled, add the option to cancel edition
            if (editingIndex != -1) {
                popup.getMenu().findItem(R.id.popup_edit).setVisible(false);
                popup.getMenu().findItem(R.id.popup_cancel_edit).setVisible(true);
            }
            // Set popup menu onClickListener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.popup_send:
                            sendColor(colors.get(iterator));
                            return true;
                        case R.id.popup_set:
                            chooseFromHex(colors.get(iterator));
                            return true;
                        case R.id.popup_edit:
                            editingIndex = iterator;
                            hexUnedit = colors.get(iterator);
                            chooseFromHex(colors.get(iterator));
                            Toast.makeText(getContext(), R.string.color_editing, Toast.LENGTH_SHORT).show();
                            return true;
                        case R.id.popup_cancel_edit:
                            editColor(editingIndex, hexUnedit);
                            editingIndex = -1;
                            Toast.makeText(getContext(), R.string.color_edit_canceled, Toast.LENGTH_SHORT).show();
                            return true;
                        case R.id.popup_overwrite:
                            if (!colors.contains(hex)) {
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

            // When the popup is shown, vibrate if the vibration is enabled
            if (vibration) {
                vibrator.vibrate(vibrationTime);
            }
            // Show the popup
            popup.show();
        }
    }

    // On stop method to save the colors
    @Override
    public void onStop() {
        super.onStop();
        guardarRecientes();
    }

    // On destroy method to close the database
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (colorsDB != null) {
            colorsDB.close();
        }
        if (sqLiteHelper != null) {
            sqLiteHelper.close();
        }
    }

    // Method to navigate to the settings fragment
    public void settings() {
        NavHostFragment.findNavController(this).navigate(R.id.action_nav_home_to_nav_settings);
    }

    // Method to navigate to the lamp fragment
    public void connect() {
        NavHostFragment.findNavController(this).navigate(R.id.action_mainFragment_to_lampFragment);
    }
}