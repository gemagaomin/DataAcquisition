package com.gema.soft.dataacquisition.activitys;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blakequ.bluetooth_manager_lib.BleManager;
import com.blakequ.bluetooth_manager_lib.BleParamsOptions;
import com.blakequ.bluetooth_manager_lib.connect.ConnectConfig;
import com.gema.soft.dataacquisition.BuildConfig;
import com.gema.soft.dataacquisition.R;
import com.gema.soft.dataacquisition.adapter.TrainTypeAutoTextViewAdapter;
import com.gema.soft.dataacquisition.broadcasts.ServiceBroadcastReceiver;
import com.gema.soft.dataacquisition.broadcasts.USBBroadcastReceiver;
import com.gema.soft.dataacquisition.enums.FileEnum;
import com.gema.soft.dataacquisition.interfaces.httpInterFace;
import com.gema.soft.dataacquisition.models.CalculateDataModel;
import com.gema.soft.dataacquisition.models.FileModel;
import com.gema.soft.dataacquisition.models.RefreshDatas;
import com.gema.soft.dataacquisition.models.SmoothBumpModel;
import com.gema.soft.dataacquisition.models.TrainInfoModel;
import com.gema.soft.dataacquisition.models.TrainTypeModel;
import com.gema.soft.dataacquisition.models.WorkModel;
import com.gema.soft.dataacquisition.pojo.WorkPojo;
import com.gema.soft.dataacquisition.services.WriteService;
import com.gema.soft.dataacquisition.services.WriteTempService;
import com.gema.soft.dataacquisition.utils.AlgorithmUtil;
import com.gema.soft.dataacquisition.utils.BluetoothUtil;
import com.gema.soft.dataacquisition.utils.CalculateUtil;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.FileUtil;
import com.gema.soft.dataacquisition.utils.HttpUtil;
import com.gema.soft.dataacquisition.utils.LoadingDialog;
import com.gema.soft.dataacquisition.utils.MyApplication;
import com.gema.soft.dataacquisition.utils.MyLog;
import com.gema.soft.dataacquisition.utils.StringUtil;
import com.gema.soft.dataacquisition.utils.TcpServerConnect;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends BaseActivity {
    private String TAG="MainActivity";
    private String[] REQUESTED_PERMISSIONS;
    private DataUtil dataUtil;
    private httpInterFace httpUtil;
    private FileUtil fileUtil;
    private BluetoothUtil bluetoothUtil;
    private TrainInfoModel trainInfoModel;
    private WorkPojo workPojo;
    private final int GET_MESSAGE_FROM_SERVICE_FAIL=0;
    private final int NO_TRAIN_INFO_ERROR=3;
    private final int GOTO_SELECT_TRAIN_INFO=4;
    private final int NO_TRAIN_INFO_ERROR_SERVER=5;
    private final int NO_TRAIN_INFO_NOT_HAS_DATA=6;
    private final int NO_HAVE_IP_MENU=7;
    private List<TrainInfoModel> trainInfoModels;
    private String trainOrder;
    private long time = 0;
    private CalculateUtil calculateUtil;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int what=msg.what;
            switch (what){
                case GET_MESSAGE_FROM_SERVICE_FAIL:
                    Bundle bundle=msg.getData();
                    String errorMes=bundle.getString("error");
                    Toast.makeText(MainActivity.this,errorMes,Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    goToAAChartView();
                    break;
                case NO_TRAIN_INFO_ERROR:
                    Toast.makeText(MainActivity.this,"获取机车信息失败，请稍后再次尝试。",Toast.LENGTH_LONG).show();
                    break;
                case NO_TRAIN_INFO_ERROR_SERVER:
                    Toast.makeText(MainActivity.this,"因为网络原因，获取机车信息失败，请稍后再次尝试。",Toast.LENGTH_LONG).show();
                    break;
                case GOTO_SELECT_TRAIN_INFO:
                    Intent intent=new Intent(MainActivity.this,TrainInfoDialogActivity.class);
                    intent.putExtra("data",(Serializable)trainInfoModels);
                    startActivityForResult(intent,5);
                    break;
                case NO_TRAIN_INFO_NOT_HAS_DATA:
                    Toast.makeText(MainActivity.this,"没有符合要求的数据。",Toast.LENGTH_LONG).show();
                    break;
                case NO_HAVE_IP_MENU:
                    Toast.makeText(MainActivity.this,"离线版本无法使用当前功能。",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private TextView textView;
    private TextView versionView;
    private Button getDeviceBtn;
    private Button getTrainInfoBtn;
    private Button startWorkBtn;
    private Button settingHZBtn;

    private EditText trainOrderET;
    private AutoCompleteTextView trainTypeAutoCompleteTextView;
    private TrainTypeAutoTextViewAdapter trainTypeAutoTextViewAdapter;
    private List<TrainTypeModel> trainTypeModelList;
    private List<TrainTypeModel> trainTypeModelListTemp;
    private EditText trainNumberET;
    private EditText driverNameET;
    private EditText assistantDriverNameET;
    private TextView blueLineTV;
    private Intent intent;
    private TrainTypeModel selectedTrainTypeModel;
    private AdapterView.OnItemClickListener trainTypeOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectedTrainTypeModel=trainTypeModelList.get(position);
            EventBus.getDefault().post(new RefreshDatas("",3));
        }
    };
    private Timer bluetoothLineStatusTimer=new Timer();
    private TimerTask bluetoothLineTask;
    private final Timer timer = new Timer();
    private final int SelectDevice=0;
    private final int StartData=1;
    private final int GetTrainInfoData=3;
    private final int SettingHz=4;
    private final int CloseBluetooth=5;

    private Button testUSBBut;
    private TcpServerConnect tcpServerConnect;
    private Thread thread;

    USBBroadcastReceiver usbBroadcastReceiver;
    ServiceBroadcastReceiver serviceBroadcastReceiver;
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        getMenuInflater().inflate(R.menu.main_titile_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case R.id.main_title_menu_selectDevice:
                goSelectDevice();
                break;
            case R.id.main_title_menu_setting_ip:
                if(DataUtil.getInstance().AppHasNet==DataUtil.getInstance().APP_HAS_NET){
                    goSettingIP();
                }else{
                    item.setChecked(false);
                    handler.sendEmptyMessage(NO_HAVE_IP_MENU);
                }
                break;
            case R.id.main_title_menu_setting_hz:
                if(canClick(SettingHz))
                    goSettingHz();
                break;
            case R.id.main_title_menu_setting_close_bluetooth:
                if(canClick(CloseBluetooth))
                    openCloseBluetoothAlertDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DataUtil.FILE_APP_PATH=this.getFilesDir().getPath();
        Toolbar toolbar=findViewById(R.id.toolbar);
        toolbar.setTitle("基础信息");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        MyApplication.addActivity(this);
        dataUtil=DataUtil.getInstance();
        REQUESTED_PERMISSIONS=dataUtil.REQUESTED_PERMISSIONS;

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("android.hardware.usb.action.USB_STATE");
        usbBroadcastReceiver=new USBBroadcastReceiver();
        registerReceiver(usbBroadcastReceiver,intentFilter);
        IntentFilter serviceIntentFilter=new IntentFilter();
        serviceIntentFilter.addAction("com.gema.soft.dataacquisition.broadcasts.MY_BROADCAST");
        serviceBroadcastReceiver=new ServiceBroadcastReceiver();
        registerReceiver(serviceBroadcastReceiver,serviceIntentFilter);
        if(isIgnoringBatteryOptimizations()){
        }else{
            requestIgnoreBatteryOptimizations();
        }
        if(requestAllPower()){
            initDoNext();
        }
    }

    public void doNext(int requestCode, int[] grantResults) {
        if (requestCode == 107) {
            for(int i=0,num=grantResults.length;i<num;i++){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this,"未获得授权，本软件无法正常运行！",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                }
            }
            initDoNext();
        }
    }

    private void initDoNext(){
        initData();
        initView();
        initConfig();// 蓝牙初始设置
        initVersionFile();
        SharedPreferences sharedPreferences=getSharedPreferences(MyApplication.DATA_BLUE_TOOLTH_INFO, Activity.MODE_PRIVATE);
        String mac=sharedPreferences.getString("data","");
        SharedPreferences httpIP = getSharedPreferences(MyApplication.DATA_HTTP_IP, Activity.MODE_PRIVATE);
        String ip=httpIP.getString("filePath","");
        if(!TextUtils.isEmpty(ip)){
            dataUtil.FILE_PATH=ip;
        }
        if(!TextUtils.isEmpty(mac)){
            bluetoothUtil.isUseBluetooth=true;
            ArrayList<String> connectDeviceMacList=new ArrayList<>();
            ArrayList<String> connectDeviceList=new ArrayList<>();
            connectDeviceMacList.add(mac);
            bluetoothUtil.setConnectDeviceMacList(connectDeviceMacList);
            connectDeviceList.add(mac);
            bluetoothUtil.setDeviceList(connectDeviceList);
            bluetoothUtil.selectDeviceMacs=mac;
            bluetoothUtil.connectMac(getApplicationContext());
            initConn();
        }
        SharedPreferences sharedPreferencesWork=getSharedPreferences(MyApplication.DATA_HTTP_WORK_INFO,Activity.MODE_PRIVATE);
        String workDataStr=sharedPreferencesWork.getString("workData","");
        if(!TextUtils.isEmpty(workDataStr)){
            WorkModel workModel= JSONArray.parseObject(workDataStr,WorkModel.class);
            workPojo=new WorkPojo(workModel);
        }
        if(workPojo!=null){
            String workStatus=workPojo.getWorkStatus();
            if((dataUtil.WORK_STATUS_RUNNING.equals(workStatus)||dataUtil.WORK_STATUS_WAIT_FILE_UP.equals(workStatus)||dataUtil.WORK_STATUS_FILE_UP_ERROR.equals(workStatus))&&!TextUtils.isEmpty(workPojo.getBluetoothMac())){
                if(dataUtil.WORK_STATUS_RUNNING.equals(workStatus)){
                    dataUtil.isStart=true;
                    String algorithmFilePath=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.WORK_FILE_PATH)+File.separator+workPojo.getWorkId()+FileUtil.algorithmFileNameEnd);
                    if(StringUtil.isNotNull(algorithmFilePath)){
                        calculateUtil.StartCalculate(algorithmFilePath);
                    }
                }
                String fileName=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.WORK_FILE_PATH)+File.separator+workPojo.getWorkId()+);
                dataUtil.runFilePath=fileName;
                dataUtil.runWorkPojo=workPojo;
                if(bluetoothUtil.isUseBluetooth)
                    handler.sendEmptyMessage(2);
            }
        }
      /*  if(intentTemp==null){
            intentTemp=new Intent(this, WriteTempService.class);
            startService(intentTemp);
        }*/
        if(intent==null){
            intent=new Intent(getApplicationContext(), WriteService.class);
            startService(intent);
        }
        EventBus.getDefault().register(this);
        startTimer();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RefreshDatas event) {
        int type=event.getType();
        switch (type){
            case 0:
                textView.setText(event.getStr());
                break;
            case 1:
                if(!TextUtils.isEmpty(event.getStr()))
                    blueLineTV.setText(event.getStr());
                break;
            case 2:
                refreshViewText();
                break;
            case 3:
                String trainTypeName=selectedTrainTypeModel.getTrainTypeName();
                trainTypeAutoCompleteTextView.setText(trainTypeName);
                trainTypeAutoCompleteTextView.setSelection(trainTypeName.length());
                break;
        }

    }

    private void initData(){
    /*    dbUtil=DBUtil.getInstance();
        dbUtil.createTable();*/
        bluetoothUtil=BluetoothUtil.getInstance();
        fileUtil=FileUtil.getInstance();
        httpUtil=HttpUtil.getInstance();
        calculateUtil=CalculateUtil.getInstance();
        trainTypeModelList=new ArrayList<>();
        trainTypeModelList.addAll(dataUtil.getTrainTypeModels());
        trainTypeModelListTemp=new ArrayList<>();
        trainTypeModelListTemp.addAll(dataUtil.getTrainTypeModels());
        SharedPreferences workInfo= getSharedPreferences(MyApplication.DATA_HTTP_WORK_INFO, Activity.MODE_PRIVATE);
        String infoData=workInfo.getString("workData","");
        if(!TextUtils.isEmpty(infoData)){
            WorkModel workModel=JSONArray.parseObject(infoData, WorkModel.class);
            workPojo=new WorkPojo(workModel);
            dataUtil.runWorkPojo=workPojo;
            trainInfoModel=workPojo.getTrainInfo();
            if(trainInfoModel!=null){
                selectedTrainTypeModel=new TrainTypeModel(trainInfoModel.getTrainTypeId(),trainInfoModel.getTrainTypeName());
            }
        }
        MyApplication.getAppVersionCode(getApplicationContext());

    }

    private void initView(){
        getDeviceBtn=findViewById(R.id.main_get_device_btn);
        versionView=findViewById(R.id.main_version_code);
        versionView.setText(DataUtil.versionCode+"");
        getTrainInfoBtn=findViewById(R.id.main_train_info_btn);
        startWorkBtn=findViewById(R.id.main_start_get_data_btn);
        textView=findViewById(R.id.textView);
        trainOrderET=findViewById(R.id.main_trian_type_name_pt);
        blueLineTV=findViewById(R.id.main_line_status);
        if(DataUtil.isTest){
            blueLineTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //todo 测试使用2021929
                    // startBlued();
                    getTestFileData();
                }
            });
        }
        getTrainInfoBtn.setOnClickListener(this);
        getDeviceBtn.setOnClickListener(this);
        startWorkBtn.setOnClickListener(this);
        settingHZBtn=findViewById(R.id.main_setting_hz);
        settingHZBtn.setOnClickListener(this);
        trainTypeAutoCompleteTextView =findViewById(R.id.main_train_type_name_tv);
        trainTypeAutoTextViewAdapter = new TrainTypeAutoTextViewAdapter(trainTypeModelList, getApplicationContext());
        trainTypeAutoCompleteTextView.setAdapter(trainTypeAutoTextViewAdapter);
        trainTypeAutoCompleteTextView.setDropDownHeight(400);
        trainTypeAutoCompleteTextView.setOnItemClickListener(trainTypeOnItemClickListener);
        trainTypeAutoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoCompleteTextView view = (AutoCompleteTextView) v;
                view.showDropDown();
            }
        });
        trainTypeAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                AutoCompleteTextView view = (AutoCompleteTextView) v;
                if (hasFocus) {
                    view.showDropDown();
                } else {
                    validateTrainType();
                }
            }
        });

        driverNameET=findViewById(R.id.main_driver_name_et);
        trainNumberET=findViewById(R.id.main_train_number_ev);
        assistantDriverNameET=findViewById(R.id.main_assistant_driver_name_ev);
        //todo 测试使用
        testUSBBut=findViewById(R.id.test_usb_btn);
        testUSBBut.setOnClickListener(this);
        refreshViewText();
    }

    public boolean validateTrainType() {
        String trainTypeName = trainTypeAutoCompleteTextView.getText().toString();
        if(selectedTrainTypeModel==null){
            selectedTrainTypeModel=new TrainTypeModel();
        }
        if (TextUtils.isEmpty(trainTypeName)) {
            selectedTrainTypeModel=null;
        } else {
            boolean ret = false;
            String id = "";
            for (TrainTypeModel o : trainTypeModelListTemp) {
                if (trainTypeName.equals(o.getTrainTypeName())) {
                    id = o.getTrainTypeId();
                    ret = true;
                    break;
                }
            }
            if (ret) {
                selectedTrainTypeModel.setTrainTypeId(id);
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        trainTypeAutoCompleteTextView.setText(null);
                        trainTypeAutoCompleteTextView.setHint("选择正确的车型");
                        trainTypeAutoCompleteTextView.setHintTextColor(getResources().getColor(R.color.errorAccent));
                    }
                });
                return false;
            }
        }
        return true;
    }

    private void refreshViewText(){
        if(trainInfoModel!=null){
            selectedTrainTypeModel=new TrainTypeModel(trainInfoModel.getTrainTypeId(),trainInfoModel.getTrainTypeName());
            trainNumberET.setText(trainInfoModel.getTrainId());
            assistantDriverNameET.setText(trainInfoModel.getAssistantDriverId());
            trainTypeAutoCompleteTextView.setText(selectedTrainTypeModel.getTrainTypeName());
            driverNameET.setText(trainInfoModel.getDriverId());
            trainOrderET.setText(trainInfoModel.getTrainOrder());
        }else{
            trainNumberET.setText("");
            assistantDriverNameET.setText("");
            trainTypeAutoCompleteTextView.setText("");
            driverNameET.setText("");
            trainOrderET.setText("");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* if(intentTemp==null){
            intentTemp=new Intent(this,WriteTempService.class);
            startService(intentTemp);
        }
        if(intent==null){
            intent=new Intent(this, WriteService.class);
            startService(intent);
        }*/
        workPojo=dataUtil.runWorkPojo;
        if(workPojo!=null){
            trainInfoModel=workPojo.getTrainInfo();
            refreshViewText();
        }
        UsbManager mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        if(mUsbManager == null) {
            Log.d(TAG, "mUsbManager is null");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onNoDoubleClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.main_get_device_btn:
                if(canClick(SelectDevice)){
                    goSelectDevice();
                }
                break;
            case R.id.main_start_get_data_btn:
                //todo 测试使用
                if(DataUtil.isTest){
                    //todo 2021930 upTempFile();
                    getResult();
                }else{
                    if(canClick(StartData))
                        startGetData();
                }
                break;
            case R.id.main_train_info_btn:
                //todo 测试使用2021.9.27
              //  String fileDataPath=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.WORK_FILE_INFO_PATH)+"/"+"workinfodata.txt");

               /* String fileDataPath=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.WORK_FILE_INFO_PATH)+"/"+"workinfodata.txt");
                workPojo=new WorkPojo();
                workPojo.setBluetoothMac(dataUtil.blueMac);
                workPojo.setBlueToothConStatus(dataUtil.lineStatus);
                workPojo.setStartTime(new Date().getTime()+"");
                workPojo.setWorkId(UUID.randomUUID().toString());
                workPojo.setWorkStatus(dataUtil.WORK_STATUS_RUNNING);
                String fileName=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.WORK_FILE_PATH)+"/"+workPojo.getWorkId());
                if(fileName!=null){
                    dataUtil.runFilePath=fileName;
                    File file=new File(fileName);
                    if(!file.exists()){
                        return;
                    }
                    TrainInfoModel myTrainInfoModel=new TrainInfoModel();
                    myTrainInfoModel.setTrainId("123");
                    myTrainInfoModel.setTrainTypeName("123");
                    myTrainInfoModel.setTrainOrder("123");
                    myTrainInfoModel.setAssistantDriverId("123");
                    myTrainInfoModel.setDriverId("123");
                    workPojo.setFileName(fileName);
                    workPojo.setTrainInfo(myTrainInfoModel);
                    fileUtil.WriteMessageToFile(myTrainInfoModel,fileName);
                }
                WorkModel workModel=new WorkModel(workPojo);
                boolean aa=fileUtil.WriteDataToFile(fileDataPath, JSON.toJSONString(workModel));
                Log.d(TAG, "onNoDoubleClick: "+aa);*/
                if(DataUtil.isTest){
                    //todo 测试 2021930 downFile();
                    goToCalculateView();
                }else{
                    if(canClick(GetTrainInfoData)){
                        trainOrder=trainOrderET.getText().toString();
                        getTrainInfoFromLMDService(driverNameET.getText().toString());
                    }
                }
                break;
            case  R.id.test_usb_btn:
                thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(tcpServerConnect ==null){
                            tcpServerConnect =new TcpServerConnect("test","testType");
                            tcpServerConnect.run();
                        }
                    }
                });
                thread.start();
                break;
            default:
                break;
        }
    }

    public void goSelectDevice(){
        if(!bluetoothUtil.isUseBluetooth){
            bluetoothUtil.isUseBluetooth=true;
            bluetoothUtil.setMultiConnectManager(getApplicationContext());
            bluetoothUtil.getBluetoothAdapter();
            bluetoothUtil.initBle(getApplicationContext());
        }
        Intent intent = new Intent(this, SelectDeviceActivity.class);
        startActivityForResult(intent, 1);
    }

    public void goSettingIP(){
        Intent intent = new Intent(this, SettingIPActivity.class);
        startActivity(intent);
    }

    public void goSettingHz(){
        Intent intent = new Intent(this, SettingHzActivity.class);
        startActivity(intent);
    }

    public void startGetData(){
        workPojo=new WorkPojo();
        workPojo.setBluetoothMac(dataUtil.blueMac);
        workPojo.setBlueToothConStatus(dataUtil.lineStatus);
        workPojo.setStartTime(new Date().getTime()+"");
        workPojo.setWorkId(UUID.randomUUID().toString());
        workPojo.setWorkStatus(dataUtil.WORK_STATUS_RUNNING);
        String fileName=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.WORK_FILE_PATH)+File.separator+workPojo.getWorkId());
        if(StringUtil.isNotNull(fileName)){
            dataUtil.runFilePath=fileName;
            TrainInfoModel myTrainInfoModel=new TrainInfoModel();
            if(trainInfoModel!=null){
                myTrainInfoModel=trainInfoModel;
            }
            workPojo.setFileName(fileName);
            workPojo.setTrainInfo(myTrainInfoModel);
            fileUtil.WriteMessageToFile(trainInfoModel,fileName);
        }
        String algorithmFilePath=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.WORK_FILE_PATH)+File.separator+workPojo.getWorkId()+FileUtil.algorithmFileNameEnd);
        if(StringUtil.isNotNull(algorithmFilePath)){
            calculateUtil.StartCalculate(algorithmFilePath);
        }
        dataUtil.runWorkPojo=workPojo;
        WorkModel workModel=new WorkModel(workPojo);
        SharedPreferences workInfo= getSharedPreferences(MyApplication.DATA_HTTP_WORK_INFO, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=workInfo.edit();
        editor.putString("workData",JSONArray.toJSON(workModel).toString());
        editor.commit();
        dataUtil.isStart=true;
        dataUtil.startTime=new Date().getTime();
        dataUtil.number=0;//todo 测试接收数据频率使用
        goToAAChartView();
    }

    public void goToAAChartView(){
        Intent intentAc=new Intent();
        intentAc.setClass(MainActivity.this,AAChartViewActivity.class);
        startActivityForResult(intentAc,2);
    }

    public void startTimer(){
        if(bluetoothLineTask==null){
            bluetoothLineTask=new TimerTask() {
                @Override
                public void run() {
                    String str=dataUtil.lineStatus;
                    String mac=BluetoothUtil.getInstance().selectDeviceMacs;
                    if(!TextUtils.isEmpty(mac)&&!mac.equals(dataUtil.blueMac)){
                        dataUtil.blueMac=mac;
                        SharedPreferences sharedPreferences=getSharedPreferences(MyApplication.DATA_BLUE_TOOLTH_INFO,Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString("data",mac);
                        editor.commit();
                    }
                    //todo 测试使用放出来
                    if(!DataUtil.isTest)
                        EventBus.getDefault().post(new RefreshDatas(str,1));
                }
            };
            if(bluetoothLineStatusTimer==null)
                bluetoothLineStatusTimer=new Timer();
            bluetoothLineStatusTimer.schedule(bluetoothLineTask,0,1000);
        }
        //todo 暂时关掉基础数据同步
       /* if(task==null){
            task=new TimerTask() {
                @Override
                public void run() {
                    getVersionData();
                }
            };
            int time=1000*60;
            timer.schedule(task,0,time);
        }*/
    }

    private void closeBluetoothTimer(){
        if(bluetoothLineStatusTimer!=null){
            bluetoothLineStatusTimer.cancel();
            if(bluetoothLineTask!=null){
                bluetoothLineTask.cancel();
            }
            bluetoothLineTask=null;
            bluetoothLineStatusTimer=null;
        }
    }

    private void closeBluetooth(){
        closeBluetoothTimer();
        bluetoothUtil.selectDeviceMacs="";
        bluetoothUtil.bluetoothLineStatus=false;
        DataUtil.getInstance().blueMac="";
        bluetoothUtil.isUseBluetooth=false;
        bluetoothUtil.closeBluetooth();
        SharedPreferences sharedPreferences=getSharedPreferences(MyApplication.DATA_BLUE_TOOLTH_INFO,Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("data","");
        editor.commit();
        EventBus.getDefault().post(new RefreshDatas(DataUtil.getInstance().lineStatus,1));
    }

    private void openCloseBluetoothAlertDialog(){
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this)
                .setMessage("是否关闭蓝牙链接？")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WriteService.isServiceRun=false;
                        WriteTempService.isServiceRun=false;
                        closeBluetooth();
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    public boolean requestAllPower(){
         if    ( checkSelfPermission( REQUESTED_PERMISSIONS[0]  )         !=PERMISSION_GRANTED
        ||checkSelfPermission(REQUESTED_PERMISSIONS[1]          )!=PERMISSION_GRANTED
        ||checkSelfPermission(REQUESTED_PERMISSIONS[2]           )!=PERMISSION_GRANTED
        ||checkSelfPermission(REQUESTED_PERMISSIONS[3]           )!=PERMISSION_GRANTED
        ||checkSelfPermission(REQUESTED_PERMISSIONS[4]           )!=PERMISSION_GRANTED
        ||checkSelfPermission(REQUESTED_PERMISSIONS[5]           )!=PERMISSION_GRANTED
        ||checkSelfPermission(REQUESTED_PERMISSIONS[6]           )!=PERMISSION_GRANTED
        ||checkSelfPermission(REQUESTED_PERMISSIONS[7]           )!=PERMISSION_GRANTED
        )
        {
            requestPermissions(REQUESTED_PERMISSIONS,107);
            return false;
        }else{
            return true;
        }
    }

    public void getTrainInfoFromLMDService(String driverId){
        if(TextUtils.isEmpty(trainOrder) || TextUtils.isEmpty(driverId) ){
            Toast.makeText(this, "车次、司机号不能为空！", Toast.LENGTH_LONG).show();
            return;
        }
        Map<String,String> map=new HashMap<>();
        map.put("trainorder",trainOrder);
        map.put("driverid", driverId);
        final LoadingDialog loadingDialog = LoadingDialog.getInstance(this, "加载机车相关信息...");
        loadingDialog.show(this);
        Map<String, Object> params = new HashMap<>();
        params.put("data", JSONObject.toJSONString(map));
       httpUtil.asynch("http://218.206.94.242:18188/LMD/trainmap/searchByTrainorder.do", httpUtil.TYPE_POST, params, new Callback() {
           @Override
           public void onFailure(@NotNull Call call, @NotNull IOException e) {
               loadingDialog.dismiss();
               Message message=new Message();
               message.what=GET_MESSAGE_FROM_SERVICE_FAIL;
               Bundle bundle=new Bundle();
               bundle.putString("error","获取机车信息失败。");
               message.setData(bundle);
               handler.sendMessage(message);
           }

           @Override
           public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
               if (response != null && response.code() != 500 && response.isSuccessful()) {
                   try {
                       String str = response.body().string();
                       String result = str;
                       if(TextUtils.isEmpty(str)){
                           handler.sendEmptyMessage(NO_TRAIN_INFO_NOT_HAS_DATA);
                           return;
                       }
                       JSONArray jsonArray = JSONArray.parseArray(result);
                       if (jsonArray!=null&&jsonArray.size()>0) {
                           trainInfoModels=new ArrayList<>();
                           for(int i=0;i<jsonArray.size();i++){
                               JSONObject object = jsonArray.getJSONObject(i);
                               TrainInfoModel trainInfoModel=new TrainInfoModel(object);
                               trainInfoModels.add(trainInfoModel);
                           }
                           handler.sendEmptyMessage(GOTO_SELECT_TRAIN_INFO);
                       }else{
                           handler.sendEmptyMessage(NO_TRAIN_INFO_ERROR);
                       }
                       //todo 后期处理

                   } catch (Exception e) {
                       loadingDialog.dismiss();
                       handler.sendEmptyMessage(NO_TRAIN_INFO_ERROR_SERVER);
                   } finally {
                       loadingDialog.dismiss();
                   }
               }
               loadingDialog.dismiss();
           }
       });
    }

    /*申请加入白名单*/
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent,108);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean canClick(int type){
        if(type==SelectDevice){
            if(TextUtils.isEmpty(bluetoothUtil.selectDeviceMacs)||workPojo==null){
                return true;
            }else{
                if(dataUtil.WORK_STATUS_RUNNING.equals(workPojo.getWorkStatus())){
                    Toast.makeText(this,"设备自连接，无需手动操作。",Toast.LENGTH_LONG).show();
                    return false;
                }
                Toast.makeText(this,"已有链接设备无法再次选择。",Toast.LENGTH_LONG).show();
            }
        }else if(type==StartData){
            //todo 后期正式使用的时候需要放出来
            if(!BluetoothUtil.getInstance().bluetoothLineStatus){
                String textStr="没有链接设备，无法开始工作。";
                Toast.makeText(this,textStr,Toast.LENGTH_LONG).show();
                return false;
               /* if(TextUtils.isEmpty(bluetoothUtil.selectDeviceMacs)){
                    textStr="设备正在自动连接中，无法进行当前操作、请稍等。";
                }*/
            }
            if(!isCanStartWork()){
                Toast.makeText(this,"信息填充完整，才可以进行此操作。",Toast.LENGTH_LONG).show();
                return false;
            }
            if(dataUtil.runFilePath!=null||dataUtil.runWorkPojo!=null){
                goToAAChartView();
            }
            if(workPojo==null){
                return true;
            }
        }else if(type==GetTrainInfoData){
            if(TextUtils.isEmpty(trainOrderET.getText().toString())||TextUtils.isEmpty(driverNameET.getText().toString())){
                Toast.makeText(this,"将车次和司机信息补充完整，才可以进行此操作。",Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }else if(type==SettingHz){
            if(TextUtils.isEmpty(bluetoothUtil.selectDeviceMacs)){
                Toast.makeText(this,"请先连接设备，才可以进行此操作。",Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }else if(type==CloseBluetooth){
            if(TextUtils.isEmpty(bluetoothUtil.selectDeviceMacs)){
                Toast.makeText(this,"没有连接过设备，无法进行此操作。",Toast.LENGTH_LONG).show();
            }else if(workPojo!=null){
                Toast.makeText(this,"有正在进行的记录，无法进行此操作。",Toast.LENGTH_LONG).show();
            }else{
                return true;
            }
        }
        return false;
    }

    private boolean isCanStartWork(){
        if(selectedTrainTypeModel==null){
            return false;
        }
        if(trainInfoModel==null)
            trainInfoModel=new TrainInfoModel();
        trainInfoModel.setTrainOrder(trainOrderET.getText().toString());
        trainInfoModel.setTrainTypeId(selectedTrainTypeModel.getTrainTypeId());
        trainInfoModel.setTrainTypeName(selectedTrainTypeModel.getTrainTypeName());
        trainInfoModel.setDriverId(driverNameET.getText().toString());
        trainInfoModel.setTrainId(trainNumberET.getText().toString());
        trainInfoModel.setAssistantDriverId(assistantDriverNameET.getText().toString());
        if(trainInfoModel!=null&&!TextUtils.isEmpty(trainInfoModel.getDriverId())
                &&!TextUtils.isEmpty(trainInfoModel.getTrainId())
                &&!TextUtils.isEmpty(trainInfoModel.getTrainTypeId())
                &&!TextUtils.isEmpty(trainInfoModel.getAssistantDriverId())
                &&!TextUtils.isEmpty(trainInfoModel.getTrainOrder())){

            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            return exitBy2Click();      //调用双击退出函数
        }
        return super.dispatchKeyEvent(event);
    }

    public boolean exitBy2Click() {
        if (System.currentTimeMillis() - time > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            time = System.currentTimeMillis();
        } else {
            MyApplication.finishAll();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        doNext(requestCode,grantResults);
    }

    /**
     * 将软件版本写入文件里，为了客户端读取版本信息更新使用
     */
    public void initVersionFile(){
        FileUtil fileUtil=FileUtil.getInstance();
        String apkFilePath=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.APK_VERSION_FILE_PATH)+"/apkVersion.txt");
        String versionCode=MyApplication.getAppVersionCode(getApplicationContext())+"";
        List<String> list=fileUtil.readStringListFormFile(apkFilePath);
        if(list!=null&&list.size()>0){
            String vc=list.get(0).split(":")[1];
            if(vc.equals(versionCode))
                return;
            list.set(0,"versionCode:"+versionCode);

        }else{
            list=new ArrayList<>();
            list.add("versionCode:"+versionCode);
        }
        fileUtil.writeDataToFile(list,apkFilePath);

    }
    /**
     * 对蓝牙的初始化操作
     */
    public void initConn(){
        if(bluetoothUtil.bIdle){
            bluetoothUtil.connectBluetooth();
        }
    }

    private void initConfig() {
        BluetoothUtil bluetoothUtil=BluetoothUtil.getInstance();
        bluetoothUtil.setMultiConnectManager(getApplicationContext());
        // 获取蓝牙适配器
        try {
            // 获取蓝牙适配器
            BluetoothAdapter bluetoothAdapter =bluetoothUtil.getBluetoothAdapter();
            //
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "蓝牙不可用", Toast.LENGTH_LONG).show();
                return;
            }
            // 蓝牙没打开的时候打开蓝牙
            if (!bluetoothAdapter.isEnabled())
                bluetoothAdapter.enable();
        } catch (Exception err) {
        }
        BleManager.setBleParamsOptions(new BleParamsOptions.Builder()
                .setBackgroundBetweenScanPeriod(5 * 60 * 1000)
                .setBackgroundScanPeriod(10000)
                .setForegroundBetweenScanPeriod(2000)
                .setForegroundScanPeriod(10000)
                .setDebugMode(BuildConfig.DEBUG)
                .setMaxConnectDeviceNum(7)            //最大可以连接的蓝牙设备个数
                .setReconnectBaseSpaceTime(1000)
                .setReconnectMaxTimes(Integer.MAX_VALUE)
                .setReconnectStrategy(ConnectConfig.RECONNECT_LINE_EXPONENT)
                .setReconnectedLineToExponentTimes(5)
                .setConnectTimeOutTimes(20000)
                .build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if(data!=null){
                    ArrayList<String> str=data.getStringArrayListExtra("data");
                    if(str!=null&&str.size()>0){
                        SharedPreferences sharedPreferences=getSharedPreferences(MyApplication.DATA_BLUE_TOOLTH_INFO, Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString("data",str.get(0));
                        editor.commit();
                        bluetoothUtil=BluetoothUtil.getInstance();
                        String aa=str.get(0);
                        if(!bluetoothUtil.selectDeviceMacs.equals(aa)&&bluetoothUtil.isUseBluetooth){
                            bluetoothUtil.setConnectDeviceMacList(str);
                            bluetoothUtil.connectMac(getApplicationContext());
                            bluetoothUtil.selectDeviceMacs=aa;
                            initConn();
                            if(!WriteService.isServiceRun||intent==null){
                                if(intent==null)
                                    intent=new Intent(getApplicationContext(),WriteService.class);
                                startService(intent);
                            }
                            startTimer();
                        }
                    }
                }
                break;
            case 2:
                workPojo=dataUtil.runWorkPojo;
                if(workPojo==null){
                    trainInfoModel=null;
                }
                EventBus.getDefault().post(new RefreshDatas("",2));
                break;
            case 5:
                if(resultCode==4){
                    trainInfoModel=(TrainInfoModel) data.getSerializableExtra("data");
                    EventBus.getDefault().post(new RefreshDatas("",2));
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(intent!=null){
            stopService(intent);
            WriteService.isServiceRun=false;
            bluetoothUtil.closeBluetooth();
        }
        EventBus.getDefault().unregister(this);
        if(bluetoothLineStatusTimer!=null){
            bluetoothLineStatusTimer.cancel();
        }
        if(timer!=null){
            timer.cancel();
        }
        if(tcpServerConnect !=null){
            tcpServerConnect.closeSocket();
        }
        MyApplication.removeActivity(this);
        if(usbBroadcastReceiver!=null)
            unregisterReceiver(usbBroadcastReceiver);
        if(serviceBroadcastReceiver!=null)
            unregisterReceiver(serviceBroadcastReceiver);
    }

    @Override
    public void finish() {
        super.finish();
    }

    //todo 基础信息更新

    /* public void getVersionData() {
        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Map map = new HashMap();
                    UserModel userModel = dataUtil.getUser();
                    if (userModel != null) {
                        map.put("userId", userModel.getUserId());
                        Map parasMap = new HashMap();
                        parasMap.put("data", JSONObject.toJSONString(map));
                        Map<String, String> resultMap = httpUtil.synch("/app/getuserversion", httpUtil.TYPE_POST, parasMap);
                        String error = resultMap.get("error");
                        String request = resultMap.get("result");
                        if ("0".equals(error)) {
                            if (!TextUtils.isEmpty(request)) {
                                JSONObject jsonObject = JSONObject.parseObject(request);
                                //TODO 修改时间每个字典表不一致，后期需改
                                String errorCode = jsonObject.getString("errorCode");
                                if ("0".equals(errorCode)) {
                                    SharedPreferences versionPreferences = getSharedPreferences(MyApplication.DATA_VERSION, Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = versionPreferences.edit();
                                    String personVersion = jsonObject.getString("personVersion");
                                    String trainTypeVersion = jsonObject.getString("trainTypeVersion");
                                    Map<String, String> versionMap = dataUtil.getVersionMap();
                                    if (getVersionChange("personVersion", personVersion, versionMap)) {
                                        Map<String, String> personMap = httpUtil.synch("/app/getpersons", httpUtil.TYPE_POST, parasMap);
                                        String personError = personMap.get("error");
                                        String personRequest = personMap.get("result");
                                        Map<String, PersonModel> driverMap = new HashMap<String, PersonModel>();
                                        List<PersonModel> driverList = new ArrayList<PersonModel>();
                                        if ("0".equals(personError)) {
                                            if (!TextUtils.isEmpty(personRequest)) {
                                                JSONObject personJsonObject = JSONObject.parseObject(personRequest);
                                                String errorCodePerson = personJsonObject.getString("errorCode");
                                                if ("0".equals(errorCodePerson)) {
                                                    JSONArray personJsonArray = personJsonObject.getJSONArray("persons");
                                                    if (personJsonArray != null) {
                                                        int num = personJsonArray.size();
                                                        if (num > 0) {
                                                            String driver = DataUtil.TableNameEnum.PERSON.toString();
                                                            dbUtil.deleteAll(driver);
                                                            dbUtil.db.beginTransaction();
                                                            for (int i = 0; i < num; i++) {
                                                                PersonModel personModel = personJsonArray.getObject(i, PersonModel.class);
                                                                String Id = personModel.getPersonId();
                                                                driverMap.put(Id, personModel);
                                                                driverList.add(personModel);
                                                                dbUtil.db.insert(driver, null, personModel.getContentValues(personModel));
                                                            }
                                                            dbUtil.db.setTransactionSuccessful();
                                                            dbUtil.db.endTransaction();
                                                        }
                                                    }
                                                    dataUtil.setPersonModels(driverList);
                                                    dataUtil.setPersonModelMap(driverMap);
                                                }
                                            }
                                        }
                                    }
                                    if (getVersionChange("trainTypeVersion", trainTypeVersion, versionMap)) {
                                        Map<String, String> trainTypeResultMap = httpUtil.synch("/app/gettraintypes", httpUtil.TYPE_POST, parasMap);
                                        String trainTypeError = trainTypeResultMap.get("error");
                                        String trainTypeRequest = trainTypeResultMap.get("result");
                                        Map<String, TrainTypeModel> trainTypeMap = new HashMap<>();
                                        List<TrainTypeModel> trainTypeList = new ArrayList<>();
                                        if ("0".equals(trainTypeError)) {
                                            if (!TextUtils.isEmpty(trainTypeRequest)) {
                                                JSONObject trainTypeJsonObject = JSONObject.parseObject(trainTypeRequest);
                                                String errorCodeTrainType = trainTypeJsonObject.getString("errorCode");
                                                if ("0".equals(errorCodeTrainType)) {
                                                    JSONArray trainTypeJsonArray = trainTypeJsonObject.getJSONArray("traintypes");
                                                    if (trainTypeJsonArray != null) {
                                                        int num = trainTypeJsonArray.size();
                                                        if (num > 0) {
                                                            String trainType = DataUtil.TableNameEnum.TRAIN_TYPE.toString();
                                                            dbUtil.deleteAll(trainType);
                                                            dbUtil.db.beginTransaction();
                                                            for (int i = 0; i < num; i++) {
                                                                TrainTypeModel trainTypeModel = trainTypeJsonArray.getObject(i, TrainTypeModel.class);
                                                                String Id = trainTypeModel.getTrainTypeId();
                                                                trainTypeMap.put(Id, trainTypeModel);
                                                                trainTypeList.add(trainTypeModel);
                                                                dbUtil.db.insert(trainType, null, trainTypeModel.getContentValues(trainTypeModel));
                                                            }
                                                            dbUtil.db.setTransactionSuccessful();
                                                            dbUtil.db.endTransaction();
                                                        }
                                                    }
                                                    dataUtil.setTrainTypeModelMap(trainTypeMap);
                                                    dataUtil.setTrainTypeModels(trainTypeList);
                                                }
                                            }
                                        }
                                    }
                                    editor.putString("personVersion", personVersion);
                                    editor.putString("trainTypeVersion", trainTypeVersion);
                                    editor.commit();
                                    dataUtil.setVersionMap(versionPreferences);
                                }
                            }
                        }
                    }
                }
            };
            timer.schedule(task, 0, 300000);
        }
    }

    public boolean getVersionChange(String key, String str, Map<String, String> versionMap) {
        if (versionMap == null || versionMap.get(key) == null) {
            return false;
        }
        if (TextUtils.isEmpty(versionMap.get(key))) {
            return true;
        }
        boolean result = StringUtil.RESULT_TRUE.equals(StringUtil.CompareDateSize(str, versionMap.get(key)));
        return result;
    }

    */

    //todo 测试使用
    public void startBlued(){
           DataUtil.getInstance().number=0;
           workPojo=new WorkPojo();
           workPojo.setBluetoothMac(dataUtil.blueMac);
           workPojo.setBlueToothConStatus(dataUtil.lineStatus);
           workPojo.setStartTime(new Date().getTime()+"");
           workPojo.setWorkId(UUID.randomUUID().toString());
           workPojo.setWorkStatus(dataUtil.WORK_STATUS_RUNNING);
           String fileName=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.WORK_FILE_PATH)+"/"+workPojo.getWorkId());
           if(fileName!=null){
               dataUtil.runFilePath=fileName;
               File file=new File(fileName);
               if(!file.exists()){
                   return;
               }
               TrainInfoModel myTrainInfoModel=new TrainInfoModel();
               myTrainInfoModel.setTrainId("123");
               myTrainInfoModel.setTrainTypeName("123");
               myTrainInfoModel.setTrainOrder("123");
               myTrainInfoModel.setAssistantDriverId("123");
               myTrainInfoModel.setDriverId("123");
               workPojo.setFileName(fileName);
               workPojo.setTrainInfo(myTrainInfoModel);
               fileUtil.WriteMessageToFile(myTrainInfoModel,fileName);
           }
           dataUtil.runWorkPojo=workPojo;
           dataUtil.isStart=true;
        Timer timer=new Timer();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                dataUtil.isStart=false;
                int list=bluetoothUtil.myQueue.QueueLength();
                Log.d(TAG,"获取数据个数  "+DataUtil.getInstance().number);
                if(list>0){
                    Log.d(TAG,"数据个数  "+list);
                    FileModel fileModel=new FileModel();
                    fileModel.setFileName(workPojo.getWorkId());
                    fileModel.setFilePath(fileName);
                    String filePathIndex=FileUtil.getInstance().makeDirOrFile(FileUtil.getInstance().getFilePath(FileEnum.INDEX_FILE_PATH))+FileUtil.getInstance().indexName;
                    FileUtil.getInstance().WriteMessageToFile(workPojo.getTrainInfo(),fileName);
                    FileUtil.getInstance().WriteIndexMessageToFile(-1,filePathIndex,fileName);
                    if(FileUtil.getInstance().refFileNewTemp(fileName,fileName+"1")){
                        upDateFile(fileModel);
                    }
                    Log.d(TAG, "run: "+list);
                }
            }
        };
        timer.schedule(timerTask,1000);
    }

    public void upDateFile(FileModel fileModel){
        File file=new File(fileModel.getFilePath());
        if(!file.exists()){
            Log.d(TAG, "upDateFile: 错误");
            return;
        }
        String filePath=fileModel.getFilePath();
        String filePathTemp=filePath+"1";
        String filePathTempName=filePath+"1";
        fileModel.setFileName(filePathTempName);
        fileModel.setFilePath(filePathTemp);
        if(FileUtil.getInstance().refFileNewTemp(filePath,filePathTemp)){
            Callback callback=new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.d(TAG, "onFailure: error");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "onFailure: ok");
                    }
                }
            };
            HttpUtil.getInstance().asynchFile(fileModel,callback);
        }
    }
    //todo 测试方法
    public void downFile(){
           MyLog.getInstance().writeToFileTest();
       /* DownloadManager downloadManager=(DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if(fileUtil==null){
            fileUtil=FileUtil.getInstance();
        }
        String filePath="PWCZ";
        String fileName="dc6be751-cef1-497e-ada3-ebf6466a65411";
        String url="http://192.168.137.1:8033/downloadjzgk/dc6be751-cef1-497e-ada3-ebf6466a65411";//"http://192.168.137.1:8081/smooth/pwcz/downloadWorkTempFile"
        long id=HttpUtil.getInstance().downFileHttp(url,filePath,fileName,downloadManager);
        Log.d(TAG, "downFile: id"+id);*/
    }
    //todo 测试方法
    public void upTempFile(){
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date=new Date();
        String fileName="dc6be751-cef1-497e-ada3-ebf6466a65411";
        File file=null;
        boolean sdStatus=Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if(sdStatus){
            file= Environment.getExternalStorageDirectory();
        }
        String fileBluetoothPath=FileUtil.getSDPath();//Environment.getExternalStorageDirectory()+"/bluetooth";
        StringBuffer showStr=new StringBuffer();
        showStr.append("upTempFile: st"+date.getTime()+" "+dateFormat.format(date));
        Log.d(TAG, "upTempFile: st"+date.getTime()+" "+dateFormat.format(date));
        EventBus.getDefault().post(new RefreshDatas(showStr.toString(),1));
        String filePath= fileBluetoothPath+"/"+fileName;
        String filePathTemp=fileBluetoothPath+"/"+fileName+1;
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                boolean  isRefFile=FileUtil.getInstance().refFileNewTemp(filePath,filePathTemp);
                Date date=new Date();
                showStr.append("upTempFile:  isRefFile et"+date.getTime()+" "+dateFormat.format(date));
                Log.d(TAG, "upTempFile:  isRefFile et"+date.getTime()+" "+dateFormat.format(date));
                EventBus.getDefault().post(new RefreshDatas(showStr.toString(),1));
                if(isRefFile){
                    Callback callback=new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Log.d(TAG, "onFailure: ");
                            Date date=new Date();
                            showStr.append("upTempFile:  upfile et"+date.getTime()+" "+dateFormat.format(date));
                            Log.d(TAG, "upTempFile:  upfile et"+date.getTime()+" "+dateFormat.format(date));
                            EventBus.getDefault().post(new RefreshDatas(showStr.toString(),1));
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if(response.isSuccessful())
                            {
                                Log.d(TAG, "onResponse: success");
                            }
                            Date date=new Date();
                            showStr.append("upTempFile:  upfile et"+date.getTime()+" "+dateFormat.format(date));
                            Log.d(TAG, "upTempFile:  upfile et"+date.getTime()+" "+dateFormat.format(date));
                            EventBus.getDefault().post(new RefreshDatas(showStr.toString(),1));
                        }
                    };
                    FileModel fileModel=new FileModel();
                    fileModel.setFilePath(filePathTemp);
                    fileModel.setFileName(fileName+1);
                    httpUtil.asynchFile(fileModel,callback);
                }
            }
        });
        thread.start();
    }

    private String fileName="b74bf2ac-ec3a-47be-93b5-17bf415a2a5d1";//"a4acd735-a47c-4c9e-918a-8b0f4d802959";//"33d9e430-954d-4e94-9a03-61f96da9adfa";//"caf79f38-3e14-4136-ab4d-1a4f8504d2851";//"049f7756-6caa-43c7-8b71-27c97e933e0a1";//"3eaad59f-e989-42f5-8ed3-66579790e2c71";//;"cdad795f-8877-463a-a67f-abe1dd5b20081";//

    //todo 测试将后台算法提到前台
    public void getTestFileData(){
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Date date=new Date();
                Log.d(TAG,"getTestFileData startTime "+dateFormat.format(date));
                FileUtil fileUtil=FileUtil.getInstance();
                CalculateUtil calculateUtil=CalculateUtil.getInstance();
                String filePath=FileUtil.getSDPath()+File.separator+"workfile"+File.separator+fileName;//Environment.getExternalStorageDirectory()+"/bluetooth";
                String calculateFilePath=filePath+FileUtil.algorithmFileNameEnd;
                File fileDelete=new File(calculateFilePath);
                if(fileDelete.exists()){
                    fileDelete.delete();
                }
                String fileCalculateFilePath=fileUtil.makeDirOrFile(calculateFilePath);
                calculateUtil.StartCalculate(fileCalculateFilePath);
                File file=new File(filePath);
                DataInputStream inputStream=null;
                try{
                    if(file.exists()){
                        inputStream=new DataInputStream(new FileInputStream(file));
                        if(inputStream!=null){
                            int num=inputStream.readInt();
                            byte[] workInfo=new byte[num];
                            inputStream.read(workInfo);
                            CalculateDataModel calculateDataModel=readData(inputStream);
                            int i=1;
                            while (calculateDataModel!=null){
                                if(calculateDataModel.getY()!=999){
                                    calculateUtil.addNewData(calculateDataModel);
                                }
                                calculateDataModel=readData(inputStream);
                            }
                            Log.d(TAG,"getTestFileData "+i);
                            //calculateUtil.StopCalculate();
                            Log.d(TAG,"getTestFileData endTime "+dateFormat.format(new Date()));
                        }
                    }
                }catch (Exception e){
                    Log.d(TAG,e.getMessage());
                }finally {
                    try{
                        if(inputStream!=null){
                            inputStream.close();
                        }
                    }catch (Exception e){

                    }
                }
            }
        });
        thread.start();
    }

    private CalculateDataModel readData(DataInputStream inputStream){
        CalculateDataModel calculateDataModel=null;
        try{
            long time = inputStream.readLong();
            float x = inputStream.readFloat();
            float y = inputStream.readFloat();
            float z = inputStream.readFloat();
            calculateDataModel=new CalculateDataModel();
            calculateDataModel.setTime(time);
            calculateDataModel.setAr(0f);
            if(x==999&&y==999&&z==999){
            }else{
                y =y *9.8f;
            }
            calculateDataModel.setY(y);
        }catch (IOException e){
            return null;
        }
        return calculateDataModel;
    }

    private void getResult(){
        CalculateUtil.getInstance().StopCalculate();
        String filePath=fileUtil.makeDirOrFile(FileUtil.getSDPath()+ File.separator+fileName+FileUtil.algorithmFileNameEnd);//fileUtil.makeDirOrFile(FileUtil.getSDPath()+ File.separator+fileName+FileUtil.algorithmFileNameEnd);
        List listTemp=new ArrayList<>();
        List<SmoothBumpModel> list=new ArrayList<SmoothBumpModel>();
        List<String> dataList=fileUtil.readStringListFormFile(filePath);
        if(dataList==null){
            Log.d(TAG," dataList null");
            return;
        }
        for (String item:dataList
        ) {
            SmoothBumpModel smoothBumpModel= JSON.toJavaObject(JSONObject.parseObject(item),SmoothBumpModel.class);
            listTemp.add(smoothBumpModel);

        }
        list.addAll(AlgorithmUtil.getInstance().mergeTo(listTemp));
        int oneLevelNum=0,twoLevelNum=0,threeLevelNum=0,fourLevelNum=0,fiveLevelNum=0;
        for (SmoothBumpModel item:list
        ) {
            switch (item.getLevel()){
                case 1:
                    oneLevelNum++;
                    break;
                case 2:
                    twoLevelNum++;
                    break;
                case 3:
                    threeLevelNum++;
                    break;
                case 4:
                    fourLevelNum++;
                    break;
                case 5:
                    fiveLevelNum++;
                    break;
            }
        }
        Log.d(TAG,"one "+oneLevelNum+"  two "+twoLevelNum+" three "+threeLevelNum+"  four "+fourLevelNum+" five "+fiveLevelNum);
    }

    private void goToCalculateView(){
        CalculateUtil.getInstance().StopCalculate();
        Intent intent=new Intent(MainActivity.this,CalculateActivity.class);
        intent.putExtra("workId",fileName);
        startActivityForResult(intent,1);
    }
}
