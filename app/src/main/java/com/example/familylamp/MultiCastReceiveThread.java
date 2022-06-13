package com.example.familylamp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MultiCastReceiveThread extends Thread {
    private InetAddress group = null;
    private Handler handler;
    private final int PORT = 11555;
    private String message = null;

    public MulticastSocket socket;
    LampFragment lampFragment;

    public MultiCastReceiveThread(Handler handler, LampFragment lampFragment) {
        try {
            this.group = InetAddress.getByName("239.255.255.128");
            this.socket = new MulticastSocket(PORT);
            this.socket.joinGroup(this.group);
            this.lampFragment = lampFragment;
            this.handler = handler;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {

        while(true) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                socket.receive(packet);
                message = new String(packet.getData(), 0, packet.getLength());
                Log.d("message", message);
                if (message.split(",")[0].equals("Lamp")) {
                    String[] lampData = message.split(",");
                    String lampName = lampData[1];
                    InetAddress lampIP = InetAddress.getByName(lampData[2]);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Lamp lamp = new Lamp(lampName, lampIP);
                            lampFragment.addLamp(lamp);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
