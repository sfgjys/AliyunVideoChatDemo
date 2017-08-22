package com.alivc.videochat.demo.base;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * 类的描述: 该类的主要作用是从开启Fragment的Activity中获取实例化的FragmentInteraction接口
 */
public class ActionFragment extends Fragment {
    protected FragmentInteraction mActionListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (activity instanceof FragmentInteraction) {
            mActionListener = (FragmentInteraction) activity;
        } else {
            throw new IllegalStateException("Activity must implements FragmentInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActionListener = null;
    }
}
