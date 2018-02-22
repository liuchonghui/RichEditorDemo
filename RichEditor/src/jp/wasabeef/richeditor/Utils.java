package jp.wasabeef.richeditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Xml;

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

public final class Utils {

  public static String toBase64(Bitmap bitmap) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
    byte[] bytes = baos.toByteArray();

    return Base64.encodeToString(bytes, Base64.NO_WRAP);
  }

  public static Bitmap toBitmap(Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }

    int width = drawable.getIntrinsicWidth();
    width = width > 0 ? width : 1;
    int height = drawable.getIntrinsicHeight();
    height = height > 0 ? height : 1;

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);

    return bitmap;
  }

  public static Bitmap decodeResource(Context context, int resId) {
    return BitmapFactory.decodeResource(context.getResources(), resId);
  }
	public static RichEditerContent parseContent(String xmlPath) {
//		List<RichEditerContent> contents = new ArrayList<RichEditerContent>();
		RichEditerContent content = null;
		InputStream inputStream = null;
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			inputStream = getStringStream(xmlPath);
			xmlParser.setInput(inputStream, "utf-8");
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT) {
				switch (evtType) {
				case XmlPullParser.START_TAG:
					String tag = xmlParser.getName();
					if (tag.equalsIgnoreCase("img")) {
						content = new RichEditerContent();
						String path = xmlParser.getAttributeValue(null, "src");
						int position = 0;
						String stattag = "file://";
						if(path.contains(stattag)){
							position =path.indexOf(stattag)+stattag.length();
						}
						path = path.substring(position);
						content.setSrc(path);
						content.setWidth(xmlParser.getAttributeValue(null,
								"width"));
						content.setHeight(xmlParser.getAttributeValue(null,
								"height"));
						content.setAlt(xmlParser.getAttributeValue(null, "alt"));
//						contents.add(content);
					}
					break;
				}

				evtType = xmlParser.next();

			}

		} catch (XmlPullParserException e) {

			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		} 

		return content;
	}
	public static InputStream getStringStream(String sInputString) {
		if (sInputString != null && !sInputString.trim().equals("")) {
			try {
				ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(
						sInputString.getBytes());
				return tInputStringStream;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}
}
