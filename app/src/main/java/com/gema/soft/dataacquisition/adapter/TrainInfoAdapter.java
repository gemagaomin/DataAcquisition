package com.gema.soft.dataacquisition.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gema.soft.dataacquisition.R;
import com.gema.soft.dataacquisition.models.TrainInfoModel;

import java.util.List;

public class TrainInfoAdapter extends BaseAdapter {
    private List<TrainInfoModel> list;
    private Context context;
    private TrainInfoModel selectedOne;


    public TrainInfoAdapter(List<TrainInfoModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public TrainInfoModel getSelectedOne() {
        return selectedOne;
    }

    public void setSelectedOne(TrainInfoModel selectedOne) {
        this.selectedOne = selectedOne;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TrainInfoModel trainInfoModel=list.get(position);
        TrainInfoHolder trainInfoHolder;
        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.item_train_info,parent,false);
            trainInfoHolder=new TrainInfoHolder();
            trainInfoHolder.trainTV=convertView.findViewById(R.id.item_train_into_train);
            trainInfoHolder.linearLayout=convertView.findViewById(R.id.item_train_info_ll);
            trainInfoHolder.personDriverTV=convertView.findViewById(R.id.item_train_into_person_sj);
            trainInfoHolder.personADriverTV=convertView.findViewById(R.id.item_train_into_person_fsj);
            convertView.setTag(trainInfoHolder);
        }else{
            trainInfoHolder=(TrainInfoHolder) convertView.getTag();
        }

        trainInfoHolder.trainTV.setText(trainInfoModel.trainTypeIdName()+"  "+trainInfoModel.getTrainOrder());
        trainInfoHolder.personDriverTV.setText("司  机："+trainInfoModel.getDriverName()+"("+trainInfoModel.getDriverId()+")");
        trainInfoHolder.personADriverTV.setText("副司机："+trainInfoModel.getAssistantDriverName()+"("+trainInfoModel.getAssistantDriverId()+")");
        int colorInt=convertView.getResources().getColor(R.color.white);
        if(selectedOne!=null&&selectedOne.equals(trainInfoModel)){
            colorInt=convertView.getResources().getColor(R.color.select_item_light);
        }
        trainInfoHolder.linearLayout.setBackgroundColor(colorInt);
        return convertView;
    }

    class TrainInfoHolder{
        TextView trainTV;
        TextView personDriverTV;
        TextView personADriverTV;
        LinearLayout linearLayout;
    }
}
