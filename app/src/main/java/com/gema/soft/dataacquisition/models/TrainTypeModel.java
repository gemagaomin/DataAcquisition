package com.gema.soft.dataacquisition.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class TrainTypeModel {
    private String trainTypeId;
    private String trainTypeName;
    private String traintype;//车型中文
    private String maxLunJing;//车轮最大轮径
    private String minLunJing;//车轮最小轮径
    private int trainnum;// 单双节车  双节车=2
    private double trainweight;//车重
    private String type;//类型
    private final String TAG="TrainTypeModel";

    public TrainTypeModel(String trainTypeId, String trainTypeName) {
        this.trainTypeId = trainTypeId;
        this.trainTypeName = trainTypeName;
    }

    public String getTraintype() {
        return traintype;
    }

    public void setTraintype(String traintype) {
        this.traintype = traintype;
    }

    public String getMaxLunJing() {
        return maxLunJing;
    }

    public void setMaxLunJing(String maxLunJing) {
        this.maxLunJing = maxLunJing;
    }

    public String getMinLunJing() {
        return minLunJing;
    }

    public void setMinLunJing(String minLunJing) {
        this.minLunJing = minLunJing;
    }

    public int getTrainnum() {
        return trainnum;
    }

    public void setTrainnum(int trainnum) {
        this.trainnum = trainnum;
    }

    public double getTrainweight() {
        return trainweight;
    }

    public void setTrainweight(double trainweight) {
        this.trainweight = trainweight;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TrainTypeModel() {
    }
    public TrainTypeModel(JSONObject jsonObject) {
        try{
            this.trainTypeId=jsonObject.getString("trainTypeId");
            this.trainTypeName=jsonObject.getString("trainTypeName");
        }catch (JSONException e){
            Log.d(TAG, "TrainTypeModel: ");
        }
    }

    public TrainTypeModel(Cursor cursor) {
        this.trainTypeId=cursor.getString(cursor.getColumnIndex("traintypeid"));
        this.trainTypeName=cursor.getString(cursor.getColumnIndex("traintypename"));
    }

    public ContentValues getContentValues(TrainTypeModel trainTypeModel){
        ContentValues contentValues=new ContentValues();
        contentValues.put("traintypeid",trainTypeModel.getTrainTypeId());
        contentValues.put("traintypename",trainTypeModel.getTrainTypeName());
        return contentValues;
    }

    public String getTrainTypeId() {
        return trainTypeId;
    }

    public void setTrainTypeId(String trainTypeId) {
        this.trainTypeId = trainTypeId;
    }

    public String getTrainTypeName() {
        return trainTypeName;
    }

    public void setTrainTypeName(String trainTypeName) {
        this.trainTypeName = trainTypeName;
    }

    @Override
    public String toString() {
        return  trainTypeName ;
    }
}
