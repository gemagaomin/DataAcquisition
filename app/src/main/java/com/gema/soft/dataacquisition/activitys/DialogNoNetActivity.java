package com.gema.soft.dataacquisition.activitys;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.gema.soft.dataacquisition.R;
import com.gema.soft.dataacquisition.enums.FileEnum;
import com.gema.soft.dataacquisition.models.FileModel;
import com.gema.soft.dataacquisition.models.WorkModel;
import com.gema.soft.dataacquisition.pojo.WorkPojo;
import com.gema.soft.dataacquisition.utils.BluetoothUtil;
import com.gema.soft.dataacquisition.utils.CalculateUtil;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.FileUtil;
import com.gema.soft.dataacquisition.utils.MyApplication;

import java.io.File;
import java.util.Date;
import java.util.List;

public class DialogNoNetActivity extends BaseActivity {
    private FileUtil fileUtil;
    private WorkPojo workPojo;
    private DataUtil dataUtil;
    private Button saveWorkBtn;
    private Button cancelBtn;
    private TextView showTipsTV;
    private final int SAVE_WORK_SUCCESS=0,SAVE_WORK_FAIL=1,SAVE_WORK=2,FILE_EXIST=3,SAVE_WORK_RUNNING=4,SHOW_CALCULATE_RECORD=5,ASK_IF_END_WORK=6;
    private final int SAVE_WORK_SUCCESS_STATE=0,SAVE_WORK_FAIL_STATE=1,SAVE_WORK_STATE=2,SAVE_WORK_RUNNING_STATE=4,SHOW_CALCULATE_RECORD_STATE=5,ASK_IF_END_WORK_STATE=6;
    private int state=-1;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what=msg.what;
            cancelBtn.setVisibility(View.GONE);
            switch (what){
                case SAVE_WORK:
                    showTipsTV.setText("信息保存中，请等候...");
                    break;
                case SAVE_WORK_FAIL:
                    showTipsTV.setText("文件保存失败，点击确定按钮再次保存。");
                    break;
                case SAVE_WORK_SUCCESS:
                    showTipsTV.setText("保存成功，点击确定按钮查看本次任务冲动记录。");
                    break;
                case SHOW_CALCULATE_RECORD:
                    showTipsTV.setText("保存成功，点击确定按钮完成本次任务。");
                    break;
                case FILE_EXIST:
                    showTipsTV.setText("本次任务，文件丢失。");
                    break;
                case SAVE_WORK_RUNNING:
                    Toast.makeText(getBaseContext(),"工作保存中，请稍等...",Toast.LENGTH_LONG).show();
                    break;
                case ASK_IF_END_WORK:
                    showTipsTV.setText("点击确定按钮确认结束、点击取消按钮返回。");
                    cancelBtn.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_no_net);
        MyApplication.addActivity(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initData();
        initView();
    }

    public void initView(){
        saveWorkBtn=findViewById(R.id.dialog_no_net_save_btn);
        showTipsTV=findViewById(R.id.dialog_no_net_show_tv);
        saveWorkBtn.setOnClickListener(this);
        cancelBtn=findViewById(R.id.dialog_no_cancel_btn);
        cancelBtn.setOnClickListener(this);
        if(dataUtil.WORK_STATUS_WAIT_FILE_UP.equals(workPojo.getWorkStatus())){
            handler.sendEmptyMessage(SAVE_WORK_SUCCESS);
        }else{
            handler.sendEmptyMessage(ASK_IF_END_WORK);
        }

    }

    public void initData(){
        dataUtil=DataUtil.getInstance();
        fileUtil=FileUtil.getInstance();
        workPojo=dataUtil.runWorkPojo;
        if(dataUtil.WORK_STATUS_WAIT_FILE_UP.equals(workPojo.getWorkStatus())){
            state=SAVE_WORK_SUCCESS_STATE;
        }else{
            state=SAVE_WORK_STATE;
        }

    }

