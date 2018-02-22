package jp.wasabeef.sample;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.android.overlay.RunningEnvironment;
import com.android.overlay.utils.LogUtils;
import com.sina.sinagame.windowattacher.FooterPopupAttacher;

/**
 * 
 * @author liu_chonghui
 * 
 */
public class ImageSelectWindowAttacher extends FooterPopupAttacher implements
		View.OnClickListener, UploadImageAttacher {

	public final int PHOTO_REQUEST_CUT = 10020;
	public final int PHOTO_REQUEST_TAKE = 10018;
	public final int PHOTO_REQUEST_PICK = 10019;
	protected boolean CROP = false;
	protected int pxScreenW = 0;
	protected int pxScreenH = 0;
	protected int dpScreenW = 0;
	protected int dpScreenH = 0;

	public void setCrop(boolean needCrop) {
		CROP = needCrop;
	}

	protected String getCacheDir() {
		return getFileDir() + "r_e" + File.separator;
	}

	protected String getFileType() {
		return ".jpeg";
	}

	public ImageSelectWindowAttacher(Activity attachedActivity) {
		this(attachedActivity, R.layout.imageselect_windowattacher,
				R.id.popup_animation_layout);
	}

	protected ImageSelectWindowAttacher(Activity attachedActivity,
			int layoutResId, int animationViewGroupId) {
		super(attachedActivity, layoutResId, animationViewGroupId);
		DisplayMetrics metrics = attachedActivity.getResources()
				.getDisplayMetrics();
		pxScreenW = metrics.widthPixels;
		pxScreenH = metrics.heightPixels;
		dpScreenW = px2dip(attachedActivity, pxScreenW);
		dpScreenH = px2dip(attachedActivity, pxScreenH);
		asyncClearFile(attachedActivity);
		setCrop(true);
	}

	TextView picture;
	TextView shoting;
	TextView cancel;

	@Override
	public void findViewByContentView(View contentView) {
		contentView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			}
		});
		picture = (TextView) contentView.findViewById(R.id.picture);
		picture.setOnClickListener(this);
		shoting = (TextView) contentView.findViewById(R.id.shoting);
		shoting.setOnClickListener(this);
		cancel = (TextView) contentView.findViewById(R.id.cancel);
		cancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		final int id = view.getId();
		if (R.id.picture == id) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			getAttachedActivity().startActivityForResult(intent,
					PHOTO_REQUEST_PICK);
			closePop();

		} else if (R.id.shoting == id) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			// intent.putExtra(MediaStore.EXTRA_OUTPUT,
			// Uri.fromFile(new
			// File(Environment.getExternalStorageDirectory(),
			// "header")));
			getAttachedActivity().startActivityForResult(intent,
					PHOTO_REQUEST_TAKE);
			closePop();

		} else if (R.id.cancel == id) {
			closePop();
		}
	}

	@Override
	public void adjustContentView(View contentView) {
		super.adjustContentView(contentView);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		String path = null;
		if (requestCode == PHOTO_REQUEST_CUT) {
			// 从剪切图片返回的数据
			if (data != null) {
				Bitmap bitmap = data.getParcelableExtra("data");
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				Date date = new Date();
				Long timestamp = date.getTime();
				path = saveFile(getAttachedActivity(), +timestamp
						+ getFileType(), bitmap);
				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
				}
				// TODO
				File file = new File(path);
				if (file != null && file.exists()) {
					LogUtils.d("ISWA", "onBitmapCaptured:" + path);
					captureBitmap(path, width, height);
				}
			}
		} else if (requestCode == PHOTO_REQUEST_TAKE) {
			Intent intent = new Intent();
			// 选择完或者拍完照后会在这里处理，然后我们继续使用setResult返回Intent以便可以传递数据和调用
			if (data.getExtras() != null) {
				intent.putExtras(data.getExtras());
			}
			if (data.getData() != null) {
				intent.setData(data.getData());
			}
			if (CROP) {
				cropPhotoForData(intent);
			} else {
				getPhotoForData(intent);
			}
		} else if (requestCode == PHOTO_REQUEST_PICK) {
			Intent intent = new Intent();
			// 选择完或者拍完照后会在这里处理，然后我们继续使用setResult返回Intent以便可以传递数据和调用
			if (data.getExtras() != null) {
				intent.putExtras(data.getExtras());
			}
			if (data.getData() != null) {
				intent.setData(data.getData());
			}
			if (CROP) {
				cropPhotoForData(intent);
			} else {
				getPhotoForData(intent);
			}
		}
	}

	protected void asyncClearFile(Context context) {
		String filePath = getCacheDir();
		if (filePath != null && filePath.length() > 0) {
			File dir = new File(filePath);
			if (dir == null || !dir.exists()) {
				dir.mkdir();
				return;
			} else {
				final Long now = new Date().getTime();
				final Long yesterday = now - 24 * 60 * 60 * 1000L;
				File[] files = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File file, String filename) {
						if (filename.endsWith(getFileType())) {
							String name = filename.replace(getFileType(), "");
							Long time = now;
							try {
								time = Long.parseLong(name);
							} catch (Exception e) {
								time = yesterday;
							}
							if (time <= yesterday) {
								return true;
							}
						}
						return false;
					}
				});
				if (files != null && files.length > 0) {
					for (File file : files) {
						if (file != null && file.exists()) {
							file.delete();
						}
					}
				}
			}
		}
	}

	protected void getPhotoForData(Intent intent) {
		Uri imageCaptureUri = intent.getData();
		String path = null;
		if (imageCaptureUri == null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				Bitmap bitmap = extras.getParcelable("data");
				if (bitmap != null) {
					imageCaptureUri = Uri.parse(MediaStore.Images.Media
							.insertImage(getAttachedActivity()
									.getContentResolver(), bitmap, null, null));
					bitmap.recycle();
				}
			}
		}
		if (imageCaptureUri != null) {
			path = getPath(getAttachedActivity(), imageCaptureUri);
			Bitmap bitmap = decodeBitmapFromPath(path);
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			int wh[] = computeImageWH(getAttachedActivity(), width, height);
			Date date = new Date();
			Long timestamp = date.getTime();
			path = saveFile(getAttachedActivity(), +timestamp + getFileType(),
					bitmap);
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
			}
			// TODO
			File file = new File(path);
			if (file != null && file.exists()) {
				LogUtils.d("ISWA", "onBitmapCaptured:" + path);
				captureBitmap(path, wh[0], wh[1]);
			}
		}
	}

	/**
	 * 剪裁获取到的图片
	 * 
	 * @param data
	 */
	protected void cropPhotoForData(Intent data) {
		if (data != null) {
			// 取得返回的Uri,基本上选择照片的时候返回的是以Uri形式，但是在拍照中有得机子呢Uri是空的，所以要特别注意
			Uri mImageCaptureUri = data.getData();
			// 返回的Uri不为空时，那么图片信息数据都会在Uri中获得。如果为空，那么我们就进行下面的方式获取
			if (mImageCaptureUri != null) {
				crop(mImageCaptureUri);
			} else {
				Bundle extras = data.getExtras();
				if (extras != null) {
					// 这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片
					Bitmap image = extras.getParcelable("data");
					if (image != null) {
						Uri uri = Uri.parse(MediaStore.Images.Media
								.insertImage(getAttachedActivity()
										.getContentResolver(), image, null,
										null));
						crop(uri);
						image.recycle();
					}
				}
			}

		}
	}

	/**
	 * 剪切图片
	 * 
	 * @param uri
	 */
	protected void crop(Uri uri) {
		// 裁剪图片意图
		Intent intent = new Intent("com.android.camera.action.CROP");
		// intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		// // 裁剪框的比例，1：1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// 裁剪后输出图片的尺寸大小
		intent.putExtra("outputX", 250);
		intent.putExtra("outputY", 250);

		intent.putExtra("outputFormat", "JPEG");// 图片格式
		intent.putExtra("noFaceDetection", true);// 取消人脸识别
		intent.putExtra("return-data", true);

		String path = getPath(getAttachedActivity(), uri);
		Bitmap bitmap = decodeBitmapFromPath(path);

		String strUri = MediaStore.Images.Media.insertImage(
				getAttachedActivity().getContentResolver(), bitmap, null, null);
		// if (!bitmap.isRecycled()) {
		// bitmap.recycle();
		// }
		if (strUri != null && strUri.length() > 0) {
			uri = Uri.parse(strUri);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			path = getPath(getAttachedActivity(), uri);
			intent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
		} else {
			path = getPath(getAttachedActivity(), uri);
			intent.setDataAndType(uri, "image/*");
		}

		// 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
		getAttachedActivity().startActivityForResult(intent, PHOTO_REQUEST_CUT);
	}

	protected Bitmap decodeBitmapFromPath(String path) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		int degree = readPictureDegree(path);
		int sampleSize = checkNeedCompress(path);
		if (sampleSize > 0) {
			options.inSampleSize = sampleSize;
		}
		options.inJustDecodeBounds = false;
		// 调用上面定义的方法计算inSampleSize值
		// 使用获取到的inSampleSize值再次解析图片
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		Bitmap newbitmap = rotaingImageView(degree, bitmap);
		// if(!bitmap.isRecycled())
		// bitmap.recycle();
		return newbitmap;
	}

	/**
	 * 读取图片属性：旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return degree旋转的角度
	 */
	protected int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 检测图片是否需要压缩
	 * 
	 * @param path
	 * @return
	 */
	protected int checkNeedCompress(String path) {
		int sampleSize = 0;
		int maxMemory = (int) (Runtime.getRuntime().maxMemory());
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		int imageHeight = options.outHeight;
		int imageWidth = options.outWidth;
		int picSize = imageWidth * imageHeight * 4;
		int memorySize = maxMemory / 8;
		if (picSize > memorySize) {
			sampleSize = (int) Math.ceil(picSize / memorySize);
			if (sampleSize < 2)
				sampleSize = 2;
		}
		return sampleSize;
	}

	/**
	 * 旋转图片
	 * 
	 * @param angle
	 * 
	 * @param bitmap
	 * 
	 * @return Bitmap
	 */
	protected Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

	protected void captureBitmap(final String filepath, final int width,
			final int height) {
		RunningEnvironment.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (listener != null) {
					listener.onBitmapCaptured(filepath, width, height);
				} else {
					onBitmapCaptured(filepath, width, height);
				}
			}
		});
	}
	
	OnBitmapCapturedListener listener;
	
	public void setOnBitmapCapturedListener(OnBitmapCapturedListener l) {
		this.listener = l;
	}

	public void onBitmapCaptured(String filepath, int width, int height) {
	}

	protected String saveFile(Context c, String fileName, Bitmap bitmap) {
		return saveFile(c, "", fileName, bitmap);
	}

	protected String saveFile(Context c, String filePath, String fileName,
			Bitmap bitmap) {
		byte[] bytes = bitmapToBytes(bitmap);
		return saveFile(c, filePath, fileName, bytes);
	}

	protected byte[] bitmapToBytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(CompressFormat.JPEG, 100, baos);
		return baos.toByteArray();
	}

	protected String saveFile(Context c, String filePath, String fileName,
			byte[] bytes) {
		String fileFullName = "";
		FileOutputStream fos = null;
		try {
			String suffix = "";
			if (filePath == null || filePath.trim().length() == 0) {
				filePath = getCacheDir();
			}
			File file = new File(filePath);
			if (!file.exists()) {
				file.mkdirs();
			}
			File fullFile = new File(filePath, fileName + suffix);
			fileFullName = fullFile.getPath();
			fos = new FileOutputStream(new File(filePath, fileName + suffix));
			fos.write(bytes);
		} catch (Exception e) {
			fileFullName = "";
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					fileFullName = "";
				}
			}
		}
		return fileFullName;
	}

	@SuppressLint("NewApi")
	protected String getPath(final Context context, final Uri uri) {

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

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	protected boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	protected boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	protected boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	protected boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	protected String getDataColumn(Context context, Uri uri, String selection,
			String[] selectionArgs) {

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

	protected String getFileDir() {
		String dir = getAttachedActivity().getFilesDir().getAbsolutePath()
				+ File.separator;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String dowload = android.os.Environment.DIRECTORY_DOWNLOADS;
			File downloadDir = getAttachedActivity().getExternalFilesDir(
					dowload);
			if (downloadDir != null) {
				if (!downloadDir.exists()) {
					downloadDir.mkdirs();
				}
				dir = downloadDir.getAbsolutePath() + File.separator;
			}
		}
		return dir;
	}

	protected int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	protected int[] computeImageWH(Context context, int width, int height) {
		int[] wh = new int[2];
		float density = context.getResources().getDisplayMetrics().density;
		if (density > 1) {
			width = (int) (width * density);
			height = (int) (height * density);
		}
		int dp_width = px2dip(context, width);
		int dp_height = px2dip(context, height);
		if (dp_width < dpScreenW && dp_height < dpScreenH) {
			wh[0] = dp_width;
			wh[1] = dp_height;
		} else {
			int image_w = dpScreenW - 40;
			int image_h = image_w * dp_height / dp_width;
			wh[0] = image_w;
			wh[1] = image_h;
		}
		return wh;
	}
}
