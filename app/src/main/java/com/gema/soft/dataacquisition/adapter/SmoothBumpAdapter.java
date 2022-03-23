package com.gema.soft.dataacquisition.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gema.soft.dataacquisition.R;
import com.gema.soft.dataacquisition.models.SmoothBumpModel;
import com.gema.soft.dataacquisition.utils.DataUtil;
import com.gema.soft.dataacquisition.utils.DateUtil;
import java.util.List;

public class SmoothBumpAdapter extends BaseAdapter {
    private List list;
    private Context context;
    private int type=-1;
    private String TAG="SmoothBumpAdapter";

    public SmoothBumpAdapter(List list, Context context,int type) {
        this.list = list;
        this.context = context;
        this.type=type;
    }

    @Override
    public int getCount() {
        return list!=null?list.size():0;
    }

    @Override
    public Object getItem(int position) {
        return list!=null&&list.size()>position?list.get(position):null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SmoothBumpHolder smoothBumpHolder;
        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.item_calculate_smooth_bump_result,parent,false);
            smoothBumpHolder=new SmoothBumpHolder();
            smoothBumpHolder.linearLayout=convertView.findViewById(R.id.item_calculate_ll);
            smoothBumpHolder.indexTV=convertView.findViewById(R.id.item_calculate_index_tv);
            smoothBumpHolder.stTimeTV=convertView.findViewById(R.id.item_calculate_st_time_tv);
            smoothBumpHolder.levelTV=convertView.findViewById(R.id.item_calculate_level_tv);
            if(type==0){
                ViewGroup.LayoutParams layoutParams= smoothBumpHolder.linearLayout.getLayoutParams();
                layoutParams.height=100;
                smoothBumpHolder.linearLayout.setLayoutParams(layoutParams);
                Resources resources=convertView.getResources();
                smoothBumpHolder.levelTV.setBackground(resources.getDrawable(R.drawable.dark_textview_border_right_top_bottom));
                smoothBumpHolder.levelTV.setTextColor(resources.getColor(R.color.white));
                smoothBumpHolder.stTimeTV.setBackground(resources.getDrawable(R.drawable.dark_textview_border));
                smoothBumpHolder.stTimeTV.setTextColor(resources.getColor(R.color.white));
                smoothBumpHolder.indexTV.setBackground(resources.getDrawable(R.drawable.dark_textview_border_left_top_bottom));
                smoothBumpHolder.indexTV.setTextColor(resources.getColor(R.color.white));
            }
            convertView.setTag(smoothBumpHolder);
        }else{
            smoothBumpHolder=(SmoothBumpHolder)convertView.getTag();
        }
        if(type==0){
            List<String> heads=(List<String>)list.get(position);
            smoothBumpHolder.indexTV.setText(heads.get(0));
            smoothBumpHolder.stTimeTV.setText(heads.get(1));
            smoothBumpHolder.levelTV.setText(heads.get(2));
        }else{
            Log.d(TAG, "getView: "+position);
            SmoothBumpModel smoothBumpModel=(SmoothBumpModel)list.get(position);
            smoothBumpHolder.indexTV.setText((position+1)+"");
            smoothBumpHolder.stTimeTV.setText(DateUtil.getInstance().getDataLong(smoothBumpModel.getZone_st()));
            smoothBumpHolder.levelTV.setText(DataUtil.smoothLevelMap.get(smoothBumpModel.getLevel()));
        }
        return convertView;
    }

    class SmoothBumpHolder{
        private LinearLayout linearLayout;
        private TextView indexTV;
        private TextView stTimeTV;
        private TextView levelTV;
    }
}
