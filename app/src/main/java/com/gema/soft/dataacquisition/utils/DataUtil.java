package com.gema.soft.dataacquisition.utils;

import android.Manifest;

import com.gema.soft.dataacquisition.enums.FileEnum;
import com.gema.soft.dataacquisition.models.FileDataByteModel;
import com.gema.soft.dataacquisition.models.FileModel;
import com.gema.soft.dataacquisition.models.PersonModel;
import com.gema.soft.dataacquisition.models.TrainTypeModel;
import com.gema.soft.dataacquisition.models.UserModel;
import com.gema.soft.dataacquisition.pojo.WorkPojo;
import com.gema.soft.dataacquisition.task.MsgKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataUtil {
    private static DataUtil dataUtil;
    public static final boolean wcy = false;//true数据不加密；false;数据加密
    public static final String ERROR_TIPS_INTNET_DATA = "errorData";
    public static String httpKey = "qVahl/VD1Ay8OeY4TigYnw==";
    public static final int REAL_TIME_START = 0, REAL_TIME_RUNNING = 1, REAL_TIME_END = 2;
    public final String WORK_STATUS_RUNNING="0",WORK_STATUS_WAIT_FILE_UP="1",WORK_STATUS_FINISH="2",WORK_STATUS_FILE_UP_ERROR="3";
    public static final int TASK_TYPE_WRITHE=0,TASK_TYPE_READ=1,TASK_TYPE_GET_DATA_FROM_BLUETOOTH=2,TASK_TYPE_CHECK_BLUETOOTH_STATUS=3;
    public String DB_PATH = "";
    public WorkPojo runWorkPojo=null;
    public String runFilePath=null;
    public final int  APP_HAS_NET=0,APP_NOT_HAS_NET=1;
    public int AppHasNet=APP_NOT_HAS_NET;//0有网；1脱网
    public final int fz=50;
    public float readIndexTime=0;
    public boolean isStart=false;
    public long startTime=0;
    public long endTime=0;
    public int number=0;
    public String FILE_PATH="http://218.206.94.241:28181/smooth/pwcz/getUploadFile";//"http://192.168.137.1:8081/smooth_new_Web_exploded/pwcz/getUploadFile";//"http://218.206.94.241:28181/smooth/pwcz/getUploadFile";//"http://192.168.137.1:8081/smooth/pwcz/getUploadFile";//
    public static float NOT_SHOW=0xFF;
    public static String FILE_APP_PATH;
    public int LongMaxNumber=250;
    public int shortMaxNumber=50;
    public int oneIndex=0;
    public MsgKey writheMsgKey;
    public static int versionCode;
    public static String versionName;
    public static List<String> hzList;
    public static byte[] hzBtList=new byte[8];
    public static Map<String, String> VersionMap = new HashMap<>();
    public static final boolean IS_UPDATE_ZIP=false;//更新的文件是否是压缩文件
    public String[] REQUESTED_PERMISSIONS ={
            Manifest.permission.BLUETOOTH ,
            Manifest.permission.BLUETOOTH_ADMIN
            ,Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        };
    public String lineStatus="";
    public String blueMac="";
    public List<FileDataByteModel> fileDataByteModels;//存放原始数据
    public FileDataByteModel newFileDataByteModel;//存放原始数据
    public FileDataByteModel fileDataByteModelForOne;//存放1s更新一次的加速度数据
    public List<FileDataByteModel> fileDataByteModelForTen;//存放10s更新一次的加速度数据
    public List<FileDataByteModel> fileDataByteModelsForJDD;
    //public Map<Long,FileDataByteModel> fileDataByteModelMap;
    public UserModel userModel;
    public boolean isWrited=false;
    private Map<String, FileModel> fileModelMap;
    private List<TrainTypeModel> trainTypeModels;
    private Map<String,TrainTypeModel> trainTypeModelMap;
    private List<PersonModel> personModels;
    private Map<String ,PersonModel> personModelMap;
    public static boolean isTest=false;
    public static boolean isUSBOnline=false;
    public static boolean isSocketOnline=false;
    public static Map<Integer,String> smoothLevelMap;
    private  DataUtil() {
        smoothLevelMap=new HashMap<>();
        smoothLevelMap.put(1,"Ⅰ");
        smoothLevelMap.put(2,"Ⅱ");
        smoothLevelMap.put(3,"Ⅲ");
        smoothLevelMap.put(4,"Ⅳ");
        smoothLevelMap.put(5,"Ⅴ");
        fileDataByteModels=new ArrayList<>();
        fileDataByteModelsForJDD=new ArrayList<>();
        fileDataByteModelForTen=new ArrayList<>();
        DB_PATH=FileUtil.getInstance().makeDir(FileUtil.getInstance().getFilePath(FileEnum.DB_FILE_PATH));
        trainTypeModels=new ArrayList<>();
        trainTypeModels.add(new TrainTypeModel("239","HXD3C"));
        trainTypeModels.add(new TrainTypeModel("101","DF1"));
        trainTypeModels.add(new TrainTypeModel("102","DF2"));
        trainTypeModels.add(new TrainTypeModel("103","DF3"));
        trainTypeModels.add(new TrainTypeModel("104","DF4"));
        trainTypeModels.add(new TrainTypeModel("105","DF4K"));
        trainTypeModels.add(new TrainTypeModel("106","DF4C"));
        trainTypeModels.add(new TrainTypeModel("107","DF5"));
        trainTypeModels.add(new TrainTypeModel("108","DF5(KUAN)"));
        trainTypeModels.add(new TrainTypeModel("109","DF6"));
        trainTypeModels.add(new TrainTypeModel("110","DF7"));
        trainTypeModels.add(new TrainTypeModel("111","DF8"));
        trainTypeModels.add(new TrainTypeModel("112","DF9"));
        trainTypeModels.add(new TrainTypeModel("235","HXD2B"));
        trainTypeModels.add(new TrainTypeModel("114","DFH1"));
        trainTypeModels.add(new TrainTypeModel("115","DFH2"));
        trainTypeModels.add(new TrainTypeModel("116","DFH3"));
        trainTypeModels.add(new TrainTypeModel("117","DFH5"));
        trainTypeModels.add(new TrainTypeModel("118","BJ"));
        trainTypeModels.add(new TrainTypeModel("119","BJ(KUAN)"));
        trainTypeModels.add(new TrainTypeModel("120","ND2"));
        trainTypeModels.add(new TrainTypeModel("121","ND3"));
        trainTypeModels.add(new TrainTypeModel("122","ND4"));
        trainTypeModels.add(new TrainTypeModel("123","ND5"));
        trainTypeModels.add(new TrainTypeModel("124","NY5"));
        trainTypeModels.add(new TrainTypeModel("125","NY6"));
        trainTypeModels.add(new TrainTypeModel("126","NY7"));
        trainTypeModels.add(new TrainTypeModel("127","QY"));
        trainTypeModels.add(new TrainTypeModel("128","DFH21"));
        trainTypeModels.add(new TrainTypeModel("129","DF7B"));
        trainTypeModels.add(new TrainTypeModel("130","DF5(KOU)"));
        trainTypeModels.add(new TrainTypeModel("131","DF7C"));
        trainTypeModels.add(new TrainTypeModel("132","DF7S"));
        trainTypeModels.add(new TrainTypeModel("133","GK1"));
        trainTypeModels.add(new TrainTypeModel("134","GK1F"));
        trainTypeModels.add(new TrainTypeModel("135","DF4E"));
        trainTypeModels.add(new TrainTypeModel("136","DF7D"));
        trainTypeModels.add(new TrainTypeModel("137","GK1A"));
        trainTypeModels.add(new TrainTypeModel("138","DF11"));
        trainTypeModels.add(new TrainTypeModel("139","TA"));
        trainTypeModels.add(new TrainTypeModel("140","DF10"));
        trainTypeModels.add(new TrainTypeModel("141","DF4D"));
        trainTypeModels.add(new TrainTypeModel("142","DF8B"));
        trainTypeModels.add(new TrainTypeModel("143","DF12"));
        trainTypeModels.add(new TrainTypeModel("144","DF7E"));
        trainTypeModels.add(new TrainTypeModel("148","DF4DJ"));
        trainTypeModels.add(new TrainTypeModel("151","DF8BJ"));
        trainTypeModels.add(new TrainTypeModel("152","NDJ3"));
        trainTypeModels.add(new TrainTypeModel("153","DF7G"));
        trainTypeModels.add(new TrainTypeModel("154","NJ1"));
        trainTypeModels.add(new TrainTypeModel("155","JL"));
        trainTypeModels.add(new TrainTypeModel("156","DF11Z"));
        trainTypeModels.add(new TrainTypeModel("157","DF7J"));
        trainTypeModels.add(new TrainTypeModel("158","DF11G"));
        trainTypeModels.add(new TrainTypeModel("201","8G"));
        trainTypeModels.add(new TrainTypeModel("202","8K"));
        trainTypeModels.add(new TrainTypeModel("203","6G"));
        trainTypeModels.add(new TrainTypeModel("204","6K"));
        trainTypeModels.add(new TrainTypeModel("205","SS1"));
        trainTypeModels.add(new TrainTypeModel("206","SS3"));
        trainTypeModels.add(new TrainTypeModel("207","SS4"));
        trainTypeModels.add(new TrainTypeModel("208","SS5"));
        trainTypeModels.add(new TrainTypeModel("209","SS6"));
        trainTypeModels.add(new TrainTypeModel("210","SS3G"));
        trainTypeModels.add(new TrainTypeModel("211","SS7"));
        trainTypeModels.add(new TrainTypeModel("212","SS8"));
        trainTypeModels.add(new TrainTypeModel("213","SS7B"));
        trainTypeModels.add(new TrainTypeModel("214","SS7C"));
        trainTypeModels.add(new TrainTypeModel("215","SS6B"));
        trainTypeModels.add(new TrainTypeModel("216","SS9"));
        trainTypeModels.add(new TrainTypeModel("217","SS7D"));
        trainTypeModels.add(new TrainTypeModel("219","DJ1"));
        trainTypeModels.add(new TrainTypeModel("220","DJ2"));
        trainTypeModels.add(new TrainTypeModel("221","DJF"));
        trainTypeModels.add(new TrainTypeModel("222","DJJ1"));
        trainTypeModels.add(new TrainTypeModel("223","DJF1"));
        trainTypeModels.add(new TrainTypeModel("224","SS7E"));
        trainTypeModels.add(new TrainTypeModel("225","SSJ3"));
        trainTypeModels.add(new TrainTypeModel("226","SS3C"));
        trainTypeModels.add(new TrainTypeModel("301","CRH1"));
        trainTypeModels.add(new TrainTypeModel("302","CRH2"));
        trainTypeModels.add(new TrainTypeModel("303","CRH3"));
        trainTypeModels.add(new TrainTypeModel("305","CRH5"));
        trainTypeModels.add(new TrainTypeModel("232","HXD2"));
        trainTypeModels.add(new TrainTypeModel("233","HXD3"));
        trainTypeModels.add(new TrainTypeModel("160","HXN3"));
        trainTypeModels.add(new TrainTypeModel("161","HXN5"));
        trainTypeModels.add(new TrainTypeModel("231","HXD1"));
        trainTypeModels.add(new TrainTypeModel("234","HXD1B"));
        trainTypeModels.add(new TrainTypeModel("146","DF12"));
        trainTypeModels.add(new TrainTypeModel("236","HXD3B"));
        trainTypeModels.add(new TrainTypeModel("901","HT30"));
        trainTypeModels.add(new TrainTypeModel("306","CRH380"));
        trainTypeModels.add(new TrainTypeModel("57","DF4B"));
        trainTypeModels.add(new TrainTypeModel("400","HXD3D"));
        trainTypeModels.add(new TrainTypeModel("401","HXD1D"));
        trainTypeModelMap=new HashMap<>();
        if(trainTypeModels!=null&&trainTypeModels.size()>0){
            for (TrainTypeModel t :
                    trainTypeModels) {
                trainTypeModelMap.put(t.getTrainTypeId(),t);
            }
        }
        hzList=new ArrayList<>();
        hzList.add("0.1Hz");
        hzList.add("0.5Hz");
        hzList.add("1Hz");
        hzList.add("2Hz");
        hzList.add("5Hz");
        hzList.add("10Hz");
        hzList.add("20Hz");
        hzList.add("50Hz");
        hzBtList[0]=0x01;
        hzBtList[1]=0x02;
        hzBtList[2]=0x03;
        hzBtList[3]=0x04;
        hzBtList[4]=0x05;
        hzBtList[5]=0x06;
        hzBtList[6]=0x07;
        hzBtList[7]=0x08;
    }

    public static DataUtil getInstance(){
        if (dataUtil==null){
            synchronized (DataUtil.class){
                if(dataUtil==null){
                    dataUtil=new DataUtil();
                }

            }
        }
        return dataUtil;
    }

    public UserModel getUser() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public List<TrainTypeModel> getTrainTypeModels() {
        return trainTypeModels;
    }

    public void setTrainTypeModels(List<TrainTypeModel> trainTypeModels) {
        this.trainTypeModels = trainTypeModels;
    }

    public Map<String, TrainTypeModel> getTrainTypeModelMap() {
        return trainTypeModelMap;
    }

    public void setTrainTypeModelMap(Map<String, TrainTypeModel> trainTypeModelMap) {
        this.trainTypeModelMap = trainTypeModelMap;
    }

    public List<PersonModel> getPersonModels() {
        return personModels;
    }

    public void setPersonModels(List<PersonModel> personModels) {
        this.personModels = personModels;
    }

    public Map<String, PersonModel> getPersonModelMap() {
        return personModelMap;
    }

    public void setPersonModelMap(Map<String, PersonModel> personModelMap) {
        this.personModelMap = personModelMap;
    }

    public enum TableNameEnum {
        SUBMITFILE("submitfile"),
        WORK("work"),
        PERSON("t_person"),
        TRAIN_TYPE("t_traintype");
        private final String data;
        private TableNameEnum(String data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return data;
        }
    }


    public void setFileDataByteModels(List<FileDataByteModel> fileDataByteModels) {
        this.fileDataByteModels = fileDataByteModels;
    }


    public FileDataByteModel getNewData(){
        return newFileDataByteModel;
    }


    public FileDataByteModel getOneFileDataByteModel(){
        return fileDataByteModelForOne;
    }

    public void setFileDataByteModels(FileDataByteModel fileDataByteModel){
        if(fileDataByteModel.getZ()==0x999&&fileDataByteModel.getY()==0x999&&fileDataByteModel.getX()==0x999){
            return;
        }
        newFileDataByteModel=fileDataByteModel;
        fileDataByteModels.add(fileDataByteModel);
        fileDataByteModelForOne=fileDataByteModel;
        fileDataByteModelForTen.add(fileDataByteModel);
    }
    public static int getFlagInt(byte[] data,int offset)
    {
        return (data[offset] & 0x00FF<< 8) + (data[offset + 1]& 0x00FF);

    }

    public static byte[] getFlagByte(int flag)
    {
        return new byte[] {
                (byte)((flag&0x00FF)>>8),
                (byte)((flag&0x000F))
        };
    }

    public static byte[] getLengthByte(int length)
    {
        return new byte[] {
                (byte)((length&0xFFFF)>>24),
                (byte)((length&0xFFFF)>>16),
                (byte)((length&0xFFFF)>>8),
                (byte)((length&0xFFFF))
        };
    }
    public static int getLengthInt(byte[] data, int offset)
    {
        return (data[offset] & 0xFFFF << 24 )+ (data[offset + 1] & 0xFFFF << 16) + (data[offset + 2] & 0xFFFF << 8) +( data[offset + 3] & 0xFFFF);
    }
    public static Map<String, String> getVersionMap() {
        return VersionMap;
    }


}
