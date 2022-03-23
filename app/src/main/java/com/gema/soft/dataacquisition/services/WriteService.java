package com.gema.soft.dataacquisition.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.gema.soft.dataacquisition.task.MsgKey;
import com.gema.soft.dataacquisition.task.MsgManager;
import com.gema.soft.dataacquisition.task.MsgTask;
import com.gema.soft.dataacquisition.utils.BluetoothUtil;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.MyLog;

public class WriteService extends Service {
    private MsgKey writheMsgKey;
    private MsgKey checkMsgKey;
    private ServiceConnection serviceConnection;
    private BluetoothUtil bluetoothUtil;
    private String TAG="WriteService";
    private MyLog myLog;
    public static boolean isServiceRun=true;
    public WriteService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myLog=MyLog.getInstance();
        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    //链接上
                    myLog.writeToFile(TAG+ "StepTempService:建立链接");
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    //断开链接
                    myLog.writeToFile(TAG+ "StepTempService:断开链接");
                    if(bluetoothUtil!=null&&bluetoothUtil.isUseBluetooth){
                        myLog.writeToFile(TAG+ "StepTempService:断开链接、准备启动 WriteTempService");
                        startService(new Intent(WriteService.this, WriteTempService.class));
                    }
                }
            };
        }
        bluetoothUtil=BluetoothUtil.getInstance();
        myLog.writeToFile(TAG+"onCreate: ");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(bluetoothUtil.isUseBluetooth){
            if(bluetoothUtil==null){
                bluetoothUtil=BluetoothUtil.getInstance();
            }
           initConn();
        }
        writeData();
        myLog.writeToFile(TAG+"  onStartCommand: ");
        new Thread(new Runnable() {
            @Override
            public void run() {
                myLog.writeToFile(TAG+ "StepTempService:进入手动断开链接"+ "WriteService.isServiceRun");
                while (WriteService.isServiceRun) {

                }
                myLog.writeToFile(TAG+ "StepTempService:手动断开链接"+ "WriteService.isServiceRun");
                stopSelf();
            }
        }).start();
        return Service.START_STICKY;
    }

    public void initConn(){
        bluetoothUtil.setMultiConnectManager(getApplicationContext());
        bluetoothUtil.getBluetoothAdapter();
        bluetoothUtil.initBle(getApplicationContext());
        myLog.writeToFile(TAG+"  initConn: ");
    }

    public void writeData(){
        writheMsgKey=DataUtil.getInstance().writheMsgKey;
        if(writheMsgKey==null){
            writheMsgKey=new MsgKey();
            writheMsgKey.setIndex(DataUtil.TASK_TYPE_WRITHE);
            MsgTask writheMsgTask=new MsgTask(writheMsgKey,60000);
            MsgManager.startMsgTask(writheMsgKey,writheMsgTask);
            myLog.writeToFile(TAG+"  writeData: ");
        }
        if(checkMsgKey==null){
            checkMsgKey=new MsgKey();
            checkMsgKey.setIndex(DataUtil.TASK_TYPE_CHECK_BLUETOOTH_STATUS);
            MsgTask checkMsgTask=new MsgTask(checkMsgKey,30000);
            MsgManager.startMsgTask(checkMsgKey,checkMsgTask);
            myLog.writeToFile(TAG+"  checkMsgTask: ");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MsgManager.stopMsgTask(writheMsgKey);
        MsgManager.stopMsgTask(checkMsgKey);
        myLog.writeToFile(TAG+"  onDestroy: ");
    }

}
