package com.alivc.videochat.demo.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.base.BaseActivity;
import com.alivc.videochat.demo.im.ImManager;
import com.alivc.videochat.demo.im.model.MessageType;
import com.alivc.videochat.demo.im.model.MsgDataComment;
import com.alivc.videochat.demo.im.model.MsgDataLike;
import com.alivc.videochat.demo.im.ImHelper;
import com.alivc.videochat.demo.ui.adapter.LiveCommentAdapter;
import com.alivc.videochat.demo.ui.beans.LiveCommentBean;
import com.alibaba.view.BubblingView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liujianghao on 16-7-28.
 * 交互操作的Fragment（评论、点赞等）
 */
public class InteractionFragment extends Fragment {
    private static final String TAG = "ActionFragment";
    private BubblingView mBubblingView;
    private RecyclerView mCommentView;
    private TextView mTvName;

    private int[] images = {
            R.drawable.heart0,
            R.drawable.heart1,
            R.drawable.heart2,
            R.drawable.heart3,
            R.drawable.heart4,
            R.drawable.heart5,
            R.drawable.heart6,
            R.drawable.heart7,
            R.drawable.heart8
    };
    int i = 0;
    LiveCommentAdapter mAdapter;
    LinearLayoutManager mCommentManager;
    private Fragment mBottomFragment;


    private String mUID;
    private String mAnchorName;


    private ImManager mImManger;

    public void initArgs() {
        Bundle args = getArguments();
        mAnchorName = args.getString(ExtraConstant.EXTRA_NAME);
        mUID = ((BaseActivity) getActivity()).getUid();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArgs();

        if (mBottomFragment != null) {
            getChildFragmentManager().beginTransaction().add(R.id.bottom_container, mBottomFragment).commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_interaction, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBubblingView = (BubblingView) view.findViewById(R.id.bv_like);
        mCommentView = (RecyclerView) view.findViewById(R.id.rv_comment);
        mTvName = (TextView) view.findViewById(R.id.tv_name);

        mTvName.setText(mAnchorName + "(" + mUID + ")");

        mCommentView.setHasFixedSize(true);
        mAdapter = new LiveCommentAdapter();
        mCommentManager = new LinearLayoutManager(getActivity());
        mCommentManager.setStackFromEnd(true);
        mCommentManager.setSmoothScrollbarEnabled(true);
        mCommentView.setLayoutManager(mCommentManager);
        mCommentView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mImManger != null) {
            mImManger.register(MessageType.LIKE, mLikeFunc, MsgDataLike.class);
            mImManger.register(MessageType.COMMENT, mCommentFunc, MsgDataComment.class);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mImManger != null) {
            mImManger.unRegister(MessageType.COMMENT);
            mImManger.unRegister(MessageType.LIKE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void setBottomFragment(Fragment fragment) {
        this.mBottomFragment = fragment;
    }


    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 收到点赞消息处理Action
     */
    private ImHelper.Func<MsgDataLike> mLikeFunc = new ImHelper.Func<MsgDataLike>() {

        @Override
        public void action(final MsgDataLike o) {
            if (!mUID.equals(o.getUid())) {
                showLikeUI(o.getName(), true, false);
            } else {
                showLikeUI(o.getName(), true, true);
            }
        }
    };

    /**
     * @param name
     * @param isRemote
     * @param isMine
     */
    public void showLikeUI(final String name, final boolean isRemote, final boolean isMine) {
        mBubblingView.addBubblingItem(images[(i + 1) % images.length]);
        i = (i + 1) % images.length;
        final LiveCommentBean commentBean = new LiveCommentBean();
        if (isRemote) {
            mLikeMsgCount++;
            if (isMine) {
                commentBean.setContent(String.format("收到远程点赞[%1$s],当前收到点赞消息总数：%2$d，Msg ID:%3$d",
                        sdf.format(new Date()),
                        mLikeMsgCount,
                        mRemoteLikeMsgID++));
            } else {
                commentBean.setContent(String.format("收到远程点赞[%1$s],当前收到点赞消息总数：%2$d", sdf.format(new Date()), mLikeMsgCount));
            }
        } else {
            commentBean.setContent(String.format("本地点赞[%1$s]， Msg ID:%2$d",
                    sdf.format(new Date()),
                    mLocalLikeMsgID++));
        }
        commentBean.setName(name);
        commentBean.setColorResID(R.color.live_comment_circle_color);
        mAdapter.addComment(commentBean);
        mCommentView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
    }

    private int mCommentMsgCount = 0;
    private int mLikeMsgCount = 0;
    private int mRemoteLikeMsgID = 0;
    private int mLocalLikeMsgID = 0;
    /**
     * 收到评论消息处理的Action
     */
    private ImHelper.Func<MsgDataComment> mCommentFunc = new ImHelper.Func<MsgDataComment>() {

        @Override
        public void action(final MsgDataComment msgDataComment) {
            mCommentMsgCount++;
            final LiveCommentBean commentBean = new LiveCommentBean();
            commentBean.setContent(String.format("收到评论：%1$s [%2$s], 当前收到评论消息总数：%3$d",
                    msgDataComment.getComment(),
                    sdf.format(new Date()),
                    mCommentMsgCount));
            commentBean.setName(msgDataComment.getName());
            commentBean.setColorResID(R.color.live_comment_circle_color);
            mAdapter.addComment(commentBean);
            mCommentView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            mAdapter.notifyDataSetChanged();

        }
    };


    public static InteractionFragment newInstance(String roomID, String anchorName, String anchorUID) {
        InteractionFragment fragment = new InteractionFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ExtraConstant.EXTRA_ROOM_ID, roomID);
        bundle.putString(ExtraConstant.EXTRA_NAME, anchorName);
        bundle.putString(ExtraConstant.EXTRA_ANCHOR_UID, anchorUID);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setImManger(ImManager imManger) {
        this.mImManger = imManger;
    }
}
