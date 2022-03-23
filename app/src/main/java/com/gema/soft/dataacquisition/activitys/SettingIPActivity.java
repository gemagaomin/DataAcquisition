package com.gema.soft.dataacquisition.activitys;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gema.soft.dataacquisition.R;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.MyApplication;

public class SettingIPActivity extends BaseActivity {
    private EditText editText;
    private String TAG="SettingIPActivity";
    private Button cancelBtn;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_ip);
        SharedPreferences httpIP = getSharedPreferences(MyApplication.DATA_HTTP_IP, Activity.MODE_PRIVATE);
        String filePath = httpIP.getString("filePath", "");
        MyApplication.addActivity(this);
        if(!TextUtils.isEmpty(filePath))
            DataUtil.getInstance().FILE_PATH=filePath;
        else
            filePath=DataUtil.getInstance().FILE_PATH;
        editText=findViewById(R.id.setting_file_ip);
        editText.setText(filePath);
        button=findViewById(R.id.setting_btn);
        button.setOnClickListener(this);
        cancelBtn=findViewById(R.id.setting_cancel_btn);
        cancelBtn.setOnClickListener(this);
    }

    @Override
    public void onNoDoubleClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.setting_cancel_btn:
                finish();
                break;
            case R.id.setting_btn:
                String filePath =editText.getText().toString();
                if(TextUtils.isEmpty(filePath)){
                    Toast.makeText(SettingIPActivity.this,"地址不能为空",Toast.LENGTH_LONG).show();
                    return;
                }
                SharedPreferences httpIP = getSharedPreferences(MyApplication.DATA_HTTP_IP, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = httpIP.edit();
                editor.putString("filePath", filePath);
                editor.commit();
                DataUtil.getInstance().FILE_PATH=filePath;
                finish();
                break;
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
