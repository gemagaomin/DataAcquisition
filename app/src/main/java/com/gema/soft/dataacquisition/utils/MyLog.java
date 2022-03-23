package com.gema.soft.dataacquisition.utils;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.gema.soft.dataacquisition.BuildConfig;
import com.gema.soft.dataacquisition.enums.FileEnum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;


public class MyLog {
    private static String TAG="myApp";
    private static boolean sDebug= BuildConfig.DEBUG;
    public static void d(String msg, Object...args){
        if(!sDebug)
            return;
        android.util.Log.d(TAG, String.format(msg,args));
    }

    public static void dNew(String url, JSONObject msg, Object...args){
        if(!sDebug)
            return;
        System.out.println(url);
        String str = msg.toJSONString();
        int index = 0;
        for (int i=0;(i+1)*1000<str.length();i++){
            System.out.println(str.substring(i*1000,(i+1)*1000));
            index++;
        }
        System.out.println(str.substring(index*1000));
        android.util.Log.d(TAG+"2019-12-11", String.format(str,args));
    }

    private static MyLog myLog;

    private MyLog() {
    }

    public static MyLog getInstance(){
        if(myLog ==null){
            synchronized (MyLog.class){
                if(myLog ==null){
                    myLog =new MyLog();
                }
            }
        }
        return myLog;
    }
    public  void writeToFile(String msg){
        FileUtil fileUtil=FileUtil.getInstance();
        Date date=new Date();
        String str=fileUtil.makeDirOrFile(FileUtil.getSDPath()+FileEnum.LOG_FILE_PATH.getFilePath()+"/"+date.getYear()+date.getMonth()+date.getDate()+".txt");
        if(str!=null){
            MyFile myFile=null;
            try{
                String time=DateUtil.getInstance().getDataLongS(new Date());
                myFile=new MyFile(str);
                myFile.Write(time+":"+msg+"\n");
            }catch (Exception e){
                if(myFile!=null){
                    try{
                        myFile.Close();
                    }catch (Exception e1){

                    }
                }
            }

        }

    }

    public  void writeToFileTest(){
        FileUtil fileUtil=FileUtil.getInstance();
        Date date=new Date();
        String str=fileUtil.makeDirOrFile(FileUtil.getSDPath()+FileEnum.LOG_FILE_PATH.getFilePath()+"/"+date.getYear()+date.getMonth()+date.getDate()+".txt");
        if(str!=null){
            File myFile=null;
            try{
                String time=DateUtil.getInstance().getDataLongS(new Date());
                myFile=new File(str);
                if(myFile.exists());{
                    String strQ=fileUtil.makeDirOrFile(FileUtil.getSDPath()+FileEnum.LOG_FILE_PATH.getFilePath()+"/"+date.getYear()+date.getMonth()+date.getDate()+"1.txt");
                    File fileT=new File(strQ);
                    if(myFile.renameTo(fileT)){
                        String fn=myFile.getName();
                        String fn1= fileT.getName();
                        Log.d(TAG, "writeToFileTest: "+myFile.exists());
                        Log.d(TAG, "writeToFileTest: "+fileT.exists());
                        Log.d(TAG, "writeToFileTest: "+fn);
                        Log.d(TAG, "writeToFileTest: "+fn1);
                    }
                }

            }catch (Exception e){
            }

        }

    }
    class MyFile {
        FileOutputStream fout;

        public MyFile(String fileName) throws FileNotFoundException {
            fout = new FileOutputStream(fileName, true);
        }

        public void Write(String str) throws IOException {
            byte[] bytes = str.getBytes();
            fout.write(bytes);
        }

        public void Close() throws IOException {
            fout.close();
            fout.flush();
        }
    }
}
