package com.gema.soft.dataacquisition.utils;

import com.alibaba.fastjson.JSONObject;
import com.gema.soft.dataacquisition.BuildConfig;


public class Log {
    private static String TAG="myApp";
    private static boolean sDebug= BuildConfig.DEBUG;
    public static void d(String msg,Object ...args){
        if(!sDebug)
            return;
        android.util.Log.d(TAG, String.format(msg,args));
    }

    public static void dNew(String url,JSONObject msg, Object ...args){
        if(!sDebug)
            return;
        System.out.println(url);
        String str = msg.toJSONString();
        int index = 0;
        for (int i=0;(i+1)*1000<str.length();i++){
            System.out.println(str.substring(i*1000,(i+1)*1000));
            index++;
        }
        System.out.println(str.substring(index*1000));
        android.util.Log.d(TAG+"2019-12-11", String.format(str,args));
    }
}
