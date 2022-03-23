package com.gema.soft.dataacquisition.pojo;


import com.gema.soft.dataacquisition.models.PersonModel;

import java.io.Serializable;

public class PersonPojo implements Serializable {
    private String personId;
    private String personName;
    private String unitId;
    private String unitName;
    private String depName;//小单位
    private String selectUnitId;
    private String band;
    private boolean select=false;

    public PersonPojo() {
    }

    public PersonModel getPersonModel(){
        PersonModel personModel=new PersonModel();
        personModel.setPersonId(this.personId      );
        personModel.setPersonName(this.personName    );
        personModel.setUnitId(this.unitId        );
        personModel.setUnitName(this.unitName      );
        personModel.setDepName(this.depName       );
        personModel.setSelectUnitId(this.selectUnitId  );
        personModel.setBand(this.band          );
        return personModel;
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

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
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

    public String getSelectUnitId() {
        return selectUnitId;
    }

    public void setSelectUnitId(String selectUnitId) {
        this.selectUnitId = selectUnitId;
    }

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
