package com.gema.soft.dataacquisition.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    private final String LONG="yyyy-MM-dd HH:mm:ss";
    private final String LONG_S="yyyy-MM-dd HH:mm:ss.SSS";
    private SimpleDateFormat simpleDateFormat;
    private static DateUtil dateUtil;

    private DateUtil() {
    }

    public static DateUtil getInstance(){
        if(dateUtil==null){
            synchronized (DateUtil.class){
                if(dateUtil==null){
                    dateUtil=new DateUtil();
                }
            }
        }
        return dateUtil;
    }
    public String getDataLong(Date date){
        simpleDateFormat=new SimpleDateFormat(LONG);
        return simpleDateFormat.format(date);
    }

    public String getDataLongS(Date date){
        simpleDateFormat=new SimpleDateFormat(LONG_S);
        return simpleDateFormat.format(date);
    }

    public String getDataLong(long time){
        Date date=new Date(time);
        simpleDateFormat=new SimpleDateFormat(LONG);
        return simpleDateFormat.format(date);
    }

}
