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
import java.util.Locale;

/**
 * 类的描述: 直播和观看使用的交互界面Fragment，该界面有聊天室控件，点赞冒泡控件，还有一个FrameLayout控件用来开启底部按钮Fragment
 */
public class InteractionFragment extends Fragment {
    private static final String TAG = "ActionFragment";
    /**
     * 变量的描述: 点赞冒泡控件
     */
    private BubblingView mBubblingView;
    /**
     * 变量的描述: 显示聊天内容的控件
     */
    private RecyclerView mCommentView;
    /**
     * 变量的描述: 显示主播信息的控件
     */
    private TextView mTvName;
    /**
     * 变量的描述:
     */
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
    /**
     * 变量的描述: 聊天列表的适配器
     */
    LiveCommentAdapter mAdapter;
    /**
     * 变量的描述: 聊天列表的布局管理
     */
    LinearLayoutManager mCommentManager;
    /**
     * 变量的描述: 底部按钮Fragment
     */
    private Fragment mBottomFragment;
    /**
     * 变量的描述: 用户登录时保存的数据
     */
    private String mUID;
    /**
     * 变量的描述: 请求网络获取推流地址的结果LiveCreateResult对象的mName
     */
    private String mAnchorName;
    /**
     * 变量的描述: 点赞冒泡显示的图片
     */
    private int[] mBubblingImageID = {
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
    /**
     * 变量的描述: 冒泡显示图片的角标
     */
    int index = 0;

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 创建InteractionFragment对象。并将参数设置进Bundle，进行传递
     *
     * @param roomID     请求网络获取推流地址的结果LiveCreateResult对象的mRoomID
     * @param anchorName 请求网络获取推流地址的结果LiveCreateResult对象的mName
     * @param anchorUID  请求网络获取推流地址的结果LiveCreateResult对象的mUid
     */
    public static InteractionFragment newInstance(String roomID, String anchorName, String anchorUID) {
        InteractionFragment fragment = new InteractionFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ExtraConstant.EXTRA_ROOM_ID, roomID);
        bundle.putString(ExtraConstant.EXTRA_NAME, anchorName);
        bundle.putString(ExtraConstant.EXTRA_ANCHOR_UID, anchorUID);
        fragment.setArguments(bundle);
        return fragment;
    }

    // --------------------------------------------------------------------------------------------------------

    /**
     * 方法描述: 获取一些需要用到的数据
     */
    public void initArgs() {
        Bundle args = getArguments();
        mAnchorName = args.getString(ExtraConstant.EXTRA_NAME);
        mUID = ((BaseActivity) getActivity()).getUid();
    }

    // **************************************************** 开启一个底部Frgament ****************************************************

    /**
     * 方法描述: 传递在Fragment要开启的Fragment的实例进来
     */
    public void setBottomFragment(Fragment fragment) {
        this.mBottomFragment = fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArgs();

        if (mBottomFragment != null) {
            // 使用Fragment的管理器去获取事物开启fragment  一个在底部开启的Fragment
            getChildFragmentManager().beginTransaction().add(R.id.bottom_container, mBottomFragment).commit();
        }
    }

    // -------------------------------------------------------------------------------------------------------- 

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_interaction, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 冒泡控件
        mBubblingView = (BubblingView) view.findViewById(R.id.bv_like);

        // 设置主播信息
        mTvName = (TextView) view.findViewById(R.id.tv_name);
        mTvName.setText(mAnchorName + "(" + mUID + ")");

