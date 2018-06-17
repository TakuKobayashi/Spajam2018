package kobayashi.taku.taptappun.net.spajam2018;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class AudioService extends Service {
    private Timer mTimer = null;
    private AudioManager mAudioManager = null;
    private HeadsetStateReceiver mHeadsetStateReceiver;

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
        // タイマーの設定 1秒毎にループ
        mTimer = new Timer(true);
        mTimer.schedule( new TimerTask(){
            @Override
            public void run(){
                Log.d(Config.TAG, "timer:" + mAudioManager.isMusicActive());
            }
        }, 1000, 1000);
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
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.d(Config.TAG, "onBind");
        return null;
    }
}
