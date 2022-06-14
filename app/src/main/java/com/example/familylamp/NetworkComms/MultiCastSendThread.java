package com.example.familylamp.NetworkComms;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MultiCastSendThread extends Thread{
    private InetAddress group = null;
    private final int PORT = 11555, PING_INTERVAL = 5000;
    private String message = null;
    public MulticastSocket socket;

    public MultiCastSendThread(String message) {
        try {
            this.message = message;
            this.group = InetAddress.getByName("239.255.255.128");
            this.socket = new MulticastSocket(PORT);
            this.socket.joinGroup(this.group);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                socket.send(new DatagramPacket(message.getBytes(), message.length(), group, PORT));
                Thread.sleep(PING_INTERVAL);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
