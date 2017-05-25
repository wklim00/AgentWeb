package com.just.library;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.design.widget.Snackbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <b>@项目名：</b> agentweb<br>
 * <b>@包名：</b>com.just.library<br>
 * <b>@创建者：</b> cxz --  just<br>
 * <b>@创建时间：</b> &{DATE}<br>
 * <b>@公司：</b> 宝诺科技<br>
 * <b>@邮箱：</b> cenxiaozhong.qqcom@qq.com<br>
 * <b>@描述</b><br>
 * source code  https://github.com/Justson/AgentWeb
 */

public class AgentWebUtils {

    public static int px2dp(Context context, float pxValue) {

        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dp2px(Context context, float dipValue) {

        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }


    public static final void clearWebView(WebView m) {

        if (m == null)
            return;
        if (Looper.myLooper() != Looper.getMainLooper())
            return;
        m.getHandler().removeCallbacksAndMessages(null);
        m.removeAllViews();
        ((ViewGroup) m.getParent()).removeView(m);
        m.setTag(null);
        m.clearHistory();
        m.destroy();
        m = null;

    }

    public static boolean checkWifi(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }
        NetworkInfo info = connectivity.getActiveNetworkInfo();
        return info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean checkNetwork(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }
        NetworkInfo info = connectivity.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public static long getAvailableStorage() {
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                return stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
            } else {
                return (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
            }
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    public static Intent getFileIntent(File file) {
//       Uri uri = Uri.parse("http://m.ql18.com.cn/hpf10/1.pdf");
        Uri uri = Uri.fromFile(file);
        String type = getMIMEType(file);
        Log.i("tag", "type=" + type);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, type);
        return intent;
    }

    private static String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
      /* 取得扩展名 */
        String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();

      /* 依扩展名的类型决定MimeType */
        if (end.equals("pdf")) {
            type = "application/pdf";//
        } else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") ||
                end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            type = "audio/*";
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = "video/*";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") ||
                end.equals("jpeg") || end.equals("bmp")) {
            type = "image/*";
        } else if (end.equals("apk")) {
        /* android.permission.INSTALL_PACKAGES */
            type = "application/vnd.android.package-archive";
        }
//      else if(end.equals("pptx")||end.equals("ppt")){
//        type = "application/vnd.ms-powerpoint";
//      }else if(end.equals("docx")||end.equals("doc")){
//        type = "application/vnd.ms-word";
//      }else if(end.equals("xlsx")||end.equals("xls")){
//        type = "application/vnd.ms-excel";
//      }
        else {
//        /*如果无法直接打开，就跳出软件列表给用户选择 */
            type = "*/*";
        }
        return type;
    }


    private static WeakReference<Snackbar> snackbarWeakReference;

    public static void show(View parent,
                            CharSequence text,
                            int duration,
                            @ColorInt int textColor,
                            @ColorInt int bgColor,
                            CharSequence actionText,
                            @ColorInt int actionTextColor,
                            View.OnClickListener listener) {
        SpannableString spannableString = new SpannableString(text);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(textColor);
        spannableString.setSpan(colorSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        snackbarWeakReference = new WeakReference<>(Snackbar.make(parent, spannableString, duration));
        Snackbar snackbar = snackbarWeakReference.get();
        View view = snackbar.getView();
        view.setBackgroundColor(bgColor);
        if (actionText != null && actionText.length() > 0 && listener != null) {
            snackbar.setActionTextColor(actionTextColor);
            snackbar.setAction(actionText, listener);
        }
        snackbar.show();
    }

    public static void dismiss() {
        if (snackbarWeakReference != null && snackbarWeakReference.get() != null) {
            snackbarWeakReference.get().dismiss();
            snackbarWeakReference = null;
        }
    }

    public static boolean isOverriedMethod(Object currentObject, String methodName, String method, Class... clazzs) {
//        Log.i("Info","currentObject:"+currentObject+"  methodName:"+methodName+"   method:"+method);
        boolean tag = false;
        if (currentObject == null)
            return tag;

        try {

            Class clazz = currentObject.getClass();
            Method mMethod = clazz.getMethod(methodName, clazzs);
            String gStr = mMethod.toGenericString();

//            Log.i("Info","gstr:"+gStr+"  method:"+method);
            tag = !gStr.contains(method);
        } catch (Exception igonre) {
//                igonre.printStackTrace();
        }


        return tag;
    }


    public static void clearWebViewAllCache(Context context, WebView webView) {

        try {
            webView.clearCache(true);
            webView.clearHistory();
            webView.clearFormData();
            AgentWebConfig.removeAllCookies(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearWebViewAllCache(Context context) {

        try {

            clearWebViewAllCache(context, new WebView(context.getApplicationContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //// 来源stackflow
    static int clearCacheFolder(final File dir, final int numDays) {

        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {

                    //first delete subdirectories recursively
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }

                    //then delete the files and subdirectories in this dir
                    //only empty directories can be deleted, so subdirs have been done first
                    if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Info", String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }
        return deletedFiles;
    }

    public static String FileParcetoJson(FileParcel[] fileParcels){

        if(fileParcels==null||fileParcels.length==0)
            return null;

        Log.i("Info","json:"+new JSONArray(Arrays.asList(fileParcels)).toString());
        return new JSONArray(Arrays.asList(fileParcels)).toString();


    }

    public static String FileParcetoJson(Collection<FileParcel> collection){

        if(collection==null||collection.size()==0)
            return null;



        Iterator<FileParcel> mFileParcels=collection.iterator();
        JSONArray mJSONArray=new JSONArray();
        try{
            while(mFileParcels.hasNext()){
                JSONObject jo=new JSONObject();
                FileParcel mFileParcel=mFileParcels.next();

                jo.put("contentPath",mFileParcel.getContentPath());
                jo.put("fileBase64",mFileParcel.getFileBase64());
                jo.put("id",mFileParcel.getId());
                mJSONArray.put(jo);
            }

        }catch (Exception e){

        }


//        Log.i("Info","json:"+mJSONArray);
        return mJSONArray+"";


    }

    /*
     * Delete the files older than numDays days from the application cache
     * 0 means all files.
     *
     * // 来源stackflow
     */
    public static void clearCache(final Context context, final int numDays) {
        Log.i("Info", String.format("Starting cache prune, deleting files older than %d days", numDays));
        int numDeletedFiles = clearCacheFolder(context.getCacheDir(), numDays);
        Log.i("Info", String.format("Cache pruning completed, %d files deleted", numDeletedFiles));
    }


    //必须执行在子线程, 会阻塞 直到完成;
    public static Queue<FileParcel> convertFile(Context context, Uri[] uris) throws Exception {

        if (uris == null || uris.length == 0)
            return null;
        Executor mExecutor = Executors.newCachedThreadPool();
        final Queue<FileParcel> mQueue = new LinkedBlockingQueue<>();
        CountDownLatch mCountDownLatch = new CountDownLatch(uris.length);

        int i=1;
        for (Uri mUri : uris) {

            String path = getRealFilePath(context, mUri);
//            Log.i("Info","path:"+path+"  uri:"+mUri);
            if (TextUtils.isEmpty(path)){
                mCountDownLatch.countDown();
                continue;
            }

            mExecutor.execute(new EncodeFileRunnable(path, mQueue, mCountDownLatch,i++));

        }
        mCountDownLatch.await();

        return mQueue;
    }


    static class EncodeFileRunnable implements Runnable {

        private String filePath;
        private Queue<FileParcel> mQueue;
        private CountDownLatch mCountDownLatch;
        private int id;

        public EncodeFileRunnable(String filePath, Queue<FileParcel> queue, CountDownLatch countDownLatch,int id) {
            this.filePath = filePath;
            this.mQueue = queue;
            this.mCountDownLatch = countDownLatch;
            this.id=id;
        }


        @Override
        public void run() {
            InputStream is = null;
            ByteArrayOutputStream os=null;
            try {
                File mFile = new File(filePath);
                if (mFile.exists()) {

                    is = new FileInputStream(mFile);
                    if (is == null)
                        return;

                    os= new ByteArrayOutputStream();
                    byte[] b = new byte[1024];
                    int len;
                    while ((len = is.read(b, 0, 1024)) != -1) {
                        os.write(b, 0, len);
                    }
                    mQueue.offer(new FileParcel(id,mFile.getAbsolutePath(),Base64.encodeToString(os.toByteArray(), Base64.DEFAULT)));

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                CloseUtils.closeIO(is);
                CloseUtils.closeIO(os);
                mCountDownLatch.countDown();
            }


        }
    }

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

}