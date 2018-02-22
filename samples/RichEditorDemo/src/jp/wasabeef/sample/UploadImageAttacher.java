package jp.wasabeef.sample;

import com.sina.sinagame.windowattacher.WindowAttacher;

import android.content.Intent;

public interface UploadImageAttacher extends WindowAttacher,
		OnBitmapCapturedListener {
	void onActivityResult(int requestCode, int resultCode, Intent data);
	void setOnBitmapCapturedListener(OnBitmapCapturedListener l);
}
