package com.gema.soft.dataacquisition.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.gema.soft.dataacquisition.utils.FileUtil;

import java.io.Serializable;

public class FileModel implements Serializable {
    private String fileId;
    private String fileName;
    private String filePath;
    private String fileTime;
    private String fileRank;
    private String fileStatus;
    private String workId;
    private String userId;
    private String fileType;
    private String itemId;


    public FileModel() {
    }

    public FileModel(String name, String time) {
        this.fileId     =name;
        this.fileName   =name;
        this.fileRank   =FileUtil.FILE_RANK_LOWER;
        this.fileStatus =  FileUtil.FILE_STATUS_WAIT_UPLOADED;
        this.fileTime   =time;
        this.filePath   ="";
    }

    public FileModel(String name, String time,String filePath) {
        this.fileId     =name;
        this.fileName   =name;
        this.fileRank   =FileUtil.FILE_RANK_LOWER;
        this.fileStatus =  FileUtil.FILE_STATUS_WAIT_UPLOADED;
        this.fileTime   =time;
        this.filePath   =filePath;
    }

    public ContentValues getContentValues(FileModel fileModel){
        ContentValues contentValues=new ContentValues();
        contentValues.put("fileid", fileModel.getFileId());
        contentValues.put("filename", fileModel.getFileName());
        contentValues.put("filepath", fileModel.getFilePath());
        contentValues.put("filerank", fileModel.getFileRank());
        contentValues.put("filetime", fileModel.getFileTime());
        contentValues.put("filestatus", fileModel.getFileStatus());
        contentValues.put("workid"      ,fileModel.getWorkId());
        contentValues.put("userid"      ,fileModel.getUserId());
        contentValues.put("filetype"    ,fileModel.getFileType());
        contentValues.put("itemid"      ,fileModel.getItemId());
        return contentValues;
    }

    public FileModel(Cursor cursor){
        this.fileId     =cursor.getString(cursor.getColumnIndex("fileid"));
        this.fileName   =cursor.getString(cursor.getColumnIndex("filename"));
        this.fileRank   =cursor.getString(cursor.getColumnIndex("filerank"));
        this.fileStatus =cursor.getString(cursor.getColumnIndex("filestatus"));
        this.fileTime   =cursor.getString(cursor.getColumnIndex("filetime"));
        this.filePath   =cursor.getString(cursor.getColumnIndex("filepath"));
        this.workId             =cursor.getString(cursor.getColumnIndex("workid"  ));
        this.userId             =cursor.getString(cursor.getColumnIndex("userid"  ));
        this.fileType         =cursor.getString(cursor.getColumnIndex("filetype"));
        this.itemId           =cursor.getString(cursor.getColumnIndex("itemid"  ));
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileTime() {
        return fileTime;
    }

    public void setFileTime(String fileTime) {
        this.fileTime = fileTime;
    }

    public String getFileRank() {
        return fileRank;
    }

    public void setFileRank(String fileRank) {
        this.fileRank = fileRank;
    }

    public String getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(String fileStatus) {
        this.fileStatus = fileStatus;
    }

    public String getWorkId() {
        return workId;
    }

    public void setWorkId(String workId) {
        this.workId = workId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return "FileModel{" +
                "fileId='" + fileId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileTime='" + fileTime + '\'' +
                ", fileRank='" + fileRank + '\'' +
                ", fileStatus='" + fileStatus + '\'' +
                ", workId='" + workId + '\'' +
                ", userId='" + userId + '\'' +
                ", fileType='" + fileType + '\'' +
                ", itemId='" + itemId + '\'' +
                '}';
    }

}
