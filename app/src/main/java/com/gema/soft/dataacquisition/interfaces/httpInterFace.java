package com.gema.soft.dataacquisition.interfaces;

import com.gema.soft.dataacquisition.models.FileModel;

import java.util.Map;

import okhttp3.Callback;

public interface httpInterFace {
    public final String TYPE_POST = "POST";
    public final String TYPE_GET = "GET";
    void asynch(String url, String type, Map<String,Object> params, Callback callback);
    void asynchFile(FileModel fileModel,Callback callback);
}
