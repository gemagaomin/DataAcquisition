package com.gema.soft.dataacquisition.utils;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gema.soft.dataacquisition.models.FileDataByteModel;
import com.gema.soft.dataacquisition.models.TrainInfoModel;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileDataAnalysisUtil {
    private static FileDataAnalysisUtil dataAnalysisUtil;
    private String TAG="FileDataAnalysisUtil";
    private MyLog myLog;
    private FileDataAnalysisUtil() {
        myLog=MyLog.getInstance();
    }
    public static FileDataAnalysisUtil getInstance(){
        if(dataAnalysisUtil==null){
            synchronized (FileDataAnalysisUtil.class){
                if(dataAnalysisUtil==null){
                    dataAnalysisUtil=new FileDataAnalysisUtil();
                }
            }
        }
        return dataAnalysisUtil;
    }

    public byte[] getInfoData(TrainInfoModel trainInfoModel){
        byte[] bytesResult=null;
        try{
            String str= new String(JSON.toJSONString(trainInfoModel).getBytes(),"UTF-8");
            byte[]bytesData=str.getBytes("UTF-8");
            int numberLength=bytesData.length;
            byte[] bytes=getByteByInt(numberLength,false);
            bytesResult=new byte[numberLength+bytes.length];
            System.arraycopy(bytes,0,bytesResult,0,4);
            System.arraycopy(bytesData,0,bytesResult,4,bytesData.length);
        }catch (Exception e){

        }
        return bytesResult;
    }


    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    public List<String> getFileByteData(String filePath){
        if(TextUtils.isEmpty(filePath))
            return null;
        File file=FileUtil.getInstance().getFile(filePath);
        if(file==null){
            return null;
        }
        List<String> objects=new ArrayList<>();
        DataInputStream inputStream=null;
        try{
            inputStream=new DataInputStream(new FileInputStream(file));
                int index=inputStream.readInt()+4;
                inputStream.readUTF();
                byte[] bytes=new byte[20];
                long time=inputStream.readLong();
                float x=inputStream.readFloat();
                float y=inputStream.readFloat();
                float z=inputStream.readFloat();
                index+=20;
                    Log.d(TAG, "getFileByteData: jx 123"+"x+y+z = " +time+"   x:"+ x+" y:"+y+"  z:"+z);

        }catch (Exception e){
            myLog.writeToFile("没有获取到文件"+DataUtil.getInstance().runFilePath);
        }finally {
            if(inputStream!=null){
                try{
                    inputStream.close();
                }catch (IOException e){
                    Log.d(TAG, "getFileByteData: "+e.getMessage());
                }
            }
        }
        return objects;
    }

    public byte[] getByteFromFile(InputStream inputStream){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        try{
            while ((n = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        }catch (IOException e){
            Log.d(TAG, "getByteFromFile: "+e.getMessage());
        }
        return out.toByteArray();
    }
    public byte[] getTime(){
        long time=new Date().getTime();
        return getByteByLong(time,false);
    }


    public byte[] getByteByInt(int i,boolean isBeginEnding){
        byte[] result=new byte[4];
        if(isBeginEnding){
            for(int j=result.length-1;j>=0;j--){
                result[j]=(byte)(i & 0xFF);
                i>>=8;
            }
        }else{
            for(int j=0;j<result.length;j++){
                result[j]=(byte)(i&0xFF);
                i>>=8;
            }
        }
        return result;
    }

    public static String bytesToHexString(byte[] bytes) {
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }


    //todo 有问题
    public int getIntByBtye(byte[] buf,boolean bBigEnding){
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }

        if (buf.length > 4) {
            throw new IllegalArgumentException("byte array size > 4 !");
        }

        int r = 0;
        if (bBigEnding) {
            for (int i = 0; i < buf.length; i++) {
                r <<= 8;
                r |= (buf[i] & 0xFF);
            }
        } else {
            for (int i = buf.length - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[i] & 0xFF);
            }
        }
        return r;
    }

    public TrainInfoModel getTrainInfoModelByByte(byte[] src,int startIndex,int endIndex){
        TrainInfoModel trainInfoModel=null;
        try{
            byte[] datas=new byte[endIndex-startIndex];
            System.arraycopy(src, startIndex, datas, 0, datas.length);
            String obj=new String(datas,"UTF-8");
            trainInfoModel= JSONObject.parseObject(obj,TrainInfoModel.class);
        }catch (UnsupportedEncodingException e){
            myLog.writeToFile(TAG+"getTrainInfoModelByByte"+e.getMessage());
        }
        return trainInfoModel;
    }

    public List<FileDataByteModel> getFileDataByteModeByByte(byte[] src,int startIndex,int stepLength){
        int allLength=src.length;
        List<FileDataByteModel> list=new ArrayList<>();
        if(startIndex>=allLength)
            return list;
        int number=0;
        for(int i=startIndex;i<allLength;i=i+stepLength){
            byte[] timeByte=new byte[8];
            int shortStartLength=startIndex+(number)*stepLength;
            System.arraycopy(src,shortStartLength,timeByte,0,8);
            long time=getLongByByte(timeByte,false);
            timeByte=new byte[4];
            shortStartLength+=8;
            System.arraycopy(src,shortStartLength,timeByte,0,4);
            float x=getFloatByByte(timeByte,false);
            timeByte=new byte[4];
            shortStartLength+=4;
            System.arraycopy(src,shortStartLength,timeByte,0,4);
            float y=getFloatByByte(timeByte,false);
            timeByte=new byte[4];
            shortStartLength+=4;
            System.arraycopy(src,shortStartLength,timeByte,0,4);
            float z=getFloatByByte(timeByte,false);
            list.add(new FileDataByteModel(time,x,y,z));
            number++;
        }
        return list;
    }

    public byte[] getByteByFloat(float f){
        byte[] result=ByteBuffer.allocate(4).putFloat(f).array();
       return result;
    }

    public byte[] getByteByLong(long i,boolean isBeginEnding){
        byte[] result=new byte[8];
        if(isBeginEnding){
            for(int j=result.length-1;j>=0;j--){
                result[j]=(byte)(i & 0xFF);
                i>>=8;
            }
        }else{
            for(int j=0;j<result.length;j++){
                result[j]=(byte)(i&0xFF);
                i>>=8;
            }
        }
        return result;
    }

    public long getLongByByte(byte[] buf,boolean isBeginEnding){
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }

        if (buf.length > 8) {
            throw new IllegalArgumentException("byte array size > 8 !");
        }

        long r=0l;
        if (isBeginEnding) {
            for (int i = 0; i < buf.length; i++) {
                r <<= 8;
                r |= (buf[i] & 0xFF);
            }
        } else {
            for (int i = buf.length - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[i] & 0xFF);
            }
        }

        return r;
    }

    public float getFloatByByte(byte[] buf,boolean isBeginEnding){
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }

        if (buf.length > 4) {
            throw new IllegalArgumentException("byte array size > 4 !");
        }
        int r = 0;
        if (isBeginEnding) {
            for (int i = 0; i < buf.length; i++) {
                r <<= 8;
                r |= (buf[i] & 0xFF);
            }
        } else {
            for (int i = buf.length - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[i] & 0xFF);
            }
        }
        return Float.intBitsToFloat(r);
    }

    public byte[] objectToByte(Object obj) throws Exception {
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            return bos.toByteArray();
        } finally {
            if (oos != null) oos.close();
        }
    }
}
