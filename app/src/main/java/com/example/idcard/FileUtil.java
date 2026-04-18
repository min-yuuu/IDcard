package com.example.idcard;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.InputStream;

public class FileUtil {

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String result = null;
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                result = cursor.getString(column_index);
            }
        } catch (Exception e) {
            Log.e("FileUtil", "Error getting path", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public static byte[] getFileBytes(Context context, Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new Exception("无法打开文件流");
        }

        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        inputStream.close();
        return buffer;
    }
}