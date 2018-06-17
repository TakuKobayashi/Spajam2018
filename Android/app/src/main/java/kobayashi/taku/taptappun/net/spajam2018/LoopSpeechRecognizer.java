package kobayashi.taku.taptappun.net.spajam2018;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

public class LoopSpeechRecognizer implements RecognitionListener{
    private Context mContext;
	private SpeechRecognizer mSpeechRecognizer;
	private RecognizeCallback mCallback;

	public LoopSpeechRecognizer(Context context){
		mContext = context;
	}

	public void startListening() {
		try {
			if (mSpeechRecognizer == null) {
				mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
				mSpeechRecognizer.setRecognitionListener(this);
			}
			// インテントの作成
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			// 言語モデル指定
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
			mSpeechRecognizer.startListening(intent);
		}catch (Exception ex) {
			Log.d(Config.TAG, "errSt:" + ex.getStackTrace());
			ex.printStackTrace();
		}
	}

	// 音声認識を終了する
	public void stopListening() {
		if (mSpeechRecognizer != null){
			mSpeechRecognizer.stopListening();
			mSpeechRecognizer.cancel();
			mSpeechRecognizer.destroy();
		}
		mSpeechRecognizer = null;
	}

	// 音声認識を再開する
	public void restartListeningService() {
		stopListening();
		startListening();
	}

	@Override
	public void onReadyForSpeech(Bundle bundle) {

	}

	@Override
	public void onBeginningOfSpeech() {

	}

	@Override
	public void onRmsChanged(float v) {

	}

	@Override
	public void onBufferReceived(byte[] bytes) {

	}

	@Override
	public void onEndOfSpeech() {

	}

	@Override
	public void onError(int error) {
		String reason = "";
		switch (error) {
			// Audio recording error
			case SpeechRecognizer.ERROR_AUDIO:
				reason = "ERROR_AUDIO";
				break;
			// Other client side errors
			case SpeechRecognizer.ERROR_CLIENT:
				reason = "ERROR_CLIENT";
				break;
			// Insufficient permissions
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
				reason = "ERROR_INSUFFICIENT_PERMISSIONS";
				break;
			// 	Other network related errors
			case SpeechRecognizer.ERROR_NETWORK:
				reason = "ERROR_NETWORK";
                    /* ネットワーク接続をチェックする処理をここに入れる */
				break;
			// Network operation timed out
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
				reason = "ERROR_NETWORK_TIMEOUT";
				break;
			// No recognition result matched
			case SpeechRecognizer.ERROR_NO_MATCH:
				reason = "ERROR_NO_MATCH";
				break;
			// RecognitionService busy
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
				reason = "ERROR_RECOGNIZER_BUSY";
				break;
			// Server sends error status
			case SpeechRecognizer.ERROR_SERVER:
				reason = "ERROR_SERVER";
                    /* ネットワーク接続をチェックをする処理をここに入れる */
				break;
			// No speech input
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
				reason = "ERROR_SPEECH_TIMEOUT";
				break;
		}
		Log.d(Config.TAG, reason);
		restartListeningService();
	}

	@Override
	public void onResults(Bundle bundle) {
		Log.d(Config.TAG, "success");
		// 結果をArrayListとして取得
		ArrayList<String> results_array = bundle.getStringArrayList(
				SpeechRecognizer.RESULTS_RECOGNITION);

		float maxScore = 0f;
		int index = -1;
		float[] scores = bundle.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

		for(int i = 0;i < scores.length;++i){
			Log.d(Config.TAG, "score:" + scores[i]);
			if(scores[i] > maxScore){
				maxScore = scores[i];
				index = i;
			}
		}
		Log.d(Config.TAG, "index:" + index);
		if(index >= 0){
			if(mCallback != null){
				mCallback.onSuccess(scores[index], results_array.get(index));
			}
		}
		restartListeningService();
	}

	public void setCallback(RecognizeCallback callback){
		mCallback = callback;
	}

	public interface RecognizeCallback{
		public void onSuccess(float confidence, String value);
	}

	@Override
	public void onPartialResults(Bundle bundle) {

	}

	@Override
	public void onEvent(int i, Bundle bundle) {

	}
}