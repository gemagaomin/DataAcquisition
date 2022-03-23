package com.gema.soft.dataacquisition.activitys;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gema.soft.dataacquisition.BuildConfig;
import com.gema.soft.dataacquisition.R;
import com.gema.soft.dataacquisition.receivers.DownloadReceiver;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.HttpUtil;
import com.gema.soft.dataacquisition.utils.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VersionUpdateActivity extends BaseActivity implements DownloadReceiver.Message {
    private static final String TAG = "VersionUpdateActivity";
    private Button updateButton;
    private HttpUtil okHttpUtil;
    private TextView textView;
    private ProgressBar process;
    private DownloadManager downloadManager;
    private DownloadReceiver receiver;
    private long downloadFileServiceId;
    private Timer timer;
    private String basePath;
    private String path = "/PWCZ/apk";
    private String zipPath = "/zip";
    private String fileName = "myUpdate.zip";//下载得到的文件名
    private String assetsFilePath = "/gblzapk";//解压后的文件路径
    private String apkFileName = "";
    //private String url = "http://218.206.94.235:18087/LMD/phoneakpuploadation/selectLastSoftVersion.do";
    private String url = "";
    private Handler handler = new Handler();
    private Cursor cursor;
    private SharedPreferences sharedPreferences;
    private Button button1;
    private Button cancelButton;
    private Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        basePath = Environment.getExternalStoragePublicDirectory(path).getAbsolutePath();
        setContentView(R.layout.activity_version_update_layout);
        requestPermission(VersionUpdateActivity.this);
        updateButton = (Button) findViewById(R.id.button);
        cancelButton = (Button) findViewById(R.id.cancel);
        HttpUtil httpUtil = HttpUtil.getInstance();
        Intent intent = getIntent();
        String fileName = intent.getStringExtra("fileName");
        url = fileName;
        closeButton = (Button) findViewById(R.id.close);
        cancelButton.setOnClickListener(this);
        updateButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        if (okHttpUtil == null) {
            okHttpUtil = HttpUtil.getInstance();
        }
        textView = (TextView) findViewById(R.id.textView);
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        process = (ProgressBar) findViewById(R.id.progressBar);
        button1 = (Button) findViewById(R.id.button2);
        button1.setOnClickListener(this);
        textView.setText("等待下载");
        MyApplication.addActivity(this);
    }

    @Override
    public void onNoDoubleClick(View v) {
        int num = v.getId();
        if (num == R.id.button) {
            Long id = Long.getLong(getValueByKey("id"));
            if (id != null && id > 0) {
                downloadFileServiceId = id;
            }
            if (downloadFileServiceId > 0l) {
                cursor = getCursor();
                if (cursor != null) {
                    changeShowView();
                } else {
                    downloadManager.remove(downloadFileServiceId);
                    DeleteFile(basePath + zipPath + fileName);
                    downloadFileServiceId = 0l;
                    setKey("id", "");
                }
            }
            if (process != null)
                process.setProgress(0);
            deleteFileNotSelf(basePath + zipPath);
            if(!DataUtil.IS_UPDATE_ZIP){
                zipPath=assetsFilePath;
                if(!TextUtils.isEmpty(url)){
                    String[] fileNameArr=url.split(File.separator);
                    if(fileNameArr.length>0){
                        fileName=fileNameArr[fileNameArr.length-1];
                        apkFileName=fileName;
                    }
                }
            }
            downloadFileServiceId = okHttpUtil.downFileHttp(url, path + zipPath, fileName, downloadManager);
            setKey("id", Long.toString(downloadFileServiceId));
            setKey("deletePath", basePath + zipPath + fileName);
            receiver.setMyId(downloadFileServiceId);

        } else if (num == R.id.button2) {
            try {
                Boolean unZip = UnzipFile(basePath + zipPath + File.separator + fileName, basePath + assetsFilePath + File.separator);
                if (unZip) {
                    Toast.makeText(VersionUpdateActivity.this, "升级成功", Toast.LENGTH_LONG);
                } else {
                    Toast.makeText(VersionUpdateActivity.this, "升级失败", Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                Log.d(TAG, "onClick: ", e);
            }
        } else if (num == R.id.cancel) {
            if (downloadManager != null && downloadFileServiceId > 0) {
                downloadManager.remove(downloadFileServiceId);
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateButton.setVisibility(View.GONE);
                    updateButton.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);
                }
            });
        } else if (num == R.id.close) {
            finish();
        }
    }

    public void findDownloadWord() {
        String idS = getValueByKey("id");
        Long id = TextUtils.isEmpty(idS) ? 0 : Long.parseLong(idS);
        if (id > 0) {
            downloadFileServiceId = id;
            changeShowView();
        }
    }

    /**
     * 下载进度线程
     *
     * @return
     */
    public void changeShowView() {
        Cursor cursor = getCursor();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                final int num = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                switch (num) {
                    case DownloadManager.STATUS_RUNNING:
                        final int num1 = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        final int num2 = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        final DecimalFormat decimalFormat = new DecimalFormat("0.00");
                        final String total = num1 > 0 ? decimalFormat.format((double) num1 / (1024 * 1024)) : "0";
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateButton.setVisibility(View.GONE);
                                cancelButton.setVisibility(View.GONE);
                                closeButton.setVisibility(View.GONE);
                                String show = num2 > 0 ? (decimalFormat.format((num2 / (double) num1) * 100)) + "%  " : "0.00%  ";
                                textView.setText("加载：" + show + " /大小为：" + total + "MB");
                                process.setMax(num1);
                                process.setProgress(num2);
                            }
                        });
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        if (timer != null)
                            timer.cancel();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                process.setProgress(process.getMax());
                                boolean issuccess=true;
                                if(DataUtil.IS_UPDATE_ZIP){
                                    textView.setText("文件解压中");
                                    issuccess=UnzipFile(basePath + zipPath + File.separator + fileName, basePath + assetsFilePath + File.separator);
                                    textView.setText("解压成功");
                                }else{
                                    issuccess=!TextUtils.isEmpty(apkFileName);
                                }
                                if(issuccess){
                                    setKey("id", "");
                                    InstallApp();
                                } else {
                                    setKey("id", "");
                                    textView.setText("升级失败");
                                    updateButton.setVisibility(View.VISIBLE);
                                    cancelButton.setVisibility(View.GONE);
                                    closeButton.setVisibility(View.GONE);
                                }
                            }
                        });
                        break;
                    case DownloadManager.STATUS_FAILED:
                        setKey("id", "");
                        downloadFileServiceId = 0l;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateButton.setVisibility(View.VISIBLE);
                                textView.setText("下载失败，请重新加载" + num);
                            }
                        });
                        break;
                    case DownloadManager.STATUS_PENDING:
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateButton.setVisibility(View.GONE);
                                cancelButton.setVisibility(View.VISIBLE);
                                closeButton.setVisibility(View.GONE);
                                textView.setText("下载等待开始" + num);
                            }
                        });
                    default:
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateButton.setVisibility(View.GONE);
                                cancelButton.setVisibility(View.VISIBLE);
                                textView.setText("下载等待中" + num);
                            }
                        });
                        break;

                }
            }
            cursor.close();
        }
    }

    /**
     * 下载完成文件下载管理器接收器回调方法
     *
     * @param s
     */
    @Override
    public void getMessage(String s) {
        if (!TextUtils.isEmpty(s)) {
            Toast.makeText(VersionUpdateActivity.this, "完成", Toast.LENGTH_LONG).show();
            changeShowView();
        }
    }

    /**
     * 删除文件
     *
     * @param path 文件路径
     * @return
     */
    public Boolean DeleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        file.delete();
        return true;
    }

    /**
     * 解压文件
     *
     * @param path     压缩文件目录
     * @param filePath 解压后文件存放目录
     * @return
     */
    public Boolean UnzipFile(String path, String filePath) {
        File fileZip = new File(path);
        FileOutputStream fileOutputStream = null;
        ZipInputStream inputStream = null;
        String webName = "";
        if (!fileZip.exists()) {
            return false;
        }
        try {
            inputStream = new ZipInputStream(new FileInputStream(fileZip));
            ZipEntry zipEntry = null;
            deleteFileNotSelf(basePath + assetsFilePath + File.separator);
            while ((zipEntry = inputStream.getNextEntry()) != null) {
                String filePathTemp = filePath;
                if (zipEntry.isDirectory()) {
                    File file = new File(filePath);
                    if (!file.exists())
                        file.mkdirs();
                } else {
                    String Name = zipEntry.getName();
                    if (!TextUtils.isEmpty(Name)) {
                        apkFileName = Name;
                    }
                    filePathTemp = filePathTemp + zipEntry.getName();
                    File file = new File(filePathTemp);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    fileOutputStream = new FileOutputStream(file);
                    int len = 0;
                    byte[] bytes = new byte[1024];
                    while ((len = inputStream.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, len);
                        fileOutputStream.flush();
                    }
                    fileOutputStream.close();
                }
            }
            inputStream.close();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                return false;
            }

        }
        return true;

    }

    /**
     * 安装APK方法
     *
     * @return
     */
    public Boolean InstallApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        File file = new File(basePath + assetsFilePath + File.separator + apkFileName);
        if (!file.exists()) {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_LONG).show();
            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android N 写法
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                Uri apkUri = FileProvider.getUriForFile(VersionUpdateActivity.this, BuildConfig.APPLICATION_ID + ".fileProvider", file);//在AndroidManifest中的android:authorities值
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            }
            setKey("id", "");
            downloadFileServiceId = 01;
            startActivity(intent);
        }
        finish();
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        receiver = new DownloadReceiver(VersionUpdateActivity.this);
        receiver.setMessage(this);
        String id = getValueByKey("id");
        if (!TextUtils.isEmpty(id)) {
            receiver.setMyId(Long.parseLong(id));
        }
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        startTimer();
        findDownloadWord();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null)
            timer.cancel();
        unregisterReceiver(receiver);

    }

    /**
     * 开启权限
     *
     * @param context
     */
    private void requestPermission(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, "android.permission.DOWNLOAD_WITHOUT_NOTIFICATION")
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE, "android.permission.DOWNLOAD_WITHOUT_NOTIFICATION"}, 0);

            }
        }
    }

    /**
     * 定时器
     */
    private void startTimer() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                changeShowView();
            }
        };
        timer.schedule(task, 0, 10);
    }

    private void setKey(String key, String value) {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences("load", Activity.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private String getValueByKey(String key) {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences("load", Activity.MODE_PRIVATE);
        }
        String ret = sharedPreferences.getString(key, "");
        return ret;
    }

    private Cursor getCursor() {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadFileServiceId);
        Cursor cursor = downloadManager.query(query);
        return cursor;
    }

    public void deleteFileNotSelf(String path) {
        File file = new File(path);
        if (file == null || !file.exists() || !file.isDirectory())
            return;
        for (File fileItem : file.listFiles()) {
            if (fileItem.isFile())
                fileItem.delete(); // 删除所有文件
            else if (fileItem.isDirectory())
                deleteDirWithFile(fileItem); // 递规的方式删除文件夹
        }
    }

    public static void deleteDirWithFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWithFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }
    @Override
    public void onBackPressed() {
        Toast.makeText(this,"为了正常使用，请更新软件！",Toast.LENGTH_LONG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.removeActivity(this);
    }
}
