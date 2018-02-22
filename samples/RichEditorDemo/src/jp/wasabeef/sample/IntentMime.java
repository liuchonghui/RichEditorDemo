package jp.wasabeef.sample;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author liu_chonghui
 * 
 */
public class IntentMime extends Mime implements Parcelable {
	public static String VIDEO_TYPE = "video";
	public static String AUDIO_TYPE = "audio";
	public static String IMAGE_TYPE = "image";
	public static String GPS_TYPE = "gps";
	public static int LOCATION_LOCAL = 0;
	public static int LOCATION_ONLINE = 1;

	private String uri;
	private String uuid;
	private String fileSize;
	private String type;
	private int location = -1;

	protected IntentMime() {
	}

	public IntentMime(Mime mime) {
		super(mime);
	}

	public IntentMime(IntentMime mime) {
		this.file = mime.getFile();
		this.filePath = mime.getFilePath();
		this.fileName = mime.getFileName();
		this.extension = mime.getExtension();
		this.mimeType = mime.getMimeType();

		this.uri = mime.getUri();
		this.uuid = mime.getUuid();
		this.fileSize = mime.getFileSize();
		this.type = mime.getType();
		this.location = mime.getLocation();
	}

	public IntentMime(String filePath) {
		this(from(new Mime(filePath)));
	}

	@Override
	public File getFile() {
		File file = super.getFile();
		if (file == null || !file.exists()) {
			if (filePath != null && filePath.length() > 0) {
				file = new File(filePath);
			}
		}
		return file;
	}

	public String getUri() {
		return uri;
	}

	protected void setUri(String uri) {
		this.uri = uri;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getType() {
		return type;
	}

	protected void setType(String type) {
		this.type = type;
	}

	public String getFileSize() {
		return fileSize;
	}

	protected void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public int getLocation() {
		return location;
	}

	protected void setLocation(int location) {
		this.location = location;
	}

	public static IntentMime from(Mime mime) {
		IntentMime im = null;
		if (mime != null) {
			im = new IntentMime(mime);
			String path = mime.getFilePath();
			if (path != null && path.length() > 0) {
				if (path.startsWith(File.separator)) {
					im.setLocation(LOCATION_LOCAL);
					im.setUri("file://" + path);
				} else {
					im.setLocation(LOCATION_ONLINE);
					im.setUri(path);
				}
				im.setUuid(UUID.randomUUID().toString());
				im.setFileSize(sizeSerialize(mime.getFile()));
				if (mime.getMimeType().contains(AUDIO_TYPE)) {
					im.setType(AUDIO_TYPE);
				} else if (mime.getMimeType().contains(VIDEO_TYPE)) {
					im.setType(VIDEO_TYPE);
				} else if (mime.getMimeType().contains(IMAGE_TYPE)) {
					im.setType(IMAGE_TYPE);
				} else if (mime.getMimeType().contains(GPS_TYPE)) {
					im.setType(GPS_TYPE);
				}
			}
		}
		return im;
	}

	public static ArrayList<IntentMime> from(List<Mime> from) {
		if (from == null || from.size() == 0) {
			return null;
		}

		ArrayList<IntentMime> list = new ArrayList<IntentMime>();
		for (Mime obj : from) {
			list.add(from(obj));
		}
		return list;
	}

	public static ArrayList<IntentMime> from(ArrayList<IntentMime> from) {
		if (from == null || from.size() == 0) {
			return null;
		}

		ArrayList<IntentMime> list = new ArrayList<IntentMime>();
		list.addAll(from);
		return list;
	}

	public static ArrayList<IntentMime> from(HashMap<String, Mime> container) {
		if (container == null || container.size() == 0) {
			return null;
		}

		ArrayList<IntentMime> list = new ArrayList<IntentMime>();
		for (Map.Entry<String, Mime> obj : container.entrySet()) {
			list.add(from(obj.getValue()));
		}

		return list;
	}

	public static String getString(ArrayList<IntentMime> mimeList,
			String mimeType) {
		StringBuilder sb = new StringBuilder();
		for (IntentMime obj : mimeList) {
			String type = obj.getMimeType();
			File file = obj.getFile();
			if (mimeType.equalsIgnoreCase(type)) {
				if (sb.toString().length() > 0) {
					sb.append(";");
				}
				sb.append(file.getAbsolutePath());
			}
		}
		return sb.toString();
	}

	public static Intent formatIntent(Intent intent, ArrayList<IntentMime> mimes) {
		if (intent == null || mimes == null) {
			return null;
		}

		Bundle extras = new Bundle();
		extras.putParcelableArrayList("mimes", mimes);
		intent.putExtras(extras);
		return intent;
	}

	public static ArrayList<IntentMime> formatList(Intent intent) {
		if (intent == null) {
			return null;
		}

		ArrayList<IntentMime> mimes = new ArrayList<IntentMime>();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			ArrayList<IntentMime> list = extras.getParcelableArrayList("mimes");
			if (list != null && list.size() > 0) {
				for (IntentMime mime : list) {
					String path = mime.getFilePath();
					if (path != null && path.length() > 0) {
						mimes.add(new IntentMime(mime.getFilePath()));
					}
				}
			}
		}
		return mimes;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.uri);
		dest.writeString(this.uuid);
		dest.writeString(this.filePath);
		dest.writeString(this.extension);
		dest.writeString(this.mimeType);
		dest.writeString(this.fileSize);
		dest.writeString(this.type);
		dest.writeInt(this.location);
	}

	public static final Parcelable.Creator<IntentMime> CREATOR = new Parcelable.Creator<IntentMime>() {

		@Override
		public IntentMime createFromParcel(Parcel arg0) {
			return new IntentMime(arg0);
		}

		@Override
		public IntentMime[] newArray(int arg0) {
			return new IntentMime[arg0];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	public IntentMime(Parcel in) {
		this.uri = in.readString();
		this.uuid = in.readString();
		this.filePath = in.readString();
		this.extension = in.readString();
		this.mimeType = in.readString();
		this.fileSize = in.readString();
		this.type = in.readString();
		this.location = in.readInt();
	}

	static int SIZE_SCALE = 2;
	static int SIZE_1K = 1024;
	static int SIZE_1M = 1024 * 1024;

	public static String sizeSerialize(long length) {
		if (length < 0) {
			return null;
		}

		String size = null;
		if (length < SIZE_1M) {
			size = formatToKB(length);
		} else {
			size = formatToMB(length);
		}
		return size;
	}

	public static String sizeSerialize(File file) {
		if (file == null || !file.exists()) {
			return null;
		}

		String size = null;
		long value = file.length();
		if (value < SIZE_1M) {
			size = formatToKB(value);
		} else {
			size = formatToMB(value);
		}
		return size;
	}

	private static String formatToKB(long value) {
		double kbSize = (double) value / SIZE_1K;
		StringBuilder sb = new StringBuilder();
		sb.append(formatDouble(kbSize)).append(" KB");
		return sb.toString();
	}

	private static String formatToMB(long value) {
		double mbSize = (double) value / SIZE_1M;
		StringBuilder sb = new StringBuilder();
		sb.append(formatDouble(mbSize)).append(" MB");
		return sb.toString();
	}

	private static String formatDouble(double number) {
		String pattern = String.format(Locale.getDefault(), "%%.%df",
				SIZE_SCALE);
		return String.format(pattern, number);
	}

}
