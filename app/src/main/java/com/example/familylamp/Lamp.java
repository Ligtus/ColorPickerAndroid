package com.example.familylamp;

import java.net.InetAddress;

public class Lamp {
    private String name;
    private InetAddress ip;

    public Lamp(String name, InetAddress ip) {
        this.name = name;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lamp lamp = (Lamp) o;

        if (this.hashCode() == lamp.hashCode()) return true;
        return ip == lamp.ip;
    }
}