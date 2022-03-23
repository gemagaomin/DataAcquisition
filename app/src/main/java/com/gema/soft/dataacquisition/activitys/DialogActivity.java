package com.gema.soft.dataacquisition.activitys;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.gema.soft.dataacquisition.R;
import com.gema.soft.dataacquisition.enums.FileEnum;
import com.gema.soft.dataacquisition.models.FileModel;
import com.gema.soft.dataacquisition.models.WorkModel;
import com.gema.soft.dataacquisition.pojo.WorkPojo;
import com.gema.soft.dataacquisition.utils.BluetoothUtil;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.FileUtil;
import com.gema.soft.dataacquisition.utils.HttpUtil;
import com.gema.soft.dataacquisition.utils.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import pl.droidsonroids.gif.GifImageView;

public class DialogActivity extends BaseActivity {
    private HttpUtil httpUtil;
    private DataUtil dataUtil;
    private FileUtil fileUtil;
    private WorkPojo workPojo;
    private TextView textView;
    private ImageView imageView;
    private Button button;
    private GifImageView gifImageView;
    private int BTN_STATUS_FILE_UP_DEFAULT=1;//TODO 继续捋顺按钮显示顺序
    private int BTN_STATUS_FILE_UP_RUNNING=0;
    private int BtnStatus=BTN_STATUS_FILE_UP_DEFAULT;
    private int second=-1;
    private Timer timer=new Timer();
    private TimerTask timerTask;
    private Button cancelBtn;
    private String result;
    private final int FILE_UPDATE_END=4;
    private final int FILE_BTN_CHANGE=0;
    private final int FILE_UPDATE_RUNNING=1;
    private final int FILE_UPDATE_RUNNING_WAIT=2;
    private final int FILE_WAIT_UPDATE=3;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what=msg.what;
            switch (what){
                case FILE_BTN_CHANGE:
                    BtnStatus=BTN_STATUS_FILE_UP_RUNNING;
                    button.setText("确定");
                    if("0".equals(result)){
                        imageView.setVisibility(View.GONE);
                        gifImageView.setVisibility(View.GONE);
                        button.setVisibility(View.VISIBLE);
                        cancelBtn.setVisibility(View.GONE);
                        textView.setText("文件上传成功，点击确定按钮结束工作。");
                    }else if("-1".equals(result)){
                        button.setText("重新上传");
                        textView.setText("因为网络原因，文件上传失败，请稍后重试。");
                        cancelBtn.setVisibility(View.VISIBLE);
                        button.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.VISIBLE);
                        gifImageView.setVisibility(View.GONE);
                        BtnStatus=1;
                        canSubmit();
                    }else if("2".equals(result)){
                        imageView.setVisibility(View.VISIBLE);
                        gifImageView.setVisibility(View.GONE);
                        button.setVisibility(View.GONE);
                        cancelBtn.setVisibility(View.VISIBLE);
                        textView.setText("没有找到本次要上传的文件。");
                    }
                    break;
                case FILE_UPDATE_RUNNING:
                    imageView.setVisibility(View.GONE);
                    gifImageView.setVisibility(View.VISIBLE);
                    cancelBtn.setVisibility(View.GONE);
                    button.setVisibility(View.GONE);
                    textView.setText("文件上传中，请稍等");
                    BtnStatus=2;
                    break;
                case FILE_UPDATE_RUNNING_WAIT:
                    Toast.makeText(getBaseContext(),"文件正在上传，无法进行当前操作。",Toast.LENGTH_LONG).show();
                    break;
                case FILE_WAIT_UPDATE:
                    second--;
                    if(second!=-1){
                        textView.setText(second+"秒后可再次提交。");
                    }else {
                        textView.setText("点击重新提交按钮，进行再次提交。");
                        endTimer();
                    }
                    break;
                case FILE_UPDATE_END:
                    Toast.makeText(getBaseContext(),"提交成功，当前记录结束。",Toast.LENGTH_LONG);
                    Intent i = new Intent();
                    setResult(4, i);
                    finish();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        MyApplication.addActivity(this);
        initData();
        initView();
    }

    @Override
    public void onNoDoubleClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.dialog_file_btn:
                if(second!=-1)
                    return;
                if(BtnStatus==-1){
                    handler.sendEmptyMessage(FILE_UPDATE_RUNNING_WAIT);
                    return;
                }
                if(BtnStatus==BTN_STATUS_FILE_UP_RUNNING){
                    Intent i = new Intent();
                    setResult(4, i);
                    finish();
                }else if(BtnStatus==1){
                    upDateFile();
                }else {
                    handler.sendEmptyMessage(FILE_UPDATE_RUNNING_WAIT);
                }
                break;
            case R.id.dialog_cancel_btn:
                Intent i = new Intent();
                setResult(3, i);
                finish();
                break;
        }

    }

    private void initData(){
        httpUtil=HttpUtil.getInstance();
        dataUtil=DataUtil.getInstance();
        fileUtil=FileUtil.getInstance();
        workPojo=dataUtil.runWorkPojo;
        //todo 将要操作的数据传过来
       // workPojo=(WorkPojo) getIntent().getSerializableExtra("data");
    }

    private void initView(){
        textView=findViewById(R.id.dialog_tv);
        imageView=findViewById(R.id.dialog_im);
        button=findViewById(R.id.dialog_file_btn);
        gifImageView=findViewById(R.id.dialog_gif);
        button.setOnClickListener(this);
        cancelBtn=findViewById(R.id.dialog_cancel_btn);
        cancelBtn.setOnClickListener(this);
    }

    public void upDateFile(){
        handler.sendEmptyMessage(FILE_UPDATE_RUNNING);
        WorkModel workModel=new WorkModel(workPojo);
        String filePath=fileUtil.getFilePath(FileEnum.WORK_FILE_PATH)+"/"+workPojo.getWorkId();
        final FileModel fileModel=new FileModel(workPojo.getWorkId(),workModel.getStartTime(),filePath);
        File file=new File(fileModel.getFilePath());
        if(!file.exists()){
            result="2";
            handler.sendEmptyMessage(FILE_BTN_CHANGE);
            return;
        }
        String filePathTemp=filePath+"1";
        final String filePathTempName=workPojo.getWorkId()+"1";
        //todo 测试给接收的数据分组
        Thread thread=new Thread(){
            @Override
            public void run() {
                String filePathIndex=FileUtil.getInstance().makeDirOrFile(FileUtil.getInstance().getFilePath(FileEnum.INDEX_FILE_PATH))+FileUtil.getInstance().indexName;
                List<Object[]> list=FileUtil.getInstance().getIndexDataList(filePathIndex);
                if(list!=null&&list.size()>0&&(long)(list.get(list.size()-1)[1])!=-1){
                    FileUtil.getInstance().WriteIndexMessageToFile(-1,filePathIndex,filePath);
                    BluetoothUtil.getInstance().myQueue.clear();
                }
                //todo 测试使用
                boolean  isRefFile=FileUtil.getInstance().refFileNewTemp(filePath,filePathTemp);
                if(!isRefFile){
                    result="-1";
                    handler.sendEmptyMessage(FILE_BTN_CHANGE);
                    return;
                }
                fileModel.setFileName(filePathTempName);
                fileModel.setFilePath(filePathTemp);
                Callback callback=new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        result="-1";
                        workModel.setWorkStatus(DataUtil.getInstance().WORK_STATUS_FILE_UP_ERROR);
                        SharedPreferences sharedPreferences=getSharedPreferences(MyApplication.DATA_HTTP_WORK_INFO, Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString("workData", JSONArray.toJSON(workModel).toString());
                        editor.commit();
                        handler.sendEmptyMessage(FILE_BTN_CHANGE);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if(response.isSuccessful())
                        {
                            FileUtil.getInstance().deleteFile(filePathTempName);
                            FileUtil.getInstance().deleteFile(filePath);
                            String filePath=FileUtil.getInstance().makeDirOrFile(FileUtil.getInstance().getFilePath(FileEnum.INDEX_FILE_PATH))+FileUtil.getInstance().indexName;
                            FileUtil.getInstance().deleteFile(filePath);
                            result="0";
                            handler.sendEmptyMessage(FILE_BTN_CHANGE);
                            return;
                        }
                        result="-1";
                        handler.sendEmptyMessage(FILE_BTN_CHANGE);
                    }
                };

                httpUtil.asynchFile(fileModel,callback);
                //todo 改为HTTPS
               /* HTTPSUtil httpsUtil=HTTPSUtil.getInstance(getBaseContext());
                httpsUtil.asynchFile(fileModel,callback);*/
            }
        };
        thread.start();
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

    private void startTimer(){
        if(timerTask==null){
            timerTask=new TimerTask() {
                @Override
                public void run() {
                    if(second>=0){
                        handler.sendEmptyMessage(FILE_WAIT_UPDATE);
                    }
                }
            };
            if(timer==null){
                timer=new Timer();
            }
            timer.schedule(timerTask,0,1000);
        }
    }

    private void endTimer(){
        second=-1;
        BtnStatus=1;
        if(timerTask!=null){
            timerTask.cancel();
            timerTask=null;
        }

    }

    private boolean canSubmit(){
        if(second!=-1){
            return false;
        }
        second=10;
        startTimer();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endTimer();
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
        MyApplication.removeActivity(this);
    }
}
