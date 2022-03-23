package com.gema.soft.dataacquisition.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class AppOrServiceStatusUtil {
    public static  boolean ServiceIsRunning (Context context,String serviceClassName){
        ActivityManager activityManager= (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> list=activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo r:list) {
            if(r.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }
}
