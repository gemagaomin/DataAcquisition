package com.gema.soft.dataacquisition.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.widget.Toast;


public class MyException extends Exception {
    private static boolean sDebug= false;
    public static String SERVER_MESSAGE="服务器维护中";
    public static String INTERNET_MESSAGE="网络链接错误";
    private MyLog myLog;
    public void buildException(Exception e){
        //TODO 通过不同的异常，做不同的提示
        Log.d(e.toString());
    }

    public void buildException(String str){
        Log.d(str);
    }


    public void buildException(Exception e,Context context){
        //TODO 通过不同的异常，做不同的提示
        Log.d(e.toString());
        Looper.prepare();
        String str="";
        if(e!=null)
            str=e.getMessage();
        if(!sDebug){
            str="网络链接错误！";
        }
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    public void buildExceptionToLogFile(String message){
        //TODO 通过不同的异常，做不同的提示
        if(myLog==null){
            myLog=MyLog.getInstance();
        }
        myLog.writeToFile(message);
    }
}
