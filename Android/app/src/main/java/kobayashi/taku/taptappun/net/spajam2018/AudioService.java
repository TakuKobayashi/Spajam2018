package kobayashi.taku.taptappun.net.spajam2018;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback;

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
    private LoopSpeechRecognizer mLoopSpeechRecognizer;
    private boolean isSaid = false;
    private int sayCounter = 0;
    private int viveCounter = 0;

    private SensorManager mSensorManager;
    private float fAccell[] = new float[3];
    private float mDiffAccell[] = new float[3];

    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        // 値変更時
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            // センサーの種類で値を取得
            switch( sensorEvent.sensor.getType()) {
                // 加速度
                case Sensor.TYPE_ACCELEROMETER:
                    float[] accell = sensorEvent.values.clone();
                    for(int i = 0;i < fAccell.length;++i){
                        mDiffAccell[i] = accell[i] - fAccell[i];
                    }
                    fAccell = accell;
                    if(Math.abs(mDiffAccell[0]) + Math.abs(mDiffAccell[1]) + Math.abs(mDiffAccell[2]) > 1.5f){
                        viveCounter++;
                    }
                    if(viveCounter > 5){
                        isSaid = true;
                        sayCounter = 0;
                        viveCounter = 0;
                    }
/*
                    String str = "";
                    str += "加速度センサー\n"
                            + "X:" + Math.abs(mDiffAccell[0]) + "\n"
                            + "Y:" + Math.abs(mDiffAccell[1]) + "\n"
                            + "Z:" + Math.abs(mDiffAccell[2]) + "\n";
                    Log.d(Config.TAG, str);
*/
                    break;
            }
        }

        //
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    private SharedPreferences.OnSharedPreferenceChangeListener mSharedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals("HeadsetStatus")){
                registerAction();
            }
        }
    };

    private void initParams(){
        mPrevVolumn = -1;
        isSaid = false;
        sayCounter = 0;
        viveCounter = 0;
    }

    @Override
    public void onCreate() {
        Log.d(Config.TAG, "onCreate");
        initParams();
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mHeadsetStateReceiver = new HeadsetStateReceiver();
        registerReceiver(mHeadsetStateReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        registerReceiver(mHeadsetStateReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        SharedPreferences sp = Preferences.getCommonPreferences(AudioService.this);
        sp.registerOnSharedPreferenceChangeListener(mSharedListener);
    }

    private void registerAction(){
        initParams();
        SharedPreferences sp = Preferences.getCommonPreferences(AudioService.this);
        if(sp.getInt("HeadsetStatus", -1) > 0){
            mSensorManager.unregisterListener(mSensorEventListener);
            mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
            mLoopSpeechRecognizer.stopListening();
        }else{
            mSensorManager.unregisterListener(mSensorEventListener);
            if(mLoopSpeechRecognizer != null){
                mLoopSpeechRecognizer.stopListening();
            }
            mLoopSpeechRecognizer = new LoopSpeechRecognizer(this);
            mLoopSpeechRecognizer.setCallback(new LoopSpeechRecognizer.RecognizeCallback() {
                @Override
                public void onSuccess(float confidence, String value) {
                    isSaid = true;
                    sayCounter = 0;
                    Log.d(Config.TAG, "success:" + value);
                }
            });
            mLoopSpeechRecognizer.startListening();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Config.TAG, "onStartCommand");
        registerAction();
        downloadSound("http://35.194.5.215/Youtube/?v=SO48dmRMINE");
        // タイマーの設定 1秒毎にループ
        mTimer = new Timer(true);
        mTimer.schedule( new TimerTask(){
            @Override
            public void run(){
                SharedPreferences sp = Preferences.getCommonPreferences(AudioService.this);
                //&& sp.getInt("HeadsetStatus", -1) > 0;
                if(mAudioManager.isMusicActive() && isSaid){
                    sayCounter++;
                    viveCounter = 0;
                    if(sayCounter > 5){
                        isSaid = false;
                        sayCounter = 0;
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mPrevVolumn, 0);
                        mPrevVolumn = -1;
                    }else{
                        String message = "話しかけたい人がいます";
                        try {
                            mPrevVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                            String sayUrl = "http://35.194.5.215/Aitalk/?text=" + URLEncoder.encode(message, "UTF-8");
                            downloadSound(sayUrl);
                            mUrlMp.get(sayUrl).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 2, 0);
                                }
                            });
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, 1000, 1000);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(Config.TAG, "onDestroy");
        SharedPreferences sp = Preferences.getCommonPreferences(AudioService.this);
        sp.unregisterOnSharedPreferenceChangeListener(mSharedListener);
        unregisterReceiver(mHeadsetStateReceiver);
        mSensorManager.unregisterListener(mSensorEventListener);
        mLoopSpeechRecognizer.stopListening();

        // タイマー停止
        if( mTimer != null ){
            mTimer.cancel();
            mTimer = null;
        }

        if(mPrevVolumn > 0){
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mPrevVolumn, 0);
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
            MediaPlayer mp = playSound(soundFile);
            mUrlMp.put(url, mp);
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
                    MediaPlayer mp = playSound(downloadedFile.getPath());
                    mUrlMp.put(url, mp);
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