        // 聊天内容显示控件
        mCommentView = (RecyclerView) view.findViewById(R.id.rv_comment);
        mCommentView.setHasFixedSize(true);// 确保尺寸是个常数不变
        mAdapter = new LiveCommentAdapter();
        mCommentManager = new LinearLayoutManager(getActivity());
        // 下面两行代码应该可以使新添加的item始终显示在列表最底部显示
        mCommentManager.setStackFromEnd(true);// 可以让最后添加的item始终显示在RecycleView中；
        mCommentManager.setSmoothScrollbarEnabled(true);
        mCommentView.setLayoutManager(mCommentManager);
        mCommentView.setAdapter(mAdapter);
    }

    // **************************************************** 获取MNS管理器 ****************************************************
    private ImManager mImManger;

    public void setImManger(ImManager imManger) {
        this.mImManger = imManger;
    }

    // --------------------------------------------------------------------------------------------------------
    // **************************************************** 注册和注销订阅 ****************************************************
    // 因为本Fragment是随着直播和观看界面开启的，而在直播和观看界面开启的时候一般已经初始化并建立了MNS的链接，所以这里只要对订阅事件进行操作
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

    // --------------------------------------------------------------------------------------------------------

    /**
     * 变量的描述: 从MNS收到的评论消息总数
     */
    private int mCommentMsgCount = 0;
    /**
     * 变量的描述: 从MNS收到的点赞总数
     */
    private int mLikeMsgCount = 0;
    /**
     * 变量的描述: 从MNS收到的自己点赞的总数
     */
    private int mRemoteLikeMsgID = 0;
    /**
     * 变量的描述: 在本地记录的自己点赞的总数
     */
    private int mLocalLikeMsgID = 0;

    /**
     * 方法描述: 显示点赞控件
     *
     * @param name     TODO
     * @param isRemote 是远程的点赞么
     * @param isMine   是自己的点赞么
     */
    public void showLikeUI(final String name, final boolean isRemote, final boolean isMine) {
        // (i + 1) % mBubblingImageID.length 这个代码效果等价 i++
        // mBubblingView控件按照顺序添加冒泡要显示的图片
        mBubblingView.addBubblingItem(mBubblingImageID[(index + 1) % mBubblingImageID.length]);
        index = (index + 1) % mBubblingImageID.length;

        // 将点赞的信息显示在聊天控件中
        final LiveCommentBean commentBean = new LiveCommentBean();
        if (isRemote) {
            mLikeMsgCount++;
            if (isMine) {
                commentBean.setContent(String.format("收到远程点赞[%1$s],当前收到点赞消息总数：%2$d，Msg ID:%3$d",
                        simpleDateFormat.format(new Date()),
                        mLikeMsgCount,
                        mRemoteLikeMsgID++));
            } else {
                commentBean.setContent(String.format("收到远程点赞[%1$s],当前收到点赞消息总数：%2$d", simpleDateFormat.format(new Date()), mLikeMsgCount));
            }
        } else {
            commentBean.setContent(String.format("本地点赞[%1$s]， Msg ID:%2$d",
                    simpleDateFormat.format(new Date()),
                    mLocalLikeMsgID++));
        }
        commentBean.setName(name);
        commentBean.setColorResID(R.color.live_comment_circle_color);
        mAdapter.addComment(commentBean);
        mCommentView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
    }
    // **************************************************** 点赞和聊天的消息订阅 ****************************************************
    /**
     * 变量的描述: 收到点赞消息处理Action
     */
    private ImHelper.Func<MsgDataLike> mLikeFunc = new ImHelper.Func<MsgDataLike>() {
        @Override
        public void action(final MsgDataLike msgDataLike) {
            // 根据点赞人的id信息来判断是远程点赞还是自己点赞的
            if (!mUID.equals(msgDataLike.getUid())) {
                showLikeUI(msgDataLike.getName(), true, false);
            } else {
                showLikeUI(msgDataLike.getName(), true, true);
            }
        }
    };
    /**
     * 变量的描述: 收到评论消息处理的Action
     */
    private ImHelper.Func<MsgDataComment> mCommentFunc = new ImHelper.Func<MsgDataComment>() {
        @Override
        public void action(final MsgDataComment msgDataComment) {
            mCommentMsgCount++;
            final LiveCommentBean commentBean = new LiveCommentBean();
            commentBean.setContent(String.format("收到评论：%1$s [%2$s], 当前收到评论消息总数：%3$d",
                    msgDataComment.getComment(),
                    simpleDateFormat.format(new Date()),
                    mCommentMsgCount));
            commentBean.setName(msgDataComment.getName());
            commentBean.setColorResID(R.color.live_comment_circle_color);
            mAdapter.addComment(commentBean);
            mCommentView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            mAdapter.notifyDataSetChanged();
        }
    };

    // --------------------------------------------------------------------------------------------------------

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
