package com.gema.soft.dataacquisition.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.blakequ.bluetooth_manager_lib.BleManager;
import com.blakequ.bluetooth_manager_lib.connect.BluetoothSubScribeData;
import com.blakequ.bluetooth_manager_lib.connect.ConnectState;
import com.blakequ.bluetooth_manager_lib.connect.ConnectStateListener;
import com.blakequ.bluetooth_manager_lib.connect.multiple.MultiConnectManager;
import com.blakequ.bluetooth_manager_lib.scan.BluetoothScanManager;
import com.blakequ.bluetooth_manager_lib.scan.ScanOverListener;
import com.blakequ.bluetooth_manager_lib.scan.bluetoothcompat.ScanCallbackCompat;
import com.blakequ.bluetooth_manager_lib.scan.bluetoothcompat.ScanResultCompat;
import com.gema.soft.dataacquisition.enums.FileEnum;
import com.gema.soft.dataacquisition.models.CalculateDataModel;
import com.gema.soft.dataacquisition.models.FileDataByteModel;
import com.gema.soft.dataacquisition.models.RefreshDatas;
import com.gema.soft.dataacquisition.queues.MyQueue;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothUtil {
    private final String TAG="BluetoothUtil";
    public MyQueue myQueue;
    private ArrayList<String> connectDeviceMacList;
    public String bluetoothStr="0000ffe5-0000-1000-8000-00805f9a34fb";
    public String bluetoothCharacteristicStr="0000ffe4-0000-1000-8000-00805f9a34fb";
    public String getBluetoothWriteStr="0000ffe9-0000-1000-8000-00805f9a34fb";
    public MultiConnectManager multiConnectManager;
    public boolean bIdle = true;
    public String selectDeviceMacs="";
    public MyLog myLog;
    private BluetoothAdapter bluetoothAdapter;   //蓝牙适配器
    public byte[] cmdReadMagCali = new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x27, (byte) 0x03, (byte) 0x00};//读取内容
    public byte[] cmdMagCali = new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x03, (byte) 0x08, (byte) 0x00};//设置回传频率
    public byte[] cmdSaveMagCali = new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x00, (byte) 0x00, (byte) 0x00};//保存设置
    public ArrayList<BluetoothGatt> gattArrayList;
    private static BluetoothUtil bluetoothUtil;
    private ArrayList<String> deviceList;
    public BluetoothScanManager scanManager ;
    public boolean bluetoothLineStatus=false;//蓝牙链接状态
    public boolean isUseBluetooth=true;//是否启用蓝牙功能
    public long startTime=0;
    public long endTime=0;
    public boolean isFirstLine=true;
    public BluetoothGattCallback bluetoothGattCallback=null;
    public ConnectStateListener connectStateListener=null;
    private DataUtil dataUtil;
    private CalculateUtil calculateUtil;
    private FileDataByteModel fileDataByteModel=null;
    private CalculateDataModel calculateDataModel=null;
    private Context _context;

    public void setBluetoothGattCallback() {
        if(this.bluetoothGattCallback==null){
            this.bluetoothGattCallback = new BluetoothGattCallback() {
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                    //todo 测试数据
                    dealCallDatasNew(gatt, characteristic);
                    //dealCallDatas(gatt, characteristic);
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    byte[] value = characteristic.getValue();
                    String sdata = "";
                    if(value!=null&&value.length>0){
                        super.onCharacteristicWrite(gatt, characteristic, status);
                        for (int i = 0; i < value.length; i++) {
                            sdata = sdata + String.format("%02x", (0xff & value[i]));
                        }
                    }
                    Log.e("--", "校准指令 = " + sdata);
                    myLog.writeToFile(TAG+"校准指令 = " + sdata);
                }
            };
        }
    }

    public void setConnectStateListener() {
        if(this.connectStateListener==null){
            this.connectStateListener =new ConnectStateListener() {
                @Override
                public void onConnectStateChanged(String address, ConnectState state) {
                    String str="未连接";
                    switch (state) {
                        case CONNECTING:
                            str="设备:" + address + "连接状态:正在连接";
                            bluetoothLineStatus=false;
                            break;
                        case CONNECTED:
                            str="设备:" + address + "连接状态:成功";
                            if(isUseBluetooth){
                                isFirstLine=true;
                                bluetoothLineStatus=true;
                                selectDeviceMacs=address;
                            }
                            break;
                        case NORMAL:
                            str="设备:" + address + "连接状态:失败";
                            bluetoothLineStatus=false;
                            break;
                    }
                    if(!isUseBluetooth){
                        multiConnectManager.close(address);
                        multiConnectManager.removeDeviceFromQueue(address);
                        str="未连接";
                    }
                    DataUtil.getInstance().lineStatus=str;
                }
            };
        }
    }

    private BluetoothUtil() {
        connectDeviceMacList=new ArrayList<>();
        myQueue=MyQueue.getInstance();
        gattArrayList=new ArrayList<>();
        deviceList=new ArrayList<>();
        myLog=MyLog.getInstance();
        dataUtil=DataUtil.getInstance();
        calculateUtil=CalculateUtil.getInstance();
        setBluetoothGattCallback();
        setConnectStateListener();
    }

    public ArrayList<String> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(ArrayList<String> deviceList) {
        this.deviceList=deviceList;
    }

    public static BluetoothUtil getInstance(){
        if(bluetoothUtil==null){
            synchronized (BluetoothUtil.class){
                if(bluetoothUtil==null){
                    bluetoothUtil=new BluetoothUtil();
                }
            }
        }
        return bluetoothUtil;
    }

    public ArrayList<String> getConnectDeviceMacList() {
        return connectDeviceMacList;
    }

    public void setConnectDeviceMacList(ArrayList<String> connectDeviceMacList) {
        this.connectDeviceMacList=connectDeviceMacList;
    }

    public MultiConnectManager getMultiConnectManager() {
        return multiConnectManager;
    }

    private BluetoothSubScribeData readBluetoothSubScribeData;
    public void setMultiConnectManager(Context context) {
        if(this.multiConnectManager==null){
            this.multiConnectManager = BleManager.getMultiConnectManager(context);
            _context=context;
            setConnectStateListener();
            multiConnectManager.addConnectStateListener(connectStateListener);
            /**
             * 数据回调
             */
            setBluetoothGattCallback();
            multiConnectManager.setBluetoothGattCallback(bluetoothGattCallback);
            multiConnectManager.setServiceUUID(bluetoothStr);
            readBluetoothSubScribeData=new BluetoothSubScribeData.Builder().setCharacteristicNotify(UUID.fromString(bluetoothCharacteristicStr)).build();
            multiConnectManager.addBluetoothSubscribeData(readBluetoothSubScribeData);
        }

    }

    public BluetoothAdapter getBluetoothAdapter(){
        if(bluetoothAdapter==null){
            bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        }
        return bluetoothAdapter;
    }

    public boolean removeDeviceToQueue(String mac){
        List<BluetoothDevice> devices=multiConnectManager.getConnectedDevice();
        if(devices!=null&&devices.stream().filter(d->mac.equals(d.getAddress())).count()>0){
            multiConnectManager.removeDeviceFromQueue(mac);
        }
        return true;
    }
    /**
     * 连接需要连接的传感器
     *
     * @param
     */
    public boolean connectBluetooth() {
        try{
            myLog.writeToFile(" 进入 connectBluetooth()");
            if(bluetoothLineStatus)
                return true;
            if(connectDeviceMacList.size()==0)
                return false;
            String[] objects = connectDeviceMacList.toArray(new String[connectDeviceMacList.size()]);
            if(gattArrayList!=null){
                multiConnectManager.closeAll();
                multiConnectManager.addDeviceToQueue(objects);
            }
            //还有读写descriptor
            //start descriptor(注意，在使用时当回调onServicesDiscovered成功时会自动调用该方法，所以只需要在连接之前完成1,3步即可)
            if(gattArrayList!=null&&gattArrayList.size()>0){
                for (int i = 0; i < gattArrayList.size(); i++) {
                    multiConnectManager.startSubscribe(gattArrayList.get(i));
                }
                multiConnectManager.startConnect();
            }
        }catch (Exception e){
            MyException myException=new MyException();
            myException.buildExceptionToLogFile(" 调用connectMac "+e.getMessage());
        }finally {
            return true;
        }
    }

    /**
     * 重连需要连接的传感器
     *
     * @param
     */
    public boolean reConnectBluetooth() {
        try{
            if(bluetoothLineStatus)
                return true;
            if(gattArrayList==null||gattArrayList.size()==0)
                return false;
            if(gattArrayList!=null){
                int length=gattArrayList.size();
                for(int i=0;i<length;i++){
                    BluetoothGatt bluetoothGatt=gattArrayList.get(i);
                    BluetoothDevice bluetoothDevice=bluetoothGatt.getDevice();
                    if(bluetoothDevice.getBondState()==BluetoothDevice.BOND_NONE){
                        multiConnectManager.startConnect(bluetoothDevice.getAddress());
                    }
                }
            }
        }catch (Exception e){
            MyException myException=new MyException();
            myException.buildExceptionToLogFile(" 调用connectMac "+e.getMessage());
        }finally {
            return true;
        }
    }

    /**
     * 通过Mac地址链接设备
     * @param context
     */
    public void connectMac(Context context){
        try{
            closeScanManager();
            if(bluetoothAdapter==null||bluetoothLineStatus)
                return;
            myLog.writeToFile(TAG+"---需要连接的mac" + connectDeviceMacList.toString());
            //获取设备gatt对象
            if(connectDeviceMacList!=null&&connectDeviceMacList.size()>0){
                for (int i = 0; i < connectDeviceMacList.size(); i++) {
                    String deviceStr=connectDeviceMacList.get(i);
                    BluetoothDevice device= bluetoothAdapter.getRemoteDevice(connectDeviceMacList.get(i));
                    BluetoothGatt gatt =device.connectGatt(context, false, bluetoothGattCallback);
                    if(device==null){
                        gattArrayList.add(gatt);
                    }else{
                        if(device.getBondState()==BluetoothDevice.BOND_NONE){
                            if(!gattArrayList.contains(deviceStr)){
                                gattArrayList.add(gatt);
                            }
                        }
                    }
                    myLog.writeToFile("添加了connentMac " + connectDeviceMacList.get(i));
                }
            }
            isUseBluetooth=true;
        }catch (Exception e){
            MyException myException=new MyException();
            myException.buildExceptionToLogFile(" 调用connectMac "+e.getMessage());
        }
    }

    /**
     * 处理回调的数据
     *
     * @param gatt
     * @param characteristic
     */

    float[][] floats = new float[7][30];

       private void dealCallDatas(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        int position = connectDeviceMacList.indexOf(gatt.getDevice().getAddress());
        //第一个传感器数据
        byte[] value = characteristic.getValue();
        if (value[0] != 0x55) {
            return; //开头不是0x55的数据删除
        }
        switch (value[1]) {
            case 0x61:
                if(DataUtil.getInstance().isStart&&bIdle) {
                    //加速度数据
                    floats[position][3] = ((((short) value[3]) << 8) | ((short) value[2] & 0xff)) / 32768.0f * 16;   //x轴
                    floats[position][4] = ((((short) value[5]) << 8) | ((short) value[4] & 0xff)) / 32768.0f * 16;   //y轴
                    floats[position][5] = ((((short) value[7]) << 8) | ((short) value[6] & 0xff)) / 32768.0f * 16;   //z轴
                    //角速度数据
                    floats[position][6] = ((((short) value[9]) << 8) | ((short) value[8] & 0xff)) / 32768.0f * 2000;  //x轴
                    floats[position][7] = ((((short) value[11]) << 8) | ((short) value[10] & 0xff)) / 32768.0f * 2000;  //y轴
                    floats[position][8] = ((((short) value[13]) << 8) | ((short) value[12] & 0xff)) / 32768.0f * 2000;  //z轴
                    //角度
                    floats[position][9] = ((((short) value[15]) << 8) | ((short) value[14] & 0xff)) / 32768.0f * 180;   //x轴
                    floats[position][10] = ((((short) value[17]) << 8) | ((short) value[16] & 0xff)) / 32768.0f * 180;   //y轴
                    floats[position][11] = ((((short) value[19]) << 8) | ((short) value[18] & 0xff)) / 32768.0f * 180;   //z轴
                    if (position == 0) {
                        long newTime=new Date().getTime();
                        if(startTime==0)
                            startTime=newTime;
                        if(isFirstLine){
                            fileDataByteModel=new FileDataByteModel(newTime,floats[position][3], floats[position][4], floats[position][5]);
                            FileUtil fileUtil=FileUtil.getInstance();
                            String path=fileUtil.makeDir(fileUtil.getFilePath(FileEnum.INDEX_FILE_PATH))+fileUtil.indexName;
                            fileUtil.WriteIndexMessageToFile(newTime,path,DataUtil.getInstance().runFilePath);
                            isFirstLine=false;
                        }else{
                            fileDataByteModel=new FileDataByteModel(newTime,floats[position][3], floats[position][4], floats[position][5]);
                        }
                        myQueue.enQueue(fileDataByteModel);
                        //todo 同时给计算队列添加数据
                        calculateDataModel=new CalculateDataModel();
                        calculateDataModel.setTime(newTime);
                        calculateDataModel.setY(floats[position][4]*9.8f);//todo 直接乘9.8f用于计算
                        calculateUtil.addNewData(calculateDataModel);
                        dataUtil.setFileDataByteModels(fileDataByteModel);
                        dataUtil.number++;
                        endTime=newTime;
                    }
                }
                break;
            case 0x62:
                //电池电压
                 /*   floats[position][21] = (float) 1.2 * 4 * (((value[11] << 8) | value[10]) + 1) / 1024;
                    //充电状态
                    floats[position][22] = value[12];
                    //低电压报警
                    floats[position][23] = value[14];*/
                break;
            case 0x71:
                float aa=((((short) value[3]) << 8) | ((short) value[2] & 0xff));
                Log.d(TAG, "dealCallDatas: "+aa);
                break;
        }


    }

    private void dealCallDatasNew(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        int iStart = 0;
        float fData[] = new float[9];
        float h[] = new float[3];
        float T,Version,eq;
        int position = connectDeviceMacList.indexOf(gatt.getDevice().getAddress());
        byte[] value = characteristic.getValue(); //第一个传感器数据
        while(iStart+20<=value.length) {
           /* for (int i = 0; i < 8; i++)
                fData[i] = (((short) value[iStart+i * 2 + 5]) << 8) | ((short) value[iStart+i * 2 + 4] & 0xff);
         */   switch (value[iStart+1]){
                case 0x61:
                    if(DataUtil.getInstance().isStart&&bIdle) {
                        //加速度数据
                        floats[position][3] = ((((short) value[3]) << 8) | ((short) value[2] & 0xff)) / 32768.0f * 16;   //x轴
                        floats[position][4] = ((((short) value[5]) << 8) | ((short) value[4] & 0xff)) / 32768.0f * 16;   //y轴
                        floats[position][5] = ((((short) value[7]) << 8) | ((short) value[6] & 0xff)) / 32768.0f * 16;   //z轴
                        //角速度数据
                        floats[position][6] = ((((short) value[9]) << 8) | ((short) value[8] & 0xff)) / 32768.0f * 2000;  //x轴
                        floats[position][7] = ((((short) value[11]) << 8) | ((short) value[10] & 0xff)) / 32768.0f * 2000;  //x轴
                        floats[position][8] = ((((short) value[13]) << 8) | ((short) value[12] & 0xff)) / 32768.0f * 2000;  //x轴
                        //角度
                        floats[position][9] = ((((short) value[15]) << 8) | ((short) value[14] & 0xff)) / 32768.0f * 180;   //x轴
                        floats[position][10] = ((((short) value[17]) << 8) | ((short) value[16] & 0xff)) / 32768.0f * 180;   //y轴
                        floats[position][11] = ((((short) value[19]) << 8) | ((short) value[18] & 0xff)) / 32768.0f * 180;   //z轴

                        if (position == 0) {
                            long newTime=new Date().getTime();
                            if(startTime==0)
                                startTime=newTime;
                            if(isFirstLine){
                                fileDataByteModel=new FileDataByteModel(newTime,floats[position][3], floats[position][4], floats[position][5]);
                                FileUtil fileUtil=FileUtil.getInstance();
                                String path=fileUtil.makeDir(fileUtil.getFilePath(FileEnum.INDEX_FILE_PATH))+fileUtil.indexName;
                                fileUtil.WriteIndexMessageToFile(newTime,path,DataUtil.getInstance().runFilePath);
                                isFirstLine=false;
                            }else{
                                fileDataByteModel=new FileDataByteModel(newTime,floats[position][3], floats[position][4], floats[position][5]);
                            }
                            myQueue.enQueue(fileDataByteModel);
                            //todo 同时给计算队列添加数据
                            calculateDataModel=new CalculateDataModel();
                            calculateDataModel.setTime(newTime);
                            calculateDataModel.setY(floats[position][4]*9.8f);//todo 直接乘9.8f用于计算
                            calculateUtil.addNewData(calculateDataModel);
                            dataUtil.setFileDataByteModels(fileDataByteModel);
                            dataUtil.number++;
                            endTime=newTime;
                        }
                    }
                    break;
                case 0x71:
                    switch (value[iStart + 2]) {
                        case 0x3A:
                            for (int i = 0; i < 3; i++) h[i] = fData[i];
                            break;
                        case 0x40:
                            T = (float) (fData[0] / 100.0);
                            break;
                        case 0x2e:
                            Version = fData[0];
                            break;
                        case 0x64://电量
                            eq = fData[0];
                            break;
                    }
                    return;
            }
            iStart+=20;
            /*if (position == 0) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss.SSS");
                Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                String str = formatter.format(curDate);
                for (int i = 0; i < connectDeviceMacList.size(); i++) {
                    str += "\t" + i + "\t" + connectDeviceMacList.get(i) + "\t" + String.format("%.3f", floats[i][3]) + "\t" + String.format("%.3f", floats[i][4]) + "\t" + String.format("%.3f", floats[i][5])
                            + "\t" + String.format("%.3f", floats[i][6]) + "\t" + String.format("%.3f", floats[i][6]) + "\t" + String.format("%.3f", floats[i][8])
                            + "\t" + String.format("%.3f", floats[i][9]) + "\t" + String.format("%.3f", floats[i][10]) + "\t" + String.format("%.3f", floats[i][11]);

                }
                str += "\n";
                if (!isRxd) {
                    try {
                        myFile.Write(str);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }*/
        }
        //EventBus.getDefault().post(new RefreshDatas()); // 发送消息，更新UI 显示数据
    }


    private Timer timer;
    /**
     * 给设备发送信息
     * @param value
     * @return
     */
   /* public boolean writeBytes(byte[] value)
    {
        //Log.d(TAG, "writeBytes: "+value[0]+" "+value[1]+" "+value[2]+" "+value[3]+" "+value[4]+" ");
        if (bIdle == false){
            //myLog.writeToFile("修改设置频率失败");
            //Log.d(TAG, "writeBytes: 修改设置频率失败");
            return false;
        }
        if(timer==null){
            timer=new Timer();
        }
        bIdle = false;
        long iDelay = 1000;
        if ((value[2]== (byte)0x01)&&(value[3]==(byte)0x01)) iDelay = 6000;
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                bIdle=true;
            }
        };
        timer.schedule(timerTask,iDelay);
        multiConnectManager.closeAll();
        multiConnectManager.cleanSubscribeData();
        String[] objects = connectDeviceMacList.toArray(new String[connectDeviceMacList.size()]);
        if(objects!=null&&objects.length>0){
            for(int i=0;i<objects.length;i++){
                multiConnectManager.removeDeviceFromQueue(objects[i]);
            }
        }
        multiConnectManager.addDeviceToQueue(objects);
        multiConnectManager.setBluetoothGattCallback(bluetoothGattCallback);
        multiConnectManager.setServiceUUID(bluetoothStr);
        multiConnectManager.addBluetoothSubscribeData(
                new BluetoothSubScribeData.Builder().setCharacteristicWrite(
                        UUID.fromString(getBluetoothWriteStr), value).build());
        for (int i = 0; i < gattArrayList.size(); i++) {
            multiConnectManager.startSubscribe(gattArrayList.get(i));
        }
        multiConnectManager.startConnect();
        return true;
    }*/

   private BluetoothSubScribeData writeBluetoothSubScribeData;
    public boolean writeBytes(byte[] value)
    {
        //Log.d(TAG, "writeBytes: "+value[0]+" "+value[1]+" "+value[2]+" "+value[3]+" "+value[4]+" ");
        if (bIdle == false){
            //myLog.writeToFile("修改设置频率失败");
            //Log.d(TAG, "writeBytes: 修改设置频率失败");
            return false;
        }
        if(timer==null){
            timer=new Timer();
        }
        bIdle = false;
        long iDelay = 1000;
        if ((value[2]== (byte)0x01)&&(value[3]==(byte)0x01)) iDelay = 6000;
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                bIdle=true;
            }
        };
        timer.schedule(timerTask,iDelay);
        multiConnectManager.closeAll();
        multiConnectManager.cleanSubscribeData();
        String[] objects = connectDeviceMacList.toArray(new String[connectDeviceMacList.size()]);
        if(objects!=null&&objects.length>0){
            for(int i=0;i<objects.length;i++){
                multiConnectManager.removeDeviceFromQueue(objects[i]);
            }
        }
        /**
         * 数据回调
         */
        multiConnectManager.setBluetoothGattCallback(bluetoothGattCallback);
        writeBluetoothSubScribeData= new BluetoothSubScribeData.Builder().setCharacteristicWrite(
                UUID.fromString(getBluetoothWriteStr), value).build();
        multiConnectManager.addBluetoothSubscribeData(writeBluetoothSubScribeData);
        multiConnectManager.addDeviceToQueue(objects);
        for (int i = 0; i < gattArrayList.size(); i++) {
            multiConnectManager.startSubscribe(gattArrayList.get(i));
        }
        multiConnectManager.startConnect();
        return true;
    }

    public boolean reReadSubscribeData(){
        multiConnectManager.closeAll();
        multiConnectManager.cleanSubscribeData();
        String[] objects = connectDeviceMacList.toArray(new String[connectDeviceMacList.size()]);
        if(objects!=null&&objects.length>0){
            for(int i=0;i<objects.length;i++){
                multiConnectManager.removeDeviceFromQueue(objects[i]);
            }
        }
        /**
         * 数据回调
         */
        setBluetoothGattCallback();
        multiConnectManager.setBluetoothGattCallback(bluetoothGattCallback);
        multiConnectManager.addBluetoothSubscribeData(readBluetoothSubScribeData);
        multiConnectManager.addDeviceToQueue(objects);
        for (int i = 0; i < gattArrayList.size(); i++) {
            multiConnectManager.startSubscribe(gattArrayList.get(i));
        }
        multiConnectManager.startConnect();
        return true;
    }
    /**
     * 关闭蓝牙客户端
     * @return
     */
    public void closeBluetooth(){
        myLog.writeToFile("进入closeBluetooth() ");
        if(gattArrayList!=null&&gattArrayList.size()>0){
            for (int i = 0; i < gattArrayList.size(); i++) {
                BluetoothGatt gatt=gattArrayList.get(i);
                BluetoothDevice bluetoothDevice=gatt.getDevice();
                gatt.disconnect();
                multiConnectManager.close(bluetoothDevice.getAddress());
            }
        }
        if(multiConnectManager!=null&&multiConnectManager.getConnectedDevice().size()>0){
            multiConnectManager.cleanSubscribeData();
        }
        deviceList.clear();
        gattArrayList.clear();
    }

    public void initBle(final Context context) {
        if(scanManager!=null&&scanManager.isScanning())
            return;
        scanManager = BleManager.getScanManager(context);
        scanManager.setScanOverListener(new ScanOverListener() {
            @Override
            public void onScanOver() {
            }
        });
        scanManager.setScanCallbackCompat(new ScanCallbackCompat() {
            @Override
            public void onBatchScanResults(List<ScanResultCompat> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(final int errorCode) {
                super.onScanFailed(errorCode);
                if (errorCode == SCAN_FAILED_LOCATION_CLOSE){
                    Toast.makeText(context, "Location is closed, you should open first", Toast.LENGTH_LONG).show();
                }else if(errorCode == SCAN_FAILED_LOCATION_PERMISSION_FORBID){
                    Toast.makeText(context, "You have not permission of location", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(context, "Other exception", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onScanResult(int callbackType, ScanResultCompat result) {
                super.onScanResult(callbackType, result);
                String strName = result.getLeDevice().getName();
                if (strName==null) return;
                if (strName.contains("WT")){
                    myLog.writeToFile(TAG+"扫描获取的设备名称onScanResult: name"+strName);
                    String strMAC =  result.getLeDevice().getAddress();
                    int iRssi = result.getLeDevice().getRssi();
                    String strdevice = strName+strMAC+iRssi;
                    int iIndex = deviceList.indexOf(strdevice);
                    if(iIndex==-1){
                        deviceList.add(strdevice);
                    }else{
                        deviceList.set(iIndex,strdevice);
                    }
                    int iIndexMac = connectDeviceMacList.indexOf(strMAC);
                    if(iIndex==-1){
                        connectDeviceMacList.add(strMAC);
                    }else{
                        connectDeviceMacList.set(iIndexMac,strMAC);
                    }
                }
            }
        });
    }

    public void closeScanManager(){
        if (scanManager!=null&&scanManager.isScanning()) {
            scanManager.stopCycleScan();
        }
    }

}
