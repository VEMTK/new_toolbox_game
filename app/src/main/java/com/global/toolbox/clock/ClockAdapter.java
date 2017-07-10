package com.global.toolbox.clock;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.global.toolbox.R;

import java.util.List;


/**
 * Created by xlc on 2016/10/12.
 */
public class ClockAdapter extends BaseAdapter {


    private LayoutInflater layoutInflater;

    private List<Clock> mList;

    private Context mContext;

    public ClockAdapter(Context context, List<Clock> s) {
        layoutInflater = LayoutInflater.from(context);
        this.mList = s;
        this.mContext=context;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = layoutInflater.inflate(R.layout.clock_time_item, null);

            holder.textView = (TextView) convertView.findViewById(R.id.clock_time_text);

            holder.jiange = (TextView) convertView.findViewById(R.id.clock_jiange);

            holder.current_time = (TextView) convertView.findViewById(R.id.clock_current_time);

            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(mList.get(position).getTimes());

        holder.jiange.setText(mList.get(position).getJiange());

        holder.current_time.setText(mList.get(position).getCurrent_time());

        return convertView;
    }
    public final class ViewHolder {

        public TextView textView,jiange,current_time;

    }
}
