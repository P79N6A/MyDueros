package com.seuic.zh.mydueros;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class settingActivity extends AppCompatActivity {

    public static settingActivity settingActivity;

    private Fragment preferenceFragment;
    private FragmentTransaction ft;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        settingActivity = this;
        if (savedInstanceState == null) {
            preferenceFragment = new SettingsFragment();
            ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.pref_container, preferenceFragment);
            ft.commit();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }
}
