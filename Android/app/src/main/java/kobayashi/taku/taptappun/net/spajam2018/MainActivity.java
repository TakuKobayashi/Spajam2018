package kobayashi.taku.taptappun.net.spajam2018;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.taptappun.taku.kobayashi.runtimepermissionchecker.RuntimePermissionChecker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;


// IBM Watson SDK
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.dto.SpeechConfiguration;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.ISpeechDelegate;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.SpeechToText;


public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Config.TAG, "start");
                startService( new Intent(getApplicationContext(), AudioService.class ) );
            }
        });

        Button stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Config.TAG, "stop");
                stopService( new Intent( getApplicationContext(), AudioService.class ) );
            }
        });

        RuntimePermissionChecker.requestAllPermissions(this, REQUEST_CODE);
        //downloadSound();
    }


    private void downloadSound(){
        HttpRequestTask task = new HttpRequestTask();
        task.addCallback(new HttpRequestTask.ResponseCallback() {
            @Override
            public void onSuccess(String url, ResponseBody response) {
                File downloadedFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + UUID.randomUUID().toString() + ".wav");
                try {
                    BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
                    sink.writeAll(response.source());
                    sink.close();
                    MediaPlayer mp = new MediaPlayer();
                    mp.setDataSource(downloadedFile.getPath());
                    mp.prepare();
                    mp.start();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        task.execute("https://s3-ap-northeast-1.amazonaws.com/taptappun/test/popteamepic.wav");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
