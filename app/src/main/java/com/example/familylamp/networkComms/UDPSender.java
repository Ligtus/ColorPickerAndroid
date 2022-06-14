package com.example.familylamp.networkComms;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSender extends Thread {
    private final InetAddress address;
    private final int port;
    private final String message;

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
