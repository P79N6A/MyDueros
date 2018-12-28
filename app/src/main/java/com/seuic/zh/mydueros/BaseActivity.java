package com.seuic.zh.mydueros;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.duer.dcs.api.DcsSdkBuilder;
import com.baidu.duer.dcs.api.IConnectionStatusListener;
import com.baidu.duer.dcs.api.IDcsSdk;
import com.baidu.duer.dcs.api.IDialogStateListener;
import com.baidu.duer.dcs.api.IDirectiveIntercepter;
import com.baidu.duer.dcs.api.IFinishedDirectiveListener;
import com.baidu.duer.dcs.api.IMessageSender;
import com.baidu.duer.dcs.api.IVoiceRequestListener;
import com.baidu.duer.dcs.api.config.DcsConfig;
import com.baidu.duer.dcs.api.config.DefaultSdkConfigProvider;
import com.baidu.duer.dcs.api.config.SdkConfigProvider;
import com.baidu.duer.dcs.api.player.ITTSPositionInfoListener;
import com.baidu.duer.dcs.api.recorder.AudioRecordImpl;
import com.baidu.duer.dcs.api.recorder.BaseAudioRecorder;
import com.baidu.duer.dcs.api.wakeup.BaseWakeup;
import com.baidu.duer.dcs.api.wakeup.IWakeupAgent;
import com.baidu.duer.dcs.api.wakeup.IWakeupProvider;
import com.baidu.duer.dcs.componentapi.AbsDcsClient;
import com.baidu.duer.dcs.devicemodule.custominteraction.CustomUserInteractionDeviceModule;
import com.baidu.duer.dcs.framework.DcsSdkImpl;
import com.baidu.duer.dcs.framework.ILoginListener;
import com.baidu.duer.dcs.framework.InternalApi;
import com.baidu.duer.dcs.framework.internalapi.IDirectiveReceivedListener;
import com.baidu.duer.dcs.framework.internalapi.IErrorListener;
import com.baidu.duer.dcs.oauth.api.code.OauthCodeImpl;
import com.baidu.duer.dcs.systeminterface.IOauth;
import com.baidu.duer.dcs.util.AsrType;
import com.baidu.duer.dcs.util.DcsErrorCode;
import com.baidu.duer.dcs.util.HttpProxy;
import com.baidu.duer.dcs.util.api.IDcsRequestBodySentListener;
import com.baidu.duer.dcs.util.dispatcher.DialogRequestIdHandler;
import com.baidu.duer.dcs.util.message.DcsRequestBody;
import com.baidu.duer.dcs.util.message.Directive;
import com.baidu.duer.dcs.util.message.Payload;
import com.baidu.duer.dcs.util.util.CommonUtil;
import com.baidu.duer.dcs.util.util.NetWorkUtil;
import com.baidu.duer.dcs.util.util.StandbyDeviceIdUtil;
import com.baidu.duer.kitt.KittWakeUpImpl;
import com.baidu.duer.kitt.KittWakeUpServiceImpl;
import com.baidu.duer.kitt.WakeUpConfig;
import com.baidu.duer.kitt.WakeUpWord;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.alarms.AlarmsDeviceModule;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.alarms.message.SetAlarmPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.alarms.message.SetTimerPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.alarms.message.ShowAlarmsPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.alarms.message.ShowTimersPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.app.AppDeviceModule;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.app.message.LaunchAppPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.app.message.TryLaunchAppPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.applauncher.AppLauncherDeviceModule;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.applauncher.IAppLauncher;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.applauncher.message.AppInfo;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.DeviceControlDeviceModule;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.AdjustBrightnessPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetAssistiveTouchPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetBluetoothPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetBrightnessPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetCellularModePayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetCellularPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetGpsPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetHotspotPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetNfcPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetPhoneModePayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetPhonePowerPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetPortraitLockPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetSynchronizationPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetVibrationPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetVpnPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.devicecontrol.message.SetWifiPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.phonecall.PhoneCallDeviceModule;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.phonecall.message.PhonecallByNamePayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.phonecall.message.PhonecallByNumberPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.phonecall.message.SelectCalleePayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.screen.ScreenDeviceModule;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.screen.extend.card.ScreenExtendDeviceModule;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.screen.extend.card.message.RenderAudioListPlayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.screen.extend.card.message.RenderPlayerInfoPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.screen.message.HtmlPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.screen.message.RenderCardPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.screen.message.RenderHintPayload;
import com.seuic.zh.mydueros.sample.sdk.devicemodule.screen.message.RenderVoiceInputTextPayload;
import com.seuic.zh.mydueros.widget.DcsWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.seuic.zh.mydueros.Main2Activity;
public abstract class BaseActivity extends AppCompatActivity implements
        View.OnClickListener {
    public static final String TAG = "DCS-SDK";
    // demo使用的CLIENT_ID，正式产品请用自己申请的CLIENT_ID、PID
    public static final String CLIENT_ID = "uqDk9qvUwoThE2d5Hx4DYkPWqcaO7uHQ";
    public static final int PID = 1704 ;
    public static final String APP_KEY = "com.baidu.dumi.open.far";
    // 唤醒配置
    // 格式必须为：浮点数，用','分隔，每个模型对应3个灵敏度
    // 例如有2个模型,就需要6个灵敏度，0.35,0.35,0.40,0.45,0.45,0.55
    private static final String WAKEUP_RES_PATH = "snowboy/common.res";
    private static final String WAKEUP_UMDL_PATH = "snowboy/xiaoduxiaodu_all_11272017.umdl";
    private static final String WAKEUP_SENSITIVITY = "0.35,0.35,0.40";
    private static final String WAKEUP_HIGH_SENSITIVITY = "0.45,0.45,0.55";
    // 唤醒成功后是否需要播放提示音
    private static final boolean ENABLE_PLAY_WARNING = true;
    private static final int REQUEST_CODE = 123;


    protected EditText textInput;
    protected Button sendButton;
    protected IDcsSdk dcsSdk;
    protected ScreenDeviceModule screenDeviceModule;
    protected PhoneCallDeviceModule phoneCallDeviceModule;
    protected com.seuic.zh.mydueros.sample.sdk.devicemodule.sms.SmsDeviceModule smsDeviceModule;
    protected AppLauncherDeviceModule appLauncherDeviceModule;
    protected AppDeviceModule appDeviceModule;
    protected DeviceControlDeviceModule deviceControlDeviceModule;
    protected AlarmsDeviceModule alarmsDeviceModule;
    public final static String B_PHONE_STATE = TelephonyManager.ACTION_PHONE_STATE_CHANGED;
    private Button nextButton;
    private Button preButton;
    private Button playButton;
    private Button voiceButton;
    private Button cancelVoiceButton;
    private boolean isPlaying;
    private TextView textViewWakeUpTip;
    private LinearLayout mTopLinearLayout;
    public DcsWebView dcsWebView;
    //    private ILocation location;
    // for dcs统计-demo
    public long duerResultT;
    // for dcs统计-demo
    protected TextView textViewRenderVoiceInputText;
    public IDialogStateListener dialogStateListener;
    public IDialogStateListener.DialogState currentDialogState = IDialogStateListener.DialogState.IDLE;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setTitle(BuildConfig.APP_TITLE);

        initViews();
        initPermission();
        initSdk();
        sdkRun();
        initListener();
//        initLocation();

    }

    protected void initListener() {
        // 设置各种监听器
        dcsSdk.addConnectionStatusListener(connectionStatusListener);
        // 错误
        getInternalApi().addErrorListener(errorListener);
        // event发送
        getInternalApi().addRequestBodySentListener(dcsRequestBodySentListener);
        // 对话状态
        initDialogStateListener();
        // 语音文本同步
        initTTSPositionInfoListener();
        // 唤醒
        initWakeUpAgentListener();
        // 所有指令透传，建议在各自的DeviceModule中处理
        addDirectiveReceivedListener();
        // 指令执行完毕回调
        initFinishedDirectiveListener();
        // 语音音量回调监听
        initVolumeListener();
        initVoiceErrorListener();
        initDirectiveIntercepter();
    }

