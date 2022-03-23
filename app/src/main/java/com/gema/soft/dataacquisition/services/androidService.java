package com.gema.soft.dataacquisition.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.gema.soft.dataacquisition.sockets.ThreadReadWriterIOSocket;
import com.gema.soft.dataacquisition.utils.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class androidService extends Service {
    public static final String TAG = "shq";
    public static Boolean mainThreadFlag = true;
    public static Boolean ioThreadFlag = true;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        android.util.Log.e(TAG,"androidService--onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        android.util.Log.e(TAG,"androidService--onStartCommand()");
        mainThreadFlag = true;
        new Thread(){
            @Override
            public void run() {

            }
        }.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //关闭线程
        mainThreadFlag = false;
        ioThreadFlag = false;
        android.util.Log.e(TAG, Thread.currentThread().getName() + "--"
                + "serverSocket.close()");
        android.util.Log.e(TAG, Thread.currentThread().getName() + "--onDestroy()");
    }
}
