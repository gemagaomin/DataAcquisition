package com.gema.soft.dataacquisition.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gema.soft.dataacquisition.R;

import java.util.List;

public class HzAdapter extends BaseAdapter  {
    private List<String> hzList;
    private Context context;
    private String selectedOne;

    public HzAdapter(List<String> hzList, Context context) {
        this.hzList = hzList;
        this.context = context;
    }

    public void setSelectedOne(String selectedOne) {
        this.selectedOne = selectedOne;
    }

    @Override
    public int getCount() {
        return hzList==null?0:hzList.size();
    }

    @Override
    public Object getItem(int position) {
        return hzList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        String str=hzList.get(position);
        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.item_setting_hz,parent,false);
            holder=new Holder();
            holder.textView=convertView.findViewById(R.id.item_setting_hz);
            convertView.setTag(holder);
        }else{
            holder=(Holder)convertView.getTag();
        }
        holder.textView.setText(str);
        int colorInt=convertView.getResources().getColor(R.color.white);
        if(!TextUtils.isEmpty(selectedOne) &&selectedOne.equals(str)){
            colorInt=convertView.getResources().getColor(R.color.select_item_light);
        }
        holder.textView.setBackgroundColor(colorInt);
        return convertView;
    }

    class Holder{
        TextView textView;
    }
}
