package com.gema.soft.dataacquisition.models;

/**
* @author xqx
* @email djlxqx@163.com
* blog:http://www.cnblogs.com/xqxacm/
* createAt 2017/9/6
* description: 用于获取到数据之后刷新界面 显示数据
*/


public class RefreshDatas {
    private String str;
    private int type;

    public RefreshDatas(String str, int type) {
        this.str = str;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }
}
