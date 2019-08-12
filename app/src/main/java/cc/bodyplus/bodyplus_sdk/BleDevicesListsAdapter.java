package cc.bodyplus.bodyplus_sdk;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cc.bodyplus.sdk.ble.utils.MyBleDevice;


/**
 * Created by shihu.wang on 2017/3/22.
 * Email shihu.wang@bodyplus.cc
 */

public class BleDevicesListsAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<MyBleDevice> mList = new ArrayList<>();

    public BleDevicesListsAdapter(Context context) {
        mContext = context;

    }

    public void setData(ArrayList<MyBleDevice> lists){
        if(lists == null){
            this.mList.clear();
        }else {
            this.mList = lists;
        }
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        if (mList!= null &&  mList.size() > 0) {
            return mList.size();
        }
        return 0;
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
        DialogViewHolder holder;
        if (convertView == null) {
            holder = new DialogViewHolder();
            convertView = View.inflate(mContext, R.layout.item_device_list, null);
            holder.device_sn = (TextView) convertView.findViewById(R.id.device_sn);
            holder.device_check = (CheckBox) convertView.findViewById(R.id.device_check);
            holder.device_rs = (ImageView) convertView.findViewById(R.id.device_rs);
            holder.device_name = (TextView) convertView.findViewById(R.id.device_name);
            convertView.setTag(holder);
        } else {
            holder = (DialogViewHolder) convertView.getTag();
        }
        holder.device_sn.setText(String.format(mContext.getResources().getString(R.string.equipment_sn),mList.get(position).getDeviceSn()));
        holder.device_name.setText(mList.get(position).getDeviceName());
        if (mList.get(position).getRssi() < -90) {
            holder.device_rs.setImageResource(R.drawable.device_rs1);
        } else if (mList.get(position).getRssi() < -70) {
            holder.device_rs.setImageResource(R.drawable.device_rs2);
        } else if (mList.get(position).getRssi() < -50) {
            holder.device_rs.setImageResource(R.drawable.device_rs3);
        } else {
            holder.device_rs.setImageResource(R.drawable.device_rs3);
        }
        return convertView;
    }

    public class DialogViewHolder {
        public TextView device_sn;
        public TextView device_name;
        public CheckBox device_check;
        public ImageView device_rs;
    }

}
