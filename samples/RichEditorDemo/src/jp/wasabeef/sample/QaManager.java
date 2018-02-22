package jp.wasabeef.sample;

import java.io.Serializable;

import android.app.Activity;

import com.android.overlay.RunningEnvironment;
import com.android.overlay.utils.LogUtils;

/**
 * Manage Question & Answer.
 * 
 * @author liu_chonghui
 * 
 */
@SuppressWarnings("serial")
public class QaManager implements Serializable {

	protected static QaManager instance;

	static {
		instance = new QaManager();
		RunningEnvironment.getInstance().addManager(instance);
	}

	public static QaManager getInstance() {
		return instance;
	}

	public QaManager() {
	}

	public UploadImageAttacher getUploadImageAttacher(Activity activity,
			OnBitmapCapturedListener l) {
		UploadImageAttacher attacher;
		int maxMemory = (int) (Runtime.getRuntime().maxMemory());
		int freeMemory = (int) (Runtime.getRuntime().freeMemory());
		int totalMemory = (int) (Runtime.getRuntime().totalMemory());
		LogUtils.d("QA", "maxMemory=" + maxMemory + ", freeMeory=" + freeMemory
				+ ", totalMeory=" + +totalMemory);
		if (totalMemory <= 10 * 1024 * 1024) {
			attacher = new ImageSelectWindowAttacher(activity);
		} else if (maxMemory >= 97 * 1024 * 1024) {
			attacher = new ImageChooseWindowAttacher(activity);
		} else if (maxMemory >= 64 * 1024 * 1024 && maxMemory / 2 < totalMemory) {
			attacher = new ImageChooseWindowAttacher(activity);
		} else {
			attacher = new ImageSelectWindowAttacher(activity);
		}
		attacher.setOnBitmapCapturedListener(l);
		return attacher;
	}

}
