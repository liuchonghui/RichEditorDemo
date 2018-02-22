package jp.wasabeef.sample;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.sina.sinagame.windowattacher.CenterDialogAttacher;

public class ShowImgEditDialog extends CenterDialogAttacher implements
		View.OnClickListener {

	public ShowImgEditDialog(Activity attachedActivity) {
		this(attachedActivity, R.layout.show_img_edit_popupattacher);
	}

	protected ShowImgEditDialog(Activity attachedActivity, int layoutResId) {
		super(attachedActivity, layoutResId);
	}

	Button cancle;
	Button comfirm;

	@Override
	public void findViewByContentView(View contentView) {
		cancle = (Button) contentView.findViewById(R.id.pop_button_cancel);
		cancle.setOnClickListener(this);
		comfirm = (Button) contentView.findViewById(R.id.pop_button_confirm);
		comfirm.setOnClickListener(this);
	}

	@Override
	public void adjustContentView(View contentView) {
		super.adjustContentView(contentView);
	}

	@Override
	public void onClick(View view) {
		final int id = view.getId();
		if (R.id.pop_button_cancel == id) {
			closePop();
		} else if (R.id.pop_button_confirm == id) {
			if (listener != null) {
				listener.onConfirm();
			}
			closePop();
		}
	}

	OnConfirmListener listener;

	public interface OnConfirmListener {
		void onConfirm();
	}

	public void setOnConfirmListener(OnConfirmListener l) {
		this.listener = l;
	}

}
