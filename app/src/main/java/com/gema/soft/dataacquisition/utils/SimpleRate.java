package com.gema.soft.dataacquisition.utils;

public class SimpleRate {
    /**
     * 10HZ
     */
    private int SENSOR_RATE_TEN=100000;
    /**
     * 50Hz
     */
    private int SENSOR_RATE_NORMAL = 20000;
    /**
     * 80Hz
     */
    private int SENSOR_RATE_MIDDLE = 12500;
    /**
     * 100Hz
     */
    private int SENSOR_RATE_FAST = 10000;

    public SimpleRate() {
    };
    /**
     * 10Hz
     *
     * @return
     */
    public int get_SENSOR_RATE_TEN() {
        return SENSOR_RATE_TEN;
    }

    /**
     * 50Hz
     * @return
     */
    public int get_SENSOR_RATE_NORMAL() {
        return this.SENSOR_RATE_NORMAL;
    }

    /**
     * 80Hz
     *
     * @return
     */
    public int get_SENSOR_RATE_MIDDLE() {
        return this.SENSOR_RATE_MIDDLE;
    }

    /**
     * 100Hz
     *
     * @return
     */
    public int get_SENSOR_RATE_FAST() {
        return this.SENSOR_RATE_FAST;
    }

}
