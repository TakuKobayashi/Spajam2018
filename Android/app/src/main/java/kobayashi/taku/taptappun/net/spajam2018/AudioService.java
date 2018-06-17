package kobayashi.taku.taptappun.net.spajam2018;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

public class AudioService extends Service {
    private Timer mTimer = null;
    private AudioManager mAudioManager = null;
    private HeadsetStateReceiver mHeadsetStateReceiver;
    private HashMap<String, MediaPlayer> mUrlMp = new HashMap<String, MediaPlayer>();
    private int audioVolumnState = 0;
    private int mPrevVolumn = -1;

    @Override
    public void onCreate() {
        Log.d(Config.TAG, "onCreate");
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mHeadsetStateReceiver = new HeadsetStateReceiver();
        registerReceiver(mHeadsetStateReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        registerReceiver(mHeadsetStateReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Config.TAG, "onStartCommand");
        downloadSound("http://35.194.5.215/Youtube/?v=cz-qlUSPhfg");
        // タイマーの設定 1秒毎にループ
        mTimer = new Timer(true);
        mTimer.schedule( new TimerTask(){
            @Override
            public void run(){
                SharedPreferences sp = Preferences.getCommonPreferences(AudioService.this);
                if(mAudioManager.isMusicActive() && sp.getInt("HeadsetStatus", -1) > 0){
                    if(audioVolumnState == 0){
                        mPrevVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        Log.d(Config.TAG, "volumn:" + mPrevVolumn);
                        Log.d(Config.TAG, "volumn2:" + mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                        audioVolumnState = 1;
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                        String message = "話しかけたい人がいます";
                        try {
                            downloadSound("http://35.194.5.215/Aitalk/?text=" + URLEncoder.encode(message, "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }else{
                        audioVolumnState = 0;
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mPrevVolumn, 0);
                    }
                }
            }
        }, 5000, 5000);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(Config.TAG, "onDestroy");
        unregisterReceiver(mHeadsetStateReceiver);

        // タイマー停止
        if( mTimer != null ){
            mTimer.cancel();
            mTimer = null;
        }
        for (String key : mUrlMp.keySet()) {
            MediaPlayer mp = mUrlMp.get(key);
            if(mp.isPlaying()){
                mp.stop();
            }
            mp.release();
        }
        mUrlMp.clear();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.d(Config.TAG, "onBind");
        return null;
    }

    private void downloadSound(String url){
        SharedPreferences sp = Preferences.getCommonPreferences(this);
        String soundFile = sp.getString(url, null);
        if(soundFile != null){
            playSound(soundFile);
            return;
        }

        HttpRequestTask task = new HttpRequestTask();
        task.addCallback(new HttpRequestTask.ResponseCallback() {
            @Override
            public void onSuccess(String url, ResponseBody response) {
                File downloadedFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + UUID.randomUUID().toString() + ".wav");
                try {
                    BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
                    sink.writeAll(response.source());
                    sink.close();
                    Preferences.saveCommonParam(AudioService.this, url, downloadedFile.getPath());
                    playSound(downloadedFile.getPath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute(url);
    }

    private MediaPlayer playSound(String dataPath){
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(dataPath);
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mp;
    }
}
