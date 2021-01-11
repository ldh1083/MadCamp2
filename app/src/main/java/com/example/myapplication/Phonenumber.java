package com.example.myapplication;

public class Phonenumber implements Comparable<Phonenumber> {
    private String name;
    private String number;
    private boolean at_local;
    private boolean at_server;

    public Phonenumber (String name, String number, boolean at_local, boolean at_server) {
        this.name = name;
        this.number = number;
        this.at_local = at_local;
        this.at_server = at_server;
    }

    @Override
    public int compareTo(Phonenumber phonenumber) { return this.name.compareTo(phonenumber.name); }

    public String getName() {
        return name;
    }
    public String getNumber() {
        return number;
    }
    public boolean getAtLocal() {
        return at_local;
    }
    public boolean getAtServer() {
        return at_server;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public void setAtLocal(boolean at_local) {
        this.at_local = at_local;
    }
    public void setAtServer(boolean at_server) {
        this.at_server = at_server;
    }
}
