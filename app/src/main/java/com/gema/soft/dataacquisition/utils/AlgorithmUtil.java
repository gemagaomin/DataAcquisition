package com.gema.soft.dataacquisition.utils;

import com.gema.soft.dataacquisition.models.CalculateDataModel;
import com.gema.soft.dataacquisition.models.SmoothBumpModel;
import com.gema.soft.dataacquisition.pojo.ZonePojo;
import java.util.ArrayList;
import java.util.List;

public class AlgorithmUtil {
    private final int maxZoneLing=10;
    private final float min=3f;
    private final float minAr=20f;
    private long maxZoneTimeLong=10;
    private double[] levelNumbers=new double[]{8,10,12,14,16};
    private PropertiesUtil propertiesUtil;
    private String maxZoneTime;
    private int number=0;

    private static AlgorithmUtil algorithmUtil;

    private AlgorithmUtil() {
        propertiesUtil=PropertiesUtil.getInstance();
        maxZoneTime= propertiesUtil.getProperty("maxZoneTimeLong");
        String strLevelNumbers=propertiesUtil.getProperty("levelNumbers");
        if(strLevelNumbers!=null&&strLevelNumbers.length()>0){
            String[] arr=strLevelNumbers.split(",");
            if(arr!=null&&arr.length==5){
                levelNumbers=new double[5];
                for(int i=0;i<5;i++){
                    levelNumbers[i]=Double.parseDouble(arr[i]);
                }
            }
        }
    }

    public static AlgorithmUtil getInstance(){
        if(algorithmUtil==null){
            synchronized (AlgorithmUtil.class){
                if(algorithmUtil==null){
                    algorithmUtil=new AlgorithmUtil();
                }
            }
        }
        return algorithmUtil;
    }

    //todo 给android使用
    public List<SmoothBumpModel> buildData(ArrayList<CalculateDataModel> list){
      List<ZonePojo> zonePojos=new ArrayList<ZonePojo>();
        if(list!=null&&list.size()>0){
            number+=list.size();
            boolean isZone=false;
            CalculateDataModel temp=null;
            int count=0;
            ZonePojo zonePojo=null;
            if(StringUtil.isNotNull(maxZoneTime)){
                maxZoneTimeLong=Long.parseLong(maxZoneTime);
            }
            for(int i=0;i<list.size();i++){
                CalculateDataModel one=list.get(i);
                float ar=one.getAr();
                long time=one.getTime();
                if(temp!=null){
                    ar=((one.getY()-temp.getY())*1000/(time-temp.getTime()));
                }
                if(isZone){
                    if(count<maxZoneLing){
                        if(Math.abs(ar)<min){
                            count++;
                            if(count==maxZoneLing){
                                zonePojo.setEndIndex(i-maxZoneLing);
                                long endTime=list.get(i-maxZoneLing).getTime();
                                zonePojo.setEndTime(endTime);
                                zonePojos.add(zonePojo);
                                isZone=false;
                                zonePojo=null;
                                count=0;
                            }
                        }else{
                            if(i==list.size()-1){
                                if(zonePojo!=null){
                                    zonePojo.setEndIndex(i);
                                    zonePojo.setEndTime(list.get(i).getTime());
                                    zonePojos.add(zonePojo);
                                    isZone=false;
                                    count=0;
                                    zonePojo=null;
                                }
                                continue;
                            }

                            if((time-zonePojo.getStartTime())>maxZoneTimeLong*1000){
                                if(zonePojo!=null){
                                    zonePojo.setEndIndex(i);
                                    zonePojo.setEndTime(list.get(i).getTime());
                                    zonePojos.add(zonePojo);
                                    isZone=false;
                                    count=0;
                                    zonePojo=null;
                                }
                                continue;
                            }
                            count=0;
                        }
                    }else{
                        if(i==list.size()-1){
                            if(zonePojo!=null){
                                zonePojo.setEndIndex(i);
                                zonePojo.setEndTime(list.get(i).getTime());
                                zonePojos.add(zonePojo);
                                isZone=false;
                                count=0;
                                zonePojo=null;
                            }
                            continue;
                        }
                        count=0;
                    }
                }else{
                    if(zonePojo==null){
                        if(Math.abs(ar)>minAr){
                            zonePojo=new ZonePojo();
                            zonePojo.setStartIndex(i);
                            zonePojo.setStartTime(time);
                            isZone=true;
                            count=0;
                        }
                    }
                }
                one.setAr(ar);
                temp=one;
            }
        }
        List<SmoothBumpModel> saveList=getSaveData(zonePojos,list);
        return saveList;
    }

