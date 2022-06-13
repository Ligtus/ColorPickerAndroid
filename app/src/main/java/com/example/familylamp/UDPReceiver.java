package com.example.familylamp;

import android.os.Handler;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPReceiver extends Thread {
    private DatagramSocket socket;
    private Handler handler;
    private MainFragment mainFragment;

    public UDPReceiver(DatagramSocket socket, Handler handler, MainFragment lampFragment) {
        this.socket = socket;
        this.handler = handler;
        this.mainFragment = lampFragment;
    }

    public void run() {
        try {
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            while (true) {
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                String msgData[] = received.split(",");
                if (msgData[0].equals("Color")) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mainFragment.chooseFromHex(msgData[1]);
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
