package com.gema.soft.dataacquisition.task;

import com.gema.soft.dataacquisition.utils.DataUtil;

public class MsgProcessor {
  /**
   * 处理超时的消息
   * @param index
   */
  public void dealOverTimeMsg(int index){
    switch (index){
      case DataUtil.TASK_TYPE_WRITHE:
        writheData();
        break;
      case DataUtil.TASK_TYPE_READ:
        readData();
        break;
      case DataUtil.TASK_TYPE_GET_DATA_FROM_BLUETOOTH:
        getDataFromBluetooth();
        break;
    }
  }

  private void writheData(){

  }
  private void readData(){

  }

  private void getDataFromBluetooth(){

  }
}
