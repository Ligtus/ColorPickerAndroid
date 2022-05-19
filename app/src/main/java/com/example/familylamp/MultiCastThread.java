package com.example.familylamp;

import android.view.View;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MultiCastThread extends Thread {
    private InetAddress mIpAddress = null;
    private int mPort = 11555;
    private String mMessageSend, mMessageReceive = null;
    private MulticastSocket mSocket;
    private TextView mTextView;

    public MultiCastThread(String message, TextView textView) {
        try {
            mIpAddress = InetAddress.getByName("228.5.6.7");
            mMessageSend = message;
            mTextView = textView;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            mSocket = new MulticastSocket(mPort);
            mSocket.joinGroup(mIpAddress);
            byte[] buffer = mMessageSend.trim().getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, mIpAddress, mPort);
            mSocket.send(packet);

            while (true) {
                byte[] buf = new byte[256];
                DatagramPacket response = new DatagramPacket(buf, buf.length, mIpAddress, mPort);
                mSocket.receive(response);
                mMessageReceive = new String(response.getData());

                if (!mMessageReceive.trim().equals(mMessageSend)) {
                    mTextView.post(new Runnable() {
                           @Override
                           public void run() {
                               mTextView.setText(mMessageReceive);
                           }
                       }
                    );
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getResponse() {
        return mMessageReceive;
    }
}
