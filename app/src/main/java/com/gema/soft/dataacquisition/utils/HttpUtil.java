package com.gema.soft.dataacquisition.utils;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.gema.soft.dataacquisition.interfaces.httpInterFace;
import com.gema.soft.dataacquisition.models.FileModel;
import com.gema.soft.dataacquisition.models.WorkModel;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil implements httpInterFace {
    public static String BASE_PATH_OTHER = "http://192.168.137.1:/jzgk";
    public static final String BASE_PATH_SERVER = "http://218.206.94.241:18989/perfect_jzgk";//"http://192.168.137.1:/jzgk";//"http://192.168.137.1:8088/sjz_jzgk_Web_exploded";
    public static final String BASE_PATH_MYSELF = "http://192.168.137.1:8080//testWeb_Web_exploded/";
    public static String BASE_PATH = "http://218.206.94.242:18188/LMD/trainmap/searchByTrainorder.do";
    private OkHttpClient okHttpClient;
    private static HttpUtil httpUtil;
    private DBUtil dbUtil;
    private HttpUtil() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.readTimeout(30000, TimeUnit.MILLISECONDS);
            builder.writeTimeout(30000, TimeUnit.MILLISECONDS);
            builder.connectTimeout(10000, TimeUnit.MILLISECONDS);
            okHttpClient = builder.build();
        }
    }

    public static String getBasePath() {
        return BASE_PATH;
    }

    public static HttpUtil getInstance() {
        if (httpUtil == null) {
            synchronized (HttpUtil.class) {
                if (httpUtil == null) {
                    httpUtil = new HttpUtil();
                }
            }
        }
        return httpUtil;
    }

    /**
     * @param url
     * @param type
     * @param params
     * @return map.put(" error ", error);//过程中是否有错；-1：网络错误，500：服务器错误；
     * map.put("result", result);//解密后服务器返回的数据
     */
    public Map<String, String> synch(String url, String type, Map<String, Object> params) {
        Map map = new HashMap();
        String error = "0";
        String result = "";
        Response response = null;
        if (TextUtils.isEmpty(type)) {
            type = TYPE_GET;
        }
        try {

            if (TYPE_GET.equals(type)) {
                StringBuffer strB = new StringBuffer();
                String urlAndParams = "?";
                if (params != null && params.size() > 0) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        strB.append(entry.getKey()).append("=").append((String) entry.getValue()).append("&");
                    }
                }
                urlAndParams += strB.toString();
                Request request = new Request.Builder().url(BASE_PATH + url + urlAndParams).build();
                response = okHttpClient.newCall(request).execute();
            } else if (TYPE_POST.equals(type)) {
                map.put("data", (String) params.get("data"));
                RequestBody  body = FormBody.create(MediaType.parse("application/json;charset=utf-8"), JSONObject.toJSONString(map));
                Request request = new Request.Builder().url(BASE_PATH + url).addHeader("header", "").post(body).build();
                response = okHttpClient.newCall(request).execute();
            }
            if (response == null) {
                error = "-1";
            } else {
                if (response.isSuccessful()) {
                    result = response.body().string();
                }
                if (response.code() == 500) {
                    error = "500";
                }
            }
        } catch (IOException e) {
            MyException myException = new MyException();
            myException.buildException(e);
            error = "-1";
            result = e.toString();
        } catch (Exception e) {
            MyException myException = new MyException();
            myException.buildException(e);
            error = "-1";
            result = e.toString();
        }
        map.put("error", error);//过程中是否有错；-1：网络错误，500：服务器错误；
        map.put("result", result);//解码后服务器返回的数据
        return map;
    }

    public void asynch(String url, String type, Map<String, Object> params, Callback callback) {
        if (TextUtils.isEmpty(type)) {
            type = TYPE_GET;
        }
        try {
            Map map = new HashMap();
            if (TYPE_GET.equals(type)) {
                StringBuffer strB = new StringBuffer();
                String urlAndParams = "?";
                if (params != null && params.size() > 0) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        strB.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                    }
                }
                urlAndParams += strB.toString();
                Request request = new Request.Builder().url( url + urlAndParams).build();
                okHttpClient.newCall(request).enqueue(callback);
            } else if (TYPE_POST.equals(type)) {
                map.put("data", params.get("data"));
                MediaType mediaType=MediaType.Companion.parse("application/json;charset=utf-8");
                RequestBody body = RequestBody.Companion.create( JSONObject.toJSONString(map),mediaType);
                Request request = new Request.Builder().url( url).addHeader("header", "").post(body).build();
                okHttpClient.newCall(request).enqueue(callback);
            }
        } catch (Exception e) {
            MyException myException = new MyException();
            myException.buildException(e);
        }

    }


    public boolean synchStartOrEndRealTime(List locationModelList, int realTimeStatus) {
        String url = "/map/savework";

        Map<String, Object> parasMap = new HashMap<>();
        parasMap.put("data", locationModelList);
        Map map = new HashMap();
        map.put("header", getHearModel());
        boolean result = false;
        Response response;
        try {
            String data = JSONObject.toJSONString(parasMap);
            System.out.println("data = " + data);
            map.put("data", data);
            RequestBody  body = FormBody.create(MediaType.parse("application/json;charset=utf-8"), JSONObject.toJSONString(map));
            Request request = new Request.Builder().url(BASE_PATH + url).post(body).build();
            response = okHttpClient.newCall(request).execute();
            if (response != null) {
                if (response.isSuccessful()) {
                    String errorCode = JSONObject.parseObject(response.body().string()).getString("errorCode");
                    if ("0".equals(errorCode)) {
                        if (realTimeStatus == DataUtil.REAL_TIME_START) {
                            if (dbUtil == null) {
                                dbUtil = DBUtil.getInstance();
                                for (Object m : locationModelList) {
                                    ContentValues val = new ContentValues();
                                    val.put("issendoutsuccess", 0);
                                    WorkModel workModel = (WorkModel) m;
                                    dbUtil.update(DataUtil.TableNameEnum.WORK.toString(), val, " workid=? ", new String[]{workModel.getWorkId()});
                                }
                            }
                        }
                        result = true;
                    }
                }
                if (response.code() == 500) {
                    MyException myException = new MyException();
                    myException.buildException(MyException.SERVER_MESSAGE);
                }
            }
        } catch (Exception e) {
            MyException myException = new MyException();
            myException.buildException(e);
        } finally {
            return result;
        }
    }


    public void asynchFile(FileModel fileModel, Callback callback) {
        String filePath = fileModel.getFilePath();
        String fileName = fileModel.getFileName();
        File file = new File(filePath);
        if (file.exists()) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName, fileBody)
                    .build();
            try {
                Request request = new Request.Builder().url(DataUtil.getInstance().FILE_PATH ).addHeader("header", "").post(requestBody).build();
                okHttpClient.newCall(request).enqueue(callback);

            } catch (Exception e) {
                MyException myException = new MyException();
                myException.buildException(e);
            }
        }
    }

    public long downFileHttp(String url, String dirpath, String filePath, DownloadManager downloadManager) {
        Uri uri=Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        //request.setNotificationVisibility(request.VISIBILITY_HIDDEN);
        request.setDestinationInExternalPublicDir(dirpath, filePath);
        long id = downloadManager.enqueue(request);
        return id;
    }

    public String getHearModel() {
        String header = "";
        try {
        } catch (Exception e) {
            MyException myException = new MyException();
            myException.buildException(e);
        }
        return header;
    }

}
