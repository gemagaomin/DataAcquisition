package com.gema.soft.dataacquisition.utils;

import android.text.TextUtils;

public class StringUtil {
    private static StringUtil stringUtil;
    public static final String RESULT_ERROR="2";
    public static final String RESULT_TRUE="1";
    public static final String RESULT_FALSE="0";
    private StringUtil() {
    }

    public static StringUtil getInstance(){
        if(stringUtil==null){
            synchronized (StringUtil.class){
                if(stringUtil==null){
                    stringUtil=new StringUtil();
                }
            }
        }
        return stringUtil;
    }

    /**
     * 比较两个数字型字符串大小
     * @param one
     * @param two
     * @return RESULT_TRUE:one>two;RESULT_FALSE:one<two;RESULT_ERROR:数据为空；
     */
    public static String  CompareSize(String one,String two){
        if(TextUtils.isEmpty(one)){
            return RESULT_TRUE;
        }
        if(TextUtils.isEmpty(two)){
            return RESULT_ERROR;
        }
        int oneInt=Integer.valueOf(one);
        int twoInt=Integer.valueOf(two);
        if(oneInt>twoInt){
            return RESULT_TRUE;
        }
        return RESULT_FALSE;
    }

    public static String  CompareDateSize(String one,String two){
        if(TextUtils.isEmpty(one)){
            return RESULT_TRUE;
        }
        if(TextUtils.isEmpty(two)){
            return RESULT_ERROR;
        }
        long oneLong=Long.valueOf(one.replaceAll("[-\\s:]",""));
        long twoLong=Long.valueOf(two.replaceAll("[-\\s:]",""));
        if(oneLong>twoLong){
            return RESULT_TRUE;
        }
        return RESULT_FALSE;
    }

    public static String getString(String str){
        if(TextUtils.isEmpty(str)){
            return "";
        }
        return str;
    }

    public static boolean isNotNull(String str){
        if(str==null||str.length()<=0)
            return false;
        return true;
    }

}
