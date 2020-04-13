package com.example.servise;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

/**
 * 背景處理,使用後耗時動作時,網際網路
 * 擁有多個條件下需要使用時,比如藍牙設置,必須擁有多個任務在不同的生命週期下操作
 * servise：擁有生命週期
 */

public class MainActivity extends AppCompatActivity {
    private SeekBar seekBar;//進度條
    private MyReceiver myReceiver;//廣播器
    private int newlen;
    private boolean isReadSD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    123);

        }else {
            init();
        }
    }
    private void init(){
        isReadSD =true;
        seekBar = findViewById(R.id.seekBar);
        //監聽SeekBar點擊的監聽事件
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                fromUser = 由使用這點擊時,會是true
                if (fromUser){
                    seekTo(progress);//點擊進度條到哪裡時,會轉成int progress
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        init2();//第一次安裝會執行它
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    private void seekTo(int progress){

        //把值Intent到後台,讓後台接收值
        Intent intent =new Intent(this,MyService.class);
        intent.putExtra("ACTION","seekto");
        intent.putExtra("where",progress);
        startService(intent);
    }
    //程式一直行,用廣播器拿取音樂長度
    @Override
    protected void onStart() {
        super.onStart();
        //第一次安裝執行時,在還沒拿到權限前就會執行完onStart了
        Log.v("brad","onStart");

        init2();//執行過一次有權限的會執行它

    }

    private void init2(){
        //因為安裝後第一次此程式再拿取權限前,會去執行這段,導致沒音樂播放或跳出ＥＲＲＯＲ,廣播都走完了
        //所以加入機制,第一次程式執行問權限會去init()執行init2的監聽
        //如果程式進來是有權限的狀況,才會去執行onStar()的init2
        if (isReadSD) {
            myReceiver = new MyReceiver();
            //建立過濾器
            IntentFilter filter = new IntentFilter("fromService");
            //廣播器只接收fromService字串的物件
            registerReceiver(myReceiver, filter);


            Intent intent = new Intent(this, MyService.class);
            intent.putExtra("ACTION", "restart");
            startService(intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //取消註冊廣播
        unregisterReceiver(myReceiver);
    }

    //啟動Service
    public void test1(View view) {
        Intent intent = new Intent(this,MyService.class);
        //使用intent啟動Service

        intent.putExtra("ACTION","start");//intent值過去讓對方判斷是start還是pause

        //注意：在這裡啟動啟動Service是使用startService而不是使用startActivity
        startService(intent);
    }
    //關閉Service
    public void test2(View view) {
        Intent intent = new Intent(this,MyService.class);
        stopService(intent);
    }
    //暫停音樂
    public void test3(View view) {
        Intent intent =new Intent(this,MyService.class);
        intent.putExtra("ACTION","pause");//intent值過去讓對方判斷是start還是pause
        startService(intent);
    }

    //建立廣播器
    private class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String aaa = intent.getAction();
            Log.v("brad",aaa);

            //拿取音樂長度
            //第二個參數是使用該標籤而取不到資料時的預設值
           int len = intent.getIntExtra("len",-1);

//           if(len>0){
//               newlen =len;
//           }
//            Log.v("brad","now:"+len);
           //等真的有抓到音樂長度,才去把值加入seekBar
           if (len >0)  seekBar.setMax(len);//給予seekBar目前這首歌的最大長度

            int now = intent.getIntExtra("now",-1);
            if (now >0) seekBar.setProgress(now);

        }
    }
}
