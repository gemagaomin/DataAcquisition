package com.gema.soft.dataacquisition.sockets;

import android.content.Context;
import android.util.Log;

import com.gema.soft.dataacquisition.activitys.LockScreenActivity;
import com.gema.soft.dataacquisition.enums.WorkTypeSocketEnum;
import com.gema.soft.dataacquisition.models.Header;
import com.gema.soft.dataacquisition.services.androidService;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.MyApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ServiceSocketListener {
    private static ServiceSocketListener serviceSocketListener;
    private Socket listenerSocket;
    private int port=10086;
    private ServerSocket serverSocket;
    private Timer timer;
    private String TAG="ServiceSocketListener";
    private Thread thread;
    private ThreadReadWriterIOSocket threadReadWriterIOSocket;
    private long oldDate=0;
    public boolean isCan;
    private Context context;
    private InputStream inputStream;
    private OutputStream outputStream;

    private ServiceSocketListener(Context context) {
        this.context=context;

    }

    public Socket getListenerSocket() {
        return listenerSocket;
    }

    public static  ServiceSocketListener getInstance(Context context){
        if(serviceSocketListener==null){
            synchronized (ServiceSocketListener.class){
                if(serviceSocketListener==null){
                    serviceSocketListener=new ServiceSocketListener(context);
                }
            }
        }
        return serviceSocketListener;
    }

    public void startServerSocket(){
            if(serverSocket==null){
                thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            serverSocket=new ServerSocket();
                            serverSocket.bind(new InetSocketAddress(port));
                            while (isCan){
                                if(serverSocket!=null){
                                    listenerSocket=serverSocket.accept();
                                    threadReadWriterIOSocket=new ThreadReadWriterIOSocket(serviceSocketListener);
                                    threadReadWriterIOSocket.run();
                                }
                            }
                        }catch (IOException e){
                            Log.d(TAG, "run: "+e.getMessage());
                        }
                    }
                });
                thread.start();
            }
    }

    private void startTimer(){
        if(timer==null){
            timer=new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(new Date().getTime()-oldDate>1000&&oldDate>0){
                        DataUtil.isSocketOnline=false;
                        close();
                        MyApplication.closeLockScreen();
                    }else{
                        DataUtil.isSocketOnline=true;
                        MyApplication.OpenLockScreen(context);
                    }
                }
            },0,1000);
        }
    }

    public void endTimer(){
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
    }

    public void ReceiverMessage(){
         try{
            inputStream =listenerSocket.getInputStream();
            byte[] head=new byte[40];
            Header h=null;
            int length=inputStream.read(head,0,40);
            Log.d(TAG, "ReceiverMessage: length"+length);
            if(length>0){
                long newTime=new Date().getTime();
                if(oldDate==0){
                    startTimer();
                }
                if(oldDate<newTime){
                    oldDate=newTime;
                    if(timer==null){
                        startTimer();
                    }
                }
                if(length==40){
                    h=GetHeader(head,length);
                    if(h!=null){
                        byte[] data=new byte[h.length];
                        inputStream.read(data);
                        h.data=data;
                    }
                }
            }
            if(h!=null){
                SendMessage(new byte[]{1},WorkTypeSocketEnum.ASKONLINE.getId(),h);
            }
        }catch (IOException e){
            androidService.ioThreadFlag=false;
            DataUtil.isSocketOnline=false;
            MyApplication.closeLockScreen();
            Log.e(TAG, "ReceiverMessage: "+ e.getMessage());
        }
    }
    public void SendMessage(byte[] response,int flag,Header header){
        try{
            Log.d(TAG, "SendMessage: ");
            outputStream =listenerSocket.getOutputStream();
            outputStream.write(GetResponse(response,flag,header));
            MyApplication.OpenLockScreen(context);
        }catch (IOException e){
            Log.d(TAG, "SendMessage: "+e.getMessage());
            androidService.ioThreadFlag=false;
            DataUtil.isSocketOnline=false;
            MyApplication.closeLockScreen();
        }
    }

    public Header GetHeader(byte[] data, int number)
    {
        Header header = null;
        try{
            if (number >= 40 &&(data[0] & 0xFF) == 0xFA && (data[1] & 0xFF)  ==0xFB)
            {
                header = new Header();
                header.id = new String(Arrays.copyOfRange(data,2,32),"UTF-8");
                header.flag = DataUtil.getFlagInt(data,34);
                header.length =  DataUtil.getLengthInt(data,36);
            }
        }catch (UnsupportedEncodingException e){

        }
        return header;
    }

    public byte[] GetResponse(byte[] data,int flag,Header header)
    {
        byte[] response = new byte[40 + (data!=null?data.length:0)];
        response[0] = (byte) 0xFA;
        response[1] = (byte) 0xFB;
        try{
            System.arraycopy(header.id.getBytes("UTF-8"),0,response,2,header.id.length());
            System.arraycopy( DataUtil.getFlagByte(flag), 0, response, 34, 2);
            System.arraycopy( DataUtil.getLengthByte((data!=null?data.length:0)), 0, response, 36,4);
            System.arraycopy(data, 0, response, 40, (data!=null?data.length:0));
        }catch (IOException e){

        }
        return response;
    }


    public void close(){
        try{
            isCan=false;
            endTimer();
            if(inputStream!=null){
                inputStream.close();
                inputStream=null;
            }
            if(outputStream!=null){
                outputStream.close();
                outputStream=null;
            }
            if(serverSocket!=null){
                serverSocket.close();
                serverSocket=null;
            }
            if(listenerSocket!=null){
                listenerSocket.close();
                listenerSocket=null;
            }
            if(threadReadWriterIOSocket!=null){
                androidService.ioThreadFlag=false;
                threadReadWriterIOSocket=null;
            }
            if(thread!=null){
                thread.destroy();
                thread=null;
            }
            oldDate=0;
        }catch (Exception e){
            Log.d(TAG, "close: "+e.getMessage());
        }
    }


}
