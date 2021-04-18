package com.cretin.tools.cityselect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cretin.tools.cityselect.R;
import com.cretin.tools.cityselect.callback.OnItemClickListener;
import com.cretin.tools.cityselect.model.DataInfoModel;

import java.util.List;

public class HotRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DataInfoModel> mDatas;

    private Context mContext;

    private OnItemClickListener itemClickListener;

    public HotRecyclerViewAdapter(Context context, List<DataInfoModel> data) {
        this.mDatas = data;
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NormalHolder(LayoutInflater.from(mContext).inflate(R.layout.item_layout_hot_city, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        DataInfoModel dataInfoModel = mDatas.get(position);
        NormalHolder realHolder = (NormalHolder) holder;
        realHolder.tvCity.setText(dataInfoModel.getCityName());
        realHolder.tvCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(mDatas.get(position));
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        if (mDatas != null) {
            return mDatas.size();
        }
        return 0;
    }

    private class NormalHolder extends RecyclerView.ViewHolder {
        TextView tvCity;

        public NormalHolder(View itemView) {
            super(itemView);
            tvCity = itemView.findViewById(R.id.tv_city);
        }
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
