package com.example.familylamp.Lamps;

import java.net.InetAddress;

public class Lamp {
    private String name;
    private InetAddress ip;

    // Constructor
    public Lamp(String name, InetAddress ip) {
        this.name = name;
        this.ip = ip;
    }

    // Getters and setters
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

    // Override hashcode and equals to compare lamps
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