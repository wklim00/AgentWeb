package com.just.agentweb.filechooser;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UriConverter {
  public static Uri[] convert(ContentResolver contentResolver, Context context, Uri[] datas) {
    List<Uri> list = new ArrayList<>();
    for (int i = 0; i < datas.length; i++) {
      try {
        String fileName = prepareFileUri(context, datas[i]);
        list.add(Uri.parse("file://" + fileName));
      } catch (Throwable throwable) {
        System.out.println(throwable);
      }
    }
    return list.toArray(new Uri[list.size()]);
  }

  private static String prepareFileUri(Context context, Uri uri) {
    File dir = context.getCacheDir();

    // we don't want to rename the file so we put it into a unique location
    dir = new File(dir, UUID.randomUUID().toString());
    try {
      boolean didCreateDir = dir.mkdir();
      if (!didCreateDir) {
        throw new IOException("failed to create directory at " + dir.getAbsolutePath());
      }
      String fileName = getFileName(context.getContentResolver(),uri);
      if(fileName == null){
        fileName =  String.valueOf(System.currentTimeMillis()) +  getExt(context,uri);
      }

      File destFile = new File(dir, fileName);
      if(destFile.exists()){
        destFile.delete();
      }
      return  copyFile(context, uri, destFile);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static String getFileName(ContentResolver contentResolver, Uri uri) {
    String result = null;
    if (uri.getScheme().equals("content")) {
      Cursor cursor = contentResolver.query(uri, null, null, null, null);
      try {
        if (cursor != null && cursor.moveToFirst()) {
          result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }
      } finally {
        if (cursor != null) {
          cursor.close();
        }
      }
    }
    if (result == null) {
      result = uri.getLastPathSegment();
    }
    return result;
  }

  private static String getExt(Context context,Uri uri){
    String extension;
    //Check uri format to avoid null
    if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
      //If scheme is a content
      final MimeTypeMap mime = MimeTypeMap.getSingleton();
      extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
    } else {
      //If scheme is a File
      //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
      extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
    }
    return extension;
  }


  private static String copyFile(Context context, Uri uri, File destFile) throws IOException {
    InputStream in = null;
    FileOutputStream out = null;
    try {
      in = context.getContentResolver().openInputStream(uri);
      if (in != null) {
        out = new FileOutputStream(destFile);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
          out.write(buffer, 0, len);
        }
        out.close();
        in.close();
        return destFile.getAbsolutePath();
      } else {
        throw new NullPointerException("Invalid input stream");
      }
    } catch (Exception e) {
      try {
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
      } catch (IOException ignored) {
      }
      throw e;
    }
  }
}
