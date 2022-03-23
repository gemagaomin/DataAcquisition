package com.gema.soft.dataacquisition.utils;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
    private static PropertiesUtil propertiesUtil;
    private Properties props ;
    private PropertiesUtil() {
        try{
            InputStream in = PropertiesUtil.class.getResourceAsStream("/assets/system.properties");
            if(in!=null){
                props= new Properties();
                props.load(in);
            }
        }catch (Exception e){

        }
    }
    public static PropertiesUtil getInstance(){
        if(propertiesUtil==null){
            synchronized (PropertiesUtil.class){
                if(propertiesUtil==null){
                    propertiesUtil=new PropertiesUtil();
                }
            }
        }
        return propertiesUtil;
    }

    public String getProperty(String key){
        if(props!=null){
            return props.getProperty(key);
        }
        return null;
    }
}
