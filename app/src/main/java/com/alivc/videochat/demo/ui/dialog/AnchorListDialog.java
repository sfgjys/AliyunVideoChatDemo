package com.alivc.videochat.demo.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.alivc.videochat.demo.R;
import com.alivc.videochat.demo.ui.AnchorListFragment;
import com.alivc.videochat.demo.ui.ExtraConstant;
import com.alivc.videochat.demo.ui.adapter.VideoCallListPagerAdapter;
import com.alivc.videochat.demo.uitils.DensityUtil;

/**
 * 类的描述: 主要责任是主播从多个观众或者主播中选择邀请进行连麦，这是一个有多个Tab绑定Fragment的对话框
 */
public class AnchorListDialog extends BaseTransparentDialog {

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ImageView mIvClose;
    private String mRoomID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);// 父类中调用了setStyle方法设置自定义的对话框样式
        mRoomID = getArguments().getString(ExtraConstant.EXTRA_ROOM_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_anchor_list, container, false);
    }

    /**
     * 技巧:
     * DialogFragment的onViewCreated(控件已经创建了)生命周期方法中可以获取对话框的Window控件，对其宽高进行重新设置，以达到我们需要的宽高，例子如下
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        mIvClose = (ImageView) view.findViewById(R.id.iv_close);

        mIvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭对话框
                dismiss();
            }
        });

        // 重新设置对话框的宽高
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setLayout(DensityUtil.dp2px(getActivity(), 250), DensityUtil.dp2px(getActivity(), 400));
        }

        initViewPager();
    }

    /**
     * 方法描述: 初始化Fragment的ViewPager
     */
    private void initViewPager() {
        VideoCallListPagerAdapter adapter = new VideoCallListPagerAdapter(getChildFragmentManager());
        // 参数一 实例的Fragment给适配器去管理是显示，隐藏还是滑动，参数二TabLayout上显示的文本，与Fragment一一对应
        adapter.addFragment(AnchorListFragment.newInstance(AnchorListFragment.FLAG_ANCHOR, mRoomID), getString(R.string.anchor));
        adapter.addFragment(AnchorListFragment.newInstance(AnchorListFragment.FLAG_WATCHER, mRoomID), getString(R.string.watcher));
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * 方法描述: 初始化创建一个对话框对象，并将参数传递下去
     *
     * @param roomID 请求网络获取推流地址的结果LiveCreateResult对象的mRoomID
     */
    public static AnchorListDialog newInstance(String roomID) {
        AnchorListDialog dialog = new AnchorListDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ExtraConstant.EXTRA_ROOM_ID, roomID);
        dialog.setArguments(bundle);
        return dialog;
    }
}
