package com.example.rock.todo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by rock on 2017. 12. 18..
 */

public class CustomListAdapter extends BaseAdapter implements View.OnTouchListener {
    private ArrayList<NewsItem> listData;
    private LayoutInflater layoutInflater;

    public CustomListAdapter(Context aContext, ArrayList<NewsItem> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        View view;
        ViewHolder vh;

        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            view.setOnTouchListener(this);
            vh = new ViewHolder(view);
            view.setTag(vh);
        } else {
            view = convertView;
            vh = (ViewHolder) view.getTag();
        }

        vh.text.setText(listData.get(position).toString());
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_row_layout, null);
            holder = new ViewHolder(view);
            holder.headlineView = (TextView) convertView.findViewById(R.id.title);
            holder.reporterNameView = (TextView) convertView.findViewById(R.id.reporter);
            holder.reportedDateView = (TextView) convertView.findViewById(R.id.date);
            holder.ddayView = (TextView) convertView.findViewById(R.id.dday);
            holder.barView = (TextView) convertView.findViewById(R.id.bar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.headlineView.setText(listData.get(position).getHeadline());
        holder.reporterNameView.setText(listData.get(position).getReporterName());
        holder.reportedDateView.setText(listData.get(position).getDate());
        switch(listData.get(position).getLabel()) {
            case "None":
                holder.barView.setBackgroundColor(Color.rgb(212, 212, 212));
                break;
            case "Black":
                holder.barView.setBackgroundColor(Color.rgb(0, 0, 0));
                break;
            case "Red":
                holder.barView.setBackgroundColor(Color.rgb(255, 0, 0));
                break;
            case "Yellow":
                holder.barView.setBackgroundColor(Color.rgb(255, 236, 59));
                break;
            case "Green":
                holder.barView.setBackgroundColor(Color.rgb(76, 175, 79));
                break;
            case "Blue":
                holder.barView.setBackgroundColor(Color.rgb(34, 150, 243));
                break;
            case "Purple":
                holder.barView.setBackgroundColor(Color.rgb(125, 75, 204));
                break;
        }
        if (Integer.parseInt(listData.get(position).getDday()) > 0) {
            holder.ddayView.setText(listData.get(position).getDday() + "일 남음");
        }
        else if(Integer.parseInt(listData.get(position).getDday()) == 0){
            holder.ddayView.setText("오늘까지");
            holder.ddayView.setTextColor(Color.rgb(0,0,0));
        }
        else {
            holder.ddayView.setText(-Integer.parseInt(listData.get(position).getDday()) + "일 지남");
            holder.ddayView.setTextColor(Color.rgb(255,0,0));
        }
        return convertView;
    }
    public void remove(int position){
        listData.remove(getItem(position));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ViewHolder vh = (ViewHolder) v.getTag();

        vh.lastTouchedX = event.getX();
        vh.lastTouchedY = event.getY();

        return false;
    }

    static class ViewHolder {
        TextView headlineView;
        TextView reporterNameView;
        TextView reportedDateView;
        TextView ddayView;
        public TextView text;
        public float lastTouchedX;
        public float lastTouchedY;
        public TextView barView;


        public ViewHolder(View v) {
            text = (TextView) v;
        }
    }
}