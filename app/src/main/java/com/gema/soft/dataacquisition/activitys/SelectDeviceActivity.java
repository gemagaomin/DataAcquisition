package com.gema.soft.dataacquisition.activitys;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.blakequ.bluetooth_manager_lib.BleManager;
import com.blakequ.bluetooth_manager_lib.scan.BluetoothScanManager;
import com.blakequ.bluetooth_manager_lib.scan.ScanOverListener;
import com.blakequ.bluetooth_manager_lib.scan.bluetoothcompat.ScanCallbackCompat;
import com.blakequ.bluetooth_manager_lib.scan.bluetoothcompat.ScanResultCompat;
import com.gema.soft.dataacquisition.adapter.ScanDeviceAdapter;


import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gema.soft.dataacquisition.R;
import com.gema.soft.dataacquisition.utils.BluetoothUtil;
import com.gema.soft.dataacquisition.utils.MyApplication;
import com.gema.soft.dataacquisition.utils.MyLog;

import java.util.ArrayList;
import java.util.List;


public class SelectDeviceActivity extends BaseActivity implements View.OnClickListener{

    private Button btnOk;   //选择好了需要连接的mac设备
    private String TAG="SelectDeviceActivity";


    /* 列表相关 */
    private RecyclerView recyclerView ; //列表
    private ScanDeviceAdapter adapter;
    private ArrayList<String> deviceList; // 数据源 ： 所有扫描到的设备mac地址
    private ArrayList<String> deviceMacs;
    private ArrayList<String> selectDeviceMacs; // 选择的需要连接的设备的mac集合
    private int GPS_REQUEST_CODE = 10;
    private BluetoothUtil bluetoothUtil;
    private MyLog myLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);*/
        setContentView(R.layout.activity_select_device);
        MyApplication.addActivity(this);
        Toolbar toolbar=findViewById(R.id.select_device_toolbar);
        toolbar.setTitle("设备列表");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo 测试后期做什么
                finish();
            }
        });
        bluetoothUtil=BluetoothUtil.getInstance();
        myLog=MyLog.getInstance();
        //todo 20210415测试使用注销掉
        //bluetoothUtil.closeBluetooth();
        deviceList = new ArrayList<>();
        deviceList.addAll(bluetoothUtil.getDeviceList());
        selectDeviceMacs=new ArrayList<>();
        if(!TextUtils.isEmpty(bluetoothUtil.selectDeviceMacs)){
            selectDeviceMacs.add(bluetoothUtil.selectDeviceMacs);
        }
        initView();
        initEvent();
        initBle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
            }
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
            }
        }

    }

    private void initEvent() {
        btnOk.setOnClickListener(this);
    }

    private void initBle(){
        BluetoothScanManager scanManager=bluetoothUtil.scanManager ;
        if(scanManager==null||scanManager.isScanning())
            return;
        scanManager = BleManager.getScanManager(this);
        Log.i("lc","scanManager="+scanManager);
        scanManager.setScanOverListener(new ScanOverListener() {
            @Override
            public void onScanOver() {
            }
        });
        scanManager.setScanCallbackCompat(new ScanCallbackCompat() {
            @Override
            public void onBatchScanResults(List<ScanResultCompat> results) {
                super.onBatchScanResults(results);
                Log.i("lc","onBatchScanResults");
            }

            @Override
            public void onScanFailed(final int errorCode) {
                super.onScanFailed(errorCode);
                Log.i("lc","onScanFailed"+errorCode);
                if (errorCode == SCAN_FAILED_LOCATION_CLOSE){
                    Toast.makeText(getApplicationContext(), "Location is closed, you should open first", Toast.LENGTH_LONG).show();
                }else if(errorCode == SCAN_FAILED_LOCATION_PERMISSION_FORBID){
                    Toast.makeText(getApplicationContext(), "You have not permission of location", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Other exception", Toast.LENGTH_LONG).show();
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
                    String strDevice = strMAC;
                    if(deviceList.size()==0||!deviceList.contains(strDevice)){
                        deviceList.add(strDevice);
                        bluetoothUtil.setDeviceList(deviceList);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void initView() {
//        btnScan = (Button) findViewById(R.id.btnScan);
//        btnStopScan = (Button) findViewById(R.id.btnStopScan);
        btnOk = (Button) findViewById(R.id.btnOk);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
     /*   TextView tv_scan= (TextView) findViewById(R.id.tv_right);
        tv_scan.setText("扫描");
        tv_scan.setOnClickListener(this);*/
        // 列表相关初始化
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScanDeviceAdapter(deviceList,selectDeviceMacs,getBaseContext());
        adapter.setOnItemClickListener(
                new ScanDeviceAdapter.OnItemClickListener() {
                   @Override
                   public void onItemClick(View view, int position) {
                        BluetoothScanManager scanManager=BluetoothUtil.getInstance().scanManager;
                       if (scanManager.isScanning()) {
                           scanManager.stopCycleScan();
                       }
                       if(deviceList!=null&&deviceList.size()>position){
                           String mac=deviceList.get(position);
                           if (!selectDeviceMacs.contains(mac)){
                               //如果改item的mac不在已选中的mac集合中 说明没有选中，添加进已选中mac集合中，状态改为"已选择"
                               selectDeviceMacs.clear();
                               selectDeviceMacs.add(mac);
                               adapter.setSelectedList(selectDeviceMacs);
                               adapter.notifyDataSetChanged();
                           }
                       }
                   }

                   @Override
                   public void onItemLongClick(View view, int position) {

                   }
               }
        );

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onNoDoubleClick(View v) {
        switch (v.getId()){
            case R.id.tv_right:
                //开始 扫描
//                scanManager.startCycleScan(); //不会立即开始，可能会延时
                // scanManager.startScanNow(); //立即开始扫描
                openGPSSettings();
                break;
//
//            case R.id.btnStopScan:
//                // 如果正在扫描中 停止扫描
//                if (scanManager.isScanning()) {
//                    scanManager.stopCycleScan();
//                }
//                break;
            case R.id.btnOk:
                if(selectDeviceMacs==null||selectDeviceMacs.size()==0){
                    Toast.makeText(getBaseContext(),"请选择要连接的设备",Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent();
                bluetoothUtil.setDeviceList(deviceList);
                intent.putExtra("data",selectDeviceMacs);                // 设置结果，并进行传送
                myLog.writeToFile("需要连接的蓝牙设备个数为："+selectDeviceMacs.size()+"  第一个设备为"+(selectDeviceMacs.size()>0?selectDeviceMacs.get(0):"") );
                this.setResult(1, intent);
                if (bluetoothUtil.scanManager.isScanning()) {
                    bluetoothUtil.scanManager.stopCycleScan();
                }
                this.finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        openGPSSettings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 如果正在扫描中 停止扫描
        if (bluetoothUtil.scanManager.isScanning()) {
            bluetoothUtil.scanManager.stopCycleScan();
        }
        MyApplication.removeActivity(this);
    }

    private boolean checkGPSIsOpen() {
        boolean isOpen;
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        isOpen = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isOpen;
    }

    private void openGPSSettings() {
        if (checkGPSIsOpen()) {
            bluetoothUtil.scanManager.startScanNow(); //立即开始扫描
            myLog.writeToFile("openGPSSettings()立即开始扫描");
        } else {
            //没有打开则弹出对话框
            new AlertDialog.Builder(this)
                    .setTitle(R.string.notifyTitle)
                    .setMessage(R.string.gpsNotifyMsg)
                    // 拒绝, 退出应用
                    .setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })

                    .setPositiveButton(R.string.setting,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //跳转GPS设置界面
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivityForResult(intent, GPS_REQUEST_CODE);
                                }
                            })

                    .setCancelable(false)
                    .show();

        }
    }
}
