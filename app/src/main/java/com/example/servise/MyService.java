package com.example.servise;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 在java文件夾按右鍵->new->Service->選擇Service(不要有東西的)
 */
public class MyService extends Service {

    /**
     * 兩種模式：這裡使用起啟動型
     * 1.Start Service <--啟動型Service
     * 2.Bind Service 綁定行Service
     */

    private MediaPlayer mediaPlayer;//創建媒體播放器
    private int musiclen;//取得音樂長度
    private Timer timer;
    private int now;
    private File sdroot;

    //抓取sd卡內的音樂欓
    public MyService(){
        sdroot = Environment.getExternalStorageDirectory();
    }
    //綁定,Service出錯時返回到這
    @Override
    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
    //執行時先觸發的,啟動時只會執行一次,類似建構式啟動
    //當使用onDestroy關閉時,onCreate才能再次啟動
    //單純按下返回鍵,並不會重啟Service
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("brad","onCreate");

        timer = new Timer();
        timer.schedule(new MyTask(),0,100);//執行timer

//        mediaPlayer = MediaPlayer.create(this,R.raw.ed4);
        //在res右鍵->Recourse Directry ->新增raw資料夾：背景音樂檔會放這
        //讓播放器mediaPlayer擁有這首歌MediaPlayer.create

        //此招為抓取內部sd卡方法
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(sdroot.getAbsolutePath()+"/Download/ヒカルの碁-棋靈王-05.MUSIC IS MY THING-dream(ED5).mp3");
            mediaPlayer.prepare();//讓她準備
        } catch (IOException e) {
            Log.v("brad",e.toString());
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //設定串流類型,有響鈴拉鬧鐘等..類型



//        這裡可以不用寫,因為返回鍵而導致無法取得音樂長度,直接在onStartCommand抓取音樂長度
        musiclen = mediaPlayer.getDuration();//取得音樂長度
        Intent intent = new Intent("fromService");//定義發送出去的名稱
        intent.putExtra("len",musiclen);//將音樂長度加入intent
        sendBroadcast(intent);//發送給主頁廣播器

    }

    //之後啟動的都是這個
    //需要參數傳遞,在這裡撰寫, onStartCommand參數有：Intent intent
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("brad","onStartCommand");

        //拿取intent的值
        String act =  intent.getStringExtra("ACTION");

        //點擊播放做的事
        if (act.equals("start")){
            mediaPlayer.start();//開始播歌
         //點擊暫停做的事
        }else if (act.equals("pause") && mediaPlayer !=null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
         //拉動SeekBar時做的事
        }else if (act.equals("seekto")&& mediaPlayer !=null){
            int where = intent.getIntExtra("where",-1);
            if (where >=0){
                mediaPlayer.seekTo(where);
            }
            //因為返回鍵後,不會重跑onCreate,所以在這又重新抓取音樂總長度一次
            //如果按下返回鍵,重新抓取音樂總長度,intent過去
        }else if (act.equals("restart")&& mediaPlayer !=null){
            musiclen = mediaPlayer.getDuration();//取得音樂長度
            Intent intent2 = new Intent("fromService");//定義發送出去的名稱
            intent2.putExtra("len",musiclen);//將音樂長度加入intent
            sendBroadcast(intent2);//發送給主頁廣播器

        }
        return super.onStartCommand(intent, flags, startId);
    }


    private class MyTask extends TimerTask{
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()){

                now = mediaPlayer.getCurrentPosition();//取得現在的音樂長度

                Intent intent = new Intent("fromService");//定義發送出去的名稱
                intent.putExtra("now",now);//將音樂長度加入intent
                sendBroadcast(intent);//發送給主頁廣播器
            }
        }
    }


    //摧毀掉時會在這裡
    //各家手機有點不一樣,並不一定每支手機會在app完全關掉時會啟動onDestroy方法
    @Override
    public void onDestroy() {
        Log.v("brad","onDestroy");

        //關閉播放中狀態
        if (timer != null){
            timer.cancel();//取消全部任務
            timer.purge();//拿掉全部任務
            timer = null;//因為會吃記憶體,把記憶體釋放
        }

        //判斷音樂有在播放,才能關閉音樂
        if (mediaPlayer!=null){
            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();//關閉音樂
            }
            mediaPlayer.release();//釋放掉記憶體,因為這東西吃記憶體
        }

        super.onDestroy();
    }
}
