package com.example.familylamp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class UDPThread extends Thread {
    private InetSocketAddress address = null;
    private int port = 11555;
    private String message = "";

    public UDPThread(InetAddress address, int port, String message) {
        this.address = new InetSocketAddress(address, port);
        this.port = port;
        this.message = message;
    }

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(address);
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address);
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
