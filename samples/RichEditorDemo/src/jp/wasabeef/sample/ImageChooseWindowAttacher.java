package jp.wasabeef.sample;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
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
public class ImageChooseWindowAttacher extends FooterPopupAttacher implements
		View.OnClickListener, UploadImageAttacher {

	protected int pxScreenW = 0;
	protected int pxScreenH = 0;
	protected int dpScreenW = 0;
	protected int dpScreenH = 0;
	protected int padding = 10 + 10; // leftMargin:10/rightMargin:10

	protected String getCacheDir() {
		return getFileDir() + "r_e" + File.separator;
	}

	protected String getFileType() {
		return ".jpeg";
	}

	public ImageChooseWindowAttacher(Activity attachedActivity) {
		this(attachedActivity, R.layout.imageselect_windowattacher,
				R.id.popup_animation_layout);
	}

	protected ImageChooseWindowAttacher(Activity attachedActivity,
			int layoutResId, int animationViewGroupId) {
		super(attachedActivity, layoutResId, animationViewGroupId);
		getCacheDir();
		DisplayMetrics metrics = attachedActivity.getResources()
				.getDisplayMetrics();
		pxScreenW = metrics.widthPixels;
		pxScreenH = metrics.heightPixels;
		dpScreenW = px2dip(attachedActivity, pxScreenW);
		dpScreenH = px2dip(attachedActivity, pxScreenH);
		asyncClearFile(attachedActivity);
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

	private static final int PICTURE_REQUEST = 4;
	private static final int CAMERA_REUQEST = 1;
	File picImg;
	Uri outputFileUri;

	@Override
	public void onClick(View view) {
		final int id = view.getId();
		if (R.id.picture == id) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			getAttachedActivity().startActivityForResult(intent,
					PICTURE_REQUEST);
			closePop();

		} else if (R.id.shoting == id) {
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			picImg = new File(getCacheDir(), "temp.jpeg");
			if (picImg != null && !picImg.exists()) {
				try {
					picImg.createNewFile();
				} catch (Exception e) {
					picImg = null;
				}
			}
			outputFileUri = Uri.fromFile(new File(getCacheDir(), "temp.jpeg"));
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			getAttachedActivity().startActivityForResult(cameraIntent,
					CAMERA_REUQEST);
			closePop();

		} else if (R.id.cancel == id) {
			closePop();
		}
	}

	@Override
	public void adjustContentView(View contentView) {
		super.adjustContentView(contentView);
	}

	int width;
	int height;
	int from;

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		String savePath = null;
		if (resultCode == Activity.RESULT_OK) {
			if (CAMERA_REUQEST == requestCode || PICTURE_REQUEST == requestCode) {
				from = requestCode;
				savePath = handleImageResult(getAttachedActivity(),
						requestCode, resultCode, data);
			}
		}
		if (savePath != null && savePath.length() > 0) {
			File file = new File(savePath);
			if (file != null && file.exists()) {
				LogUtils.d("ICWA", "onBitmapCaptured:" + savePath);
				int targetWidth;
				int targetHeight;
				if (CAMERA_REUQEST == from) {
					if (width > dpScreenW - padding) {
						targetWidth = dpScreenW - padding;
						targetHeight = targetWidth * height / width;
					} else {
						targetWidth = width;
						targetHeight = height;
					}
				} else {
					targetWidth = dpScreenW - padding;
					targetHeight = targetWidth * height / width;
				}
				captureBitmap(savePath, targetWidth, targetHeight);
			}
		}
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

	public String handleImageResult(Context context, int requestCode,
			int resultCode, Intent intent) {
		String savePath = null;
		int rotation = 0;
		Bitmap bitmap = null;

		if (Activity.RESULT_OK != resultCode) {
			return savePath;
		}
		if (CAMERA_REUQEST != requestCode && intent == null) {
			return savePath;
		}
		if (CAMERA_REUQEST == requestCode && intent == null
				&& outputFileUri != null) {
			try {
				bitmap = Media.getBitmap(context.getContentResolver(), outputFileUri);
				FileOutputStream out = new FileOutputStream(picImg);  
				bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);  
				bitmap.recycle();
			} catch (Exception e) {
				e.printStackTrace();
				bitmap = null;
			}
			savePath = savePicture(outputFileUri.getPath());
			return savePath;
		}

		ImageMime mime = ImageMime.getMediaStoreData(context, intent);
		savePath = mime.getFilePath();
		rotation = mime.getDegrees();

		savePath = savePicture(savePath);
		if (rotation > 0) {
			bitmap = resizeAndRotateBitmap(savePath, rotation);
			if (bitmap != null && !bitmap.isRecycled()) {
				savePath = savePicture(bitmap);
			}
		}
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}

		return savePath;
	}

	public String savePicture(String filePath) {
		if (filePath == null || filePath.length() == 0) {
			return filePath;
		}
		Bitmap bitmap = null;
		File file = new File(filePath);
		if (file != null && file.exists()) {
			if (CAMERA_REUQEST == from) {
				bitmap = decodeBitmapFromPath(filePath);
				int width = 0;
				if (bitmap != null) {
					width = bitmap.getWidth();
				}
				if (width > pxScreenW / 2) {
					bitmap = ScaleFitX(bitmap, pxScreenW / 2);
				}
				
			} else {
				bitmap = ScaleFitX(decodeBitmapFromPath(filePath),
						pxScreenW / 2);
			}
		}
		return savePicture(bitmap);
	}

	public String savePicture(Bitmap bm) {
		if (bm == null || bm.isRecycled()) {
			return null;
		}
		width = bm.getWidth();
		height = bm.getHeight();
		File storageDir = null;
		storageDir = new File(getCacheDir());

		if (!storageDir.exists()) {
			storageDir.mkdirs();
		}

		File outFile = new File(storageDir + "/" + System.currentTimeMillis()
				+ ".jpeg");
		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(outFile));
			bm.compress(Bitmap.CompressFormat.JPEG, 80, bos); /* 采用压缩转档方法 */
			bos.flush();
			bos.close();
		} catch (Exception e) {
		}
		if (outFile != null && outFile.exists()) {
			if (bm != null && !bm.isRecycled()) {
				bm.recycle();
				bm = null;
			}
		}
		return outFile.getAbsolutePath();
	}

	public Bitmap resizeAndRotateBitmap(String filePath, int degrees) {
		if (filePath == null || filePath.length() == 0) {
			return null;
		}
		Bitmap bitmap = null;
		File file = new File(filePath);
		if (file != null && file.exists()) {
			bitmap = decodeBitmapFromPath(filePath);
		}
		Bitmap tmp = null;
		if (bitmap != null && !bitmap.isRecycled()) {
			tmp = rotateAndScale(bitmap, degrees, 1f);
		}
		if (tmp != null && !tmp.isRecycled()) {
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
			return tmp;
		}
		return bitmap;
	}

	public Bitmap rotateAndScale(Bitmap source, int degrees, float scale) {
		if (source == null || source.isRecycled()) {
			return null;
		}

		Matrix matrix = new Matrix();
		if (degrees > 0) {
			matrix.setRotate(degrees);
		}
		if (Float.compare(scale, 1f) != 0) {
			matrix.setScale(scale, scale);
		}

		Bitmap output = null;
		int count = 0;
		do {
			count++;
			try {
				output = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
						source.getHeight(), matrix, true);
			} catch (OutOfMemoryError ex) {
				ex.printStackTrace();
			}
		} while (output == null && count <= 3);

		if (output != source) {
			source.recycle();
			source = null;
		}
		return output;
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

	public Bitmap decodeBitmapFromPath(String pathName) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		int sampleSize = checkNeedCompress(pathName);
		if (sampleSize > 0) {
			options.inSampleSize = sampleSize;
		}
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);
		return bitmap;
	}

	public int checkNeedCompress(String path) {
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

	public Bitmap ScaleFitX(Bitmap source, int targetWidth) {
		if (source == null || targetWidth == 0) {
			return source;
		}
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		int targetHeight = targetWidth * sourceHeight / sourceWidth;
		if (targetHeight < 0) {
			return source;
		}

		return resize(source, targetWidth, targetHeight);
	}

	public Bitmap resize(Bitmap source, int newWidth, int newHeight) {
		if (source == null || source.isRecycled()) {
			return null;
		}

		Matrix matrix = new Matrix();
		int originalWidth = source.getWidth();
		int orginalHeight = source.getHeight();
		matrix.postScale(((float) newWidth / originalWidth),
				((float) newHeight / orginalHeight));

		Bitmap output = null;
		int count = 0;
		do {
			count++;
			try {
				output = Bitmap.createBitmap(source, 0, 0, originalWidth,
						orginalHeight, matrix, false);
			} catch (OutOfMemoryError ex) {
				ex.printStackTrace();
			}
		} while (output == null && count <= 3);
		if (output != null && !source.equals(output)) {
			source.recycle();
		}
		return output;
	}
}
