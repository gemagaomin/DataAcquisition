package com.gema.soft.dataacquisition.formatter;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyXFormatter extends ValueFormatter {
    DateFormat bf = new SimpleDateFormat("mm:ss.SSS");

    public MyXFormatter() {

    }

    @Override
    public String getFormattedValue(float value) {
        super.getFormattedValue(value);
        Date date=new Date((long) value);
        return bf.format(date);
    }
}
