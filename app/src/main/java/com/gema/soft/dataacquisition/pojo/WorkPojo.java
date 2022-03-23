package com.gema.soft.dataacquisition.pojo;


import com.gema.soft.dataacquisition.models.TrainInfoModel;
import com.gema.soft.dataacquisition.models.WorkModel;
import java.io.Serializable;
import java.util.Objects;

public class WorkPojo implements Serializable {
    private String workId;
    private String blueToothConStatus;
    private String workStatus;
    private String startTime;
    private String endTime;
    private TrainInfoModel trainInfo;
    private String fileName;
    private String showTime;
    private String bluetoothMac;


    public WorkPojo() {
            this.trainInfo=new TrainInfoModel();
    }

    public WorkPojo(WorkModel workModel) {
        this.workId            =workModel.getWorkId();
        this.blueToothConStatus =workModel.getBlueToothConStatus();
        this.workStatus        =workModel.getWorkStatus();
        this.startTime         =workModel.getStartTime();
        this.endTime           =workModel.getEndTime();
        this.fileName          =workModel.getFileName();
        this.trainInfo         =workModel.getTrainInfo();
        this.bluetoothMac       =workModel.getBlueToothMas();
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


    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkPojo workPojo = (WorkPojo) o;
        return workId.equals(workPojo.workId);
    }

    public String getBluetoothMac() {
        return bluetoothMac;
    }

    public void setBluetoothMac(String bluetoothMac) {
        this.bluetoothMac = bluetoothMac;
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId);
    }

}
