package com.gema.soft.dataacquisition.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.gema.soft.dataacquisition.R;
import com.gema.soft.dataacquisition.models.TrainTypeModel;

import java.util.ArrayList;
import java.util.List;

public class TrainTypeAutoTextViewAdapter extends BaseAdapter implements Filterable {
    List<TrainTypeModel> list;
    Context context;
    List<TrainTypeModel> tempList=new ArrayList<>();

    public TrainTypeAutoTextViewAdapter(List<TrainTypeModel> list, Context context) {
        this.list = list;
        this.context = context;
        this.tempList.addAll(list);
    }

    @Override
    public int getCount() {
        return list!=null?list.size():0;
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
        Holder holder=null;
        TrainTypeModel trainTypeModel=list.get(position);
        if(convertView==null){
            holder=new Holder();
            convertView= LayoutInflater.from(context).inflate(R.layout.item_work_search_common,parent,false);
            holder.textView=(TextView)convertView.findViewById(R.id.search_text_view);
            holder.textView.setText(trainTypeModel.getTrainTypeName());
            convertView.setTag(holder);
        }else{
            holder=(Holder) convertView.getTag();
            holder.textView.setText(trainTypeModel.getTrainTypeName());
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter=new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results=new FilterResults();
                List<TrainTypeModel> addList=new ArrayList<TrainTypeModel>();
                if(TextUtils.isEmpty(constraint)){
                    addList.addAll(tempList);
                }else{
                    int num=constraint.length();
                    for (TrainTypeModel o:tempList
                         ) {
                        if(o.getTrainTypeName().length()>num&&constraint.equals(o.getTrainTypeName().substring(0,num))){
                            addList.add(o);
                        }
                    }
                }
                results.values=addList;
                results.count=addList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list.clear();
                list.addAll((List<TrainTypeModel>) results.values);
                notifyDataSetChanged();
            }
        };
        return filter;
    }

    class Holder{
        TextView textView;
    }
}
