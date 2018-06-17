package kobayashi.taku.taptappun.net.spajam2018;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class HeadsetStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action == null) {
            return;
        }

        switch (action) {
            case Intent.ACTION_HEADSET_PLUG:
                Log.d(Config.TAG, "Intent.ACTION_HEADSET_PLUG");
                int state = intent.getIntExtra("state", -1);
                Preferences.saveCommonParam(context, "HeadsetStatus", state);
                if (state == 0) {
                    // ヘッドセットが装着されていない・外された
                } else if (state > 0) {
                    // イヤホン・ヘッドセット(マイク付き)が装着された
                }
                break;
            case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                Log.e(Config.TAG, "AudioManager.ACTION_AUDIO_BECOMING_NOISY");
                // 音声経路の変更！大きな音が鳴りますよ！！
                break;
            default:
                break;
        }
    }
}
