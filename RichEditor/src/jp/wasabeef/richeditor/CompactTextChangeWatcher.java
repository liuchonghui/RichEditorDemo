package jp.wasabeef.richeditor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.wasabeef.richeditor.RichEditor.OnTextChangeListener;
import android.text.Html;

import com.android.overlay.utils.LogUtils;

public class CompactTextChangeWatcher implements OnTextChangeListener {

	String htmlText = "";
	String compactText = "";
	String convertText = "";
	String innerText = "";
	Set<String> savedPath = new HashSet<String>();
	ArrayList<String> paths = new ArrayList<String>();

	public String getHtmlText() {
		if ("<br>".equalsIgnoreCase(htmlText)) {
			return "";
		}
		return htmlText;
	}
	
	@Override
	public void getPastedText(String text) {
		LogUtils.d("CTCW", "getPastedText[" + text + "]");
	}
	
	@Override
	public void onChange(String text) {
		LogUtils.d("CTCW", "onChange[" + text + "]");		
	}
	
	protected void onPastedTextReceived(String text) {
	}

	public String getCompactText() {
		if (this.compactText == null || this.compactText.length() == 0) {
			flushText(getHtmlText());
		}
		return this.compactText;
	}

	protected void onImgDeleted(Set<String> deletedImgPath) {

	}

	public List<String> getImgs() {
		if (compactText != null && compactText.contains(imgSpanTag)) {
			if (this.paths == null || this.paths.size() == 0) {
				subImg(compactText);
			}
		}
		return this.paths;
	}

	public void flushText(String inuptHtmlText) {
		LogUtils.d("CTCW", "flushText[" + inuptHtmlText + "]");
		htmlText = inuptHtmlText;
		String subNbsp = subNbsp(inuptHtmlText);
		String subDiv = subDiv(subNbsp);
		String subBr = subBr(subDiv);
		String subImg = subImg(subBr);
		CharSequence fromHtml = Html.fromHtml(subImg);
		String removeSpanLabel = removeSpanLabel(fromHtml.toString());
		String addImgSpan = addImgSpan(removeSpanLabel);
		compactText = addImgSpan;
	}

	boolean ifFromHtml = false;
	
	@Override
	public void getInnerText(String text) {
		LogUtils.d("CTCW", "<60071>getInnerText[" + text + "]");
		innerText = text;
		onInnerTextReceived(text);
	}
	
	protected void onInnerTextReceived(String text) {
	}

	@Override
	final public void onTextChange(String text) {
		LogUtils.d("CTCW", "<0>onTextChange[" + text + "]");
		htmlText = text;
		onHtmlTextChange("<br>".equalsIgnoreCase(htmlText) ? "" : htmlText);
		String subNbsp = subNbsp(text);
		LogUtils.d("CTCW", "<1>after subNbsp[" + subNbsp + "]");
		String subDiv = subDiv(subNbsp);
		LogUtils.d("CTCW", "<2>after subDiv[" + subDiv + "]");
		String subBr = subBr(subDiv);
		LogUtils.d("CTCW", "<3>after subBr[" + subBr + "]");
		String subImg = subImg(subBr);
		if (savedPath.size() > paths.size()) {
			savedPath.removeAll(paths);
		}
		onImgDeleted(savedPath);
		LogUtils.d("CTCW", "<4>after subImg[" + subImg + "]");
		CharSequence result = Html.fromHtml(subImg);
		String fromHtml = result.toString();
		LogUtils.d("CTCW", "<5>after fromHtml[" + fromHtml + "]");
		// String removeSpanLabel = removeSpanLabel(fromHtml);
		// LogUtils.d("CTCW", "<6>after removeSpanLabel[" + removeSpanLabel + "]");
		String addImgSpan = addImgSpan(fromHtml);
		LogUtils.d("CTCW", "<7>add ImgSpan[" + addImgSpan + "]");
		compactText = addImgSpan;
		onCompactTextChange(compactText);

		ifFromHtml = false;
		if (!fromHtml.equalsIgnoreCase(subImg)) {
			ifFromHtml = true;
		}

		if (ifFromHtml) {
			restoreText(compactText);
		}

		String addBr = addBr(compactText);
		onConvertComplete(addBr);
	}