//    private void initLocation() {
//        // 定位
//        location = new LocationImpl(getApplicationContext());
//        location.requestLocation(false);
//        // 需要定位后赋值
//        // 目前是写死的北京的
//        getInternalApi().setLocationHandler(locationHandler);
//    }
//
//    protected Location.LocationHandler locationHandler = new Location.LocationHandler() {
//        @Override
//        public double getLongitude() {
//            if (location == null) {
//                return 0;
//            }
//            return location.getLocationInfo().longitude;
//        }
//
//        @Override
//        public double getLatitude() {
//            if (location == null) {
//                return 0;
//            }
//            return location.getLocationInfo().latitude;
//        }
//
//        @Override
//        public String getCity() {
//            if (location == null) {
//                return "";
//            }
//            return location.getLocationInfo().city;
//        }
//
//        @Override
//        public Location.EGeoCoordinateSystem getGeoCoordinateSystem() {
//            return Location.EGeoCoordinateSystem.BD09LL;
//        }
//    };

    private ScreenDeviceModule.IScreenListener screenListener = new ScreenDeviceModule.IScreenListener() {
        @Override
        public void onRenderVoiceInputText(RenderVoiceInputTextPayload payload) {
            handleRenderVoiceInputTextPayload(payload);
        }

        @Override
        public void onHtmlPayload(HtmlPayload htmlPayload) {
            handleHtmlPayload(htmlPayload);
        }

        @Override
        public void onRenderCard(RenderCardPayload renderCardPayload) {

        }

        @Override
        public void onRenderHint(RenderHintPayload renderHintPayload) {

        }
    };


    //打电话监听器
    private PhoneCallDeviceModule.IPhoneCallListener phoneCallListener = new PhoneCallDeviceModule.IPhoneCallListener() {
        @Override
        public void onPhoneCallByName(PhonecallByNamePayload payload) {
            handlePhonecallByNamePayload(payload);
        }

        @Override
        public void onSelectCallee(SelectCalleePayload payload) {

        }

        @Override
        public void onPhoneCallByNumber(PhonecallByNumberPayload payload) {

            handlePhonecallByNumberPayload(payload);

        }
    };



    //发短信监听器
    private com.seuic.zh.mydueros.sample.sdk.devicemodule.sms.SmsDeviceModule.ISmsListener smsListener = new com.seuic.zh.mydueros.sample.sdk.devicemodule.sms.SmsDeviceModule.ISmsListener() {
        @Override
        public void onSendSmsByName(com.seuic.zh.mydueros.sample.sdk.devicemodule.sms.message.SendSmsByNamePayload payload) {
            handleSendSmsByNamePayload(payload);
        }

        @Override
        public void onSelectRecipient(com.seuic.zh.mydueros.sample.sdk.devicemodule.sms.message.SelectRecipientPayload payload) {

        }

        @Override
        public void onSendSmsByNumber(com.seuic.zh.mydueros.sample.sdk.devicemodule.sms.message.SendSmsByNumberPayload payload) {

            handleSendSmsByNumberPayload(payload);
        }
    };

