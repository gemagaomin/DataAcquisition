package com.gema.soft.dataacquisition.models;


import android.text.TextUtils;


import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.Map;


public class TrainInfoModel implements Serializable {
    private String driverId;
    private String driverName;
    private String trainTypeId;
    private String trainOrder;
    private String trainId;
    private String assistantDriverName;
    private String assistantDriverId;
    private String trainTypeName;
    //todo 后期添加检查人员信息2021-3-31
    private String examinerId;
    private String examinerName;


    public TrainInfoModel() {
    }

    public TrainInfoModel(JSONObject jsonObject) {
        this.trainId=jsonObject.getString("trainid");
        this.trainTypeId=jsonObject.getString("traintypeid");
        this.trainTypeName=jsonObject.getString("traintypename");
        this.trainOrder=jsonObject.getString("trainorder");
        this.assistantDriverName=jsonObject.getString("fsjm");
        this.assistantDriverId=jsonObject.getString("vidriverid");
        this.driverId=jsonObject.getString("driverid");
        this.driverName=jsonObject.getString("sjm");
        this.examinerId="";
        this.examinerName="";
    }


    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getTrainTypeId() {
        return trainTypeId;
    }

    public void setTrainTypeId(String trainTypeId) {
        this.trainTypeId = trainTypeId;
    }

    public String getTrainOrder() {
        return trainOrder;
    }

    public void setTrainOrder(String trainOrder) {
        this.trainOrder = trainOrder;
    }

    public String getAssistantDriverName() {
        return assistantDriverName;
    }

    public void setAssistantDriverName(String assistantDriverName) {
        this.assistantDriverName = assistantDriverName;
    }

    public String getAssistantDriverId() {
        return assistantDriverId;
    }

    public void setAssistantDriverId(String assistantDriverId) {
        this.assistantDriverId = assistantDriverId;
    }

    public String getTrainId() {
        return trainId;
    }

    public void setTrainId(String trainId) {
        this.trainId = trainId;
    }

    public String getTrainTypeName() {
        return trainTypeName;
    }

    public void setTrainTypeName(String trainTypeName) {
        this.trainTypeName = trainTypeName;
    }

    public String trainTypeIdName(Map<String,TrainTypeModel> trainTypeModelMap){
        StringBuffer str=new StringBuffer();
        if(TextUtils.isEmpty(trainId))
            return "";
        TrainTypeModel trainTypeModel=trainTypeModelMap.get(trainTypeId);
        if(trainTypeModel!=null){
            if(trainTypeModel==null|| TextUtils.isEmpty(trainTypeModel.getTrainTypeName())){
                str.append(trainTypeId);
            }else{
                str.append(trainTypeModel.getTrainTypeName());
            }
            str.append("-").append(trainId);
        }
        return str.toString();
    }

    public String trainTypeIdName(){
        StringBuffer str=new StringBuffer();
        if(TextUtils.isEmpty(trainId))
            return "";
        if(TextUtils.isEmpty(this.trainTypeId)||TextUtils.isEmpty(this.trainTypeName)){
            str.append(trainTypeId);
        }else{
            str.append(this.trainTypeName);
        }
        str.append("-").append(trainId);
        return str.toString();
    }

    @Override
    public String toString() {
        return "TrainInfoModel{" +
                "driverId='" + driverId + '\'' +
                ", driverName='" + driverName + '\'' +
                ", trainTypeId='" + trainTypeId + '\'' +
                ", trainOrder='" + trainOrder + '\'' +
                ", trainId='" + trainId + '\'' +
                ", assistantDriverName='" + assistantDriverName + '\'' +
                ", assistantDriverId='" + assistantDriverId + '\'' +
                ", trainTypeName='" + trainTypeName + '\'' +
                ", examinerId='" + examinerId + '\'' +
                ", examinerName='" + examinerName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null){
            return false;
        }
        if(this==obj){
            return true;
        }
        if(obj instanceof TrainInfoModel){
            TrainInfoModel trainInfoModel=(TrainInfoModel)obj;
            if(this.trainTypeId.equals(trainInfoModel.getTrainTypeId())
                    &&this.trainOrder.equals(trainInfoModel.getTrainOrder())
                    &&this.trainId.equals(trainInfoModel.getTrainId())
                    &&this.driverId.equals(trainInfoModel.getDriverId())
                    &&this.assistantDriverId.equals(trainInfoModel.getAssistantDriverId())){
                return true;
            }else{
                return false;
            }
        }
        return false;
    }
}
