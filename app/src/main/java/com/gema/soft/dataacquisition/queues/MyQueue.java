package com.gema.soft.dataacquisition.queues;
import java.util.LinkedList;

public class MyQueue {
    private static MyQueue myQueue;

    private MyQueue() {
    }

    public static MyQueue getInstance(){
        if(myQueue==null){
            synchronized (MyQueue.class){
                if(myQueue==null){
                    myQueue=new MyQueue();
                }
            }
        }
        return myQueue;
    }

    private LinkedList list = new LinkedList();

    public void clear()//销毁队列
    {
        list.clear();
    }
    public boolean QueueEmpty()//判断队列是否为空
    {
        return list.isEmpty();
    }
    public void enQueue(Object o)//进队
    {
        list.addLast(o);
    }
    public Object deQueue()//出队
    {
        try{
            if(!list.isEmpty()&&list.size()>1)
            {
                return list.removeFirst();
            }
        }catch (Exception e){
            return null;//"队列为空";
        }
        return null;
    }
    public int QueueLength()//获取队列长度
    {
        return list==null?0:list.size();
    }
    public Object QueuePeek()//查看队首元素
    {
        return list==null?null:((list.isEmpty()||list.size()==0)?null:list.getFirst());
    }

    public Object getQueueObject(int index){//获取指定信息
        return list.isEmpty()||list.size()==0||list.size()==0?null:list.get(index);
    }
}
