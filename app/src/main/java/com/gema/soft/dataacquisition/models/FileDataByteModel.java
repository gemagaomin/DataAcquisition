package com.gema.soft.dataacquisition.models;

public class FileDataByteModel {
    private long time;
    private float x;
    private float y;
    private float z;

    public FileDataByteModel() {
    }

    public FileDataByteModel(long time, float x, float y, float z) {
        this.time = time;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "FileDataByteModel{" +
                "time=" + time +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
