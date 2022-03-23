package com.gema.soft.dataacquisition.utils;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerConnect implements Runnable {
    private final int SERVER_PORT=10086;
    private ServerSocket mServerSocket;
    private Socket mClient;
    private String mDeviceId;
    private String mDeviceType;
    private String TAG="TcpServerConnect";
    public TcpServerConnect(String aDeviceId, String aDeviceType){
        this.mDeviceId=aDeviceId;
        this.mDeviceType=aDeviceType;
        try{
            String ip= InetAddress.getLocalHost().getHostAddress();
            System.out.println("ip地址是:"+ip);
            //System.out.println(aDeviceId+"型号:"+aDeviceType);
            mServerSocket=new ServerSocket(SERVER_PORT);
            System.out.println("TcpServerConnect"+"建立Socket");
            //listen();
        }catch(IOException e){
            //TODOAuto-generatedcatchblock
            //e.printStackTrace();
            System.out.println("TcpServerConnect"+e.getMessage());
        }
    }
    public void listen(){
        while(true){
            try{
                mClient=mServerSocket.accept();
                //Log.e("TcpServerConnect","在积极的监听");
            }catch(IOException e){
                //TODOAuto-generatedcatchblock
                //e1.printStackTrace();
                System.out.println("TcpServerConnect"+e.getMessage());
            }
        }
    }
    @Override
    public void run(){
        //TODOAuto-generatedmethodstub
        //if(mClient.isConnected()){
        BufferedOutputStream out=null;
        System.out.println("TcpServerConnect"+"开始监听");
        while(true){
            try{
                //Log.e("TcpServerConnect","开始监听");
                mClient=mServerSocket.accept();
                //if(mClient.isConnected()){
                    System.out.println("TcpServerConnect"+"检测到有连接");
                    out=new BufferedOutputStream(mClient.getOutputStream());
                    String recordStr=mDeviceId+"|"+mDeviceType;
                    out.write(recordStr.getBytes("utf-8"));
                    //intlength=recordStr.getBytes().length;
                    //byte[]b=recordStr.getBytes();
                    //out.writeInt(length);
                    //out.write(b);
                    out.flush();
                    //Log.e("TcpServerConnect",recordStr);
                    //out.flush();
                //}
            }catch(Exception e){
                System.out.println("TcpServerConnect"+e.getMessage());
            }finally{
                if(out!=null){
                    try{
                        out.close();
                    }catch(IOException e){
                        //TODOAuto-generatedcatchblock
                        System.out.println("TcpServerConnect"+e.getMessage());
                    }
                }
                if(mServerSocket!=null){
                    try{
                        mServerSocket.close();
                    }catch(IOException e){
                        //TODOAuto-generatedcatchblock
                        System.out.println("TcpServerConnect"+e.getMessage());
                    }
                }
//}
            }
        }
    }

    public void closeSocket(){
        try{
            if(mClient!=null){
                mClient.close();
            }
            if(mServerSocket!=null){
                mServerSocket.close();
            }
        }catch (IOException e){
            Log.d(TAG, "instance initializer: "+e.getMessage());
        }
    }
}
