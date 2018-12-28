package com.seuic.zh.mydueros;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyService extends NotificationListenerService {

    private String packageName = null;
    private String title = null;
    private String content = null;
    private int number = 0;


    //来通知时的调用
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Notification notification = sbn.getNotification();
        if (notification == null) {
            return;
        }
        MyList myNotification = new MyList();
        Bundle extras = notification.extras;

        if (notification.actions != null) {
            for (Notification.Action action : notification.actions) {
//                LogUtils.e("action:" + action.title.toString());
                if ("接听".equals(action.title.toString())) {
                    AppApplication.acceptIntent = action.actionIntent;

                } else if ("挂断".equals(action.title.toString()) || "拒接".equals(action.title.toString())) {
                    AppApplication.killIntent = action.actionIntent;
                }
            }
        }

        if (extras != null) {

//            获取包名
            packageName = sbn.getPackageName();
//             获取通知标题，来短信或者来电时，为联系人或者号码
            title = extras.get("android.title").toString();

//             获取通知内容
            content =extras.get("android.text").toString() ;


            /*
            这一条主要是针对短信的，只有来短信了，那么tickerText才不是空，
            并且里面为来电号码+内容或者联系人加内容
             */
            CharSequence tickerText = sbn.getNotification().tickerText;

            if (packageName.equals("com.android.dialer")&& regExTest(content,"来电")){

                myNotification.setType("来电");
                number++;
//              这个number主要是用来记录来电时，系统连发两次广播，我只要取第二次。
                if (number == 2) {
                    myNotification.setContact(title);
                    myNotification.setContent(content);
                    number=0;
                    sendBroadcast(myNotification);

                }
            }

            if (packageName.equals("com.android.dialer")&& regExTest(content,"正在拨号")){
                number++;
                if (number ==2) {
                    myNotification.setType("正在拨号");
                    myNotification.setContact(title);
                    myNotification.setContent(content);
                    sendBroadcast(myNotification);
                }
            }


            if (packageName.equals("com.android.mms")&& !(tickerText == null)){


                myNotification.setType("接收短信");
                myNotification.setContact(title);

                /*
                这边这要是从tickerText中的短信内容提取出来
                之所以不用上面的content是因为当用户选择短信合并模式的时候
                content只是显示多少条短信，不会显示最新一条短信内容
                 */
                StringBuffer stringBuffer = new StringBuffer(tickerText.toString());
                String messageContent = stringBuffer.delete(0,title.length()).toString();
                myNotification.setContent(messageContent);

                sendBroadcast(myNotification);
            }
        }
    }

    private void sendBroadcast (MyList myNotification) {
        Intent intent = new Intent();
        intent.putExtra("myNotification", (Serializable) myNotification);
        intent.setAction("com.seuic.zh.mydueros.MyService");
        sendBroadcast(intent);
    }


    private boolean regExTest(String text, String keyWord) {
        Pattern p;
        Matcher m;
        //用于过滤掉相关字符或词汇(用括号括起来的)的正则表达式，
        String regEx = "[`~!@#$%^&*()+=|{}:;\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？a-zA-Z 0-9,嗯恩哦啊请吧(一下)(好吗)(可以吗)]";
        p = Pattern.compile(regEx);
        m = p.matcher(text);
        text = m.replaceAll("").trim();
        String regEx1 = new StringBuilder().append(".{0,10}(").append(keyWord).append(").{0,10}").toString();//eg:  ".{0,10}(景色|漂亮).{0,10}"（todo 适用于长度不超多20的文本）
        p = Pattern.compile(regEx1);
        m = p.matcher(text);
        if (m.matches()) {
            return true;
        } else {
            return false;
        }
    }


    //删除通知时的调用
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Notification notification = sbn.getNotification();
        if (notification == null) {
            return;
        }
        Bundle extras = notification.extras;
        String content = "";
        if (extras != null) {
            // 获取通知标题
            String title = extras.getString(Notification.EXTRA_TITLE, "");
            // 获取通知内容
            content = extras.getString(Notification.EXTRA_TEXT, "");
        }

    }
}