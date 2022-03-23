package com.gema.soft.dataacquisition.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;


import com.gema.soft.dataacquisition.pojo.PersonPojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PersonModel implements Serializable {
    private String personId;
    private String personName;
    private String unitId;
    private String unitName;
    private String depName;//小单位
    private String selectUnitId;
    private String band;

    public PersonModel() {
    }

    public static List<PersonModel> getPersonListByString(String str, List<PersonModel> personList){
        List<PersonModel> list=new ArrayList<>();
        if(TextUtils.isEmpty(str)){
            list.addAll(personList);
        }else{
            for(int i=0,num=personList.size();i<num;i++){
                PersonModel personModel=personList.get(i);
                String id=personModel.getPersonId();
                String name=personModel.getPersonName();
                if((str.length()<=id.length()&&id.indexOf(str)!=-1)||(str.length()<=name.length()&&name.indexOf(str)!=-1)){
                    list.add(personModel);
                }
            }
        }
        return list;
    }

    public static List<PersonModel> getPersonListByBand(String band,List<PersonModel> list){
        List<PersonModel> retList=new ArrayList<>();
        if(TextUtils.isEmpty(band)){
            retList.addAll(list);
        }else{
            if(list!=null&&list.size()>0){
                for (PersonModel o:list
                ) {
                    if(band.equals(o.getBand())){
                        retList.add(o);
                    }
                }
            }
        }
        return retList;
    }

    public PersonModel(Cursor cursor) {
        this.personId=cursor.getString(cursor.getColumnIndex("personid"));
        this.personName=cursor.getString(cursor.getColumnIndex("personname"));
        this.unitId=cursor.getString(cursor.getColumnIndex("unitid"));
        this.unitName=cursor.getString(cursor.getColumnIndex("unitname"));
        this.band=cursor.getString(cursor.getColumnIndex("band"));
        this.depName=cursor.getString(cursor.getColumnIndex("depname"));
    }

    public ContentValues getContentValues(PersonModel personModel){
        ContentValues contentValues=new ContentValues();
        contentValues.put("personid",personModel.getPersonId());
        contentValues.put("personname",personModel.getPersonName());
        contentValues.put("unitid",personModel.getUnitId());
        contentValues.put("unitname",personModel.getUnitName());
        contentValues.put("band",personModel.getBand());
        contentValues.put("depname",personModel.getBand());
        return contentValues;
    }

    public PersonPojo getPersonPojo(String personId){
        PersonPojo personPojo=new PersonPojo();
        personPojo.setPersonId(this.personId      );
        personPojo.setPersonName(this.personName    );
        personPojo.setUnitId(this.unitId        );
        personPojo.setUnitName(this.unitName      );
        personPojo.setDepName(this.depName       );
        personPojo.setSelectUnitId(this.selectUnitId  );
        personPojo.setBand(this.band          );
        personPojo.setSelect(false);
        if(!TextUtils.isEmpty(personId)&&personId.equals(this.personId)){
            personPojo.setSelect(true);
        }
        return personPojo;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getSelectUnitId() {
        return selectUnitId;
    }

    public void setSelectUnitId(String selectUnitId) {
        this.selectUnitId = selectUnitId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getDepName() {
        return depName;
    }

    public void setDepName(String depName) {
        this.depName = depName;
    }

    @Override
    public String toString() {
        return "PersonModel{" +
                "personId='" + personId + '\'' +
                ", personName='" + personName + '\'' +
                ", unitId='" + unitId + '\'' +
                ", unitName='" + unitName + '\'' +
                ", depName='" + depName + '\'' +
                ", selectUnitId='" + selectUnitId + '\'' +
                ", band='" + band + '\'' +
                '}';
    }
}
