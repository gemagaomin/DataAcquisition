package com.gema.soft.dataacquisition.models;


public class MyPointModel{
    private float x;
    private float y;
    private int type=-1;

    public MyPointModel() {
    }

    public MyPointModel(float y) {
        this.y = y;
    }

    public MyPointModel(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public MyPointModel(float x, float y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
