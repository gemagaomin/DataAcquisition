package com.gema.soft.dataacquisition.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class DownloadReceiver extends BroadcastReceiver {
    private Context context;
    private Message message;
    private Long myId;

    public DownloadReceiver(Context context) {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
         Long r=intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1);
         if(myId!=null&&r.equals(myId)){
             message.getMessage("完成");
         }
    }

    public interface  Message {
        void getMessage(String s);
    }

    public void setMyId(Long myId) {
        this.myId = myId;
    }

    public void setMessage(Message message){
        this.message=message;
    }
}
