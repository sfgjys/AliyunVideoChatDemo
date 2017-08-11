package com.alivc.videochat.demo.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.ui.beans.LiveCommentBean;

import java.util.ArrayList;
import java.util.List;

/**
 * author:杭州短趣网络传媒技术有限公司
 * date:2016/6/27
 * description:DemoActivity
 */
public class LiveCommentAdapter extends RecyclerView.Adapter<LiveCommentAdapter.LiveCommentViewHolder> {
    private static final String TAG = "LiveCommentAdapter";

    private List<LiveCommentBean> mCommentList;

    public LiveCommentAdapter() {
        mCommentList = new ArrayList<>();
    }


    @Override
    public LiveCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_comment, parent, false);
        LiveCommentViewHolder viewHolder = new LiveCommentViewHolder(view);
        viewHolder.mTextView = (TextView) view.findViewById(R.id.tv_comment);
        return viewHolder;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final LiveCommentViewHolder holder, final int position) {
        final LiveCommentBean commentBean = getItem(position);
        if (commentBean != null) {
            holder.mTextView.setText(commentBean.getName()+commentBean.getContent());
//            holder.mTextView.setClickableText(commentBean.getContent());
            holder.mTextView.setTextColor(holder.mTextView.getResources().getColor(R.color.live_comment_name_color));
//            holder.mTextView.setClickableTextColor(holder.mTextView.getResources().getColor(commentBean.getColorResID()));
        }
    }

    public void addComment(LiveCommentBean comment) {
//        int position = mCommentList.size();
        mCommentList.add(comment);
//        notifyItemInserted(position);
    }

    @Override
    public int getItemCount() {
        int count = mCommentList == null ? 0 : mCommentList.size();
        return count;
    }


    private synchronized void remove(String comment) {
        if (mCommentList != null) {
            mCommentList.remove(comment);
            notifyDataSetChanged();
        }
    }


    public LiveCommentBean getItem(int position) {
        if (mCommentList == null) {
            return null;
        } else {
            return mCommentList.get(position);
        }
    }


    public static class LiveCommentViewHolder extends RecyclerView.ViewHolder {
//        PartClickableTextView mTextView;
        TextView mTextView;

        public LiveCommentViewHolder(View itemView) {
            super(itemView);
        }
    }

}