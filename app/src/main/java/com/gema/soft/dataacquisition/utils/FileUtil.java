package com.gema.soft.dataacquisition.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.gema.soft.dataacquisition.enums.FileEnum;
import com.gema.soft.dataacquisition.models.FileDataByteModel;
import com.gema.soft.dataacquisition.models.SmoothBumpModel;
import com.gema.soft.dataacquisition.models.TrainInfoModel;
import com.gema.soft.dataacquisition.queues.MyQueue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileUtil {
    public static final String FILE_STATUS_WAIT_UPLOADED="2";//文件等待上传
    public static  String SD_ABSOLUTE_FILE_PATH="";
    public static final int WORK_FILE_PATH=0;//todo 带后缀名称的为计算结果文件
    public static final  int LOG_FILE_PATH=1;
    public static final  int DB_FILE_PATH=2;
    public static final int INDEX_FILE_PATH=3;
    public static final int WORK_FILE_INFO_PATH=4;
    public static final int APK_VERSION_FILE_PATH=5;
    private float zone=999;//插入空针数据
    public static  String huanhang = "";
    public static String FILE_RANK_LOWER="0";
    public String indexName="/indexdata.b";//存放开始数据的文件
    public final  int maxByteBuffer=1024000;//1024*1000;
    private MyLog myLog;
    private String TAG="FileUtil";
    public static  String algorithmFileNameEnd=".arg";//计算结果文件
    private FileUtil() {
        myLog=MyLog.getInstance();
    }

    private static FileUtil fileUtil;

    public static FileUtil getInstance(){
        if(fileUtil==null){
            synchronized (FileUtil.class){
                if(fileUtil==null){
                    fileUtil=new FileUtil();
                    //todo 存放在项目根目录
                    //SD_ABSOLUTE_FILE_PATH=DataUtil.FILE_APP_PATH;//存放在项目根目录
                    //todo 存放在SD
                    SD_ABSOLUTE_FILE_PATH=getSDPath();//存放在SD
                    huanhang=System.getProperty("line.separator");
                }
            }
        }
        return fileUtil;
    }

    public static String getSDPath(){
        File file=null;
        boolean sdStatus=Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if(sdStatus){
            file= Environment.getExternalStorageDirectory();
        }
        return file.getPath()+"/PWCZ";
    }

    public String getFilePath(FileEnum fileEnum){
        String result=SD_ABSOLUTE_FILE_PATH+fileEnum.getFilePath();
        return result;
    }

    public String getWorkFile

    public String makeDirOrFile(String filePath){
        File file=new File(filePath);
        if(file.exists()){
            return file.getAbsolutePath();
        }
        File fileParent=file.getParentFile();
        if(!fileParent.exists()){
            fileParent.mkdirs();
        }
        if(file.isDirectory()){
            file.mkdir();
        }else{
            try{
                file.createNewFile();
            }catch (IOException e){
                MyException myException=new MyException();
                myException.buildExceptionToLogFile(TAG+" makeDirOrFile "+e.getMessage());
            }
        }
        return file.getAbsolutePath();
    }

    public String makeDir(String filePath){
        File file=new File(filePath);
        if(file.exists()){
            return file.getAbsolutePath();
        }
        file.mkdirs();
        return file.getAbsolutePath();
    }

    public void WriteMessageToFile(TrainInfoModel trainInfoModel, String filePath){
        File file=new File(filePath);
        if(!file.exists()||trainInfoModel==null)
            return;
        DataOutputStream out=null;
        try{
            out=new DataOutputStream(new FileOutputStream(file,true));
            String str=JSON.toJSONString(trainInfoModel);
            byte[] strByte= str.getBytes("UTF-8");
            out.writeInt(strByte.length);
            out.write(strByte);
        }catch (FileNotFoundException e){
            MyException myException=new MyException();
            myException.buildExceptionToLogFile(TAG+" WriteMessageToFile TrainInfoModel "+e.getMessage());
        }catch (IOException e){
            MyException myException=new MyException();
            myException.buildExceptionToLogFile(TAG+" WriteMessageToFile TrainInfoModel "+e.getMessage());
        }finally {
            if(out!=null){
                try{
                    out.flush();
                    out.close();
                }catch (IOException e){
                    MyException myException=new MyException();
                    myException.buildExceptionToLogFile(TAG+" WriteMessageToFile TrainInfoModel "+e.getMessage());
                }
            }
        }
    }

    public void WriteMessageToFile(List<FileDataByteModel>list, String filePath){
        File file=new File(filePath);
        if(!file.exists()||list==null||list.size()==0)
            return;
        DataOutputStream out=null;
        try{
            out=new DataOutputStream(new FileOutputStream(file,true));
            for (FileDataByteModel f :
                    list) {
                out.writeLong(f.getTime());
                out.writeFloat(f.getX());
                out.writeFloat(f.getY());
                out.writeFloat(f.getZ());
            }
        }catch (FileNotFoundException e){
            MyException myException=new MyException();
            myException.buildExceptionToLogFile(TAG+" WriteMessageToFile List "+e.getMessage());
        }catch (IOException e){
            MyException myException=new MyException();
            myException.buildExceptionToLogFile(TAG+" WriteMessageToFile List "+e.getMessage());
        }finally {
            list.clear();
            if(out!=null){
                try{
                    out.flush();
                    out.close();
                }catch (IOException e){
                    MyException myException=new MyException();
                    myException.buildExceptionToLogFile(TAG+" WriteMessageToFile List "+e.getMessage());
                }
            }
        }
    }

    public void WriteIndexMessageToFile(long time,String filePath,String dataFilePath){
        boolean isWrite=false;
        while (!DataUtil.getInstance().isWrited&&!isWrite){
            writheData();
            boolean isAddZoneData=false;
            DataUtil.getInstance().isWrited=true;
            File dataFile=new File(dataFilePath);
            long dataFileLength=dataFile.length();
            if(!dataFile.exists()){
                return;
            }
            DataInputStream dataInputStream=null;
            DataInputStream dataInputStream1=null;
            float fz=0l;
            DataOutputStream out=null;
            try{
                Object[] newIndex=null;
                File file=new File(filePath);
                if(!file.exists()){
                    file.createNewFile();
                }
                List<Object []> indexList=getIndexDataList(filePath);
                if(indexList==null||indexList.size()==0){
                    newIndex=new Object[]{0,time,fz};
                }else if(indexList.size()>0){
                    Object[] indexEnd=indexList.get(indexList.size()-1);
                    long sTime=(long)indexEnd[1];
                    int sIndex=(int)indexEnd[0];
                    dataInputStream=new DataInputStream(new FileInputStream(dataFile));
                    int length=dataInputStream.readInt();
                    long noHasLength=dataFileLength-length-4;
                    int count=(int)(noHasLength)/20;
                    int indexData=Math.max(count-2,0);
                    if(indexData>0){
                        long skipNumber=20*indexData+length;
                        dataInputStream.skip(skipNumber);
                        FileDataByteModel lastModel=getOneFileDataByteModel(dataInputStream);
                        FileDataByteModel lastEndModel=getOneFileDataByteModel(dataInputStream);
                        long et=0;
                        if(lastEndModel!=null){
                            if(lastEndModel.getX()==zone&&lastEndModel.getY()==zone&&lastEndModel.getZ()==zone){
                                et=lastModel.getTime();
                            }else{
                                et=lastEndModel.getTime();
                            }
                            if(time!=-1&&time-et>100){
                                isAddZoneData=true;
                                count++;
                            }
                            float c=indexData-sIndex;
                            if(c>0){
                                fz=(et-sTime)/c;
                                indexEnd[2]=fz;
                            }
                            newIndex=new Object[]{count+1,time,fz};
                        }
                    }
                    dataInputStream.close();
                    dataInputStream=null;
                    if(noHasLength>0&&noHasLength%20!=0){
                        dataInputStream1=new DataInputStream(new FileInputStream(dataFile));
                        int intLength=dataInputStream1.readInt();
                        byte[] data=new byte[intLength];
                        dataInputStream1.read(data);
                        int number=(int)noHasLength/20;
                        int index=0;
                        String tempDataFileName=fileUtil.makeDirOrFile(fileUtil.getFilePath(FileEnum.WORK_FILE_PATH))+"/tempworkdata";
                        File tempDataFile=new File(tempDataFileName);
                        if(!tempDataFile.exists()){
                            tempDataFile.createNewFile();
                        }
                        out=new DataOutputStream(new FileOutputStream(tempDataFile));
                        out.writeInt(intLength);
                        out.write(data);
                        byte[] datas;
                        int endLength=0;
                        int maxByteNumber=maxByteBuffer/20;
                        int sxNumber=0;
                        if(number>0){
                            while (index<number-1){
                                sxNumber=(number-1-index);
                                endLength=Math.min(sxNumber,maxByteNumber);
                                datas=new byte[endLength*20];
                                dataInputStream1.read(datas);
                                out.write(datas);
                                index+=endLength;
                            }
                        }
                        if(isAddZoneData){
                            out.writeLong(new Date().getTime());
                            out.writeFloat(zone);
                            out.writeFloat(zone);
                            out.writeFloat(zone);
                        }
                        out.flush();
                        out.close();
                        out=null;
                        long temp=(tempDataFile.length()-intLength-4)%20;
                        if(dataFile!=null&&dataFile.exists()&&temp==0){
                            String fileOldName=dataFile.getName();
                            String fileOldPath=dataFile.getParent();
                            File newDataOldFile=new File(fileOldPath+"/"+fileOldName+2);
                            if(dataFile.renameTo(newDataOldFile)){
                                if(tempDataFile.renameTo(new File(fileOldPath+"/"+fileOldName))){
                                    newDataOldFile.delete();
                                }else{
                                    newDataOldFile.renameTo(new File(fileOldPath+"/"+fileOldName));
                                }
                            }
                        }
                    }else{
                        if(isAddZoneData){
                            out=new DataOutputStream(new FileOutputStream(dataFile,true));
                            out.writeLong(new Date().getTime());
                            out.writeFloat(zone);
                            out.writeFloat(zone);
                            out.writeFloat(zone);
                            out.flush();
                            out.close();
                            out=null;
                        }
                    }
                }
                if(newIndex!=null){
                    out=new DataOutputStream(new FileOutputStream(file,true));
                    out.writeInt((int)newIndex[0]);
                    out.writeLong((long) newIndex[1]);
                    out.writeFloat((float)newIndex[2]);
                }
                isWrite=true;
            }catch (FileNotFoundException e){
                MyException myException=new MyException();
                myException.buildExceptionToLogFile(TAG+" WriteIndexMessageToFile "+e.getMessage());
            }catch (IOException e){
                MyException myException=new MyException();
                myException.buildExceptionToLogFile(TAG+" WriteIndexMessageToFile  "+e.getMessage());
            }finally {
                if(dataInputStream!=null){
                    try{
                        dataInputStream.close();
                    }catch (IOException e){
                        MyLog.d("WriteIndexMessageToFile   "+e.getMessage());
                    }
                }
                if(dataInputStream1!=null){
                    try{
                        dataInputStream1.close();
                    }catch (IOException e){
                        MyException myException=new MyException();
                        myException.buildExceptionToLogFile(TAG+"WriteIndexMessageToFile "+e.getMessage());
                    }
                }
                if(out!=null){
                    try{
                        out.flush();
                        out.close();
                    }catch (IOException e){
                        MyException myException=new MyException();
                        myException.buildExceptionToLogFile(TAG+"file write fail! "+e.getMessage());
                    }
                }
                DataUtil.getInstance().isWrited=false;
            }
        }
    }

    public boolean WriteDataToFile(String path,String str){
        boolean result=false;
        File file=new File(path);
        OutputStream outputStream=null;
        try{
            if(file.exists()){
                str+="\n";
                outputStream=new FileOutputStream(file,true);
                outputStream.write(str.getBytes("UTF-8"));
                outputStream.flush();
                result=true;
            }
        }catch (IOException e){

        }finally {
            try{
                if(outputStream!=null){
                    outputStream.close();
                }
            }catch (IOException e){

            }
        }
        return result;
    }

    public boolean deleteFile(String filePath){
        if(TextUtils.isEmpty(filePath))
            return true;
        File file=new File(filePath);
        boolean result=false;
        if(file.exists()){
            result=file.delete();
        }
        File fileTemp=new File(filePath+"1");
        if(fileTemp.exists()){
            result=file.delete();
        }
        return result;
    }

    public File getFile(String filePath){
        File file=new File(filePath);
        if(file!=null&&file.exists()){
            return file;
        }
        return null;
    }

    public boolean refFileNewTemp(String filePath,String reFilePath){
        File file=new File(filePath);
        DataInputStream inputStream=null;
        DataInputStream inputStreamTemp=null;
        DataOutputStream outputStream=null;
        if(!file.exists())
            return false;
        try{
            inputStream=new DataInputStream(new FileInputStream(file));
            int dataLength=inputStream.readInt();
            byte[] dataStrByte=new byte[dataLength];
            inputStream.read(dataStrByte);
            String indexFilePath=FileUtil.getInstance().makeDirOrFile(FileUtil.getInstance().getFilePath(FileEnum.INDEX_FILE_PATH))+indexName;
            List<Object[]> zoneList=getIndexDataList(indexFilePath);
            File file1=new File(reFilePath);
            if(!file1.exists()){
                file1.createNewFile();
            }
            int number=(int)(file.length()-4-dataLength)/20;
            int isStartLength=0;
            boolean isHasStart=false;
            int z_index=1;
            Object[] zoneNext=null;
            float ts=0;
            int next_index=0;
            long st=0l;
            int old_index=0;
            long time=0;
            if(zoneList!=null&&zoneList.size()>0){
                isStartLength=zoneList.size();
                isHasStart=zoneList!=null&&isStartLength>=2;
                zoneNext=zoneList.get(0);
                ts=  (float)zoneNext[2];
                st=(long)zoneNext[1];
                old_index=(int)zoneNext[0];
                time=st;
            }
            outputStream=new DataOutputStream(new FileOutputStream(file1));
            outputStream.writeInt(dataLength);
            outputStream.write(dataStrByte);
            byte[] byteBuffer=null;
            int maxLength=0;
            int index=0;
            if(isHasStart&&z_index<isStartLength){
                for(int i=z_index;i<isStartLength;i++){
                    old_index=(int)zoneNext[0];
                    st=(long)zoneNext[1];
                    zoneNext=zoneList.get(i);
                    ts=(float)zoneNext[2];
                    next_index=(int)zoneNext[0]-1;
                    int innerIndex=0;
                    int ys=0;
                    if(next_index>old_index){
                        maxLength=(next_index-old_index)*20;
                        int end=Math.min(maxLength,maxByteBuffer)/20;
                        if(maxLength>maxByteBuffer){
                            innerIndex=maxLength/maxByteBuffer;
                            for(int j=0;j<innerIndex;j++){
                                byteBuffer=new byte[maxByteBuffer];
                                inputStream.read(byteBuffer);
                                for(int n=0;n<end;n++){
                                    time=st+(long)(ts*(n+end*j));
                                    getLongToByte(time,byteBuffer,n*20);
                                    index++;
                                }
                                outputStream.write(byteBuffer);
                            }
                            ys=maxLength%maxByteBuffer;
                            if(ys>0){
                                int oldEndIndex=innerIndex*(maxByteBuffer/20);
                                end=ys/20;
                                byteBuffer=new byte[ys];
                                inputStream.read(byteBuffer);
                                for(int n=0;n<end;n++){
                                    time=st+(long)(ts*(n+oldEndIndex));
                                    getLongToByte(time,byteBuffer,n*20);
                                    index++;
                                }
                                outputStream.write(byteBuffer);
                            }
                        }else{
                            byteBuffer=new byte[maxLength];
                            inputStream.read(byteBuffer);
                            for(int n=0;n<end;n++){
                                time=st+(long)(ts*(n));
                                getLongToByte(time,byteBuffer,n*20);
                                index++;
                            }
                            outputStream.write(byteBuffer);
                        }
                    }
                }
            }else{
                if(number>0){
                    FileDataByteModel startModel=getOneFileDataByteModel(inputStream);
                    inputStream.skip(((number-2)*20));
                    FileDataByteModel endModel=getOneFileDataByteModel(inputStream);
                    inputStream.close();
                    inputStream=null;
                    inputStreamTemp=new DataInputStream(new FileInputStream(filePath));
                    inputStreamTemp.skip(dataLength+4);
                    st=startModel.getTime();
                    maxLength=number*20;
                    long endTime=endModel!=null?endModel.getTime():st;
                    ts=(endTime-st)/number;
                    int end=Math.min(maxLength,maxByteBuffer)/20;
                    if(maxLength>maxByteBuffer){
                        int innerIndex=maxLength/maxByteBuffer;
                        for(int j=0;j<innerIndex;j++){
                            byteBuffer=new byte[maxByteBuffer];
                            inputStreamTemp.read(byteBuffer);
                            for(int n=0;n<end;n++){
                                time=st+(long)(ts*((end*j)+n));
                                getLongToByte(time,byteBuffer,n*20);
                                index++;
                            }
                            outputStream.write(byteBuffer);
                        }
                        int ys=maxLength%maxByteBuffer;
                        if(ys>0){
                            int oldEndIndex=innerIndex*(maxByteBuffer/20);
                            end=ys/20;
                            byteBuffer=new byte[ys];
                            inputStreamTemp.read(byteBuffer);
                            for(int n=0;n<end;n++){
                                time=st+(long)(ts*(n+oldEndIndex));
                                getLongToByte(time,byteBuffer,n*20);
                                index++;
                            }
                            outputStream.write(byteBuffer);
                        }
                    }else{
                        byteBuffer=new byte[maxLength];
                        inputStreamTemp.read(byteBuffer);
                        for(int n=0;n<end;n++){
                            time=st+(long)(ts*(n));
                            getLongToByte(time,byteBuffer,n*20);
                            index++;
                        }
                        outputStream.write(byteBuffer);
                    }
                }
            }
            byteBuffer=null;
        }catch (Exception e){
            MyException myException=new MyException();
            myException.buildExceptionToLogFile(TAG+" refFileNewTemp"+e.getMessage());
            Log.d("MainActivity","文件丢失 refFile"+e.getMessage());
            return false;
        }finally {
            try{
                if(inputStream!=null){
                    inputStream.close();
                }
                if(inputStreamTemp!=null){
                    inputStreamTemp.close();
                }
                if(outputStream!=null){
                    outputStream.close();
                }
            }catch (IOException e){
                MyException myException=new MyException();
                myException.buildExceptionToLogFile(TAG+" refFileNewTemp"+e.getMessage());
            }
        }
        return true;
    }

    public FileDataByteModel getOneFileDataByteModel(DataInputStream inputStream){
        FileDataByteModel fileDataByteModel=new FileDataByteModel();
        try{
            fileDataByteModel.setTime(inputStream.readLong());
            fileDataByteModel.setX(inputStream.readFloat());
            fileDataByteModel.setY(inputStream.readFloat());
            fileDataByteModel.setZ(inputStream.readFloat());
        }catch (Exception e){
            return null;
        }
        return fileDataByteModel;
    }

    public List<Object[]> getIndexDataList(String  filePath){
        List<Object[]> result=new ArrayList<>();
        DataInputStream inputStream=null;
        File file=new File(filePath);
        try{
            if(!file.exists()){
                return null;
            }
            if(file.length()>0){
                int index=0;
                int number=(int)file.length()/16;
                inputStream=new DataInputStream(new FileInputStream(file));
                while (index<number){
                    Object[] one=new Object[3];
                    one[0]=inputStream.readInt();
                    one[1]=inputStream.readLong();
                    one[2]=inputStream.readFloat();
                    result.add(one);
                    index++;
                }
            }
        }catch (Exception e){
            Log.d("getIndexDataList",e.getLocalizedMessage());
            MyException myException=new MyException();
            myException.buildExceptionToLogFile(TAG+"getIndexDataList: error "+e.getMessage());
            return null;
        }finally {
            try{
                if(inputStream!=null){
                    inputStream.close();
                }
            }catch (IOException e){
                Log.d("getIndexDataList",e.getMessage());
                MyException myException=new MyException();
                myException.buildExceptionToLogFile(TAG+"getIndexDataList: error "+e.getMessage());
            }
        }
        return result;
    }

    public void writheData(){
        if(DataUtil.getInstance().isWrited||DataUtil.getInstance().runFilePath==null)
            return;
        MyQueue myQueue=MyQueue.getInstance();
        if(myQueue.QueueEmpty())
            return;
        if(myQueue.QueueLength()==0)
            return;
        String workFilePath=DataUtil.getInstance().runFilePath;
        if(workFilePath==null)
            return;
        DataUtil.getInstance().isWrited=true;
        int fz=myQueue.QueueLength();
        if(fz<=0)
            return;
        List<FileDataByteModel> list=new ArrayList<>();
        try{
            FileDataByteModel fileDataByteModel=(FileDataByteModel)myQueue.deQueue();
            while (fileDataByteModel!=null){
                list.add(fileDataByteModel);
                fileDataByteModel=(FileDataByteModel)myQueue.deQueue();
            }
            if(list.size()>0){
                FileUtil.getInstance().WriteMessageToFile(list,DataUtil.getInstance().runFilePath);
                list.clear();
            }
        }catch (Exception e){
            MyException myException=new MyException();
            myException.buildExceptionToLogFile(TAG+"writheData: error "+e.getMessage());
        }finally {
            DataUtil.getInstance().isWrited=false;
        }
    }

    public final void getLongToByte(long v,byte[] writeBuffer,int startIndex) throws IOException {
        writeBuffer[startIndex+0] = (byte)(v >>> 56);
        writeBuffer[startIndex+1] = (byte)(v >>> 48);
        writeBuffer[startIndex+2] = (byte)(v >>> 40);
        writeBuffer[startIndex+3] = (byte)(v >>> 32);
        writeBuffer[startIndex+4] = (byte)(v >>> 24);
        writeBuffer[startIndex+5] = (byte)(v >>> 16);
        writeBuffer[startIndex+6] = (byte)(v >>>  8);
        writeBuffer[startIndex+7] = (byte)(v >>>  0);
    }

    public List<String> readStringListFormFile(String filePath){
        File file=new File(filePath);
        if(file.exists()){
            BufferedReader bufferedReader=null;
            try{
                bufferedReader=new BufferedReader(new FileReader(file));
                List<String> strList=new ArrayList<>();
                String str;
                while ((str=bufferedReader.readLine())!=null){
                    strList.add(str);
                }
                return strList;
            }catch (Exception e){

            }finally {
                try{
                    if(bufferedReader!=null)
                        bufferedReader.close();
                }catch (Exception e){

                }
            }
        }
        return null;
    }

    public void writeDataToFile(List<String> list,String filePath){
        File file=new File(filePath);
        BufferedWriter bufferedWriter=null;
        try{
            if(!file.exists()){
                file.createNewFile();
            }
            bufferedWriter=new BufferedWriter(new FileWriter(file));
            for(int i=0,num=list.size();i<num;i++){
                String item=list.get(i);
                bufferedWriter.write(item,0,item.length());
                bufferedWriter.newLine();
            }
        }catch (Exception e){

        }finally {
            try{
                if(bufferedWriter!=null)
                    bufferedWriter.close();
            }catch (Exception e){

            }
        }
    }

    public void writeDataToFile(String str,String filePath,boolean append){
        File file=new File(filePath);
        BufferedWriter bufferedWriter=null;
        try{
            if(!file.exists()){
                file.createNewFile();
            }
            bufferedWriter=new BufferedWriter(new FileWriter(file,append));
            bufferedWriter.write(str,0,str.length());
            bufferedWriter.newLine();
        }catch (Exception e){

        }finally {
            try{
                if(bufferedWriter!=null)
                    bufferedWriter.close();
            }catch (Exception e){

            }
        }
    }

    public boolean writeCalculateDataToFile(List<SmoothBumpModel> list,String filePath,boolean append){
        File file=new File(filePath);
        BufferedWriter bufferedWriter=null;
        boolean result=true;
        try{
            if(!file.exists()){
                file.createNewFile();
            }
            bufferedWriter=new BufferedWriter(new FileWriter(file,append));
            for(int i=0,num=list.size();i<num;i++){
                String item=JSON.toJSONString(list.get(i));
                bufferedWriter.write(item,0,item.length());
                bufferedWriter.newLine();
            }
        }catch (Exception e){
            result=false;
        }finally {
            try{
                if(bufferedWriter!=null)
                    bufferedWriter.close();
            }catch (Exception e){

            }
            return result;
        }
    }

    public boolean copyFile(String filePath,String copyFilePath){
        File file=new File(filePath);
        InputStream inputStream=null;
        OutputStream outputStream=null;
        boolean result=true;
        try{
            if(file.exists()){
                File copyFilePathFile=new File(copyFilePath);
                inputStream=new FileInputStream(file);
                byte[] bytes=new byte[1024];
                outputStream = new FileOutputStream(copyFilePathFile);
                int c;
                while ((c = inputStream.read(bytes)) > 0) {
                    outputStream.write(bytes, 0, c);
                }
            }else{
                result=false;
            }
        }catch (FileNotFoundException e){
            result=false;
        }catch (IOException e){
            result=false;
        }finally {
            try{
                if(inputStream!=null){
                    inputStream.close();
                }
                if(outputStream!=null){
                    outputStream.close();
                }
            }catch (Exception e){

            }
            return result;
        }
    }
}
