package com.gema.soft.dataacquisition.task;

import android.util.Log;

import com.gema.soft.dataacquisition.models.FileDataByteModel;
import com.gema.soft.dataacquisition.queues.MyQueue;
import com.gema.soft.dataacquisition.utils.BluetoothUtil;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.FileUtil;
import com.gema.soft.dataacquisition.utils.MyLog;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class MsgTask extends TimerTask {
    private long deltaTime;  //时间增量，及任务执行等待时间
    private MsgKey msgKey;
    private MsgProcessor msgProcessor = new MsgProcessor();
    private FileUtil fileUtil;
    private String TAG="MsgTask";
    private MyLog myLog=MyLog.getInstance();

    public MsgTask(MsgKey msgKey,long deltaTime) {
        super();
        this.msgKey = msgKey;
        this.deltaTime = deltaTime;
        fileUtil= FileUtil.getInstance();
    }

    public long getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(long deltaTime) {
        this.deltaTime = deltaTime;
    }

    public MsgKey getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(MsgKey msgKey) {
        this.msgKey = msgKey;
    }

    @Override
    public void run() {//等待时间到了以后，就执行
        int index = msgKey.getIndex();
        switch (index){
            case DataUtil.TASK_TYPE_WRITHE:
                fileUtil.writheData();
                break;
            case DataUtil.TASK_TYPE_READ:
                break;
            case DataUtil.TASK_TYPE_GET_DATA_FROM_BLUETOOTH:
                //getDataFromBluetooth();
                break;
            case DataUtil.TASK_TYPE_CHECK_BLUETOOTH_STATUS:
                checkBluetoothStatus();
                break;
        }
        msgProcessor.dealOverTimeMsg(index);
       // MsgManager.removeMsgTask(msgKey);
        //this.cancel();
    }


    private void checkBluetoothStatus(){
        BluetoothUtil bluetoothUtil=BluetoothUtil.getInstance();
        if(bluetoothUtil.bluetoothLineStatus&&!bluetoothUtil.isUseBluetooth)
            return;
        try{
            bluetoothUtil.reConnectBluetooth();
        }catch (Exception e){
            myLog.writeToFile("checkBluetoothStatus() 进入 reConnectBluetooth() 报错");
        }
    }

}
