package jp.wasabeef.sample;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author liu_chonghui
 * 
 */
public class Mime {

	protected File file;
	protected String filePath;
	protected String fileName;
	protected String extension;
	protected String mimeType;

	private Object extra;

	protected Mime() {
	}

	protected Mime(Mime mime) {
		this.file = mime.getFile();
		this.filePath = mime.getFilePath();
		this.fileName = mime.getFileName();
		this.extension = mime.getExtension();
		this.mimeType = mime.getMimeType();
	}

	protected Mime(String filePath) {
		if (filePath != null && filePath.length() > 0) {
			this.filePath = filePath;
			file = new File(filePath);
			if (file != null && file.exists()) {
				fileName = file.getName();
			} else {
				int lenth = filePath.length();
				int index = filePath.lastIndexOf("/");
				if (index != -1 && (index + 1) < lenth) {
					fileName = filePath.substring(index + 1, lenth);
				}
			}
			extension = getFileExtenstion(filePath);
			mimeType = getMimeType(extension);
		}
	}

	public static String getFileExtenstion(String filePath) {
		return MimeTypeUtil.getFileExtensionFromUrl(filePath);
	}

	public static String getMimeType(String fileExtension) {
		return MimeTypeUtil.mimeTypeFromExtension(fileExtension);
	}

	public File getFile() {
		return this.file;
	}

	public String getFileName() {
		return this.fileName;
	}

	public String getExtension() {
		return this.extension;
	}

	public String getMimeType() {
		return this.mimeType;
	}

	public String getFilePath() {
		return this.filePath;
	}

	public String getFileSize() {
		return getFileLength();
	}

	public String getFileLength() {
		if (this.file != null && this.file.exists()) {
			return String.valueOf(this.file.length());
		}
		return null;
	}

	public void setExtra(Object obj) {
		this.extra = obj;
	}

	@SuppressWarnings("unchecked")
	public <T> T getExtra(Class<T> cls) {
		T target = null;
		try {
			if (extra != null && cls.isInstance(extra)) {
				return (T) extra;
			}
		} catch (Exception e) {
			target = null;
		}
		return target;
	}

	// public <T> T getExtra(T input) {
	// try {
	// if (extra != null && extra instanceof StringMap) {
	// Iterator it = ((StringMap) extra).entrySet().iterator();
	// while (it.hasNext()) {
	// Entry pairs = (Entry) it.next();
	// Class<?> c = input.getClass();
	// Field value = c.getDeclaredField((String) pairs.getKey());
	// value.setAccessible(true);
	// value.set(input, pairs.getValue());
	// }
	//
	// return input;
	// }
	// } catch (Exception e) {
	// input = null;
	// }
	// return input;
	// }

	public Object getExtra() {
		return this.extra;
	}

	public <T> T parseExtra(String extraStr, Class<T> cls) {
//		return GsonUtil.parseObject(extraStr, cls);
		return null;
	}

	public static String getString(HashMap<String, Mime> mime, String mimeType) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Mime> obj : mime.entrySet()) {
			String type = obj.getValue().getMimeType();
			File file = obj.getValue().getFile();
			if (type.contains(mimeType)) {
				if (sb.toString().length() > 0) {
					sb.append(";");
				}
				sb.append(file.getAbsolutePath());
			}
		}
		return sb.toString();
	}

	@Override
	public String toString() {
//		return GsonUtil.objectToJson(this);
		return super.toString();
	}
}
