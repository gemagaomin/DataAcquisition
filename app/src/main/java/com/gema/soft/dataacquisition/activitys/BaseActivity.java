package com.gema.soft.dataacquisition.activitys;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.jetbrains.annotations.Nullable;


public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private boolean ifCanOnclick=true;
    private long lastOnclickTime=0;
    private static final long INTERVAL_TIME=500;
    private String TAG="BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ifCanOnclick=true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void hideInput(){
        InputMethodManager imm=(InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View view=getWindow().peekDecorView();
        if(null!=view){
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }
    public boolean isIfCanOnclick() {
        return ifCanOnclick;
    }

    public void lockIfCanOnclick() {
        this.ifCanOnclick = false;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if(isIfCanOnclick()){
            lockIfCanOnclick();
            super.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onClick(View v) {
        long currentTime= System.currentTimeMillis();
        if((currentTime-lastOnclickTime>=INTERVAL_TIME)&&isIfCanOnclick()){
            lastOnclickTime=currentTime;
            onNoDoubleClick(v);
        }
    }
    public abstract void onNoDoubleClick(View v);

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /*判断我们的应用是否在白名单中*/
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
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


}
