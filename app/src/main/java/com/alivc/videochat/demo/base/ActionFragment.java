package com.alivc.videochat.demo.base;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by liujianghao on 16-9-28.
 */

public class ActionFragment extends Fragment{
    protected FragmentInteraction mActionListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if(activity instanceof FragmentInteraction) {
            mActionListener = (FragmentInteraction) activity;
        }else {
            throw new IllegalStateException("Activity must implements FragmentInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActionListener = null;
    }
}
