package com.seuic.zh.mydueros;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.duer.dcs.api.config.DcsConfig;
import com.baidu.duer.dcs.util.AsrType;
import com.baidu.duer.dcs.util.api.IDcsRequestBodySentListener;
import com.baidu.duer.dcs.util.message.DcsRequestBody;
import com.baidu.duer.dcs.util.util.NetWorkUtil;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.phonecall.message.PhonecallByNamePayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.phonecall.message.PhonecallByNumberPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.screen.message.HtmlPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.screen.message.RenderVoiceInputTextPayload;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends BaseActivity {

    private MyReceiverOnline receiver;
    private String myNotification_Type = null;
    private String myNotification_Contact = null;
    private String myNotification_Content = null;

    private Boolean swift_network = false;
    private Boolean swift_phoneCall = false;


    //设置自己的指令操作，只使用度秘的语音识别。
    private Boolean get_message_content = false;
    private Boolean start_Config = false;
    private Boolean answer_message = false;
    private Boolean stop_speaker = false;
    private Boolean message_broadcast = false;
    private Boolean make_sure;


    private int systemTime;
    private SharedPreferences sharedPreferences;
    private int startTime;
    private int stopTime;
    private boolean typeOfSpeak;
    private String keyWord1;
    private String keyWord2;
    private String keyWord3;

    private String message_content = null;
    private String message_content_old = null;

    private MyList myList ;

    @Override
    protected void onCreate (Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        getInternalApi().addRequestBodySentListener(dcsRequestBodySentListener2);
        //通知栏监听器

        receiver=new MyReceiverOnline();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.seuic.zh.mydueros.MyService");
        filter.addAction("com.seuic.zh.offline_to_online");
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        MainActivity.this.registerReceiver(receiver,filter);

        if (!isNotificationListenerEnabled(this)){
            openNotificationListenSettings();
        }
        toggleNotificationListenerService();

        String offline_to_online = getIntent().getStringExtra("extra_data");
        Intent intent = new Intent();
        intent.setAction("com.seuic.zh.offline_to_online");
        intent.putExtra("extra_data",offline_to_online);
        sendBroadcast(intent);
    }

    //检测通知监听服务是否被授权
    public boolean isNotificationListenerEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(this);
        if (packageNames.contains(context.getPackageName())) {
            return true;
        }
        return false;
    }
    //打开通知监听设置页面
    public void openNotificationListenSettings() {
        try {
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            } else {
                intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            }
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //把应用的NotificationListenerService实现类disable再enable，即可触发系统rebind操作
    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(MainActivity.this, MyService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(
                new ComponentName(MainActivity.this, MyService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public class MyReceiverOnline extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {

            if ("com.seuic.zh.offline_to_online".equals(intent.getAction())) {
                getInternalApi().speakRequest(intent.getStringExtra("extra_data"));
            }
            if ("com.seuic.zh.mydueros.MyService".equals(intent.getAction())){
                MyList myNotification = (MyList) intent.getSerializableExtra("myNotification");
                myNotification_Type = myNotification.getType();
                myNotification_Contact = myNotification.getContact();
                myNotification_Content = myNotification.getContent();

                if (myNotification_Type.equals("来电")) {
                    myList = new MyList(myNotification_Type,myNotification_Contact,myNotification_Content);
                    swift_phoneCall = true;
                    swift_to_offline();
                }

                if (myNotification_Type.equals("接收短信")) {
                    message_broadcast = true;
                        myList = new MyList(myNotification_Type,myNotification_Contact,myNotification_Content);
                        swift_phoneCall = true;
                        swift_to_offline();
                }
            }

//             监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                if (!NetWorkUtil.isNetworkConnected(MainActivity.this)) {
                    delay(2000);
                    if (!NetWorkUtil.isNetworkConnected(MainActivity.this)) {
                        myList = new MyList("没有网络","无","无");
                        swift_to_offline();
                    }
                }
            }
        }
    }

    private void swift_to_offline () {
        Intent intent = new Intent(MainActivity.this, MidActivity.class);
        intent.putExtra("extra_data", (Serializable) myList);
        dcsSdk.release();
        startActivity(intent);
        finish();
    }

    //获取云端返回联系人号码
    @Override
    public void handlePhonecallByNumberPayload(PhonecallByNumberPayload payload) {
        String callNumber = payload.getCallee().getPhoneNumber();
        myList = new MyList("正在拨号",callNumber,null);
        swift_phoneCall = true;
        swift_to_offline();
    }
    //获取云端返回联系人名字
    @Override
    public void handlePhonecallByNamePayload(PhonecallByNamePayload payload) {
        String callName = payload.getCandidateCallees().get(0).contactName;
        String callNumber = getPhoneByName(this,callName).get(0);
        myList = new MyList("正在拨号",callNumber,callName);
        swift_phoneCall = true;
        swift_to_offline();
    }
    @Override
    public void handleHtmlPayload(HtmlPayload payload) {
        if (stop_speaker) {
            cancelVoiceRequest();
        }else{
            dcsWebView.loadUrl(payload.getUrl());
            duerResultT = System.currentTimeMillis();
        }

    }

    private void parareset () {
        message_broadcast = false;
        stop_speaker = false;
        start_Config = false;
        answer_message = false;
        get_message_content = false;
    }

    private void mySpeakeOnline(String contact, String context) {

        Boolean key_word_match = false;
        Boolean time_setting_match = false;
        Boolean bluetooth_speak = false;

        //获取用户设置的信息
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        startTime = sharedPreferences.getInt("time_start", 420);
        stopTime = sharedPreferences.getInt("time_stop", 1050);
        typeOfSpeak = sharedPreferences.getBoolean("yuyin", false);
        keyWord1 = sharedPreferences.getString("key_word1", "");
        keyWord2 = sharedPreferences.getString("key_word2", "");
        keyWord3 = sharedPreferences.getString("key_word3", "");
        //获取系统的时间
        Calendar calendar = Calendar.getInstance();
        systemTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        Log.d("systemTime",String.valueOf(systemTime));

        //获取蓝牙的连接状态,这一部分要改进下
        if (typeOfSpeak && GetBluetoothState() == 2) {
            bluetooth_speak = true;
        }
        if (!typeOfSpeak) {
            bluetooth_speak = true;
        }
        if (systemTime > startTime && systemTime < stopTime) {
            time_setting_match = true;
        }
        if (regExTest(contact+context, keyWord1) | regExTest(contact+context, keyWord2) | regExTest(contact+context, keyWord3)) {
            key_word_match = true;
        }

        Log.d("mySpeakeOnline",String.valueOf(bluetooth_speak)+"\n"+String.valueOf(time_setting_match)+"\n"+String.valueOf(key_word_match));
        if (time_setting_match && bluetooth_speak && key_word_match) {

            getInternalApi().speakRequest(contact+"来短信了"+contact+"来短信了"+"请问需要朗读吗");
            start_Config = true;
        }
    }

    private static int GetBluetoothState() {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (ba == null) {
            return -2;
        }
        try {
            if (!ba.isEnabled()) {
                return -1;
            }
        } catch (Exception e) {
            return -3;
        }
        try {
            //可操控蓝牙设备，如带播放暂停功能的蓝牙耳机
            int a2dp = ba.getProfileConnectionState(BluetoothProfile.A2DP);
            if (a2dp == BluetoothProfile.STATE_CONNECTING || a2dp == BluetoothProfile.STATE_CONNECTED) {
                Log.d("a2dp", String.valueOf(a2dp));
                return a2dp;
            }
            //蓝牙头戴式耳机，支持语音输入输出
            int headset = ba.getProfileConnectionState(BluetoothProfile.HEADSET);
            if (headset == BluetoothProfile.STATE_CONNECTING || headset == BluetoothProfile.STATE_CONNECTED) {
                Log.d("headset", String.valueOf(headset));
                return headset;
            }
            //蓝牙穿戴式设备
            int health = ba.getProfileConnectionState(BluetoothProfile.HEALTH);
            if (health == BluetoothProfile.STATE_CONNECTING || health == BluetoothProfile.STATE_CONNECTED) {
                Log.d("health", String.valueOf(health));
                return health;
            }
        } catch (Exception e) {

        }
        Log.d("STATEDISCONNECTED", String.valueOf(BluetoothProfile.STATE_DISCONNECTED));
        return BluetoothProfile.STATE_DISCONNECTED;
    }

    private IDcsRequestBodySentListener dcsRequestBodySentListener2 = new IDcsRequestBodySentListener() {

        @Override
        public void onDcsRequestBody(DcsRequestBody dcsRequestBody) {
            String eventName = dcsRequestBody.getEvent().getHeader().getName();
            Log.v(TAG, "eventName:" + eventName);

            if (eventName.equals("SpeechFinished") && start_Config) {
                beginVoiceRequest(true);
                getInternalApi().startWakeup();
            }


        }
    };


    @Override
    public boolean enableWakeUp() {
        return true;
    }

    @Override
    public int getAsrMode() {
        return DcsConfig.ASR_MODE_ONLINE;
    }

    @Override
    public AsrType getAsrType() {
        return AsrType.AUTO;
    }


    /**
     * 通过正则表达式进行关键词匹配
     *
     * @param text    待匹配的原始数据
     * @param keyWord 关键词
     * @return
     */
    private boolean regExTest(String text, String keyWord) {
        Log.i(TAG, "regExTest: originalText::" + text + " keyWord::" + keyWord);
        Pattern p;
        Matcher m;
        //用于过滤掉相关字符或词汇(用括号括起来的)的正则表达式，
        String regEx = "[`~!@#$%^&*()+=|{}:;\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？a-zA-Z 0-9,嗯恩哦啊请吧(一下)(好吗)(可以吗)]";
        p = Pattern.compile(regEx);
        m = p.matcher(text);
        text = m.replaceAll("").trim();
        Log.i(TAG, "regExTest: text::" + text);
        String regEx1 = new StringBuilder().append(".{0,10}(").append(keyWord).append(").{0,10}").toString();//eg:  ".{0,10}(景色|漂亮).{0,10}"（todo 适用于长度不超多20的文本）
        p = Pattern.compile(regEx1);
        m = p.matcher(text);
        Log.i(TAG, "Pattern:text::" + text + " matcher:" + m.matches());
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onDestroy (){
        super.onDestroy();
        unregisterReceiver(receiver);
        getInternalApi().removeRequestBodySentListener(dcsRequestBodySentListener2);
        dcsRequestBodySentListener2 = null;
        dcsSdk.release();
    }
}

