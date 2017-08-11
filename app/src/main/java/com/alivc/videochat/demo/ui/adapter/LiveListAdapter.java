package com.alivc.videochat.demo.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.app.AppSettings;
import com.alivc.videochat.demo.bi.ServiceBI;
import com.alivc.videochat.demo.bi.ServiceBIFactory;
import com.alivc.videochat.demo.http.model.LiveItemResult;
import com.alivc.videochat.demo.http.model.WatchLiveResult;
import com.alivc.videochat.demo.ui.WatchLiveActivity;
import com.alivc.videochat.demo.uitils.ToastUtils;

import java.util.List;

/**
 * Created by liujianghao on 16-8-10.
 */
public class LiveListAdapter extends RecyclerView.Adapter<LiveListAdapter.LiveViewHolder> {
    private static final String TAG = "LiveListAdapter";
    private List<LiveItemResult> mDataList;
    private AppSettings mSettings;
    private final String FLV;
    private final String RTMP;
    private final String M3U8;
    private final String mUID;

    public LiveListAdapter(Context context, String uid) {
        mSettings = new AppSettings(context);
        FLV = context.getString(R.string.flv);
        RTMP = context.getString(R.string.rtmp);
        M3U8 = context.getString(R.string.m3u8);

        mUID = uid;
    }

    /**
     * update data list
     *
     * @param dataList
     */
    public void setDataList(List<LiveItemResult> dataList) {
        this.mDataList = dataList;
    }

    @Override
    public LiveViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_live, parent, false);
        LiveViewHolder holder = new LiveViewHolder(view);

        holder.mTvName = (TextView) view.findViewById(R.id.tv_live_name);
        holder.mIvCover = (ImageView) view.findViewById(R.id.iv_live_cover);
        holder.mIvAvatar = (ImageView) view.findViewById(R.id.iv_live_avatar);
        return holder;
    }

    @Override
    public void onBindViewHolder(LiveViewHolder holder, int position) {
        final LiveItemResult itemResult = getItem(position);
        if (itemResult != null) {
            holder.mTvName.setText(itemResult.getName() + "(" + itemResult.getUid() + ")");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemResult.getStatus() == LiveItemResult.STATUS_CREATE_NO_STREAM) {
                        ToastUtils.showToast(v.getContext(), R.string.no_stream_tip);
                    } else {
//                        String playFormat = mSettings.getPlayFormat(FLV);
                        String playUrl = "";
//                        if (playFormat.equals(FLV)) {
//                            playUrl = itemResult.getFlvPlayUrl();
//                        } else if (playFormat.equals(RTMP)) {
                            playUrl = itemResult.getRtmpPlayUrl();
//                        } else {
//                            playUrl = itemResult.getM3u8PlayUrl();
//                        }
                        Log.d(TAG, "play url: " + playUrl);
                        gotoWatchLive(v.getContext(),
                                playUrl,
                                itemResult);
                    }
                }
            });
        }
    }

    private void gotoWatchLive(final Context context,
                               final String playUrl,
                               final LiveItemResult itemResult) {
        WatchLiveActivity.startActivity(context,
                playUrl,
                itemResult.getRoomID(),
                itemResult.getName(),
                itemResult.getUid());
        ServiceBIFactory.getLiveServiceBI().watchLive(itemResult.getRoomID(), mUID,
                new ServiceBI.Callback<WatchLiveResult>() {

                    @Override
                    public void onResponse(int code, WatchLiveResult result) {
//                        WatchLiveActivity.startActivity(context,
//                                playUrl,
//                                itemResult.getRoomID(),
//                                itemResult.getName(),
//                                itemResult.getUid(),
//                                result.getConnectModel(),
//                                result.getMNSModel());
                    }

                    @Override
                    public void onFailure(Throwable t) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }


    private LiveItemResult getItem(int position) {
        if (mDataList != null
                && position >= 0
                && position < mDataList.size()) {
            return mDataList.get(position);
        }
        return null;
    }


    public static class LiveViewHolder extends RecyclerView.ViewHolder {
        TextView mTvName;
        ImageView mIvCover;
        ImageView mIvAvatar;


        public LiveViewHolder(View itemView) {
            super(itemView);
        }
    }
}
