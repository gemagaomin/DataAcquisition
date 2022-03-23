package com.gema.soft.dataacquisition.activitys;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.gema.soft.dataacquisition.R;
import com.gema.soft.dataacquisition.adapter.TrainInfoAdapter;
import com.gema.soft.dataacquisition.models.TrainInfoModel;
import com.gema.soft.dataacquisition.utils.MyApplication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrainInfoDialogActivity extends BaseActivity {
    private List<TrainInfoModel> list;
    private TrainInfoAdapter trainInfoAdapter;
    private ListView listView;
    private Button okBtn;
    private Button cancleBtn;
    private TrainInfoModel selectTrainInfoModel;
    private final int REFRESH_LIST=0;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what=msg.what;
            switch (what){
                case REFRESH_LIST:
                    trainInfoAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_info_dialog);
        MyApplication.addActivity(this);
        initData();
        initView();
    }

    @Override
    public void onNoDoubleClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.train_info_dialog_ok_btn:
                if(selectTrainInfoModel==null){
                    Toast.makeText(getApplicationContext(),"请选择信息后，再点击确认按钮。",Toast.LENGTH_LONG).show();
                    return;
                }
                toToMain(4);
                break;
            case R.id.train_info_dialog_cancel_btn:
                toToMain(3);
                break;
        }

    }

    private void initData(){
        Intent intent=getIntent();
        List<TrainInfoModel> listTemp=(List<TrainInfoModel>) intent.getSerializableExtra("data");
        list=new ArrayList<>();
        if(listTemp!=null&&listTemp.size()>0){
            list=listTemp;
        }
    }

    private void initView(){
        listView=findViewById(R.id.train_info_lv);
        trainInfoAdapter=new TrainInfoAdapter(list,getApplicationContext());
        listView.setAdapter(trainInfoAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectTrainInfoModel=list.get(position);
                trainInfoAdapter.setSelectedOne(selectTrainInfoModel);
                handler.sendEmptyMessage(REFRESH_LIST);
            }
        });

        okBtn=findViewById(R.id.train_info_dialog_ok_btn);
        cancleBtn=findViewById(R.id.train_info_dialog_cancel_btn);
        okBtn.setOnClickListener(this);
        cancleBtn.setOnClickListener(this);

    }

    private void toToMain(int resultCode){
        Intent intent=new Intent();
        intent.setClass(TrainInfoDialogActivity.this,MainActivity.class);
        intent.putExtra("data",(Serializable)selectTrainInfoModel);
        setResult(resultCode,intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.removeActivity(this);
    }
}
