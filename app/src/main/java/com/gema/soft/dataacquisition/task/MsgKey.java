package com.gema.soft.dataacquisition.task;

public class MsgKey {
    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MsgKey) {
            MsgKey msgKey = (MsgKey) obj;
            return this.index == msgKey.getIndex();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return String.valueOf(this.index).hashCode();
    }
}
