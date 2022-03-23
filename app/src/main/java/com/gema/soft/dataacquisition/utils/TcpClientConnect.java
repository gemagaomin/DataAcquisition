package com.gema.soft.dataacquisition.utils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpClientConnect implements Runnable {
    private Socket socket;
    private int port=11000;
    private String ip="127.0.0.1";
    private String TAG="TcpClientConnect";

    public TcpClientConnect() {
    }

    @Override
    public void run() {
        OutputStream outputStream=null;
        InputStream inputStream=null;
        try{
            if(socket==null){
                socket=new Socket(ip,port);
            }
            boolean isCon=socket.isConnected();
            if(!isCon){
                socket.connect(socket.getLocalSocketAddress());
            }
            if(isCon){
                outputStream.write("apptext<EOF>".getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();
                inputStream=socket.getInputStream();
                byte[] data=new byte[4];
                inputStream.read(data);
                inputStream.close();
                Log.d(TAG, "run: "+new String(data));
            }
        }catch (IOException e){
            Log.e(TAG, "run: "+e.getMessage() );
        }
        finally {
            try{
                if(outputStream!=null){
                    outputStream.close();
                    outputStream=null;
                }
                if(inputStream!=null){
                    inputStream.close();
                    inputStream=null;
                }
                if(socket!=null){
                    socket.close();
                    socket=null;
                }
            }catch (IOException e){
                Log.e(TAG, "run: "+e.getMessage() );
            }
        }
    }
}
