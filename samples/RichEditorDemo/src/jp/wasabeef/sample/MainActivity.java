package jp.wasabeef.sample;

import jp.wasabeef.richeditor.CompactTextChangeWatcher;
import jp.wasabeef.richeditor.RichEditor;
import jp.wasabeef.richeditor.RichEditor.OnImgTapListener;
import jp.wasabeef.sample.ShowImgEditDialog.OnConfirmListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.android.overlay.ApplicationUncaughtHandler;
import com.android.overlay.utils.LogUtils;

public class MainActivity extends Activity {

	private RichEditor mEditor;
	private CompactTextChangeWatcher mCompactTextChangeWatcher;
	private TextView mPreview;
	private TextView mCPreview;
	private RichEditor cEditor;
	private TextView mCVreview;

	EditText edit_text;
	TextView text_view;

//	ImageSelectWindowAttacher mImageAttacher;
//	ImageChooseWindowAttacher mImageAttacher;
	UploadImageAttacher mImageAttacher;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mImageAttacher != null) {
			mImageAttacher.onActivityResult(requestCode, resultCode, data);
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new ApplicationUncaughtHandler(
				getActivity()));
		setContentView(R.layout.activity_main);
		
		mImageAttacher = QaManager.getInstance().getUploadImageAttacher(
				getActivity(), new OnBitmapCapturedListener() {
					@Override
					public void onBitmapCaptured(String filepath, int width,
							int height) {
//						LogUtils.d("RED", "filepath:" + filepath);
						if (mEditor != null) {
							LogUtils.d("MA", "<1>filepath[" + filepath + "]");
							mEditor.insertImageWithNewLine(
									"file://" + filepath, width, height,
									"IMG_0");
						}
					}
				});
		
