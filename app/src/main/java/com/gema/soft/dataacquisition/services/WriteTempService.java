package com.gema.soft.dataacquisition.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.gema.soft.dataacquisition.task.MsgKey;
import com.gema.soft.dataacquisition.task.MsgManager;
import com.gema.soft.dataacquisition.task.MsgTask;
import com.gema.soft.dataacquisition.utils.BluetoothUtil;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.Log;
import com.gema.soft.dataacquisition.utils.MyLog;

public class WriteTempService extends Service {
    private ServiceConnection serviceConnection;
    private BluetoothUtil bluetoothUtil;
    private MsgKey writheMsgKey;
    private MyLog myLog;
    private String TAG="WriteTempService";
    public static Boolean isServiceRun=true;
    public WriteTempService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
                    myLog.writeToFile(TAG+"StepTempService:建立链接");
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    //断开链接
                    myLog.writeToFile(TAG+"StepTempService:断开链接");
                    if(bluetoothUtil!=null&&bluetoothUtil.isUseBluetooth){
                        myLog.writeToFile(TAG+"StepTempService:断开链接，尝试启动 WriteService");
                        startService(new Intent(WriteTempService.this, WriteService.class));
                    }
                }
            };
        }
        bluetoothUtil=BluetoothUtil.getInstance();
        myLog.writeToFile(TAG+" onCreate:");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(bluetoothUtil.isUseBluetooth){
            if(bluetoothUtil==null){
                //todo 测试后去掉20210322
                bluetoothUtil=BluetoothUtil.getInstance();
            }
            initConn();
        }
        writeData();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (WriteTempService.isServiceRun) {

                }
                stopSelf();
            }
        }).start();
        myLog.writeToFile(TAG+" onStartCommand:");
        return Service.START_STICKY;
    }

    public void initConn(){
        bluetoothUtil.setMultiConnectManager(getApplicationContext());
        bluetoothUtil.getBluetoothAdapter();
        bluetoothUtil.initBle(getBaseContext());
        bluetoothUtil.connectMac(getBaseContext());
        bluetoothUtil.connectBluetooth();
        myLog.writeToFile(TAG+" initConn:");
    }

    public void writeData(){
        writheMsgKey= DataUtil.getInstance().writheMsgKey;
        if(writheMsgKey==null){
            writheMsgKey=new MsgKey();
            writheMsgKey.setIndex(DataUtil.TASK_TYPE_WRITHE);
            MsgTask writheMsgTask=new MsgTask(writheMsgKey,10000);
            MsgManager.startMsgTask(writheMsgKey,writheMsgTask);
            myLog.writeToFile(TAG+" writeData:");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MsgManager.stopMsgTask(writheMsgKey);
        bluetoothUtil.closeBluetooth();
        myLog.writeToFile(TAG+" onDestroy:");
    }
}
