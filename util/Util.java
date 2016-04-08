package com.example.baton.mayi;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by ouikan on 2016/4/8.
 */
public class Util {
    /**
     * 把Bitmap转换成byte数组
     * @param bmp
     * @param needRecycle
     * @return
     */
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        Bitmap bitmap = ThumbnailUtils.extractThumbnail(bmp, 100, 100);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
            bitmap.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 取出拼音首字母
     *
     * @param source
     * @return
     */
    static public String getFirstPinYin(String source) {
        if (source.equals("")) {
            return source;
        } else if (source.charAt(0) >= 33 && source.charAt(0) <= 127) {
            if ((source.charAt(0) >= 'a' && source.charAt(0) <= 'z') || (source.charAt(0) >= 'A' && source.charAt(0) <= 'Z')) {
                return (source.charAt(0) + "").toUpperCase();
            }
            return "#";
        }

        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(source);
        StringBuilder sb = new StringBuilder();
        if (tokens != null && tokens.size() > 0) {
            for (HanziToPinyin.Token token : tokens) {
                if (HanziToPinyin.Token.PINYIN == token.type) {
                    sb.append(token.target);
                } else {
                    sb.append(token.source);
                }
            }
        }
        if (sb.toString().equals("")) {
            return sb.toString();
        } else {
            return sb.toString().toUpperCase().substring(0, 1);
        }
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context
     *            Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    public static int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 获取mac地址
     *
     * @param context
     * @return
     */
    public static String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }
    /**
     * 时间字符串转换成时间戳
     * @param time
     * @param format
     * @return
     */
    public static Long getTime(String time,String format){
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat(format);
        Date date = null;
            try {
                date = simpleDateFormat .parse(time);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        return date.getTime();
    }

    public static String getTimeStr(long time,String format){
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        re_StrTime = sdf.format(new Date(time));
        return re_StrTime;
    }


}
