package com.gema.soft.dataacquisition.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


import com.facebook.drawee.backends.pipeline.Fresco;
import com.gema.soft.dataacquisition.activitys.LockScreenActivity;
import com.gema.soft.dataacquisition.activitys.VersionUpdateActivity;
import com.gema.soft.dataacquisition.services.WriteService;
import com.gema.soft.dataacquisition.services.WriteTempService;
import com.gema.soft.dataacquisition.sockets.ServiceSocketListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MyApplication extends Application {
    public static final String USER_NAME = "user";
    public static final String DATA_VERSION = "dataVersion";
    public static final String DATA_HTTP_IP = "dataIP";
    public static final String DATA_HTTP_WORK_INFO = "dataWorkInfo";
    public static final String DATA_BLUE_TOOLTH_INFO = "dataBluetoothInfo";
    public static final String DATA_IP = "ip";
    public static List<Activity> list = new ArrayList<>();
    public static MyApplication instance = null;
    public static ArrayList<String> gpsFileName = new ArrayList<>();
    private static DataUtil dataUtil;
    private static Intent intent;
    private static Intent intentTemp;
    public static String UUID= java.util.UUID.randomUUID().toString();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        dataUtil = DataUtil.getInstance();
        Fresco.initialize(this);
    }

    /**
     * 隐藏软键盘(可用于Activity)
     */
    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public List<Activity> getList() {
        return list;
    }

    public void setList(List<Activity> list) {
        this.list = list;
    }

    public static void addActivity(Activity activity) {
        list.add(activity);
    }

    public static void removeActivity(Activity activity) {
        list.remove(activity);
    }

    public static void finishAll() {
        for (Activity activity : list) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    public void begin() {
        if(intent==null){
            intent = new Intent(MyApplication.this, WriteService.class);
        }
        startService(intent);
    }

    /**
     * 返回当前程序版本号
     */
    public static int getAppVersionCode(Context context) {
        int versioncode = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versioncode = pi.versionCode;
        } catch (Exception e) {
        }
        DataUtil.versionCode=versioncode;
        return versioncode;
    }

    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = null;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (Exception e) {

        }
        DataUtil.versionName=versionName;
        return versionName;
    }

    public void realTime(Context context) {
        if (((!AppOrServiceStatusUtil.ServiceIsRunning(context, WriteService.class.getName())) || (!AppOrServiceStatusUtil.ServiceIsRunning(context, WriteTempService.class.getName())))) {
            if(intentTemp==null){
                intentTemp=new Intent(context,WriteTempService.class);
                startService(intentTemp);
            }
            if(intent==null){
                intent=new Intent(context, WriteService.class);
                startService(intent);
            }
        }
    }

    /**
     * 去版本更新界面
     * @param fileName 更新apk的文件名乘
     * @param context
     */
    public static void GoUpdate(String fileName, Context context) {
        Intent intent = new Intent(context, VersionUpdateActivity.class);
        if(TextUtils.isEmpty(fileName)){
            //todo 添加默认地址
            fileName="";
        }
        intent.putExtra("fileName",fileName);
        context.startActivity(intent);
    }

    public static void closeLockScreen(){
        if((!DataUtil.isUSBOnline||!DataUtil.isSocketOnline)&& LockScreenActivity.lockScreenActivity!=null){
            LockScreenActivity.lockScreenActivity.finish();
            LockScreenActivity.lockScreenActivity=null;
        }
    }

    public static void OpenLockScreen(Context context){
        if(DataUtil.isUSBOnline&&DataUtil.isSocketOnline&&LockScreenActivity.lockScreenActivity==null&&context!=null){
            Intent intent=new Intent(context,LockScreenActivity.class);
            context.startActivity(intent);
        }
    }
}
