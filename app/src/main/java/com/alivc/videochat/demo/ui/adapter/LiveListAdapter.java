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
import com.alivc.videochat.demo.http.result.LiveItemResult;
import com.alivc.videochat.demo.ui.WatchLiveActivity;
import com.alivc.videochat.demo.uitils.ToastUtils;

import java.util.List;

/**
 * 类的描述: 直播列表的适配器
 */
public class LiveListAdapter extends RecyclerView.Adapter<LiveListAdapter.LiveViewHolder> {
    private static final String TAG = "LiveListAdapter";
    private List<LiveItemResult> mDataList;
    private final String mUID;

    public LiveListAdapter(Context context, String uid) {
        mUID = uid;
    }

    /**
     * 方法描述: 更新列表的数据源
     */
    public void setDataList(List<LiveItemResult> dataList) {
        this.mDataList = dataList;
    }

    // **************************************************** Holder相关 ****************************************************

    @Override
    public LiveViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_live, parent, false);
        return new LiveViewHolder(view);
    }

    public static class LiveViewHolder extends RecyclerView.ViewHolder {
        TextView mTvName;
        ImageView mIvCover;
        ImageView mIvAvatar;

        public LiveViewHolder(View itemView) {
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tv_live_name);
            mIvCover = (ImageView) itemView.findViewById(R.id.iv_live_cover);
            mIvAvatar = (ImageView) itemView.findViewById(R.id.iv_live_avatar);
        }
    }

    // --------------------------------------------------------------------------------------------------------

    private LiveItemResult getItem(int position) {
        if (mDataList != null && position >= 0 && position < mDataList.size()) {
            return mDataList.get(position);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(LiveViewHolder holder, int position) {
        // 获取item对应的数据对象
        final LiveItemResult itemResult = getItem(position);
        if (itemResult != null) {
            holder.mTvName.setText(itemResult.getName() + "(" + itemResult.getUid() + ")");
            //  holder.itemView 获取的是整个Item控件
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemResult.getStatus() == LiveItemResult.STATUS_CREATE_NO_STREAM) {
                        ToastUtils.showToast(v.getContext(), R.string.no_stream_tip);
                    } else {
                        String playUrl = "";
                        playUrl = itemResult.getRtmpPlayUrl();
                        Log.d(TAG, "rtmpPlay url: " + playUrl);
                        // 获取了rtmp格式的播放流，并请求网络开启播放
                        gotoWatchLive(v.getContext(), playUrl, itemResult);
                    }
                }
            });
        }
    }

    /**
     * 方法描述: 点击列表item调用本方法开启观看直播界面
     */
    private void gotoWatchLive(final Context context, final String playUrl, final LiveItemResult itemResult) {
        WatchLiveActivity.startActivity(context, playUrl, itemResult.getRoomID(), itemResult.getName(), itemResult.getUid());
        /*ServiceBIFactory.getLiveServiceBI().watchLive(itemResult.getRoomID(), mUID,
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
                });*/
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

}
