package com.seuic.zh.mydueros;
/**
 * Created by liangxianlan on 13/04/2018.
 * <p>
 * 集成离线语音识别步骤：
 * <p>
 * 一、libbd_model_easr_dat.so是离线资源文件，离线资源文件可以预置到data中，也可以放到sdcard下
 * <p>
 * 二、离线参数设置
 * 1. 设置离线资源文件绝对路径，例如放在sdcard中：
 * AsrParam.ASR_OFFLINE_ENGINE_DAT_FILE_PATH = Environment.getExternalStorageDirectory() + "/libbd_model_easr_dat.so";
 * 2.打开离线标点开关，0关闭，1打开
 * AsrParam.ASR_OFFLINE_PUNCTUATION_SETTING_VALUE = 1;
 * 3.设置为离线模式，0在线，1离线。注意，从离线切换到在线时，AsrParam.ASR_DECODER需要设置为0。
 * (1). AsrParam.ASR_DECODER = 1;
 * (2). asrMode = DcsConfig.ASR_MODE_OFFLINE
 * (3). asrOnly = true
 * 4.设置离线license，离线license没有设置，第一次识别需要联网在线鉴权
 * AsrParam.ASR_OFFLINE_ENGINE_LICENSE_FILE_PATH = "assets://temp_license_2018-07-04";
 * <p>
 * 注：离线asr不支持longspeech
 *
 *
 * 2018.07.13
 * 增加离线识别：通讯录和应用程序名称的识别
 * 1. asrMode = DcsConfig.ASR_MODE_OFFLINE_SEMANTIC
 * 2. AsrParam.ASR_OFFLINE_PUNCTUATION_SETTING_VALUE = 0(无标点)
 */
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.os.Handler;

import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;

import android.support.v4.app.NotificationManagerCompat;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.baidu.dcs.acl.AsrParam;
import com.baidu.duer.dcs.api.IDcsSdk;
import com.baidu.duer.dcs.api.IMessageSender;
import com.baidu.duer.dcs.api.config.DcsConfig;
import com.baidu.duer.dcs.api.config.DefaultSdkConfigProvider;
import com.baidu.duer.dcs.api.config.SdkConfigProvider;
import com.baidu.duer.dcs.tts.TtsImpl;
import com.baidu.duer.dcs.util.AsrType;
import com.baidu.duer.dcs.util.api.IDcsRequestBodySentListener;
import com.baidu.duer.dcs.util.devicemodule.asr.message.HandleAsrResultPayload;
import com.baidu.duer.dcs.util.message.DcsRequestBody;
import com.baidu.duer.dcs.util.util.NetWorkUtil;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.asr.AsrDeviceModule;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main2Activity extends BaseActivity {

    private LinearLayout topLinearLayout;
    private TextView asrContentView;
    private Handler handler;
    private TextView textView ;

    public static final String TTS_APIKEY = "3ooiznB7weqeCAxnaZzCY84Z";
    public static final String TTS_APPID = "14603645";
    public static final String TTS_SERCERTKEY = "xTtXO9q80cLIfuaPqYpG9UlCS97XNfUr";

    private Boolean action_Call_Phone = false;
    private Boolean action_Send_Message = false;
    private Boolean action_Answer_Phone = false;
    private Boolean action_Reject_Phone = false;
    private Boolean get_Number = false;
    private Boolean get_Message_Content = false;
    private Boolean to_get_Message_Content = false;
    private Boolean time_to_call = false;

    private String catch_Number = null;
    private String catch_Message_Content = null;
    private String call_Name = null;

    private List<PhoneDto> contact_list = null;

    private MyNotificationReceiver myNotificationReceiver;
    private MySwiftReceiver mySwiftReceiver;

    private String myNotification_Type = null;
    private String myNotification_Contact = null;
    private String myNotification_Content = null;
    private String mySwift_Type = null;
    private String mySwift_Contact = null;
    private String mySwift_Content = null;

    private StringCompare stringCompare;

    private MyLevenshtein myLevenshtein;

    private int systemTime;
    private SharedPreferences sharedPreferences;
    private int startTime;
    private int stopTime;
    private boolean typeOfSpeak;
    private String keyWord1;
    private String keyWord2;
    private String keyWord3;
    private Boolean swift = false;

    private Boolean Online_swift_Offline = false;
    private Boolean starte_Config = false;
    private MyList myList2 ;
    private Timer timer;
    private int counterTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        Button button = (Button) findViewById(R.id.click);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Get_Phone_Contact get_phone_contact = new Get_Phone_Contact(Main2Activity.this);
                contact_list = get_phone_contact.getPhone();
                Log.d("1111111111111111",Match_Number_In_Phone("赵欢",contact_list));
            }
        });
        // 设置离线资源文件绝对路径，这里是放在sdcard中
        // libbd_model_easr_dat.so在extras/offlineresource/下
        String offLineSourceFilePath = Environment.getExternalStorageDirectory() + "/libbd_model_easr_dat.so";
        if (!new File(offLineSourceFilePath).exists()) {
            Toast.makeText(Main2Activity.this,
                    "离线资源文件：" + offLineSourceFilePath + "不存在",
                    Toast.LENGTH_SHORT)
                    .show();
        }
        AsrParam.ASR_OFFLINE_ENGINE_DAT_FILE_PATH = offLineSourceFilePath;
        // 打开离线标点开关
        AsrParam.ASR_OFFLINE_PUNCTUATION_SETTING_VALUE = 2;
        // 设置为离线模式:1
        AsrParam.ASR_DECODER = 1;
        // 离线识别时，较长vad检测开关，检测时间1300ms
        AsrParam.ASR_OFFLINE_VAD_LONG = true;
        // 设置离线 license
