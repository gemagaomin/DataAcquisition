package com.gema.soft.dataacquisition.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.gema.soft.dataacquisition.R;
import com.gema.soft.dataacquisition.adapter.SmoothBumpAdapter;
import com.gema.soft.dataacquisition.enums.FileEnum;
import com.gema.soft.dataacquisition.models.SmoothBumpModel;
import com.gema.soft.dataacquisition.pojo.WorkPojo;
import com.gema.soft.dataacquisition.utils.AlgorithmUtil;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.FileUtil;
import com.gema.soft.dataacquisition.utils.MyApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CalculateActivity extends BaseActivity  {
    private List<SmoothBumpModel> listTemp;
    private ListView calculateDataListView;
    private SmoothBumpAdapter calculateDataAdapter;
    private List calculateDataLists;
    private ListView calculateHeadListView;
    private List calculateHeadLists;
    private FileUtil fileUtil;
    private int oneLevelNum=0,twoLevelNum=0,threeLevelNum=0,fourLevelNum=0,fiveLevelNum=0;
    private TextView oneLevelTV;
    private TextView twoLevelTV;
    private TextView threeLevelTV;
    private TextView fourLevelTV;
    private TextView fiveLevelTV;
    private TextView trainTypeTrainIdTV;
    private TextView trainOrderTV;
    private TextView driverNoTV;
    private TextView fDriverNoTV;
    private TextView editTrainInfoTV;
    private Button okBtn;
    private LinearLayout calculateLoadingLL;
    private TextView calculateLoadingTV;
    private String filePath;
    private final int DATA_LOADING_FAIL=0,FILE_FAIL=1,SHOW_DATA=2;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int what=msg.what;
            switch (what){
                case DATA_LOADING_FAIL:
                    calculateLoadingTV.setText("冲动记录数据加载失败、请重新加载...");
                    break;
                case FILE_FAIL:
                    calculateLoadingTV.setText("冲动记录文件丢失、数据无法加载，点击确定按钮、结束本次任务。");
                    break;
                case SHOW_DATA:
                    refreshListViewData();
                    calculateLoadingLL.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate);
        Toolbar toolbar = findViewById(R.id.calculate_toolbar);
        toolbar.setTitle("冲动记录");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        MyApplication.addActivity(this);
        initData();
        initView();
        Thread thread=new Thread(){
            @Override
            public void run() {
                handler.sendEmptyMessage(SHOW_DATA);
            }
        };
        thread.start();
    }

    private void initData(){
        fileUtil=FileUtil.getInstance();
        calculateHeadLists=new ArrayList<>();
        List heads=new ArrayList();
        heads.add("序号");
        heads.add("发生时间");
        heads.add("冲动值");
        calculateHeadLists.add(heads);
        calculateDataLists=new ArrayList();
    }

    private void readFileData(){
        String fileName=getIntent().getStringExtra("workId");
        filePath=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.WORK_FILE_PATH)+ File.separator+fileName+FileUtil.algorithmFileNameEnd);//fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.WORK_FILE_PATH))+ File.separator+fileName+FileUtil.algorithmFileNameEnd;
        listTemp=new ArrayList<>();
        List<String> dataList=fileUtil.readStringListFormFile(filePath);
        if(dataList==null){
            handler.sendEmptyMessage(FILE_FAIL);
            return;
        }
        for (String item:dataList
        ) {
            SmoothBumpModel smoothBumpModel=JSON.toJavaObject(JSON.parseObject(item),SmoothBumpModel.class);
            listTemp.add(smoothBumpModel);
        }
        calculateDataLists.addAll(AlgorithmUtil.getInstance().mergeTo(listTemp));
        for (int i=0;i<calculateDataLists.size();i++) {
            SmoothBumpModel item=(SmoothBumpModel)calculateDataLists.get(i);
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
    }

    private void initView(){
        okBtn=findViewById(R.id.calculate_start_get_data_btn);
        okBtn.setOnClickListener(this);
        oneLevelTV=findViewById(R.id.smooth_grade_1_tv);
        twoLevelTV=findViewById(R.id.smooth_grade_2_tv);
        threeLevelTV=findViewById(R.id.smooth_grade_3_tv);
        fourLevelTV=findViewById(R.id.smooth_grade_4_tv);
        fiveLevelTV=findViewById(R.id.smooth_grade_5_tv);
        calculateLoadingLL=findViewById(R.id.calculate_loading_ll);
        calculateLoadingTV=findViewById(R.id.calculate_loading_tv);
        calculateHeadListView=findViewById(R.id.calculate_data_title_list_view);
        SmoothBumpAdapter headSmoothBumpAdapter=new SmoothBumpAdapter(calculateHeadLists,getApplicationContext(),0);
        calculateHeadListView.setAdapter(headSmoothBumpAdapter);
        calculateDataListView=findViewById(R.id.calculate_data_list_view);
        calculateDataAdapter=new SmoothBumpAdapter(calculateDataLists,getApplicationContext(),-1);
        calculateDataListView.setAdapter(calculateDataAdapter);

        trainTypeTrainIdTV=findViewById(R.id.train_type_number_tv);
        trainOrderTV=findViewById(R.id.train_order_tv);
        driverNoTV=findViewById(R.id.driver_no_tv);
        fDriverNoTV=findViewById(R.id.f_driver_no_tv);
        editTrainInfoTV=findViewById(R.id.edit_train_info_tv);
        editTrainInfoTV.setOnClickListener(this);
    }
    private void refreshListViewData(){
        readFileData();
        oneLevelTV.setText(oneLevelNum+"");
        twoLevelTV.setText(twoLevelNum+"");
        threeLevelTV.setText(threeLevelNum+"");
        fourLevelTV.setText(fourLevelNum+"");
        fiveLevelTV.setText(fiveLevelNum+"");
        calculateDataAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNoDoubleClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.calculate_start_get_data_btn:
                saveData();
                returnView();
                finish();
                break;
            case R.id.edit_train_info_tv:
                //todo 去编辑基础信息界面
                break;
        }
    }

    private void saveData(){
        String filePathTemp=fileUtil.makeDirOrFile(filePath+1);
        fileUtil.copyFile(filePath,filePathTemp);
        WorkPojo workPojo= DataUtil.getInstance().runWorkPojo;
        if(workPojo!=null){
            fileUtil.writeDataToFile(JSON.toJSONString(workPojo.getTrainInfo()),filePath,false);
        }else{
            fileUtil.writeDataToFile("abc",filePath,false);
        }
        if(fileUtil.writeCalculateDataToFile(calculateDataLists,filePath,true)){
            //todo 后期将备份用的文件删掉
            fileUtil.deleteFile(filePathTemp);
        }else{
            fileUtil.copyFile(filePathTemp,filePath);
        }
    }

    private void returnView(){
        Intent intent=new Intent(CalculateActivity.this,DialogNoNetActivity.class);
        setResult(2, intent);
    }

}