//    打开应用监听器
    private AppDeviceModule.IAppListener appListener = new AppDeviceModule.IAppListener() {
        @Override
        public void onTryLaunchApp(TryLaunchAppPayload payload) {
            handleTryLaunchAppPayload(payload);
        }

        @Override
        public void onLaunchApp(LaunchAppPayload payload) {

        }
    };


    private AppLauncherDeviceModule.IAppLauncherListener appLauncherListener = new AppLauncherDeviceModule.IAppLauncherListener() {
        @Override
        public void onLaunchApp(com.seuic.zh.mydueros.sample.sdk.devicemodule.applauncher.message.LaunchAppPayload payload) {
            handleLaunchApp(payload);
        }
    };

    private DeviceControlDeviceModule.IDeviceControlListener deviceControlListener = new DeviceControlDeviceModule.IDeviceControlListener() {
        @Override
        public void onAdjustBrightness(AdjustBrightnessPayload payload) {

        }

        @Override
        public void onSetAssistiveTouch(SetAssistiveTouchPayload payload) {

            handleToucher(payload);
        }

        @Override
        public void onSetBluetooth(SetBluetoothPayload payload) {

        }

        @Override
        public void onSetBrightness(SetBrightnessPayload payload) {

        }

        @Override
        public void onSetCellular(SetCellularPayload payload) {

        }

        @Override
        public void onSetCellularMode(SetCellularModePayload payload) {

        }

        @Override
        public void onSetGps(SetGpsPayload payload) {

        }

        @Override
        public void onSetHotspot(SetHotspotPayload payload) {

        }

        @Override
        public void onSetNfc(SetNfcPayload payload) {

        }

        @Override
        public void onSetPhoneMode(SetPhoneModePayload payload) {

        }

        @Override
        public void onSetPhonePower(SetPhonePowerPayload payload) {

            handleSetPhonePowerPayload(payload);
        }

        @Override
        public void onSetPortraitLock(SetPortraitLockPayload payload) {

        }

        @Override
        public void onSetSynchronization(SetSynchronizationPayload payload) {

        }

        @Override
        public void onSetVibration(SetVibrationPayload payload) {

        }

        @Override
        public void onSetVpn(SetVpnPayload payload) {

        }

        @Override
        public void onSetWifi(SetWifiPayload payload) {
            handleSetWifiPayload(payload);
        }
    };

    private void handleSetPhonePowerPayload (SetPhonePowerPayload payload) {

        wakeUpAndUnlock();
    }

    private void handleSetWifiPayload (SetWifiPayload payload) {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (payload.getWifi()) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        }else {
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
        }
    }


    public void wakeUpAndUnlock() {
        // 获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();
        if (!screenOn) {
            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wl.acquire(10000); // 点亮屏幕
            wl.release(); // 释放
        }
        // 屏幕解锁
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        // 屏幕锁定
        keyguardLock.reenableKeyguard();
        keyguardLock.disableKeyguard(); // 解锁
    }


    public void handleHtmlPayload(HtmlPayload payload) {
        dcsWebView.loadUrl(payload.getUrl());
        duerResultT = System.currentTimeMillis();
    }

    public void handleRenderVoiceInputTextPayload(RenderVoiceInputTextPayload payload) {
        textViewRenderVoiceInputText.setText(payload.text);
    }

    private void handleToucher (SetAssistiveTouchPayload payload) {


        if (!Settings.canDrawOverlays(BaseActivity.this))
        {
            //若没有权限，提示获取.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            getInternalApi().speakRequest("需要取得权限以使用悬浮窗");
//            Toast.makeText(BaseActivity.this,"需要取得权限以使用悬浮窗",Toast.LENGTH_SHORT).show();
            startActivity(intent);
        } else  {
                if (payload.getAssistiveTouch()) {
                        Intent intent = new Intent(BaseActivity.this,ToucherService.class);
                        Toast.makeText(BaseActivity.this,"已开启Toucher",Toast.LENGTH_SHORT).show();
                        startService(intent);
                }else{
                    Intent intent = new Intent(BaseActivity.this,ToucherService.class);
                    stopService(intent);
                }
        }




    }

    //获取云端返回联系人号码
    public void handlePhonecallByNumberPayload(PhonecallByNumberPayload payload) {
        String callNumber = payload.getCallee().getPhoneNumber();
        callOut(callNumber);
    }
    //获取云端返回联系人名字
    public void handlePhonecallByNamePayload(PhonecallByNamePayload payload) {
        String callName = payload.getCandidateCallees().get(0).contactName;
        String callNumber = getPhoneByName(this,callName).get(0);
        callOut(callNumber);
    }

    //通过姓名获取手机联系人号码
    public synchronized static ArrayList<String> getPhoneByName(Context context, String name) {
        ArrayList<String> numbers = new ArrayList<String>();
        String number=null;
        ContentResolver resolver = context.getContentResolver();
        String[] projection=new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?",
                new String[]{name}, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                number= cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if (number!=null&&number!="") {
                    numbers.add(number);
                }
            }
        }
        return numbers;
    }

    //打电话操作
    public void callOut (String string){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:"+string));
        startActivity(intent);
    }

    //获取云端返回的短信收件人号码
    private void handleSendSmsByNumberPayload (com.seuic.zh.mydueros.sample.sdk.
                                                       devicemodule.sms.message.SendSmsByNumberPayload payload) {
        String sendNumber = payload.getRecipient().getPhoneNumber();
        String sendContent = payload.getMessageContent();
        sendMessage(sendNumber,sendContent);
    }

    private void handleSendSmsByNamePayload (com.seuic.zh.mydueros.sample.sdk.
                                                     devicemodule.sms.message.SendSmsByNamePayload payload){
        String sendName = payload.getCandidateRecipients().get(0).getContactName();
        String sendNumber = getPhoneByName(this,sendName).get(0);
        String sendContent = payload.getMessageContent();
        sendMessage(sendNumber,sendContent);
    }

    //发短信操作
    public void sendMessage (String sendNumber, String sendContent){
        delay(1000);
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+sendNumber));
        intent.putExtra("sms_body", sendContent);
        startActivity(intent);
    }


    //打开应用操作
    private void handleTryLaunchAppPayload (TryLaunchAppPayload payload){
        String appName = payload.getAppName();
        launchApp(appName);
    }

    private void handleLaunchApp (com.seuic.zh.mydueros.sample.sdk.devicemodule.applauncher.message.LaunchAppPayload payload) {
        String appName = payload.getAppName();
        launchApp(appName);
    }

    // 获取手机里的应用列表
    public void launchApp (String appName) {
        PackageManager packageManager = BaseActivity.this.getPackageManager();
        List<PackageInfo> pInfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pInfo.size(); i++)
        {
            PackageInfo p = pInfo.get(i);
            // 获取相关包的<application>中的label信息，也就是-->应用程序的名字
            String label = packageManager.getApplicationLabel(p.applicationInfo).toString();
            System.out.println(label);
            if (label.equals(appName)){ //比较label
                String pName = p.packageName; //获取包名
                Intent intent = new Intent();
                //获取intent
                intent =packageManager.getLaunchIntentForPackage(pName);
                startActivity(intent);
            }
        }
    }

    public IDcsRequestBodySentListener dcsRequestBodySentListener = new IDcsRequestBodySentListener() {

        public String eventName;
        @Override
        public void onDcsRequestBody(DcsRequestBody dcsRequestBody) {
            eventName = dcsRequestBody.getEvent().getHeader().getName();
            Log.v(TAG, "eventName:" + eventName);

//            if (eventName.equals("SpeechFinished")&& my_config ){
//                delay(1000);
//                beginVoiceRequest(true);
//                getInternalApi().startWakeup();
//            }

            if (eventName.equals("PlaybackStopped") || eventName.equals("PlaybackFinished")
                    || eventName.equals("PlaybackFailed")) {
                playButton.setText("等待音乐");
                isPlaying = false;
            } else if (eventName.equals("PlaybackPaused")) {
                playButton.setText("暂停中");
                isPlaying = false;
            } else if (eventName.equals("PlaybackStarted") || eventName.equals("PlaybackResumed")) {
                playButton.setText("播放中...");
                isPlaying = true;
            }
        }
    };


    public void delay(int ms){

        try {

            Thread.currentThread();

            Thread.sleep(ms);

        } catch (InterruptedException e) {

            e.printStackTrace();

        }

    }




    private IErrorListener errorListener = new IErrorListener() {
        @Override
        public void onErrorCode(DcsErrorCode errorCode) {
            if (errorCode.error == DcsErrorCode.VOICE_REQUEST_EXCEPTION) {
                if (errorCode.subError == DcsErrorCode.NETWORK_UNAVAILABLE) {
                    Toast.makeText(BaseActivity.this,
                            "网络不可用",
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(BaseActivity.this,
                            getResources().getString(R.string.voice_err_msg),
                            Toast.LENGTH_SHORT)
                            .show();
                }

            } else if (errorCode.error == DcsErrorCode.LOGIN_FAILED) {
                // 未登录
                Toast.makeText(BaseActivity.this,
                        "未登录",
                        Toast.LENGTH_SHORT)
                        .show();
            } else if (errorCode.subError == DcsErrorCode.UNAUTHORIZED_REQUEST) {
                // 以下仅针对 passport 登陆情况下的账号刷新，非 passport 刷新请参看文档。
            }
        }
    };

    private IConnectionStatusListener connectionStatusListener = new IConnectionStatusListener() {
        @Override
        public void onConnectStatus(ConnectionStatus connectionStatus) {
            Log.d(TAG, "onConnectionStatusChange: " + connectionStatus);

        }
    };

    /**
     * tts文字同步
     */
    private void initTTSPositionInfoListener() {
        getInternalApi().addTTSPositionInfoListener(new ITTSPositionInfoListener() {
            @Override
            public void onPositionInfo(long pos, long playTimeMs, long mark) {
            }
        });
    }

    /**
     * 语音音量回调监听
     */
    private void initVolumeListener() {
        getInternalApi().getDcsClient().addVolumeListener(new AbsDcsClient.IVolumeListener() {
            @Override
            public void onVolume(int volume, int percent) {
                Log.d(TAG, "volume  ----->" + volume);
                Log.d(TAG, "percent ----->" + percent);
            }
        });
    }




    /**
     * 语音错误回调监听
     */
    private void initVoiceErrorListener() {
        getInternalApi().getDcsClient().addVoiceErrorListener(new AbsDcsClient.IVoiceErrorListener() {
            @Override
            public void onVoiceError(int error, int subError) {
                Log.d(TAG, "onVoiceError:" + error + " " + subError);
            }
        });
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ArrayList<String> toApplyList = new ArrayList<>();
        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                    perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.
            }
        }
        if (!toApplyList.isEmpty()) {
            String tmpList[] = new String[toApplyList.size()];
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。

    }

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Exception ignored) {
            // LEFT-DO-NOTHING
        }
    }

    protected void initSdk() {
        // 第一步初始化sdk
        // BaseAudioRecorder audioRecorder = new PcmAudioRecorderImpl(); pcm 输入方式
        BaseAudioRecorder audioRecorder = new AudioRecordImpl();
        IOauth oauth = getOauth();
        // 唤醒单独开启唤醒进程；  如果不需要将唤醒放入一个单独进程，可以使用KittWakeUpImpl
        final BaseWakeup wakeup = new KittWakeUpImpl(audioRecorder);
        // 百度语音团队的离线asr和百度语音团队的唤醒，2个so库冲突，暂时不要用WakeupImpl实现的唤醒功能！！
//        final BaseWakeup wakeup = new WakeupImpl();
        final IWakeupProvider wakeupProvider = new IWakeupProvider() {
            @Override
            public WakeUpConfig wakeUpConfig() {
                // 添加多唤醒词和索引
                // 此处传入的index需要和Snowboy唤醒模型文件一致
                // 例：模型文件中有3个唤醒词，分别为不同语速的"小度小度"，index分别为1-3，则需要按照以下格式添加
                // 唤醒成功后，回调中会包含被唤醒的WakeUpWord
                List<WakeUpWord> wakeupWordList = new ArrayList<>();
                wakeupWordList.add(new WakeUpWord(1, "小度小度"));
                wakeupWordList.add(new WakeUpWord(2, "小度小度"));
                wakeupWordList.add(new WakeUpWord(3, "小度小度"));
                wakeupWordList.add(new WakeUpWord(4, "小度小度"));
                wakeupWordList.add(new WakeUpWord(5, "小度小度"));
                final List<String> umdlPaths = new ArrayList<>();
                umdlPaths.add(WAKEUP_UMDL_PATH);
                return new WakeUpConfig.Builder()
                        .resPath(WAKEUP_RES_PATH)
                        .umdlPath(umdlPaths)
                        .sensitivity(WAKEUP_SENSITIVITY)
                        .highSensitivity(WAKEUP_HIGH_SENSITIVITY)
                        .wakeUpWords(wakeupWordList)
                        .build();
            }

            @Override
            public boolean enableWarning() {
                return ENABLE_PLAY_WARNING;
            }

            @Override
            public String warningSource() {
                // 每次在播放唤醒提示音前调用该方法
                // assets目录下的以assets://开头
                // 文件为绝对路径
                return "assets://ding.wav";
            }

            @Override
            public float volume() {
                // 每次在播放唤醒提示音前调用该方法
                // [0-1]
                return 0.8f;
            }

            @Override
            public boolean wakeAlways() {
                return BaseActivity.this.enableWakeUp();
            }

            @Override
            public BaseWakeup wakeupImpl() {
                return wakeup;
            }

            @Override
            public int audioType() {
                // 用户自定义类型
                return AudioManager.STREAM_MUSIC;
            }
        };


        // proxyIp 为代理IP
        // proxyPort  为代理port
        HttpProxy httpProxy = new HttpProxy("172.24.194.28", 8888);

        // SDK配置，ClientId、语音PID、代理等
        SdkConfigProvider sdkConfigProvider = getSdkConfigProvider();
        // 构造dcs sdk
        DcsSdkBuilder builder = new DcsSdkBuilder();
        dcsSdk = builder.withSdkConfig(sdkConfigProvider)
                .withWakeupProvider(wakeupProvider)
                .withOauth(oauth)
                .withAudioRecorder(audioRecorder)
                // 1.withDeviceId设置设备唯一ID
                // 2.强烈建议！！！！
                //   如果开发者清晰的知道自己设备的唯一id，可以按照自己的规则传入
                //   需要保证设置正确，保证唯一、刷机和升级后不变
                // 3.sdk提供的方法，但是不保证所有的设别都是唯一的
                //   StandbyDeviceIdUtil.getStandbyDeviceId()
                //   该方法的算法是MD5（android_id + imei + Mac地址）32位  +  32位UUID总共64位
                //   生成：首次按照上述算法生成ID，生成后依次存储apk内部->存储系统数据库->存储外部文件
                //   获取：存储apk内部->存储系统数据库->存储外部文件，都没有则重新生成
                .withDeviceId(StandbyDeviceIdUtil.getStandbyDeviceId())
                // 设置音乐播放器的实现，sdk 内部默认实现为MediaPlayerImpl
                // .withMediaPlayer(new MediaPlayerImpl(AudioManager.STREAM_MUSIC))
                .build();

        // 设置Oneshot
        getInternalApi().setSupportOneshot(false);
        // ！！！！临时配置需要在run之前设置！！！！
        // 临时配置开始
        // 暂时没有定的API接口，可以通过getInternalApi设置后使用
        // 设置唤醒参数后，初始化唤醒h
        getInternalApi().initWakeUp();
//        getInternalApi().setOnPlayingWakeUpSensitivity(WAKEUP_ON_PLAYING_SENSITIVITY);
//        getInternalApi().setOnPlayingWakeUpHighSensitivity(WAKEUP_ON_PLAYING_HIGH_SENSITIVITY);
        getInternalApi().setAsrMode(getAsrMode());
        // 测试数据，具体bduss值
        // getInternalApi().setBDuss("");
        // 临时配置结束
        // dbp平台
        // getInternalApi().setDebugBot("f15be387-1348-b71b-2ae5-8f19f2375ea1");

        // 第二步：可以按需添加内置端能力和用户自定义端能力（需要继承BaseDeviceModule）
        // 屏幕展示
        IMessageSender messageSender = getInternalApi().getMessageSender();

        // 上屏
        screenDeviceModule = new ScreenDeviceModule(messageSender);
        screenDeviceModule.addScreenListener(screenListener);
        dcsSdk.putDeviceModule(screenDeviceModule);

        ScreenExtendDeviceModule screenExtendDeviceModule = new ScreenExtendDeviceModule(messageSender);
        screenExtendDeviceModule.addExtensionListener(mScreenExtensionListener);
        dcsSdk.putDeviceModule(screenExtendDeviceModule);

//        添加内置端打电话模块
        phoneCallDeviceModule = new PhoneCallDeviceModule(messageSender);
        phoneCallDeviceModule.addPhoneCallListener(phoneCallListener);
        dcsSdk.putDeviceModule(phoneCallDeviceModule);

        //添加内置端发短信模块
        smsDeviceModule = new com.seuic.zh.mydueros.sample.sdk.devicemodule.sms.SmsDeviceModule(messageSender);
        smsDeviceModule.addSmsListener(smsListener);
        dcsSdk.putDeviceModule(smsDeviceModule);

        // 在线返回文本的播报，eg:你好，返回你好的播报
        DialogRequestIdHandler dialogRequestIdHandler =
                ((DcsSdkImpl) dcsSdk).getProvider().getDialogRequestIdHandler();
        CustomUserInteractionDeviceModule customUserInteractionDeviceModule =
                new CustomUserInteractionDeviceModule(messageSender, dialogRequestIdHandler);
        dcsSdk.putDeviceModule(customUserInteractionDeviceModule);

        IAppLauncher appLauncher = new IAppLauncher() {
            @Override
            public boolean launchAppByName(Context context, String appname) {
                return true;
            }

            @Override
            public boolean launchAppByPackageName(Context context, String packageName) {
                return false;
            }

            @Override
            public void launchAppByDeepLink(Context context, String deepLink) {

            }

            @Override
            public void updateAppList(Context context) {

            }

            @Override
            public boolean launchMarketWithAppName(Context context, String appname) {
                return false;
            }

            @Override
            public boolean launchMarketWithPackageName(Context context, String packageName) {
                return false;
            }

            @Override
            public List<AppInfo> getAppList() {
                return null;
            }
        };

        appDeviceModule = new AppDeviceModule(messageSender,appLauncher);
        appDeviceModule.addAppListener(appListener);
        dcsSdk.putDeviceModule(appDeviceModule);

        appLauncherDeviceModule = new AppLauncherDeviceModule(messageSender,appLauncher);
        appLauncherDeviceModule.addAppLauncherListener(appLauncherListener);
        dcsSdk.putDeviceModule(appLauncherDeviceModule);

        deviceControlDeviceModule = new DeviceControlDeviceModule(messageSender);
        deviceControlDeviceModule.addDeviceControlListener(deviceControlListener);
        dcsSdk.putDeviceModule(deviceControlDeviceModule);

        // 扩展自定义DeviceModule,eg...
        addOtherDeviceModule(dcsSdk, messageSender);
        // 获取设备列表
        // getInternalApi().getSmartHomeManager().getDeviceList(null, null);


        //上传手机通讯录
        ContactsChoiceUtil contactsChoiceUtil = new ContactsChoiceUtil();
        try {
            getInternalApi().getUpload().uploadPhoneContacts(contactsChoiceUtil.getAllContacts(this),false,null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class ContactsChoiceUtil {
        public String getAllContacts(Context context) throws JSONException {
            JSONArray array = new JSONArray();
            JSONObject object;
            ContentResolver resolver = context.getContentResolver();
            // 获取手机联系人
            Cursor phoneCursor = resolver.query(ContactsContract.Contacts.CONTENT_URI,
                    new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME},
                    null, null, null);

            if (phoneCursor != null) {
                int column = phoneCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                while (phoneCursor.moveToNext() && column > -1) {
                    String displayName = phoneCursor.getString(column);
                    if (!TextUtils.isEmpty(displayName)) {
                        object = new JSONObject();
                        object.put("name", displayName);
                        array.put(object);
                    }
                }
            }
            return array.toString();
        }
    }





    protected void addOtherDeviceModule(IDcsSdk dcsSdk, IMessageSender messageSender) {

    }

    protected SdkConfigProvider getSdkConfigProvider() {
        return new DefaultSdkConfigProvider() {
            @Override
            public String clientId() {
                return CLIENT_ID;
            }

            @Override
            public int pid() {
                return PID;
            }

            @Override
            public String appKey() {
                return APP_KEY;
            }
        };
    }

    private String mRenderPlayerInfoToken = null;
    private String mPlayToken = null;
    private ScreenExtendDeviceModule.IScreenExtensionListener mScreenExtensionListener = new ScreenExtendDeviceModule
            .IScreenExtensionListener() {


        @Override
        public void onRenderPlayerInfo(RenderPlayerInfoPayload renderPlayerInfoPayload) {
            // handleRenderPlayerInfoPayload(renderPlayerInfoPayload);
        }

        @Override
        public void onRenderAudioList(RenderAudioListPlayload renderAudioListPlayload) {

        }
    };

    protected void sdkRun() {
        // 第三步，将sdk跑起来
        ((DcsSdkImpl) dcsSdk).getInternalApi().login(new ILoginListener() {
            @Override
            public void onSucceed(String accessToken) {
                dcsSdk.run(null);
                Toast.makeText(BaseActivity.this.getApplicationContext(), "登录成功", Toast
                        .LENGTH_SHORT).show();

            }

            @Override
            public void onFailed(String errorMessage) {
                Toast.makeText(BaseActivity.this.getApplicationContext(), "登录失败", Toast
                        .LENGTH_SHORT).show();
                Log.e(TAG, "login onFailed. ");
                finish();
            }

            @Override
            public void onCancel() {
                Toast.makeText(BaseActivity.this.getApplicationContext(), "登录被取消", Toast
                        .LENGTH_SHORT).show();
                Log.e(TAG, "login onCancel. ");
                finish();
            }
        });
    }

    private void initViews() {
        textViewWakeUpTip = (TextView) findViewById(R.id.id_tv_wakeup_tip);
        textInput = (EditText) findViewById(R.id.textInput);
        sendButton = (Button) findViewById(R.id.sendBtn);
        sendButton.setOnClickListener(this);
        voiceButton = (Button) findViewById(R.id.voiceBtn);
        voiceButton.setOnClickListener(this);
        cancelVoiceButton = (Button) findViewById(R.id.cancelBtn);
        cancelVoiceButton.setOnClickListener(this);
        textViewRenderVoiceInputText = (TextView) findViewById(R.id.id_tv_RenderVoiceInputText);
        mTopLinearLayout = (LinearLayout) findViewById(R.id.topLinearLayout);
        dcsWebView = new DcsWebView(this.getApplicationContext());
        mTopLinearLayout.addView(dcsWebView);

        textViewWakeUpTip.setVisibility(enableWakeUp() ? View.VISIBLE : View.GONE);
        initDcsWebView();
    }

    private void initDcsWebView() {
        dcsWebView.setLoadListener(new DcsWebView.LoadListener() {
            @Override
            public void onPageStarted() {

            }

            @Override
            public void onPageFinished() {
                if (duerResultT > 0) {
                    // DCSStatisticsImpl.getInstance().reportView(duerResultT, System.currentTimeMillis());
                    Toast.makeText(BaseActivity.this, (System.currentTimeMillis() - duerResultT)
                            + " ms", Toast.LENGTH_LONG).show();
                    duerResultT = 0;
                }
            }
        });
    }

    public InternalApi getInternalApi() {
        return ((DcsSdkImpl) dcsSdk).getInternalApi();
    }

    private IWakeupAgent.IWakeupAgentListener wakeupAgentListener = new IWakeupAgent.SimpleWakeUpAgentListener() {
        @Override
        public void onWakeupSucceed(WakeUpWord wakeUpWord) {
            Toast.makeText(BaseActivity.this,
                    "唤醒成功",
                    Toast.LENGTH_LONG).show();
        }
    };

    private void initWakeUpAgentListener() {
        IWakeupAgent wakeupAgent = getInternalApi().getWakeupAgent();
        if (wakeupAgent != null) {
            wakeupAgent.addWakeupAgentListener(wakeupAgentListener);
        }
    }

    public void beginVoiceRequest(final boolean vad) {
        // 必须先调用cancel
        dcsSdk.getVoiceRequest().cancelVoiceRequest(new IVoiceRequestListener() {
            @Override
            public void onSucceed() {
                dcsSdk.getVoiceRequest().beginVoiceRequest(vad);
            }
        });
    }

    public void initDialogStateListener() {
        // 添加会话状态监听
        dialogStateListener = new IDialogStateListener() {
            @Override
            public void onDialogStateChanged(final DialogState dialogState) {
                currentDialogState = dialogState;
                Log.d(TAG, "onDialogStateChanged: " + dialogState);
                switch (dialogState) {
                    case IDLE:
                        voiceButton.setText(getResources().getString(R.string.stop_record));
                        break;
                    case LISTENING:
                        textViewRenderVoiceInputText.setText("");
                        voiceButton.setText(getResources().getString(R.string.start_record));
                        break;
                    case SPEAKING:
                        voiceButton.setText(getResources().getString(R.string.speaking));
                        break;
                    case THINKING:
                        voiceButton.setText(getResources().getString(R.string.think));
                        break;
                    default:
                        break;
                }
            }
        };
        dcsSdk.getVoiceRequest().addDialogStateListener(dialogStateListener);
    }

    private void addDirectiveReceivedListener() {
        getInternalApi().addDirectiveReceivedListener(new IDirectiveReceivedListener() {
            @Override
            public void onDirective(Directive directive) {
                if (directive == null) {
                    return;
                }
                if (directive.getName().equals("Play")) {
                    Payload mPayload = directive.getPayload();
                    if (mPayload instanceof com.baidu.duer.dcs.devicemodule.audioplayer.message.PlayPayload) {
                        com.baidu.duer.dcs.devicemodule.audioplayer.message.PlayPayload.Stream stream =
                                ((com.baidu.duer.dcs.devicemodule.audioplayer.message.PlayPayload) mPayload)
                                        .audioItem.stream;
                        if (stream != null) {
                            mPlayToken = ((com.baidu.duer.dcs.devicemodule.audioplayer.message.PlayPayload) mPayload)
                                    .audioItem.stream.token;
                            Log.i(TAG, "  directive mToken = " + mPlayToken);
                        }
                    }
                } else if (directive.getName().equals("RenderPlayerInfo")) {
                    Payload mPayload = directive.getPayload();
                    if (mPayload instanceof RenderPlayerInfoPayload) {
                        mRenderPlayerInfoToken = ((RenderPlayerInfoPayload) mPayload).getToken();
                    }
                }
            }
        });
    }

    private void initDirectiveIntercepter() {
        getInternalApi().setDirectiveIntercepter(new IDirectiveIntercepter() {
            @Override
            public boolean onInterceptDirective(Directive directive) {
                return false;
            }
        });
    }

    private void initFinishedDirectiveListener() {
        // 所有指令执行完毕的回调监听
        getInternalApi().addFinishedDirectiveListener(new IFinishedDirectiveListener() {
            @Override
            public void onFinishedDirective() {
                Log.d(TAG, "所有指令执行完毕");
            }
        });
    }







    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendBtn:
                String inputText = textInput.getText().toString().trim();
                if (TextUtils.isEmpty(inputText)) {
                    Toast.makeText(this, getResources().getString(R.string
                                    .inputed_text_cannot_be_empty),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // 清空并收起键盘
                textInput.getEditableText().clear();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context
                        .INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
                if (!NetWorkUtil.isNetworkConnected(this)) {
                    Toast.makeText(this,
                            getResources().getString(R.string.err_net_msg),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                getInternalApi().sendQuery(inputText);
                break;
            case R.id.voiceBtn:
                if (getAsrMode() == DcsConfig.ASR_MODE_ONLINE) {
                    if (!NetWorkUtil.isNetworkConnected(this)) {
                        Toast.makeText(this,
                                getResources().getString(R.string.err_net_msg),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                // 为了解决频繁的点击 而服务器没有时间返回结果造成的不能点击的bug
                if (currentDialogState == IDialogStateListener.DialogState.LISTENING) {
                    dcsSdk.getVoiceRequest().endVoiceRequest(new IVoiceRequestListener() {
                        @Override
                        public void onSucceed() {

                        }
                    });
                } else {
                    beginVoiceRequest(getAsrType() == AsrType.AUTO);
                }
                break;
            case R.id.cancelBtn:
                // 取消识别，不再返回任何识别结果
                cancelVoiceRequest();
                break;
            default:
                break;
        }
    }

    public void cancelVoiceRequest() {
        dcsSdk.getVoiceRequest().cancelVoiceRequest(new IVoiceRequestListener() {
            @Override
            public void onSucceed() {
                Log.d(TAG, "cancelVoiceRequest onSucceed");
            }
        });
    }


    private int calculateVolume(byte[] buffer) {
        short[] audioData = new short[buffer.length / 2];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioData);
        double sum = 0;
        // 将 buffer 内容取出，进行平方和运算
        for (int i = 0; i < audioData.length; i++) {
            sum += audioData[i] * audioData[i];
        }
        // 平方和除以数据总长度，得到音量大小
        double mean = sum / (double) audioData.length;
        final double volume = 10 * Math.log10(mean);
        return (int) volume;
    }

    private void wakeUp() {
        getInternalApi().startWakeup();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // 这里是为了展示如何使用下面的2个方法，如果不需要可以不用调用
        // 停止tts，音乐等有关播放.
        getInternalApi().pauseSpeaker();
        // 如果有唤醒，则停止唤醒
        getInternalApi().startWakeup();
        // 取消识别，不返回结果
        cancelVoiceRequest();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 这里是为了展示如何使用下面的2个方法，如果不需要可以不用调用
        Log.d(TAG, "onRestart");
        // 恢复tts，音乐等有关播放
        getInternalApi().resumeSpeaker();
        // 如果有唤醒，则恢复唤醒
        wakeUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        // dcsWebView
        dcsWebView.setLoadListener(null);
        mTopLinearLayout.removeView(dcsWebView);
        dcsWebView.removeAllViews();
        dcsWebView.destroy();

        if (screenDeviceModule != null) {
            screenDeviceModule.removeScreenListener(screenListener);
        }
        screenListener = null;

        dcsSdk.getVoiceRequest().removeDialogStateListener(dialogStateListener);
        dialogStateListener = null;

        dcsSdk.removeConnectionStatusListener(connectionStatusListener);
        connectionStatusListener = null;

        getInternalApi().removeErrorListener(errorListener);
        errorListener = null;

        getInternalApi().removeRequestBodySentListener(dcsRequestBodySentListener);
        dcsRequestBodySentListener = null;

        getInternalApi().setLocationHandler(null);
//        locationHandler = null;
//        if (location != null) {
//            location.release();
//        }

        // 第3步，释放sdk
        dcsSdk.release();
    }

    protected IOauth getOauth() {
        return new OauthCodeImpl(CLIENT_ID, this);
    }

    /**
     * 是否启用唤醒
     *
     * @return
     */
    public abstract boolean enableWakeUp();

    /**
     * asr的识别类型-在线or离线
     *
     * @return
     */
    public abstract int getAsrMode();

    /**
     * 识别模式
     *
     * @return
     */
    public abstract AsrType getAsrType();

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings1:
                Intent intent = new Intent(BaseActivity.this, settingActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
