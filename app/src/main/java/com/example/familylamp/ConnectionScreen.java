package com.example.familylamp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.net.InetAddress;

public class ConnectionScreen extends AppCompatActivity {
    String ip = "";
    TextView ipText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_screen);
    }

    /*public void sendMessage(View view) {
        BroadCastThread bcast = new BroadCastThread("Holi", 11555);
        bcast.start();
    }

    public void sendText(View view) {
        TextView textView = (TextView) findViewById(R.id.sendText);
        String text = textView.getText().toString();
        InetAddress address = null;
        int port = 11555;
        try {
            address = InetAddress.getByName(ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        UDPThread sender = new UDPThread(address, port, text);
        sender.start();
    }


    public void Connect(View view){
        Intent i = new Intent(this, com.example.familylamp.MainActivity.class);
        SharedPreferences.Editor sharedPreferencesEditor = getPreferences(MODE_PRIVATE).edit();
        sharedPreferencesEditor.putString("ip", ip);
        sharedPreferencesEditor.commit();
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    //function to go to mainactivity
    public void goToMain(View view){
        Intent i = new Intent(this, com.example.familylamp.MainActivity.class);
        startActivity(i);
    }*/
}
