package com.gema.soft.dataacquisition.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.gema.soft.dataacquisition.activitys.LockScreenActivity;
import com.gema.soft.dataacquisition.activitys.MainActivity;
import com.gema.soft.dataacquisition.services.androidService;
import com.gema.soft.dataacquisition.sockets.ClientSocket;
import com.gema.soft.dataacquisition.sockets.ServiceSocketListener;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.Log;
import com.gema.soft.dataacquisition.utils.MyApplication;
import com.gema.soft.dataacquisition.utils.MyLog;
import com.gema.soft.dataacquisition.utils.StringUtil;

public class ServiceBroadcastReceiver extends BroadcastReceiver {
    private ServiceSocketListener serviceSocketListener;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String atr=intent.getStringExtra("data");
        if(atr!=null&&DataUtil.getInstance().runWorkPojo==null){
            if(serviceSocketListener!=null){
                serviceSocketListener.close();
                serviceSocketListener=null;
            }
            serviceSocketListener =ServiceSocketListener.getInstance(context);
            serviceSocketListener.isCan=true;
            serviceSocketListener.startServerSocket();
            return;
        }else{
            DataUtil.isSocketOnline=false;
        }
        MyApplication.closeLockScreen();
    }
}
