package com.gema.soft.dataacquisition.models;

import android.content.ContentValues;
import android.database.Cursor;
import com.alibaba.fastjson.JSONObject;
import com.gema.soft.dataacquisition.pojo.WorkPojo;
import com.gema.soft.dataacquisition.utils.MyException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkModel implements Serializable {
    private String workId;
    private String workStatus;
    private String startTime;
    private String endTime;
    private String fileName="";
    private TrainInfoModel trainInfo;
    private String blueToothConStatus;
    private String blueToothMas;
    public WorkModel() {
    }


    public WorkModel(Cursor cursor) {
        this.workId=cursor.getString(cursor.getColumnIndex("workid"));
        this.blueToothConStatus =cursor.getString(cursor.getColumnIndex("bluetoothconstatus"));
        this.workStatus = cursor.getString(cursor.getColumnIndex("workstatus"));
        this.startTime=cursor.getString(cursor.getColumnIndex("starttime"));
        this.endTime=cursor.getString(cursor.getColumnIndex("endtime"));
        this.fileName=cursor.getString(cursor.getColumnIndex("fileName"));
        this.trainInfo= JSONObject.parseObject(cursor.getString(cursor.getColumnIndex("traininfo")),TrainInfoModel.class);
        this.blueToothMas=cursor.getString(cursor.getColumnIndex("bluetoothmas"));
    }

    public ContentValues getContentValues(WorkModel workModel){
        ContentValues contentValues=new ContentValues();
        contentValues.put("workid",workModel.getWorkId());
        contentValues.put("bluetoothconstatus",workModel.getBlueToothConStatus());
        contentValues.put("workstatus",workModel.getWorkStatus());
        contentValues.put("starttime",workModel.getStartTime());
        contentValues.put("endtime",workModel.getEndTime());
        contentValues.put("fileName",workModel.getFileName());
        contentValues.put("traininfo",JSONObject.toJSONString(workModel.getTrainInfo()));
        contentValues.put("bluetoothmas",workModel.getBlueToothMas());
        return contentValues;
    }


    public String getWorkId() {
        return workId;
    }

    public void setWorkId(String workId) {
        this.workId = workId;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


    public TrainInfoModel getTrainInfo() {
        return trainInfo;
    }

    public void setTrainInfo(TrainInfoModel trainInfo) {
        this.trainInfo = trainInfo;
    }

    public String getBlueToothConStatus() {
        return blueToothConStatus;
    }

    public void setBlueToothConStatus(String blueToothConStatus) {
        this.blueToothConStatus = blueToothConStatus;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }



    public WorkModel(WorkPojo workPojo) {
        try{
            this.workId            =workPojo.getWorkId();
            this.workStatus        =workPojo.getWorkStatus();
            this.startTime         =workPojo.getStartTime();
            this.endTime           =workPojo.getEndTime();
            this.fileName=workPojo.getFileName();
            this.trainInfo         =workPojo.getTrainInfo();
            this.blueToothConStatus=workPojo.getBlueToothConStatus();
            this.blueToothMas=workPojo.getBluetoothMac();
        }catch (Exception e){
            MyException myException=new MyException();
            myException.buildException(e);
        }
    }


    public List<WorkModel> getList( List<WorkPojo> list){
        List<WorkModel> locationModels=new ArrayList<>();
        for (WorkPojo work:list) {
            locationModels.add(new WorkModel(work));
        }
        return locationModels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkModel workModel = (WorkModel) o;
        return Objects.equals(workId, workModel.workId) &&
                Objects.equals(workStatus, workModel.workStatus) &&
                Objects.equals(startTime, workModel.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId,  workStatus, startTime);
    }

    public String getBlueToothMas() {
        return blueToothMas;
    }

    public void setBlueToothMas(String blueToothMas) {
        this.blueToothMas = blueToothMas;
    }
}
