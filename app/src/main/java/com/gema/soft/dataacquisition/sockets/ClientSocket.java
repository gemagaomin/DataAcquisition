package com.gema.soft.dataacquisition.sockets;

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
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

public class ClientSocket {
    private Socket socket;
    private String ip;
    private int port;
    private static ClientSocket clientSocket;
    private  ThreadReadWriterIOSocket threadReadWriterIOSocket;
    private long oldDate;
    private Thread thread;

    private ClientSocket() {
    }

    public static ClientSocket getInstance(){
        if(clientSocket==null){
            synchronized (ClientSocket.class){
                if(clientSocket==null){
                    clientSocket=new ClientSocket();
                }
            }
        }
        return clientSocket;
    }

    public Socket getSocket() {
        return socket;
    }

    public void startConnect(String ip, int port){
        this.ip = ip;
        this.port = port;
        if(thread==null){
            thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        if(socket==null||!socket.isConnected()){
                            socket=new Socket();
                            socket.connect(new InetSocketAddress(ip,port),3000);
                            if(socket.isConnected()){
                                threadReadWriterIOSocket=new ThreadReadWriterIOSocket(clientSocket);
                                threadReadWriterIOSocket.run();
                            }
                        }
                    }catch (IOException e){
                        DataUtil.isSocketOnline=false;
                        MyApplication.closeLockScreen();
                    }
                }
            });
            thread.start();
        }
    }

    public void sendMessage(int flag,byte[] responseData){
        OutputStream outputStream=null;
        InputStream inputStream=null;
        try{
            outputStream.write(GetResponse(responseData,flag));
            inputStream=socket.getInputStream();
            byte[] head=new byte[40];
            int length=inputStream.read(head);
            Header h=null;
            if(length==40){
                h=GetHeader(head,length);
            }
            if(h!=null){
                if(h.length>0){
                    int dataLength=h.length;
                    byte[] data=new byte[dataLength];
                    h.data=data;
                    inputStream.read(data);
                    if(h.flag== WorkTypeSocketEnum.ASKONLINE.getId()){
                        if(data[0]==1){
                            if(oldDate==0){
                                oldDate=new Date().getTime();
                            }else{
                                long newDate=new Date().getTime();
                                if(newDate-oldDate>=15000){//服务器断了
                                    DataUtil.isSocketOnline=false;
                                    clientSocket.close();
                                    MyApplication.closeLockScreen();
                                    return;
                                }
                                DataUtil.isSocketOnline=true;
                            }
                        }
                    }
                }
            }
        }catch (IOException e){

        }finally {
            try{
                if(inputStream!=null)
                    inputStream.close();
                if(outputStream!=null)
                    outputStream.close();
            }catch (IOException e){

            }
        }
    }

    public Header GetHeader(byte[] data, int number)
    {
        Header header = null;
        try{
            if (number >= 40 && data[0] == 0xFC && data[1] == 0xFD)
            {
                header = new Header();
                header.flag = DataUtil.getFlagInt(data,2);
                header.id = new String(Arrays.copyOfRange(data,4,32),"UTF-8");
                header.length = DataUtil.getLengthInt(data,36);
            }
        }catch (UnsupportedEncodingException e){

        }
        return header;
    }

    public byte[] GetResponse(byte[] data,int flag)
    {
        byte[] response = new byte[40 + (data!=null?data.length:0)];
        response[0] = (byte) 0xFC;
        response[1] = (byte) 0xFD;
        try{
            System.arraycopy(MyApplication.UUID.getBytes("UTF-8"),0,response,2,MyApplication.UUID.length());
            System.arraycopy(DataUtil.getFlagByte(flag), 0, response, 34, 2);
            System.arraycopy(DataUtil.getLengthByte((data!=null?data.length:0)), 0, response, 36,4);
            System.arraycopy(data, 0, response, 40, (data!=null?data.length:0));
        }catch (IOException e){

        }
        return response;
    }


    public void close(){
        try{
            if(socket!=null){
                socket.close();
                socket=null;
            }
            if(threadReadWriterIOSocket!=null){
                androidService.ioThreadFlag=false;
                threadReadWriterIOSocket=null;
            }
            if(thread!=null){
                thread.stop();
            }
        }catch (IOException e){

        }
    }


}
