package com.alivc.videochat.demo.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.http.result.LiveItemResult;

import java.util.List;

/**
 * 类的描述: 这是一个条目样式为文本控件的RecyclerView适配器，并且将条目的点击接口实现外放出去了
 */
public class AnchorListAdapter extends RecyclerView.Adapter<AnchorListAdapter.ViewHolder> {
    private List<LiveItemResult> mDataList;
    private OnItemClickListener mItemClickListener;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 条目的样式就是一个文本控件
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_anchor_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.tvName = (TextView) view.findViewById(R.id.tv_name);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        int count = getItemCount();
        if (position >= 0 && position < count) {
            final LiveItemResult itemData = mDataList.get(position);
            holder.tvName.setText(itemData.getName() + "(" + itemData.getUid() + ")");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(position, itemData);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, LiveItemResult itemData);
    }

    public void setDataList(List<LiveItemResult> dataList) {
        this.mDataList = dataList;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }
}
