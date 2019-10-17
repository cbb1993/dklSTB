package com.huanhong.decathlonstb.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AppJsonFileReader {
	public static String getJson(Context context, String fileName) {

		StringBuilder stringBuilder = new StringBuilder();
		try {
			AssetManager assetManager = context.getAssets();
			BufferedReader bf = new BufferedReader(new InputStreamReader(
					assetManager.open(fileName)));
			String line;
			while ((line = bf.readLine()) != null) {
				stringBuilder.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return stringBuilder.toString();
	}

	public static void copyAssetFileToFiles(Context context, String filename) {

		try {
			InputStream is = context.getAssets().open(filename);
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			is.close();

			File of = new File(context.getFilesDir() + "/" + filename);
			of.createNewFile();
			FileOutputStream os = new FileOutputStream(of);
			os.write(buffer);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void writeToFiles(Context context, String data,
			String filename) {
		if (TextUtils.isEmpty(data))
			return;
		try {
			InputStream is = new ByteArrayInputStream(data.getBytes("utf-8"));
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			is.close();

			File of = new File(context.getFilesDir() + "/" + filename);
			of.createNewFile();
			FileOutputStream os = new FileOutputStream(of);
			os.write(buffer);
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getFileStr(Context context, String filename) {
		String data = null;
		try {
			FileInputStream inStream = context.openFileInput(filename);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();// 输出到内存
			int len = 0;
			byte[] buffer = new byte[1024];
			while ((len = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);//
			}
			byte[] content_byte = outStream.toByteArray();
			data = new String(content_byte);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return data;
		} catch (IOException e) {
			e.printStackTrace();
			return data;
		}
		return data;
	}
}
