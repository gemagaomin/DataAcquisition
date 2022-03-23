package com.gema.soft.dataacquisition.sockets;

import android.util.Log;

import com.gema.soft.dataacquisition.services.androidService;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.MyApplication;

import java.net.Socket;


public class ThreadReadWriterIOSocket implements Runnable {
    private ServiceSocketListener socket;
    private ClientSocket clientSocket;
    private String TAG="ThreadReadWriterIOSocket";
    public ThreadReadWriterIOSocket( ServiceSocketListener socket) {
        this.socket = socket;
    }
    public ThreadReadWriterIOSocket( ClientSocket socket) {
        this.clientSocket = socket;
    }
    @Override
    public void run() {
        try{
            android.util.Log.e(androidService.TAG, "a client has connected to server!");
            /*pc端发来的数据msg*/
            androidService.ioThreadFlag = true;
            Socket clientSocket=null;
            while (androidService.ioThreadFlag){
                clientSocket=socket.getListenerSocket();
                if(clientSocket==null){
                    androidService.ioThreadFlag=false;
                    DataUtil.isSocketOnline=false;
                    MyApplication.closeLockScreen();
                    return;
                }
                if (clientSocket!=null&&!clientSocket.isConnected()){
                    androidService.ioThreadFlag=false;
                    DataUtil.isSocketOnline=false;
                    socket.close();
                    MyApplication.closeLockScreen();
                    return;
                }
                socket.ReceiverMessage();
            }
        }catch (Exception e){
            Log.d(TAG, "run: "+e.getMessage());
        }
    }
}