    private List<SmoothBumpModel> getSaveData(List<ZonePojo> zonePojos,ArrayList<CalculateDataModel> sourcesList){
        List<SmoothBumpModel> result=new ArrayList<SmoothBumpModel>();
        List<SmoothBumpModel> resultTemp=new ArrayList<SmoothBumpModel>();
        for(int i=0;i<zonePojos.size();i++){
            ZonePojo zonePojo=zonePojos.get(i);
            int sI=zonePojo.getStartIndex();
            if(sI>1){
                for(int j=0;j<sI;j++){
                    CalculateDataModel calculateDataModel=sourcesList.get(sI-j-1);
                    float ar=calculateDataModel.getAr();
                    if(Math.abs(ar)!=0&&Math.abs(ar)<min){
                        zonePojo.setStartTime(calculateDataModel.getTime());
                        zonePojo.setStartIndex(sI-j-1);
                        break;
                    }
                }
            }
            SmoothBumpModel resultData=getHasResult(zonePojo.getStartIndex(),zonePojo.getEndIndex(),sourcesList);
            if(resultData!=null){
                resultTemp.add(resultData);
            }
        }
        if(resultTemp!=null&&resultTemp.size()>0){
            //todo  result.addAll(mergeTo(resultTemp));
            result.addAll(resultTemp);
        }
        return result;
    }

    public  SmoothBumpModel getHasResult(int startIndex,int endIndex, ArrayList<CalculateDataModel>  list){
        float max=0;
        float sum=0;
        int count=0;
        for(int i=startIndex;i<=endIndex;i++){
            CalculateDataModel s=list.get(i);
            sum+=Math.pow(s.getY(),2);
            float a=(float) Math.sqrt(sum/(count+1));
            if(max<Math.abs(a)){
                max=Math.abs(a);
            }
            count++;
        }
        if(max<levelNumbers[0])
            return null;
        SmoothBumpModel smoothBump=new SmoothBumpModel();
        int level=1;
        if(max>levelNumbers[1]){
            if(max<levelNumbers[2]){
                level=2;
            }else if(max<levelNumbers[3]){
                level=3;
            }else if(max<levelNumbers[4]){
                level=4;
            }else{
                level=5;
            }
        }
        smoothBump.setLevel(level);
        smoothBump.setMaxEffectiveValue(max);
        smoothBump.setZone_st(list.get(startIndex).getTime());
        smoothBump.setZone_et(list.get(endIndex).getTime());
        return smoothBump;
    }

    public List<SmoothBumpModel> mergeTo(List<SmoothBumpModel> list){
        int step = 6;
        List<SmoothBumpModel> removeList = new ArrayList<SmoothBumpModel>();
        List<SmoothBumpModel> listTemp=new ArrayList<>();
        if(list!=null&&list.size()>0){
            SmoothBumpModel old=list.get(0);
            listTemp.add(old);
            //Log.d("MainActivity","old.getZone_st()+ old.getZone_et() = " + old.getZone_st()+"   "+ old.getZone_et()+"  "+old.getMaxEffectiveValue());
            for(int i=1;i<list.size();i++){
                SmoothBumpModel temp=list.get(i);
                //Log.d("MainActivity","temp.getZone_st()+ temp.getZone_et() = " + temp.getZone_st()+"   "+ temp.getZone_et()+"  "+temp.getMaxEffectiveValue());
                long time =old.getZone_st();
                if(temp.getZone_st()==time&&temp.getZone_et()==old.getZone_et()){
                    listTemp.remove(old);
                }else if(temp.getZone_et()==old.getZone_et()||temp.getZone_st()<old.getZone_et()){
                    temp.setZone_st(old.getZone_st());
                    listTemp.remove(old);
                }else if(temp.getZone_st()==time){
                    listTemp.remove(old);
                }
                listTemp.add(temp);
                old=temp;
            }
            for (int l=5;l>=2;l--){
                for(int i=0;i<listTemp.size();i++){
                    SmoothBumpModel sb = listTemp.get(i);
                    if(sb.getLevel()==l && !removeList.contains(sb)){
                        long time = sb.getZone_st();
                        for (int z=0;z<listTemp.size();z++){
                            if (Math.abs(listTemp.get(z).getZone_st()-time)<((l-1)*step*1000) && z!=i){
                                removeList.add(listTemp.get(z));
                            }
                        }
                    }
                }
            }
            listTemp.removeAll(removeList);
        }
        return  listTemp;
    }
}