    @Override
    public void onNoDoubleClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.dialog_no_net_save_btn:
                switch (state){
                    case SAVE_WORK_SUCCESS_STATE:
                        /*String filePath=FileUtil.getInstance().makeDirOrFile(FileUtil.getInstance().getFilePath(FileEnum.INDEX_FILE_PATH))+FileUtil.getInstance().indexName;
                        FileUtil.getInstance().deleteFile(filePath);
                        Intent i = new Intent();
                        setResult(5, i);
                        finish();*/
                        //todo 显示冲动记录
                        goToCalculateView();
                        break;
                    case SHOW_CALCULATE_RECORD_STATE:
                        String filePath=FileUtil.getInstance().makeDirOrFile(FileUtil.getInstance().getFilePath(FileEnum.INDEX_FILE_PATH))+FileUtil.getInstance().indexName;
                        FileUtil.getInstance().deleteFile(filePath);
                        Intent i = new Intent();
                        setResult(5, i);
                        finish();
                        break;
                    case SAVE_WORK_STATE:
                        buildData();
                        break;
                    case SAVE_WORK_FAIL_STATE:
                        state=SAVE_WORK_STATE;
                        handler.sendEmptyMessage(SAVE_WORK_FAIL);
                        break;
                    case SAVE_WORK_RUNNING:
                        handler.sendEmptyMessage(SAVE_WORK_RUNNING);
                        break;
                    case ASK_IF_END_WORK_STATE:
                        state=SAVE_WORK_STATE;
                        handler.sendEmptyMessage(ASK_IF_END_WORK);
                        break;
                }
                break;
            case R.id.dialog_no_cancel_btn:
                finish();
                break;
        }
    }
    public void endGetData(){
        dataUtil.isStart=false;
        BluetoothUtil bluetoothUtil=BluetoothUtil.getInstance();
        bluetoothUtil.isFirstLine=true;
        bluetoothUtil.startTime=0;
        CalculateUtil.getInstance().StopCalculate();
        long endTime=new Date().getTime();
        workPojo.setEndTime(endTime+"");
        dataUtil.endTime=endTime;
        workPojo.setWorkStatus(dataUtil.WORK_STATUS_WAIT_FILE_UP);
        WorkModel workModel=new WorkModel(workPojo);
        SharedPreferences sharedPreferences=getSharedPreferences(MyApplication.DATA_HTTP_WORK_INFO, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("workData", JSONArray.toJSON(workModel).toString());
        editor.commit();
        //dbUtil.update(DataUtil.TableNameEnum.WORK.toString(),workModel.getContentValues(workModel)," workid = ? ",new String[]{workModel.getWorkId()});
        /*
        int nub=dataUtil.number;
        float a=(endTime-Long.parseLong(dataUtil.runWorkPojo.getStartTime()))/1000;
        float n=nub==0?0:nub/a;
        String str="endGetData: "+n+"   s "+a+" number"+nub;
        Log.d(TAG, "endGetData: "+str);*/
    }

    public void buildData(){
        handler.sendEmptyMessage(SAVE_WORK);
        state=SAVE_WORK_RUNNING_STATE;
        String filePath=fileUtil.getFilePath(FileEnum.WORK_FILE_PATH)+"/"+workPojo.getWorkId();
        final FileModel fileModel=new FileModel(workPojo.getWorkId(),workPojo.getStartTime(),filePath);
        File file=new File(fileModel.getFilePath());
        String algorithmFilePath=fileUtil.getFilePath(FileEnum.WORK_FILE_PATH)+"/"+workPojo.getWorkId()+FileUtil.algorithmFileNameEnd;
        if(!file.exists()){
            handler.sendEmptyMessage(FILE_EXIST);
            return;
        }
        File fileAlgorithmFile=new File(algorithmFilePath);
        if(!fileAlgorithmFile.exists()){
            handler.sendEmptyMessage(FILE_EXIST);
            return;
        }
        String filePathTemp=filePath+"1";
        final String filePathTempName=workPojo.getWorkId()+"1";
        Thread thread=new Thread(){
            @Override
            public void run() {
                String filePathIndex=FileUtil.getInstance().makeDirOrFile(FileUtil.getInstance().getFilePath(FileEnum.INDEX_FILE_PATH))+FileUtil.getInstance().indexName;
                List<Object[]> list=FileUtil.getInstance().getIndexDataList(filePathIndex);
                if(list!=null&&list.size()>0&&(long)(list.get(list.size()-1)[1])!=-1){
                    FileUtil.getInstance().WriteIndexMessageToFile(-1,filePathIndex,filePath);
                    BluetoothUtil.getInstance().myQueue.clear();
                }
                boolean  isRefFile=FileUtil.getInstance().refFileNewTemp(filePath,filePathTemp);
                if(!isRefFile){
                    handler.sendEmptyMessage(SAVE_WORK_FAIL);
                    return;
                }
                fileModel.setFileName(filePathTempName);
                fileModel.setFilePath(filePathTemp);
                workPojo.setFileName(filePathTemp);
                workPojo.setWorkStatus(dataUtil.WORK_STATUS_FINISH);
                endGetData();
                WorkModel workModel=new WorkModel(workPojo);
                String fileDataPath=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.WORK_FILE_INFO_PATH)+"/"+"workinfodata.txt");
                boolean fileSaveResult=fileUtil.WriteDataToFile(fileDataPath, JSON.toJSONString(workModel));
                if(fileSaveResult){
                    state=SAVE_WORK_SUCCESS_STATE;
                    handler.sendEmptyMessage(SAVE_WORK_SUCCESS);
                }else{
                    state=SAVE_WORK_FAIL_STATE;
                    handler.sendEmptyMessage(SAVE_WORK_FAIL);
                }
            }
        };
        thread.start();
    }

    private void goToCalculateView(){
        Intent intent=new Intent(DialogNoNetActivity.this,CalculateActivity.class);
        intent.putExtra("workId",workPojo.getWorkId());
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==2){
                endWork();
                finish();
            }
        }
    }

    private void endWork(){//todo 查看记录后直接结束工作
        String filePath=FileUtil.getInstance().makeDirOrFile(FileUtil.getInstance().getFilePath(FileEnum.INDEX_FILE_PATH))+FileUtil.getInstance().indexName;
        FileUtil.getInstance().deleteFile(filePath);
        Intent i = new Intent();
        setResult(5, i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.removeActivity(this);
    }
}
