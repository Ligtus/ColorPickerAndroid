package com.example.familylamp.NetworkComms;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class UDPSender extends Thread {
    private InetAddress address = null;
    private int port;
    private String message = "";

    public UDPSender(InetAddress address, int port, String message) {
        this.address = address;
        this.port = port;
        this.message = message;
    }

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
