package jp.wasabeef.richeditor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.overlay.utils.LogUtils;

/**
 * Copyright (C) 2015 Wasabeef
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class RichEditor extends WebView {

  public enum Type {
    BOLD,
    ITALIC,
    SUBSCRIPT,
    SUPERSCRIPT,
    STRIKETHROUGH,
    UNDERLINE,
    H1,
    H2,
    H3,
    H4,
    H5,
    H6
  }
  
  public interface OnTextChangeListener {

    void onTextChange(String text);
    
    void getInnerText(String text);
    
    void getPastedText(String text);
    
    void onChange(String text);
  }

  public interface OnDecorationStateListener {

    void onStateChangeListener(String text, List<Type> types);
  }

  public interface AfterInitialLoadListener {

    void onAfterInitialLoad(boolean isReady);
  }
  
  public interface OnImgTapListener {
	void onImgTap(String imgcontent);
  }

  private static final String SETUP_HTML = "file:///android_res/raw/editor.html";
  private static final String INNERTEXT_SCHEME = "inner-text://";
  private static final String CALLBACK_SCHEME = "re-callback://";
  private static final String CHANGE_SCHEME = "change://";
  private static final String STATE_SCHEME = "re-state://";
  private static final String PASTE_SCHEME = "pastein://";
  private static final String CURSOR_SCHEME = "cursor://";
  private static final String IMGTAP_SCHEME = "img-tap://";
  private boolean isReady = false;
  private String mContents;
  private OnTextChangeListener mTextChangeListener;
  private OnDecorationStateListener mDecorationStateListener;
  private AfterInitialLoadListener mLoadListener;
  private OnImgTapListener mImgTapListener;

  private static ExecutorService sThreadPool = Executors.newSingleThreadExecutor();

  public RichEditor(Context context) {
    this(context, null);
  }

  public RichEditor(Context context, AttributeSet attrs) {
    this(context, attrs, android.R.attr.webViewStyle);
  }
  
  BaseInputConnection mConnection;
  
  private int illegitimacy(int cursorOffset, String cursorContent, String editStr, int selectionStart, int selectionEnd) {
	  int flag = 0;
	  if (cursorOffset == 1 && "".equalsIgnoreCase(cursorContent)) {
		  LogUtils.d("RED", ">>>前方有图片：Offset=1，行内容却是空");
		  flag = 1;
	  }
	  return flag;
  }
  
  private void deleteImage(int flag, Editable editable, String innerHtml) {
	  if (1 == flag) {
		  LogUtils.d("RED", ">>>1号情况：");
		  int size = images.size();
		  for (int i = 0; i < size; i++) {
			  String image = images.get(i);
			  if (innerHtml.contains(image)) {
				  innerHtml = innerHtml.replace(image, "");
				  setHtml(innerHtml);
			  }
		  }
	  }
  }
  
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
//        return new BaseInputConnection(this, false);
        
		return new CustomInputConnectionWrapper(
				super.onCreateInputConnection(outAttrs)) {
			@Override
			public boolean sendKeyEvent(KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP) {
		            if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
		            	BaseInputConnection target = getTarget();
		            	Editable editable = target.getEditable();
		            	String editStr = editable.toString();
		            	int selectionStart = Selection.getSelectionStart(editable);
	                    int selectionEnd = Selection.getSelectionEnd(editable);
	                    String innerHtml = mContents;
						LogUtils.d("RED", "KEYCODE_DEL->" + "cursorOffset="
								+ cursorOffset + ", cursorContent="
								+ cursorContent);
						LogUtils.d("RED", "editable=" + editStr + "selectionStart=" + selectionStart + ", selectionEnd=" + selectionEnd);
//	                    int flag = illegitimacy(cursorOffset, cursorContent, editStr, selectionStart, selectionEnd);
//	                    if (flag > 0) {
//	                    	deleteImage(flag, editable, innerHtml);
//	                    } else {
	                    	deleteSurroundingText(1, 0);
//	                    }
		                return true;
		            } else if (event.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL) {
		            	deleteSurroundingText(0, 1);
		                return true;
		            } else {
		                super.sendKeyEvent(event);
		            }
		        } else if (event.getAction() == KeyEvent.ACTION_DOWN) {
		            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
		                super.sendKeyEvent(event);
		                return true;
		            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
		                return true;
		            } else if (event.getKeyCode() == KeyEvent.KEYCODE_FORWARD_DEL) {
		                return true;
		            }
		        }
		        super.sendKeyEvent(event);
		        return true;
			}

			@Override
			public boolean commitText(CharSequence text, int newCursorPosition) {
				String input = text.toString();
				boolean matches = Pattern.compile("[^\\u0000-\\uFFFF]")
						.matcher(input).matches();
				if (matches) {
					return false;
				}
				return super.commitText(text, newCursorPosition);
			}

		};
	}
	
//	@Override
//	public boolean dispatchKeyFromIme(KeyEvent event) {
//	}

	private class ClipboardReader {
		@SuppressLint("NewApi")
		@JavascriptInterface
		public String getClipboardDataAsText() {
			String plainText = "";
			LogUtils.d("RED", "getClipboardDataAsText");
			ClipboardManager cm = (ClipboardManager) getContext()
					.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData cd = cm.getPrimaryClip();
			if (cd != null) {
//				ClipDescription cdes = cd.getDescription();
//				if (cdes != null) {
//					cdes.
//				}
				Item item = cd.getItemAt(0);
				plainText = item.getText().toString();
                LogUtils.d("RED", "originText[" + plainText + "]");
				plainText = plainText.replaceAll("[^\\u0000-\\uFFFF]", "");
				LogUtils.d("RED", "plainText[" + plainText + "]");
			}
			return plainText;
		}
		
		@SuppressLint("NewApi")
		@JavascriptInterface
		public String getDataOffset(String a) {
			String offset = "";
			if (a != null) {
				offset = a.toString();
				try {
					cursorOffset = Integer.valueOf(offset);
				} catch (Exception e) {
					cursorOffset = -1;
				}
			}
			return "getDataOffset" + offset;
		}
		
		@SuppressLint("NewApi")
		@JavascriptInterface
		public String getDataContent(String a) {
			String content = "";
			if (a != null) {
				content = a.toString();
				cursorContent = content;
			}
			return "getDataContent" + content;
		}
	}
	
  int cursorOffset = -1;
  String cursorContent = null;

  @SuppressLint("SetJavaScriptEnabled")
  public RichEditor(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setVerticalScrollBarEnabled(false);
    setHorizontalScrollBarEnabled(false);
    getSettings().setJavaScriptEnabled(true);
    addJavascriptInterface(new ClipboardReader(), "ClipboardReader");
    setWebChromeClient(new WebChromeClient() {
    	
    	
		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				JsResult result) {
			LogUtils.d("RED", "----------------------onJsAlert[" + url + "][" + message + "]");
			return super.onJsAlert(view, url, message, result);
		}
    });
    setWebViewClient(new WebViewClient() {
      @Override public void onPageFinished(WebView view, String url) {
        isReady = url.equalsIgnoreCase(SETUP_HTML);
        if (mLoadListener != null) {
          mLoadListener.onAfterInitialLoad(isReady);
        }
      }

//      @Override
//	public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
//		return true;
//	}

	@Override 
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
		LogUtils.d("RRR", "shouldOverrideUrlLoading:" + url);
        String decode;
        try {
          decode = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          // No handling
          return false;
        }

        if (TextUtils.indexOf(url, CALLBACK_SCHEME) == 0) {
          callback(decode);
          return true;
        } else if (TextUtils.indexOf(url, STATE_SCHEME) == 0) {
          stateCheck(decode);
          return true;
        } else if (TextUtils.indexOf(url, PASTE_SCHEME) == 0) {
          pastein(decode);
          return true;
        } else if (TextUtils.indexOf(url, INNERTEXT_SCHEME) == 0) {
          innerText(decode);
          return true;
        } else if (TextUtils.indexOf(url, CHANGE_SCHEME) == 0) {
          change(decode);
          return true;
        } else if (TextUtils.indexOf(url, CURSOR_SCHEME) == 0) {
          cursor(decode);
          return true;
        } else if (TextUtils.indexOf(url, IMGTAP_SCHEME) == 0) {
          imagetap(decode);
          return true;
        }

        return super.shouldOverrideUrlLoading(view, url);
      }
    });
    loadUrl(SETUP_HTML);

    applyAttributes(context, attrs);
  }

  public void setOnTextChangeListener(OnTextChangeListener listener) {
    mTextChangeListener = listener;
  }

  public void setOnDecorationChangeListener(OnDecorationStateListener listener) {
    mDecorationStateListener = listener;
  }

  public void setOnInitialLoadListener(AfterInitialLoadListener listener) {
    mLoadListener = listener;
  }
  
  public void setOnImgTapListener(OnImgTapListener listener) {
	  mImgTapListener = listener;
  }

  private void callback(String text) {
	LogUtils.d("RED", "callback[" + text + "]");
    mContents = text.replaceFirst(CALLBACK_SCHEME, "");
    if (mTextChangeListener != null) {
      mTextChangeListener.onTextChange(mContents);
    }
  }

  private void stateCheck(String text) {
	  LogUtils.d("RED", "stateCheck[" + text + "]");
    String state = text.replaceFirst(STATE_SCHEME, "").toUpperCase(Locale.ENGLISH);
    List<Type> types = new ArrayList<Type>();
    for (Type type : Type.values()) {
      if (TextUtils.indexOf(state, type.name()) != -1) {
        types.add(type);
      }
    }

    if (mDecorationStateListener != null) {
      mDecorationStateListener.onStateChangeListener(state, types);
    }
  }
  
  private void pastein(String text) {
	  LogUtils.d("RED", "pastein[" + text + "]");
	  String contents = text.replaceFirst(PASTE_SCHEME, "");
	  LogUtils.d("RED", "pastein[" + text + "]");
	  if (mTextChangeListener != null) {
	      mTextChangeListener.getPastedText(contents);
	  }
  }
  
  private void innerText(String text) {
	  LogUtils.d("RED", "innerText[" + text + "]");
	    String contents = text.replaceFirst(INNERTEXT_SCHEME, "");
	    if (mTextChangeListener != null) {
	      mTextChangeListener.getInnerText(contents);
	    }
  }
  
  private void change(String text) {
	  LogUtils.d("RED", "change[" + text + "]");
	  String contents = text.replaceFirst(CHANGE_SCHEME, "");
	    if (mTextChangeListener != null) {
	      mTextChangeListener.getInnerText(contents);
	    }
  }
  
  private void cursor(String text) {
	  LogUtils.d("RED", "cursor[" + text + "]");
	  String contents = text.replaceFirst(CURSOR_SCHEME, "");
  }
  
  private void imagetap(String text) {
	  LogUtils.d("RED", "imagetap[" + text + "]");
	  String contents = text.replaceFirst(IMGTAP_SCHEME, "");
	  if (mImgTapListener != null) {
		  mImgTapListener.onImgTap(contents);
	  }
  }

  private void applyAttributes(Context context, AttributeSet attrs) {
    final int[] attrsArray = new int[] {
        android.R.attr.gravity
    };
    TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);

    int gravity = ta.getInt(0, NO_ID);
    switch (gravity) {
      case Gravity.LEFT:
        exec("javascript:RE.setTextAlign(\"left\")");
        break;
      case Gravity.RIGHT:
        exec("javascript:RE.setTextAlign(\"right\")");
        break;
      case Gravity.TOP:
        exec("javascript:RE.setVerticalAlign(\"top\")");
        break;
      case Gravity.BOTTOM:
        exec("javascript:RE.setVerticalAlign(\"bottom\")");
        break;
      case Gravity.CENTER_VERTICAL:
        exec("javascript:RE.setVerticalAlign(\"middle\")");
        break;
      case Gravity.CENTER_HORIZONTAL:
        exec("javascript:RE.setTextAlign(\"center\")");
        break;
      case Gravity.CENTER:
        exec("javascript:RE.setVerticalAlign(\"middle\")");
        exec("javascript:RE.setTextAlign(\"center\")");
        break;
    }

    ta.recycle();
  }

  public void setHtml(String contents) {
    if (contents == null) {
      contents = "";
    }
    try {
      exec("javascript:RE.setHtml('" + URLEncoder.encode(contents, "UTF-8") + "');");
    } catch (UnsupportedEncodingException e) {
      // No handling
    }
    mContents = contents;
  }

  public String getHtml() {
    return mContents;
  }
  
  public void getInnerText() {
	exec("javascript:RE.getInnerText();");
  }
  
  public void refreshVisibleViewportSize() {
	exec("javascript:RE.refreshVisibleViewportSize();");
  }

  public void setEditorFontColor(int color) {
    String hex = convertHexColorString(color);
    exec("javascript:RE.setBaseTextColor('" + hex + "');");
  }

  public void setEditorFontSize(int px) {
    exec("javascript:RE.setBaseFontSize('" + px + "px');");
  }

  @Override public void setPadding(int left, int top, int right, int bottom) {
    super.setPadding(left, top, right, bottom);
    exec("javascript:RE.setPadding('" + left + "px', '" + top + "px', '" + right + "px', '" + bottom
        + "px');");
  }

  @Override public void setPaddingRelative(int start, int top, int end, int bottom) {
    // still not support RTL.
    setPadding(start, top, end, bottom);
  }

  public void setEditorBackgroundColor(int color) {
    setBackgroundColor(color);
  }

  @Override public void setBackgroundColor(int color) {
    super.setBackgroundColor(color);
  }

  @Override public void setBackgroundResource(int resid) {
    Bitmap bitmap = Utils.decodeResource(getContext(), resid);
    String base64 = Utils.toBase64(bitmap);
    bitmap.recycle();

    exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
  }

  @Override public void setBackground(Drawable background) {
    Bitmap bitmap = Utils.toBitmap(background);
    String base64 = Utils.toBase64(bitmap);
    bitmap.recycle();

    exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
  }

  public void setBackground(String url) {
    exec("javascript:RE.setBackgroundImage('url(" + url + ")');");
  }

  public void setEditorWidth(int px) {
    exec("javascript:RE.setWidth('" + px + "px');");
  }

  public void setEditorHeight(int px) {
    exec("javascript:RE.setHeight('" + px + "px');");
  }

  public void setPlaceholder(String placeholder) {
    exec("javascript:RE.setPlaceholder('" + placeholder + "');");
  }

  public void loadCSS(String cssFile) {
    String jsCSSImport = "(function() {" +
        "    var head  = document.getElementsByTagName(\"head\")[0];" +
        "    var link  = document.createElement(\"link\");" +
        "    link.rel  = \"stylesheet\";" +
        "    link.type = \"text/css\";" +
        "    link.href = \"" + cssFile + "\";" +
        "    link.media = \"all\";" +
        "    head.appendChild(link);" +
        "}) ();";
    exec("javascript:" + jsCSSImport + "");
  }

  public void undo() {
    exec("javascript:RE.undo();");
  }

  public void redo() {
    exec("javascript:RE.redo();");
  }

  public void setBold() {
    exec("javascript:RE.setBold();");
  }

  public void setItalic() {
    exec("javascript:RE.setItalic();");
  }

  public void setSubscript() {
    exec("javascript:RE.setSubscript();");
  }

  public void setSuperscript() {
    exec("javascript:RE.setSuperscript();");
  }

  public void setStrikeThrough() {
    exec("javascript:RE.setStrikeThrough();");
  }

  public void setUnderline() {
    exec("javascript:RE.setUnderline();");
  }

  public void setTextColor(int color) {
    exec("javascript:RE.prepareInsert();");

    String hex = convertHexColorString(color);
    exec("javascript:RE.setTextColor('" + hex + "');");
  }

  public void setTextBackgroundColor(int color) {
    exec("javascript:RE.prepareInsert();");

    String hex = convertHexColorString(color);
    exec("javascript:RE.setTextBackgroundColor('" + hex + "');");
  }

  public void removeFormat() {
    exec("javascript:RE.removeFormat();");
  }

  public void setHeading(int heading) {
    exec("javascript:RE.setHeading('" + heading + "');");
  }

  public void setIndent() {
    exec("javascript:RE.setIndent();");
  }

  public void setOutdent() {
    exec("javascript:RE.setOutdent();");
  }

  public void setAlignLeft() {
    exec("javascript:RE.setJustifyLeft();");
  }

  public void setAlignCenter() {
    exec("javascript:RE.setJustifyCenter();");
  }

  public void setAlignRight() {
    exec("javascript:RE.setJustifyRight();");
  }

  public void setBlockquote() {
    exec("javascript:RE.setBlockquote();");
  }

  public void insertImage(String url, String alt) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertImage('" + url + "', '" + alt + "');");
  }
  
  public void insertImage(String url, int width, int height, String alt) {
	exec("javascript:RE.prepareInsert();");
	exec("javascript:RE.insertImage('" + url + "', '"
			+ String.valueOf(width) + "', '" + String.valueOf(height)
			+ "', '" + alt + "');");
	images.add("<img src=\"" + url + "\" width=\"" + width + "px\" height=\""
			+ height + "px\" alt=\"" + alt + "\">");
  }
  
  public void insertImageWithNewLine(String url, int width, int height, String alt) {
	exec("javascript:RE.prepareInsert();");
	exec("javascript:RE.insertImageWithNewLine('" + url + "', '"
			+ String.valueOf(width) + "', '" + String.valueOf(height)
			+ "', '" + alt + "');");
	images.add("<img src=\"" + url + "\" width=\"" + width + "px\" height=\""
			+ height + "px\" alt=\"" + alt + "\">");
  }

  List<String> images = new ArrayList<String>();

  public void insertLink(String href, String title) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertLink('" + href + "', '" + title + "');");
  }

  public void focusEditor() {
    requestFocus();
    exec("javascript:RE.focus();");
  }

  public void clearFocusEditor() {
    exec("javascript:RE.blurFocus();");
  }

  private String convertHexColorString(int color) {
    return String.format("#%06X", (0xFFFFFF & color));
  }

  private void exec(String trigger) {
    if (isReady) {
      load(trigger);
    } else {
      new WaitLoad(trigger).executeOnExecutor(sThreadPool);
    }
  }

  @SuppressLint("NewApi")
private void load(String trigger) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      evaluateJavascript(trigger, null);
    } else {
      loadUrl(trigger);
    }
  }

  private class WaitLoad extends AsyncTask<Void, Void, Void> {

    private String mTrigger;

    public WaitLoad(String trigger) {
      super();
      mTrigger = trigger;
    }

    @Override protected Void doInBackground(Void... params) {
      while (!RichEditor.this.isReady) {
        sleep(100);
      }
      return null;
    }

    @Override protected void onPostExecute(Void aVoid) {
      load(mTrigger);
    }

    private synchronized void sleep(long ms) {
      try {
        wait(ms);
      } catch (InterruptedException ignore) {
      }
    }
  }

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		LogUtils.d("RED", "onScrollChanged[l=" + l + ", t=" + t + ", oldl="
				+ oldl + ", oldt=" + oldt);
		super.onScrollChanged(l, t, oldl, oldt);
	}
  
  
}