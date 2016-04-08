package com.huatune.mantanote.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

public class PhotoUtils {
    public static final String FILE_PROVIDER_AUTHORITY = "这里输入包名.files";
    private static final String PHOTO_DATE_FORMAT = "'IMG'_yyyyMMdd_HHmmss";

    public static final int GET_PHOTO_REQUESTCODE = 0;
    public static final int CROP_PHOTO_REQUESTCODE = 1;

    public static Uri PickResulTempUri;
    public static Uri CropResulTempUri;

    public static Intent getPickPhotoIntent(Uri resultUri) {
        // final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        // intent.setType("image/*");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // 用于启动指定包名的应用
        // intent.setPackage("com.huatune.gallery");
        addPhotoPickerExtras(intent, resultUri);
        // intent.putExtra(MediaStore.EXTRA_OUTPUT, resultUri);
        return intent;
    }

    @SuppressLint("NewApi")
    public static void addPhotoPickerExtras(Intent intent, Uri photoUri) {
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setClipData(ClipData
                .newRawUri(MediaStore.EXTRA_OUTPUT, photoUri));
    }

    public static Intent getCropPhotoIntent(Uri inputUri, Uri resultUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(inputUri, "image/*");
        // 设置可裁剪
        intent.putExtra("crop", true);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        // aspectX aspectY 是宽高的比例
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        // intent.putExtra("outputX", 720);
         intent.putExtra("outputY", 1080);
//         intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f))//
        intent.putExtra("return-data", false);
        // intent.setPackage("com.huatune.gallery");
        addPhotoPickerExtras(intent, resultUri);
        return intent;
    }

    public static Intent getTakePhotoIntent(Uri resultUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 用于启动指定包名的应用
        // intent.setPackage("com.huatune.gallery");
        // intent.putExtra(MediaStore.EXTRA_OUTPUT, resultUri);
        addPhotoPickerExtras(intent, resultUri);
        return intent;
    }

    public static Uri getPickResulTempUri(Context context) {
        // ContentValues values = new ContentValues();
        // values.put(Media.TITLE, "pick_result_tempfile");
        // return
        // context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        // values);
        return generateTempImageUri(context);
    }

    /**
     * Generate a new, unique file to be used as an out-of-band communication
     * channel, since hi-res Bitmaps are too big to serialize into a Bundle.
     * This file will be passed (as a uri) to other activities (such as the
     * gallery/camera/ cropper/etc.), and read by us once they are finished
     * writing it.
     */
    public static Uri generateTempImageUri(Context context) {
        return FileProvider
                .getUriForFile(context, FILE_PROVIDER_AUTHORITY, new File(
                        pathForTempPhoto(context, generateTempPhotoFileName())));
    }

    public static String generateTempPhotoFileName() {
        final Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(PHOTO_DATE_FORMAT,
                Locale.US);
        return "Photo-" + dateFormat.format(date) + ".jpg";
    }

    private static String pathForTempPhoto(Context context, String fileName) {
        final File dir = context.getCacheDir();
        dir.mkdirs();
        final File f = new File(dir, fileName);
        return f.getAbsolutePath();
    }

    public static Uri getCropResulTempUri(Context context) {
        // ContentValues values = new ContentValues();
        // values.put(Media.TITLE, "crop_result_tempfile");
        // return
        // context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        // values);
        return generateTempCroppedImageUri(context);
    }

    public static Uri generateTempCroppedImageUri(Context context) {
        return FileProvider.getUriForFile(
                context,
                FILE_PROVIDER_AUTHORITY,
                new File(pathForTempPhoto(context,
                        generateTempCroppedPhotoFileName())));
    }

    private static String generateTempCroppedPhotoFileName() {
        final Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(PHOTO_DATE_FORMAT,
                Locale.US);
        return "Photo-" + dateFormat.format(date) + "-cropped.jpg";
    }

    /**
     * Given an input photo stored in a uri, save it to a destination uri
     */
    public static boolean savePhotoFromUriToUri(Context context, Uri inputUri,
            Uri outputUri, boolean deleteAfterSave) {
        if (outputUri == null) {
            return false;
        }
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            outputStream = context.getContentResolver()
                    .openAssetFileDescriptor(outputUri, "rw")
                    .createOutputStream();
            inputStream = context.getContentResolver()
                    .openInputStream(inputUri);

            final byte[] buffer = new byte[16 * 1024];
            int length;
            int totalLength = 0;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
                totalLength += length;
            }
        } catch (IOException e) {
            return false;
        } finally {
            Closeables.closeQuietly(inputStream);
            Closeables.closeQuietly(outputStream);
            if (deleteAfterSave) {
                context.getContentResolver().delete(inputUri, null, null);
            }
        }
        return true;
    }

    public static void saveImgToLocalPath(Context context, Bitmap bitmap,
            String filePath) {
        Log.i("aaa", "saveImgToLocalPath = " + filePath);
        if (isSDCardIsReadly()) {
            File imgFile = new File(
                    filePath);
            if (!imgFile.getParentFile().exists()) {
                imgFile.getParentFile().mkdirs();
            }
            FileOutputStream fos = null;
            try {
                imgFile.createNewFile();
                fos = new FileOutputStream(imgFile);
                bitmap.compress(CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                Intent mediaScanIntent = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.parse("file://"
                        + imgFile.getAbsolutePath()));
                context.sendBroadcast(mediaScanIntent);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fos = null;
            }
        }
    }

    private static boolean isSDCardIsReadly() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

}