//        AsrParam.ASR_OFFLINE_ENGINE_LICENSE_FILE_PATH = "assets://temp_license_2018-07-04"


        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TtsImpl impl = getInternalApi().initLocalTts(getApplicationContext(), null, null,
                        TTS_APIKEY,
                        TTS_SERCERTKEY, TTS_APPID, null);
                impl.setSpeaker(2);
                String textFile = getApplicationInfo().nativeLibraryDir + "/libbd_etts_text.dat.so";
                String speechMode = getApplicationInfo().nativeLibraryDir + "/" + TtsImpl.SPEECH_MODEL_NAME_GEZI;
                impl.loadSpeechModel(speechMode, textFile);
                getInternalApi().setVolume(0.8f);
            }
        }, 200);



        //通知栏监听器

        myNotificationReceiver = new MyNotificationReceiver();
        IntentFilter myNotificationFilter = new IntentFilter();
        myNotificationFilter.addAction("com.seuic.zh.mydueros.MyService");
        Main2Activity.this.registerReceiver(myNotificationReceiver, myNotificationFilter);

        //切换时广播接收器
        mySwiftReceiver = new MySwiftReceiver();
        IntentFilter mySwiftFilter = new IntentFilter();
        mySwiftFilter.addAction("com.seuic.zh.mySwift");
        Main2Activity.this.registerReceiver(mySwiftReceiver,mySwiftFilter);

        if (!isNotificationListenerEnabled(this)) {
            openNotificationListenSettings();
        }
        toggleNotificationListenerService();

        getInternalApi().addRequestBodySentListener(dcsRequestBodySentListener1);

        MyList mySwift1 = (MyList) getIntent().getSerializableExtra("extra_data");
        Intent intent = new Intent();
        intent.setAction("com.seuic.zh.mySwift");
        intent.putExtra("mySwift",(Serializable) mySwift1);
        if (!(mySwift1.getType().equals("没有网络"))) {
            Online_swift_Offline = true;
        }
        sendBroadcast(intent);

        //每隔一段时间检测网络以及activity的活动状态
        timer = new Timer();
        timer.schedule(timerTask, 0, 60000);
        counterTime = 0;
    }


    private TimerTask timerTask = new TimerTask()
    {
        @Override
        public void run()
        {
            counterTime++;
            if (counterTime !=1) {
                Message msg = new Message();
                msg.what = 1;
                myHandler.sendMessage(msg);
            }
        }
    };


    private Handler myHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case 1:
                    if (NetWorkUtil.isNetworkConnected(Main2Activity.this)) {
                        if (!(action_Answer_Phone | action_Call_Phone | action_Reject_Phone | action_Send_Message)) {
                            getInternalApi().speakOfflineRequest("当前网络连接上，为您切换到在线模式，请稍后");
                            swift = true;
                        }
                    }
                    break;
            }
        }
    };

    private void swift_to_online () {
        myList2 = new MyList("offline_to_online","","");
        Intent intent = new Intent(Main2Activity.this, MidActivity.class);
        intent.putExtra("extra_data",(Serializable) myList2);
        dcsSdk.release();
        startActivity(intent);
        finish();
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
                new ComponentName(this, MyService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(
                new ComponentName(this, MyService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    //接收notification传过来的电话和短信通知
    /*
    notification发送过来的Message_Type以及Message_Title
    Message_Title：如果手机里面有联系人，那么就是联系人姓名；不然就是号码加归属地
    Message_Type：主要是区分来电还是来短信
     */

    public class MySwiftReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {

            MyList mySwift2 = (MyList) intent.getSerializableExtra("mySwift");
            mySwift_Type = mySwift2.getType();
            mySwift_Contact = mySwift2.getContact();
            mySwift_Content = mySwift2.getContent();
            if (mySwift_Type.equals("来电")) {
                parareset();
                delay(500);
                getInternalApi().speakOfflineRequest(mySwift_Contact + "来电话了" + "\n" + mySwift_Contact + "来电话了" + "请问接听还是挂断");
                starte_Config = true;
            }
            if (mySwift_Type.equals("接收短信")) {
                parareset();
                delay(500);
                mySpeakeOffline(mySwift_Contact, mySwift_Content);
//                    getInternalApi().speakOfflineRequest(myList.getContact() + "来短信了" + "\n" + myList.getContact() + "来短信了" + "请问需要朗读吗");
                starte_Config = true;
            }
            if (mySwift_Type.equals("正在拨号")) {
                parareset();
                delay(500);
                time_to_call = true;
                if (mySwift_Content != null) {
                    getInternalApi().speakOfflineRequest("正在给" + mySwift_Content + "打电话" + "\n" + "正在给" + mySwift_Content + "打电话");
                } else {
                    getInternalApi().speakOfflineRequest("正在给" + mySwift_Contact + "打电话" + "\n" + "正在给" + mySwift_Contact + "打电话");
                }
                catch_Number = mySwift_Contact;
            }
            if (mySwift_Type.equals("没有网络")) {
                parareset();
                delay(500);
                getInternalApi().speakOfflineRequest("已经为您切换到离线模式");
            }
        }
    }

    public class MyNotificationReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {

            if ("com.seuic.zh.mydueros.MyService".equals(intent.getAction())) {
                MyList myNotification = (MyList) intent.getSerializableExtra("myNotification");
                myNotification_Type = myNotification.getType();
                myNotification_Contact = myNotification.getContact();


                if (myNotification_Type.equals("来电")) {
                    parareset();
                    getInternalApi().speakOfflineRequest(myNotification_Contact + "来电话了" + "\n" + myNotification_Contact + "来电话了" + "请问接听还是挂断");
                    starte_Config = true;
                }
                if (myNotification_Type.equals("接收短信")) {
                    myNotification_Content = myNotification.getContent();
                    parareset();
                    mySpeakeOffline(myNotification_Contact ,myNotification_Contact);
                    starte_Config = true;
                }
            }
        }
    }

    private IDcsRequestBodySentListener dcsRequestBodySentListener1 = new IDcsRequestBodySentListener() {

        @Override
        public void onDcsRequestBody(DcsRequestBody dcsRequestBody) {
            String eventName = dcsRequestBody.getEvent().getHeader().getName();
            Log.v(TAG, "eventName:" + eventName);

            if (eventName.equals("SpeechFinished")&& starte_Config ){
//                delay(500);
                beginVoiceRequest(true);
                getInternalApi().startWakeup();
            }
            if (eventName.equals("SpeechFinished")&& swift) {
                swift = false;
                timer.cancel();
                timer = null;
                timerTask.cancel();
                timerTask = null;
                swift_to_online();
            }
            if (eventName.equals("SpeechFinished")&& time_to_call) {
                time_to_call = false;
                callOut(catch_Number);
                Online_swift_Offline = false;
                parareset();
            }


        }
    };

    @Override
    public AsrType getAsrType() {
        return AsrType.AUTO;
    }

    @Override
    public int getAsrMode() {
        return DcsConfig.ASR_MODE_OFFLINE;
    }

    @Override
    public boolean enableWakeUp() {
        return true;
    }

    @Override
    protected SdkConfigProvider getSdkConfigProvider() {
        return new DefaultSdkConfigProvider() {
            @Override
            public String clientId() {
                return CLIENT_ID;
            }

            @Override
            public int pid() {
                return 708;
            }

            @Override
            public boolean asrOnly() {
                // 设置为离线模式-3
                return true;
            }

            @Override
            public boolean longSpeech() {
                return false;
            }
        };
    }

    private void initViews() {
        textView = (TextView) findViewById(R.id.id_tv_wakeup_tip);
        textView.setText("离线模式");
        topLinearLayout = (LinearLayout) findViewById(R.id.topLinearLayout);
        topLinearLayout.removeAllViews();
        asrContentView = new TextView(this);
        String myDirective = "\n唤醒\n打电话\n发短信\n接电话\n挂电话\n取消\n打回去\n朗读\n回复\n发回去";
        AsrParam.ASR_OFFLINE_PUNCTUATION_SETTING_VALUE = 0;
        getInternalApi().setAsrMode(DcsConfig.ASR_MODE_OFFLINE_SEMANTIC);
        AsrParam.ASR_OFFLINE_CONTACTS = Get_Phone_Name()+myDirective;
//        AsrParam.ASR_OFFLINE_APPNAMES = "微信\n王者荣耀\n手机百度";
//        button.setText("识别联系人和应用名称：关闭");

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                button.setText("识别联系人和应用名称：开启");
//                AsrParam.ASR_OFFLINE_PUNCTUATION_SETTING_VALUE = 0;
//                getInternalApi().setAsrMode(DcsConfig.ASR_MODE_OFFLINE_SEMANTIC);
//                AsrParam.ASR_OFFLINE_CONTACTS = "张三\n小李\n王老师";
//                AsrParam.ASR_OFFLINE_APPNAMES = "微信\n王者荣耀\n手机百度";
//            }
//        });
//        topLinearLayout.addView(button, new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        topLinearLayout.addView(asrContentView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }


    @Override
    protected void addOtherDeviceModule(IDcsSdk dcsSdk, IMessageSender messageSender) {
        super.addOtherDeviceModule(dcsSdk, messageSender);
        // 单ASR，不返回度秘结果
        AsrDeviceModule asrDeviceModule = new AsrDeviceModule(messageSender);
        asrDeviceModule.addAsrListener(new AsrDeviceModule.IAsrListener() {
            @Override
            public void onHandleAsrResult(HandleAsrResultPayload payload) {
                handleAsrResultPayload(payload);
            }
        });
        dcsSdk.putDeviceModule(asrDeviceModule);

    }


    public void handleAsrResultPayload(HandleAsrResultPayload payload) {

        asrContentView.setText(payload.getContent());

        Get_Phone_Contact get_phone_contact = new Get_Phone_Contact(this);
        contact_list = get_phone_contact.getPhone();


//        这边判断通过尾部标点来判断语音识别是否结束
        String reg = ".*[\\.。]$";
        if (payload.getContent().matches(reg)) {
            delay(600);
            String text = payload.getContent();



            if (regExTest(payload.getContent(), "唤醒")) {
                wakeUpAndUnlock();
            }
            else if (regExTest(payload.getContent(), "打电话")) {
                action_Call_Phone = true;
                action_Send_Message = false;
                action_Answer_Phone = false;
                action_Reject_Phone = false;
            }
            else if (regExTest(payload.getContent(), "发短信")) {
                action_Call_Phone = false;
                action_Send_Message = true;
                action_Answer_Phone = false;
                action_Reject_Phone = false;
            }
            else if (regExTest(payload.getContent(), "接电话")) {
                action_Call_Phone = false;
                action_Send_Message = false;
                action_Answer_Phone = true;
                action_Reject_Phone = false;
            }
            else if (regExTest(payload.getContent(), "挂电话")) {
                action_Call_Phone = false;
                action_Send_Message = false;
                action_Answer_Phone = false;
                action_Reject_Phone = true;
            }
            //这个是在语音对话时随时中断对话
            else if (regExTest(payload.getContent(), "取消")) {
                parareset();
                Online_swift_Offline = false;
            }

            if (!(Get_Number_In_Text(payload.getContent()) == "号码错误")) {
                get_Number = true;
                catch_Number = Get_Number_In_Text(payload.getContent());
            }
            if (!(Match_Number_In_Phone(text, contact_list) == null)) {
                get_Number = true;
                catch_Number = Match_Number_In_Phone(text, contact_list);
            }
            if (regExTest(payload.getContent(), "打回去")) {
                if (!(get_Lastest_Phone_Number() == null)) {
                    get_Number = true;
                    catch_Number = get_Lastest_Phone_Number();
                    action_Call_Phone = true;
                }
            }

            if (regExTest(payload.getContent(), "朗读|读出来")) {
                if (Online_swift_Offline) {
                    if (mySwift_Contact != null) {
                        getInternalApi().speakOfflineRequest("短信内容为" + mySwift_Content + "请问需要回复吗");
//                        Online_swift_Offline = false;
                        starte_Config = true;
                    }
//                    action_Send_Message = true;
                }else {
                        if (myNotification_Content != null) {
                            getInternalApi().speakOfflineRequest("短信内容为" + myNotification_Content+"请问需要回复吗");
                            starte_Config = true;
                        }
                    }
            }
            if (regExTest(payload.getContent(), "回复|发回去")) {
                parareset();
                action_Send_Message = true;
                if (Online_swift_Offline) {
                    if (!(Match_Number_In_Phone(mySwift_Contact, contact_list) == null)) {
                        get_Number = true;
                        catch_Number = Match_Number_In_Phone(mySwift_Contact, contact_list);
                    } else {
                        get_Number = true;
                        catch_Number = mySwift_Contact;
                    }
                    Online_swift_Offline = false;
                }
                else {
                    if (!(Match_Number_In_Phone(myNotification_Contact, contact_list) == null)) {
                        get_Number = true;
                        catch_Number = Match_Number_In_Phone(myNotification_Contact, contact_list);
                    } else {
                        get_Number = true;
                        catch_Number = myNotification_Contact;
                    }
                }
            }

            if (action_Send_Message && get_Number && to_get_Message_Content) {
                get_Message_Content = true;
                catch_Message_Content = payload.getContent();
            }

            if (!action_Call_Phone && !action_Send_Message && !action_Answer_Phone && !action_Reject_Phone && get_Number) {
                getInternalApi().speakOfflineRequest("请问你要打电话还是发短信");
                parareset();
                get_Number = true;
                starte_Config = true;
            }
            if (action_Call_Phone) {
                if (action_Call_Phone && !get_Number) {
                    getInternalApi().speakOfflineRequest("请问你要打电话给谁");
                    parareset();
                    starte_Config = true;
                    action_Call_Phone = true;
                }else if (action_Call_Phone && get_Number) {
                    parareset();
                    time_to_call = true;
                    if (!(Match_Name_In_Phone(catch_Number,contact_list)==null)) {
                        call_Name = Match_Name_In_Phone(catch_Number,contact_list);
                    }
                    else {
                        call_Name = catch_Number;
                    }
                    getInternalApi().speakOfflineRequest("正在给"+call_Name + "\n" + "打电话" + "\n"+"正在给"+ call_Name + "\n" + "打电话" );
                }
            }

            if (action_Send_Message) {
                if (action_Send_Message && !get_Number && !get_Message_Content) {
                    getInternalApi().speakOfflineRequest("请问你要发短信给谁");
                    parareset();
                    starte_Config = true;
                    action_Send_Message = true;
                }
                else if (action_Send_Message && get_Number && !get_Message_Content) {
                    getInternalApi().speakOfflineRequest("请问内容是什么");
                    parareset();
                    action_Send_Message = true;
                    get_Number = true;
                    to_get_Message_Content = true;
                    starte_Config = true;
                }
                else if (action_Send_Message && get_Number && get_Message_Content) {
                        sendMessage(catch_Number, catch_Message_Content);
                        Online_swift_Offline = false;
                        parareset();
                }
            }

            if (action_Answer_Phone) {
                parareset();
                acceptCall();
            }
            if (action_Reject_Phone) {
                parareset();
                endPhone();
            }
        }
    }

    private void mySpeakeOffline(String contact, String content ) {

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
        if (regExTest(contact+content, keyWord1) | regExTest(contact+content, keyWord2) | regExTest(contact+content, keyWord3)) {
            key_word_match = true;
        }
        Log.d("key_word_match",String.valueOf(key_word_match));
        if (time_setting_match && bluetooth_speak && key_word_match) {

            getInternalApi().speakOfflineRequest(contact+"来短信了"+"\n"+contact+"来短信了"+"\n"+"请问需要朗读吗");
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

    private void myWakeUp() {
        getInternalApi().startWakeup();
    }

    private void parareset() {
        action_Call_Phone = false;
        action_Send_Message = false;
        action_Answer_Phone = false;
        action_Reject_Phone = false;
        get_Number = false;
        get_Message_Content = false;
        to_get_Message_Content = false;
        starte_Config = false;
        time_to_call =false;
    }

    private void endPhone() {
        Method method = null;
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{"phone"});
            ITelephony telephony = ITelephony.Stub.asInterface(binder);
            telephony.endCall();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Online_swift_Offline = false;
    }

    public void acceptCall() {
        if (AppApplication.acceptIntent != null) {
            try {
                AppApplication.acceptIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
        Online_swift_Offline = false;
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

    //提取文本中的手机号码
    public static String Get_Number_In_Text(String content) {
        String n = null;
        Pattern p = Pattern.compile("\\d{3}-\\d{8}|\\d{4}-\\d{7}|\\d{11}");
        Matcher matcher = p.matcher(content);
        if (matcher.find()) {
            n = matcher.group(0);
            return n;
        }
        return "号码错误";
    }


    //新建一个手机联系人的数据类型,方便下面查找联系人
    public class PhoneDto {
        private String name;        //联系人姓名
        private String telPhone;    //电话号码


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTelPhone() {
            return telPhone;
        }

        public void setTelPhone(String telPhone) {
            this.telPhone = telPhone;
        }

        public PhoneDto() {
        }

        public PhoneDto(String name, String telPhone) {
            this.name = name;
            this.telPhone = telPhone;
        }
    }

    //获取所有手机联系人，返回一个PhoneDtos的列表
    public class Get_Phone_Contact {

        // 号码
        public final static String NUM = ContactsContract.CommonDataKinds.Phone.NUMBER;
        // 联系人姓名
        public final static String NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;

        //上下文对象
        private Context context;
        //联系人提供者的uri
        private Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        public Get_Phone_Contact(Context context) {
            this.context = context;
        }

        //获取所有联系人
        public List<PhoneDto> getPhone() {
            List<PhoneDto> phoneDtos = new ArrayList<>();
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(phoneUri, new String[]{NUM, NAME}, null, null, null);
            while (cursor.moveToNext()) {
                PhoneDto phoneDto = new PhoneDto(cursor.getString(cursor.getColumnIndex(NAME)), cursor.getString(cursor.getColumnIndex(NUM)));
                phoneDtos.add(phoneDto);
            }
            return phoneDtos;
        }
    }

    private String Get_Phone_Name () {
        Get_Phone_Contact get_phone_contact = new Get_Phone_Contact(this);
        List<PhoneDto> phoneDtoList = get_phone_contact.getPhone();
        String phoneContact = null;
        for (int i = 0; i < phoneDtoList.size(); i++) {
            if (i==0) {
                phoneContact = phoneDtoList.get(i).getName();
            }else {
                phoneContact = phoneContact+"\n"+phoneDtoList.get(i).getName();
            }
        }
        return phoneContact;
    }

    private String Match_Name_In_Phone (String call_number,List<PhoneDto> list) {
        String matchName = null;
        for (int i = 0; i < list.size(); i++) {
            if (call_number.equals(list.get(i).getTelPhone())) {
                matchName = list.get(i).getName();
                break;
            }
            else {
                matchName = null;
            }
        }
        return matchName;
    }

//    private String Match_Number_In_Phone(String name, List<PhoneDto> list) {
//        String matchNumber = null;
//        stringCompare = new StringCompare();
//        myLevenshtein = new MyLevenshtein();
//        for (int i = 0; i < list.size(); i++) {
//            String maxLengthCommonString = stringCompare.getMaxLengthCommonString(name, getPinYin(list.get(i).getName()));
//            float similarity = myLevenshtein.levenshtein(maxLengthCommonString, getPinYin(list.get(i).getName()));
//            System.out.println("相似度：" + similarity);
//            if (similarity >= 0.8) {
//                matchNumber = list.get(i).getTelPhone();
//                Log.d(list.get(i).getName(), list.get(i).getTelPhone());
//                break;
//            } else {
//                matchNumber = null;
//            }
//        }
//        return matchNumber;
//    }
    private String Match_Number_In_Phone (String text, List<PhoneDto> list) {
        String matchNumber = null;
        for (int i = 0;i<list.size();i++) {
            if (regExTest(text,list.get(i).getName())) {
                matchNumber = list.get(i).getTelPhone();
                break;
            }
        }
        return matchNumber;
    }

    /*
    这边主要问题是在Android7.0时电话的分类更加细致了
    INCOMING_TYPE，OUTGOING_TYPE，REJECTED_TYPE，MISSED_TYPE
    除了OUTGOING_TYPE的电话我不需要回拨之外，其他的都存在回拨的可能性
    所以在这些里面我要获取最新的。供用户回拨！
     */
    public String get_Lastest_Phone_Number() {
        String result = null;
        @SuppressLint("MissingPermission") Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                new String[]{
                        CallLog.Calls.TYPE,
                        CallLog.Calls.NUMBER},
                null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
        boolean hasRecord = cursor.moveToFirst();
        int count = 0;
        String strPhone = "";
        String date;
        while (hasRecord && (result == null)) {
            int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
            strPhone = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
//            result = result + "phone :" + strPhone + ",";
            switch (type) {
//                    对方来电，接通
                case CallLog.Calls.INCOMING_TYPE:
                    result = strPhone;
                    break;
//                    打出去
                case CallLog.Calls.OUTGOING_TYPE:
                    break;
//                    对方来电，拒接
                case CallLog.Calls.REJECTED_TYPE:
                    result = strPhone;
                    break;
//                    对方来点，未接
                case CallLog.Calls.MISSED_TYPE:
                    result = strPhone;
                    break;
                default:
                    break;
            }
            hasRecord = cursor.moveToNext();
        }
        return result;

    }


    // 输入汉字返回拼音的通用方法函数。

    public static String getPinYin(String hanzi) {
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(hanzi);
        StringBuilder sb = new StringBuilder();
        if (tokens != null && tokens.size() > 0) {
            for (HanziToPinyin.Token token : tokens) {
                if (HanziToPinyin.Token.PINYIN == token.type) {
                    sb.append(token.target);
                } else {
                    sb.append(token.source);
                }
            }
        }
        return sb.toString().toUpperCase();
    }

    //找出公共字符串
    public class StringCompare {
        private int a;
        private int b;
        private int maxLength = -1;

        public String getMaxLengthCommonString(String s1, String s2) {

            if (s1 == null || s2 == null) {
                return null;
            }

            a = s1.length();//s1长度做行
            b = s2.length();//s2长度做列

            if (a == 0 || b == 0) {
                return "";
            }

            //设置匹配矩阵
            boolean[][] array = new boolean[a][b];
            for (int i = 0; i < a; i++) {
                char c1 = s1.charAt(i);
                for (int j = 0; j < b; j++) {
                    char c2 = s2.charAt(j);
                    if (c1 == c2) {
                        array[i][j] = true;
                    } else {
                        array[i][j] = false;
                    }
                }
            }

            //求所有公因子字符串，保存信息为相对第二个字符串的起始位置和长度
            List<ChildString> childStrings = new ArrayList<ChildString>();
            for (int i = 0; i < a; i++) {
                getMaxSort(i, 0, array, childStrings);
            }

            for (int i = 1; i < b; i++) {
                getMaxSort(0, i, array, childStrings);
            }

            StringBuffer sb = new StringBuffer();
            for (ChildString s : childStrings) {
                sb.append(s2.substring(s.maxStart, s.maxStart + s.maxLength));
//                sb.append("\n");
            }
            return sb.toString();
        }


        //求一条斜线上的公因子字符串
        private void getMaxSort(int i, int j, boolean[][] array, List<ChildString> sortBean) {
            int length = 0;
            int start = j;
            for (; i < a && j < b; i++, j++) {
                if (array[i][j]) {
                    length++;
                } else {

                    //直接add，保存所有子串，下面的判断，只保存当前最大的子串
                    //sortBean.add(new ChildString(length, start));
                    if (length == maxLength) {
                        sortBean.add(new ChildString(length, start));
                    } else if (length > maxLength) {
                        sortBean.clear();
                        maxLength = length;
                        sortBean.add(new ChildString(length, start));
                    }

                    length = 0;
                    start = j + 1;
                }

                if (i == a - 1 || j == b - 1) {

                    //直接add，保存所有子串，下面的判断，只保存当前最大的子串
                    //sortBean.add(new ChildString(length, start));
                    if (length == maxLength) {
                        sortBean.add(new ChildString(length, start));
                    } else if (length > maxLength) {
                        sortBean.clear();
                        maxLength = length;
                        sortBean.add(new ChildString(length, start));
                    }
                }
            }
        }

        //公因子类
        class ChildString {
            int maxLength;
            int maxStart;

            ChildString(int maxLength, int maxStart) {
                this.maxLength = maxLength;
                this.maxStart = maxStart;
            }
        }

//        /**
//         * @param args
//         */
//        public  void main(String[] args) {
//            // TODO Auto-generated method stub
//            System.out.println(new StringCompare().getMaxLengthCommonString("abcdef", "defabc"));
//        }
    }


    /**
     * @className:MyLevenshtein.java
     * @classDescription:Levenshtein Distance 算法实现
     * 可以使用的地方：DNA分析 　　拼字检查 　　语音辨识 　　抄袭侦测
     * @author:donghai.wan
     * @createTime:2012-1-12
     */

    public class MyLevenshtein {
        public void main(String[] args) {
            //要比较的两个字符串
            String str1 = "weiyong";
            String str2 = "hejiong";
            levenshtein(str1, str2);
        }

        /**
         * 　　DNA分析 　　拼字检查 　　语音辨识 　　抄袭侦测
         *
         * @createTime 2012-1-12
         */

        public float levenshtein(String str1, String str2) {

            //计算两个字符串的长度。
            int len1 = str1.length();
            int len2 = str2.length();

            //建立上面说的数组，比字符长度大一个空间
            int[][] dif = new int[len1 + 1][len2 + 1];
            //赋初值，步骤B。
            for (int a = 0; a <= len1; a++) {
                dif[a][0] = a;
            }

            for (int a = 0; a <= len2; a++) {
                dif[0][a] = a;
            }

            //计算两个字符是否一样，计算左上的值
            int temp;
            for (int i = 1; i <= len1; i++) {
                for (int j = 1; j <= len2; j++) {
                    if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                        temp = 0;
                    } else {
                        temp = 1;
                    }

                    //取三个值中最小的
                    dif[i][j] = min(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1,
                            dif[i - 1][j] + 1);
                }
            }
//            System.out.println("字符串\"" + str1 + "\"与\"" + str2 + "\"的比较");
//
//            //取数组右下角的值，同样不同位置代表不同字符串的比较
//            System.out.println("差异步骤：" + dif[len1][len2]);

            //计算相似度
            float similarity = 1 - (float) dif[len1][len2] / Math.max(str1.length(), str2.length());
            return similarity;
//            System.out.println("相似度：" + similarity);
        }

        //得到最小值

        private int min(int... is) {
            int min = Integer.MAX_VALUE;
            for (int i : is) {
                if (min > i) {
                    min = i;
                }
            }
            return min;
        }
    }


//    @Override
//    protected void onDestroy (){
//        super.onDestroy();
//        Log.d("onDestroy","onDestroy");
//        unregisterReceiver(receiver);
//        getInternalApi().removeRequestBodySentListener(dcsRequestBodySentListener1);
//        dcsRequestBodySentListener1 = null;
//        dcsSdk.release();
//    }
}