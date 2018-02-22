package jp.wasabeef.sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Spannable;

/**
 * 
 * @author liu_chonghui
 * 
 */
public class ImageMime extends IntentMime {

	protected int degrees;

	protected ImageMime() {
	}

	public ImageMime(String savePath) {
		super(savePath);
		if (!IMAGE_TYPE.equalsIgnoreCase(getType())) {
			throw new IllegalStateException(getMimeType()
					+ " was not an IMAGE_TYPE!");
		}
	}

	public int getDegrees() {
		return degrees;
	}

	public void setDegrees(int degrees) {
		this.degrees = degrees;
	}

	public static ImageMime getMediaStoreData(Context context, Intent intent) {
		if (context == null || intent == null) {
			return null;
		}

		ImageMime mime = new ImageMime();
		Uri selectedImage = intent.getData();
		if (selectedImage == null) {
			Object obj = null;
			Bundle extra = intent.getExtras();
			if (extra != null) {
				obj = extra.get("data");
			}
			if (obj != null && obj instanceof Bitmap) {
				Bitmap bmp = (Bitmap) obj;
				String tempFile = new String(getFileDir(context) + "temp.jpeg");
				if (saveBitmapToFile(bmp, tempFile)) {
					mime = new ImageMime(tempFile);
				}
			}

		} else {
			int rotation = 0;
			String savePath = null;
			String[] mediaStoreColumns = { MediaStore.Images.Media.DATA,
					MediaStore.Images.ImageColumns.ORIENTATION };
			Cursor c = context.getContentResolver().query(selectedImage,
					mediaStoreColumns, null, null, null);
			if (c != null) {
				c.moveToFirst();
				int pathIndex = c.getColumnIndex(mediaStoreColumns[0]);
				int rotateIndex = c.getColumnIndex(mediaStoreColumns[1]);
				rotation = c.getInt(rotateIndex);
				savePath = c.getString(pathIndex);
				c.close();
			}
			if (savePath == null || savePath.length() == 0) {
				savePath = getPath(context, selectedImage);
			}
			if (savePath != null && savePath.length() > 0) {
				mime = new ImageMime(savePath);
				mime.setDegrees(rotation);
			}
		}
		return mime;
	}

	public static Spannable getMimeText(Context context, CharSequence text,
			int drawableId) {
		Spannable spannable = Spannable.Factory.getInstance()
				.newSpannable(text);
		getMimeText(context, spannable, drawableId);
		return spannable;
	}

	protected static String getFileDir(Context context) {
		String dir = context.getFilesDir().getAbsolutePath() + File.separator;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String dowload = android.os.Environment.DIRECTORY_DOWNLOADS;
			File downloadDir = context.getExternalFilesDir(dowload);
			if (downloadDir != null) {
				if (!downloadDir.exists()) {
					downloadDir.mkdirs();
				}
				dir = downloadDir.getAbsolutePath() + File.separator;
			}
		}
		return dir;
	}

	public static boolean saveBitmapToFile(Bitmap bmp, String fileName) {
		if (bmp == null || bmp.isRecycled()) {
			return false;
		}
		if (fileName == null || fileName.length() == 0) {
			return false;
		}

		File file = new File(fileName);
		if (file != null && !file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		CompressFormat format = Bitmap.CompressFormat.JPEG;
		int quality = 80;
		OutputStream stream = null;
		try {
			stream = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return bmp.compress(format, quality, stream);
	}

	@SuppressLint("NewApi")
	protected static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}

			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	protected static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}

	protected static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	protected static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	protected static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	protected static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}
}
