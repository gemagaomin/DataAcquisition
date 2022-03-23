package com.gema.soft.dataacquisition.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gema.soft.dataacquisition.R;

import java.util.ArrayList;
import java.util.List;



/**
 * @author xqx
 * @email djlxqx@163.com
 * blog:http://www.cnblogs.com/xqxacm/
 * createAt 2017/9/6
 * description:  扫描得到的蓝牙设备列表适配器
 */

public class ScanDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<String> list;
    private List<String> selectedList;
    private View view;
    private OnItemClickListener onItemClickListener;
    public ScanDeviceAdapter(ArrayList<String> datas,ArrayList<String> selectedList, Context context) {
        this.context=context;
        this.list=datas;
        this.selectedList=selectedList;
    }

    public List<String> getSelectedList() {
        return selectedList;
    }

    public void setSelectedList(List<String> selectedList) {
        this.selectedList = selectedList;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        view= LayoutInflater.from(context).inflate(R.layout.item_device,parent,false);
        holder=new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(list!=null&&list.size()>0){
            Holder myHolder=(Holder) holder;
            String one=list.get(position).trim();
            myHolder.getMesTv().setText(one);
            TextView tt=myHolder.getStatusTv();
            String str="未接收";
            if(selectedList!=null&&selectedList.size()>0){
                for (int i=0;i<selectedList.size();i++){
                    String ss=selectedList.get(i);
                    if(one.indexOf(ss)!=-1||one.equals(ss)){
                        str="已选择";
                    }
                }
            }

            tt.setText(str);
            tt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v,position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list!=null?list.size():0;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    class Holder extends RecyclerView.ViewHolder {
        private TextView mesTv;
        private TextView statusTv;

        public Holder(View view) {
            super(view);
            mesTv=view.findViewById(R.id.txtMac);
            statusTv=view.findViewById(R.id.txtState);
        }

        public TextView getMesTv() {
            return mesTv;
        }

        public void setMesTv(TextView mesTv) {
            this.mesTv = mesTv;
        }

        public TextView getStatusTv() {
            return statusTv;
        }

        public void setStatusTv(TextView statusTv) {
            this.statusTv = statusTv;
        }
    }
}