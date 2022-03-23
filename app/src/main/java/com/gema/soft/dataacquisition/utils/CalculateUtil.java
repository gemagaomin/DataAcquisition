package com.gema.soft.dataacquisition.utils;

import com.gema.soft.dataacquisition.models.CalculateDataModel;
import com.gema.soft.dataacquisition.models.SmoothBumpModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CalculateUtil {
    private ConcurrentLinkedQueue<CalculateDataModel> array = new ConcurrentLinkedQueue<>();
    private static final int Rate = 50;
    private static final int calculateDelay = 1000;//滚动时长
    private static final int calculateLength = 2000;

    private int perTime = 1000/Rate;
    private int calculateCount = Math.round((Rate-5)*((float)calculateLength/1000));

    private long waitTime = 0;
    private long dateSum = 0;
    private double xSum = 0;
    private static AlgorithmUtil algorithmUtil;
    private static CalculateUtil calculateUtil;
    private  Thread tCal;
    private boolean isWrite=true;
    private String filePath=null;

    private CalculateUtil() {
        algorithmUtil=AlgorithmUtil.getInstance();
    }

    public static CalculateUtil getInstance(){
        if(calculateUtil==null){
            synchronized (CalculateUtil.class){
                if(calculateUtil==null){
                    calculateUtil=new CalculateUtil();
                }
            }
        }
        return calculateUtil;
    }

    public void addNewData(CalculateDataModel data){
        if(filePath==null)
            return;
        dateSum++;
        xSum+= data.getY();
        array.add(data);
    }

    public void StartCalculate(String filePath){
        this.filePath=filePath;
        this.isWrite=true;
        if(tCal==null){
            tCal = new Thread(){
                @Override
                public void run() {
                    while(isWrite){
                        ArrayList<CalculateDataModel> ret = pollCalculateData();
                        if (ret == null){
                            try {
                                Thread.sleep(waitTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }else{
                            List<SmoothBumpModel> result=algorithmUtil.buildData(ret);
                            writeDataToFile(result);
                        }
                    }
                }
            };
            tCal.start();
        }
    }

    public void StopCalculate(){
        //todo 结束时需要做的事情
        try{
            if(!array.isEmpty()){
                ArrayList<CalculateDataModel> ret=new ArrayList<>();
                while (!array.isEmpty()){
                    ret.add(array.poll());
                }
                algorithmUtil.buildData(ret);
            }
            if(tCal!=null){
                isWrite=false;
                tCal.stop();
                tCal.destroy();
            }
        }catch (Exception e){
            Log.d("error"+e.getMessage());
        }finally {
            array.clear();
            dateSum=0;
            xSum=0;
            waitTime=0;
            filePath=null;
            tCal=null;
        }
    }

    public void writeDataToFile(List<SmoothBumpModel> result){
        if(result.size()>0){
            FileUtil.getInstance().writeCalculateDataToFile(result,filePath,true);
        }
    }


  /*  public void testRun() {
        Thread tAdd = new Thread(){
            @Override
            public void run() {
                while(true){
                    CalculateDataModel d = new CalculateDataModel();
                    d.setTime(new Date().getTime());
                    addNewData(d);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        tAdd.start();
        try {
            Thread.sleep(1000*60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/
    private ArrayList<CalculateDataModel> pollCalculateData(){
        waitTime = 0;
        ArrayList<CalculateDataModel> ret = new ArrayList<CalculateDataModel>();
        int removeLength = -1;

        int count = array.size();
        if (count<calculateCount){
            waitTime = Math.max((calculateCount-count)*perTime,5 * perTime);
            return null;
        }
        CalculateDataModel head = array.peek();
        long startTime = head.getTime();
        long endTime = head.getTime() + calculateLength;

        int index = 0;
        Iterator<CalculateDataModel> it = array.iterator();

        while(it.hasNext()){
            index++;
            CalculateDataModel m = it.next();
            if (m.getTime()>=startTime+calculateDelay && removeLength<0){
                removeLength = index;
            }
            if (m.getTime()>=endTime){
                break;
            }
            ret.add(m);
        }

        if (ret.size()<=calculateCount){
            removeLength = ret.size();
            ret = null;
        }

        for (int i=0;i<removeLength;i++){
            array.poll();
        }
        return ret;
    }
}