	public void restoreText(String compactText) {

	}

	protected void onTextRestore(String restoreHtmlText) {

	}

	protected void onHtmlTextChange(String htmlText) {
	}

	protected void onCompactTextChange(String text) {
	}

	private String subDiv(String input) {
		String str = new String(input);
		try {
			str = str.replaceAll("<div><br></div>", "\n");
			str = str.replaceAll("</div>", "");
			str = str.replaceAll("<div>", "\n");
		} catch (Exception e) {
			e.printStackTrace();
			str = new String(input);
		}
		return str;
	}

	private String subBr(String input) {
		String str = new String(input);
		try {
			str = str.replaceAll("<br>", "\n");
		} catch (Exception e) {
			e.printStackTrace();
			str = new String(input);
		}
		return str;
	}

	private String subImg(String input) {
		String str = new String(input);
		try {
			int no = 0;
			while (str.contains(imgSpanTag)) {
				str = cutString(str, no);
				no++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			str = new String(input);
		}
		return str;
	}

	private String removeSpanLabel(String input) {
		String str = new String(input);
		try {
			str = str.replaceAll("<span>", "");
			str = str.replaceAll("[span]", "");
			str = str.replaceAll("</span>", "");
			str = str.replaceAll("[/span]", "");
		} catch (Exception e) {
			e.printStackTrace();
			str = new String(input);
		}
		return str;
	}

	private String addImgSpan(String input) {
		String str = new String(input);
		try {
			str = str.replaceAll("IMG_0", "[!--IMG_0--]");
		} catch (Exception e) {
			e.printStackTrace();
			str = new String(input);
		}
		return str;
	}

	private String subNbsp(String input) {
		String str = new String(input);
		try {
			str = str.replaceAll("&nbsp;", " ");
		} catch (Exception e) {
			e.printStackTrace();
			str = new String(input);
		}
		return str;
	}

	public final String imgSpanTag = "<img src=\"";
	public final String imgSpanHead = "<img src=\"file://";
	public final String imgSpanTail = "\" alt=\"IMG_0\">";

	private String cutString(String input, int position) {
		int start = input.indexOf(imgSpanHead);
//		int pathStart = start + imgSpanHead.length();
		int pathEnd = input.indexOf(imgSpanTail);
		int end = pathEnd + imgSpanTail.length();
		String span = input.substring(start, end);
//		String path = input.substring(pathStart, pathEnd);
		if (position == 0) {
			savedPath.addAll(paths);
			paths.clear();
		}
		RichEditerContent richContent= Utils.parseContent(span);
		paths.add(richContent.getSrc());
		LogUtils.d("RED", "span[" + span + "]");
		for (int i = 0; i < paths.size(); i++) {
			String content = paths.get(i);
			LogUtils.d("RED", "path[" + i + "]=[" + content + "]");
		}
		String ret = input.replaceFirst(span, "IMG_0");
		return ret;
	}

	public String compactToHtml(String compactText) {
		String htmlText = compactText;
		return htmlText;
	}

	private String addBr(String input) {
		String str = new String(input);
		try {
			str = str.replaceAll("\n", "<br>");
		} catch (Exception e) {
			e.printStackTrace();
			str = new String(input);
		}
		return str;
	}

	protected void onConvertComplete(String convertText) {
	}

	public static String adjustToCommit(String commit) {
		String result = commit;
		if (commit == null || commit.length() == 0) {
			return "";
		}
		result = result.replace("[!--IMG_", "\n[!--IMG_");
		result = result.replace("--]", "--]\n");
		while (result.endsWith("\n")) {
			result = result.substring(0, result.length() - "\n".length());
		}
		while (result.startsWith("\n")) {
			result = result.substring("\n".length(), result.length());
		}
		result = result.replace("--]\n\n[!--IMG_", "--]\n[!--IMG_");
		return result;
	}

}
