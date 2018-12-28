package com.seuic.zh.mydueros;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.Serializable;


public class MidActivity extends AppCompatActivity {
    private MyList myList;
//    private String data;
    private int time = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_time);
    }

    @Override
    protected void onStart() {
        super.onStart();
        myList = (MyList) getIntent().getSerializableExtra("extra_data");
        handler.sendEmptyMessageDelayed(0,500);
        MyCount myCount = new MyCount(500,500);
        // 开始倒计时：
        myCount.start();
    }


    public class MyCount extends CountDownTimer {


        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
        }
        @Override
        public void onTick(long millisUntilFinished) {
        }

    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (myList.getType().equals("offline_to_online")){
                        Intent intent = new Intent(MidActivity.this, MainActivity.class);
                        intent.putExtra("extra_data","已经为您切换到在线模式");
                        startActivity(intent);
                        finish();
                    }else {

                    Intent intent = new Intent(MidActivity.this, Main2Activity.class);
                    intent.putExtra("extra_data",(Serializable) myList);
                    startActivity(intent);
                    finish();
                }
                    break;

                default:
                    break;
            }
        }
    };



}