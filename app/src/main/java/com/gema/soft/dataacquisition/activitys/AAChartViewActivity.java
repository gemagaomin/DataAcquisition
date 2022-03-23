package com.gema.soft.dataacquisition.activitys;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.gema.soft.dataacquisition.R;
import com.gema.soft.dataacquisition.enums.FileEnum;
import com.gema.soft.dataacquisition.formatter.MyXFormatter;
import com.gema.soft.dataacquisition.models.FileDataByteModel;
import com.gema.soft.dataacquisition.models.TrainInfoModel;
import com.gema.soft.dataacquisition.models.WorkModel;
import com.gema.soft.dataacquisition.pojo.WorkPojo;
import com.gema.soft.dataacquisition.utils.BluetoothUtil;
import com.gema.soft.dataacquisition.utils.CalculateUtil;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.FileUtil;
import com.gema.soft.dataacquisition.utils.Log;
import com.gema.soft.dataacquisition.utils.MyApplication;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AAChartViewActivity extends BaseActivity {
    private String TAG="AAChartViewActivity";
    private TextView bluetoothLineStatusTV;
    private TextView bluetoothLineDataTV;
    private LinearLayout runningLL;
    private LinearLayout upFileLL;

    private BluetoothUtil bluetoothUtil;
    private CalculateUtil calculateUtil;
    private Timer thread;
    private Timer thread1;
    private Timer thread2;
    private TimerTask timerTask1;
    private TimerTask timerTask2;
    private final Timer bluetoothLineStatusTimer=new Timer();
    private TimerTask bluetoothLineTask;
    private FileUtil fileUtil;
    private final int NO_HAVE_IP_MENU=5;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what=msg.what;
            switch (what){
                case 0:
                    FileDataByteModel fileDataByteModel= dataUtil.getOneFileDataByteModel();
                    if(fileDataByteModel!=null){
                        long time=fileDataByteModel.getTime();
                        float showY=fileDataByteModel.getY()*9.8f;
                        setOneDataSS(new Entry(time,showY));
                        setOneDataSLong(fileDataByteModel);
                    }
                    //todo 2021.9.27 之前一直更新mLineChar.invalidate();
                    break;
                case 1:
                    //todo 2021.9.27 测试1s的刷新变10HZ
                    mLineChar.invalidate();
                    mLineCharLong.invalidate();
                    //todo 更新X和Y轴数据
                    break;
                case 3:
                    goUpdateFile();
                    break;
                case 4:
                    Bundle bundle=msg.getData();
                    FileDataByteModel fileDataByteModel1=dataUtil.getNewData();
                    StringBuffer y=new StringBuffer();
                    if(fileDataByteModel1!=null){
                        Date date=new Date(fileDataByteModel1.getTime());
                        DateFormat bf = new SimpleDateFormat("HH:mm:ss.SSS");
                        y.append(bf.format(date));
                        y.append("  Y轴：").append(fileDataByteModel1.getY()).append(" (g)");
                        y.append("  X轴：").append(fileDataByteModel1.getX()).append(" (g)");
                        y.append("  Z轴：").append(fileDataByteModel1.getZ()).append(" (g)");
                    }else{
                        y.append(" 设备连接中...");
                    }
                    bluetoothLineDataTV.setText(y.toString());
                    bluetoothLineStatusTV.setText(bundle.getString("data"));
                    break;
                    case NO_HAVE_IP_MENU:
                    Toast.makeText(AAChartViewActivity.this,"离线版本无法使用当前功能。",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private WorkPojo workPojo;
    private TrainInfoModel trainInfoModel;
   /* private DBUtil dbUtil;*/
    private DataUtil dataUtil;
    private Toolbar toolbar;
    private LineChart mLineChar;
    private LineChart mLineCharLong;
    private int LongMaxNumber=250;
    private int shortMaxNumber=50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);*/
        setContentView(R.layout.activity_aachart_view);
        toolbar=findViewById(R.id.aachart_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo 测试后期做什么
            }
        });
        initInfo();
        initView();
        //todo 测试指定时间内获取的数据条数
        if(DataUtil.isTest){
            Timer timer=new Timer();
            TimerTask timerTask=new TimerTask() {
                @Override
                public void run() {
                    endGetData();
                }
            };
            timer.schedule(timerTask,60000);
        }
       /* Timer timer=new Timer();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                endGetData();
            }
        };
        timer.schedule(timerTask,10000);*/
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        getMenuInflater().inflate(R.menu.aachart_titile_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case R.id.aachart_view_title_menu_end_work:
                goUpdateFile();
                break;
            case R.id.aachart_view_title_menu_setting:
                if(DataUtil.getInstance().AppHasNet==DataUtil.getInstance().APP_HAS_NET){
                    goSetting();
                }else{
                    item.setChecked(false);
                    handler.sendEmptyMessage(NO_HAVE_IP_MENU);
                }
                break;
            case R.id.aachart_view_title_qx_menu_setting:
                if(workPojo!=null){
                    endGetData();
                    fileUtil.deleteFile(dataUtil.runFilePath);
                    String filePath=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.INDEX_FILE_PATH))+fileUtil.indexName;
                    fileUtil.deleteFile(filePath);
                    String algorithmFilePath=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.WORK_FILE_PATH))+workPojo.getWorkId()+FileUtil.algorithmFileNameEnd;
                    fileUtil.deleteFile(algorithmFilePath);
                    dataUtil.runFilePath=null;
                    dataUtil.runWorkPojo=null;
                    workPojo=null;
                }
                cancelWork();
                goToMain();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void endGetData(){
        dataUtil.isStart=false;
        bluetoothUtil.isFirstLine=true;
        bluetoothUtil.startTime=0;
        calculateUtil.StopCalculate();
        long endTime=new Date().getTime();
        int nub=dataUtil.number;
        String str="endGetData: "+" number"+nub;
        Log.d(TAG, "endGetData: "+str);
        float a=(endTime-Long.parseLong(dataUtil.runWorkPojo.getStartTime()))/1000;
        float n=nub==0?0:nub/a;
        Log.d(TAG, "endGetData: "+str);
        workPojo.setEndTime(endTime+"");
        dataUtil.endTime=endTime;
        workPojo.setWorkStatus(dataUtil.WORK_STATUS_WAIT_FILE_UP);
        WorkModel workModel=new WorkModel(workPojo);
        SharedPreferences sharedPreferences=getSharedPreferences(MyApplication.DATA_HTTP_WORK_INFO, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("workData", JSONArray.toJSON(workModel).toString());
        editor.commit();
        //dbUtil.update(DataUtil.TableNameEnum.WORK.toString(),workModel.getContentValues(workModel)," workid = ? ",new String[]{workModel.getWorkId()});

    }
    public void goSetting(){
        Intent intent = new Intent(this, SettingIPActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        workPojo=dataUtil.runWorkPojo;
        if(workPojo!=null){
            trainInfoModel=workPojo.getTrainInfo();
            toolbar.setTitle(trainInfoModel.trainTypeIdName()+" "+trainInfoModel.getTrainOrder());
            if(DataUtil.getInstance().WORK_STATUS_RUNNING.equals(workPojo.getWorkStatus())){
                upFileLL.setVisibility(View.GONE);
                runningLL.setVisibility(View.VISIBLE);
                start();
            }else if(DataUtil.getInstance().WORK_STATUS_WAIT_FILE_UP.equals(workPojo.getWorkStatus())||dataUtil.WORK_STATUS_FILE_UP_ERROR.equals(workPojo.getWorkStatus())){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        upFileLL.setVisibility(View.VISIBLE);
                        runningLL.setVisibility(View.GONE);
                    }
                });
            }
        }
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        end();
    }

    @Override
    public void onNoDoubleClick(View v) {

    }

    private void initInfo(){
        dataUtil=DataUtil.getInstance();
        fileUtil= FileUtil.getInstance();
        bluetoothUtil=BluetoothUtil.getInstance();
        calculateUtil=CalculateUtil.getInstance();
    }

    private void initView(){
        bluetoothLineStatusTV=findViewById(R.id.aachart_line_status);
        bluetoothLineDataTV=findViewById(R.id.aachart_data);
        runningLL=findViewById(R.id.aachart_running_ll);
        upFileLL=findViewById(R.id.aachart_upfile_ll);
        mLineChar=findViewById(R.id.mLineChar);
        createdLineChar(mLineChar);
        mLineCharLong=findViewById(R.id.mLineChar_long);
        createdLineCharLong(mLineCharLong);
    }

    private void createdLineChar(LineChart mLineChar){
        //后台绘制
        mLineChar.setDrawGridBackground(false);
        //设置描述文本
        mLineChar.getDescription().setEnabled(false);
        mLineChar.setPinchZoom(true);

        mLineChar.setTouchEnabled(false); // 设置是否可以触摸
        mLineChar.setDragEnabled(false);// 是否可以拖拽
        mLineChar.setScaleEnabled(false);// 是否可以缩放 x和y轴, 默认是true
        mLineChar.setScaleXEnabled(false); //是否可以缩放 仅x轴
        mLineChar.setScaleYEnabled(false); //是否可以缩放 仅y轴
        mLineChar.setPinchZoom(false);  //设置x轴和y轴能否同时缩放。默认是否
        mLineChar.setDoubleTapToZoomEnabled(false);//设置是否可以通过双击屏幕放大图表。默认是true
        mLineChar.setHighlightPerDragEnabled(false);//能否拖拽高亮线(数据点与坐标的提示线)，默认是true
        mLineChar.setDragDecelerationEnabled(false);//拖拽滚动时，手放开是否会持续滚动，默认是true（false是拖到哪是哪，true拖拽之后还会有缓冲）
        XAxis xAxis = mLineChar.getXAxis();
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setValueFormatter(new MyXFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setEnabled(true);
        xAxis.setLabelCount(shortMaxNumber);
        xAxis.setDrawLabels(false);
        YAxis leftAxis = mLineChar.getAxisLeft();
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setAxisMaximum(10);
        leftAxis.setAxisMinimum(-10);
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setTextSize(20);
        YAxis rightAxis = mLineChar.getAxisRight();
        rightAxis.setTextColor(ColorTemplate.getHoloBlue());
        rightAxis.setAxisMaximum(10);
        rightAxis.setAxisMinimum(-10);
        rightAxis.setDrawGridLines(true);
        rightAxis.setTextColor(Color.BLACK);
        rightAxis.setTextSize(20);
        mLineChar.setData(new LineData());
        mLineChar.invalidate();
    }

    private void createdLineCharLong(LineChart mLineChar){
        //后台绘制
        mLineChar.setDrawGridBackground(false);
        //设置描述文本
        mLineChar.getDescription().setEnabled(false);
        mLineChar.setTouchEnabled(false); // 设置是否可以触摸
        mLineChar.setDragEnabled(false);// 是否可以拖拽
        mLineChar.setScaleEnabled(false);// 是否可以缩放 x和y轴, 默认是true
        mLineChar.setScaleXEnabled(false); //是否可以缩放 仅x轴
        mLineChar.setScaleYEnabled(false); //是否可以缩放 仅y轴
        mLineChar.setPinchZoom(false);  //设置x轴和y轴能否同时缩放。默认是否
        mLineChar.setDoubleTapToZoomEnabled(false);//设置是否可以通过双击屏幕放大图表。默认是true
        mLineChar.setHighlightPerDragEnabled(false);//能否拖拽高亮线(数据点与坐标的提示线)，默认是true
        mLineChar.setDragDecelerationEnabled(false);//拖拽滚动时，手放开是否会持续滚动，默认是true（false是拖到哪是哪，true拖拽之后还会有缓冲）

        XAxis xAxis = mLineChar.getXAxis();
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setDrawLabels(false);
        xAxis.setLabelCount(LongMaxNumber);
        YAxis leftAxis = mLineChar.getAxisLeft();
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setAxisMaximum(10);
        leftAxis.setAxisMinimum(-10);
        leftAxis.setTextSize(20);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setTextColor(Color.BLACK);
        YAxis rightAxis = mLineChar.getAxisRight();
        rightAxis.setTextColor(ColorTemplate.getHoloBlue());
        rightAxis.setAxisMaximum(10);
        rightAxis.setAxisMinimum(-10);
        rightAxis.setTextSize(20);
        rightAxis.setDrawGridLines(true);
        rightAxis.setGranularityEnabled(true);
        rightAxis.setTextColor(Color.BLACK);
        mLineChar.setData(new LineData());
        mLineChar.invalidate();
    }

    private int i = 0;
    private int iLong = 0;

    private void setOneDataSS(Entry y){
        LineData data = mLineChar.getData();
            if (data != null) {
                ILineDataSet set = data.getDataSetByIndex(0);
                if (set == null) {
                    set = createSet("加速度 ("+shortMaxNumber/50+"s)");
                    set.setDrawValues(false);
                    data.addDataSet(set);
                }
                if (i > shortMaxNumber) {
                    set.removeFirst();
                    data.addEntry(new Entry(set.getEntryCount()+(float)(i-shortMaxNumber), y.getY()), 0);
                } else {
                    data.addEntry(new Entry(set.getEntryCount() , y.getY()),0);
                }
                i++;
                data.notifyDataChanged();
                mLineChar.notifyDataSetChanged();
                // 折线图最多显示的数量
                mLineChar.setVisibleXRangeMaximum(shortMaxNumber);
                // mChart.setVisibleYRange(30, AxisDependency.LEFT);
                // move to the latest entry
                 mLineCharLong.moveViewToX(data.getEntryCount());

                // this automatically refreshes the chart (calls invalidate())
                // mChart.moveViewTo(data.getXValCount()-7, 55f,
                // AxisDependency.LEFT);
            }
    }

    private void setOneDataSLong( FileDataByteModel fileDataByteModel){
        LineData data = mLineCharLong.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well
            if (set == null) {
                set = createSet("加速度 ("+LongMaxNumber/50+" s)");
                set.setDrawValues(false);
                data.addDataSet(set);
            }
            //在120个数据之后开始删除已经没有显示在折线图范围的数据 后面的数据还是从右向左出现  当然也可以不用删除，这样可以滑动折线图浏览以前的数据

           /* for (FileDataByteModel f:fileDataByteModels
            ) {*/
                if (iLong > LongMaxNumber){
                    set.removeFirst();
                    data.addEntry(new Entry(set.getEntryCount()+(float)(iLong-LongMaxNumber),  fileDataByteModel.getY()*9.8f), 0);
                }else{
                    data.addEntry(new Entry(set.getEntryCount(),fileDataByteModel.getY()*9.8f), 0);
                }
                iLong++;
                data.notifyDataChanged();
                mLineCharLong.notifyDataSetChanged();
            /*}*/
            // let the chart know it's data has changed


            // 折线图最多显示的数量
            mLineCharLong.setVisibleXRangeMaximum(LongMaxNumber);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            //
             mLineCharLong.moveViewToX(data.getEntryCount());
            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet(String tlName) {
        LineDataSet set1=new LineDataSet(null,tlName);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(0.5f);
        set1.setCircleRadius(1.5f);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(false);
        if (Utils.getSDKInt() >= 18) {
            // 填充背景只支持18以上
            //Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.ic_launcher);
            //set1.setFillDrawable(drawable);
            set1.setFillColor(Color.YELLOW);
        } else {
            set1.setFillColor(Color.BLACK);
        }
        return set1;
    }

    private void start(){
        if(thread==null){
            thread=new Timer();
            timerTask1=new TimerTask() {
                @Override
                public void run() {
                    if(dataUtil.isStart){
                        handler.sendEmptyMessage(0);
                    }
                }
            };
            thread.schedule(timerTask1,0,20);
        }
       if(thread1==null){
            thread1=new Timer();
            timerTask2=new TimerTask() {
                @Override
                public void run() {
                    if(dataUtil.isStart){
                        handler.sendEmptyMessage(1);
                    }
                }
            };
            thread1.schedule(timerTask2,0,100);
        }
    }

    private void end(){
        if(thread!=null){
            thread.cancel();
            thread=null;
        }
        if(thread1!=null){
            thread1.cancel();
            thread1=null;
        }
        if(thread2!=null){
            thread2.cancel();
            thread2=null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.removeActivity(this);
    }

    public void goUpdateFile(){
        Intent intent=null;
        if(dataUtil.AppHasNet==dataUtil.APP_HAS_NET){
            endGetData();
            intent=new Intent(AAChartViewActivity.this,DialogActivity.class);
        }else if(dataUtil.AppHasNet==dataUtil.APP_NOT_HAS_NET){
            intent=new Intent(AAChartViewActivity.this,DialogNoNetActivity.class);
        }
        startActivityForResult(intent,3);
    }

    public void cancelWork(){
        SharedPreferences sharedPreferences=getSharedPreferences(MyApplication.DATA_HTTP_WORK_INFO, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("workData", null);
        editor.commit();
        //dbUtil.delete(DataUtil.TableNameEnum.WORK.toString(),"  workid = ?",new String[]{workPojo.getWorkId()});
        dataUtil.runFilePath=null;
        dataUtil.runWorkPojo=null;
        workPojo=null;
        BluetoothUtil.getInstance().myQueue.clear();
    }

    public void endWork(){
        cancelWork();
    }

    private void goToMain(){
        Intent intent=new Intent();
        intent.setClass(this,MainActivity.class);
        onActivityResult(2,4,intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 3:
                if(resultCode==4){
                    endWork();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            goToMain();
                        }
                    });
                }else if(resultCode==5){
                    endWork();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            goToMain();
                        }
                    });
                }
                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK||keyCode==KeyEvent.KEYCODE_HOME) {
            return isCosumenBackKey();
        }
        return false;
    }

    private boolean isCosumenBackKey() {
        // 这儿做返回键的控制，如果自己处理返回键逻辑就返回true，如果返回false,代表继续向下传递back事件，由系统去控制
        return true;
    }

    public void startTimer(){
        if(bluetoothLineTask==null){
            bluetoothLineTask=new TimerTask() {
                @Override
                public void run() {
                    String mac=bluetoothUtil.selectDeviceMacs;
                    if(!TextUtils.isEmpty(mac)&&!mac.equals(dataUtil.blueMac)){
                        DataUtil.getInstance().blueMac=mac;
                    }
                    Message message=new Message();
                    message.what=4;
                    Bundle bundle=new Bundle();
                    bundle.putString("data",mac);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            };
            bluetoothLineStatusTimer.schedule(bluetoothLineTask,0,100);
        }
    }
}
