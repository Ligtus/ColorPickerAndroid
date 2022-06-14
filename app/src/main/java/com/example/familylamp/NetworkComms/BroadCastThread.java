package com.example.familylamp.NetworkComms;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class BroadCastThread extends Thread {
    private DatagramSocket socket;
    private String message;
    private int port;

    public BroadCastThread(String message, int port) {
        try {
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
            this.message = message;
            this.port = port;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            byte[] data = message.getBytes();
            socket.send(new java.net.DatagramPacket(data, data.length, java.net.InetAddress.getByName("255.255.255.255"), port));
            socket.close();
            this.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
