package com.seuic.zh.mydueros;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SwitchPreference yuyin;
    private com.seuic.zh.mydueros.TimePreference timeStart;
    private com.seuic.zh.mydueros.TimePreference timeEnd;
    private EditTextPreference keyWord1;
    private EditTextPreference keyWord2;
    private EditTextPreference keyWord3;
    private Toast toast;
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.app_pref);

        yuyin = (SwitchPreference) findPreference("yuyin");
        timeStart = (com.seuic.zh.mydueros.TimePreference) findPreference("time_start");
        timeEnd = (com.seuic.zh.mydueros.TimePreference) findPreference("time_stop");
        keyWord1 = (EditTextPreference) findPreference("key_word1");
        keyWord2 = (EditTextPreference) findPreference("key_word2");
        keyWord3 = (EditTextPreference) findPreference("key_word3");
//        switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                return true;
//            }
//        });
//        editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                return true;
//            }
//        });
//        checkBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                return true;
//            }
//        });
//        timePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                return true;
//            }
//        });

    }

    @Override
    public void onResume() {
        super.onResume();
        yuyin.setSummary(getPreferenceScreen().getSharedPreferences().getBoolean("yuyin",false)?
                "蓝牙播报" : "系统播报");
        timeStart.setSummary(showTime(getPreferenceScreen().getSharedPreferences().getInt("time_start",420)));
        timeEnd.setSummary(showTime(getPreferenceScreen().getSharedPreferences().getInt("time_stop",1050)));
        keyWord1.setSummary(getPreferenceScreen().getSharedPreferences().getString("key_word1",""));
        keyWord2.setSummary(getPreferenceScreen().getSharedPreferences().getString("key_word2",""));
        keyWord3.setSummary(getPreferenceScreen().getSharedPreferences().getString("key_word3",""));
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;
        if (preference instanceof TimePreference) {
            // Create a new instance of TimePreferenceDialogFragment with the key of the related
            // Preference
            dialogFragment = TimePreferenceDialogFragmentCompat
                    .newInstance(preference.getKey());
        }

        // If it was one of our cutom Preferences, show its dialog
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(),
                    "android.support.v7.preference" +
                            ".PreferenceFragment.DIALOG");
        }
        // Could not be handled here. Try with the super method.
        else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("yuyin")) {
            yuyin.setSummary(sharedPreferences.getBoolean(key,false)?"蓝牙播报" : "系统播报");
            toast = Toast.makeText(settingActivity.settingActivity,sharedPreferences.getBoolean(key,false)?"蓝牙播报" : "系统播报",Toast.LENGTH_SHORT);

        }

        if (key.equals("time_start")) {
            timeStart.setSummary(showTime(sharedPreferences.getInt(key,420)));
            toast = Toast.makeText(settingActivity.settingActivity,"起始时间为："+showTime(sharedPreferences.getInt(key,420)),Toast.LENGTH_SHORT);

        }
        if (key.equals("time_stop")) {
            timeEnd.setSummary(showTime(sharedPreferences.getInt(key,1050)));
            toast = Toast.makeText(settingActivity.settingActivity,"结束时间为："+showTime(sharedPreferences.getInt(key,1050)),Toast.LENGTH_SHORT);

        }

        if (key.equals("key_word1")) {
            keyWord1.setSummary(sharedPreferences.getString(key,""));
            toast = Toast.makeText(settingActivity.settingActivity,"关键字1为："+sharedPreferences.getString(key,""),Toast.LENGTH_SHORT);

        }
        if (key.equals("key_word2")) {
            keyWord2.setSummary(sharedPreferences.getString(key,""));
            toast = Toast.makeText(settingActivity.settingActivity,"关键字2为："+sharedPreferences.getString(key,""),Toast.LENGTH_SHORT);

        }
        if (key.equals("key_word3")) {
            keyWord3.setSummary(sharedPreferences.getString(key,""));
            toast = Toast.makeText(settingActivity.settingActivity,"关键字3为："+sharedPreferences.getString(key,""),Toast.LENGTH_SHORT);

        }
        showMyToast(toast,700);
    }

    private String showTime (int time) {
        String hour = null;
        String minute = null;
        if (time/60<10){
            hour = "0"+String.valueOf(time/60);
        }else{
            hour = String.valueOf(time/60);
        }

        if (time%60<10){
            minute = "0"+String.valueOf(time%60);
        }else {
            minute = String.valueOf(time%60);
        }

        String currentTime =hour + ":" + minute;
        return currentTime;
    }

    public void showMyToast(final Toast toast, final int cnt) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 3500);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt );
    }
}
