package com.gema.soft.dataacquisition.models;

public class SmoothBumpModel {
    private Integer level;
    private long zone_st;
    private long zone_et;
    private float maxEffectiveValue;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }


    public long getZone_st() {
        return zone_st;
    }

    public void setZone_st(long zone_st) {
        this.zone_st = zone_st;
    }

    public long getZone_et() {
        return zone_et;
    }

    public void setZone_et(long zone_et) {
        this.zone_et = zone_et;
    }

    public float getMaxEffectiveValue() {
        return maxEffectiveValue;
    }

    public void setMaxEffectiveValue(float maxEffectiveValue) {
        this.maxEffectiveValue = maxEffectiveValue;
    }
}
