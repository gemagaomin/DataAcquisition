package com.gema.soft.dataacquisition.activitys;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gema.soft.dataacquisition.R;
import com.gema.soft.dataacquisition.adapter.HzAdapter;
import com.gema.soft.dataacquisition.utils.BluetoothUtil;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.Log;
import com.gema.soft.dataacquisition.utils.MyApplication;

import java.util.List;

public class SettingHzActivity extends BaseActivity {
    private String TAG="SettingHzActivity";
    private String selectedHZ="10Hz";
    private ListView listViewHz;
    private HzAdapter hzAdapter;
    private List<String> list;
    private LinearLayout hzll;
    private int isCanConn=-1;
    private boolean isCan=true;
    private Button cancelBtn;
    private Button button;
    private TextView hzStatusTV;
    BluetoothUtil bluetoothUtil=BluetoothUtil.getInstance();
    private final int REFRESH_DATA=0;
    private final int HZ=1;
    private final int HZ_SETTING=2;
    private final int HZ_SETTING_SUCCESS=3;
    private final int HZ_SETTING_BTN=4;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what=msg.what;
            switch (what){
                case REFRESH_DATA:
                    hzAdapter.notifyDataSetChanged();
                    break;
                case HZ:
                    Toast.makeText(getBaseContext(),"频率设置失败，请重新设置。否则使用"+selectedHZ,Toast.LENGTH_LONG).show();
                    break;
                case HZ_SETTING:
                    hzStatusTV.setText("频率设置中。");
                    break;
                case HZ_SETTING_SUCCESS:
                    hzStatusTV.setText("频率设置成功。");
                    finish();
                    break;
                case HZ_SETTING_BTN:
                    Toast.makeText(getBaseContext(),"频率设置中，请稍后操作。",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_hz);
        SharedPreferences httpIP = getSharedPreferences(MyApplication.DATA_HTTP_IP, Activity.MODE_PRIVATE);
        String oldHZ = httpIP.getString("hz", "");
        if(!TextUtils.isEmpty(oldHZ)){
            selectedHZ=oldHZ;
        }
        list=DataUtil.hzList;
        hzll=findViewById(R.id.setting_hz_ll);
        hzStatusTV=findViewById(R.id.setting_hz_status);
        hzll.setVisibility(View.VISIBLE);
        listViewHz=findViewById(R.id.setting_hz_lv);
        hzAdapter=new HzAdapter(list,getBaseContext());
        hzAdapter.setSelectedOne(selectedHZ);
        listViewHz.setAdapter(hzAdapter);
        listViewHz.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedHZ=list.get(position);
                hzAdapter.setSelectedOne(selectedHZ);
                handler.sendEmptyMessage(0);
            }
        });
        cancelBtn=findViewById(R.id.setting_hz_btn_cancle);
        cancelBtn.setOnClickListener(this);
        button=findViewById(R.id.setting_hz_btn_ok);
        button.setOnClickListener(this);
        MyApplication.addActivity(this);
    }

    @Override
    public void onNoDoubleClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.setting_hz_btn_ok:
                if(!isCan){
                    Toast.makeText(getBaseContext(),"设置中，请稍等。",Toast.LENGTH_LONG).show();
                    return;
                }
                isCan=false;
                if(isCanConn==0){
                    handler.sendEmptyMessage(HZ_SETTING_BTN);
                    return;
                }
                if(isCanConn==-1){
                    isCanConn=0;
                    if(TextUtils.isEmpty(selectedHZ)){
                        Toast.makeText(SettingHzActivity.this,"请选择频率",Toast.LENGTH_LONG).show();
                        return;
                    }
                    byte[] reHz=bluetoothUtil.cmdMagCali;
                    byte[] temp=DataUtil.hzBtList;
                    List<String> hzList=DataUtil.hzList;
                    for(int i=0;i<hzList.size();i++){
                        String one=hzList.get(i);
                        if(selectedHZ.equals(one)){
                            reHz[3]=temp[i];
                        }
                    }
                    bluetoothUtil.cmdMagCali=reHz;
                    handler.sendEmptyMessage(HZ_SETTING);
                    initConn();
                }
                break;
            case R.id.setting_hz_btn_cancle:
                finish();
                break;
        }
    }

    public void initConn(){
        if(bluetoothUtil.writeBytes(bluetoothUtil.cmdMagCali)){
            Thread thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean isSave=true;
                    int num=0;
                    try{
                        while (isSave){
                            if(num>10){
                                isSave=false;
                                handler.sendEmptyMessage(HZ);
                            }
                            if(bluetoothUtil.bIdle){
                                isSave=false;
                                boolean isConn=true;
                                int saveNum=0;
                                while (isConn){
                                    if(saveNum>10){
                                        isConn=false;
                                        handler.sendEmptyMessage(HZ);
                                    }
                                    if(bluetoothUtil.writeBytes(bluetoothUtil.cmdSaveMagCali)){
                                        isConn=false;
                                        boolean isCB=true;
                                        int cBNum=0;
                                        while (isCB){
                                            if(cBNum>10){
                                                isCB=false;
                                                handler.sendEmptyMessage(HZ);
                                            }
                                            if(bluetoothUtil.bIdle){
                                                isCB=false;
                                                bluetoothUtil.connectBluetooth();
                                                if(bluetoothUtil.reReadSubscribeData()){
                                                    isCanConn=1;
                                                    SharedPreferences httpIP = getSharedPreferences(MyApplication.DATA_HTTP_IP, Activity.MODE_PRIVATE);
                                                    SharedPreferences.Editor editorHz = httpIP.edit();
                                                    editorHz.putString("hz", selectedHZ);
                                                    editorHz.commit();
                                                    handler.sendEmptyMessage(HZ_SETTING_SUCCESS);
                                                }
                                            }
                                            Thread.sleep(200);
                                            cBNum++;
                                        }
                                    }
                                    Thread.sleep(200);
                                    saveNum++;
                                }
                            }
                            Thread.sleep(200);
                            num++;
                        }
                    }catch (Exception o){
                        Log.d(TAG, "run: "+o.getMessage());
                    }
                }
            });
            thread.start();
        }
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK||keyCode==KeyEvent.KEYCODE_HOME) {
            return isBackKey();
        }
        return false;
    }

    private boolean isBackKey() {
        // 这儿做返回键的控制，如果自己处理返回键逻辑就返回true，如果返回false,代表继续向下传递back事件，由系统去控制
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.removeActivity(this);
    }
}
