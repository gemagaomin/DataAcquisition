package com.gema.soft.dataacquisition.models;

public class CalculateDataModel {
    private long time;
    private float y;
    private float ar=0f;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getAr() {
        return ar;
    }

    public void setAr(float ar) {
        this.ar = ar;
    }
}