//		int maxMemory = (int) (Runtime.getRuntime().maxMemory());
//		int freeMeory = (int) (Runtime.getRuntime().freeMemory());
//		int totalMeory = (int) (Runtime.getRuntime().totalMemory());
//		LogUtils.d("UIA", "maxMemory=" + maxMemory + ", freeMeory=" + freeMeory
//				+ ", totalMeory=" + +totalMeory);
//		if (maxMemory > 64 * 1024 * 1024) {
//			mImageAttacher = new ImageChooseWindowAttacher(this) {
//				@Override
//				public void onBitmapCaptured(String filepath, int width, int height) {
////					LogUtils.d("RED", "filepath:" + filepath);
//					if (mEditor != null) {
//						mEditor.insertImageWithNewLine("file://" + filepath, width, height, "IMG_0");
//					}
//				}
//			};
//		} else {
//			mImageAttacher = new ImageSelectWindowAttacher(this) {
//				@Override
//				public void onBitmapCaptured(String filepath, int width, int height) {
////					LogUtils.d("RED", "filepath:" + filepath);
//					if (mEditor != null) {
//						mEditor.insertImageWithNewLine("file://" + filepath, width, height, "IMG_0");
//					}
//				}
//			};
//		}
		

		mEditor = (RichEditor) findViewById(R.id.editor);
		mEditor.setEditorHeight(200);
		mCompactTextChangeWatcher = new CompactTextChangeWatcher() {
			@Override
			protected void onHtmlTextChange(String text) {
				mPreview.setText(text);
				edit_text.setText(text);
				text_view.setText(edit_text.getText());
			}

			@Override
			protected void onCompactTextChange(String text) {
				mCPreview.setText(text);
			}

			@SuppressLint("NewApi")
			@Override
			protected void onConvertComplete(String convertText) {
				mCVreview.setText(convertText);
				cEditor.setHtml(convertText);
			}
		};
		
		mEditor.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
            	mEditor.post(new Runnable() {
                    @Override
                    public void run() {
//                    	mEditor.refreshVisibleViewportSize();
                    }
                });
            }
        });
		mEditor.setOnImgTapListener(new OnImgTapListener() {
			@Override
			public void onImgTap(final String imgcontent) {
				ShowImgEditDialog dialog = new ShowImgEditDialog(getActivity());
				dialog.setOnConfirmListener(new OnConfirmListener() {
					@Override
					public void onConfirm() {
						String html = mEditor.getHtml();
						LogUtils.d("MA", "<2>html[" + html + "]");
						if (html.contains(imgcontent)) {
							html = html.replace(imgcontent, "");
							LogUtils.d("MA", "<3>imgcontent[" + imgcontent + "]");
						} else {
							String addon = imgcontent.replace(">",
									" class=\"\">");
							html = html.replace(addon, "");
							LogUtils.d("MA", "<3>imgcontent[" + addon + "]");
						}
						mEditor.setHtml(html);
						mEditor.invalidate();
					}
				});
				dialog.show();
			}
		});

		cEditor = (RichEditor) findViewById(R.id.convert_editor);
		cEditor.setEditorHeight(200);

		mPreview = (TextView) findViewById(R.id.preview);
		mCPreview = (TextView) findViewById(R.id.compact_preview);
		mCVreview = (TextView) findViewById(R.id.convert_preview);
		mEditor.setOnTextChangeListener(mCompactTextChangeWatcher);
		if (getActivity().getIntent() != null) {
			String text = getActivity().getIntent().getStringExtra("text");
			if (text != null && text.length() > 0) {
				mEditor.setHtml(text);
			}
		}

		edit_text = (EditText) findViewById(R.id.edit_text);
		text_view = (TextView) findViewById(R.id.text_view);

		findViewById(R.id.action_undo).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.undo();
					}
				});

		findViewById(R.id.action_redo).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.redo();
					}
				});

		findViewById(R.id.action_bold).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setBold();
					}
				});

		findViewById(R.id.action_italic).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setItalic();
					}
				});

		findViewById(R.id.action_subscript).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setSubscript();
					}
				});

		findViewById(R.id.action_superscript).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setSuperscript();
					}
				});

		findViewById(R.id.action_strikethrough).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setStrikeThrough();
					}
				});

		findViewById(R.id.action_underline).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setUnderline();
					}
				});

		findViewById(R.id.action_heading1).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setHeading(1);
					}
				});

		findViewById(R.id.action_heading2).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setHeading(2);
					}
				});

		findViewById(R.id.action_heading3).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setHeading(3);
					}
				});

		findViewById(R.id.action_heading4).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setHeading(4);
					}
				});

		findViewById(R.id.action_heading5).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setHeading(5);
					}
				});

		findViewById(R.id.action_heading6).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setHeading(6);
					}
				});

		findViewById(R.id.action_txt_color).setOnClickListener(
				new View.OnClickListener() {
					boolean isChanged;

					@Override
					public void onClick(View v) {
						mEditor.setTextColor(isChanged ? Color.BLACK
								: Color.RED);
						isChanged = !isChanged;
					}
				});

		findViewById(R.id.action_bg_color).setOnClickListener(
				new View.OnClickListener() {
					boolean isChanged;

					@Override
					public void onClick(View v) {
						mEditor.setTextBackgroundColor(isChanged ? Color.TRANSPARENT
								: Color.YELLOW);
						isChanged = !isChanged;
					}
				});

		findViewById(R.id.action_indent).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setIndent();
					}
				});

		findViewById(R.id.action_outdent).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setOutdent();
					}
				});

		findViewById(R.id.action_align_left).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setAlignLeft();
					}
				});

		findViewById(R.id.action_align_center).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setAlignCenter();
					}
				});

		findViewById(R.id.action_align_right).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setAlignRight();
					}
				});

		findViewById(R.id.action_blockquote).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.setBlockquote();
					}
				});

		findViewById(R.id.action_insert_image).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
//						LogUtils.d("RED", "action_insert_image");
						// mEditor.insertImage(
						// "http://www.1honeywan.com/dachshund/image/7.21/7.21_3_thumb.JPG",
						// "dachshund");
						if (mImageAttacher != null) {
							mImageAttacher.toggle();
						}

						// mEditor.insertImage(
						// "file:///data/data/jp.wasabeef.sample/files/re_temp/editor_image.jpg",
						// "dachshund");
						// mEditor.insertImage(
						// "http://www.baidu.com/img/bd_logo1.png",
						// "dachshund");
					}
				});

		findViewById(R.id.action_insert_link).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mEditor.insertLink("https://github.com/wasabeef",
								"wasabeef");
					}
				});

		findViewById(R.id.action_launch).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = new Intent(getActivity(),
								MainActivity.class);
						intent.putExtra("text",
								mCompactTextChangeWatcher.getHtmlText());
						getActivity().startActivity(intent);
					}
				});
		mEditor.focusEditor();
	}

	Activity getActivity() {
		return this;
	}
}
