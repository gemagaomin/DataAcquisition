package com.gema.soft.dataacquisition.utils;

public class USBUtil {
    private static USBUtil usbUtil;

    private USBUtil() {
    }

    public static USBUtil getInstance(){
        if(usbUtil==null){
            synchronized (USBUtil.class){
                if(usbUtil==null){
                    usbUtil=new USBUtil();
                }
            }
        }
        return usbUtil;
    }


}
