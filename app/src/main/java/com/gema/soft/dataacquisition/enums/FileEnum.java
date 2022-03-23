package com.gema.soft.dataacquisition.enums;

import com.gema.soft.dataacquisition.utils.FileUtil;

public enum FileEnum {
    WORK_FILE_PATH(FileUtil.WORK_FILE_PATH,"/workfile"),
    LOG_FILE_PATH(FileUtil.LOG_FILE_PATH,"/log"),
    DB_FILE_PATH(FileUtil.DB_FILE_PATH,"/db/"),
    WORK_FILE_INFO_PATH(FileUtil.WORK_FILE_INFO_PATH,"/workdata"),
    INDEX_FILE_PATH(FileUtil.INDEX_FILE_PATH,"/indexfile"),
    APK_VERSION_FILE_PATH(FileUtil.APK_VERSION_FILE_PATH,"/apkfile");
    private final int type;
    private final String filePath;
    FileEnum(final int type,final String filePath) {
        this.type=type;
        this.filePath=filePath;
    }

    public int getType() {
        return type;
    }

    public String getFilePath() {
        FileEnum[] fileEnums=values();
        for (FileEnum f:fileEnums
             ) {
            if(f.type==this.type){
                return f.filePath;
            }
        }
        return null;
    }
}
