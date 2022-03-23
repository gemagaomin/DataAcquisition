package com.gema.soft.dataacquisition.task;

import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

public class MsgManager {
    private static Timer timer = new Timer();
    private static String TAG="MsgManager";
    private static Map<MsgKey, MsgTask> msgTasks = Collections.synchronizedMap(new HashMap<MsgKey, MsgTask>());

    public static void putMsgTask(MsgKey msgKey,MsgTask msgTask) {
        synchronized (msgTasks) {
            msgTasks.put(msgKey, msgTask);
        }
    }

    public static void startMsgTask(MsgKey msgKey,MsgTask msgTask) {
        putMsgTask(msgKey, msgTask);
        timer.schedule(msgTask, 0,msgTask.getDeltaTime());
        Log.d(TAG, "startMsgTask: "+msgKey.getIndex());
    }

    public static MsgTask removeMsgTask(MsgKey msgKey) {
        MsgTask msgTask = null;
        synchronized (msgTasks) {
            msgTask = msgTasks.remove(msgKey);
        }
        Log.d(TAG, "removeMsgTask: "+msgKey.getIndex());
        return msgTask;
    }

    public static boolean stopMsgTask(MsgKey msgKey) {
        MsgTask msgTask = removeMsgTask(msgKey);
        Log.d(TAG, "stopMsgTask: "+msgKey.getIndex());
        if (msgTask != null){
            msgTask.cancel();
            return true;
        }
        return false;
    }
}
