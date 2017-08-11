package com.alivc.videochat.demo.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alivc.videochat.demo.http.model.LogInfo;

/**
 * Created by liujianghao on 16-9-22.
 */
public class LogInfoAdapter extends RecyclerView.Adapter<LogInfoAdapter.LogViewHolder> {
    public static class LogItem {
        public static final int AUDIO_ENCODE_BITRATE = 0;       //音频编码码率
        public static final int VIDEO_ENCODE_BITRATE = 1;       //视频编码码率
        public static final int AUDIO_UPLOAD_BITRATE = 2;       //音频上传码率
        public static final int VIDEO_UPLOAD_BITRATE = 3;       //视频上传码率
        public static final int AUDIO_FRAMES_IN_QUEUE = 4;      //队列中音频总帧数
        public static final int VIDEO_FRAMES_IN_QUEUE = 5;      //队列中视频总帧数
        public static final int VIDEO_ENCODE_FRAME_RATE = 6;    //视频编码帧率
        public static final int VIDEO_UPLOAD_FRAME_RATE = 7;    //视频上传帧率
        public static final int VIDEO_CAPTURE_FRAME_RATE = 8;   //视频采集帧率
        public static final int CURRENT_VIDEO_PTS = 9;          //当前上传视频帧PTS
        public static final int CURRENT_AUDIO_PTS = 10;         //当前上传音频帧PTS
        public static final int PREVIOUS_I_FRAME_PTS = 11;      //前一个关键帧PTS
        public static final int VIDEO_ENCODE_FRAMES = 12;        //视频编码总帧数
        public static final int VIDEO_ENCODE_DURATIONS = 13;     //视频编码总时长
        public static final int UPLOAD_PACKETS_SIZE = 14;        //上传Packets的总大小
        public static final int UPLOAD_PACKETS_DURATIONS = 15;   //上传Packets的总时长
        public static final int UPLOAD_VIDEO_FRAMES = 16;        //上传视频的总帧数
        public static final int DROPPED_VIDEO_DURATIONS = 17;    //丢弃视频的总时长
        public static final int VIDEO_CAPTURE_TO_UPLOAD_DELAY = 18;  //视频采集到上传的延时
        public static final int AUDIO_CAPTURE_TO_UPLOAD_DELAY = 19;  //音频采集到上传的延时

        //播放
        public static final int CACHE_VIDEO_FRAME_COUNT = 20;         //缓冲视频帧数
        public static final int CACHE_AUDIO_FRAME_COUNT = 21;         //缓冲音频帧数
        public static final int VIDEO_DOWNLOAD_TO_PLAY_DELAY = 22;   //视频下载到播放延时
        public static final int AUDIO_DOWNLOAD_TO_PLAY_DELAY = 23;   //音频下载到播放延时
        public static final int CACHING_LAST_VIDEO_FRAME_PTS = 24;   //缓冲中最后视频帧PTS
        public static final int CACHING_LAST_AUDIO_FRAME_PTS = 25;   //缓冲中最后音频帧PTS
    }


    SparseArray<LogInfo> mLogInfos = new SparseArray<>();
    private String[] LABELS = null;

    public LogInfoAdapter(Context context) {
        loadLabels(context);
    }

    @Override
    public LogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(com.alivc.videochat.demo.R.layout.layout_item_log_info, parent, false);
        LogViewHolder holder = new LogViewHolder(itemView);
        holder.tvLabel = (TextView) itemView.findViewById(com.alivc.videochat.demo.R.id.tv_label);
        holder.tvValue = (TextView) itemView.findViewById(com.alivc.videochat.demo.R.id.tv_value);
        return holder;
    }

    @Override
    public void onBindViewHolder(LogViewHolder holder, int position) {
        LogInfo logInfo = mLogInfos.get(position);
        if (logInfo != null) {
            holder.tvLabel.setText(logInfo.getLabel());
            holder.tvValue.setText(logInfo.getValue());
        }else {
            holder.tvLabel.setText(LABELS[position]);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 更新value
     * @param key
     * @param value
     */
    public void updateValue(int key, String value) {
        LogInfo logInfo = mLogInfos.get(key);
        if(logInfo == null) {
            logInfo = new LogInfo(LABELS[key], value);
            mLogInfos.put(key, logInfo);
        }else {
            logInfo.setValue(value);
        }
    }

    /**
     * 加载Labels
     * @param context
     */
    public void loadLabels(Context context) {
        LABELS = context.getResources().getStringArray(com.alivc.videochat.demo.R.array.log_labels);
    }


    @Override
    public int getItemCount() {
        return LABELS.length;
    }

    class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;
        TextView tvValue;

        public LogViewHolder(View itemView) {
            super(itemView);
        }
    }
}
