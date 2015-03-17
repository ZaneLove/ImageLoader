package com.zanelove.imageloader.imageloaderdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by ZaneLove on 2015/3/15.
 */
public abstract class AbsSingleFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.Fragment fragment = fm.findFragmentById(R.id.id_fragmentContainer);

        if(fragment == null) {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.id_fragmentContainer,fragment).commit();
        }
    }

    //创建Fragment
    protected abstract Fragment createFragment();
    //获得布局id
    protected abstract int getLayoutId();
}
