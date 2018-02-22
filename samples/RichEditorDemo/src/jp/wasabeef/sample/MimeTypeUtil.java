package jp.wasabeef.sample;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Pattern;

import android.webkit.MimeTypeMap;

/**
 * Extend of android.webkit.MimeTypeMap: Two-way map that maps MIME-types to
 * file extensions and vice versa.
 * 
 * @author liu_chonghui
 */
public class MimeTypeUtil {

	private static MimeTypeUtil sMimeTypeUtil;

	private HashMap<String, String> mMimeTypeToExtensionMap;

	private HashMap<String, String> mExtensionToMimeTypeMap;

	private MimeTypeUtil() {
		mMimeTypeToExtensionMap = new HashMap<String, String>();
		mExtensionToMimeTypeMap = new HashMap<String, String>();
	}

	public static String getFileExtensionFromUrl(String url) {
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null && extension.length() > 0) {
			return extension;
		}

		if (url != null && url.length() > 0) {
			int query = url.lastIndexOf('?');
			if (query > 0) {
				url = url.substring(0, query);
			}
			int filenamePos = url.lastIndexOf('/');
			String filename = 0 <= filenamePos ? url.substring(filenamePos + 1)
					: url;

			if (filename.length() > 0
					&& Pattern
							.matches("[a-zA-Z_0-9\\@\\.\\-\\(\\)]+", filename)) {
				int dotPos = filename.lastIndexOf('.');
				if (0 <= dotPos) {
					return filename.substring(dotPos + 1);
				}
			}
		}

		return "";
	}

	public static String mimeTypeFromExtension(String extension) {
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				extension);
		if (mimeType != null && mimeType.length() > 0) {
			return mimeType;
		}

		return MimeTypeUtil.getSingleton().getMimeTypeFromExtension(extension);
	}

	public static MimeTypeUtil getSingleton() {
		if (sMimeTypeUtil == null) {
			sMimeTypeUtil = new MimeTypeUtil();
			sMimeTypeUtil.loadEntry("gps/ncr", "ncr");
			sMimeTypeUtil.loadEntry("image/webp", "webp");
		}
		return sMimeTypeUtil;
	}

	public String getMimeTypeFromExtension(String extension) {
		String mimeType = mExtensionToMimeTypeMap.get(extension);
		if (mimeType != null && mimeType.length() > 0) {
			return mimeType;
		}

		return "unknown" + File.separator + extension;
	}

	private void loadEntry(String mimeType, String extension) {
		if (!mMimeTypeToExtensionMap.containsKey(mimeType)) {
			mMimeTypeToExtensionMap.put(mimeType, extension);
		}

		mExtensionToMimeTypeMap.put(extension, mimeType);
	}
}
