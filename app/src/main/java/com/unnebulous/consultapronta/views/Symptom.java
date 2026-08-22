package com.unnebulous.consultapronta.views;

public class Symptom {

    private final int id;
    private final String description;
    private final String date;
    private final String location;
    private final int intensity;
    private final boolean persistent;

    public Symptom(
            int id,
            String description,
            String date,
            String location,
            int intensity,
            boolean persistent
    ) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.location = location;
        this.intensity = intensity;
        this.persistent = persistent;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public int getIntensity() {
        return intensity;
    }

    public boolean isPersistent() {
        return persistent;
    }
}
