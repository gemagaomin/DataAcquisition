package com.gema.soft.dataacquisition.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.BatteryManager;
import android.widget.Toast;

import com.gema.soft.dataacquisition.activitys.LockScreenActivity;
import com.gema.soft.dataacquisition.activitys.MainActivity;
import com.gema.soft.dataacquisition.sockets.ServiceSocketListener;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.DateUtil;
import com.gema.soft.dataacquisition.utils.MyApplication;

public class USBBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals("android.hardware.usb.action.USB_STATE")){
            if (intent.getExtras().getBoolean("connected")){
                // usb 插入
                 DataUtil.isUSBOnline=true;
              }else{
               //   usb 拔出
                  DataUtil.isUSBOnline=false;
                  if(DataUtil.isSocketOnline){
                      ServiceSocketListener serviceSocketListener=ServiceSocketListener.getInstance(context);
                      serviceSocketListener.close();
                  }
                  MyApplication.closeLockScreen();
              }
        }
    }

}
